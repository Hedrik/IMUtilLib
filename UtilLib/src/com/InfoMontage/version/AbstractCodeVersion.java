/*
 * AbstractCodeVersion.java
 * 
 * Created on July 27, 2003, 10:11 AM
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

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public abstract class AbstractCodeVersion
    extends java.lang.Object
    implements CodeVersion
{

    public static CodeVersion implCodeVersion = new GenericCodeVersion(
        "1.0.0.0b");

    protected final static int DEFAULT_MAJOR_VERSION = 0;
    protected final static int DEFAULT_MINOR_VERSION = 0;
    protected final static int DEFAULT_MAINTENANCE_VERSION = 0;
    protected final static int DEFAULT_PATCH_VERSION = 0;
    protected final static char DEFAULT_BUILD_VERSION = 'a';

    private int major;
    private int minor;
    private int maint;
    private int patch;
    private char build;

    private static final long serialVersionUID = 8514234446406222744L;

    /** Creates a new instance of AbstractVersion - only available via super() */
    protected AbstractCodeVersion() {
        major = DEFAULT_MAJOR_VERSION;
        minor = DEFAULT_MINOR_VERSION;
        maint = DEFAULT_MAINTENANCE_VERSION;
        patch = DEFAULT_PATCH_VERSION;
        build = ' ';
    }

    protected AbstractCodeVersion(final String version) {
        int maj = DEFAULT_MAJOR_VERSION;
        int min = DEFAULT_MINOR_VERSION;
        int main = DEFAULT_MAINTENANCE_VERSION;
        int pat = DEFAULT_PATCH_VERSION;
        char buil = DEFAULT_BUILD_VERSION;
        if (version == "") {
            throw new java.lang.AssertionError(
                new IllegalArgumentException(
                    "Attempt to create a CodeVersion object with an empty String as the version"));
        } else {
            AbstractCodeVersion v = asAbstractCodeVersion(version);
            if (v == null) {
                throw new java.lang.AssertionError(
                    new IllegalArgumentException(
                        "Attempt to create a CodeVersion object with an invalid String as the version\n"
                            + "String parrameter was: \"" + version + "\""));
            } else {
                maj = v.major;
                min = v.minor;
                main = v.maint;
                pat = v.patch;
                buil = v.build;
            }
        }
        major = maj;
        minor = min;
        maint = main;
        patch = pat;
        build = buil;
    }

    protected AbstractCodeVersion(final AbstractCodeVersion version) {
        if (version == null) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a CodeVersion object from null")); }
        major = version.getMajor();
        minor = version.getMinor();
        maint = version.getMaintenance();
        patch = version.getPatch();
        build = version.getBuild();
    }

    private void validateVersion(final int major, final int minor,
        final int maint, final int patch, final char build)
    {
        if (major < 0) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a CodeVersion object with a major number less than zero\n"
                    + "Major parameter was: \"" + major + "\"")); }
        if (minor < 0) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a CodeVersion object with a minor number less than zero\n"
                    + "Minor parameter was: \"" + major + "\"")); }
        if (maint < 0) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a CodeVersion object with a maintenance number less than zero\n"
                    + "Maintenance parameter was: \"" + major + "\"")); }
        if (patch < 0) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a CodeVersion object with a patch number less than zero\n"
                    + "Patch parameter was: \"" + major + "\"")); }
        if ( (build < 'a' || build > 'z') && build != DEFAULT_BUILD_VERSION) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a CodeVersion object with a char other than a lowercase "
                    + "letter as the build version\nBuild parameter was: \'"
                    + build + "\'")); }
    }

    private char validateVersion(final int major, final int minor,
        final int maint, final int patch, final String build)
    {
        if (build.length() > 1) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a CodeVersion object with a String of more than one "
                    + "character as the build version\nBuild parameter was: \'"
                    + build + "\'")); }
        char[] bc = new char[1];
        build.getChars(0, 1, bc, 0);
        validateVersion(major, minor, maint, patch, bc[0]);
        return bc[0];
    }

    public AbstractCodeVersion(final int major, final int minor,
        final int maint, final int patch, final char build)
    {
        validateVersion(major, minor, maint, patch, build);
        this.major = major;
        this.minor = minor;
        this.maint = maint;
        this.patch = patch;
        this.build = build;
    }

    public AbstractCodeVersion(final int major, final int minor,
        final int maint, final int patch, final String build)
    {
        char bc = validateVersion(major, minor, maint, patch, build);
        this.major = major;
        this.minor = minor;
        this.maint = maint;
        this.patch = patch;
        this.build = bc;
    }

    public String asString() {
        return toString();
    }

    public static final String asString(final CodeVersion version) {
        return version.asString();
    }

    protected abstract AbstractCodeVersion asAbstractCodeVersion(
        final String version);

    public final int getMajor() {
        return major;
    }

    public final int getMinor() {
        return minor;
    }

    public final int getMaintenance() {
        return maint;
    }

    public final int getPatch() {
        return patch;
    }

    public final char getBuild() {
        return build;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(major).append(".").append(minor).append(".").append(maint)
            .append(".").append(patch).append(build);
        return s.toString();
    }

    protected Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (CloneNotSupportedException e) {
            // Can ignore - we'll never get here since class is Cloneable
        }
        return o;
    }

    public boolean equals(final Object obj) {
        boolean isEq = false;
        if (obj != null) {
            CodeVersion v = (CodeVersion) obj;
            if ( (v.getMajor() == major) && (v.getMinor() == minor)
                && (v.getMaintenance() == maint) && (v.getPatch() == patch)
                && (v.getBuild() == build))
                isEq = true;
        }
        return isEq;
    }

    public int hashCode() {
        return ( (maint << 3) + (patch << 2) + (minor << 1) + (major) + (Character
            .toString(build).hashCode() << 4));
    }

    protected void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException,
        UnsupportedCodeVersion
    {
        int maj = in.readInt();
        int min = in.readInt();
        int main = in.readInt();
        int pat = in.readInt();
        char buil = in.readChar();
        try {
            validateVersion(maj, min, maint, pat, buil);
        } catch (AssertionError e) {
            throw (java.io.InvalidObjectException) new java.io.InvalidObjectException(
                "Serialization failure: serialized "
                    + "AbstractCodeVersion version '" + maj + "." + min
                    + "." + main + "." + pat + "" + buil + "' is invalid!")
                .initCause(e);
        }
        boolean compat = true;
        if (maj > AbstractCodeVersion.implCodeVersion.getMajor())
            compat = false;
        else if (maj == AbstractCodeVersion.implCodeVersion.getMajor())
            if (min > AbstractCodeVersion.implCodeVersion.getMinor())
                compat = false;
            else if (min == AbstractCodeVersion.implCodeVersion.getMinor())
                if (main > AbstractCodeVersion.implCodeVersion
                    .getMaintenance())
                    compat = false;
                else if (main == AbstractCodeVersion.implCodeVersion
                    .getMaintenance())
                    if (pat > AbstractCodeVersion.implCodeVersion
                        .getPatch())
                        compat = false;
                    else if (pat == AbstractCodeVersion.implCodeVersion
                        .getPatch())
                        if (buil > AbstractCodeVersion.implCodeVersion
                            .getBuild())
                            compat = false;
        if (!compat)
            throw new UnsupportedCodeVersion(
                "Serialization failure: serialized "
                    + "AbstractCodeVersion version '"
                    + maj
                    + "."
                    + min
                    + "."
                    + main
                    + "."
                    + pat
                    + ""
                    + buil
                    + "' greater than this AbstractCodeVersion implementation's version '"
                    + AbstractCodeVersion.implCodeVersion);
        maj = in.readInt();
        min = in.readInt();
        main = in.readInt();
        pat = in.readInt();
        buil = in.readChar();
        try {
            // this test allows for the NULL_CODE_VERSION to be serialized
            if (! ( (maj == DEFAULT_MAJOR_VERSION)
                && (min == DEFAULT_MINOR_VERSION)
                && (main == DEFAULT_MAINTENANCE_VERSION)
                && (pat == DEFAULT_PATCH_VERSION) && (buil == ' ')))
                validateVersion(maj, min, maint, pat, buil);
        } catch (AssertionError e) {
            throw (java.io.InvalidObjectException) new java.io.InvalidObjectException(
                "Serialization failure: serialized " + "CodeVersion '"
                    + maj + "." + min + "." + main + "." + pat + "" + buil
                    + "' is invalid!").initCause(e);
        }
        major = maj;
        minor = min;
        maint = main;
        patch = pat;
        build = buil;
    }

    protected void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException
    {
        out.writeInt(AbstractCodeVersion.implCodeVersion.getMajor());
        out.writeInt(AbstractCodeVersion.implCodeVersion.getMinor());
        out.writeInt(AbstractCodeVersion.implCodeVersion.getMaintenance());
        out.writeInt(AbstractCodeVersion.implCodeVersion.getPatch());
        out.writeChar(AbstractCodeVersion.implCodeVersion.getBuild());
        out.writeInt(getMajor());
        out.writeInt(getMinor());
        out.writeInt(getMaintenance());
        out.writeInt(getPatch());
        out.writeChar(getBuild());
    }

    // The readResolve method MUST create a new CodeVersion object from the
    // one
    // just deserialized in order to protect the class from hacking in a
    // public
    // reference to the private version fields.
    protected abstract Object readResolve()
        throws java.io.ObjectStreamException;

    public int compareTo(CodeVersion obj) {
        int rv = 0;
        if (major < obj.getMajor())
            rv = -1;
        else if (major > obj.getMajor())
            rv = 1;
        else if (minor < obj.getMinor())
            rv = -1;
        else if (minor > obj.getMinor())
            rv = 1;
        else if (maint < obj.getMaintenance())
            rv = -1;
        else if (maint > obj.getMaintenance())
            rv = 1;
        else if (patch < obj.getPatch())
            rv = -1;
        else if (patch > obj.getPatch())
            rv = 1;
        else if (build < obj.getBuild())
            rv = -1;
        else if (build > obj.getBuild())
            rv = 1;
        return rv;
    }

    public int compareTo(Object obj) {
        return compareTo((CodeVersion) obj);
    }

}