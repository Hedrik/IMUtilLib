/*
 * SimpleConnection.java
 * 
 * Created on Mar 29, 2004
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
import java.net.Socket;
import java.net.SocketAddress;

import com.InfoMontage.version.CodeVersion;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class SimpleConnection extends AbstractConnection {

    public static CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    /**
         * @throws IOException
         */
    public SimpleConnection() throws IOException {
	super();
	// TODO Auto-generated constructor stub
    }

    /**
         * @param port
         * @throws IllegalArgumentException
         * @throws IOException
         */
    public SimpleConnection(int port) throws IllegalArgumentException,
	    IOException {
	super(port);
	// TODO Auto-generated constructor stub
    }

    /**
         * @param hostAddr
         * @throws IOException
         */
    public SimpleConnection(SocketAddress hostAddr) throws IOException {
	super(hostAddr);
	// TODO Auto-generated constructor stub
    }

    /**
         * @param sock, hostAddr
         * @throws IOException
         */
    public SimpleConnection(Socket sock, SocketAddress hostAddr)
	    throws IOException {
	super(sock, hostAddr);
	// TODO Auto-generated constructor stub
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.net.AbstractConnection#newConnectionObj(java.net.InetSocketAddress)
         */
    protected AbstractConnection newConnectionObj(SocketAddress sockAddr)
	    throws IOException {
	return new SimpleConnection(sockAddr);
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.net.AbstractConnection#staticNewConnectionObj(java.net.InetSocketAddress)
         */
    static protected AbstractConnection staticNewConnectionObj(
	    java.net.SocketAddress sockAddr) throws IOException {
	return new SimpleConnection(sockAddr);
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.net.AbstractConnection#newConnectionObj(java.net.InetSocketAddress)
         */
    protected AbstractConnection newConnectionObj(Socket sock,
	    SocketAddress sockAddr) throws IOException {
	return new SimpleConnection(sock, sockAddr);
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.net.AbstractConnection#staticNewConnectionObj(java.net.InetSocketAddress)
         */
    static protected AbstractConnection staticNewConnectionObj(Socket sock,
	    java.net.SocketAddress sockAddr) throws IOException {
	return new SimpleConnection(sock, sockAddr);
    }

}
