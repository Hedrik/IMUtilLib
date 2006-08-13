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

    private static int[] romanNumbers = { 1000, 900, 500, 400, 100, 90, 50, 40,
	    10, 9, 5, 4, 1 };

    private static String[] romanLetters = { "M", "CM", "D", "CD", "C", "XC",
	    "L", "XL", "X", "IX", "V", "IV", "I" };

    /** Cannot create an instance of Math - static utility functions only */
    private Math() {
    }

    public static long modLong(final BigCounter bc) {
	return modLong(bc.get());
    }

    public static long modLong(final BigInteger bi) {
	return (bi.mod(MAX_LONG_PLUS_ONE)).longValue();
    }

    /**
         * Return the standard Roman numeral representation of an integer.
         * 
         * @param num
         * @return the standard Roman numeral representation of the integer num.
         */
    public static String intToRoman(final int num) {
	StringBuffer roman = new StringBuffer(); // The roman numeral.
	int n = num; // N represents the part of num that still has
	// to be converted to Roman numeral representation.
	for (int i = 0; i < romanNumbers.length; i++) {
	    while (n >= romanNumbers[i]) {
		roman.append(romanLetters[i]);
		n -= romanNumbers[i];
	    }
	}
	return roman.toString();
    }
}