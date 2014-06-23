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
public class ConnectionProtocolMessageProcessor extends
	ProtocolNegotiationMessageProcessor {
    
    private static final MessageToken MSG_TOK_CONN_PROTO_NEG_BEGIN = new MessageToken(
    "_CPNeg");

    private static final byte[] SUPPORTED_PROTOCOL_VERSIONS = new byte[] { (byte) 1 };
    
    /**
     * 
     */
    public ConnectionProtocolMessageProcessor() {
	super();
    }

    /* (non-Javadoc)
     * @see com.InfoMontage.stream.ProtocolNegotiationMessageProcessor#getProtocolNegotiationBeginToken()
     */
    protected MessageToken getProtocolNegotiationBeginToken() {
	return MSG_TOK_CONN_PROTO_NEG_BEGIN;
    }

    /* (non-Javadoc)
     * @see com.InfoMontage.stream.ProtocolNegotiationMessageProcessor#getSupportedProtocolVersionArray()
     */
    protected byte[] getSupportedProtocolVersionArray() {
	return SUPPORTED_PROTOCOL_VERSIONS;
    }

}
