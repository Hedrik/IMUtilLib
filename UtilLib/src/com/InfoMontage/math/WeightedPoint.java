/*
 * WeightedPoint.java
 *
 * Created on May 19, 2002, 1:34 PM
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

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class WeightedPoint extends com.InfoMontage.math.Point {
    
    public double w;
    
    /** Creates a new instance of WeightedPoint */
    public WeightedPoint() {
        super();
        w=1;
    }
    
    public WeightedPoint(final int n) {
        super(n);
        w=1;
    }
    
    public WeightedPoint(final double x) {
        super(x);
        w=1;
    }
    
    public WeightedPoint(final double x, final double y) {
        super(x,y);
        w=1;
    }
    
    public WeightedPoint(final double x, final double y, final double z) {
        super(x,y,z);
        w=1;
    }
    
    public WeightedPoint(final double x, final double y, final double z, final double wt) {
        super(x,y,z);
        w=wt;
    }
    
    public WeightedPoint(final double[] v) {
        super(v);
        w=1;
    }
    
    public WeightedPoint(final double[] v, final double wt) {
        super(v);
        w=wt;
    }
    
    public String toString() {
        StringBuffer s=new StringBuffer("Point"+values.length+"D+w[{");
        for (i=0;i<values.length;i++) {
            if (i>0) s.append(",");
            s.append(values[i]);
        }
        s.append("},w="+w+"]");
        return s.toString();
    }
    
    public Object clone() {
        return new WeightedPoint(this.values,this.w);
    }
}
