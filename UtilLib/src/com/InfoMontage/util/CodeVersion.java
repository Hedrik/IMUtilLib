/*
 * CodeVersion.java
 * 
 * Created on July 28, 2003, 9:56 PM
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

import com.InfoMontage.version.AbstractCodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

/**
 * The CodeVersion utility class provides static methods to obtain object and
 * interface version information using reflection. It is not intended for
 * instantiation.
 * <P>
 * For flexibility, there are methods that allow you to supply the name of the
 * variable containing the version identifier. These methods return an Object,
 * which must then be cast appropriately by the caller of the method.
 * <P>
 * The methods which do not include a parameter for the variable name will use
 * the value of the {@linkplain #DEFAULT_IMPL_VERSION_VARIABLE_NAME}or
 * {@linkplain #DEFAULT_INTERFACE_VERSION_CONSTANT_NAME}String constants
 * defined in this utility class as the variable name containing the version
 * identifier. These methods require that the version identifier implement the
 * {@link com.InfoMontage.version.CodeVersion CodeVersion}interface.
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 * @see com.InfoMontage.version.CodeVersion
 *      com.InfoMontage.version.GenericCodeVersion
 */
public final class CodeVersion {

    /**
     * The constant String value used as the name of the variable containing
     * the {@link com.InfoMontage.version.CodeVersion CodeVersion}of a class
     * definition.
     */
    public static String DEFAULT_IMPL_VERSION_VARIABLE_NAME = "implCodeVersion";
    /**
     * The constant String value used as the name of the variable containing
     * the {@link com.InfoMontage.version.CodeVersion CodeVersion}of an
     * interface definition.
     */
    public static String DEFAULT_INTERFACE_VERSION_CONSTANT_NAME = "INTERFACE_CODE_VERSION";

    /**
     * This constant contains the
     * {@link com.InfoMontage.version.CodeVersion CodeVersion}of this utility
     * class.
     */
    public static com.InfoMontage.version.CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
    .codeVersionFromCVSRevisionString("$Revision$");

    /**
     * Prevents instantiation of a utility CodeVersion object. This utility
     * class is not intended to be instantiated. It provides only static
     * methods.
     */
    private CodeVersion() {}

    /**
     * Obtain the CodeVersion of the specified interface within the class
     * hierarchy ending the class of the specified object.
     * 
     * @param obj The object used to begin the search for the version of the
     *            specified interface.
     * @param iface The name of the interface to obtain the version of, if it
     *            is implemented by the class of the specified object or any
     *            of it's superclasses.
     * @return The CodeVersion object found for the specified interface, or
     *         null if any of the following are true:
     *         <P>
     *         <LI>no such interface is implemented by the class hierarchy of
     *         the specified object, or</LI>
     *         </P>
     *         <P>
     *         <LI>if no constant with the name specified in
     *         {@link #DEFAULT_INTERFACE_VERSION_CONSTANT_NAME}is defined in
     *         that interface, or</LI>
     *         </P>
     *         <P>
     *         <LI>if the class of the object contained by that constant is
     *         not {@link com.InfoMontage.version.CodeVersion}or String, or
     *         </LI>
     *         </P>
     *         <P>
     *         <LI>the constant is a String and that String is not parsed as
     *         a valid version.</LI>
     *         </P>
     * @see com.InfoMontage.version.CodeVersion
     *      com.InfoMontage.version.CodeVersion
     */
    public static final com.InfoMontage.version.CodeVersion getInterfaceVersion(
	Object obj, String iface) throws IllegalAccessException
    {
	return getInterfaceVersion(obj, iface,
	    DEFAULT_INTERFACE_VERSION_CONSTANT_NAME);
    }

    public static final com.InfoMontage.version.CodeVersion getInterfaceVersion(
	Object obj, String iface, String versFldName)
	throws IllegalAccessException
    {
	com.InfoMontage.version.CodeVersion rcv = null;
	if (obj != null) {
	    Class c;
	    if (Class.class.isInstance(obj))
		c = (Class) obj;
	    else
		c = obj.getClass();
	    if (c != Object.class && c != Class.class) {
		String f = "." + iface;
		if (c.getName().endsWith(f))
		    if (c.isInterface())
			rcv = getCodeVersionFromClass(c, versFldName);
		    else
			throw new ClassCastException("Class '" + iface
			    + "' is not an interface!");
		if (rcv == null) {
		    Class[] ca = c.getInterfaces();
		    if (ca != null) {
			boolean found = false;
			for (int i = 0; i < ca.length && !found; i++ ) {
			    if (ca[i].getName().endsWith(f))
				rcv = getCodeVersionFromClass(ca[i],
				    versFldName);
			    if (rcv != null)
				found = true;
			    else {
				rcv = getInterfaceVersion(ca[i], iface,
				    versFldName);
				if (rcv != GenericCodeVersion.NULL_CODE_VERSION)
				    found = true;
			    }
			}
		    }
		}
		// now check the interfaces of the rest of the class hierarchy
		if (rcv == null) {
		    rcv = getInterfaceVersion(c.getSuperclass(), iface,
			versFldName);
		}
	    }
	}
	if (rcv == null)
	    rcv = GenericCodeVersion.NULL_CODE_VERSION;
	return rcv;
    }

    public static final com.InfoMontage.version.CodeVersion getImplVersion(
	Object obj, String impl) throws IllegalAccessException
    {
	return getImplVersion(obj, impl, DEFAULT_IMPL_VERSION_VARIABLE_NAME);
    }

    public static final com.InfoMontage.version.CodeVersion getImplVersion(
	Object obj, String impl, String versFldName)
	throws IllegalAccessException
    {
	com.InfoMontage.version.CodeVersion rcv = null;
	if (obj != null) {
	    Class c;
	    String f = "." + impl;
	    if (Class.class.isInstance(obj))
		c = (Class) obj;
	    else
		c = obj.getClass();
	    if (c != Object.class && c != Class.class) {
		if (c.getName().endsWith(f))
		    if (!c.isInterface())
			rcv = getCodeVersionFromClass(c, versFldName);
		    else
			throw new ClassCastException("Class '" + impl
			    + "' is an interface!");
		if (rcv == null) {
		    rcv = getImplVersion(c.getSuperclass(), impl,
			versFldName);
		}
	    }
	    // Having checked the entire class hierarchy, we now verify that
	    // it's not an interface...
	    if (rcv == null || rcv == GenericCodeVersion.NULL_CODE_VERSION)
	    {
		Class[] ca = c.getInterfaces();
		if (ca != null) {
		    boolean found = false;
		    for (int i = 0; i < ca.length && !found; i++ ) {
			rcv = getImplVersion(ca[i], impl, versFldName);
			if (rcv != GenericCodeVersion.NULL_CODE_VERSION)
			    // this should never happen - should have
			    // thrown an exception earlier
			    throw new ClassCastException("Class '" + impl
				+ "' is an interface!");
		    }
		}
	    }
	}
	if (rcv == null)
	    rcv = GenericCodeVersion.NULL_CODE_VERSION;
	return rcv;
    }

    public static final com.InfoMontage.version.CodeVersion getVersion(
	Object obj, String cls) throws IllegalAccessException
    {
	return getVersion(obj, cls, DEFAULT_IMPL_VERSION_VARIABLE_NAME,
	    DEFAULT_INTERFACE_VERSION_CONSTANT_NAME);
    }

    public static final com.InfoMontage.version.CodeVersion getVersion(
	Object obj, String cls, String implVersFldName,
	String ifaceVersFldName) throws IllegalAccessException
    {
	com.InfoMontage.version.CodeVersion rcv = null;
	if (obj != null) {
	    Class c;
	    String f = "." + cls;
	    if (Class.class.isInstance(obj))
		c = (Class) obj;
	    else
		c = obj.getClass();
	    if (c != Object.class && c != Class.class) {
		if (c.getName().endsWith(f))
		    if (c.isInterface())
			rcv = getCodeVersionFromClass(c, ifaceVersFldName);
		    else
			rcv = getCodeVersionFromClass(c, implVersFldName);
		// now check the interface hierarchy...
		if (rcv == null) {
		    Class[] ca = c.getInterfaces();
		    if (ca != null) {
			boolean found = false;
			for (int i = 0; i < ca.length && !found; i++ ) {
			    if (ca[i].getName().endsWith(f))
				rcv = getCodeVersionFromClass(ca[i],
				    ifaceVersFldName);
			    if (rcv != null)
				found = true;
			    else {
				rcv = getInterfaceVersion(ca[i], cls,
				    ifaceVersFldName);
				if (rcv != GenericCodeVersion.NULL_CODE_VERSION)
				    found = true;
			    }
			}
		    }
		}
		// now check the rest of the class hierarchy...
		if (rcv == null) {
		    rcv = getVersion(c.getSuperclass(), cls,
			implVersFldName, ifaceVersFldName);
		}
	    }
	}
	if (rcv == null)
	    rcv = GenericCodeVersion.NULL_CODE_VERSION;
	return rcv;
    }

    private static final com.InfoMontage.version.CodeVersion getCodeVersionFromClass(
	Class c, String n) throws IllegalAccessException
    {
	com.InfoMontage.version.CodeVersion cv = null;
	java.lang.reflect.Field fld = null;
	System.err.println("Checking class '" + c + "' for '" + n + "'");
	try {
	    fld = c.getDeclaredField(n);
	    if (com.InfoMontage.version.CodeVersion.class
		.isAssignableFrom(fld.getType())
		&& java.lang.reflect.Modifier.isStatic(fld.getModifiers()))
		// since Field.get(Object) ignores Object for
		// static fields, we pass in null
		cv = (com.InfoMontage.version.CodeVersion) fld.get(null);
	} catch (NoSuchFieldException e) {} catch (IllegalAccessException e)
	{
	    throw (IllegalAccessException) new IllegalAccessException(
		"Cannot access static field '" + fld.getName()
		    + "' of class '" + c.getName() + "'!").initCause(e);
	}
	return cv;
    }

    public static final boolean hasVersion(Class c) {
	// placeholder
	return false;
    }

    public static final boolean hasVersion(String cls)
	throws ClassNotFoundException
    {
	// placeholder
	return false;
    }

    public static final boolean hasVersion(Object obj) {
	// placeholder
	return false;
    }

}