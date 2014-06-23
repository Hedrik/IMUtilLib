/*
 * GenericCodeVersion_TestClass.java
 * 
 * Created on Feb 16, 2005
 */

/*
 * 
 * Part of the "Client/Server Helper" library, a project from Information
 * Montage. Copyright (C) 2004 Richard A. Mead
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

package com.InfoMontage.version;

import junit.framework.TestCase;


/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class GenericCodeVersion_TestClass
    extends TestCase
{

    static int maj = 1, min = 2, maint = 3, pat = 4;
    static char bld = 'b';
    static String iv1, iv2, iv3, iv4;
    static StringBuffer fv = new StringBuffer();
    static StringBuffer v1b = new StringBuffer();
    static StringBuffer v2b = new StringBuffer();
    static StringBuffer v3b = new StringBuffer();
    static StringBuffer v1 = new StringBuffer();
    static StringBuffer v2 = new StringBuffer();
    static StringBuffer v3 = new StringBuffer();
    static StringBuffer v4 = new StringBuffer();
    static GenericCodeVersion tgcv;

    static {
        iv1 = ".";
        iv2 = "";
        iv3 = "1.2.3.4.5";
        iv4 = "x";
        v1.append(String.valueOf(maj));
        v1b.append(v1).append(bld);
        v2.append(v1).append(".").append(String.valueOf(min));
        v2b.append(v2).append(bld);
        v3.append(v2).append(".").append(String.valueOf(maint));
        v3b.append(v3).append(bld);
        v4.append(v3).append(".").append(String.valueOf(pat));
        fv.append(v4).append(bld);
        tgcv = (GenericCodeVersion) GenericCodeVersion.asCodeVersion(fv
            .toString());
    }

    public static void main(String[] args) {
	//junit.swingui.TestRunner.run(GenericCodeVersion_TestClass.class);
	junit.textui.TestRunner.run(GenericCodeVersion_TestClass.class);
        //TestSuite suite= new TestSuite(GenericCodeVersion_TestClass.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for GenericCodeVersion_TestClass.
     * 
     * @param arg0
     */
    public GenericCodeVersion_TestClass(String arg0) {
        super(arg0);
    }


    public final void testGetMajor() {
        assertEquals(tgcv.getMajor(), maj);
    }

    public final void testGetMinor() {
        assertEquals(tgcv.getMinor(), min);
    }

    public final void testGetMaintenance() {
        assertEquals(tgcv.getMaintenance(), maint);
    }

    public final void testGetPatch() {
        assertEquals(tgcv.getPatch(), pat);
    }

    public final void testGetBuild() {
        assertEquals(tgcv.getBuild(), bld);
    }

    private final void validateStandardCodeVersion(CodeVersion t) {
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(), pat);
        assertEquals(t.getBuild(), bld);
    }

    /*
     * Class under test for void GenericCodeVersion(String)
     */
    public final void testGenericCodeVersionString() {
        GenericCodeVersion t = new GenericCodeVersion(fv.toString());
        validateStandardCodeVersion(t);
        t = new GenericCodeVersion(v1.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = new GenericCodeVersion(v1b.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        t = new GenericCodeVersion(v2.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = new GenericCodeVersion(v2b.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        t = new GenericCodeVersion(v3.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = new GenericCodeVersion(v3b.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        t = new GenericCodeVersion(v4.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(), pat);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
    }

    /*
     * Class under test for void GenericCodeVersion(CodeVersion)
     */
    public final void testGenericCodeVersionCodeVersion() {
        GenericCodeVersion tt = new GenericCodeVersion(fv.toString());
        GenericCodeVersion t = new GenericCodeVersion(tt);
        validateStandardCodeVersion(tt);
        tt = new GenericCodeVersion(v1.toString());
        t = new GenericCodeVersion(tt);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        tt = new GenericCodeVersion(v1b.toString());
        t = new GenericCodeVersion(tt);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        tt = new GenericCodeVersion(v2.toString());
        t = new GenericCodeVersion(tt);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        tt = new GenericCodeVersion(v2b.toString());
        t = new GenericCodeVersion(tt);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        tt = new GenericCodeVersion(v3.toString());
        t = new GenericCodeVersion(tt);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        tt = new GenericCodeVersion(v3b.toString());
        t = new GenericCodeVersion(tt);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        tt = new GenericCodeVersion(v4.toString());
        t = new GenericCodeVersion(tt);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(), pat);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, int, int, char)
     */
    public final void testGenericCodeVersionintintintintchar() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, maint, pat,
            bld);
        validateStandardCodeVersion(t);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, int, int,
     * String)
     */
    public final void testGenericCodeVersionintintintintString() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, maint, pat,
            String.valueOf(bld));
        validateStandardCodeVersion(t);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, int, int)
     */
    public final void testGenericCodeVersionintintintint() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, maint, pat);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(), pat);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, int, char)
     */
    public final void testGenericCodeVersionintintintchar() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, maint, bld);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, int, String)
     */
    public final void testGenericCodeVersionintintintString() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, maint,
            String.valueOf(bld));
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, int)
     */
    public final void testGenericCodeVersionintintint() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, maint);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, char)
     */
    public final void testGenericCodeVersionintintchar() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, bld);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int, String)
     */
    public final void testGenericCodeVersionintintString() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min, String
            .valueOf(bld));
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
    }

    /*
     * Class under test for void GenericCodeVersion(int, int)
     */
    public final void testGenericCodeVersionintint() {
        GenericCodeVersion t = new GenericCodeVersion(maj, min);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
    }

    /*
     * Class under test for void GenericCodeVersion(int, char)
     */
    public final void testGenericCodeVersionintchar() {
        GenericCodeVersion t = new GenericCodeVersion(maj, bld);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
    }

    /*
     * Class under test for void GenericCodeVersion(int, String)
     */
    public final void testGenericCodeVersionintString() {
        GenericCodeVersion t = new GenericCodeVersion(maj, String
            .valueOf(bld));
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
    }

    /*
     * Class under test for void GenericCodeVersion(int)
     */
    public final void testGenericCodeVersionint() {
        GenericCodeVersion t = new GenericCodeVersion(maj);
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
    }

    public final void testCodeVersionFromCVSRevisionString() {
        GenericCodeVersion t;
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + maj
                + "." + min + "." + maint + "." + pat + " $");
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(), pat);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + maj
                + "." + min + "." + maint + " $");
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + maj
                + "." + min + " $");
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + maj
                + " $");
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: $");
        assertNull(t);
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + iv1
                + " $");
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + iv2
                + " $");
        assertNull(t);
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + iv3
                + " $");
        assertNull(t);
        t = (GenericCodeVersion) GenericCodeVersion
            .codeVersionFromCVSRevisionString("$" + "Revision: " + iv4
                + " $");
        assertNull(t);
    }

    /*
     * Class under test for CodeVersion asCodeVersion(String)
     */
    public final void testAsCodeVersionString() {
        CodeVersion t;
        t = GenericCodeVersion.asCodeVersion(fv.toString());
        validateStandardCodeVersion(t);
        t = GenericCodeVersion.asCodeVersion(v1.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = GenericCodeVersion.asCodeVersion(v1b.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(),
            AbstractCodeVersion.DEFAULT_MINOR_VERSION);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        t = GenericCodeVersion.asCodeVersion(v2.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = GenericCodeVersion.asCodeVersion(v2b.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(),
            AbstractCodeVersion.DEFAULT_MAINTENANCE_VERSION);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        t = GenericCodeVersion.asCodeVersion(v3.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = GenericCodeVersion.asCodeVersion(v3b.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(),
            AbstractCodeVersion.DEFAULT_PATCH_VERSION);
        assertEquals(t.getBuild(), bld);
        t = GenericCodeVersion.asCodeVersion(v4.toString());
        assertEquals(t.getMajor(), maj);
        assertEquals(t.getMinor(), min);
        assertEquals(t.getMaintenance(), maint);
        assertEquals(t.getPatch(), pat);
        assertEquals(t.getBuild(),
            AbstractCodeVersion.DEFAULT_BUILD_VERSION);
        t = GenericCodeVersion.asCodeVersion(iv1.toString());
        assertNull(t);
        t = GenericCodeVersion.asCodeVersion(iv2.toString());
        assertNull(t);
        t = GenericCodeVersion.asCodeVersion(iv3.toString());
        assertNull(t);
        t = GenericCodeVersion.asCodeVersion(iv4.toString());
        assertNull(t);
    }

    /*
     * Class under test for String asString()
     */
    public final void testAsString() {
        assertTrue(fv.toString().equals(tgcv.asString()));
    }

    /*
     * Class under test for String asString(CodeVersion)
     */
    public final void testAsStringCodeVersion() {
        assertTrue(fv.toString().equals(GenericCodeVersion.asString(tgcv)));
    }

    /*
     * Class under test for String toString()
     */
    public final void testToString() {
        assertTrue(fv.toString().equals(tgcv.toString()));
    }

    /*
     * Class under test for boolean equals(Object)
     */
    public final void testEqualsObject() {
        boolean correct = false;
        CodeVersion t;
        t = GenericCodeVersion.asCodeVersion(fv.toString());
        assertTrue(t.equals(tgcv));
        assertTrue(tgcv.equals(t));
        t = GenericCodeVersion.asCodeVersion(v1.toString());
        assertFalse(t.equals(tgcv));
        assertFalse(tgcv.equals(t));
        t = GenericCodeVersion.asCodeVersion(v1b.toString());
        assertFalse(t.equals(tgcv));
        assertFalse(tgcv.equals(t));
        t = GenericCodeVersion.asCodeVersion(v2.toString());
        assertFalse(t.equals(tgcv));
        assertFalse(tgcv.equals(t));
        t = GenericCodeVersion.asCodeVersion(v2b.toString());
        assertFalse(t.equals(tgcv));
        assertFalse(tgcv.equals(t));
        t = GenericCodeVersion.asCodeVersion(v3.toString());
        assertFalse(t.equals(tgcv));
        assertFalse(tgcv.equals(t));
        t = GenericCodeVersion.asCodeVersion(v3b.toString());
        assertFalse(t.equals(tgcv));
        assertFalse(tgcv.equals(t));
        t = GenericCodeVersion.asCodeVersion(v4.toString());
        assertFalse(t.equals(tgcv));
        assertFalse(tgcv.equals(t));
        assertFalse(t.equals(null));
        try {
            t.equals(new Integer(0));
        } catch (ClassCastException e) {
            correct = true;
        }
        if (!correct)
            fail();
        else
            correct = false;
    }

    /*
     * Class under test for int compareTo(CodeVersion)
     */
    public final void testCompareToCodeVersion() {
        CodeVersion tt, t;
        t = GenericCodeVersion.asCodeVersion(fv.toString());
        assertEquals(t.compareTo(tgcv), 0);
        assertEquals(tgcv.compareTo(t), 0);
        t = GenericCodeVersion.asCodeVersion(v1.toString());
        assertEquals(t.compareTo(tgcv), -1);
        assertEquals(tgcv.compareTo(t), 1);
        t = GenericCodeVersion.asCodeVersion(v1b.toString());
        assertEquals(t.compareTo(tgcv), -1);
        assertEquals(tgcv.compareTo(t), 1);
        t = GenericCodeVersion.asCodeVersion(v2.toString());
        assertEquals(t.compareTo(tgcv), -1);
        assertEquals(tgcv.compareTo(t), 1);
        t = GenericCodeVersion.asCodeVersion(v2b.toString());
        assertEquals(t.compareTo(tgcv), -1);
        assertEquals(tgcv.compareTo(t), 1);
        t = GenericCodeVersion.asCodeVersion(v3.toString());
        assertEquals(t.compareTo(tgcv), -1);
        assertEquals(tgcv.compareTo(t), 1);
        t = GenericCodeVersion.asCodeVersion(v3b.toString());
        assertEquals(t.compareTo(tgcv), -1);
        assertEquals(tgcv.compareTo(t), 1);
        t = GenericCodeVersion.asCodeVersion(v4.toString());
        assertEquals(t.compareTo(tgcv), -1);
        assertEquals(tgcv.compareTo(t), 1);
        tt = GenericCodeVersion.asCodeVersion(v1.toString());
        assertEquals(tt.compareTo(t), -1);
        assertEquals(t.compareTo(tt), 1);
        tt = GenericCodeVersion.asCodeVersion(v1b.toString());
        assertEquals(tt.compareTo(t), -1);
        assertEquals(t.compareTo(tt), 1);
        tt = GenericCodeVersion.asCodeVersion(v2.toString());
        assertEquals(tt.compareTo(t), -1);
        assertEquals(t.compareTo(tt), 1);
        tt = GenericCodeVersion.asCodeVersion(v2b.toString());
        assertEquals(tt.compareTo(t), -1);
        assertEquals(t.compareTo(tt), 1);
        tt = GenericCodeVersion.asCodeVersion(v3.toString());
        assertEquals(tt.compareTo(t), -1);
        assertEquals(t.compareTo(tt), 1);
        tt = GenericCodeVersion.asCodeVersion(v3b.toString());
        assertEquals(tt.compareTo(t), -1);
        assertEquals(t.compareTo(tt), 1);
        tt = GenericCodeVersion.asCodeVersion(v4.toString());
        assertEquals(tt.compareTo(t), 0);
        assertEquals(t.compareTo(tt), 0);
        assertEquals(tt.compareTo(GenericCodeVersion.NULL_CODE_VERSION), 1);
        assertEquals(GenericCodeVersion.NULL_CODE_VERSION.compareTo(tt), -1);
    }

    /*
     * Class under test for int compareTo(Object)
     */
    public final void testCompareToObject() {
        boolean correct = false;
        CodeVersion t = GenericCodeVersion.asCodeVersion(fv.toString());
        assertEquals(t.compareTo((Object) tgcv), 0);
        assertEquals(tgcv.compareTo((Object) t), 0);
        try {
            t.compareTo(null);
        } catch (NullPointerException e) {
            correct = true;
        }
        if (!correct)
            fail();
        else
            correct = false;
        try {
            t.compareTo(new Integer(0));
        } catch (ClassCastException e) {
            correct = true;
        }
        if (!correct)
            fail();
        else
            correct = false;
    }

}