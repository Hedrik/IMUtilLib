/*
 * Conduit_UDP_TestClass.java
 * JUnit based test
 *
 * Created on May 22, 2004, 11:01 PM
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
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import com.InfoMontage.common.Defaults;

import junit.framework.*;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class Conduit_UDP_TestClass extends TestCase {
    
    final int inPort=12354;
    final int outPort=12355;
    
    Conduit cin;
    Conduit cout;
    
    public Conduit_UDP_TestClass(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.run(Conduit_UDP_TestClass.class);
    }
    
    protected void setUp() throws Exception {
        final DatagramChannel inSC=DatagramChannel.open();
        inSC.configureBlocking(false);
        final DatagramSocket inS=inSC.socket();
        inS.setReuseAddress(true);
        //inSS.setSoTimeout(200);
        InetAddress lhIA=InetAddress.getLocalHost();
        inS.bind(new InetSocketAddress(lhIA,0));
        final SocketAddress inSA=inS.getLocalSocketAddress();
        final DatagramChannel outSC=DatagramChannel.open();
        outSC.configureBlocking(false);
        final DatagramSocket outS=outSC.socket();
        outS.setReuseAddress(true);
        //outS.setSoLinger(false,0);
        outS.setSoTimeout(20);
        outS.bind(new InetSocketAddress(lhIA,0));
        final SocketAddress outSA=outS.getLocalSocketAddress();
        Thread inT=new Thread() {
            public void run() {
                try {
                    inSC.connect(outSA);
                    cin=new Conduit(inSC);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        inT.start();
        Thread outT=new Thread() {
            public void run() {
                try {
                    outSC.connect(inSA);
                    cout=new Conduit(outSC);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        outT.start();
        inT.join();
        outT.join();
    }
    
    protected void tearDown() throws Exception  {
        cout.close();
        cout=null;
        cin.close();
        cin=null;
        System.out.flush();
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    /**
     * Test of isOpen method, of class com.InfoMontage.net.Conduit.
     */
    public void testIsOpen() {
        System.out.println("testIsOpen");
        
        assertTrue("The in Conduit is not open!",cin.isOpen());
        assertTrue("The out Conduit is not open!",cout.isOpen());
    }
    
    /**
     * Test of close method, of class com.InfoMontage.net.Conduit.
     */
    public void testClose() throws IOException {
        System.out.println("testClose");
        
        cin.close();
        assertFalse("Close on in Conduit failed!", cin.isOpen());
        cout.close();
        assertFalse("Close on out Conduit failed!", cout.isOpen());
    }
    
    public void testHasInput() throws IOException {
        System.out.print("testHasInput: ");
        
        String testString="testHasInput blah test";
        java.nio.CharBuffer cbuf=java.nio.CharBuffer.allocate(testString.length());
        cbuf.put(Defaults.DEFAULT_CHARSET.encode(testString).asCharBuffer());
        char[] ocbuf=cbuf.array();
        cout.write(ocbuf, 0, ocbuf.length);
        cout.flush();
        System.out.print("write, ");
        int count=0, timeout=30;
        try {
            while (!cin.hasInput() && ++count<timeout) {
                System.out.print(".");
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println((cin.hasInput())?"received":"timed out");
            assertTrue("Input not recieved!", cin.hasInput());
        }
    }
    
    /**
     * Test of 'raw' write and read methods, of class com.InfoMontage.net.Conduit.
     */
    public void testRawWriteRead() throws IOException {
        System.out.print("testRawWriteRead: ");
        
        String testString="testRawWriteRead blah test";
        char[] ocbuf=testString.toCharArray();
        char[] icbuf=new char[ocbuf.length];
        cout.write(ocbuf, 0, ocbuf.length);
        cout.flush();
        System.out.print("write=\"");
        System.out.print(ocbuf);
        System.out.print("\" {"+ocbuf.length+" chars}, ");
        int count=0, timeout=60, numRead=0;
        try {
            while (!cin.hasInput() && ++count<timeout) {
                System.out.print(".");
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (cin.hasInput()) {
            System.out.print(icbuf);
            numRead=cin.read(icbuf, 0, icbuf.length);
            System.out.print("read=\"");
            System.out.print(icbuf);
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        assertTrue("read does not match write!\n"
        +"write=\""+String.copyValueOf(ocbuf)+"\"\n"
        +"read=\""+String.copyValueOf(icbuf)+"\""
        ,java.util.Arrays.equals(icbuf,ocbuf));
    }
    
    /**
     * Test of write and read methods using Packets, of class
     * com.InfoMontage.net.Conduit.
     */
    public void testPacketizedWriteRead() throws IOException {
        System.out.print("testPacketizedWriteRead: ");
        
        assertTrue("Default PacketFactory for output Conduit is not \""
        +"PacketFactory\" - it is \""+cout.getPacketFactory().getClass()
        +"\"",cout.getPacketFactory()==PacketFactory.getInstance());
        assertTrue("Default PacketFactory for input Conduit is not \""
        +"PacketFactory\" - it is \""+cin.getPacketFactory().getClass()
        +"\"",cin.getPacketFactory()==PacketFactory.getInstance());
        
        String testString="testPacketizedWriteRead blah test";
        ByteBuffer ocbuf=ByteBuffer.wrap(testString.getBytes());
        ByteBuffer icbuf=ByteBuffer.allocate(ocbuf.capacity());
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        int count=0, timeout=60, numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read=\"");
            System.out.print(new String(icbuf.array()));
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        assertTrue("read does not match write!\n"
        +"write=\""+com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(ocbuf));
    }
    
    public void testPacketizedMultiWriteRead() throws IOException {
        System.out.print("testPacketizedMultiWriteRead: ");
        
        assertTrue("Default PacketFactory for output Conduit is not \""
        +"PacketFactory\" - it is \""+cout.getPacketFactory().getClass()
        +"\"",cout.getPacketFactory()==PacketFactory.getInstance());
        assertTrue("Default PacketFactory for input Conduit is not \""
        +"PacketFactory\" - it is \""+cin.getPacketFactory().getClass()
        +"\"",cin.getPacketFactory()==PacketFactory.getInstance());
        
        String testString="testPacketizedMultiWriteRead blah test";
        ByteBuffer ocbuf=ByteBuffer.wrap(testString.getBytes());
        ByteBuffer icbuf=ByteBuffer.allocate(2*ocbuf.capacity());
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write #1=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write #2=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        int count=0, timeout=60, numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                    numRead+=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read=\"");
            System.out.print(new String(icbuf.array()));
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        ByteBuffer tbb=ByteBuffer.allocate(icbuf.capacity());
        tbb.put(ocbuf);
        ocbuf.rewind();
        tbb.put(ocbuf).rewind();
        ocbuf.rewind();
        assertTrue("read does not match writes!\n"
        +"writes=\""+com.InfoMontage.util.Buffer.toString(ocbuf)
        +com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(tbb));
    }
    
    public void testPacketizedMultiWriteMultiRead() throws IOException {
        System.out.print("testPacketizedMultiWriteMultiRead: ");
        
        assertTrue("Default PacketFactory for output Conduit is not \""
        +"PacketFactory\" - it is \""+cout.getPacketFactory().getClass()
        +"\"",cout.getPacketFactory()==PacketFactory.getInstance());
        assertTrue("Default PacketFactory for input Conduit is not \""
        +"PacketFactory\" - it is \""+cin.getPacketFactory().getClass()
        +"\"",cin.getPacketFactory()==PacketFactory.getInstance());
        
        String testString="testPacketizedMultiWriteMultiRead blah test";
        ByteBuffer ocbuf=ByteBuffer.wrap(testString.getBytes());
        ByteBuffer icbuf=ByteBuffer.allocate(ocbuf.capacity());
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write #1=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        int count=0, timeout=60, numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read #1=\"");
            System.out.print(new String(icbuf.array()));
            System.out.print("\" {"+numRead+" chars}");
        } else {
            System.out.print("timed out");
        }
        assertTrue("read #1 does not match write #1!\n"
        +"write=\""+com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(ocbuf));
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print(" | write #2=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        count=0;
        timeout=60;
        numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read #2=\"");
            System.out.print(new String(icbuf.array()));
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        assertTrue("read #2 does not match write #2!\n"
        +"write=\""+com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(ocbuf));
    }
    
    public void testPacketizedLargeWriteRead() throws IOException {
        System.out.print("testPacketizedLargeWriteRead: ");
        
        assertTrue("Default PacketFactory for output Conduit is not \""
        +"PacketFactory\" - it is \""+cout.getPacketFactory().getClass()
        +"\"",cout.getPacketFactory()==PacketFactory.getInstance());
        assertTrue("Default PacketFactory for input Conduit is not \""
        +"PacketFactory\" - it is \""+cin.getPacketFactory().getClass()
        +"\"",cin.getPacketFactory()==PacketFactory.getInstance());
        
        StringBuffer testString=new StringBuffer("testPacketizedLargeWriteRead blah test");
        testString.append(testString).append(testString).append(testString)
        .append(testString).append(testString).append(testString).append(testString)
        .append(testString).append(testString);
        ByteBuffer ocbuf=ByteBuffer.wrap(testString.toString().getBytes());
        ByteBuffer icbuf=ByteBuffer.allocate(ocbuf.capacity());
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        int count=0, timeout=30, numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read=\"");
            System.out.print(new String(icbuf.array()));
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        assertTrue("read does not match write!\n"
        +"write=\""+com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(ocbuf));
    }
    
    public void testChecksumPacketizedWriteRead() throws IOException {
        System.out.print("testChecksumPacketizedWriteRead: ");
        
        cout.setPacketFactory(ChecksumPacketFactory.getInstance());
        cin.setPacketFactory(ChecksumPacketFactory.getInstance());
        
        assertTrue("PacketFactory for output Conduit was not successfully set to \""
        +"ChecksumPacketFactory\" - it is \""+cout.getPacketFactory().getClass()
        +"\"",cout.getPacketFactory()==ChecksumPacketFactory.getInstance());
        assertTrue("PacketFactory for input Conduit was not successfully set to \""
        +"ChecksumPacketFactory\" - it is \""+cin.getPacketFactory().getClass()
        +"\"",cin.getPacketFactory()==ChecksumPacketFactory.getInstance());
        
        String testString="testChecksumPacketizedWriteRead blah test";
        ByteBuffer ocbuf=ByteBuffer.wrap(testString.getBytes());
        ByteBuffer icbuf=ByteBuffer.allocate(ocbuf.capacity());
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        int count=0, timeout=60, numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read=\"");
            System.out.print(new String(icbuf.array()));
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        assertTrue("read does not match write!\n"
        +"write=\""+com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(ocbuf));
    }
    
    public void testChecksumPacketizedMultiWriteRead() throws IOException {
        System.out.print("testChecksumPacketizedMultiWriteRead: ");
        
        cout.setPacketFactory(ChecksumPacketFactory.getInstance());
        cin.setPacketFactory(ChecksumPacketFactory.getInstance());
        
        assertTrue("PacketFactory for output Conduit was not successfully set to \""
        +"ChecksumPacketFactory\" - it is \""+cout.getPacketFactory().getClass()
        +"\"",cout.getPacketFactory()==ChecksumPacketFactory.getInstance());
        assertTrue("PacketFactory for input Conduit was not successfully set to \""
        +"ChecksumPacketFactory\" - it is \""+cin.getPacketFactory().getClass()
        +"\"",cin.getPacketFactory()==ChecksumPacketFactory.getInstance());
        
        String testString="testChecksumPacketizedMultiWriteRead blah test";
        ByteBuffer ocbuf=ByteBuffer.wrap(testString.getBytes());
        ByteBuffer icbuf=ByteBuffer.allocate(2*ocbuf.capacity());
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write #1=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write #2=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        int count=0, timeout=60, numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                    numRead+=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read=\"");
            System.out.print(new String(icbuf.array()));
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        ByteBuffer tbb=ByteBuffer.allocate(icbuf.capacity());
        tbb.put(ocbuf);
        ocbuf.rewind();
        tbb.put(ocbuf).rewind();
        ocbuf.rewind();
        assertTrue("read does not match write!\n"
        +"writes=\""+com.InfoMontage.util.Buffer.toString(ocbuf)
        +com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(tbb));
    }
    
    public void testChecksumPacketizedMultiWriteMultiRead() throws IOException {
        System.out.print("testChecksumPacketizedMultiWriteMultiRead: ");
        
        cout.setPacketFactory(ChecksumPacketFactory.getInstance());
        cin.setPacketFactory(ChecksumPacketFactory.getInstance());
        
        assertTrue("PacketFactory for output Conduit was not successfully set to \""
        +"ChecksumPacketFactory\" - it is \""+cout.getPacketFactory().getClass()
        +"\"",cout.getPacketFactory()==ChecksumPacketFactory.getInstance());
        assertTrue("PacketFactory for input Conduit was not successfully set to \""
        +"ChecksumPacketFactory\" - it is \""+cin.getPacketFactory().getClass()
        +"\"",cin.getPacketFactory()==ChecksumPacketFactory.getInstance());
        
        String testString="testChecksumPacketizedMultiWriteMultiRead blah test";
        ByteBuffer ocbuf=ByteBuffer.wrap(testString.getBytes());
        ByteBuffer icbuf=ByteBuffer.allocate(ocbuf.capacity());
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print("write #1=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        int count=0, timeout=60, numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read #1=\"");
            System.out.print(new String(icbuf.array()));
            System.out.print("\" {"+numRead+" chars}");
        } else {
            System.out.print("timed out");
        }
        assertTrue("read #1 does not match write #1!\n"
        +"write=\""+com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(ocbuf));
        cout.write(ocbuf);
        //cout.flush();
        ocbuf.rewind();
        System.out.print(" | write #2=\"");
        System.out.print(new String(ocbuf.array()));
        System.out.print("\" {"+ocbuf.capacity()+" chars}, ");
        count=0;
        timeout=60;
        numRead=0;
        try {
            while (numRead==0 && count<timeout) {
                while (!cin.hasInput() && ++count<timeout) {
                    System.out.print(".");
                    Thread.sleep(200);
                }
                if (cin.hasInput()) {
                    System.out.print("!");
                    numRead=cin.read(icbuf);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (numRead>0) {
            icbuf.rewind();
            System.out.print("read #2=\"");
            System.out.print(new String(icbuf.array()));
            System.out.println("\" {"+numRead+" chars}");
        } else {
            System.out.println("timed out");
        }
        assertTrue("read #2 does not match write #2!\n"
        +"write=\""+com.InfoMontage.util.Buffer.toString(ocbuf)+"\"\n"
        +"read=\""+com.InfoMontage.util.Buffer.toString(icbuf)+"\""
        ,icbuf.equals(ocbuf));
    }
    
}