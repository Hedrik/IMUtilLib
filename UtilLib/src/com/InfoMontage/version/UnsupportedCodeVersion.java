/*
 * UnsupportedCodeVersion.java
 * 
 * Created on August 9, 2003, 9:26 PM
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

package com.InfoMontage.version;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class UnsupportedCodeVersion
    extends Exception
{

    /** Creates a new instance of UnsupportedCodeVersion */
    public UnsupportedCodeVersion() {
        super();
    }

    public UnsupportedCodeVersion(String message) {
        super(message);
    }

    public UnsupportedCodeVersion(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedCodeVersion(Throwable cause) {
        super(cause);
    }

}