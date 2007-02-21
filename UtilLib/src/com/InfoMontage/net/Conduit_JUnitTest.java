/*
 * Conduit_JUnitTest.java JUnit based test
 * 
 * Created on April 18, 2004, 9:20 PM
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class Conduit_JUnitTest extends TestCase {

    final int inPort = 12354;

    final int outPort = 12355;

    Conduit cinT;

    Conduit coutT;

    Conduit cinU;

    Conduit coutU;

    public Conduit_JUnitTest(java.lang.String testName) {
	super(testName);
    }

    public static void main(String[] args) {
	junit.swingui.TestRunner.run(Conduit_JUnitTest.class);
    }

    public static Test suite() {
	TestSuite suite = new TestSuite(Conduit_JUnitTest.class);
	return suite;
    }

    protected void setUp() throws Exception {
	setUpUDP();
	setUpTCP();
    }

    protected void setUpTCP() throws Exception {
	final java.net.ServerSocket inSS = ServerSocketChannel.open().socket();
	inSS.setReuseAddress(true);
	// inSS.setSoTimeout(200);
	inSS.bind(new InetSocketAddress("localhost", 0));
	final SocketChannel outSC = SocketChannel.open();
	outSC.configureBlocking(false);
	final Socket outS = outSC.socket();
	outS.setReuseAddress(true);
	outS.setSoLinger(false, 0);
	outS.setSoTimeout(20);
	outS.bind(new InetSocketAddress("localhost", 0));
	Thread inT = new Thread("Input") {

	    public void run() {
		try {
		    SocketChannel sc = inSS.getChannel().accept();
		    sc.configureBlocking(false);
		    cinT = new Conduit(sc);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	};
	inT.start();
	Thread outT = new Thread("Output") {

	    public void run() {
		try {
		    outSC.connect(new InetSocketAddress(inSS.getInetAddress(),
			    inSS.getLocalPort()));
		    while (!outSC.finishConnect()) {
		    }
		    coutT = new Conduit(outSC);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	};
	outT.start();
	inT.join();
	outT.join();
    }

    protected void setUpUDP() throws Exception {
	final DatagramChannel inSC = DatagramChannel.open();
	inSC.configureBlocking(false);
	final DatagramSocket inS = inSC.socket();
	inS.setReuseAddress(true);
	// inSS.setSoTimeout(200);
	InetAddress lhIA = InetAddress.getLocalHost();
	inS.bind(new InetSocketAddress(lhIA, 0));
	final SocketAddress inSA = inS.getLocalSocketAddress();
	final DatagramChannel outSC = DatagramChannel.open();
	outSC.configureBlocking(false);
	final DatagramSocket outS = outSC.socket();
	outS.setReuseAddress(true);
	// outS.setSoLinger(false,0);
	outS.setSoTimeout(20);
	outS.bind(new InetSocketAddress(lhIA, 0));
	final SocketAddress outSA = outS.getLocalSocketAddress();
	Thread inT = new Thread() {

	    public void run() {
		try {
		    inSC.connect(outSA);
		    cinU = new Conduit(inSC);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	};
	inT.start();
	Thread outT = new Thread() {

	    public void run() {
		try {
		    outSC.connect(inSA);
		    coutU = new Conduit(outSC);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	};
	outT.start();
	inT.join();
	outT.join();
    }

    protected void tearDown() throws Exception {
	coutT.close();
	coutT = null;
	cinT.close();
	cinT = null;
	coutU.close();
	coutU = null;
	cinU.close();
	cinU = null;
	System.out.flush();
    }

    // The test methods

    /**
	 * Test of isOpen method, of class com.InfoMontage.net.Conduit.
	 *
	 * @param cin
	 * @param cout
	 * @param useUDP
	 */
    public void tIsOpen(Conduit cin, Conduit cout, boolean useUDP) {
	System.out.println("testIsOpen" + (useUDP ? "UDP" : "TCP") + ": ");

	assertTrue("The " + (useUDP ? "UDP" : "TCP")
		+ " in Conduit is not open!", cin.isOpen());
	assertTrue("The " + (useUDP ? "UDP" : "TCP")
		+ " out Conduit is not open!", cout.isOpen());
    }

    /**
	 * Test of close method, of class com.InfoMontage.net.Conduit.
	 *
	 * @param cin
	 * @param cout
	 * @param useUDP
	 * @throws IOException
	 */
    public void tClose(Conduit cin, Conduit cout, boolean useUDP)
	    throws IOException {
	System.out.println("testClose" + (useUDP ? "UDP" : "TCP") + ": ");

	cinT.close();
	assertFalse("Close on " + (useUDP ? "UDP" : "TCP")
		+ " in Conduit failed!", cinT.isOpen());
	coutT.close();
	assertFalse("Close on " + (useUDP ? "UDP" : "TCP")
		+ " out Conduit failed!", coutT.isOpen());
    }

    /**
	 * Test of hasRawInput method, of class com.InfoMontage.net.Conduit.
	 *
	 * @param cin
	 * @param cout
	 * @param useUDP
	 * @throws IOException
	 */
    public void tHasRawInput(Conduit cin, Conduit cout, boolean useUDP)
	    throws IOException {
	System.out.print("testHasRawInput" + (useUDP ? "UDP" : "TCP") + ": ");

	String testString = "testHasRawInput " + (useUDP ? "UDP" : "TCP")
		+ " test";
	char[] ocbuf = testString.toCharArray();
	cout.write(ocbuf, 0, ocbuf.length);
	cout.flush();
	System.out.print("write, ");
	int count = 0, timeout = 30;
	try {
	    while (!cin.hasRawInput() && ++count < timeout) {
		System.out.print(".");
		Thread.sleep(200);
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} finally {
	    System.out.println((cin.hasRawInput()) ? "received" : "timed out");
	    assertTrue("Input not recieved!", cin.hasRawInput());
	}
    }

    /**
	 * Test of hasInput method, of class com.InfoMontage.net.Conduit.
	 *
	 * @param cin
	 * @param cout
	 * @param useUDP
	 * @throws IOException
	 */
    public void tHasInput(Conduit cin, Conduit cout, boolean useUDP)
	    throws IOException {
	System.out.print("testHasInput" + (useUDP ? "UDP" : "TCP") + ": ");

	String testString = "testHasInput " + (useUDP ? "UDP" : "TCP")
		+ " test";
	ByteBuffer ocbuf = ByteBuffer.wrap(testString.getBytes());
	cout.write(ocbuf);
	cout.flush();
	System.out.print("write, ");
	int count = 0, timeout = 30;
	try {
	    while (!cin.hasInput() && ++count < timeout) {
		System.out.print(".");
		Thread.sleep(200);
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} finally {
	    System.out.println((cin.hasInput()) ? "received" : "timed out");
	    assertTrue("Input not recieved!", cin.hasInput());
	}
    }

    /**
	 * Test of Conduit isOpen method with TCP.
	 */
    public void testIsOpenTCP() {
	tIsOpen(cinT, coutT, false);
    }

    /**
	 * Test of Conduit close method with TCP.
	 */
    public void testCloseTCP() throws IOException {
	tClose(cinT, coutT, false);
    }

    /**
	 * Test of Conduit hasRawInput method with TCP.
	 */
    public void testHasRawInputTCP() throws IOException {
	tHasRawInput(cinT, coutT, false);
    }

    /**
	 * Test of Conduit hasInput method with TCP.
	 */
    public void testHasInputTCP() throws IOException {
	tHasInput(cinT, coutT, false);
    }

    /**
	 * Test of Conduit isOpen method with UDP.
	 */
    public void testIsOpenUDP() {
	tIsOpen(cinU, coutU, true);
    }

    /**
	 * Test of Conduit close method with UDP.
	 */
    public void testCloseUDP() throws IOException {
	tClose(cinU, coutU, true);
    }

    /**
	 * Test of Conduit hasRawInput method with UDP.
	 */
    public void testHasRawInputUDP() throws IOException {
	tHasRawInput(cinU, coutU, true);
    }

    /**
	 * Test of Conduit hasInput method with UDP.
	 */
    public void testHasInputUDP() throws IOException {
	tHasInput(cinU, coutU, true);
    }

    /**
	 * Test of 'raw' write and read methods, of class
	 * com.InfoMontage.net.Conduit.
	 *
	 * @param cin
	 * @param cout
	 * @param useUDP
	 * @throws IOException
	 */
    public void tRawWriteRead(Conduit cin, Conduit cout, boolean useUDP)
	    throws IOException {
	System.out.print("testRawWriteRead" + (useUDP ? "UDP" : "TCP") + ": ");

	String testString = "testRawWriteRead " + (useUDP ? "UDP" : "TCP")
		+ " test";
	char[] ocbuf = testString.toCharArray();
	char[] icbuf = new char[ocbuf.length];
	cout.write(ocbuf, 0, ocbuf.length);
	cout.flush();
	System.out.print("write=\"");
	System.out.print(ocbuf);
	System.out.print("\" {" + ocbuf.length + " chars}, ");
	int count = 0, timeout = 60, numRead = 0;
	try {
	    while (!cin.hasRawInput() && ++count < timeout) {
		System.out.print(".");
		Thread.sleep(200);
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	if (cin.hasRawInput()) {
	    System.out.print(icbuf);
	    numRead = cin.read(icbuf, 0, icbuf.length);
	    System.out.print("read=\"");
	    System.out.print(icbuf);
	    System.out.println("\" {" + numRead + " chars}");
	} else {
	    System.out.println("timed out");
	}
	assertTrue("read does not match write!\n" + "write=\""
		+ String.copyValueOf(ocbuf) + "\"\n" + "read=\""
		+ String.copyValueOf(icbuf) + "\"", java.util.Arrays.equals(
		icbuf, ocbuf));
    }

    public void tDefaultPacketFactoryCheckImpl(Conduit cin, Conduit cout) {

	assertTrue("Default PacketFactory for output Conduit is not \""
		+ "PacketFactory\" - it is \""
		+ cout.getPacketFactory().getClass() + "\"", cout
		.getPacketFactory() == PacketFactory.getInstance());
	assertTrue("Default PacketFactory for input Conduit is not \""
		+ "PacketFactory\" - it is \""
		+ cin.getPacketFactory().getClass() + "\"", cin
		.getPacketFactory() == PacketFactory.getInstance());
    }

    public void tChecksumPacketFactoryCheckImpl(Conduit cin, Conduit cout) {

	cout.setPacketFactory(ChecksumPacketFactory.getInstance());
	cin.setPacketFactory(ChecksumPacketFactory.getInstance());

	assertTrue(
		"PacketFactory for output Conduit was not successfully set to \""
			+ "ChecksumPacketFactory\" - it is \""
			+ cout.getPacketFactory().getClass() + "\"", cout
			.getPacketFactory() == ChecksumPacketFactory
			.getInstance());
	assertTrue(
		"PacketFactory for input Conduit was not successfully set to \""
			+ "ChecksumPacketFactory\" - it is \""
			+ cin.getPacketFactory().getClass() + "\"", cin
			.getPacketFactory() == ChecksumPacketFactory
			.getInstance());
    }

    public void testDefaultPacketFactoryTCP() {
	System.out.print("DefaultPacketFactoryTCP: ");

	tDefaultPacketFactoryCheckImpl(cinT, coutT);
    }

    public void testDefaultPacketFactoryUDP() {
	System.out.print("DefaultPacketFactoryUDP: ");

	tDefaultPacketFactoryCheckImpl(cinU, coutU);
    }

    public void testChecksumPacketFactoryTCP() {
	System.out.print("ChecksumPacketFactoryTCP: ");

	tChecksumPacketFactoryCheckImpl(cinT, coutT);
    }

    public void testChecksumPacketFactoryUDP() {
	System.out.print("ChecksumPacketFactoryUDP: ");

	tChecksumPacketFactoryCheckImpl(cinU, coutU);
    }

    /**
	 * Test of write and read methods using Packets, of class
	 * com.InfoMontage.net.Conduit.
	 *
	 * @param wString
	 * @param cin
	 * @param cout
	 * @param useUDP
	 * @throws IOException
	 */
    public void tPacketizedWriteReadImpl(String wString, Conduit cin,
	    Conduit cout, boolean useUDP) throws IOException {

	String testString = wString;
	ByteBuffer ocbuf = ByteBuffer.wrap(testString.getBytes());
	ByteBuffer otcbuf = ByteBuffer.allocate(ocbuf.capacity());
	ByteBuffer icbuf = ByteBuffer.allocate(ocbuf.capacity());
	cout.write(ocbuf);
	// cout.flush();
	ocbuf.rewind();
	System.out.print("write=\"");
	System.out.print(new String(ocbuf.array()));
	System.out.print("\" {" + ocbuf.capacity() + " chars}, ");
	int count = 0, timeout = 60, numRead = 0;
	try {
	    while (numRead == 0 && count < timeout) {
		while (!cin.hasInput() && ++count < timeout) {
		    System.out.print(".");
		    Thread.sleep(200);
		}
		if (cin.hasInput()) {
		    System.out.print("!");
		    numRead = cin.read(icbuf);
		}
		if (cout.hasInput()) {
		    System.out.print("!");
		    if (cout.read(otcbuf) > 0) {
			otcbuf.rewind();
			System.out.print("output read=\"");
			System.out.println(new String(otcbuf.array()));
		    }
		}
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	if (numRead > 0) {
	    icbuf.rewind();
	    System.out.print("read=\"");
	    System.out.print(new String(icbuf.array()));
	    System.out.println("\" {" + numRead + " chars}");
	} else {
	    System.out.println("timed out");
	}
	assertTrue("read does not match write!\n" + "write=\""
		+ com.InfoMontage.util.Buffer.toString(ocbuf) + "\"\n"
		+ "read=\"" + com.InfoMontage.util.Buffer.toString(icbuf)
		+ "\"", icbuf.equals(ocbuf));
    }

    public void tPacketizedMultiWriteReadImpl(String wString, Conduit cin,
	    Conduit cout, boolean useUDP) throws IOException {

	String testString = wString;
	ByteBuffer ocbuf = ByteBuffer.wrap(testString.getBytes());
	ByteBuffer otcbuf = ByteBuffer.allocate(2 * ocbuf.capacity());
	ByteBuffer icbuf = ByteBuffer.allocate(2 * ocbuf.capacity());
	cout.write(ocbuf);
	// cout.flush();
	ocbuf.rewind();
	System.out.print("write #1=\"");
	System.out.print(new String(ocbuf.array()));
	System.out.print("\" {" + ocbuf.capacity() + " chars}, ");
	cout.write(ocbuf);
	// cout.flush();
	ocbuf.rewind();
	System.out.print("write #2=\"");
	System.out.print(new String(ocbuf.array()));
	System.out.print("\" {" + ocbuf.capacity() + " chars}, ");
	int count = 0, timeout = 60, numRead = 0, numBndls = 0;
	try {
	    while (numBndls < 2 && count < timeout) {
		while (!cin.hasInput() && ++count < timeout) {
		    System.out.print(".");
		    Thread.sleep(200);
		}
		if (cin.hasInput()) {
		    System.out.print("!");
		    numRead += cin.read(icbuf);
		    numBndls++;
		}
		if (cout.hasInput()) {
		    System.out.print("!");
		    if (cout.read(otcbuf) > 0) {
			otcbuf.rewind();
			System.out.print("output read=\"");
			System.out.println(new String(otcbuf.array()));
		    }
		}
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	if (numRead > 0) {
	    icbuf.rewind();
	    System.out.print("read=\"");
	    System.out.print(new String(icbuf.array()));
	    System.out.println("\" {" + numRead + " chars}");
	} else {
	    System.out.println("timed out");
	}
	ByteBuffer tbb = ByteBuffer.allocate(icbuf.capacity());
	tbb.put(ocbuf);
	ocbuf.rewind();
	tbb.put(ocbuf).rewind();
	ocbuf.rewind();
	assertTrue("read does not match writes!\n" + "writes=\""
		+ com.InfoMontage.util.Buffer.toString(ocbuf) + " and "
		+ com.InfoMontage.util.Buffer.toString(ocbuf) + "\"\n"
		+ "read=\"" + com.InfoMontage.util.Buffer.toString(icbuf)
		+ "\"", icbuf.equals(tbb));
    }

    public void tPacketizedMultiWriteMultiReadImpl(String wString, Conduit cin, Conduit cout,
	    boolean useUDP) throws IOException {

	String testString = wString;
	ByteBuffer ocbuf = ByteBuffer.wrap(testString.getBytes());
	ByteBuffer otcbuf = ByteBuffer.allocate(ocbuf.capacity());
	ByteBuffer icbuf = ByteBuffer.allocate(ocbuf.capacity());
	cout.write(ocbuf);
	// cout.flush();
	ocbuf.rewind();
	System.out.print("write #1=\"");
	System.out.print(new String(ocbuf.array()));
	System.out.print("\" {" + ocbuf.capacity() + " chars}, ");
	int count = 0, timeout = 60, numRead = 0;
	try {
	    while (numRead == 0 && count < timeout) {
		while (!cin.hasInput() && ++count < timeout) {
		    System.out.print(".");
		    Thread.sleep(200);
		}
		if (cin.hasInput()) {
		    System.out.print("!");
		    numRead = cin.read(icbuf);
		}
		if (cout.hasInput()) {
		    System.out.print("!");
		    if (cout.read(otcbuf) > 0) {
			otcbuf.rewind();
			System.out.print("output read=\"");
			System.out.println(new String(otcbuf.array()));
		    }
		}
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	if (numRead > 0) {
	    icbuf.rewind();
	    System.out.print("read #1=\"");
	    System.out.print(new String(icbuf.array()));
	    System.out.print("\" {" + numRead + " chars}");
	} else {
	    System.out.print("timed out");
	}
	assertTrue("read #1 does not match write #1!\n" + "write=\""
		+ com.InfoMontage.util.Buffer.toString(ocbuf) + "\"\n"
		+ "read=\"" + com.InfoMontage.util.Buffer.toString(icbuf)
		+ "\"", icbuf.equals(ocbuf));
	cout.write(ocbuf);
	// cout.flush();
	ocbuf.rewind();
	System.out.print(" | write #2=\"");
	System.out.print(new String(ocbuf.array()));
	System.out.print("\" {" + ocbuf.capacity() + " chars}, ");
	count = 0;
	timeout = 60;
	numRead = 0;
	try {
	    while (numRead == 0 && count < timeout) {
		while (!cin.hasInput() && ++count < timeout) {
		    System.out.print(".");
		    Thread.sleep(200);
		}
		if (cin.hasInput()) {
		    System.out.print("!");
		    numRead = cin.read(icbuf);
		}
		if (cout.hasInput()) {
		    System.out.print("!");
		    if (cout.read(otcbuf) > 0) {
			otcbuf.rewind();
			System.out.print("output read=\"");
			System.out.println(new String(otcbuf.array()));
		    }
		}
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	if (numRead > 0) {
	    icbuf.rewind();
	    System.out.print("read #2=\"");
	    System.out.print(new String(icbuf.array()));
	    System.out.println("\" {" + numRead + " chars}");
	} else {
	    System.out.println("timed out");
	}
	assertTrue("read #2 does not match write #2!\n" + "write=\""
		+ com.InfoMontage.util.Buffer.toString(ocbuf) + "\"\n"
		+ "read=\"" + com.InfoMontage.util.Buffer.toString(icbuf)
		+ "\"", icbuf.equals(ocbuf));
    }

    /**
	 * Test of write and read methods using Packets, of class
	 * com.InfoMontage.net.Conduit.
	 *
	 * @param cin
	 * @param cout
	 * @param useUDP
	 * @throws IOException
	 */
    public void tPacketizedWriteRead(Conduit cin, Conduit cout, boolean useUDP)
	    throws IOException {
	System.out.print("testPacketizedWriteRead" + (useUDP ? "UDP" : "TCP")
		+ ": ");

	String testString = "testPacketizedWriteRead "
		+ (useUDP ? "UDP" : "TCP") + " test";
	tPacketizedWriteReadImpl(testString, cin, cout, useUDP);
    }

    public void tPacketizedMultiWriteRead(Conduit cin, Conduit cout,
	    boolean useUDP) throws IOException {
	System.out.print("testPacketizedMultiWriteRead"
		+ (useUDP ? "UDP" : "TCP") + ": ");

	String testString = "testPacketizedMultiWriteRead "
		+ (useUDP ? "UDP" : "TCP") + " test";
	tPacketizedMultiWriteReadImpl(testString, cin, cout, useUDP);
    }

    public void tPacketizedMultiWriteMultiRead(Conduit cin, Conduit cout,
	    boolean useUDP) throws IOException {
	System.out.print("testPacketizedMultiWriteMultiRead"
		+ (useUDP ? "UDP" : "TCP") + ": ");

	String testString = "testPacketizedMultiWriteMultiRead "
		+ (useUDP ? "UDP" : "TCP") + " test";
	tPacketizedMultiWriteMultiReadImpl(testString, cin, cout, useUDP);
    }

    public void tPacketizedLargeWriteRead(Conduit cin, Conduit cout,
	    boolean useUDP) throws IOException {
	System.out.print("testPacketizedLargeWriteRead"
		+ (useUDP ? "UDP" : "TCP") + ": ");

	StringBuffer testString = new StringBuffer(
		"testPacketizedLargeWriteRead " + (useUDP ? "UDP" : "TCP")
			+ " test");
	testString.append(testString).append(testString).append(testString)
		.append(testString).append(testString).append(testString);
	tPacketizedWriteReadImpl(testString.toString(), cin, cout, useUDP);
    }

    public void tPacketizedVeryLargeWriteRead(Conduit cin, Conduit cout,
	    boolean useUDP) throws IOException {
	System.out.print("testPacketizedLargeWriteRead"
		+ (useUDP ? "UDP" : "TCP") + ": ");

	StringBuffer testString = new StringBuffer(
		"testPacketizedLargeWriteRead " + (useUDP ? "UDP" : "TCP")
			+ " test");
	testString.append(testString).append(testString).append(testString)
		.append(testString).append(testString).append(testString)
		.append(testString).append(testString).append(testString)
		.append(testString).append(testString).append(testString);
	tPacketizedWriteReadImpl(testString.toString(), cin, cout, useUDP);
    }

    public void tChecksumPacketizedWriteRead(Conduit cin, Conduit cout,
	    boolean useUDP) throws IOException {
	System.out.print("testChecksumPacketizedWriteRead"
		+ (useUDP ? "UDP" : "TCP") + ": ");

	cout.setPacketFactory(ChecksumPacketFactory.getInstance());
	cin.setPacketFactory(ChecksumPacketFactory.getInstance());

	String testString = "testChecksumPacketizedWriteRead "
		+ (useUDP ? "UDP" : "TCP") + " test";
	tPacketizedWriteReadImpl(testString, cin, cout, useUDP);
    }

    public void tChecksumPacketizedMultiWriteRead(Conduit cin, Conduit cout,
	    boolean useUDP) throws IOException {
	System.out.print("testChecksumPacketizedMultiWriteRead"
		+ (useUDP ? "UDP" : "TCP") + ": ");

	cout.setPacketFactory(ChecksumPacketFactory.getInstance());
	cin.setPacketFactory(ChecksumPacketFactory.getInstance());

	String testString = "testChecksumPacketizedMultiWriteRead "
		+ (useUDP ? "UDP" : "TCP") + " test";
	tPacketizedMultiWriteReadImpl(testString, cin, cout, useUDP);
    }

    public void tChecksumPacketizedMultiWriteMultiRead(Conduit cin,
	    Conduit cout, boolean useUDP) throws IOException {
	System.out.print("testChecksumPacketizedMultiWriteMultiRead"
		+ (useUDP ? "UDP" : "TCP") + ": ");

	cout.setPacketFactory(ChecksumPacketFactory.getInstance());
	cin.setPacketFactory(ChecksumPacketFactory.getInstance());

	String testString = "testChecksumPacketizedMultiWriteMultiRead "
		+ (useUDP ? "UDP" : "TCP") + " test";
	tPacketizedMultiWriteMultiReadImpl(testString, cin, cout, useUDP);
    }

    /**
	 * Test of 'raw' write and read methods, of class
	 * com.InfoMontage.net.Conduit.
	 */
    public void testRawWriteReadTCP() throws IOException {
	tRawWriteRead(cinT, coutT, false);
    }

    /**
	 * Test of write and read methods using Packets, of class
	 * com.InfoMontage.net.Conduit.
	 */
    public void testPacketizedWriteReadTCP() throws IOException {
	tPacketizedWriteRead(cinT, coutT, false);
    }

    public void testPacketizedMultiWriteReadTCP() throws IOException {
	tPacketizedMultiWriteRead(cinT, coutT, false);
    }

    public void testPacketizedMultiWriteMultiReadTCP() throws IOException {
	tPacketizedMultiWriteMultiRead(cinT, coutT, false);
    }

    public void testPacketizedLargeWriteReadTCP() throws IOException {
	tPacketizedLargeWriteRead(cinT, coutT, false);
    }

    public void testPacketizedVeryLargeWriteReadTCP() throws IOException {
	tPacketizedVeryLargeWriteRead(cinT, coutT, false);
    }

    public void testChecksumPacketizedWriteReadTCP() throws IOException {
	tChecksumPacketizedWriteRead(cinT, coutT, false);
    }

    public void testChecksumPacketizedMultiWriteReadTCP() throws IOException {
	tChecksumPacketizedMultiWriteRead(cinT, coutT, false);
    }

    public void testChecksumPacketizedMultiWriteMultiReadTCP()
	    throws IOException {
	tChecksumPacketizedMultiWriteMultiRead(cinT, coutT, false);
    }

    /**
	 * Test of 'raw' write and read methods, of class
	 * com.InfoMontage.net.Conduit.
	 */
    public void testRawWriteReadUDP() throws IOException {
	tRawWriteRead(cinU, coutU, true);
    }

    /**
	 * Test of write and read methods using Packets, of class
	 * com.InfoMontage.net.Conduit.
	 */
    public void testPacketizedWriteReadUDP() throws IOException {
	tPacketizedWriteRead(cinU, coutU, true);
    }

    public void testPacketizedMultiWriteReadUDP() throws IOException {
	tPacketizedMultiWriteRead(cinU, coutU, true);
    }

    public void testPacketizedMultiWriteMultiReadUDP() throws IOException {
	tPacketizedMultiWriteMultiRead(cinU, coutU, true);
    }

    public void testPacketizedLargeWriteReadUDP() throws IOException {
	tPacketizedLargeWriteRead(cinU, coutU, true);
    }

    public void testPacketizedVeryLargeWriteReadUDP() throws IOException {
	tPacketizedVeryLargeWriteRead(cinU, coutU, true);
    }

    public void testChecksumPacketizedWriteReadUDP() throws IOException {
	tChecksumPacketizedWriteRead(cinU, coutU, true);
    }

    public void testChecksumPacketizedMultiWriteReadUDP() throws IOException {
	tChecksumPacketizedMultiWriteRead(cinU, coutU, true);
    }

    public void testChecksumPacketizedMultiWriteMultiReadUDP()
	    throws IOException {
	tChecksumPacketizedMultiWriteMultiRead(cinU, coutU, true);
    }

}