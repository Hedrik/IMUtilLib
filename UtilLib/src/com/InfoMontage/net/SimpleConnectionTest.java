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

import com.InfoMontage.stream.AbstractMessageProcessor;
import com.InfoMontage.stream.Message;
import com.InfoMontage.stream.MessageTemplate;

import junit.framework.TestCase;

/**
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class SimpleConnectionTest
    extends TestCase
{

    Connection cServ = null;
    Connection cClient = null;

    public static void main(String[] args) {
        //junit.swingui.TestRunner.run(SimpleConnectionTest.class);
        junit.textui.TestRunner.run(SimpleConnectionTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        try {
//            cServ = new SimpleConnection(12048);
            cServ = new SimpleConnection();
            cServ.setMessageProcessor(new TestMessageProcessor());
            assertTrue("Server accept() call failed!  "+cServ.getAcceptFailureReason(), cServ.accept());
            cClient = new SimpleConnection();
            cClient.setMessageProcessor(new TestMessageProcessor());
//            cClient.connect(new java.net.InetSocketAddress(12048));
            assertTrue("Client connect() call failed!  "+cClient.getConnectFailureReason(), cClient.connect(((SimpleConnection)cServ).getBoundAddress()));
        } catch (IOException e) {
            fail(e.toString());
        } catch (IllegalArgumentException e) {
            fail(e.toString());
        } catch (IllegalStateException e) {
            fail(e.toString());
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        try {
            cClient.close();
            cClient = null;
            cServ.close();
            cServ = null;
        } catch (IOException e) {
            fail(e.toString());
        }
        super.tearDown();
    }

    private class TestMessageProcessor
        extends AbstractMessageProcessor
    {

        /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.stream.AbstractMessageProcessor#getSupportedProtocolVersions()
         */
        public byte[] getSupportedProtocolVersions() {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.stream.AbstractMessageProcessor#processMessage(com.InfoMontage.stream.Message)
         */
        public boolean processMessage(Connection c, Message msg) {
            return true;
        }

	/* (non-Javadoc)
	 * @see com.InfoMontage.task.Task#getResults()
	 */
	public Object[] getResults() throws IllegalStateException {
	    return null;
	}

	/* (non-Javadoc)
	 * @see com.InfoMontage.task.Task#percentComplete()
	 */
	public float percentComplete() {
	    return 0;
	}

	/* (non-Javadoc)
	 * @see com.InfoMontage.stream.MessageProcessor#getMessageType()
	 */
	public MessageTemplate getMessageTemplate() {
	    return null;
	}

	public Class getMessageType() {
	    return null;
	}

    }

    /**
     * Constructor for SimpleConnectionTest.
     * 
     * @param arg0
     */
    public SimpleConnectionTest(String arg0) {
        super(arg0);
    }

    public final void testIsConnected() {
	// 0 = good, 1 = server, 2 = client, 3 = neither connected
	int t = 3;
	long i = System.currentTimeMillis() + (10l*100l);
	while ((t != 0) && ((i-System.currentTimeMillis())>0)) {
	    t = ((cServ.isConnected())?0:1)+((cClient.isConnected())?0:2);
	}
        assertFalse("Server side of connection not connected:\n  \""+cServ.getConnectFailureReason()+"\"", t==1);
        assertFalse("Client side of connection not connected!:\n  "+cClient.getConnectFailureReason()+"\"", t==2);
        assertFalse("Neither client nor server side of connection are connected:\n  Server=\""+cServ.getConnectFailureReason()+"\"\n  Client=\""+cClient.getConnectFailureReason()+"\"", t==3);
    }

    public final void testClose() {
        try {
            cServ.close();
            cClient.close();
        } catch (IOException e) {
            fail(e.toString());
        }
        assertFalse(
            "Server connection still connected after calling close method!",
            cServ.isConnected());
        assertFalse(
            "Client connection still connected after calling close method!",
            cClient.isConnected());
    }

    public final void testGetConnectionProtocolVersion() {
    //TODO Implement getConnectionProtocolVersion().
    }

    public final void testGetMessageProtocolVersion() {
    //TODO Implement getMessageProtocolVersion().
    }
}