/*
 * SimpleConnectionTest.java
 * 
 * Created on Apr 10, 2004
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

package com.InfoMontage.net;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class SimpleConnectionConstructorsTest
    extends TestCase
{

    public static void main(String[] args) {
        //junit.swingui.TestRunner
        //.run(SimpleConnectionConstructorsTest.class);
        junit.textui.TestRunner
        .run(SimpleConnectionConstructorsTest.class);
    }

    /**
     * Constructor for SimpleConnectionTest.
     * 
     * @param arg0
     */
    public SimpleConnectionConstructorsTest(String arg0) {
        super(arg0);
    }

    /*
     * Class to test for void SimpleConnection()
     */
    public final void testSimpleConnection() {
        Connection c;
        try {
            c = new SimpleConnection();
        } catch (IOException e) {
            fail("IOException when creating new SimpleConnection()\n" + e);
        }
    }

    /*
     * Class to test for void SimpleConnection(int)
     */
    public final void testSimpleConnectionint() {
        Connection c;
        try {
            c = new SimpleConnection(0);
        } catch (IOException e) {
            fail("IOException when creating new SimpleConnection(int)\n"
                + e);
        }
    }

    /*
     * Class to test for void SimpleConnection(InetSocketAddress)
     */
    public final void testSimpleConnectionInetSocketAddress() {
        Connection c;
        try {
            c = new SimpleConnection(new java.net.InetSocketAddress(0));
        } catch (IOException e) {
            fail("IOException when creating new SimpleConnection(InetSocketAddress)\n"
                + e);
        }
    }
}