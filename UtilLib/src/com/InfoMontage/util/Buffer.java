/*
 * Buffer.java
 *
 * Created on May 22, 2003, 8:50 AM
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

package com.InfoMontage.util;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.io.Serializable;

import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;
import com.InfoMontage.stream.MessageProcessor;
import com.InfoMontage.stream.MessageElement;
import com.InfoMontage.stream.MessageToken;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public final class Buffer {

    public static CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
    .codeVersionFromCVSRevisionString("$Revision$");

    public static final String DEFAULT_CHAR_SET_NAME="US-ASCII";
    public static final java.nio.charset.Charset DEFAULT_CHAR_SET
    =java.nio.charset.Charset.forName(DEFAULT_CHAR_SET_NAME);

    /** Prevents instantiation of a Buffer by clients
     */
    private Buffer() {}

    public static final ByteBuffer create(MessageToken t) {
	if ((t.length==-1) && (t.pLen==-1))
	    return ByteBuffer.allocate(MessageProcessor.DEFAULT_MINIMUM_MESSAGE_BUFFER_SIZE);
	else
	    return ByteBuffer.allocate(t.length);
    }

    public static final ByteBuffer create(MessageElement e) {
	return ByteBuffer.allocate(e.length());
    }

    public static final ByteBuffer create(int l) {
	return ByteBuffer.allocate(l);
    }

    public static final String toString(final ByteBuffer b) {
	return toString(b,Byte.TYPE);
    }

    public static final String toString(final ByteBuffer b
    , final java.nio.charset.Charset c) {
	if (c==null)
	    throw new NullPointerException("null passed for Charset!");
	StringBuffer s=new StringBuffer();
	if (b==null)
	    s.append("[null]");
	else {
	    s.append("'");
	    synchronized (b) {
		if (!((b.position())==0 && (b.limit()==b.capacity())))
		    s.append(c.decode(b.asReadOnlyBuffer()).toString());
		s.append("'=").append(b.position()).append(",")
		.append(b.limit()).append(",").append(b.capacity());
	    }
	}
	return s.toString();
    }

    public static final Class[] POSSIBLE_BYTEBUFFER_CONVERSION_CLASSES =  {
	Byte.class,
	Byte.TYPE,
	Character.class,  // assumes default Charset - use Charset if other
	Character.TYPE,
	Double.class,
	Double.TYPE,
	Float.class,
	Float.TYPE,
	Integer.class,
	Integer.TYPE,
	Long.class,
	Long.TYPE,
	Short.class,
	Short.TYPE
    };

    private static class ClassComparator implements Comparator, Serializable {

	public int compare(Object o1, Object o2) {
	    // TBD: Sanity check both are of class Class
	    return ((Class)o1).getName().compareTo(((Class)o2).getName());
	}
     }

    private static ClassComparator classComp=new ClassComparator();

    static { java.util.Arrays.sort(POSSIBLE_BYTEBUFFER_CONVERSION_CLASSES
    , classComp); }

    public static final String toString(final ByteBuffer b
    , final Class c) {
	if (c==null)
	    throw new NullPointerException("null passed for conversion class!");
	if (java.util.Arrays.binarySearch(POSSIBLE_BYTEBUFFER_CONVERSION_CLASSES
	,c,classComp)<0)
	    throw new IllegalArgumentException("Invalid conversion class specified!");
	StringBuffer s=new StringBuffer();
	if (b==null)
	    s.append("[null]");
	else {
	    if ((c==Character.class)||(c==Character.TYPE))
		s.append(toString(b,java.nio.charset.Charset.forName(new java
		.io.InputStreamReader(System.in).getEncoding())));
	    else {
		s.append("'");
		Object o;
		java.nio.Buffer t;
		synchronized (b) {
		    if ((c==Byte.class)||(c==Byte.TYPE)) {
		    t=b.asReadOnlyBuffer().mark();
		    o=Array.newInstance(Byte.TYPE
		    ,((ByteBuffer)t).remaining());
			((ByteBuffer)t).get((byte[])o);
		    } // since we already check for Character we can skip it
		    else if ((c==Double.class)||(c==Double.TYPE)) {
		    t=b.asReadOnlyBuffer().asDoubleBuffer().mark();
		    o=Array.newInstance(Double.TYPE
		    ,((java.nio.DoubleBuffer)t).remaining());
			((java.nio.DoubleBuffer)t).get((double[])o);
		    }
		    else if ((c==Float.class)||(c==Float.TYPE)) {
		    t=b.asReadOnlyBuffer().asFloatBuffer().mark();
		    o=Array.newInstance(Float.TYPE
		    ,((java.nio.FloatBuffer)t).remaining());
			((java.nio.FloatBuffer)t).get((float[])o);
		}
		else if ((c==Integer.class)||(c==Integer.TYPE)) {
		    t=b.asReadOnlyBuffer().asIntBuffer().mark();
		    o=Array.newInstance(Integer.TYPE
		    ,((java.nio.IntBuffer)t).remaining());
			((java.nio.IntBuffer)t).get((int[])o);
		}
		    else if ((c==Long.class)||(c==Long.TYPE)) {
		    t=b.asReadOnlyBuffer().asLongBuffer().mark();
		    o=Array.newInstance(Long.TYPE
		    ,((java.nio.LongBuffer)t).remaining());
			((java.nio.LongBuffer)t).get((long[])o);
		    }
		    else { // must be Short
		    t=b.asReadOnlyBuffer().asShortBuffer().mark();
		    o=Array.newInstance(Short.TYPE
		    ,((java.nio.ShortBuffer)t).remaining());
			((java.nio.ShortBuffer)t).get((short[])o);
		    }
		    t.reset();
		    if (t.hasRemaining())
			for (int i=0; i<Array.getLength(o); i++)
			    s.append("[").append(Array.get(o,i).toString())
			    .append("]");
		    s.append("'=").append(b.position()).append(",")
		    .append(b.limit()).append(",").append(b.capacity());
		}
	    }
	}
	return s.toString();
    }

}
