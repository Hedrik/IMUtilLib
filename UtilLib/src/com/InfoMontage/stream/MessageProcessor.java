/*
 * MessageProcessor.java
 *
 * Created on August 3, 2003, 4:50 PM
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

import java.nio.ByteBuffer;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public interface MessageProcessor extends com.InfoMontage.task.Task {

    /**
         * Interface file version. By convention, for use with
         * {@link com.InfoMontage.util.CodeVersion} methods, interface versions
         * are kept in a static field named <code>INTERFACE_CODE_VERSION</code>.
         * 
         * @see com.InfoMontage.util.CodeVersion
         *      com.InfoMontage.version.CodeVersion
         *      com.InfoMontage.version.GenericCodeVersion
         */
    static final CodeVersion INTERFACE_CODE_VERSION = GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    static final int DEFAULT_MINIMUM_MESSAGE_BUFFER_SIZE = 1023;

    static final int ABSOLUTE_MINIMUM_MESSAGE_BUFFER_SIZE = 255;

    byte[] getSupportedProtocolVersions();

    void setMessageProtocolVersion(byte protVers)
	    throws IllegalArgumentException;

    void setMinimumMessageSize(int minMsgSz) throws IllegalArgumentException;

    int getMinimumMessageSize();
    
    /**
     * States the MessageTemplate types this MessageProcessor can process.
     * @return Class - the type (or supertype) of Messages understood, or null if not defined.
     */
    MessageTemplate getMessageTemplate();

    Class getMessageType();

}
