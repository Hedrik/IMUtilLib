/*
 * PacketFactory.java
 *
 * Created on May 9, 2004, 3:54 PM
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

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

/**
 *
 * @author  Richard A. Mead <BR> Information Montage
 */
class PacketFactory {
    
    public static CodeVersion implCodeVersion = new GenericCodeVersion("0.8a");
    
   /** The singleton instance of this class.
     */
    static private final PacketFactory singleton=new PacketFactory();
    
    static PacketFactory getInstance() { return singleton; }
    
    static private transient PacketFactory defaultPacketFactory=singleton;
    
    static PacketFactory getDefaultPacketFactory() {
        return defaultPacketFactory;
    }
    
   /** PacketFactories are singletons.  Classes derived from PacketFactory
     * should by convention define a private parameterless constructor.
     */
    protected PacketFactory() {}
    
    static void setDefaultPacketFactory(PacketFactory pktFact) {
        if (pktFact==null) {
            pktFact=PacketFactory.singleton;
        }
        synchronized (defaultPacketFactory) {
            defaultPacketFactory=pktFact;
        }
    }
    
    static short getDefaultPayloadSize() {
        return Packet.DEFAULT_PACKET_PAYLOAD_LENGTH;
    }
    
    static Packet getHeartbeatPacket() {
        return Packet.getHeartbeatPacket();
    }
    
    static Packet[] decompose(ByteBuffer ibb,short psz,long gen, long msg)
    throws IllegalArgumentException {
        return Packet.decompose(ibb, psz, gen, msg);
    }
    
    static Packet newPacket(long gid, long mid, int pid, short l, byte[] p) {
        return Packet.newPacket(gid,mid,pid,l,p);
    }
    
    static ByteBuffer recombine(Packet[] pkts)
    throws IllegalArgumentException, NullPointerException {
        return Packet.recombine(pkts);
    }
    
    static Packet valueOf(ByteBuffer buf)
    throws IllegalArgumentException, BufferUnderflowException {
        return Packet.valueOf(buf);
    }
    
}
