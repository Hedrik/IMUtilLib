/*
 * Math.java
 * 
 * Created on November 15, 2003, 2:42 PM
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

package com.InfoMontage.util;

import java.math.BigInteger;
import com.InfoMontage.math.BigCounter;

/**
 * 
 * @author Richard
 */
public final class Math {

    public static final BigInteger MAX_LONG_PLUS_ONE = BigInteger.valueOf(
        Long.MAX_VALUE).add(BigInteger.ONE);

    /** Cannot create an instance of Math - static utility functions only */
    private Math() {}

    public static final long modLong(BigCounter bc) {
        return modLong(bc.get());
    }

    public static final long modLong(BigInteger bi) {
        return (bi.mod(MAX_LONG_PLUS_ONE)).longValue();
    }

}