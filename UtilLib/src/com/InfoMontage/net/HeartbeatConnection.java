/*
 * HeartbeatConnection.java
 *
 * Created on November 1, 2003, 3:57 PM
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

import com.InfoMontage.math.BigCounter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketAddress;
import java.io.IOException;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class HeartbeatConnection extends AbstractConnection {
    
    private transient BigCounter numHBsSent=new BigCounter();
    private transient BigCounter numHBsRcvd=new BigCounter();

    /** Creates a new instance of HeartbeatConnection */
    public HeartbeatConnection() throws IOException {
        super();
    }
    
    public HeartbeatConnection(int port)
    throws IllegalArgumentException, IOException {
        super(port);
    }
    
    public HeartbeatConnection(SocketAddress hostAddr)
    throws IOException {
        super(hostAddr);
    }
    
    public HeartbeatConnection(Socket sock, SocketAddress hostAddr)
    throws IOException {
        super(sock, hostAddr);
    }
    
   synchronized public BigInteger getHBsRcvd() {
        //return collectedNumHBsRcvd.add(BigInteger.valueOf(lowNumHBsRcvd));
        return numHBsRcvd.get();
    }
    
    synchronized public BigInteger getHBsSent() {
        //return collectedNumHBsSent.add(BigInteger.valueOf(lowNumHBsSent));
        return numHBsSent.get();
    }
    
    protected AbstractConnection newConnectionObj(SocketAddress sockAddr) throws IOException {
        return new HeartbeatConnection(sockAddr);
    }
    
    static protected AbstractConnection staticNewConnectionObj(java.net.SocketAddress sockAddr)
	throws IOException {
        return new HeartbeatConnection(sockAddr);
    }
    
    protected AbstractConnection newConnectionObj(Socket sock, SocketAddress sockAddr) throws IOException {
        return new HeartbeatConnection(sock, sockAddr);
    }
    
    static protected AbstractConnection staticNewConnectionObj(Socket sock, java.net.SocketAddress sockAddr)
	throws IOException {
        return new HeartbeatConnection(sock, sockAddr);
    }
    
}
