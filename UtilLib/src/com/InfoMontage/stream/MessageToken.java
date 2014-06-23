/*
 * MessageToken.java
 *
 * Created on July 19, 2003, 12:14 AM
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

import java.nio.ByteBuffer;

public final class MessageToken {
    
    // TBD: incorporate delimiter and escape bytes
    
    private static final java.nio.charset.Charset chrset
    =com.InfoMontage.util.Buffer.DEFAULT_CHAR_SET;
    
    public final String sTag; // tag as String
    public final String eTag; // ending tag as String
    public final int pLen; // payload length or -1 for undefined if using end tag
    public final ByteBuffer sTagAsBB; // tag as ByteBuffer
    public final ByteBuffer eTagAsBB; // ending tag as ByteBuffer
    public final int length; // total length of encoded element, or -1 if variable
    
    // Note: hashtables are synchronized
    static final java.util.Hashtable tags=new java.util.Hashtable(50);
    static int minLen=Integer.MAX_VALUE;
    static int maxLen=0;
    static MessageToken minLenToken=null;
    
    public static final MessageToken MSG_TOKEN_ACK
    =new MessageToken("_Ack");
    public static final MessageToken MSG_TOKEN_NAK
    =new MessageToken("_Nak");
    
    public MessageToken(String t) {
        sTag=t;
        eTag=null;
        pLen=0;
        sTagAsBB=toBB(sTag).asReadOnlyBuffer();
        eTagAsBB=toBB(eTag).asReadOnlyBuffer();
        length=sTagAsBB.capacity();
        initMessageToken(sTag,eTag,pLen);
    }
    
    public MessageToken(String t, int p) {
        sTag=t;
        eTag=null;
        pLen=p;
        sTagAsBB=toBB(sTag).asReadOnlyBuffer();
        eTagAsBB=toBB(eTag).asReadOnlyBuffer();
        length=(pLen<0)?0:sTagAsBB.capacity()+pLen;
        initMessageToken(sTag,eTag,(pLen<0)?-2:pLen);
    }
    
    public MessageToken(String t, String e) {
        sTag=t;
        eTag=e;
        pLen=-1;
        sTagAsBB=toBB(sTag).asReadOnlyBuffer();
        eTagAsBB=toBB(eTag).asReadOnlyBuffer();
        length=-1;
        initMessageToken(sTag,eTag,pLen);
    }
    
    private ByteBuffer toBB(String s) {
        ByteBuffer retBuf;
        if (s!=null) {
            retBuf=chrset.encode(s);
            retBuf.rewind(); // needed?
        }
        else
            retBuf=ByteBuffer.allocate(0);
        return retBuf;
    }

    
    private void initMessageToken(String t, String e, int l) {
        if (t==null)
            throw new RuntimeException("Creation of null MessageToken!");
        if (t.length()==0)
            throw new RuntimeException("Creation of MessageToken with empty starting tag string!");
        if ((e!=null) && (e.length()==0))
            throw new RuntimeException("Creation of MessageToken with empty ending tag string!");
        if ((l<-1) || ((l==-1) && (e==null)))
            throw new RuntimeException("Creation of MessageToken with negative len payload!");
        Object oldVal=null;
        oldVal=tags.put(t,this);
        //        System.err.println("Add '"+t+"'='"+this.toString()+"' giving total of "+tags.size());
        if (oldVal!=null)
            throw new RuntimeException("Creation of duplicate MessageToken!\n"
            +"oldVal="+oldVal+"\nnewVal="+this);
        if (t.length()<minLen)
            minLen=t.length();
        if (t.length()>maxLen)
            maxLen=t.length();
        if ((this.length!=-1) && ((minLenToken==null) || (this.length<minLenToken.length)))
            minLenToken=this;
    }
    
    private static String displayByteBuffer(final ByteBuffer b) {
        return com.InfoMontage.util.Buffer.toString(b,chrset);
    }
    
    public ByteBuffer encode(ByteBuffer b, String s) {
        // TBD: encode non-US strings
        if (b==null)
            throw new Error("Error: Attempt to encode to null buffer!");
        synchronized (b) {
            if (s==null || s.length()==0)
                if (pLen==0)
                    b=encode(b);
                else
                    throw new Error("Error: Attempt to encode invalid data for tag '"
                    +toString()+"'\ndata was 'null' string (expected "+pLen+" bytes)");
            else
                synchronized (sTagAsBB) {
                    synchronized (eTagAsBB) {
                        System.err.println("Adding '"+sTag+"' ("+displayByteBuffer(sTagAsBB)+") of {"+s+"} to "+displayByteBuffer(b));
                        if ((pLen==-1 && eTag!=null) || (s.getBytes().length==pLen)) {
                            b.put(sTagAsBB).put(chrset.encode(s))
                            .put(eTagAsBB);
                            sTagAsBB.rewind();
                            eTagAsBB.rewind();
                        }
                        else
                            throw new Error("Error: Attempt to encode invalid data for tag '"
                            +toString()+"'\ndata was \""+s+"\"  (len="
                            +chrset.encode(s).remaining()
                            +", expected "+pLen+")");
                    }
                }
        }
        return b;
    }
    
    public ByteBuffer encode(ByteBuffer b, final byte d) {
        if (b==null)
            throw new Error("Error: Attempt to encode to null buffer!");
        synchronized (b) {
            if (pLen==1) {
                synchronized (sTagAsBB) {
                    synchronized (eTagAsBB) {
                        System.err.println("Adding '"+sTag+"' ("+displayByteBuffer(sTagAsBB)+") of {"+String.valueOf(d)+"} to "+displayByteBuffer(b));
                        b.put(sTagAsBB).put(d);
                        sTagAsBB.rewind();
                    }
                }
            }
            else
                throw new Error("Error: Attempt to encode invalid data for tag '"
                +toString()+"'\ndata was byte '"+d+"' (expected "+pLen+" bytes)");
        }
        return b;
    }
    
    public ByteBuffer encode(ByteBuffer b, final Byte d) {
        if (b==null)
            throw new Error("Error: Attempt to encode to null buffer!");
        synchronized (b) {
            if (d==null)
                throw new Error("Error: Attempt to encode to null Byte!");
            synchronized (d) {
                b=encode(b,d.byteValue());
            }
        }
        return b;
    }
    
    public ByteBuffer encode(ByteBuffer b, final byte[] d) {
        if (b==null)
            throw new Error("Error: Attempt to encode to null buffer!");
        synchronized (b) {
            if (d==null)
                if (pLen==0)
                    b=encode(b);
                else
                    throw new Error("Error: Attempt to encode invalid data for tag '"
                    +toString()+"'\ndata was 'null' (expected "+pLen+" bytes)");
            else
                if (d==null)
                    throw new Error("Error: Attempt to encode to null byte array!");
            synchronized (d) {
                if (pLen==d.length) {
                    synchronized (sTagAsBB) {
                        b.put(sTagAsBB).put(d);
                        sTagAsBB.rewind();
                    }
                }
                else
                    throw new Error("Error: Attempt to encode invalid data for tag '"
                    +toString()+"'\ndata was byte[] '"+d+"' (len="+d.length
                    +", expected "+pLen+")");
            }
        }
        return b;
    }
    
    public ByteBuffer encode(ByteBuffer b) {
        if (b==null)
            throw new Error("Error: Attempt to encode to null buffer!");
        synchronized (b) {
            if (pLen==0) {
                synchronized (sTagAsBB) {
                    System.err.println("Adding '"+sTag+"' ("+displayByteBuffer(sTagAsBB)+") to "+displayByteBuffer(b));
                    b.put(sTagAsBB);
                    sTagAsBB.rewind();
                }
            }
            else
                throw new Error("Error: Attempt to encode invalid data for MessageToken '"
                +toString()+"'\nno data provided (expected "+pLen+" bytes)");
        }
        return b;
    }
    
    public static MessageToken nextMessageToken(ByteBuffer b) {
        /// TBD: convert to use end of token delimiter and escape values
        //        ByteBuffer bb=b.slice(); // slice doesn't work correctly?
        MessageToken rv=null;
        if (b!=null) {
            synchronized (b) {
                // System.err.println("Getting next tag from "+displayByteBuffer(b));
                int obp=b.position();
                // b.mark();
                java.nio.CharBuffer cb=chrset.decode(b.asReadOnlyBuffer());
                // b.reset();
                int i=minLen;
                String cbs=null;
                boolean found=false;
                //        int m=tags.size();
                //        System.err.println("numTags="+m+"\n'"+cb.toString()+"'");
                //            long st=System.currentTimeMillis();
                while ((i<cb.remaining())&&(i<=maxLen)&&(!found)) {
                    cbs=cb.subSequence(0,i).toString();
                    // System.err.println("Checking if '"+cbs+"' is a tag...");
                    if(tags.containsKey(cbs))
                        found=true;
                    else
                        i++;
                }
                // if ((i<cb.remaining())&&(i<=maxLen)) {
                if (found) {
                    //CharSequence nb=cb.subSequence(0,i+1);
                    //rv=(CommTag)tags.get(nb.toString());
                    rv=(MessageToken)tags.get(cbs);
                    // synchronize this next on rv?
                    System.err.println("Found MessageToken '"+rv.toString()+"'");
                    //            st-=System.currentTimeMillis();
                    //            System.err.println("Finding tag took "+(-st)+" ms!");
                }
                if (rv!=null) {
                    synchronized (rv) {
                        //System.err.println("Incrementing position by '"+rv.sTagAsBB.limit()+"'");
                        synchronized (rv.sTagAsBB) {
                            b.position(obp+rv.sTagAsBB.limit());
                        }
                    }
                }
            }
        }
        return rv;
    }
    
    static public int getMinLength() {
        return (minLenToken==null)?0:minLenToken.length;
    }
    
    public int hashCode() {
        return (sTag.hashCode() ^ ((eTag==null)?0:eTag.hashCode()));
    }
    
    public boolean equals(Object obj) {
        return (obj==this);
    }
    
    public int compareTo(MessageToken o)
    throws NullPointerException {
        return sTag.compareTo(o.sTag);
    }
    
    public int compareTo(Object o)
    throws NullPointerException, ClassCastException {
        return this.compareTo((MessageToken)o);
    }
    
    public String toString() {
        return sTag+((pLen>0)?"("+String.valueOf(pLen)+")"
        :((pLen<0)?"/"+eTag:""));
    }
}