/*
 * Conduit.java
 * 
 * Created on Apr 11, 2004
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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
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

import com.InfoMontage.common.Defaults;
import com.InfoMontage.math.BigCounter;
import com.InfoMontage.task.AbstractTask;
import com.InfoMontage.task.TaskExecutorPool;
import com.InfoMontage.util.Buffer;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public final class Conduit
    extends AbstractSelectableChannel
{

    private static final int MAX_RECEIVER_TASK_THREADS = 4;
    protected static List conduits = new Vector();
    protected static final ReceiverTask RECEIVE_TASKS[] = new ReceiverTask[MAX_RECEIVER_TASK_THREADS];
    protected static Object lockForNextConduitToCheck = new Object();
    protected static int nextConduitToCheck = 0;

    protected static final TaskExecutorPool RECEIVER_TASK_FACTORY = TaskExecutorPool
        .getPool("ConduitReceivers", 1, MAX_RECEIVER_TASK_THREADS, false);

    private static class ConduitMonitorThread
        extends Thread
    {


        /**
         *  
         */
        private ConduitMonitorThread() {
            super("Conduit monitor");
        }

        public void run() {
            int i;
            Object[][] parms = new Object[MAX_RECEIVER_TASK_THREADS][1];
            System.err.println("Conduit monitoring thread starting.");
            while (true) {
                synchronized (Conduit.conduits) {
                    if (!Conduit.conduits.isEmpty()) {
                        System.err
                            .println("Conduit monitor thread beginning task check.");
                        for (i = 0; i < Conduit.MAX_RECEIVER_TASK_THREADS; i++ )
                        {
                            if (null != Conduit.RECEIVE_TASKS[i]) {
                                if (!Conduit.RECEIVE_TASKS[i]
                                    .isProcessing())
                                {
                                    break;
                                }
                            } else {
                                Conduit.RECEIVE_TASKS[i] = new ReceiverTask();
                                break;
                            }
                        }
                        if (i < Conduit.MAX_RECEIVER_TASK_THREADS) {
                            System.err
                                .println("Conduit monitor thread has an"
                                    + " available task to use. (#" + i
                                    + ")");
                            synchronized (Conduit.lockForNextConduitToCheck)
                            {
                                System.err
                                    .println("Conduit monitor thread has"
                                        + " aquired lock.");
                                System.err
                                    .println("Conduit monitor checking"
                                        + " conduit #"
                                        + Conduit.nextConduitToCheck + ".");
                                parms[i][0] = Conduit.conduits
                                    .get(Conduit.nextConduitToCheck++ );
                                if (Conduit.nextConduitToCheck >= Conduit.conduits
                                    .size())
                                {
                                    Conduit.nextConduitToCheck = 0;
                                }
                            }
                            System.err
                                .println("Conduit monitor thread has released lock.");
                            Conduit.RECEIVE_TASKS[i]
                                .setTaskParameters(parms[i]);
                            try {
                                Conduit.RECEIVER_TASK_FACTORY.doTask(
                                    Conduit.RECEIVE_TASKS[i], false);
                            } catch (InterruptedException e) {
                                // TODO Close the conduit??
                            }
                        }
                    }
                }
                synchronized (Conduit.lockForNextConduitToCheck) {
                    if (Conduit.nextConduitToCheck == 0) {
                        try {
                            System.err
                                .println("Conduit monitor thread sleeping.");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        System.err
                            .println("Conduit monitor thread woke up.");
                    }
                }
            }
        }
    }

    private static final ConduitMonitorThread cmt = new ConduitMonitorThread();
    static {
        cmt.start();
    }

    public static class ReceiverTask
        extends AbstractTask
    {

        /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.AbstractTask#validateParameters(java.lang.Object[])
         */
        protected Exception validateParameters(Object[] pa) {
            Exception retVal = null;
            Conduit c = null;
            try {
                c = (Conduit) pa[0];
            } catch (ClassCastException e) {
                retVal = (IllegalArgumentException) new IllegalArgumentException(
                    "Attempt" + " to set RecieverTask parameter to a "
                        + pa[0].getClass() + " instead of a Conduit!")
                    .initCause(e);
            }
            return null;
        }

        protected void doTask() {
            Iterator i;
            Conduit c;
            System.err.println("Beginning task.");
            synchronized (this.params[0]) {
                System.err.println("Attempting recieve.");
                c = (Conduit) (this.params[0]);
                try {
                    c.recieve();
                } catch (IOException e) {
                    // Lost contact?
                    if (!c.isOpen()) {
                        //                        try {
                        //                            c.close();
                        //                        } catch (IOException e1) {
                        //                            // TODO Auto-generated catch block
                        //                            e1.printStackTrace();
                        //                        } finally {
                        removeConduit(c);
                        //                        }
                    } else {
                        // Still open!
                        e.printStackTrace();
                        throw (IllegalStateException) new IllegalStateException()
                            .initCause(e);
                    }
                }
            }
        }
    }

    private static int recvBufSize;

    private static int sendBufSize;

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
        }
    }

    private transient SelectableChannel channel = null;

    private transient ByteChannel byteChannel = null;

    //private transient Selector selector=null;
    //private transient SelectionKey selectionKey=null;
    //private transient Socket sock=null;
    //private transient java.net.DatagramSocket datagramSock=null;
    private transient Charset charSet = null;

    //private transient InputStreamReader reader=null;
    //private transient OutputStreamWriter writer=null;
    //private transient java.io.InputStream in=null;
    //private transient java.io.OutputStream out=null;

    private transient PacketFactory packetFactory = null;

    private transient ByteBuffer recvBuf = null;

    private transient ByteBuffer sendBuf = null;

    private transient ByteBuffer recvReadBuf = null;

    private transient Vector readBufs = null;

    private transient byte[] bytesRecvd = null;

    private transient Map inMsgQueues = new Hashtable(7, 0.86f);

    private transient Map outMsgQueues = new Hashtable(7, 0.86f);

    private transient BigCounter numBytesSent = new BigCounter();
    private transient BigCounter numBytesRcvd = new BigCounter();
    private transient BigCounter numPktsSent = new BigCounter();
    private transient BigCounter numPktsRcvd = new BigCounter();
    private transient BigCounter numBundlesSent = new BigCounter();
    private transient BigCounter numBundlesRcvd = new BigCounter();

    private transient Short currPacketSize = new Short((short) 0);

    private final static long DEFAULT_EXPECTED_PACKET_LAG_MS = 500;

    transient long expectedPacketLagMs = DEFAULT_EXPECTED_PACKET_LAG_MS;

    private final static int NUM_PACKET_RECV_TIMES = 5; // Must be >1

    private transient long[] lastPacketRecvTimes;

    private transient long recvTimesSum;

    transient long expectedPacketLagMsDelta;

    private final static int LAG_TIMEOUT_MULTIPLE = 3;

    private final static int MAX_TIMEOUTS_TIL_EXCEPTION = 5;

    private transient int numTimeouts = 0;

    private transient Long currGen = new Long(1);

    private transient Long currMsg = new Long(1);

    private static class MsgQKey {

        transient final long GENERATION_ID;

        transient final long MSG_ID;

        private transient final int HASH_CODE;

        private MsgQKey(long g, long m) {
            this.GENERATION_ID = g;
            this.MSG_ID = m;
            this.HASH_CODE = new Long(this.GENERATION_ID ^ this.MSG_ID)
                .intValue();
            assert (Defaults.dbg().finest("Created a " + this));
        }

        public boolean equals(Object o) {
            boolean retVal = ( (o instanceof Conduit.MsgQKey)
                && ( ((Conduit.MsgQKey) o).HASH_CODE == this.HASH_CODE)
                && ( ((Conduit.MsgQKey) o).GENERATION_ID == this.GENERATION_ID) && ( ((Conduit.MsgQKey) o).MSG_ID == this.MSG_ID));
            assert (Defaults.dbg().finest("Compared equality of " + this
                + " and " + o + ": " + (retVal ? "" : "not ") + "equal"));
            return retVal;
        }

        public int hashCode() {
            return this.HASH_CODE;
        }

        public String toString() {
            StringBuffer retVal = new StringBuffer("MsgQKey[gen=").append(
                this.GENERATION_ID).append(",msg=").append(this.MSG_ID)
                .append(",hash=").append(this.HASH_CODE).append("]");
            return retVal.toString();
        }
    }

    private static class MsgQValue {

        transient ArrayList packets;

        transient long expectedCompletion;

        transient long packetsLeftToRecv = 0;

        transient boolean recvComplete = false;

        transient boolean ackSent = false;

        MsgQValue(ArrayList p) {
            this.packets = p;
            setExpectedCompletion();
        }

        void setExpectedCompletion() {
            setExpectedCompletion(DEFAULT_EXPECTED_PACKET_LAG_MS,
                this.packetsLeftToRecv);
        }

        void setExpectedCompletion(long expectedPacketDelayMs) {
            setExpectedCompletion(expectedPacketDelayMs,
                this.packetsLeftToRecv);
        }

        void setExpectedCompletion(long expectedPacketDelayMs,
            long packetsLeft)
        {
            this.expectedCompletion = System.currentTimeMillis()
                + (expectedPacketDelayMs * ( (packetsLeft > 0)
                    ? packetsLeft : 5));
        }
    }

    public Conduit(SelectableChannel c)
        throws NullPointerException, IOException, IllegalStateException,
        IllegalArgumentException
    {
        super(c.provider());
        this.initConduit(c, Defaults.DEFAULT_CHARSET, null);
    }

    public Conduit(SelectableChannel c, PacketFactory pf)
        throws NullPointerException, IOException, IllegalStateException,
        IllegalArgumentException
    {
        super(c.provider());
        this.initConduit(c, Defaults.DEFAULT_CHARSET, pf);
    }

    public Conduit(SelectableChannel c, Charset cs)
        throws NullPointerException, IOException,
        UnsupportedCharsetException, IllegalStateException,
        IllegalArgumentException
    {
        super(c.provider());
        this.initConduit(c, cs, null);
    }

    public Conduit(SelectableChannel c, Charset cs, PacketFactory pf)
        throws NullPointerException, IOException,
        UnsupportedCharsetException, IllegalStateException,
        IllegalArgumentException
    {
        super(c.provider());
        this.initConduit(c, cs, pf);
    }

    private void initConduit(SelectableChannel c, Charset cs,
        PacketFactory pf)
        throws NullPointerException, UnsupportedCharsetException,
        IllegalStateException, IllegalArgumentException, IOException
    {
        if (c == null) { throw new NullPointerException(
            "Attempt to create a Conduit using"
                + " a null SelectableChannel!"); }
        if (!c.isOpen()) { throw new IllegalStateException(
            "Attempt to create a Conduit using"
                + " an unopened SelectableChannel!"); }
        if (! (c instanceof ByteChannel)) { throw new IllegalArgumentException(
            "Attempt to create a Conduit using"
                + " a SelectableChannel that does not implement the ByteChannel interface!"); }
        //if (c.isBlocking() && c.isRegistered()) {
        //    throw new IllegalArgumentException("Attempt to create a Conduit
        // using"
        //    +" a SelectableChannel in blocking mode that has already
        // registered"
        //    +" with a Selector!");
        //}
        this.channel = c;
        this.byteChannel = (ByteChannel) c;
        //this.selector = c.provider().openSelector();
        //this.selectionKey = c.register(this.selector, c.validOps());
        //if (!this.selectionKey.isConnectable()) {
        //    throw new IllegalArgumentException("Attempt to create a Conduit
        // using"
        //    +" a SelectableChannel that has not completed it's connection!");
        //}
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
        if (cs == null) { throw new NullPointerException(
            "Attempt to create a Conduit using" + " a null Charset!"); }
        if (!cs.canEncode()) { throw new UnsupportedCharsetException(
            "Attempt to"
                + " create a Conduit using a Charset that does not support encoding!"); }
        this.charSet = cs;
        this.setPacketFactory(pf);
        this.lastPacketRecvTimes = new long[NUM_PACKET_RECV_TIMES];
        for (int i = 0; i < NUM_PACKET_RECV_TIMES; i++ ) {
            this.lastPacketRecvTimes[i] = i * expectedPacketLagMs;
        }
        this.recvTimesSum = NUM_PACKET_RECV_TIMES * expectedPacketLagMs;
        addConduit(this);
    }

    private static void addConduit(Conduit c) {
        synchronized (conduits) {
            // TBD: update nextConduitToCheck?
            System.err.println("Added a Conduit to list: had "
                + conduits.size());
            conduits.add(c);
        }
    }

    static void removeConduit(Conduit c) {
        synchronized (conduits) {
            if (!conduits.contains(c)) {
                System.err
                    .println("Requesting removal of Conduit from list"
                        + " which is not IN list!");
            } else {
                System.err.println("Removing Conduit from list: had "
                    + conduits.size());
                synchronized (lockForNextConduitToCheck) {
                    if (nextConduitToCheck > conduits.indexOf(c)) {
                        nextConduitToCheck-- ;
                    }
                    conduits.remove(c);
                    if (nextConduitToCheck >= conduits.size()) {
                        nextConduitToCheck = 0;
                    }
                }
            }
        }
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
            this.currPacketSize = new Short(
                (short) (Conduit.sendBufSize - bs));
        }
        if (this.currPacketSize.shortValue() > (Conduit.recvBufSize + bs)) {
            this.currPacketSize = new Short(
                (short) (Conduit.recvBufSize - bs));
        }
    }

    synchronized protected void recieve() throws IOException {
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
        System.err.println("Got " + got + " bytes!");
        recvBuf.limit(recvBuf.position()).position(p);
        if (got > 0 || recvBuf.hasRemaining()) {
            if (recvReadBuf.hasRemaining()) {
                p = recvReadBuf.position();
                l = recvReadBuf.limit();
                recvReadBuf.compact().position(0).limit(l - p);
            } else {
                recvReadBuf.position(0).limit(0);
            }
            if ( (this.recvReadBuf.capacity() - this.recvReadBuf.limit()) < recvBuf
                .remaining())
            {
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
            System.err.println("Added " + (recvReadBuf.position() - l)
                + " bytes to ReadBuf!");
            recvReadBuf.limit(recvReadBuf.position()).position(p);
        } else {
            // Nothing in recieve buffer, and nothing came in on the wire
            // Are we waiting for anything? (Ack or Ack of Ack or rest of
            // message)
            if (!this.outMsgQueues.isEmpty() || !this.inMsgQueues.isEmpty())
            {
                // Have we timed out?
                if ( (System.currentTimeMillis() - this.lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1]) > (LAG_TIMEOUT_MULTIPLE * expectedPacketLagMs))
                {
                    if ( ++this.numTimeouts >= Conduit.MAX_TIMEOUTS_TIL_EXCEPTION) { throw new RuntimeException(
                        "Conduit timed out!"); }
                }
            }
        }
    }

    synchronized public boolean hasInput()
        throws IOException, java.nio.channels.CancelledKeyException
    {
        //recieve();
        return (null != this.recvReadBuf && this.recvReadBuf.hasRemaining());
    }

    /**
     * 'raw' read of characters directly from the (buffering)
     * {@link InputStreamReader}
     */
    synchronized public int read(char[] cbuf, int off, int len)
        throws IOException
    {
        int retValue;
        recieve();
        CharBuffer cb = recvReadBuf.asCharBuffer();
        retValue = cb.remaining();
        retValue = (len < retValue) ? len : retValue;
        cb.get(cbuf, off, retValue);
        this.recvReadBuf.position(recvReadBuf.position() + (2 * retValue));
        return retValue;
    }

    synchronized public int read(ByteBuffer buf) throws IOException {
        int retValue = 0;
        int i;
        long newExpectedPacketLagMs;
        if (!readBufs.isEmpty()) {
            ByteBuffer tmpBuf = (ByteBuffer) readBufs.remove(0);
            retValue = tmpBuf.remaining();
            buf.put(tmpBuf);
        } else {
            while (this.hasInput() && retValue == 0) {
                Packet p = null;
                try {
                    p = this.recievePacket();
                } catch (IllegalArgumentException e) {
                    // bad header!
                    // TBD: handle bad datastream header
                    e.printStackTrace();
                } catch (BufferUnderflowException e) {
                    // not enough data! Have we timed out?
                    if ( (System.currentTimeMillis() - this.lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1]) > (LAG_TIMEOUT_MULTIPLE * expectedPacketLagMs))
                    {
                        // until we do resend requests, handle as timeout only
                        if ( ++this.numTimeouts >= Conduit.MAX_TIMEOUTS_TIL_EXCEPTION) { throw new RuntimeException(
                            "Conduit timed out!"); }
                        // We might have some metadata that we could use
                        // to request a resend?
                        // TBD: request resend if enough metadata present
                        // not enough metadata, and we've timed out...
                        // TBD: handle timeout with partial packet
                        System.err.println("Timeout with partial packet!\n"
                            + "   Partial data="
                            + com.InfoMontage.util.Buffer
                                .toString(recvReadBuf));
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (null == p) {
                    // should only get here if buffer was null -
                    // or the resend request also failed...
                    // TBD: handle bad datastream
                    throw new RuntimeException("Connection could not"
                        + " recieve a valid Packet!\nbuf={len "
                        + recvReadBuf.remaining() + "}"
                        + Buffer.toString(recvReadBuf));
                } else {
                    System.err.println("***Received packet: "
                        + p.toString());
                    this.numTimeouts = 0;
                    // update expected packet lag time
                    this.recvTimesSum -= lastPacketRecvTimes[1]
                        - lastPacketRecvTimes[0];
                    for (i = 1; i < NUM_PACKET_RECV_TIMES; i++ ) {
                        lastPacketRecvTimes[i - 1] = lastPacketRecvTimes[i];
                    }
                    lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1] = System
                        .currentTimeMillis();
                    this.recvTimesSum += lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 1]
                        - lastPacketRecvTimes[NUM_PACKET_RECV_TIMES - 2];
                    newExpectedPacketLagMs = this.recvTimesSum
                        / NUM_PACKET_RECV_TIMES;
                    expectedPacketLagMsDelta = this.expectedPacketLagMs
                        - newExpectedPacketLagMs;
                    this.expectedPacketLagMs = newExpectedPacketLagMs;
                    // handle Ack and Nak packets
                    if (p.pktID == 0 && p.genID != 0 && p.msgID != 0
                        && p.len < (short) 1)
                    {
                        // handle Ack packets
                        if (p.len == (short) 0) {
                            // Is it an Ack for something sent, or an Ack of
                            // an
                            // Ack?
                            MsgQKey mqk = new MsgQKey(p.genID, p.msgID);
                            if (this.outMsgQueues.containsKey(mqk)) {
                                // Ack the Ack and remove from queue
                                // TBD: validate removable!
                                System.err
                                    .println("Packet is Ack of sent message "
                                        + mqk.toString()
                                        + ", sending Ack of Ack.");
                                sendAckPacket(p.genID, p.msgID);
                                this.outMsgQueues.remove(mqk);
                                System.err.println(outMsgQueues.size()
                                    + " entries left in sent queue.");
                            } else if (this.inMsgQueues.containsKey(mqk)) {
                                // Remove from queue!
                                // TBD: validate removable!
                                System.err
                                    .println("Packet is Ack of Ack of recieved "
                                        + "message " + mqk.toString());
                                this.inMsgQueues.remove(mqk);
                                System.err.println(inMsgQueues.size()
                                    + " entries left in recieved queue.");
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
                        retValue = this.queuePacket(p, buf);
                        System.err.println(" messageBufLen=" + retValue);
                    }
                }
            }
        }
        return retValue;
    }

    private int queuePacket(Packet pkt, ByteBuffer buf)
        throws java.nio.BufferOverflowException
    {
        int retValue = 0;
        boolean foundInQ = false;
        boolean complete = false;
        // TBD: validate genID and renegotiate protocols if necessary
        Conduit.MsgQKey mqk = new Conduit.MsgQKey(pkt.genID, pkt.msgID);
        ArrayList pal;
        MsgQValue mqv;
        if (this.inMsgQueues.containsKey(mqk)) {
            foundInQ = true;
            mqv = (MsgQValue) this.inMsgQueues.get(mqk);
            pal = mqv.packets;
        } else {
            int cap = (pkt.pktID == 0) ? pkt.len + 1 : ( (pkt.pktID < 6)
                ? 6 : pkt.pktID + 1);
            pal = new ArrayList(cap);
            for (int i = 0; i < cap; ++i) {
                pal.add(null);
            }
            mqv = new MsgQValue(pal);
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
            // already recieved this packet ID!
            // TBD: verify same packet including data
            // TBD: increment duplicate packet count
        } else {
            pal.set(pkt.pktID, pkt);
            if (!foundInQ) {
                this.inMsgQueues.put(mqk, mqv);
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
                        //m=null;
                        //inMsgQueues.remove(mqk);
                        //sendNakPacket(h);
                    } finally {
                        if (m != null) {
                            try {
                                System.err
                                    .println("Sending Ack of recieved message "
                                        + mqk.toString());
                                sendAckPacket(h.genID, h.msgID);
                                mqv.ackSent = true;
                                mqv.setExpectedCompletion(
                                    expectedPacketLagMs, 1);
                            } catch (IOException e) {
                                // TODO handle inability to send Ack
                                e.printStackTrace();
                            }
                            retValue = m.remaining();
                            numBundlesRcvd.add(1);
                            buf.put(m);
                            // will remove from queue when recieve Ack of Ack
                            // or timeout while waiting for Ack of Ack
                            //this.inMsgQueues.remove(mqk);
                        }
                    }
                }
            }
        }
        // Check for message recieve timeout
        if (!complete && foundInQ) {
            if (System.currentTimeMillis() > (mqv.expectedCompletion + (Conduit.LAG_TIMEOUT_MULTIPLE
                * Conduit.MAX_TIMEOUTS_TIL_EXCEPTION * this.expectedPacketLagMs)))
            {
                // TBD: request resend
                throw new RuntimeException("Conduit timed out!");
            }
        }
        return retValue;
    }

    private Packet recievePacket()
        throws IllegalArgumentException, BufferUnderflowException,
        IOException
    {
        Packet rp;
        int numRead = 0;
        //while (!this.hasInput()) {
        //    try {
        //        // This should not be necessary - should only be called when
        //        // some
        //        // data is available to read...
        //        Thread.sleep(20);
        //    } catch (InterruptedException e) {
        //        // TODO Auto-generated catch block
        //        e.printStackTrace();
        //    }
        //}
        //recieve();
        numRead = recvReadBuf.position();
        rp = packetFactory.valueOf(this.recvReadBuf);
        numRead = recvReadBuf.position() - numRead;
        System.err.println("Extracted " + numRead
            + " bytes from recvReadBuf!");
        if (null != rp) {
            numPktsRcvd.add(1);
        }
        return rp;
    }

    /**
     * 'raw' write of characters directly to the {@link ByteChannel}
     */
    synchronized public void write(char[] cbuf, int off, int len)
        throws IOException
    {
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
     * Flushes the output buffered with the write(char[],int,int) method. This
     * method does nothing (other than synchronize), as the output will always
     * have been flushed as part of the call to write(char[],int,int).
     * 
     * TBD: implement flush when Conduit buffers writes
     * 
     * @throws IOException
     */
    synchronized public void flush() throws IOException {/* nothin' to do */}

    synchronized public void write(ByteBuffer buf)
        throws IOException, NullPointerException, IllegalArgumentException
    {
        // TBD: add code for buffer pool writeBufs and writer monitor thread
        Conduit.MsgQKey mqk;
        Packet[] pa;
        synchronized (this.currGen) {
            synchronized (this.currMsg) {
                mqk = new Conduit.MsgQKey(this.currGen.intValue(),
                    this.currMsg.intValue());
                this.currMsg = new Long(this.currMsg.longValue() + 1);
                if (this.outMsgQueues.containsKey(mqk)) { throw new IOException(
                    "Duplicate message key generation error in"
                        + "Conduit!\ncurrGen=" + this.currGen
                        + ", currMsg=" + (this.currMsg.longValue() - 1)); }
            }
        }
        synchronized (this.currPacketSize) {
            pa = this.packetFactory.decompose(buf, this.currPacketSize
                .shortValue(), mqk.GENERATION_ID, mqk.MSG_ID);
            outMsgQueues.put(mqk, new MsgQValue(new ArrayList(
                java.util.Arrays.asList(pa))));
            ByteBuffer bb = null;
            for (int i = 0; i < pa.length; ++i) {
                /*
                 * System.err.println("Sending packet:"+pa[i].toString());
                 * bb=pa[i].toByteBuffer(); int nw=this.byteChannel.write(bb);
                 * System.err.println("Sent "+nw+" bytes.");
                 */
                sendPacket(pa[i]);
            }
            numBundlesSent.add(1);
        }
    }

    void sendAckPacket(long g, long m) throws IOException {
        //      Send Ack packet
        Packet ackPacket = this.packetFactory.newPacket(g, m, 0, (short) 0,
            null);
        System.err.println("Sending Ack packet");
        try {
            sendPacket(ackPacket);
        } catch (IOException e) {
            // TBD: handle inability to send Ack packet
            throw e;
        } finally {
            System.err.println("Sent Ack packet");
        }
    }

    void sendNakPacket(long g, long m) throws IOException {
        //      Send Nak packet
        Packet nakPacket = this.packetFactory.newPacket(g, m, 0,
            (short) -1, null);
        System.err.println("Sending Nak packet");
        try {
            sendPacket(nakPacket);
        } catch (IOException e) {
            // TBD: handle inability to send Nak packet
            throw e;
        } finally {
            System.err.println("Sent Nak packet");
        }
    }

    void sendPacket(Packet p) throws IOException {
        System.err.println("Sending packet: " + p.toString());
        int nw = 0;
        try {
            nw = this.byteChannel.write(p.toByteBuffer());
        } catch (IOException e) {
            // TBD: handle inability to send packet
            throw e;
        } finally {
            numBytesSent.add(nw);
            System.err.println("Sent " + nw + " bytes.");
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