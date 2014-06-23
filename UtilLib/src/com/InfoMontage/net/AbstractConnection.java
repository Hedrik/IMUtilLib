/*
 * AbstractConnection.java
 * 
 * Created on July 27, 2003, 9:43 AM
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
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import com.InfoMontage.failure.Failure;
import com.InfoMontage.math.BigCounter;
import com.InfoMontage.stream.ConnectionProtocolMessageProcessor;
import com.InfoMontage.stream.MessageProtocolMessageProcessor;
import com.InfoMontage.stream.MessageTemplate;
import com.InfoMontage.stream.MessageElement;
import com.InfoMontage.stream.MessageProcessor;
import com.InfoMontage.stream.MessageToken;
import com.InfoMontage.stream.ProtocolNegotiationMessageProcessor;
import com.InfoMontage.task.TaskExecutorPool;
import com.InfoMontage.util.BooleanState;
import com.InfoMontage.util.Buffer;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public abstract class AbstractConnection implements Connection, Runnable {

    public static com.InfoMontage.version.CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    public static final int MINIMUM_CONNECTION_PROTOCOL_VERSION = 1;

    public static final int MAXIMUM_CONNECTION_PROTOCOL_VERSION = 1;

    static private final byte[] SUPPORTED_PROTOCOL_VERSIONS = new byte[] { (byte) 1 };

    private transient boolean needConnProtoVers = true;

    public static Map acceptingAddresses = new Hashtable(4, 1.0f);

    // public static Stack msgProcThreadPool=null;

    private transient final SocketAddress THIS_CONNECTION_HOST_ADDR;

    private transient Socket sock = null;

    private transient ServerSocket servSock = null;

    private transient Conduit conduit = null;

    public static final short STREAM_STATE_UNINITIALIZED = -2;

    public static final short STREAM_STATE_INITIALIZED = -1;

    public static final short STREAM_STATE_AWAITING_CONNECTION = 0;

    public static final short STREAM_STATE_AWAITING_SEQ_BEGIN = 1;

    public static final short STREAM_STATE_AWAITING_SEQ_END = 2;

    private transient short streamState = STREAM_STATE_UNINITIALIZED;

    private transient boolean hasConnected = false;

    private transient int connectionProtocolVersion = -1;

    private transient int messageProtocolVersion = -1;

    private transient int defaultInputBufferSize = 1024;

    private static final TaskExecutorPool CONNECTION_THREAD_FACTORY = TaskExecutorPool
	    .getPool("ConnectionProcessors", 2, 4);

    private static final TaskExecutorPool MESSAGE_PROCESSOR_THREAD_FACTORY = TaskExecutorPool
	    .getPool("MessageProcessors", 16, 128);

    private static final ProtocolNegotiationMessageProcessor CONN_PROTO_NEG_MSG_PROC = new ConnectionProtocolMessageProcessor();

    private static final ProtocolNegotiationMessageProcessor MSG_PROTO_NEG_MSG_PROC = new MessageProtocolMessageProcessor();

    private transient volatile MessageProcessor msgProc = null;

    // TBD: set endOf_ and escapeByte as part of connection protocol
    // negotiation
    // private final byte endOf_=(byte)0;
    // private final byte escapeByte=(byte)255;

    private static Failure acceptFailReason = null;

    private transient Failure connectFailReason = null;

    private transient Failure sendFailReason = null;

    private transient Failure setMsgProcFailReason = null;

    private transient Map msgQueues = new Hashtable(7, 0.86f);

    private transient byte[] recvBuf = null; // TBD: host recvBuf in Thread

    private transient byte[] sendBuf = null; // TBD: host recvBuf in Thread?

    private transient BigCounter numMsgsSent = new BigCounter();

    private transient BigCounter numMsgsRcvd = new BigCounter();

    private transient BooleanState iShouldStop = null;

    private static class MsgQKey {

	private transient final long GENERATION_ID;

	private transient final long MSG_ID;

	private transient final int HASH_CODE;

	private MsgQKey(long g, long m) {
	    GENERATION_ID = g;
	    MSG_ID = m;
	    HASH_CODE = new Long(GENERATION_ID ^ MSG_ID).intValue();
	}

	public boolean equals(Object o) {
	    return ((o instanceof MsgQKey)
		    && (((MsgQKey) o).HASH_CODE == this.HASH_CODE)
		    && (((MsgQKey) o).GENERATION_ID == this.GENERATION_ID) && (((MsgQKey) o).MSG_ID == this.MSG_ID));
	}

	public int hashCode() {
	    return HASH_CODE;
	}
    }

    public AbstractConnection() throws IOException {
	THIS_CONNECTION_HOST_ADDR = new InetSocketAddress(InetAddress
		.getLocalHost(), 0);
	initAbstractConnection();
    }

    public AbstractConnection(int port) throws IllegalArgumentException,
	    IOException {
	THIS_CONNECTION_HOST_ADDR = new InetSocketAddress(InetAddress
		.getLocalHost(), port);
	initAbstractConnection();
    }

    public AbstractConnection(SocketAddress hostAddr) throws IOException {
	THIS_CONNECTION_HOST_ADDR = hostAddr;
	initAbstractConnection();
    }

    public AbstractConnection(Socket sock, SocketAddress hostAddr)
	    throws IOException {
	THIS_CONNECTION_HOST_ADDR = hostAddr;
	initAbstractConnection(sock);
    }

    private void initAbstractConnection() throws IOException {
	SocketChannel sc = SocketChannel.open();
	sc.configureBlocking(false);
	sock = sc.socket();
	initAbstractConnection(sock);
    }

    private void initAbstractConnection(Socket sock)
	    throws IllegalArgumentException, IOException {
	SocketChannel sc = sock.getChannel();
	if (null == sc) {
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to create a connection using a socket with no SocketChannel!"));
	}
	if (!sock.isConnected() && !sock.isBound()) {
	    sock.bind(THIS_CONNECTION_HOST_ADDR);
	}
	streamState = STREAM_STATE_INITIALIZED;
    }

    synchronized public int getMinimumConnectionProtocolVersion() {
	return MINIMUM_CONNECTION_PROTOCOL_VERSION;
    }

    synchronized public int getMaximumConnectionProtocolVersion() {
	return MAXIMUM_CONNECTION_PROTOCOL_VERSION;
    }

    synchronized public int getConnectionProtocolVersion() {
	return connectionProtocolVersion;
    }

    synchronized public int getMessageProtocolVersion() {
	return messageProtocolVersion;
    }

    synchronized public boolean accept() throws IllegalStateException {
	boolean didConnect = true;
	if (null == msgProc) {
	    throw (IllegalStateException) new IllegalStateException()
		    .initCause(new NullPointerException(
			    "Attempt to accept connections before assigning a message processor!"));
	}
	if (streamState == STREAM_STATE_UNINITIALIZED) {
	    acceptFailReason = ConnectionAcceptFailure.ACCEPT_FAILURE_REASON_UNINITIALIZED;
	    didConnect = false;
	}
	if (didConnect && isConnected()) {
	    acceptFailReason = ConnectionAcceptFailure.ACCEPT_FAILURE_REASON_ALREADY_CONNECTED;
	    didConnect = false;
	}
	if (didConnect)
	    didConnect = internalAccept();
	return didConnect;
    }

    // synchronized public boolean accept(MessageProcessor connMsgProc)
    public static synchronized boolean accept(final InetSocketAddress hostAddr,
	    final MessageProcessor connMsgProc)
	    throws IllegalArgumentException, IOException {
	boolean didConnect = false;
	// if (null==msgProc)
	// throw (IllegalStateException) new IllegalStateException()
	// .initCause(new NullPointerException(
	// "Attempt to connect before assigning a message processor!"));
	if (null == connMsgProc) {
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to accept connections using a Null message processor!"));
	}
	AbstractConnection acceptor = staticNewConnectionObj(hostAddr);
	didConnect = acceptor.setMessageProcessor(connMsgProc);
	if (didConnect) {
	    didConnect = acceptor.accept();
	} // TBD: set failureReason
	return didConnect;
    }

    synchronized protected boolean internalAccept() {
	boolean didConnect = false;
	synchronized (acceptingAddresses) {
	    if (acceptingAddresses.containsKey(THIS_CONNECTION_HOST_ADDR)) {
		acceptFailReason = ConnectionAcceptFailure.ACCEPT_FAILURE_REASON_HOSTADDR_ALREADY_ACCEPTING;
	    } else {
		sock = null;
		try {
		    // servSock = new ServerSocket();
		    ServerSocketChannel ssc = ServerSocketChannel.open();
		    ssc.configureBlocking(false);
		    servSock = ssc.socket();
		    try {
			servSock.setReuseAddress(true);
			servSock.bind(THIS_CONNECTION_HOST_ADDR);
			acceptingAddresses.put(THIS_CONNECTION_HOST_ADDR, this);
			streamState = STREAM_STATE_AWAITING_CONNECTION;
			new Thread(this).start();
			//Thread.yield();
			didConnect = true;
		    } catch (IOException e) {
			e.printStackTrace();
			// TDB: set accept fail reason
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		    // TDB: set accept fail reason
		}
		// acceptFailReason=ConnectionAcceptFailure.ACCEPT_FAILURE_REASON_STUB_CODE;
	    }
	}
	if (didConnect) {
	    acceptFailReason = null;
	}
	return didConnect;
    }

    synchronized public Failure getAcceptFailureReason() {
	return acceptFailReason;
    }

    public synchronized boolean connect(SocketAddress a)
	    throws IllegalArgumentException, IllegalStateException {
	if (null == a) {
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to connect to a Null socket address!"));
	}
	if (null == msgProc) {
	    throw (IllegalStateException) new IllegalStateException()
		    .initCause(new NullPointerException(
			    "Attempt to connect before assigning a message processor!"));
	}
	return internalConnect(a);
    }

    public synchronized boolean connect(SocketAddress a,
	    MessageProcessor connMsgProc) throws IllegalArgumentException {
	boolean didConnect = false;
	if (null == a) {
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to connect to a Null socket address!"));
	}
	if (null == connMsgProc) {
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to connect using a Null message processor!"));
	}
	didConnect = setMessageProcessor(connMsgProc);
	if (didConnect) {
	    didConnect = internalConnect(a);
	}
	return didConnect;
    }

    protected synchronized boolean internalConnect(SocketAddress a) {
	boolean didConnect = true;
	connectFailReason = null;
	if (streamState == STREAM_STATE_UNINITIALIZED) {
	    connectFailReason = ConnectionFailure.CONNECT_FAILURE_REASON_UNINITIALIZED;
	    didConnect = false;
	}
	if (didConnect && isConnected()) {
	    connectFailReason = ConnectionFailure.CONNECT_FAILURE_REASON_ALREADY_CONNECTED;
	    didConnect = false;
	}
	// TBD: connect to server
	SocketChannel sc = null;
	if (didConnect) {
	    try {
		sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(a);
	    } catch (AlreadyConnectedException e) {
		connectFailReason = (ConnectionFailure) ConnectionFailure.CONNECT_FAILURE_REASON_ALREADY_CONNECTED
			.initCause(e);
		didConnect = false;
		sc = null;
	    } catch (IOException e) {
		connectFailReason = (ConnectionFailure) ConnectionFailure.CONNECT_FAILURE_REASON_GENERIC
			.initCause(e);
		didConnect = false;
		sc = null;
	    }
	}
	if (didConnect) {
	    SelectionKey sk = null;
	    try {
		Selector s = sc.provider().openSelector();
		sk = sc.register(s, SelectionKey.OP_CONNECT);
		if (!((sc.finishConnect()) || ((s.select(5000) > 0) && (sc
			.finishConnect())))) {
		    connectFailReason = (ConnectionFailure) ConnectionFailure.CONNECT_FAILURE_REASON_TIMEOUT;
		    didConnect = false;
		}
	    } catch (ClosedChannelException e) {
		connectFailReason = (ConnectionFailure) ConnectionFailure.CONNECT_FAILURE_REASON_CLOSED
			.initCause(e);
		didConnect = false;
		sc = null;
	    } catch (ClosedSelectorException e) {
		connectFailReason = (ConnectionFailure) ConnectionFailure.CONNECT_FAILURE_REASON_CLOSED
			.initCause(e);
		didConnect = false;
		sc = null;
	    } catch (IOException e) {
		connectFailReason = (ConnectionFailure) ConnectionFailure.CONNECT_FAILURE_REASON_GENERIC
			.initCause(e);
		didConnect = false;
		sc = null;
	    } finally {
		if (!didConnect && (null != sk)) {
		    sk.cancel();
		    sk = null;
		}
	    }
	}
	if (didConnect) {
	    try {
		conduit = new Conduit(sc);
	    } catch (Exception e) {
		connectFailReason = (ConnectionFailure) ConnectionFailure.CONNECT_FAILURE_REASON_GENERIC
			.initCause(e);
		didConnect = false;
		sc = null;
		conduit = null;
	    }
	}
	if (didConnect) {
	    // TBD: negotiate connection protocol version?
	    // TBD: assign connection protocol handler?
	    // TBD: negotiate messsage protocol version?
	    connectFailReason = Failure.FAILURE_REASON_STUB_CODE;
	}
	if (didConnect) {
	    connectFailReason = null;
	    streamState = STREAM_STATE_AWAITING_SEQ_BEGIN;
	    hasConnected = true;
	}
	return didConnect;
    }

    synchronized public Failure getConnectFailureReason() {
	return connectFailReason;
    }

    synchronized public boolean isConnected() {
	return ((null != conduit) && (conduit.isOpen())
		&& (streamState != STREAM_STATE_AWAITING_CONNECTION)
		&& (streamState != STREAM_STATE_UNINITIALIZED) && (streamState != STREAM_STATE_INITIALIZED));
    }

    synchronized public void close() throws IOException {
	// connectFailReason=null;
	// sendFailReason=null;
	// connectionProtocolVersion=-1;
	// messageProtocolVersion=-1;
	if (streamState != STREAM_STATE_UNINITIALIZED) {
	    streamState = STREAM_STATE_INITIALIZED;
	}
	if (null != conduit) {
	    conduit.close();
	}
	msgProc = null;
	hasConnected = false;
	if (null != servSock) {
	    acceptingAddresses.remove(THIS_CONNECTION_HOST_ADDR);
	}
    }

    synchronized public BigInteger getBytesRcvd() {
	return (null == conduit) ? null : conduit.getBytesRcvd();
    }

    synchronized public BigInteger getBytesSent() {
	return (null == conduit) ? null : conduit.getBytesSent();
    }

    synchronized public BigInteger getPacketsRcvd() {
	return (null == conduit) ? null : conduit.getPktsRcvd();
    }

    synchronized public BigInteger getPacketsSent() {
	return (null == conduit) ? null : conduit.getPktsSent();
    }

    synchronized public BigInteger getBundlesRcvd() {
	return (null == conduit) ? null : conduit.getBundlesRcvd();
    }

    synchronized public BigInteger getBundlesSent() {
	return (null == conduit) ? null : conduit.getBundlesSent();
    }

    synchronized public BigInteger getMessagesRcvd() {
	return numMsgsRcvd.get();
    }

    synchronized public BigInteger getMessagesSent() {
	return numMsgsSent.get();
    }

    public SocketAddress getBoundAddress() {
	return (null == sock) ? ((null == servSock) ? null : servSock
		.getLocalSocketAddress()) : (sock.getLocalSocketAddress());
    }

    synchronized public boolean send(final ByteBuffer bbuf)
	    throws IllegalArgumentException, IllegalStateException {
	return send(bbuf, true);
    }

    synchronized public boolean send(final ByteBuffer bbuf,
	    final boolean immediate) throws IllegalArgumentException,
	    IllegalStateException {
	boolean sent = false;
	if (null == bbuf) {
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to send a Null buffer!"));
	}
	if (!hasConnected) {
	    throw new IllegalStateException(
		    "Attempt to send before connecting!");
	}
	if (!isConnected()) {
	    sendFailReason = ConnectionSendFailure.SEND_FAIL_REASON_NOT_CONNECTED;
	} else {
	    // sendFailReason = Failure.FAILURE_REASON_STUB_CODE;
	    try {
		this.conduit.write(bbuf);
		if (immediate) {
		    this.conduit.flush();
		}
		sent = true;
	    } catch (IOException e) {
		e.printStackTrace();
		sendFailReason = ConnectionSendFailure.SEND_FAIL_REASON_CANNOT_WRITE;
	    }
	}
	if (sent) {
	    sendFailReason = null;
	} else {
	    sendFailReason = ConnectionSendFailure.SEND_FAIL_REASON_UNKNOWN;
	}
	return sent;
    }

    synchronized public Failure getSendFailureReason() {
	return sendFailReason;
    }

    synchronized public Class getMessageProcessorClass() {
	return (msgProc == null) ? null : msgProc.getClass();
    }

    synchronized public MessageProcessor getMessageProcessor() {
	return msgProc;
    }

    synchronized public boolean setMessageProcessor(MessageProcessor mp)
	    throws IllegalArgumentException {
	boolean msgProcSet = false;
	if (mp == null) {
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to set a connection's MessageProcessor to null!"));
	}
	if ((msgProc != null) && (isConnected())) {
	    // TBD: renegotiate message protocol version
	    // or set failure reason code
	    setMsgProcFailReason = ConnectionSetMsgProcFailure.SET_MSG_FAILURE_REASON_PROTOCOL;
	    setMsgProcFailReason = Failure.FAILURE_REASON_STUB_CODE;
	} else {
	    msgProcSet = true;
	}
	if (msgProcSet) {
	    msgProc = mp;
	    setMsgProcFailReason = null;
	}
	return msgProcSet;
    }

    synchronized public Failure getSetMsgProcFailureReason() {
	return setMsgProcFailReason;
    }

    synchronized private byte negotiateProtocolVersion(byte[] protocolVers)
	    throws Exception, ConnectionSendFailure {
	byte negProt = 0;
	if (isConnected()) {
	    ByteBuffer bb = com.InfoMontage.util.Buffer
		    .create(ProtocolNegotiationMessageProcessor.MSG_TOK_PROTO_REQ);
	    synchronized (msgProc) {
		MessageProcessor oMsgProc = msgProc;
		msgProc = CONN_PROTO_NEG_MSG_PROC;
		boolean protoMatched = false;
		byte[] negProtoArray = new byte[255];
		Arrays.fill(negProtoArray, (byte) 0);
		for (int i = 0, j = protocolVers.length; j > 0; i++, j--) {
		    negProtoArray[i] = protocolVers[j - 1];
		}
		InputStream sis = sock.getInputStream();
		ByteBuffer bytesRecvdBuf;
		MessageElement elemRecvd;
		if (!this
			.send((ByteBuffer) ProtocolNegotiationMessageProcessor.MSG_TOK_PROTO_REQ
				.encode(bb, negProtoArray).flip())) {
		    // TBD: Handle this better
		    getSendFailureReason().throwException(
			    "Send error while"
				    + "negotiating protocol version!");
		}
		do {
		    Thread.sleep(20);
		    // } while (sis.available() == 0); // We should timeout
		    // at
		} while (!this.conduit.hasInput()); // We should timeout at
		// some point
		/*
                 * if (null == recvBuf || recvBuf.length < sis.available()) {
                 * recvBuf = new byte[sis.available()]; } bytesRecvdBuf =
                 * ByteBuffer.wrap(recvBuf, 0, sis .read(recvBuf));
                 */
		bytesRecvdBuf = this.conduit.read();
		elemRecvd = MessageElement.nextElement(bytesRecvdBuf);
		if (null == elemRecvd) {
		    // TBD: Handle unrecognized element in initial
		    // protocol
		    // negotiation - Big bad problem...
		} else if (elemRecvd.tag == ProtocolNegotiationMessageProcessor.MSG_TOK_PROTO_REQ) {
		    byte[] recvProtoArray = elemRecvd.pByte;
		    for (int i = 0; (i < recvProtoArray.length)
			    && (!protoMatched); i++) {
			if (recvProtoArray[i] > 0) {
			    negProt = recvProtoArray[i];
			    protoMatched = true;
			}
		    }
		} else {
		    // TBD: Handle recognized element in initial protocol
		    // negotiation that is not part of negotiation...
		}
		// TBD: finish negotiation
		if (!protoMatched) {
		    // TBD: Handle no matching protocol version found
		}
		msgProc = oMsgProc;
	    }
	}
	return negProt;
    }

    /*
         * private Packet recievePacket() throws IllegalArgumentException,
         * BufferUnderflowException, IOException { InputStream
         * sis=sock.getInputStream(); ByteBuffer bytesRecvdBuf; Packet rp; do {
         * try { // This should not be necessary - should only be called when
         * some // data is available to read... Thread.sleep(20); } catch
         * (InterruptedException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); } } while (sis.available()==0); if
         * (null==recvBuf || recvBuf.length <sis.available()) { recvBuf=new
         * byte[sis.available()]; }
         * bytesRecvdBuf=ByteBuffer.wrap(recvBuf,0,sis.read(recvBuf));
         * rp=Packet.valueOf(bytesRecvdBuf); return rp; }
         */
    public void run() {
	boolean running = true;
	ByteBuffer m = ByteBuffer.allocate(defaultInputBufferSize);
	iShouldStop = new BooleanState();
	while (running) {
	    if (conduit != null) {
		if (needConnProtoVers) {
		    try {
			connectionProtocolVersion = negotiateProtocolVersion(SUPPORTED_PROTOCOL_VERSIONS);
		    } catch (ConnectionSendFailure e) {
			// TODO Auto-generated catch block
			if (e == ConnectionSendFailure.SEND_FAIL_REASON_NOT_CONNECTED) {
			    conduit = null;
			} else {
			    e.printStackTrace();
			}
		    } catch (IOException e) {
			conduit = null;
			iShouldStop.setState(true);
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    needConnProtoVers = false;
		}
		int ba = 0;
		// try {
		ba = conduit.read(m);
		// } catch (IOException e) {
		// TBD: handle IOException
		// }
		if (ba > 0) {

		    // TBD: process msg
		    // MessageProcessorThread mpt
		    // = MessageProcessorThread.getThread();
		    // msgProc.setMessageToProcess(m);
		    // mpt.setMessageProcessingParameters(msgProc, m, null);
		    // mpt.notify();
		    MessageProcessor mp = getMessageProcessor();
		    MessageTemplate msg = mp.getMessageTemplate();
		    try {
			MESSAGE_PROCESSOR_THREAD_FACTORY.doTask(mp,
				new Object[] { m, msg, conduit }, false);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    } else {
		if (servSock != null) {
		    try {
			// Socket s = servSock.accept();
			ServerSocketChannel ssc = servSock.getChannel();
			SocketChannel sc = ssc.accept();
			if (null == sc) {
			    Thread.sleep(10);
			} else {
			    sc.configureBlocking(false);
			    Socket s = sc.socket();
			    AbstractConnection c = newConnectionObj(s,
				    THIS_CONNECTION_HOST_ADDR);
			    c.msgProc = this.msgProc;
			    c.conduit = new Conduit(s.getChannel());
			    c.streamState = STREAM_STATE_AWAITING_SEQ_BEGIN;
			    c.hasConnected = true;
			    new Thread(c).start();
			    // TBD: assign connection protocol handler??
			    // TBD: negotiate messsage protocol version?
			    // c.callBackServer=this.callBackServer;
			    // connectionPoller.add(c);
			}
		    } catch (IOException e) {
			iShouldStop.setState(true);
		    } catch (InterruptedException e) {
			iShouldStop.setState(true);
		    }
		} else { // connection died?
		    // TBD: handle connection death
		}
	    }
	    synchronized (iShouldStop) {
		if (iShouldStop.getState()) {
		    running = false;
		}
	    }
	}
	iShouldStop = null;
	// TBD: add cleanup code
    }

    abstract protected AbstractConnection newConnectionObj(
	    SocketAddress sockAddr) throws IOException;

    abstract protected AbstractConnection newConnectionObj(Socket sock,
	    SocketAddress sockAddr) throws IOException;

    static protected AbstractConnection staticNewConnectionObj(
	    SocketAddress sockAddr) throws IllegalStateException, IOException {
	throw new IllegalStateException(
		"The method \"staticNewConnectionObj\""
			+ " in class AbstractConnection must be overridden by subclasses!");
    }

    static protected AbstractConnection staticNewConnectionObj(Socket sock,
	    SocketAddress sockAddr) throws IllegalStateException, IOException {
	throw new IllegalStateException(
		"The method \"staticNewConnectionObj\""
			+ " in class AbstractConnection must be overridden by subclasses!");
    }

    protected abstract class AbstractConnectionProtocolHandler {

	protected AbstractConnectionProtocolHandler() {
	}

	public abstract int[] getSupportedProtocolVersions();
	// TBD: negotiate connection protocol version
    }

}