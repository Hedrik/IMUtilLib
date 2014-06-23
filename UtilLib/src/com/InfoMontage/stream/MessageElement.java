/*
 * MessageElement.java
 *
 * Created on July 19, 2003, 12:47 AM
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

import com.InfoMontage.stream.MessageToken;
import java.nio.ByteBuffer;

public final class MessageElement {
    
    // TBD: incorporate delimiter and escape bytes
    
    private static final java.nio.charset.Charset chrset
    =com.InfoMontage.util.Buffer.DEFAULT_CHAR_SET;
    
    public MessageToken tag=null;
    public byte[] pByte=null; // byte array payload
    public String pString=null; // String payload
    
    private MessageElement() {}
    
    synchronized public String toString() {
        StringBuffer rs=new StringBuffer();
        if (tag==null)
            return "[null]";
        else {
            synchronized (tag) {
                rs.append(tag.sTag);
                if (pByte==null)
                    if (pString!=null)
                        synchronized (pString) {
                            rs.append("{"+pString+"}");
                        }
                    else { /* both null...? */ }
                else {
                    synchronized (pByte) {
                        // TBD: This should be a comma seperated list of byte values
                        rs.append("["+pByte.toString()+"]");
                    }
                }
                rs.append((tag.pLen<0)?"/"+tag.eTag:"");
            }
        }
        return rs.toString();
    }
    
    public ByteBuffer append(ByteBuffer bb) {
        if (tag.eTag!=null)
            tag.encode(bb,pString);
        else if (tag.pLen==0)
            tag.encode(bb);
        else if (tag.pLen>0)
            tag.encode(bb,pByte);
        return bb;
    }
    
    public static MessageElement nextElement(ByteBuffer bb) {
        MessageElement retElem=null;
        synchronized (bb) {
            byte[] tba=null;
            String ts=null;
            MessageToken t=MessageToken.nextMessageToken(bb);
            if (t!=null) {
                //e=new MessageElement(); // shouldn't need to synchronize since it's new
                //e.tag=t;
                if (t.pLen>0) {
                    tba=new byte[t.pLen];
                    bb.mark();
                    try {
                        bb.get(tba);
                    } catch (java.nio.BufferUnderflowException ex) {
                        bb.reset();
                        //e.pByte=null;
                        //e=null;
                        System.err.println("Comm error: expected "+t.pLen+" bytes, had "
                        +bb.remaining());
                        System.err.println(ex.getMessage());
                        ex.printStackTrace();
                    }
                    //e.pByte=ByteBuffer.wrap(tba);
                } else {
                    if (t.pLen==-1) {
                        bb.mark();
                        //                        StringBuffer sb=new StringBuffer();
                        ByteBuffer sb=ByteBuffer.allocate(bb.remaining()+1);
                        boolean looking=true;
                        byte cb;
                        while (looking && bb.hasRemaining()) {
                            cb=bb.get();
                            if (t.eTagAsBB.get(0)!=cb) {
                                //                                sb.append(cb);
                                sb.put(cb);
                            } else {
                                int i=0;
                                while (((i+1)<t.eTagAsBB.remaining())
                                &&(i<bb.remaining())
                                &&(bb.get(bb.position()+i)==t.eTagAsBB.get(i+1))) {
                                    i++;
                                }
                                if (i==(t.eTagAsBB.remaining()-1)) {
                                    looking=false;
                                    bb.position(bb.position()+i);
                                } else {
                                    //                                    sb.append(cb);
                                    sb.put(cb);
                                }
                            }
                        }
                        if (looking) {
                            bb.reset();
                            sb=null;
                            //e=null;
                            System.err.println("Comm error: expected end tag '"+t.eTag+"'");
                            System.err.flush();
                        } else {
                            sb.rewind();
                            // TBD: decode non-US strings
                            ts=chrset.decode(sb).toString();
                            System.err.println("Decoded '"+ts+"'");
                            //e.pString=chrset.decode(sb).toString();
                            //System.err.println("Decoded '"+e.pString+"'");
                            System.err.flush();
                            sb=null;
                        }
                    }
                }
            }
            retElem=new MessageElement();
            retElem.tag=t;
            retElem.pString=ts;
            retElem.pByte=tba;
        }
        return retElem;
    }
    
    public static MessageElement buildElement(MessageToken token) {
        MessageElement retElem=null;
        if (token.pLen != 0) {
            System.err.println("Comm error: expected a payload, got none!");
            System.err.flush();
        } else {
            retElem=new MessageElement();
            retElem.tag=token;
        }
        return retElem;
    }
    
    public static MessageElement buildElement(MessageToken token, byte[] payload) {
        MessageElement retElem=null;
        if (token.pLen == 0) {
            System.err.println("Comm error: expected no payload, but got a byte array!");
            System.err.flush();
        } else if (token.pLen < 0) {
            System.err.println("Comm error: expected String payload, but got a byte array!");
            System.err.flush();
        } else if (token.pLen != payload.length) {
            System.err.println("Comm error: expected byte array payload of length "+token.pLen+", but got a length of "+payload.length+"!");
            System.err.flush();
        } else {
            retElem=new MessageElement();
            retElem.tag=token;
            retElem.pByte=new byte[token.pLen];
            for (int i = 0; i < token.pLen; i++) {
		retElem.pByte[i] = payload[i];
	    }
        }
        return retElem;
    }
    
    public static MessageElement buildElement(MessageToken token, String payload) {
        MessageElement retElem=null;
        if (token.pLen == 0) {
            System.err.println("Comm error: expected no payload, but got a String!");
            System.err.flush();
        } else if (token.pLen > 0) {
            System.err.println("Comm error: expected byte array payload, but got a String!");
            System.err.flush();
        } else {
            retElem=new MessageElement();
            retElem.pString=payload;
        }
        return retElem;
    }
    
    public int length() {
        int l=((tag!=null)?tag.length:0)+((pString!=null)?pString.length():0)
        +((pByte!=null)?pByte.length:0);
        return l;
    }
    
    public int hashCode() {
        return ((tag==null)?0:tag.hashCode())
        ^ ((pString==null)?0:pString.hashCode())
        ^ ((pByte==null)?0:pByte.hashCode());
    }
    
    public boolean equals(Object obj) {
        boolean isEqual=(this==obj);
        if ((!isEqual) && (obj!=null)
        && (this.getClass().isAssignableFrom(obj.getClass()))) {
            MessageElement tObj=(MessageElement)obj;
            isEqual=((this.tag==tObj.tag)
            || ((this.tag!=null) && (this.tag.equals(tObj.tag))));
            isEqual=((isEqual) && ((this.pString==tObj.pString)
            || ((this.pString!=null) && (this.pString.equals(tObj.pString)))));
            isEqual=((isEqual) && ((this.pByte==tObj.pByte)
            || ((this.pByte!=null) && (this.pByte.equals(tObj.pByte)))));
        }
        return isEqual;
    }
    
    protected synchronized int compare(byte[] a, byte[] b) {
        // TBD implement compare byte arrays
        return 0;
    }
    
    public int compareTo(MessageElement o)
    throws NullPointerException {
        int cmp=((tag==null)?0:tag.compareTo(o.tag));
        cmp=((cmp!=0)?cmp:((pString==null)?0:pString.compareTo(o.pString)));
        cmp=((cmp!=0)?cmp:((pByte==null)?0:compare(pByte,o.pByte)));
        return cmp;
    }
    
    public int compareTo(Object o)
    throws NullPointerException, ClassCastException {
        return this.compareTo((MessageElement)o);
    }
    
}