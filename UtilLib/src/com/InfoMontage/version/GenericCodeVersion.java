/*
 * GenericCodeVersion.java
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

import java.io.StreamTokenizer;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public final class GenericCodeVersion
    extends AbstractCodeVersion
{

    public static final CodeVersion implCodeVersion = new GenericCodeVersion(
        "1.3a");

    public static final CodeVersion NULL_CODE_VERSION = new GenericCodeVersion();

    private static final long serialVersionUID = 6412827940260729099L;

    /** Creates a new instance of GenericVersion */
    private GenericCodeVersion() {
        super();
    }

    public GenericCodeVersion(final String version) {
        super(version);
    }

    public GenericCodeVersion(final CodeVersion version)
        throws AssertionError
    {
        super((AbstractCodeVersion) version);
        if (version == NULL_CODE_VERSION) { throw new java.lang.AssertionError(
            new IllegalArgumentException(
                "Attempt to create a duplicate nullVersion object")); }
    }

    public GenericCodeVersion(final int major, final int minor,
        final int maint, final int patch, final char build)
    {
        super(major, minor, maint, patch, build);
    }

    public GenericCodeVersion(final int major, final int minor,
        final int maint, final int patch, final String build)
    {
        super(major, minor, maint, patch, build);
    }

    public GenericCodeVersion(final int major, final int minor,
        final int maint, final int patch)
    {
        super(major, minor, maint, patch, DEFAULT_BUILD_VERSION);
    }

    public GenericCodeVersion(final int major, final int minor,
        final int maint, final char build)
    {
        super(major, minor, maint, DEFAULT_PATCH_VERSION, build);
    }

    public GenericCodeVersion(int major, int minor, int maint, String build)
    {
        super(major, minor, maint, DEFAULT_PATCH_VERSION, build);
    }

    public GenericCodeVersion(int major, int minor, int maint) {
        super(major, minor, maint, DEFAULT_PATCH_VERSION,
            DEFAULT_BUILD_VERSION);
    }

    public GenericCodeVersion(int major, int minor, char build) {
        super(major, minor, DEFAULT_MAINTENANCE_VERSION,
            DEFAULT_PATCH_VERSION, build);
    }

    public GenericCodeVersion(int major, int minor, String build) {
        super(major, minor, DEFAULT_MAINTENANCE_VERSION,
            DEFAULT_PATCH_VERSION, build);
    }

    public GenericCodeVersion(int major, int minor) {
        super(major, minor, DEFAULT_MAINTENANCE_VERSION,
            DEFAULT_PATCH_VERSION, DEFAULT_BUILD_VERSION);
    }

    public GenericCodeVersion(int major, char build) {
        super(major, DEFAULT_MINOR_VERSION, DEFAULT_MAINTENANCE_VERSION,
            DEFAULT_PATCH_VERSION, build);
    }

    public GenericCodeVersion(int major, String build) {
        super(major, DEFAULT_MINOR_VERSION, DEFAULT_MAINTENANCE_VERSION,
            DEFAULT_PATCH_VERSION, build);
    }

    public GenericCodeVersion(int major) {
        super(major, DEFAULT_MINOR_VERSION, DEFAULT_MAINTENANCE_VERSION,
            DEFAULT_PATCH_VERSION, DEFAULT_BUILD_VERSION);
    }

    public CodeVersion asCodeVersion() {
        return (CodeVersion) this.clone();
    }

    public static CodeVersion asCodeVersion(final String version) {
        CodeVersion v = null;
        int[] ver = new int[] {
            DEFAULT_MAJOR_VERSION, DEFAULT_MINOR_VERSION,
            DEFAULT_MAINTENANCE_VERSION, DEFAULT_PATCH_VERSION };
        char buil = DEFAULT_BUILD_VERSION;
        try {
            java.io.Reader r = new java.io.StringReader(version);
            StreamTokenizer st = new StreamTokenizer(r);
            st.resetSyntax();
            //st.parseNumbers(); // doesn't work 'cause it parses as decimal
            // even though we set '.' and '-' to ordinary characters!
            //st.ordinaryChar('.');
            //st.ordinaryChar('-');
            st.eolIsSignificant(true);
            st.wordChars('0', '9');
            boolean valid = true;
            boolean done = false;
            boolean mustEnd = false;
            int i = 0;
            while ( (i++ < 4) && (valid) && (!done)) {
                // first token must be an integer, and only numbers are words
                if (st.nextToken() != StreamTokenizer.TT_WORD) {
                    if ( (st.ttype == StreamTokenizer.TT_EOF)
                        || (st.ttype == StreamTokenizer.TT_EOL))
                    {
                        done = true;
                        if (!mustEnd)
                            valid = false;
                    } else
                        valid = false;
                } else {
                    try {
                        ver[i - 1] = Integer.valueOf(st.sval).intValue();
                    } catch (NumberFormatException e) {
                        valid = false;
                    }
                    // are we done?
                    if ( (st.nextToken() == StreamTokenizer.TT_EOF)
                        || (st.ttype == StreamTokenizer.TT_EOL))
                    {
                        done = true;
                    } else {
                        // next token must be a single lowercase alpha char or
                        // '.'
                        if ( (mustEnd)
                            || ( (st.ttype != '.') && ( (st.ttype < 'a') || (st.ttype > 'z'))))
                        {
                            valid = false;
                        } else {
                            if (st.ttype != '.') {
                                buil = (char) st.ttype;
                                mustEnd = true;
                            }
                        }
                    }
                }
            }
            if ( (ver[0] == DEFAULT_MAJOR_VERSION)
                && (ver[1] == DEFAULT_MINOR_VERSION)
                && (ver[2] == DEFAULT_MAINTENANCE_VERSION)
                && (ver[3] == DEFAULT_PATCH_VERSION)
                && (buil == DEFAULT_BUILD_VERSION))
                valid = false;
            if ( (i < 6) && valid) {
                v = new GenericCodeVersion(ver[0], ver[1], ver[2], ver[3],
                    buil);
            } else {
                v = null;
            }
        } catch (java.io.IOException e) {
            v = null;
        }
        return v;
    }

    protected Object clone() {
        super.clone();
        CodeVersion v = null;
        if (NULL_CODE_VERSION.equals(this))
            v = NULL_CODE_VERSION;
        if (null != this) // can't happen, right?
            v = new GenericCodeVersion(this);
        return v;
    }

    protected AbstractCodeVersion asAbstractCodeVersion(String version) {
        return (AbstractCodeVersion) asCodeVersion(version);
    }

    // If we just deserialized a NULL_CODE_VERSION then we must return the
    // singleton constant NULL_CODE_VERSION, otherwise return a new instance
    // to avoid possible hacking of an external reference to our private
    // fields.
    protected Object readResolve() throws java.io.ObjectStreamException {
        GenericCodeVersion gcv;
        if (NULL_CODE_VERSION.equals(this))
            gcv = (GenericCodeVersion) NULL_CODE_VERSION;
        else
            gcv = new GenericCodeVersion(this);
        return gcv;
    }

}