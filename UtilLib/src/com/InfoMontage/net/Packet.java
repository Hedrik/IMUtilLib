/*
 * Packet.java
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
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
class Packet implements Comparable {

    public static CodeVersion implCodeVersion = new GenericCodeVersion(
            "0.8a");

    static final int PACKET_MAGIC_ID = ByteBuffer.wrap("PaKt".getBytes())
            .getInt();

    public static final short DEFAULT_PACKET_PAYLOAD_LENGTH = 4096;

    long genID = 0; // determined at connection protocol negotiation
    long msgID = 0;
    int pktID = 0;
    short len = Short.MIN_VALUE; // to differentiate an uninitialized Packet
                                 // from a heartbeat
    byte[] payload = null;

    /**
     * Creates a new instance of Packet with default values for all fields.
     * You must override this in subclasses if you wish the heartbeat packet
     * to be of the correct class, thus having all the appropriate fields as
     * this is the object that is returned by the getHeartbeatPacket() method.
     */
    static final Packet HEARTBEAT_PACKET = new Packet(0, 0, 0, (short) 0,
            null);

    static Packet getHeartbeatPacket() {
        return HEARTBEAT_PACKET;
    }

    /**
     * Creates a new instance of Packet with values for all fields provided as
     * parameters.
     * <P>
     * The provided data will be validated.
     */
    Packet(long gid, long mid, int pid, short l, byte[] p)
            throws IllegalArgumentException
    {
        genID = gid;
        msgID = mid;
        pktID = pid;
        len = l;
        payload = p;
        if (!Packet.isValid(this))
            throw new IllegalArgumentException(
                    "Attempt to create a Packet with"
                            + " invalid or inconsistent parameters!");
    }

    /*
     * This method is provided to allow subclasses to easily generate their
     * own toString() values in the same format without having to duplicate
     * code. This is necessary because the payload is generally to be expected
     * as the last part of the returned String. If this is not the case for
     * the subclass then overriding the toString() method will account for the
     * difference, so this method is declared final. Note that if the caller
     * of this method passes a non-null value, it does not need to include a
     * comma.
     */
    public final String toString(StringBuffer xtra) {
        StringBuffer s = new StringBuffer("Packet[gen=");
        s.append(genID).append(",msg=");
        s.append(msgID).append(",pkt=");
        s.append(pktID);
        if (pktID == 0)
            s.append("{header}");
        s.append(",len=").append(len);
        if (xtra != null)
            s.append(",").append(xtra);
        s.append(",payload=");
        if (payload == null)
            s.append("null");
        else if (payload.length == 0)
            s.append("empty");
        else {
            s.append("{").append(payload[0]);
            for (int i = 1; i < payload.length; i++ )
                s.append(",").append(payload[i]);
            s.append("}");
        }
        s.append("]");
        return s.toString();
    }

    public String toString() {
        return toString(null);
    }

    /*
     * Since the combination of msgID and pktID should uniquely identify a
     * Packet (the genID is intended to validate the two ends of the
     * Connection are in sync with each other with regard to the connection
     * protocol version to be used, and thus specifies a grouping for Packets
     * that does not need to be accounted for in the hashCode, since it is
     * expected that only Packets with the same genID will be stored in any
     * Collection or array to be sorted), they are the only fields used here
     * to calculate the hash code.
     */
    public final int hashCode() {
        // 17*37=629
        return (629 + (int) (msgID ^ (msgID >>> 32))) * 37 + pktID;
    }

    public boolean equals(Packet p) throws IllegalStateException {
        boolean isEqual = ( (p.msgID == this.msgID) && (p.pktID == this.pktID));
        if (isEqual
                && ( (p.len != this.len) || !java.util.Arrays.equals(
                        p.payload, this.payload)))
            throw new IllegalStateException("Packets with equal msgID and"
                    + " pktID found with differing content!");
        return isEqual;
    }

    public boolean equals(Object obj) throws IllegalStateException {
        return ( (obj == this) || ( (obj instanceof Packet) && equals((Packet) obj)));
    }

    public int compareTo(Packet o) throws NullPointerException {
        return (o == this) ? 0 : (msgID < o.msgID) ? -1 : (msgID > o.msgID)
                ? 1 : (pktID < o.pktID) ? -1 : (pktID > o.pktID) ? 1 : 0;
    }

    public int compareTo(Object o)
            throws NullPointerException, ClassCastException
    {
        return (o == this) ? 0 : compareTo((Packet) o);
    }

    /**
     * Check the validity of a Packet. This method is provided to eliminate
     * the need to duplicate validity checking code - otherwise duplicate code
     * would be needed at instantiation time to validate the constructors'
     * parameters. Using this method allows the constructor to call the
     * Packet-specific validity checking routine, so that subclasses which
     * call super({params}) are guaranteed that the parameters passed have
     * been validated, while also being available for the non-static method to
     * use. Non-final subclasses can use a similar method (providing their own
     * private static class-specific validation method) to validate their
     * constructors' parameters.
     * <P>
     * Note that a <CODE>null</CODE> Packet parameter is never valid.
     */
    private static boolean isValid(Packet p) {
        boolean valid = false;
        if (p != null)
            if (p.pktID == 0) { // header packet
                if (p.payload == null)
                    valid = true;
            } else if ( (p.payload != null) // null only valid for header
                                            // packets
                    && (p.pktID > 0) // must be sequential to header packet
                    && (p.len == p.payload.length))
                valid = true;
        return valid;
    }

    boolean isValid() {
        return Packet.isValid(this);
    }

    /**
     * Converts a {@link ByteBuffer}into a series of {@link Packet}s. Each
     * packet will have the given genID, msgID, and a unique, sequential
     * pktID. The first Packet in the returned array will have the special
     * pktID of 0 (zero) indicating the header packet for the message
     * contained in the packet series. This packet does not contain any of the
     * actual data from the {@link ByteBuffer}, rather it contains metadata
     * for the sequence of packets: the number of packets is stored in the
     * length field. The payload for the header {@link Packet}will be null.
     * <P>
     * Each generated {@link Packet}(other than the header {@link Packet})
     * will have a payload with a length specified by the psz parameter,
     * excepting possibly the last {@link Packet}which may have less.
     * 
     * @param ibb The {@link ByteBuffer}containing the message to be
     *            decomposed into {@link Packet}s.
     * @param psz The size of the payload for each {@link Packet}.
     * @param gen The generation ID to use for the generated {@link Packet}
     *            series.
     * @param msg The message ID to use for the generated {@link Packet}
     *            series.
     * @return An array of {@link Packet}s containing the message and
     *         starting with a header {@link Packet}.
     * @throws NullPointerException if <CODE>ibb</CODE> is <CODE>null
     *             </CODE>.
     * @throws IllegalArgumentException if <CODE>ibb</CODE> is of zero
     *             length, or if <CODE>psz</CODE> is negative or zero.
     */
    static Packet[] decompose(ByteBuffer ibb, short psz, long gen, long msg)
            throws IllegalArgumentException
    {
        if (ibb == null)
            throw new NullPointerException(
                    "Attempt to compose Packets from"
                            + " a null ByteBuffer!");
        if (ibb.remaining() == 0)
            throw new IllegalArgumentException(
                    "Attempt to compose Packets from"
                            + " an empty ByteBuffer!");
        if (psz < 1)
            throw new IllegalArgumentException(
                    "Attempt to compose Packets from"
                            + " a ByteBuffer using a non-positive payload size!");
        int np = ibb.remaining() / psz;
        int rb = ibb.remaining() - (np * psz);
        np += (rb > 0) ? 1 : 0;
        Packet[] rpa = new Packet[np + 1];
        rpa[0] = newPacket(gen, msg, 0, (short) (np), null);
        for (int i = 1; i < np; i++ ) {
            byte[] p = new byte[psz];
            ibb.get(p);
            rpa[i] = newPacket(gen, msg, i, psz, p);
        }
        byte[] p = new byte[rb];
        ibb.get(p);
        rpa[np] = newPacket(gen, msg, np, (short) rb, p);
        return rpa;
    }

    static Packet newPacket(long gid, long mid, int pid, short l, byte[] p)
    {
        Packet npkt = new Packet(gid, mid, pid, l, p);
        System.err.println("Created Packet: " + npkt);
        return npkt;
    }

    /**
     * Converts the provided array of {@link Packet}s to a {@link ByteBuffer}
     * containing the message carried by the series of packets. The array may
     * consist of only a header packet - such a message might be used as a
     * 'ping' or 'heartbeat' containing no data other than a message ID. Such
     * a packet is provided as the constant Packet.HEARTBEAT_PACKET for
     * convenience. The {@link Packet}array will be manipulated and validated
     * as follows:
     * <P>
     * 1) It will be sorted in place to facilitate further processing, thus
     * modifying the provided array.
     * <P>
     * 2) It must contain a header packet. (<CODE>pktID</CODE> of 0)
     * <P>
     * 3) The header packet must have a <CODE>null</CODE> payload. (This is
     * checked when the packet is created)
     * <P>
     * 4) The number of packets in the array must be equal to the value of the
     * <CODE>len</CODE> field of the header packet plus one for the header
     * packet itself.
     * <P>
     * 5) The checksum of the contents of the resulting <CODE>ByteBuffer
     * </CODE> must equal the value of the <CODE>cksum</CODE> field of the
     * header packet.
     * <P>
     * 6) Every packet must have the same value in the <CODE>msgID</CODE>
     * field.
     * <P>
     * 7) Each packet must have a unique, sequentially increasing value in the
     * <CODE>pktID</CODE> field.
     * <P>
     * The following two validations are performing at the time of packet
     * creation.
     * <P>
     * 8) The length of each packet's payload must equal the value of that
     * packet's <CODE>len</CODE> field.
     * <P>
     * 9) The checksum of the contents of each packet's payload must equal the
     * value of that packet's <CODE>cksum</CODE> field.
     * 
     * @param pkts The array of <CODE>Packet</CODE> s to be processed.
     * @return A {@link ByteBuffer}containing the message carried by the
     *         series of packets. The length of the returned
     *         {@link ByteBuffer}will be zero if the provided array of
     *         {@link Packet}s contains only the "Heartbeat" header packet.
     * @throws IllegalArgumentException if validation fails.
     * @throws NullPointerException if <CODE>pkts</CODE> is <CODE>null
     *             </CODE>.
     */
    static ByteBuffer recombine(Packet[] pkts)
    // TBD: change to accept a List of Packets for performance
            // {so that List.toArray is not needed
            throws IllegalArgumentException, NullPointerException
    {
        int al = pkts.length;
        if (al == 0)
            throw new IllegalArgumentException(
                    "Attempt to combine Packets array"
                            + " with no elements!");
        ByteBuffer rb = null;
        int l = 0;
        if (al > 1)
            java.util.Arrays.sort(pkts);
        if (pkts[0].pktID != 0)
            throw new IllegalArgumentException(
                    "Attempt to combine Packets with"
                            + " no header packet!");
        if (pkts[0].payload != null)
            throw new IllegalArgumentException(
                    "Attempt to combine Packets with"
                            + " a header packet containing a non-null payload!");
        if (pkts[0].len != al - 1)
            throw new IllegalArgumentException(
                    "Attempt to combine Packets with"
                            + " a header packet specifying a different number of packets than"
                            + " the number available!");
        if (al > 1) {
            long m = pkts[0].msgID;
            for (int i = 1; i < al; i++ ) {
                if (pkts[i].msgID != m)
                    throw new IllegalArgumentException(
                            "Attempt to combine Packets with"
                                    + " differing message IDs!");
                if (pkts[i].pktID != i)
                    throw new IllegalArgumentException(
                            "Attempt to combine Packets with"
                                    + " nonsequential packet IDs!");
                l += pkts[i].len;
            }
            rb = ByteBuffer.allocate(l);
            for (int i = 1; i < al; i++ ) {
                rb.put(pkts[i].payload);
            }
            rb.rewind();
        } else {
            rb = ByteBuffer.allocate(0);
        }
        return rb;
    }

    public int byteLength() {
        return ( (payload == null) ? 0 : payload.length) + metaDataLength();
    }

    ByteBuffer toByteBuffer() {
        ByteBuffer rb = ByteBuffer.allocate(this.byteLength());
        appendPacketHeader(rb);
        if (payload != null) {
            rb.put(payload);
        }
        rb.rewind();
        return rb;
    }

    /**
     * Places this {@link Packet}'s metadata and payload at the end of the
     * provided ByteBuffer. If the provided ByteBuffer is not long enough to
     * hold all the data, a new one is allocated and returned in place of the
     * one passed in.
     * 
     * @param bb The ByteBuffer to append this {@link Packet}'s data to.
     * @throws NullPointerException if t <CODE>bb</CODE> is null.
     * @return A ByteBuffer containing the data from the buffer passed in to
     *         this routine, with this {@link Packet}'s data appended to it.
     *         The returned ByteBuffer may be the same as the one passed to
     *         the routine, or it may be a newly allocated one. The original
     *         ByteBuffer will NOT be modified if a newly allocated one is
     *         returned.
     */
    ByteBuffer appendToByteBuffer(ByteBuffer bb)
            throws NullPointerException
    {
        int need = ( ( (payload == null) ? 0 : payload.length) + metaDataLength())
                - bb.remaining();
        ByteBuffer rb = null;
        if (need > 0)
            rb = ByteBuffer.allocate(need + bb.capacity()).put(bb);
        else
            rb = bb;
        appendPacketHeader(rb);
        rb.put(payload);
        return rb;
    }

    /**
     * Helper method used to determine the length of this {@link Packet}'s
     * metadata. It is intended to be overridden by any subclass that includes
     * additional metadata. Such an overriding method should add the length of
     * it's metadata to the length returned by calling this method {via <CODE>
     * super.metaDataLength()</CODE>} and return the resulting sum.
     * <P>
     * This method is not static so that the potential for variable length
     * metadata fields can exist. This method is called only when converting
     * an existing Packet into it's encoded ByteBuffer form.
     * 
     * @return The length of this Packet's metadata.
     */
    protected int metaDataLength() {
        // 4+8+8+4+2=26
        return 26;
    }

    public int minimumPacketLength() {
        return HEARTBEAT_PACKET.metaDataLength();
    }

    /**
     * Internal routine to append a {@link Packet}'s metadata to a
     * ByteBuffer. This method should be overridden by subclasses, and the
     * overriding code must call this method prior to adding it's own metadata
     * to the buffer {via <CODE>super.appendToByteBuffer(bb)</CODE>}.
     * <P>
     * It is assumed that the ByteBuffer is not null and it's remaining space
     * is large enough to contain the metadata; no checking is performed.
     * 
     * @param bb The ByteBuffer to append this {@link Packet}'s metadata to.
     * @throws NullPointerException if t <CODE>bb</CODE> is null.
     * @throws BufferOverflowException if the ByteBuffer does not have enough
     *             room left to append the metadata.
     */
    protected void appendPacketHeader(final ByteBuffer bb)
            throws NullPointerException, BufferOverflowException
    {
        bb.putInt(PACKET_MAGIC_ID).putLong(genID).putLong(msgID).putInt(
                pktID).putShort(len);
    }

    static Packet valueOf(ByteBuffer buf)
            throws IllegalArgumentException, BufferUnderflowException
    {
        long g, m;
        int p;
        short l;
        byte[] b;
        Packet rp = null;
        if (buf.remaining() < HEARTBEAT_PACKET
                .minimumPacketLength()) {
            System.err.println("Oooops!");
        }
        if (buf != null
                && buf.remaining() >= HEARTBEAT_PACKET
                        .minimumPacketLength())
        {
            ByteBuffer bb = buf.asReadOnlyBuffer();
            // parse it
            try {
                if (bb.getInt() != PACKET_MAGIC_ID)
                    throw new IllegalArgumentException(
                            "Attempt to parse a ByteBuffer"
                                    + " with an invalid Packet marker!");
                g = bb.getLong();
                m = bb.getLong();
                p = bb.getInt();
                l = bb.getShort();
                b = ( (l < 1) || (p == 0)) ? null : new byte[l];
                rp = newPacket(g, m, p, l, b);
                rp.readMetaData(bb);
                if ( (rp.payload != null) && (rp.payload.length > 0)) {
                    bb.get(rp.payload);
                }
            } catch (BufferUnderflowException e) {
                rp = null;
            }
            if (rp != null) {
                buf.position(bb.position());
            }
        }
        return rp;
    }

    /**
     * Override this in subclasses to read metadata from the input ByteBuffer.
     * This method is called immediately after having read the base Packet
     * class' metadata and just prior to reading the actual payload.
     * sub-subclasses should be sure to call their superclass' version of this
     * method {via <CODE>super.readMetaData(b)</CODE>} before reading their
     * own metadata from the buffer.
     * <P>
     * It is assumed that the buffer passed in contains valid metadata.
     * 
     * @param b The ByteBuffer to read the metadata from.
     * @throws NullPointerException if <CODE>b</CODE> is null.
     * @throws BufferUnderflowException if the ByteBuffer does not contain
     *             enough data to read.
     */
    protected void readMetaData(ByteBuffer b)
            throws NullPointerException, BufferUnderflowException
    {}

}