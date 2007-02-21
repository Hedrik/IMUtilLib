/*
 * Conduit.java
 * 
 * Created on Apr 11, 2004
 * 
 * $Revision$
 */

/*
 * 
 * Part of the "Information Montage Utility Library," a project from
 * Information Montage. Copyright (C) 2004 Richard A. Mead
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package com.InfoMontage.net;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import com.InfoMontage.common.Defaults;
import com.InfoMontage.math.BigCounter;
import com.InfoMontage.task.Task;
import com.InfoMontage.task.TaskExecutor;
import com.InfoMontage.task.TaskExecutorPool;
import com.InfoMontage.util.AssertableLogger;
import com.InfoMontage.util.BooleanState;
import com.InfoMontage.version.CodeVersion;

/**
 * An abstraction of communications that hides some of the low-level drudgery of
 * polling {@link Socket}s: periodically checking connectivity, breaking up I/O
 * into "reasonable" size {@link Packet}s and reassembling them on the
 * receiving side, retransmission of {@link Packet}s upon timeout, maintaining
 * I/O statistics, etc. Will work with {@link Socket}s, {@link SSLSocket}s,{@link DatagramSocket}s,
 * and any other class that implements both the {@link SelectableChannel}and
 * the {@link ByteChannel} interfaces.
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public final class Conduit extends AbstractSelectableChannel {

    /**
	 * {@link Logger}for this class.
	 */
    private static final AssertableLogger log = new AssertableLogger(
	    Conduit.class.getName());

    /**
	 * Code version for the Conduit class. Determined from CVS file
	 * revision.
	 */
    public static CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    /**
	 * Collection of all instantiated Conduits.
	 */
    protected static List conduits = new Vector();

    /**
	 * An internal object used for synchronization of access to the
	 * collection of instantiated Conduits and it's index variable.
	 */
    protected static final Object lockForNextConduitToCheck = new Object();

    /**
	 * An index into the collection of instantiated Conduits.
	 */
    protected static int nextConduitToCheck = 0;

    protected volatile BooleanState beingProcessed = new BooleanState(false);

    /**
	 * Maximum number of {@link Thread}s allocated for receiving data from
	 * all Sockets.
	 */
    private static final int MAX_RECEIVER_TASK_THREADS = 4;

    /**
	 * Array of {@link Task}s used by the receiver threads.
	 */
    protected static final ReceiverTask RECEIVE_TASKS[] = new ReceiverTask[MAX_RECEIVER_TASK_THREADS];

    /**
	 * The pool of {@link TaskExecutor}s used to process ReceiverTasks.
	 */
    protected static final TaskExecutorPool RECEIVER_TASK_FACTORY = TaskExecutorPool
	    .getPool("ConduitReceivers", 1, MAX_RECEIVER_TASK_THREADS, false);

    /**
	 * Master {@link Thread}monitoring all instantiated Conduits for
	 * available data.
	 *
	 * @author Richard A. Mead <BR>
	 *         Information Montage
	 */
    private static class ConduitMonitorThread extends Thread {

	/**
	 * {@link Logger}for this class.
	 */
	private static final AssertableLogger log = new AssertableLogger(
		ConduitMonitorThread.class.getName());

	/**
	 * The constructor names the thread.
	 */
	private ConduitMonitorThread() {
	    super("Conduit monitor");
	}

	/**
	 * Monitoring code lives here.
	 *
	 * @see java.lang.Thread#run()
	 */
	public void run() {
	    int i;
	    assert (log.info("Conduit monitoring thread starting."));
	    while (true) {
		assert (log.gettingLock(Conduit.conduits));
		synchronized (Conduit.conduits) {
		    assert (log.gotLock(Conduit.conduits));
		    if (!Conduit.conduits.isEmpty()) {
			assert (log
				.info("Conduit monitor thread beginning task check."));
			for (i = 0; i < Conduit.MAX_RECEIVER_TASK_THREADS; i++) {
			    if (null != Conduit.RECEIVE_TASKS[i]) {
				if (!Conduit.RECEIVE_TASKS[i].isProcessing()) {
				    break;
				}
			    } else {
				Conduit.RECEIVE_TASKS[i] = new ReceiverTask();
				break;
			    }
			}
			if (i < Conduit.MAX_RECEIVER_TASK_THREADS) {
			    Conduit ctc;
			    assert (log
				    .finer("Conduit monitor thread has an available task to use. (#"
					    + i + ")"));
			    assert (log
				    .gettingLock(Conduit.lockForNextConduitToCheck));
			    synchronized (Conduit.lockForNextConduitToCheck) {
				assert (log
					.gotLock(Conduit.lockForNextConduitToCheck));
				assert (log
					.finer("Conduit monitor checking conduit #"
						+ Conduit.nextConduitToCheck
						+ 1 + "."));
				ctc = (Conduit) Conduit.conduits
					.get(Conduit.nextConduitToCheck);
				Conduit.nextConduitToCheck++;
				if (Conduit.nextConduitToCheck >= Conduit.conduits
					.size()) {
				    Conduit.nextConduitToCheck = 0;
				}
			    }
			    assert (log
				    .releasedLock(Conduit.lockForNextConduitToCheck));
			    // No need for sync locks here since this is the
			    // only thread that can initiate processing of a
			    // Conduit.
			    if (!ctc.beingProcessed.getState()) {
				ctc.beingProcessed.setState(true);
				try {
				    Conduit.RECEIVER_TASK_FACTORY.doTask(
					    Conduit.RECEIVE_TASKS[i],
					    new Object[] { ctc }, false);
				} catch (InterruptedException e) {
				    // TODO Close the conduit??
				}
			    }
			}
		    }
		}
		assert (log.releasedLock(Conduit.conduits));
		boolean shouldSleep = false;
		assert (log.gettingLock(Conduit.lockForNextConduitToCheck));
		synchronized (Conduit.lockForNextConduitToCheck) {
		    assert (log.gotLock(Conduit.lockForNextConduitToCheck));
		    shouldSleep = (Conduit.nextConduitToCheck == 0);
		}
		assert (log.releasedLock(Conduit.lockForNextConduitToCheck));
		if (shouldSleep) {
		    try {
			assert (log.finest("Conduit monitor thread sleeping."));
			Thread.sleep(100);
		    } catch (InterruptedException e) {
			// TODO Handle interruptions of the monitor
			// thread.
			assert (log.throwing(e));
		    }
		    assert (log.finest("Conduit monitor thread woke up."));
		}
	    }
	}
    }

    /**
	 * The instantiation of the master monitor {@link Thread}.
	 */
    private static final ConduitMonitorThread cmt = new ConduitMonitorThread();
    static {
	cmt.start();
    }

    /**
	 * The {@link Task}used to receive data.
	 *
	 * @author Richard A. Mead <BR>
	 *         Information Montage
	 */
    public static class ReceiverTask implements Task {

	/**
	 * {@link Logger}for this class.
	 */
	private static final AssertableLogger log = new AssertableLogger(
		ReceiverTask.class.getName());

	private Object[] params;

	/**
	 * Parameter validation routine for ReceiverTasks.
	 *
	 * @see com.InfoMontage.task.ExecutableTask#validateParameters(java.lang.Object[])
	 */
	public Exception validateParameters(Object[] pa) {
	    Exception retVal = null;
	    Conduit c = null;
	    try {
		c = (Conduit) pa[0];
	    } catch (ClassCastException e) {
		retVal = (IllegalArgumentException) new IllegalArgumentException(
			"Attempt" + " to set ReceiverTask parameter to a "
				+ pa[0].getClass() + " instead of a Conduit!")
			.initCause(e);
	    } catch (NullPointerException e) {
		retVal = (NullPointerException) new NullPointerException(
			"Attempt to set ReceiverTask parameter to null instead of"
				+ " a Conduit!").initCause(e);
	    }
	    if (null == c) {
		retVal = new NullPointerException(
			"Attempt to set ReceiverTask parameter to null instead of"
				+ " a Conduit!");
	    }
	    return retVal;
	}

	/**
	 * The actual work of receiving data is done here.
	 *
	 * @see com.InfoMontage.task.ExecutableTask#doTask()
	 */
	public void doTask() {
	    Iterator i;
	    Conduit c;
	    assert (log.finer("Beginning receive task."));
	    assert (log.gettingLock(this.params[0]));
	    synchronized (this.params[0]) {
		assert (log.gotLock(this.params[0]));
		assert (log.finest("Attempting receive."));
		c = (Conduit) (this.params[0]);
		try {
		    c.receive();
		} catch (IOException e) {
		    // Lost contact?
		    if (!c.isOpen()) {
			// TBD: Possibly attempt to re-establish?
			removeConduit(c);
		    } else {
			// Still open!
			assert (log.throwing(e));
			throw (IllegalStateException) new IllegalStateException()
				.initCause(e);
		    }
		}
	    }
	    assert (log.releasedLock(this.params[0]));
	    c.beingProcessed.setState(false);
	    assert (log.info("Receive task completed."));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.InfoMontage.task.Task#clearTaskParameters()
	 */
	public void clearParameters() throws IllegalStateException {
	    this.params = null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.InfoMontage.task.Task#isProcessing()
	 */
	public boolean isProcessing() {
	    return (this.params == null) ? false
		    : ((Conduit) (this.params[0])).beingProcessed.getState();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.InfoMontage.task.Task#percentComplete()
	 */
	public float percentComplete() {
	    return (this.isProcessing()) ? 0 : 1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.InfoMontage.task.Task#setTaskParameters(java.lang.Object[])
	 */
	public void setParameters(Object[] pa) throws IllegalArgumentException {
	    this.params = pa;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.InfoMontage.task.Task#getResults()
	 */
	public Object[] getResults() throws IllegalStateException {
	    return null;
	}
    }

    /**
	 * The size of the buffer used for getting raw data from a
	 * {@link Socket}.
	 */
    private static int recvBufSize = 0;

    /**
	 * The size of the buffer used for sending raw data to a {@link Socket}.
	 */
    private static int sendBufSize = 0;
    /**
	 * Determine buffer sizes to use for raw data to/from {@link Socket}s.
	 */
    static {
	try {
	    Socket s = new Socket();
	    DatagramSocket d = new DatagramSocket();
	    int srs = s.getReceiveBufferSize();
	    int sss = s.getSendBufferSize();
	    int drs = d.getReceiveBufferSize();
	    int dss = d.getSendBufferSize();
	    recvBufSize = (srs < drs) ? drs : srs;
	    sendBufSize = (sss < dss) ? dss : sss;
	} catch (SocketException e) {
	    // This should never happen...
	    // TBD: Handle failure cleanly.
	}
    }

    /**
	 * The current size of a {@link Packet}'s payload.
	 */
    private volatile transient Short currPacketSize = new Short((short) 0);

    /**
	 * A holding place for the {@link SelectableChannel}interface into this
	 * Conduit's {@link Socket}.
	 */
    private volatile transient SelectableChannel channel = null;

    /**
	 * A holding place for the {@link ByteChannel}interface into this
	 * Conduit's {@link Socket}.
	 */
    private volatile transient ByteChannel byteChannel = null;

    /**
	 * The {@link Charset}being used with this Conduit.
	 */
    private volatile transient Charset charSet = null;

    /**
	 * The current {@link PacketFactory}for this conduit.
	 */
    private volatile transient PacketFactory packetFactory = null;

    private volatile transient ByteBuffer recvBuf = null;

    private volatile transient ByteBuffer sendBuf = null;

    private volatile transient ByteBuffer recvReadBuf = null;

    private volatile transient Vector readBufs = null;

    private volatile transient byte[] bytesRecvd = null;

    /**
	 * A queue of bundles awaiting completion or acknowledgement. As bundle
	 * {@link Packet}s are received, they are accumulated in an array which
	 * is stored in this queue. Once all of a particular bundle's
	 * {@link Packet}s have been received, the bundle is acknowledged. Once
	 * the acknowledgement has been received by the sender, the bundle
	 * becomes available for processing and is no longer available for a
	 * resend request.
	 */
    private volatile transient Map inBndlQueues = new Hashtable(7, 0.86f);

    /**
	 * A queue of bundles awaiting acknowledgement. When a bundle is sent,
	 * it is stored in this queue until an acknowledgement of receipt is
	 * sent back. Once the acknowledgement has been received, the bundle is
	 * removed from this queue. If there is a timeout, or if the receiver
	 * requests it, the bundle can be resent.
	 */
    private volatile transient Map outBndlQueues = new Hashtable(7, 0.86f);

    /**
	 * Number of bytes sent over this Conduit.
	 */
    private volatile transient BigCounter numBytesSent = new BigCounter();

    /**
	 * Number of bytes received over this Conduit.
	 */
    private volatile transient BigCounter numBytesRcvd = new BigCounter();

    /**
	 * Number of {@link Packet}s sent over this Conduit.
	 */
    private volatile transient BigCounter numPktsSent = new BigCounter();

    /**
	 * Number of {@link Packet}s received over this Conduit.
	 */
    private volatile transient BigCounter numPktsRcvd = new BigCounter();

    /**
	 * Number of bundles of {@link Packet}s sent over this Conduit.
	 */
    private volatile transient BigCounter numBundlesSent = new BigCounter();

    /**
	 * Number of bundles of {@link Packet}s received over this Conduit.
	 */
    private volatile transient BigCounter numBundlesRcvd = new BigCounter();

    private final static long DEFAULT_EXPECTED_PACKET_LAG_MS = 500;

    volatile transient long expectedPacketLagMs = DEFAULT_EXPECTED_PACKET_LAG_MS;

    private final static int NUM_PACKET_RECV_TIMES = 5; // Must

    // be
    // >1
    private volatile transient long[] lastPacketRecvTimes;

    private volatile transient long recvTimesSum = 0;

    private volatile transient long recvTimeDivisor = 0;

    volatile transient long expectedPacketLagMsDelta = 0;

    private final static int LAG_TIMEOUT_MULTIPLE = 3;

    private final static int MAX_TIMEOUTS_TIL_EXCEPTION = 5;

    private final static int MIN_LAG_BEFORE_EXCEPTION = 30 * 1000;

    private volatile transient int numTimeouts = 0;

    /**
	 * Current generation number for this Conduit's {@link Packet}s.
	 */
    private volatile transient Long currGen = new Long(1);

    /**
	 * Current bundle number for this Conduit's {@link Packet}s.
	 */
    private volatile transient Long currBndl = new Long(1);

    /**
	 * An immutable key used to reference bundles of {@link Packet}s in a
	 * bundle ({@link Packet}bundle) queue.
	 *
	 * @author Richard A. Mead <BR>
	 *         Information Montage
	 */
    private static class BndlQKey {

	/**
	 * {@link Logger}for this class
	 */
	private static final AssertableLogger log = new AssertableLogger(
		BndlQKey.class.getName());

	/**
	 * The generation number of the {@link Packet}s.
	 */
	transient final long GENERATION_ID;

	/**
	 * The bundle number of the {@link Packet}s.
	 */
	transient final long BNDL_ID;

	/**
	 * The cached hashcode value for this key.
	 */
	private transient final int HASH_CODE;

	private BndlQKey(long g, long b) {
	    this.GENERATION_ID = g;
	    this.BNDL_ID = b;
	    this.HASH_CODE = new Long(this.GENERATION_ID ^ this.BNDL_ID)
		    .intValue();
	    assert (Defaults.dbg().finest("Created a " + this));
	}

	public boolean equals(Object o) {
	    boolean retVal = ((o instanceof Conduit.BndlQKey)
		    && (((Conduit.BndlQKey) o).HASH_CODE == this.HASH_CODE)
		    && (((Conduit.BndlQKey) o).GENERATION_ID == this.GENERATION_ID) && (((Conduit.BndlQKey) o).BNDL_ID == this.BNDL_ID));
	    assert (Defaults.dbg().finest("Compared equality of " + this
		    + " and " + o + ": " + (retVal ? "" : "not ") + "equal"));
	    return retVal;
	}

	public int hashCode() {
	    return this.HASH_CODE;
	}

	public String toString() {
	    StringBuffer retVal = new StringBuffer("BndlQKey[gen=").append(
		    this.GENERATION_ID).append(",bndl=").append(this.BNDL_ID)
		    .append(",hash=").append(this.HASH_CODE).append("]");
	    return retVal.toString();
	}
    }

    /**
	 * The value class corresponding to the BndlQKey class for bundle
	 * queues. The value is an array of {@link Packet}s, as well as some
	 * bundle state information.
	 *
	 * @author Richard A. Mead <BR>
	 *         Information Montage
	 */
    private static class BndlQValue {

	/**
	 * {@link Logger}for this class
	 */
	private static final AssertableLogger log = new AssertableLogger(
		BndlQValue.class.getName());

	/**
	 * The array of {@link Packet}s containing the bundle.
	 */
	transient ArrayList packets;

	/**
	 * The number of {@link Packet}s in the bundle that have not yet been
	 * received.
	 */
	transient long packetsLeftToRecv = 0;

	transient long expectedCompletion;

	transient boolean recvComplete = false;

	transient boolean ackSent = false;

	BndlQValue(ArrayList p) {
	    this.packets = p;
	    setExpectedCompletion();
	}

	final void setExpectedCompletion() {
	    setExpectedCompletion(DEFAULT_EXPECTED_PACKET_LAG_MS,
		    this.packetsLeftToRecv);
	}

	final void setExpectedCompletion(long expectedPacketDelayMs) {
	    setExpectedCompletion(expectedPacketDelayMs, this.packetsLeftToRecv);
	}

	final void setExpectedCompletion(long expectedPacketDelayMs, long packetsLeft) {
	    this.expectedCompletion = System.currentTimeMillis()
		    + (expectedPacketDelayMs * ((packetsLeft > 0) ? packetsLeft
			    : 5));
	}
    }

    public Conduit(SelectableChannel c) throws NullPointerException,
	    IOException, IllegalStateException, IllegalArgumentException {
	super(c.provider());
	this.initConduit(c, Defaults.DEFAULT_CHARSET, null);
    }

    public Conduit(SelectableChannel c, PacketFactory pf)
	    throws NullPointerException, IOException, IllegalStateException,
	    IllegalArgumentException {
	super(c.provider());
	this.initConduit(c, Defaults.DEFAULT_CHARSET, pf);
    }

    public Conduit(SelectableChannel c, Charset cs)
	    throws NullPointerException, IOException,
	    UnsupportedCharsetException, IllegalStateException,
	    IllegalArgumentException {
	super(c.provider());
	this.initConduit(c, cs, null);
    }

    public Conduit(SelectableChannel c, Charset cs, PacketFactory pf)
	    throws NullPointerException, IOException,
	    UnsupportedCharsetException, IllegalStateException,
	    IllegalArgumentException {
	super(c.provider());
	this.initConduit(c, cs, pf);
    }

    private void initConduit(SelectableChannel c, Charset cs, PacketFactory pf)
	    throws NullPointerException, UnsupportedCharsetException,
	    IllegalStateException, IllegalArgumentException, IOException {
	if (c == null) {
	    throw new NullPointerException("Attempt to create a Conduit using"
		    + " a null SelectableChannel!");
	}
	if (!c.isOpen()) {
	    throw new IllegalStateException("Attempt to create a Conduit using"
		    + " an unopened SelectableChannel!");
	}
	if (!(c instanceof ByteChannel)) {
	    throw new IllegalArgumentException(
		    "Attempt to create a Conduit using"
			    + " a SelectableChannel that does not implement the ByteChannel interface!");
	}
	if (c.isBlocking()) {
	    throw new IllegalStateException("Attempt to create a Conduit using"
		    + " a SelectableChannel in blocking mode!");
	}
	if (c.register(c.provider().openSelector(), c.validOps())
		.isConnectable()) {
	    throw new IllegalArgumentException(
		    "Attempt to create a Conduit using a SelectableChannel that has"
			    + " not completed it's connection!");
	}
	/*
	 * try { ((ByteChannel) c).write(ByteBuffer.allocate(0)); } catch
	 * (NotYetConnectedException e) { throw (IllegalArgumentException) new
	 * IllegalArgumentException( "Attempt to create a Conduit using a
	 * ByteChannel that is not yet connected!") .initCause(e); }
	 */// throw new IllegalArgumentException("Attempt to create a
		// Conduit
	// using"
	// +" a SelectableChannel in blocking mode that has already
	// registered"
	// +" with a Selector!");
	// }
	// if (c.isBlocking() && c.isRegistered()) {
	// throw new IllegalArgumentException("Attempt to create a Conduit
	// using"
	// +" a SelectableChannel in blocking mode that has already
	// registered"
	// +" with a Selector!");
	// }
	this.channel = c;
	this.byteChannel = (ByteChannel) c;
	recvBuf = ByteBuffer.allocate(recvBufSize);
	recvBuf.position(0);
	recvBuf.limit(0);
	sendBuf = ByteBuffer.allocate(sendBufSize);
	sendBuf.position(0);
	sendBuf.limit(0);
	recvReadBuf = ByteBuffer.allocate(recvBufSize);
	recvReadBuf.position(0);
	recvReadBuf.limit(0);
	readBufs = new Vector();
	initNonChannel(cs, pf);
    }

    private void initNonChannel(Charset cs, PacketFactory pf) {
	if (cs == null) {
	    throw new NullPointerException("Attempt to create a Conduit using"
		    + " a null Charset!");
	}
	if (!cs.canEncode()) {
	    throw new UnsupportedCharsetException(
		    "Attempt to"
			    + " create a Conduit using a Charset that does not support encoding!");
	}
	this.charSet = cs;
	this.setPacketFactory(pf);
	this.lastPacketRecvTimes = new long[NUM_PACKET_RECV_TIMES];
	for (int i = 0; i < NUM_PACKET_RECV_TIMES; i++) {
	    this.lastPacketRecvTimes[i] = -1;
	}
	this.recvTimesSum = NUM_PACKET_RECV_TIMES * expectedPacketLagMs;
	addConduit(this);
    }

    private static void addConduit(Conduit c) {
	assert (log.gettingLock(Conduit.conduits));
	synchronized (Conduit.conduits) {
	    assert (log.gotLock(Conduit.conduits));
	    Conduit.conduits.add(c);
	    assert (log.info("Added a Conduit to list: had "
		    + (Conduit.conduits.size() - 1)));
	}
	assert (log.releasedLock(Conduit.conduits));
    }

    static void removeConduit(Conduit c) {
	assert (log.gettingLock(Conduit.conduits));
	synchronized (Conduit.conduits) {
	    assert (log.gotLock(Conduit.conduits));
	    if (!Conduit.conduits.contains(c)) {
		assert (log
			.finer("Requesting removal of Conduit from list which is not IN list!"));
	    } else {
		assert (log.finest("Removing Conduit from list: had "
			+ Conduit.conduits.size()));
		assert (log.gettingLock(Conduit.lockForNextConduitToCheck));
		synchronized (Conduit.lockForNextConduitToCheck) {
		    assert (log.gotLock(Conduit.lockForNextConduitToCheck));
		    if (Conduit.nextConduitToCheck > Conduit.conduits
			    .indexOf(c)) {
			Conduit.nextConduitToCheck--;
		    }
		    Conduit.conduits.remove(c);
		    if (Conduit.nextConduitToCheck >= Conduit.conduits.size()) {
			Conduit.nextConduitToCheck = 0;
		    }
		}
		assert (log.releasedLock(Conduit.lockForNextConduitToCheck));
	    }
	}
	assert (log.releasedLock(Conduit.conduits));
    }

    public PacketFactory getPacketFactory() {
	return this.packetFactory;
    }

    public void setPacketFactory(PacketFactory pf) {
	if (pf == null) {
	    pf = PacketFactory.getDefaultPacketFactory();
	}
	this.packetFactory = pf;
	this.currPacketSize = new Short(pf.getDefaultPayloadSize());
	int bs = pf.getHeartbeatPacket().minimumPacketLength();
	if (this.currPacketSize.shortValue() > (Conduit.sendBufSize + bs)) {
	    this.currPacketSize = new Short((short) (Conduit.sendBufSize - bs));
	}
	if (this.currPacketSize.shortValue() > (Conduit.recvBufSize + bs)) {
	    this.currPacketSize = new Short((short) (Conduit.recvBufSize - bs));
	}
    }

    synchronized protected void receive() throws IOException {
	int p;
	int l;
	if (recvBuf.hasRemaining()) {
	    p = recvBuf.position();
	    l = recvBuf.limit();
	    recvBuf.compact().position(0).limit(l - p);
	} else {
	    recvBuf.position(0).limit(0);
	}
	p = recvBuf.position();
	l = recvBuf.limit();
	recvBuf.limit(recvBuf.capacity()).position(l);
	int got = this.byteChannel.read(recvBuf);
	if (got < 0) {
	    // end-of-stream: shutdown Conduit...
	    if (this.isOpen()) {
		this.close();
	    }
	}
	assert (log.info("Got " + got + " bytes!"));
	recvBuf.limit(recvBuf.position()).position(p);
	if (got > 0 || recvBuf.hasRemaining()) {
	    if (recvReadBuf.hasRemaining()) {
		p = recvReadBuf.position();
		l = recvReadBuf.limit();
		recvReadBuf.compact().position(0).limit(l - p);
	    } else {
		recvReadBuf.position(0).limit(0);
	    }
	    if ((this.recvReadBuf.capacity() - this.recvReadBuf.limit()) < recvBuf
		    .remaining()) {
		// TBD: add code for buffer pool readBufs
		ByteBuffer tbb = ByteBuffer.allocate(this.recvReadBuf
			.remaining()
			+ recvBuf.remaining());
		tbb.put(this.recvReadBuf);
		tbb.limit(tbb.position()).position(0);
		this.recvReadBuf = tbb;
	    }
	    p = recvReadBuf.position();
	    l = recvReadBuf.limit();
	    recvReadBuf.limit(recvReadBuf.capacity()).position(l);
	    recvReadBuf.put(recvBuf);
	    numBytesRcvd.add(recvReadBuf.position() - l);
	    assert (log.info("Added " + (recvReadBuf.position() - l)
		    + " bytes to RecvReadBuf!"));
	    recvReadBuf.limit(recvReadBuf.position()).position(p);
	    // Check if we have at least one full Packet in the recvReadBuf
	    internalRead();
	} else {
	    // Nothing in receive buffer, and nothing came in on the wire
	    // Are we waiting for anything? (Ack or Ack of Ack or rest of
	    // bundle)
	    if (!this.outBndlQueues.isEmpty() || !this.inBndlQueues.isEmpty()) {
		// Have we timed out?
		long td = (System.currentTimeMillis() - this.lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1]);
		if (this.lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1] == -1) {
		    this.lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1] = td;
		} else {
		    if (td > (LAG_TIMEOUT_MULTIPLE * expectedPacketLagMs)) {
			if (++(this.numTimeouts) >= Conduit.MAX_TIMEOUTS_TIL_EXCEPTION) {
			    if (td > Conduit.MIN_LAG_BEFORE_EXCEPTION) {
				throw new RuntimeException("Conduit timed out!");
			    }
			}
		    }
		}
	    }
	}
    }

    synchronized public boolean hasInput() throws IOException {
	return (null != this.readBufs && !this.readBufs.isEmpty());
    }

    synchronized public boolean hasRawInput() throws IOException {
	return (null != this.recvReadBuf && this.recvReadBuf.hasRemaining());
    }

    /**
	 * 'raw' read of characters directly from the (buffering)
	 * {@link InputStreamReader}
	 */
    synchronized public int read(char[] cbuf, int off, int len)
	    throws IOException {
	int retValue;
	receive();
	CharBuffer cb = recvReadBuf.asCharBuffer();
	retValue = cb.remaining();
	retValue = (len < retValue) ? len : retValue;
	cb.get(cbuf, off, retValue);
	this.recvReadBuf.position(recvReadBuf.position() + (2 * retValue));
	return retValue;
    }

    /**
	 * Places the next available bundle contents into the supplied buffer.
	 *
	 * @param buf
	 *                The {@link ByteBuffer}to place the data in.
	 * @return Number of bytes read into buffer.
	 */
    synchronized public int read(ByteBuffer buf) {
	int retValue = 0;
	if (!readBufs.isEmpty()) {
	    ByteBuffer tmpBuf = this.read();
	    retValue = tmpBuf.flip().remaining();
	    buf.put(tmpBuf);
	}
	return retValue;
    }

    /**
	 * Returns the next available bundle contents buffer.
	 *
	 * @return The buffer with the next available bundle's content, or null
	 *         if no bundles are available.
	 */
    synchronized public ByteBuffer read() {
	ByteBuffer retValue = null;
	if (!readBufs.isEmpty()) {
	    retValue = (ByteBuffer) readBufs.remove(0);
	}
	return retValue;
    }

    /**
	 * Attempts to parse out {@link Packet}s from the recvReadBuf and place
	 * them into the input bundle queue. If a bundle is completed, places it
	 * into the readBufs array.
	 *
	 * @throws IOException
	 */
    synchronized public void internalRead() throws IOException {
	int bndlLen = 0;
	int i;
	long newExpectedPacketLagMs;
	boolean gotOne = true;
	while (this.hasRawInput() && gotOne) {
	    gotOne = false;
	    Packet p = null;
	    try {
		p = this.receivePacket();
	    } catch (IllegalArgumentException e) {
		// bad header!
		// TBD: handle bad datastream header
		assert (log.throwing("com.InfoMontage.net.Conduit",
			"internalRead()", e));
	    } catch (BufferUnderflowException e) {
		// not enough data! Have we timed out?
		if ((System.currentTimeMillis() - this.lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1]) > (LAG_TIMEOUT_MULTIPLE * expectedPacketLagMs)) {
		    // until we do resend requests, handle as timeout only
		    if (++this.numTimeouts >= Conduit.MAX_TIMEOUTS_TIL_EXCEPTION) {
			throw new RuntimeException("Conduit timed out!");
		    }
		    // We might have some metadata that we could use
		    // to request a resend?
		    // TBD: request resend if enough metadata present
		    // not enough metadata, and we've timed out...
		    // TBD: handle timeout with partial packet
		    assert (log
			    .info("Timeout with partial packet!n   Partial data="
				    + com.InfoMontage.util.Buffer
					    .toString(recvReadBuf)));
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		assert (log.throwing("com.InfoMontage.net.Conduit",
			"internalRead()", e));
	    }
	    if (null == p) {
		// If buffer was null or not enough data yet for a Packet
		// TBD: handle bad datastream
		/*
		 * throw new RuntimeException("Connection could not" + " receive
		 * a valid Packet!\nbuf={len " + recvReadBuf.remaining() + "}" +
		 * Buffer.toString(recvReadBuf));
		 */
	    } else {
		gotOne = true;
		assert (log.info("***Received packet: " + p.toString()));
		this.numTimeouts = 0;
		// update expected packet lag time
		this.recvTimesSum = 0;
		this.recvTimeDivisor = 0;
		for (i = NUM_PACKET_RECV_TIMES - 2; (i > 0)
			&& (lastPacketRecvTimes[i] != -1); i--) {
		    this.recvTimesSum += (lastPacketRecvTimes[i + 1] - lastPacketRecvTimes[i]);
		    this.recvTimeDivisor++;
		}
		for (i = 1; i < NUM_PACKET_RECV_TIMES; i++) {
		    lastPacketRecvTimes[i - 1] = lastPacketRecvTimes[i];
		}
		lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1] = System
			.currentTimeMillis();
		newExpectedPacketLagMs = (this.recvTimesSum == 0) ? this.expectedPacketLagMs
			: this.recvTimesSum / this.recvTimeDivisor;
		expectedPacketLagMsDelta = this.expectedPacketLagMs
			- newExpectedPacketLagMs;
		this.expectedPacketLagMs = newExpectedPacketLagMs;
		// handle Ack and Nak packets
		if (p.pktID == 0 && p.genID != 0 && p.bndlID != 0
			&& p.len < (short) 1) {
		    // handle Ack packets
		    if (p.len == (short) 0) {
			// Is it an Ack for something sent, or an Ack of
			// an
			// Ack?
			BndlQKey mqk = new BndlQKey(p.genID, p.bndlID);
			if (this.outBndlQueues.containsKey(mqk)) {
			    // Ack the Ack and remove from queue
			    // TBD: validate removable!
			    assert (log.info("Packet is Ack of sent bundle "
				    + mqk.toString() + ", sending Ack of Ack."));
			    sendAckPacket(p.genID, p.bndlID);
			    this.outBndlQueues.remove(mqk);
			    assert (log.info(outBndlQueues.size()
				    + " entries left in sent queue."));
			} else if (this.inBndlQueues.containsKey(mqk)) {
			    // Remove from queue!
			    // TBD: validate removable!
			    assert (log
				    .info("Packet is Ack of Ack of received bundle "
					    + mqk.toString()));
			    this.inBndlQueues.remove(mqk);
			    assert (log.info(inBndlQueues.size()
				    + " entries left in received queue."));
			} else {
			    // TBD: what to do if we got an Ack for
			    // something
			    // we
			    // did't send
			}
		    } else { // handle Nak packets
			// TBD: Nak means...
		    }
		} else {
		    // ByteBuffer tbb = ByteBuffer.allocate(recvReadBuf
		    // .capacity());
		    BndlQKey bqk = new BndlQKey(p.genID, p.bndlID);
		    int bl = 0;
		    if (inBndlQueues.containsKey(bqk)) {
			List pa = ((BndlQValue) inBndlQueues.get(bqk)).packets;
			Packet hp = (Packet) pa.get(0);
			if (null != hp) {
			    Packet tp = null;
			    for (int j = 1; j <= hp.len; j++) {
				tp = (Packet) pa.get(j);
				if (null != tp) {
				    bl += tp.len;
				} else {
				    if (j == p.pktID) {
					bl += p.len;
				    } else {
					bl = 0;
					break;
				    }
				}
			    }
			}
		    }
		    ByteBuffer tbb = (bl == 0) ? null : ByteBuffer.allocate(bl);
		    // see if we've completed a bundle yet
		    bndlLen = this.queuePacket(p, tbb);
		    if (bndlLen > 0) {
			readBufs.add(tbb);
		    }
		    assert (log.info(" bundleBufLen=" + bndlLen));
		}
	    }
	}
    }

    private int queuePacket(Packet pkt, ByteBuffer buf)
	    throws java.nio.BufferOverflowException {
	int retValue = 0;
	boolean foundInQ = false;
	boolean complete = false;
	// TBD: validate genID and renegotiate protocols if necessary
	Conduit.BndlQKey mqk = new Conduit.BndlQKey(pkt.genID, pkt.bndlID);
	ArrayList pal;
	BndlQValue mqv;
	if (this.inBndlQueues.containsKey(mqk)) {
	    foundInQ = true;
	    mqv = (BndlQValue) this.inBndlQueues.get(mqk);
	    pal = mqv.packets;
	} else {
	    int cap = (pkt.pktID == 0) ? pkt.len + 1 : ((pkt.pktID < 6) ? 6
		    : pkt.pktID + 1);
	    pal = new ArrayList(cap);
	    for (int i = 0; i < cap; ++i) {
		pal.add(null);
	    }
	    mqv = new BndlQValue(pal);
	    mqv.setExpectedCompletion(expectedPacketLagMs);
	}
	if (pal.size() <= pkt.pktID) {
	    mqv.packetsLeftToRecv += (pkt.pktID + 1 - pal.size());
	    mqv.setExpectedCompletion(expectedPacketLagMs);
	    pal.ensureCapacity(pkt.pktID + 1);
	    for (int i = pal.size(); i <= pkt.pktID; ++i) {
		pal.add(null);
	    }
	}
	if (pal.get(pkt.pktID) != null) {
	    // already received this packet ID!
	    // TBD: verify same packet including data
	    // TBD: increment duplicate packet count
	} else {
	    pal.set(pkt.pktID, pkt);
	    if (!foundInQ) {
		this.inBndlQueues.put(mqk, mqv);
	    }
	}
	Packet h = (Packet) pal.get(0);
	if (h != null) {
	    if (pal.size() <= h.len) {
		mqv.packetsLeftToRecv += (h.len + 1 - pal.size());
		mqv.setExpectedCompletion(expectedPacketLagMs);
		pal.ensureCapacity(h.len + 1);
		for (int i = pal.size(); i <= h.len; ++i) {
		    pal.add(null);
		}
	    } else {
		complete = true;
		for (int i = 1; i <= h.len && complete; ++i) {
		    if (pal.get(i) == null) {
			complete = false;
		    }
		}
		if (complete) {
		    mqv.recvComplete = true;
		    ByteBuffer m = null;
		    try {
			Packet[] pa = new Packet[h.len + 1];
			pal.toArray(pa);
			m = this.packetFactory.recombine(pa);
		    } catch (IllegalArgumentException e) {
			// TBD: handle invalid packet list - request resend
			// m=null;
			// inBndlQueues.remove(mqk);
			// sendNakPacket(h);
		    } finally {
			if (m != null) {
			    try {
				assert (log
					.info("Sending Ack of received bundle "
						+ mqk.toString()));
				sendAckPacket(h.genID, h.bndlID);
				mqv.ackSent = true;
				mqv.setExpectedCompletion(expectedPacketLagMs,
					1);
			    } catch (IOException e) {
				// TODO handle inability to send Ack
				assert (log.throwing(
					"com.InfoMontage.net.Conduit",
					"queuePacket(Packet pkt = " + pkt
						+ ", ByteBuffer buf = " + buf
						+ ")", e));
			    }
			    retValue = m.remaining();
			    if (retValue > buf.remaining()) {
				throw new BufferOverflowException();
			    } else {
				numBundlesRcvd.add(1);
				buf.put(m);
			    }
			    // will remove from queue when receive Ack of
			    // Ack
			    // or timeout while waiting for Ack of Ack
			    // this.inBndlQueues.remove(mqk);
			}
		    }
		}
	    }
	}
	// Check for bundle receive timeout
	if (!complete && foundInQ) {
	    if (System.currentTimeMillis() > (mqv.expectedCompletion + (Conduit.LAG_TIMEOUT_MULTIPLE
		    * Conduit.MAX_TIMEOUTS_TIL_EXCEPTION * this.expectedPacketLagMs))) {
		// TBD: request resend
		throw new RuntimeException("Conduit timed out!");
	    }
	}
	return retValue;
    }

    private Packet receivePacket() throws IllegalArgumentException,
	    BufferUnderflowException, IOException {
	Packet rp;
	int numRead = 0;
	// while (!this.hasInput()) {
	// try {
	// // This should not be necessary - should only be called when
	// // some
	// // data is available to read...
	// Thread.sleep(20);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// receive();
	numRead = recvReadBuf.position();
	rp = packetFactory.valueOf(this.recvReadBuf);
	numRead = recvReadBuf.position() - numRead;
	assert (log.info("Extracted " + numRead + " bytes from recvReadBuf!"));
	if (null != rp) {
	    numPktsRcvd.add(1);
	}
	return rp;
    }

    /**
	 * 'raw' write of characters directly to the {@link ByteChannel}
	 */
    synchronized public void write(char[] cbuf, int off, int len)
	    throws IOException {
	if (this.sendBuf.capacity() < (2 * len)) {
	    this.sendBuf = ByteBuffer.allocate(2 * len);
	}
	this.sendBuf.clear();
	this.sendBuf.limit(this.sendBuf.capacity());
	CharBuffer cb = sendBuf.asCharBuffer();
	cb.put(cbuf, off, len);
	this.sendBuf.limit(2 * len);
	do {
	    numBytesSent.add(this.byteChannel.write(this.sendBuf));
	} while (this.sendBuf.hasRemaining());
	this.sendBuf.clear();
	this.sendBuf.limit(0);
    }

    /**
	 * Flushes the output buffered with the
	 * {@link Conduit#write(char[],int,int)}method. This method does
	 * nothing (other than synchronize), as the output will always have been
	 * flushed as part of the call to {@link Conduit#write(char[],int,int)}.
	 *
	 * TBD: implement flush when Conduit buffers writes
	 *
	 * @throws IOException
	 */
    synchronized public void flush() throws IOException {/* nothin' to do */
    }

    synchronized public void write(ByteBuffer buf) throws IOException,
	    NullPointerException, IllegalArgumentException {
	// TBD: add code for buffer pool writeBufs and writer monitor thread
	Conduit.BndlQKey mqk;
	Packet[] pa;
	assert (log.gettingLock(this.currGen));
	synchronized (this.currGen) {
	    assert (log.gotLock(this.currGen));
	    assert (log.gettingLock(this.currBndl));
	    synchronized (this.currBndl) {
		assert (log.gotLock(this.currBndl));
		mqk = new Conduit.BndlQKey(this.currGen.intValue(),
			this.currBndl.intValue());
		this.currBndl = new Long(this.currBndl.longValue() + 1);
		if (this.outBndlQueues.containsKey(mqk)) {
		    throw new IOException(
			    "Duplicate bundle key generation error in"
				    + "Conduit!\ncurrGen=" + this.currGen
				    + ", currBndl="
				    + (this.currBndl.longValue() - 1));
		}
	    }
	    assert (log.releasedLock(this.currBndl));
	}
	assert (log.releasedLock(this.currGen));
	assert (log.gettingLock(this.currPacketSize));
	synchronized (this.currPacketSize) {
	    assert (log.gotLock(this.currPacketSize));
	    pa = this.packetFactory.decompose(buf, this.currPacketSize
		    .shortValue(), mqk.GENERATION_ID, mqk.BNDL_ID);
	    outBndlQueues.put(mqk, new BndlQValue(new ArrayList(
		    java.util.Arrays.asList(pa))));
	    ByteBuffer bb = null;
	    for (int i = 0; i < pa.length; ++i) {
		assert (log.finest("Sending packet:" + pa[i].toString()));
		sendPacket(pa[i]);
		assert (log.finest("Sent " + pa[i].byteLength() + " bytes."));
	    }
	    numBundlesSent.add(1);
	}
	assert (log.releasedLock(this.currPacketSize));
    }

    void sendAckPacket(long g, long m) throws IOException {
	// Send Ack packet
	Packet ackPacket = this.packetFactory.newPacket(g, m, 0, (short) 0,
		null);
	assert (log.info("Sending Ack packet"));
	try {
	    sendPacket(ackPacket);
	} catch (IOException e) {
	    // TBD: handle inability to send Ack packet
	    throw e;
	} finally {
	    assert (log.info("Sent Ack packet"));
	}
    }

    void sendNakPacket(long g, long m) throws IOException {
	// Send Nak packet
	Packet nakPacket = this.packetFactory.newPacket(g, m, 0, (short) -1,
		null);
	assert (log.info("Sending Nak packet"));
	try {
	    sendPacket(nakPacket);
	} catch (IOException e) {
	    // TBD: handle inability to send Nak packet
	    throw e;
	} finally {
	    assert (log.info("Sent Nak packet"));
	}
    }

    void sendPacket(Packet p) throws IOException {
	assert (log.info("Sending packet: " + p.toString()));
	int nw = 0;
	try {
	    nw = this.byteChannel.write(p.toByteBuffer());
	} catch (IOException e) {
	    // TBD: handle inability to send packet
	    throw e;
	} finally {
	    numBytesSent.add(nw);
	    assert (log.info("Sent " + nw + " bytes."));
	    // TBD: update statistics
	    numPktsSent.add(1);
	}
    }

    public int validOps() {
	return channel.validOps();
    }

    protected void implCloseSelectableChannel() throws IOException {
	channel.close();
	removeConduit(this);
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
	throw new UnsupportedOperationException();
    }

    synchronized public BigInteger getBytesRcvd() {
	return numBytesRcvd.get();
    }

    synchronized public BigInteger getBytesSent() {
	return numBytesSent.get();
    }

    synchronized public BigInteger getPktsRcvd() {
	return numPktsRcvd.get();
    }

    synchronized public BigInteger getPktsSent() {
	return numPktsSent.get();
    }

    synchronized public BigInteger getBundlesRcvd() {
	return numBundlesRcvd.get();
    }

    synchronized public BigInteger getBundlesSent() {
	return numBundlesSent.get();
    }

    synchronized public void clearStatistics() {
	numBytesSent.clear();
	numBytesRcvd.clear();
	numBundlesSent.clear();
	numBundlesRcvd.clear();
    }

}