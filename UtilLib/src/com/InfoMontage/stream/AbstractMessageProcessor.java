/*
 * AbstractMessageProcessor.java
 * 
 * Created on August 9, 2003, 10:45 PM
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

package com.InfoMontage.stream;

import com.InfoMontage.version.*;
import com.InfoMontage.net.Conduit;
import com.InfoMontage.net.Connection;
import com.InfoMontage.stream.MessageTemplate;
import com.InfoMontage.task.Task;

import java.nio.ByteBuffer;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public abstract class AbstractMessageProcessor implements MessageProcessor,
	Task {

    static private final CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
    .codeVersionFromCVSRevisionString("$Revision$");

    private volatile int msgProtocolVersion = 0;

    private volatile int minMsgBufferSize = DEFAULT_MINIMUM_MESSAGE_BUFFER_SIZE;

    private ByteBuffer msgBufToProcess = null;

    private MessageTemplate msgTypeToProcess = null;

    private Connection connectionToReplyOn = null;

    /** Creates a new instance of AbstractMessageProcessor */
    protected AbstractMessageProcessor() {
    }

    public abstract byte[] getSupportedProtocolVersions();

    synchronized public void setMessageProtocolVersion(final byte p)
	    throws IllegalArgumentException {
	if (!isSupportedProtocol(p))
	    throw new IllegalArgumentException(
		    "Attempt to set an invalid message"
			    + " protocol version of '" + p
			    + "' - valid versions are:\n"
			    + protocolListAsString());
	msgProtocolVersion = p;
    }

    synchronized public void setMinimumMessageSize(final int s)
	    throws IllegalArgumentException {
	if (s < ABSOLUTE_MINIMUM_MESSAGE_BUFFER_SIZE)
	    throw new IllegalArgumentException(
		    "Attempt to set minimum message " + "size to less than "
			    + ABSOLUTE_MINIMUM_MESSAGE_BUFFER_SIZE + "!");
	minMsgBufferSize = s;
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see com.InfoMontage.task.Task#setParameters(java.lang.Object[])
	 */
    public void setParameters(Object[] pa) throws IllegalArgumentException {
	setMessageToProcess((ByteBuffer) pa[0], (MessageTemplate) pa[1], (Connection) pa[2]);
    }

    synchronized public Exception validateParameters(Object[] pa)
	    throws IllegalArgumentException {
	Exception retVal = null;
	ByteBuffer b = null;
	Message m = null;
	Connection c = null;
	if (pa.length < 3) {
	    retVal = (IllegalArgumentException) new IllegalArgumentException(
		    "Too few task parameters: " + pa.length + " instead of 3!");
	}
	try {
	    b = (ByteBuffer) pa[0];
	    try {
		m = (Message) pa[1];
		try {
		    c = (Connection) pa[2];
		} catch (ClassCastException e) {
		    retVal = (IllegalArgumentException) new IllegalArgumentException(
			    "Second task parameter is a " + pa[1].getClass()
				    + " instead of a Message!").initCause(e);
		}
	    } catch (ClassCastException e) {
		retVal = (IllegalArgumentException) new IllegalArgumentException(
			"Second task parameter is a " + pa[1].getClass()
				+ " instead of a Message!").initCause(e);
	    }
	} catch (ClassCastException e) {
	    retVal = (IllegalArgumentException) new IllegalArgumentException(
		    "First task parameter is a " + pa[0].getClass()
			    + " instead of a ByteBuffer!").initCause(e);
	}
	return retVal;
    }

    synchronized public void clearParameters() {
	msgBufToProcess = null;
	msgTypeToProcess = null;
    }

    synchronized public void setMessageToProcess(final ByteBuffer msgBuf,
	    final MessageTemplate msgType, final Connection conn) throws IllegalArgumentException {
	if ((null != msgBufToProcess) && (msgBufToProcess.position() != 0))
	    throw new IllegalStateException(
		    "Attempt to set new message to "
			    + "process before previous message has been fully processed!");
	if (null == msgBuf)
	    throw (IllegalArgumentException) new IllegalArgumentException()
		    .initCause(new NullPointerException(
			    "Attempt to initialize a MessageProcessor with a null message!"));
	if (!msgBuf.hasRemaining())
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to initialize a MessageProcessor with an empty message!");
	synchronized (msgBuf) {
	    if ((null == msgBufToProcess)
		    || (msgBufToProcess.capacity() < msgBuf.remaining())) {
		msgBufToProcess = java.nio.ByteBuffer.allocate((msgBuf
			.remaining() > minMsgBufferSize) ? msgBuf.remaining()
			: minMsgBufferSize);
	    }
	    msgBufToProcess.put(msgBuf);
	    msgTypeToProcess = msgType;
	    connectionToReplyOn = conn;
	}
    }

    public void doTask() throws IllegalArgumentException,
	    UnknownMessageException {
	Message m;
	if (msgBufToProcess == null) {
	    throw new IllegalStateException("Attempt to process a null message"
		    + " buffer!");
	}
	if (null == msgTypeToProcess)
	    m = new Message().setToValueOf(msgBufToProcess);
	else
	    try {
		m = msgTypeToProcess.valueOf(msgBufToProcess);
	    } catch (InstantiationException e) {
		// Should never happen!
		throw (IllegalArgumentException) new IllegalArgumentException(
			"Message type somehow unable to be instantiated!")
			.initCause(e);
	    } catch (IllegalAccessException e) {
		// Should never happen!
		throw (IllegalArgumentException) new IllegalArgumentException(
			"Message type unable to be instantiated because of insufficient"
				+ " security privileges!").initCause(e);
	    }
	if (!processMessage(connectionToReplyOn, m))
	    throw new UnknownMessageException("Recieved an unknown and/or "
		    + "unprocessable message!");
    }

    public abstract boolean processMessage(Connection connectionToReplyOn, Message msg);

    synchronized public boolean isSupportedProtocol(final byte p) {
	boolean supported = false;
	if (p > 0) {
	    byte[] spv = getSupportedProtocolVersions();
	    byte[] pva = new byte[spv.length];
	    System.arraycopy((Object) spv, 0, (Object) pva, 0, pva.length);
	    java.util.Arrays.sort(pva);
	    if (java.util.Arrays.binarySearch(pva, p) >= 0)
		supported = true;
	}
	return supported;
    }

    synchronized public String protocolListAsString() {
	StringBuffer s = new StringBuffer();
	byte[] pva = getSupportedProtocolVersions();
	s.append((pva.length == 0) ? "None" : String.valueOf(pva[0]));
	for (int i = 1; i < pva.length; i++)
	    s.append(", ").append(pva[i]);
	return s.toString();
    }

    public int getMinimumMessageSize() {
	return MessageToken.getMinLength();
    }

}