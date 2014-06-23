/*
 * ProtocolNegotiationMessageProcessor.java
 *
 * Created on November 9, 2003, 6:14 PM
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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;

import com.InfoMontage.net.Conduit;
import com.InfoMontage.net.Connection;
import com.InfoMontage.util.Buffer;

/**
 * 
 * @author Richard
 */
public abstract class ProtocolNegotiationMessageProcessor extends
	AbstractMessageProcessor {

    private byte[] SUPPORTED_PROTOCOL_VERSIONS = getSupportedProtocolVersionArray();

    protected abstract byte[] getSupportedProtocolVersionArray();

    private byte negotiatedProtocol = 0;
    
    private MessageToken MSG_TOK_PROTO_NEG_BEGIN = getProtocolNegotiationBeginToken();
    
    protected abstract MessageToken getProtocolNegotiationBeginToken();

     public static final MessageToken MSG_TOK_PROTO_REQ = new MessageToken(
	    "_PReq", 16);

    public static final MessageToken MSG_TOK_PROTO_SEL = new MessageToken(
	    "_PSel", 1);

    /** Creates a new instance of ProtocolNegotiationMessageProcessor */
    public ProtocolNegotiationMessageProcessor() {
	super();
	assert(SUPPORTED_PROTOCOL_VERSIONS.length <= MSG_TOK_PROTO_REQ.pLen);
    }

    protected static class ProtocolNegotiationMessageTemplate extends MessageTemplate {

	protected static final Class MESSAGE_TYPE = ProtocolNegotiationMessage.class;
	    
	ProtocolNegotiationMessageTemplate() {
	    super();
	}

    }
    
    protected static final MessageTemplate MSG_TMPL_PROTO_NEG = new ProtocolNegotiationMessageTemplate();
    
    public MessageTemplate getMessageTemplate() {
	return MSG_TMPL_PROTO_NEG;
    }

    public byte[] getSupportedProtocolVersions() {
	// Maybe just return the static final?
	byte[] rspva = new byte[SUPPORTED_PROTOCOL_VERSIONS.length];
	System
		.arraycopy(SUPPORTED_PROTOCOL_VERSIONS, 0, rspva, 0,
			rspva.length);
	Arrays.sort(rspva, 0, rspva.length);
	return rspva;
    }

    public boolean processMessage(Connection connectionToReplyOn, Message msg) {
	boolean processable = false;
	if (	(null != msg)
		&& (msg instanceof ProtocolNegotiationMessage)
		&& (msg.size() == 1) ) {
	    MessageElement e = (MessageElement)msg.removeFirst();
	    ByteBuffer rbb = null;
	    if (e.tag == MSG_TOK_PROTO_NEG_BEGIN)  {
		rbb = Buffer.create(this.MSG_TOK_PROTO_REQ);
		byte[] ba = new byte[SUPPORTED_PROTOCOL_VERSIONS.length];
		for (int i = 0; i<ba.length; i++) {
			ba[i] = SUPPORTED_PROTOCOL_VERSIONS[i];
			}
		this.MSG_TOK_PROTO_REQ.encode(rbb, ba);
	    } else if (e.tag == MSG_TOK_PROTO_REQ) {
		rbb = Buffer.create(this.MSG_TOK_PROTO_SEL);
		byte[] ba = e.pByte;
		Arrays.sort(SUPPORTED_PROTOCOL_VERSIONS, 0, SUPPORTED_PROTOCOL_VERSIONS.length);
		for (int i = 0; i<ba.length; i++) {
		if (Arrays.binarySearch(SUPPORTED_PROTOCOL_VERSIONS, ba[i]) < 0) {
		    ba[i] = 0;
		}
	    }
		Arrays.sort(ba, 0, ba.length);
		negotiatedProtocol = ba[ba.length-1];
		this.MSG_TOK_PROTO_SEL.encode(rbb,negotiatedProtocol);
	    } else if  (e.tag == MSG_TOK_PROTO_SEL) {
		Arrays.sort(SUPPORTED_PROTOCOL_VERSIONS, 0, SUPPORTED_PROTOCOL_VERSIONS.length);
		if (Arrays.binarySearch(SUPPORTED_PROTOCOL_VERSIONS, e.pByte[0]) < 0) {
		rbb = Buffer.create(MessageToken.MSG_TOKEN_NAK);
		} else {
			negotiatedProtocol = e.pByte[0];
			rbb = Buffer.create(MessageToken.MSG_TOKEN_ACK);
		}
	    } else if  (e.tag == MessageToken.MSG_TOKEN_ACK) {
		e = null;
	    } else if  (e.tag == MessageToken.MSG_TOKEN_NAK) {
		negotiatedProtocol = 0;
		} else {
		    // This should never happen!
		    throw (UnknownMessageException) new UnknownMessageException(
			    "Received ProtocolNegotiationMessage with unknown MessageToken!\n"+
			    "\""+e.toString()+"\"");
		}
	    if (null != rbb) {
		connectionToReplyOn.send(rbb, true);
	    }
		processable = true;
	    }
        return processable;
    }

    public byte getProtocolVersion() {
	return negotiatedProtocol;
    }

    public int getMinimumMessageSize() {
	return 1;
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#getResults()
         */
    public Object[] getResults() throws IllegalStateException {
	return new Object[] { new Byte(getProtocolVersion()) };
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#percentComplete()
         */
    public float percentComplete() {
	return negotiatedProtocol == 0 ? 0f : 100f;
    }

    /* (non-Javadoc)
     * @see com.InfoMontage.stream.MessageProcessor#getMessageType()
     */
    public Class getMessageType() {
	return ProtocolNegotiationMessageTemplate.MESSAGE_TYPE;
    }

}
