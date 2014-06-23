/*
 * MessageTemplate.java
 *
 * Created on July 8, 2003, 1:04 PM
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;
import java.nio.ByteBuffer;

/**
 * A Message is a sequence of MessageElements corresponding to the format of a
 * MessageTemplate. The Message contains a specific instance of data.
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class Message extends java.util.LinkedList implements Comparable {

    // private volatile MessageProcessor msgProc = null;

    private static Class thisClass;

    /** Creates a new instance of MessageTemplate */
    protected Message() {
	super();
	if (null == thisClass)
	    thisClass = this.getClass();
    }

    /** Creates a new instance of MessageTemplate */
    // public MessageTemplate(MessageProcessor mp) {
    // super();
    // msgProc=mp;
    // }
    /** Creates a new instance of MessageTemplate */
    // public MessageTemplate(MessageProcessor mp, MessageElement e) {
    // super();
    // msgProc=mp;
    // this.add(e);
    // }
    /** Creates a new instance of MessageTemplate */
    public Message(Collection ime) {
	super();
	if (null == thisClass)
	    thisClass = this.getClass();
	this.addAll(ime);
    }

    /** Creates a new instance of MessageTemplate */
    // public MessageTemplate(MessageProcessor mp, Collection ime) {
    // super();
    // msgProc=mp;
    // this.addAll(ime);
    // }
    /** Creates a new instance of MessageTemplate */
    public Message(MessageElement[] mea) {
	super();
	if (null == thisClass)
	    thisClass = this.getClass();
	this.addAll(mea);
    }

    public synchronized boolean add(MessageElement e)
	    throws IllegalArgumentException {
	if (e == null)
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to add an" + " null element!")
		    .initCause(new NullPointerException("Attempt to add a null"
			    + " element to a MessageTemplate object!"));
	return super.add(e);
    }

    public synchronized boolean add(Object o) throws IllegalArgumentException {
	if (o instanceof MessageElement)
	    return this.add((MessageElement) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to add an" + " element of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to add an element"
				    + " that is not of type MessageElement to a MessageTemplate object!"));
    }

    public synchronized void add(int index, MessageElement e)
	    throws IllegalArgumentException {
	if (e == null)
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to add a" + " null element!")
		    .initCause(new NullPointerException("Attempt to add a null"
			    + " element to a MessageTemplate object!"));
	super.add(index, e);
    }

    public synchronized void add(int index, Object o)
	    throws IllegalArgumentException, IndexOutOfBoundsException {
	if (o instanceof MessageElement)
	    this.add(index, (MessageElement) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to add an" + " element of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to add an element"
				    + " that is not of type MessageElement to a MessageTemplate object!"));
    }

    public synchronized boolean addAll(MessageElement[] a)
	    throws IllegalArgumentException, NullPointerException {
	return this.addAll(Arrays.asList(a));
    }

    public synchronized boolean addAll(Collection c)
	    throws IllegalArgumentException, NullPointerException {
	if (c != null) {
	    Object e;
	    Iterator i = c.iterator();
	    while (i.hasNext()) {
		e = i.next();
		if (e == null)
		    throw (IllegalArgumentException) new IllegalArgumentException(
			    "Attempt to add a Collection"
				    + " containing a null element!")
			    .initCause(new NullPointerException(
				    "Attempt to add a null"
					    + " element to a MessageTemplate object!"));
		else if (!(e instanceof MessageElement))
		    throw (IllegalArgumentException) new IllegalArgumentException(
			    "Attempt to add a Collection"
				    + " containing an element of inappropriate type!")
			    .initCause(new ClassCastException(
				    "Attempt to add an element"
					    + " that is not of type MessageElement to a MessageTemplate object!"));
	    }
	}
	return super.addAll(c);
    }

    public synchronized boolean addAll(int index, Collection c)
	    throws IllegalArgumentException, IndexOutOfBoundsException,
	    NullPointerException {
	if (c != null) {
	    Object e;
	    Iterator i = c.iterator();
	    while (i.hasNext()) {
		e = i.next();
		if (e == null)
		    throw (IllegalArgumentException) new IllegalArgumentException(
			    "Attempt to add a Collection"
				    + " containing a null element!")
			    .initCause(new NullPointerException(
				    "Attempt to add a null"
					    + " element to a MessageTemplate object!"));
		else if (!(e instanceof MessageElement))
		    throw (IllegalArgumentException) new IllegalArgumentException(
			    "Attempt to add a Collection"
				    + " containing an element of inappropriate type!")
			    .initCause(new ClassCastException(
				    "Attempt to add an element"
					    + " that is not of type MessageElement to a MessageTemplate object!"));
	    }
	}
	return super.addAll(index, c);
    }

    /** A synonym for clone() */
    public synchronized Message shallowCopy() {
	return (Message) this.clone();
    }

    public synchronized Object clone() {
	Message retValue;
	// TBD: verify clone method is complete
	retValue = (Message) super.clone();
	return retValue;
    }

    public synchronized Message deepCopy() {
	Message retValue;
	// TBD: implement deep copy method
	retValue = (Message) super.clone();
	return retValue;
    }

    public synchronized int compareTo(Message m) {
	// TBD implement compareTo MessageTemplate
	return this.get().compareTo(m.get());
    }

    public synchronized int compareTo(Object o) {
	if (o instanceof Message)
	    return this.compareTo((Message) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to compare to an"
			    + " object of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to compare an object"
				    + " that is not of type MessageTemplate to a MessageTemplate object!"));
    }

    public synchronized boolean equals(Message m) {
	boolean retValue;
	// TBD: Implement equals such that it compares only msg elements
	retValue = super.equals(m);
	return retValue;
    }

    public synchronized boolean equals(Object o) {
	boolean retValue = false;
	if (o == this)
	    retValue = true;
	else if (o instanceof Message)
	    retValue = this.equals((Message) o);
	return retValue;
    }

    public synchronized int hashCode() {
	int retValue;
	// TBD: xor each element's hashcode
	retValue = super.hashCode();
	return retValue;
    }

    public synchronized int indexOf(MessageElement e) {
	int retValue = -1;
	if (e != null)
	    retValue = super.indexOf(e);
	return retValue;
    }

    public synchronized int indexOf(Object o) {
	int retValue = -1;
	if ((o != null) && (o instanceof MessageElement))
	    retValue = super.indexOf(o);
	return retValue;
    }

    public synchronized int lastIndexOf(MessageElement e) {
	int retValue = -1;
	if (e != null)
	    retValue = super.lastIndexOf(e);
	return retValue;
    }

    public synchronized int lastIndexOf(Object o) {
	int retValue = -1;
	if ((o != null) && (o instanceof MessageElement))
	    retValue = super.lastIndexOf(o);
	return retValue;
    }

    public synchronized MessageElement set(int index, MessageElement e)
	    throws IllegalArgumentException, IndexOutOfBoundsException {
	if (e == null)
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to set an element of a"
			    + " MessageTemplate object to a null value!")
		    .initCause(new NullPointerException(
			    "Attempt to set an element of a"
				    + " MessageTemplate object to null!"));
	return (MessageElement) super.set(index, e);
    }

    public synchronized Object set(int index, Object o)
	    throws IllegalArgumentException, IndexOutOfBoundsException {
	if (o instanceof MessageElement)
	    return this.set(index, (MessageElement) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to set an element of a"
			    + " MessageTemplate object to a value of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to set an element of a"
				    + "MessageTemplate object to a value that is not of type MessageElement!"));
    }

    public synchronized Object[] toArray(Object[] a)
	    throws ArrayStoreException, NullPointerException {
	return (MessageElement[]) super.toArray(a);
    }

    public synchronized MessageElement[] toArray(MessageElement[] a)
	    throws ArrayStoreException, NullPointerException {
	return (MessageElement[]) super.toArray(a);
    }

    public synchronized Object[] toArray() {
	return super.toArray();
    }

    public synchronized String toString() {
	String retValue;
	// TBD: implement toString
	retValue = super.toString();
	return retValue;
    }

    public synchronized java.util.List subList(int fromIndex, int toIndex) {
	return (Message) super.subList(fromIndex, toIndex);
    }

    public synchronized int size() {
	return super.size();
    }

    public synchronized boolean retainAll(Collection c) {
	return super.retainAll(c);
    }

    public synchronized boolean removeAll(Collection c) {
	return super.removeAll(c);
    }

    public synchronized boolean remove(MessageElement e) {
	return super.remove(e);
    }

    public synchronized boolean remove(Object o) {
	return super.remove(o);
    }

    public synchronized Object remove(int index) {
	return (MessageElement) super.remove(index);
    }

    public synchronized boolean isEmpty() {
	return super.isEmpty();
    }

    public synchronized Object get(int index) {
	return (MessageElement) super.get(index);
    }

    public synchronized ByteBuffer get() {
	ByteBuffer b = ByteBuffer.allocate(byteLength());
	Iterator i = super.iterator();
	while (i.hasNext())
	    ((MessageElement) i.next()).append(b);
	return b;
    }

    public synchronized int byteLength() {
	int l = 0;
	Iterator i = super.iterator();
	while (i.hasNext())
	    l += ((MessageElement) i.next()).length();
	return l;
    }

    public synchronized boolean containsAll(Collection c) {
	return super.containsAll(c);
    }

    public synchronized boolean contains(MessageElement e) {
	return super.contains(e);
    }

    public synchronized void clear() {
	super.clear();
    }

    public synchronized boolean add(MessageElement[] ea) {
	boolean retVal = false;
	if (ea != null) {
	    synchronized (ea) {
		Object e;
		for (int i = 0; i < ea.length; i++) {
		    e = ea[i];
		    if (e == null)
			throw (IllegalArgumentException) new IllegalArgumentException(
				"Attempt to add an array"
					+ " containing a null element!")
				.initCause(new NullPointerException(
					"Attempt to add a null"
						+ " element to a MessageTemplate object!"));
		}
	    }
	    retVal = true;
	    for (int i = 0; (i < ea.length) && retVal; i++)
		retVal = super.add(ea[i]);
	}
	return retVal;
    }

    public MessageElement[] getAll() {
	MessageElement[] a = null;
	return (MessageElement[]) super.toArray(a);
    }

    Message setToValueOf(ByteBuffer msgBuf) {
	this.clear();
	MessageElement me;
	do {
	    me = MessageElement.nextElement(msgBuf);
	    if (null != me)
		this.add(me);
	} while (null != me);
	return this;
    }

}