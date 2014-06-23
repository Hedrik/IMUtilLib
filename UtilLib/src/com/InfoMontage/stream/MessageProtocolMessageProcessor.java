/**
 * ConnectionProtocolMessageProcessor.java
 *
 * Created Nov 2, 2008 by richard
 * 
 * Part of the "Information Montage UtilityLibrary" project from
 * Information Montage. Copyright (C) 2006 Richard A. Mead
 * 
 */
package com.InfoMontage.stream;

/**
 * ConnectionProtocolMessageProcessor
 *
 * @author Richard A. Mead <BR>
 *         Information Montage
 *
 */
public class MessageProtocolMessageProcessor extends
	ProtocolNegotiationMessageProcessor {
    
    public static final MessageToken MSG_TOK_MSG_PROTO_NEG_BEGIN = new MessageToken(
    "_MPNeg");

    private static final byte[] SUPPORTED_PROTOCOL_VERSIONS = new byte[] { (byte) 1 };
    
    /**
     * 
     */
    public MessageProtocolMessageProcessor() {
	super();
    }

    /* (non-Javadoc)
     * @see com.InfoMontage.stream.ProtocolNegotiationMessageProcessor#getProtocolNegotiationBeginToken()
     */
    protected MessageToken getProtocolNegotiationBeginToken() {
	return MSG_TOK_MSG_PROTO_NEG_BEGIN;
    }

    /* (non-Javadoc)
     * @see com.InfoMontage.stream.ProtocolNegotiationMessageProcessor#getSupportedProtocolVersionArray()
     */
    protected byte[] getSupportedProtocolVersionArray() {
	return SUPPORTED_PROTOCOL_VERSIONS;
    }

}
