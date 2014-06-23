package com.InfoMontage.stream;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

import java.nio.ByteBuffer;


/**
 * A MessageTemplate is sequence of MessageTokens that define the format of a
 * Message. The MessageTemplate is used to create a Message from a ByteBuffer.
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class MessageTemplate extends java.util.LinkedList implements Comparable {

    // private volatile MessageProcessor msgProc = null;

    protected static Class MESSAGE_TYPE = Message.class;

    /** Creates a new instance of MessageTemplate */
    protected MessageTemplate() {
	super();
    }

    /** Creates a new instance of MessageTemplate */
    // public MessageTemplate(MessageProcessor mp) {
    // super();
    // msgProc=mp;
    // }
    /** Creates a new instance of MessageTemplate */
    // public MessageTemplate(MessageProcessor mp, MessageToken e) {
    // super();
    // msgProc=mp;
    // this.add(e);
    // }
    /** Creates a new instance of MessageTemplate */
    protected MessageTemplate(Collection ime) {
	super();
	this.addAll(ime);
    }

    /** Creates a new instance of MessageTemplate */
    // public MessageTemplate(MessageProcessor mp, Collection ime) {
    // super();
    // msgProc=mp;
    // this.addAll(ime);
    // }
    /** Creates a new instance of MessageTemplate */
    public MessageTemplate(MessageElement[] mea) {
	super();
	this.addAll(mea);
    }

    /** Creates a new instance of MessageTemplate */
    public MessageTemplate(MessageToken[] mta) {
	super();
	this.addAll(mta);
    }

    public synchronized boolean add(MessageToken e)
	    throws IllegalArgumentException {
	if (e == null)
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to add an" + " null element!")
		    .initCause(new NullPointerException("Attempt to add a null"
			    + " element to a MessageTemplate object!"));
	return super.add(e);
    }

    public synchronized boolean add(Object o) throws IllegalArgumentException {
	if (o instanceof MessageToken)
	    return this.add((MessageToken) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to add an" + " element of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to add an element"
				    + " that is not of type MessageToken to a MessageTemplate object!"));
    }

    public synchronized void add(int index, MessageToken e)
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
	if (o instanceof MessageToken)
	    this.add(index, (MessageToken) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to add an" + " element of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to add an element"
				    + " that is not of type MessageToken to a MessageTemplate object!"));
    }

    public synchronized boolean addAll(MessageElement[] ea)
    throws IllegalArgumentException, NullPointerException {
	boolean retVal=true;
    	for (int i = 0; (i < ea.length) && retVal; i++)
    	    retVal = super.add(ea[i].tag);
    	return retVal;
}

    public synchronized boolean addAll(MessageToken[] ta)
    throws IllegalArgumentException, NullPointerException {
return this.addAll(Arrays.asList(ta));
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
		//else if (!((e instanceof MessageElement) || (e instanceof MessageToken)))
		else if (!(e instanceof MessageToken))
		    throw (IllegalArgumentException) new IllegalArgumentException(
			    "Attempt to add a Collection"
				    + " containing an element of inappropriate type!")
			    .initCause(new ClassCastException(
				    "Attempt to add an element"
					    + " that is not of type MessageToken to a MessageTemplate object!"));
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
		//else if (!((e instanceof MessageElement) || (e instanceof MessageToken)))
		else if (!(e instanceof MessageToken))
		    throw (IllegalArgumentException) new IllegalArgumentException(
			    "Attempt to add a Collection"
				    + " containing an element of inappropriate type!")
			    .initCause(new ClassCastException(
				    "Attempt to add an element"
					    + " that is not of type MessageToken to a MessageTemplate object!"));
	    }
	}
	return super.addAll(index, c);
    }

    /** A synonym for clone() */
    public synchronized MessageTemplate shallowCopy() {
	return (MessageTemplate) this.clone();
    }

    public synchronized Object clone() {
	MessageTemplate retValue;
	// TBD: verify clone method is complete
	retValue = (MessageTemplate) super.clone();
	return retValue;
    }

    public synchronized MessageTemplate deepCopy() {
	MessageTemplate retValue;
	// TBD: implement deep copy method
	retValue = (MessageTemplate) super.clone();
	return retValue;
    }

    public synchronized int compareTo(MessageTemplate m) {
	// TBD implement compareTo MessageTemplate
	return 0;
    }

    public synchronized int compareTo(Object o) {
	if (o instanceof MessageTemplate)
	    return this.compareTo((MessageTemplate) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to compare to an"
			    + " object of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to compare an object"
				    + " that is not of type MessageTemplate to a MessageTemplate object!"));
    }

    public synchronized boolean equals(MessageTemplate m) {
	boolean retValue;
	// TBD: Implement equals such that it compares only msg elements
	retValue = super.equals(m);
	return retValue;
    }

    public synchronized boolean equals(Object o) {
	boolean retValue = false;
	if (o == this)
	    retValue = true;
	else if (o instanceof MessageTemplate)
	    retValue = this.equals((MessageTemplate) o);
	return retValue;
    }

    public synchronized int hashCode() {
	int retValue;
	// TBD: xor each element's hashcode
	retValue = super.hashCode();
	return retValue;
    }

    public synchronized int indexOf(MessageToken e) {
	int retValue = -1;
	if (e != null)
	    retValue = super.indexOf(e);
	return retValue;
    }

    public synchronized int indexOf(Object o) {
	int retValue = -1;
	if ((o != null) && (o instanceof MessageToken))
	    retValue = super.indexOf(o);
	return retValue;
    }

    public synchronized int lastIndexOf(MessageToken e) {
	int retValue = -1;
	if (e != null)
	    retValue = super.lastIndexOf(e);
	return retValue;
    }

    public synchronized int lastIndexOf(Object o) {
	int retValue = -1;
	if ((o != null) && (o instanceof MessageToken))
	    retValue = super.lastIndexOf(o);
	return retValue;
    }

    public synchronized MessageToken set(int index, MessageToken e)
	    throws IllegalArgumentException, IndexOutOfBoundsException {
	if (e == null)
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to set an element of a"
			    + " MessageTemplate object to a null value!")
		    .initCause(new NullPointerException(
			    "Attempt to set an element of a"
				    + " MessageTemplate object to null!"));
	return (MessageToken) super.set(index, e);
    }

    public synchronized Object set(int index, Object o)
	    throws IllegalArgumentException, IndexOutOfBoundsException {
	if (o instanceof MessageToken)
	    return this.set(index, (MessageToken) o);
	else
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to set an element of a"
			    + " MessageTemplate object to a value of inappropriate type!")
		    .initCause(new ClassCastException(
			    "Attempt to set an element of a"
				    + "MessageTemplate object to a value that is not of type MessageToken!"));
    }

    public synchronized Object[] toArray(Object[] a)
	    throws ArrayStoreException, NullPointerException {
	return (MessageToken[]) super.toArray(a);
    }

/*    public synchronized MessageToken[] toArray(MessageToken[] a)
    throws ArrayStoreException, NullPointerException {
return (MessageToken[]) super.toArray(a);
}*/

    public synchronized MessageToken[] toArray(MessageToken[] a)
    throws ArrayStoreException, NullPointerException {
return (MessageToken[]) super.toArray(a);
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
	return (MessageTemplate) super.subList(fromIndex, toIndex);
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

    public synchronized boolean remove(MessageToken e) {
	return super.remove(e);
    }

    public synchronized boolean remove(Object o) {
	return super.remove(o);
    }

    public synchronized Object remove(int index) {
	return (MessageToken) super.remove(index);
    }

    public synchronized boolean isEmpty() {
	return super.isEmpty();
    }

    public synchronized Object get(int index) {
	return (MessageToken) super.get(index);
    }

/*    public synchronized ByteBuffer get() {
	ByteBuffer b = ByteBuffer.allocate(byteLength());
	Iterator i = super.iterator();
	while (i.hasNext())
	    ((MessageToken) i.next()).append(b);
	return b;
    }
*/
/*    public synchronized int byteLength() {
	int l = 0;
	Iterator i = super.iterator();
	while (i.hasNext())
	    l += ((MessageToken) i.next()).length();
	return l;
    }*/

    public synchronized boolean containsAll(Collection c) {
	return super.containsAll(c);
    }

    public synchronized boolean contains(MessageToken e) {
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
		retVal = super.add(ea[i].tag);
	}
	return retVal;
    }

    public synchronized boolean add(MessageToken[] ta) {
	boolean retVal = false;
	if (ta != null) {
	    synchronized (ta) {
		Object e;
		for (int i = 0; i < ta.length; i++) {
		    e = ta[i];
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
	    for (int i = 0; (i < ta.length) && retVal; i++)
		retVal = super.add(ta[i]);
	}
	return retVal;
    }

    public MessageToken[] getAll() {
	MessageToken[] a = null;
	return (MessageToken[]) super.toArray(a);
    }

    public static Message valueOf(ByteBuffer msgBuf)
	    throws IllegalStateException, InstantiationException,
	    IllegalAccessException {
	Message m = null;
	try {
	    m = (Message) staticGetMessageType().newInstance();
	} catch (NullPointerException e) {
	    throw (IllegalStateException) new IllegalStateException(
		    "Attempt to"
			    + " extract a Message from a buffer prior to instantiating an object"
			    + " of the relevant MessageTemplate class").initCause(e);
	}
	m.setToValueOf(msgBuf);
	return m;
    }

    /**
     * 
     */
    public Class getMessageType() {
	return staticGetMessageType();
}
    
    public static Class staticGetMessageType() {
	return MESSAGE_TYPE;
}

    /**
     * @param objects
     * @return
     */
    public Message buildMessage(Object[] objects) {
	Message builtMessage = null;
	java.util.List elements = new ArrayList();
	int i=0;
	for (Iterator iterator = this.iterator(); iterator.hasNext();) {
	    MessageToken token = (MessageToken) iterator.next();
	    if (token.pLen == 0) {
		elements.add(MessageElement.buildElement(token));
	    } else if (token.pLen < 0) {
		elements.add(MessageElement.buildElement(token, (String) objects[i++]));
	    } else {
		elements.add(MessageElement.buildElement(token, (byte[]) objects[i++]));
	    }
	}
	builtMessage=new Message(elements);
	return builtMessage;
    }

}