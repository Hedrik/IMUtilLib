/*
 * ChecksumPacket.java
 *
 * Created on November 14, 2003, 10:48 PM
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
import com.InfoMontage.math.BigCounter;
import com.InfoMontage.util.Math;
import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

/**
 *
 * @author  Richard A. Mead <BR> Information Montage
 */
class ChecksumPacket extends Packet {
    
    public static CodeVersion implCodeVersion = new GenericCodeVersion("0.7.5a");
    
    static final int PACKET_MAGIC_ID=ByteBuffer.wrap("CpKt".getBytes()).getInt();
    
    long chksum = 0;
    
    /** Creates a new instance of Packet with default values for all fields.
     * You must override this in subclasses if you wish the heartbeat packet
     * to be of the correct class, thus having all the appropriate fields, as
     * this is the object that is returned by the getHeartbeatPacket() method.
     */
    static final Packet HEARTBEAT_PACKET=new ChecksumPacket(0,0,0,(short)0,null);
    
    /** Creates a new instance of Packet with values for all fields provided
     * as parameters.<P>
     *  The provided data will be validated.
     */
    ChecksumPacket(long gid, long mid, int pid, short l, byte[] p)
    throws IllegalArgumentException {
        super(gid,mid,pid,l, p);
        chksum=getChkSum();
    }
    
    public String toString() {
        StringBuffer s=new StringBuffer("cksum=").append(chksum);
        return super.toString(s);
    }
    
    public boolean equals(ChecksumPacket p)
    throws IllegalStateException {
        boolean isEqual=super.equals(p);
        if ((isEqual) && (p.chksum!=this.chksum))
            throw new IllegalStateException("Packets with equal msgID and"
            +" pktID found with differing checksums!");
        return isEqual;
    }
    
    boolean isValid() {
        boolean valid=super.isValid();
        if ((valid) && (pktID!=0))
            valid=(chksum==getChkSum());
        return valid;
    }
    
    long getChkSum() {
        long cs=0;
        if (payload!=null && payload.length>0) {
            cs=(long)payload[1];
            if (payload.length>1) {
                BigCounter ls=new BigCounter(cs);
                for (int i=payload.length; i>1; i--)
                    ls.add((long)payload[i]);
                cs=chkSumMod(ls);
            }
        }
        return cs;
    }
    
    static long chkSumMod(BigCounter bc) {
        return Math.modLong(bc);
    }
    
    static Packet newPacket(long gid, long mid, int pid, short l, byte[] p) {
        return new ChecksumPacket(gid,mid,pid,l,p);
    }
    
    static ByteBuffer recombine(ChecksumPacket[] pkts)
    throws IllegalArgumentException, NullPointerException {
        com.InfoMontage.math.BigCounter cs=new com.InfoMontage.math.BigCounter();
        for (int i=1; i<pkts.length; i++) {
            cs.add(pkts[i].chksum);
        }
        if (chkSumMod(cs)!=pkts[0].chksum)
            throw new IllegalArgumentException("Attempt to combine Packets with"
            +" a total checksum differing from the header packet value!");
        return Packet.recombine(pkts);
    }
    
    protected void appendPacketHeader(ByteBuffer bb) {
        super.appendPacketHeader(bb);
        bb.putLong(chksum);
    }
    
    protected int metaDataLength() {
        // add 8 for checksum
        return super.metaDataLength()+8;
    }

    protected void readMetaData(ByteBuffer b)
        throws NullPointerException, BufferUnderflowException {
        super.readMetaData(b);
        chksum=b.getLong();
    }
    
}
