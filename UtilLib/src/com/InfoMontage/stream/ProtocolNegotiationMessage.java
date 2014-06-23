/**
 * ProtocolNegotiationMessage.java
 *
 * Created Nov 1, 2008 by richard
 * 
 * Part of the "Information Montage UtilityLibrary" project from
 * Information Montage. Copyright (C) 2006 Richard A. Mead
 * 
 */
package com.InfoMontage.stream;

import java.util.Collection;

/**
 * ProtocolNegotiationMessage
 *
 * @author Richard A. Mead <BR>
 *         Information Montage
 *
 */
public class ProtocolNegotiationMessage extends Message {

    /**
     * 
     */
    public ProtocolNegotiationMessage() {
	super();
    }

    /**
     * @param ime
     */
    public ProtocolNegotiationMessage(Collection ime) {
	super(ime);
    }

    /**
     * @param mea
     */
    public ProtocolNegotiationMessage(MessageElement[] mea) {
	super(mea);
    }

}
