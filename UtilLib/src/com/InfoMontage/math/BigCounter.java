/*
 * BigCounter.java
 *
 * Created on October 31, 2003, 11:52 AM
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

package com.InfoMontage.math;

import java.math.BigInteger;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public final class BigCounter {
    
    private volatile BigInteger collectedVal = BigInteger.ZERO;
    private volatile long lowVal = 0;
    
    private final static long MIN_MAX_BUFFER = 2;
    private final static long MAX_LOW_VAL = Long.MAX_VALUE-MIN_MAX_BUFFER;
    private final static long MIN_LOW_VAL = Long.MIN_VALUE+MIN_MAX_BUFFER;
    
    /** Creates a new instance of BigCounter */
    public BigCounter() {
    }
    
    /** Creates a new instance of BigCounter with a starting value */
    public BigCounter(long val) {
        lowVal=val;
    }
    
    /** Creates a new instance of BigCounter with a starting value */
    public BigCounter(BigInteger val) {
        collectedVal=val;
    }
    
    public synchronized boolean equals(Object obj) {
        boolean isEqual=false;
        Object compareObj=obj;
        if (BigCounter.class.isAssignableFrom(obj.getClass()))
            compareObj=((BigCounter)obj).get();
        isEqual=this.get().equals(compareObj);
        return isEqual;
    }
    
    public synchronized int hashCode() {
        return this.get().hashCode();
    }
    
    public synchronized String toString() {
        return this.get().toString();
    }
    
    public synchronized void add(long val) {
        if (val<0)
            subtract(-val);
        else
        if ((lowVal>(-MIN_MAX_BUFFER))
        && (((MAX_LOW_VAL-lowVal) <= val) || ((MAX_LOW_VAL-val) <= MIN_MAX_BUFFER))) {
            collectedVal=collectedVal
            .add(BigInteger.valueOf(lowVal))
            .add(BigInteger.valueOf(val));
            lowVal=0;
        }
        else
            lowVal+=val;
    }
    
    public synchronized void subtract(long val) {
        if (val<0)
            add(-val);
        else
        if ((lowVal<MIN_MAX_BUFFER)
        && (((MIN_LOW_VAL-lowVal) >= -val) || ((MIN_LOW_VAL+val) >= -MIN_MAX_BUFFER))) {
            collectedVal=collectedVal
            .add(BigInteger.valueOf(lowVal))
            .subtract(BigInteger.valueOf(val));
            lowVal=0;
        }
        else
            lowVal-=val;
    }
    
    public synchronized void add(BigInteger val) {
        collectedVal=collectedVal
        .add(BigInteger.valueOf(lowVal))
        .add(val);
        lowVal=0;
    }
    
    public synchronized void subtract(BigInteger val) {
        collectedVal=collectedVal
        .add(BigInteger.valueOf(lowVal))
        .subtract(val);
        lowVal=0;
    }
    
    public synchronized void clear() {
        collectedVal=BigInteger.ZERO;
        lowVal=0;
    }
    
    public synchronized BigInteger get() {
        return collectedVal.add(BigInteger.valueOf(lowVal));
    }
    
    /** Set this BigCounter to a value */
    public synchronized void set(long val) {
        collectedVal=BigInteger.ZERO;
        lowVal=val;
    }
    
    /** Set this BigCounter to a value */
    public synchronized void set(BigInteger val) {
        collectedVal=val;
        lowVal=0;
    }
    
}
