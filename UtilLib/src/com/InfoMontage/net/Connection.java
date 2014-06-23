/*
 * Connection.java
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
import java.math.BigInteger;
import java.net.InetSocketAddress;
import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;
import com.InfoMontage.failure.Failure;
import com.InfoMontage.stream.MessageProcessor;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public interface Connection {

    static final CodeVersion INTERFACE_CODE_VERSION = com.InfoMontage.version.GenericCodeVersion
        .codeVersionFromCVSRevisionString("$Revision$");

    int getMinimumConnectionProtocolVersion();

    int getMaximumConnectionProtocolVersion();

    int getConnectionProtocolVersion();

    int getMessageProtocolVersion();

    boolean accept() throws IllegalStateException;

    /*
     boolean accept(InetSocketAddress hostAddr, MessageProcessor
     connMsgProc)
    throws IllegalArgumentException, IllegalStateException;
    */
    
    Failure getAcceptFailureReason();

    boolean connect(java.net.SocketAddress a)
        throws IllegalArgumentException, IllegalStateException;

    boolean connect(java.net.SocketAddress a, MessageProcessor connMsgProc)
        throws IllegalArgumentException;

    Failure getConnectFailureReason();

    boolean isConnected();

    void close() throws IOException;

    BigInteger getBytesRcvd();

    BigInteger getBytesSent();

    BigInteger getMessagesRcvd();

    BigInteger getMessagesSent();

    boolean send(final java.nio.ByteBuffer bbuf, final boolean immediate)
        throws IllegalArgumentException, IllegalStateException;

    Failure getSendFailureReason();

    boolean setMessageProcessor(MessageProcessor mp)
        throws IllegalArgumentException;

    MessageProcessor getMessageProcessor() throws IllegalArgumentException;

    Class getMessageProcessorClass();

    Failure getSetMsgProcFailureReason();

}
