/*
 * Point.java
 *
 * Created on May 19, 2002, 12:09 PM
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
public class Point implements Cloneable {
    
    private static final int defaultNumAxes=2;
    protected volatile int i;
    public double[] values;
    
    /** Initializes a new instance of Point */
    private void initPoint(int nAxes) {
        values=new double[nAxes];
        for (i=0;i<nAxes;i++) values[i]=0;
    }
    
    /** Creates a new instance of Point */
    public Point() {
        initPoint(defaultNumAxes);
    }
    
    public Point(final int n) {
        initPoint(n);
    }
    
    public Point(final double x) {
        initPoint(1);
        values[0]=x;
    }
    
    public Point(final double x, final double y) {
        initPoint(2);
        values[0]=x;
        values[1]=y;
    }
    
    public Point(final double x, final double y, final double z) {
        initPoint(3);
        values[0]=x;
        values[1]=y;
        values[2]=z;
    }
    
    public Point(final double[] v) {
        initPoint(v.length);
        for (i=0;i<v.length;i++) values[i]=v[i];
    }
    
    public void add(final double a) {
        for (i=0;i<values.length;i++) values[i]+=a;
    }
    
    public void add(final Point p) {
        for (i=0;i<values.length;i++) values[i]+=p.values[i];
    }
    
    public static Point plus(final Point p, final double a) {
        Point rp=(Point)p.clone();
        rp.add(a);
        return rp;
    }
        
    public Point plus(final double a) {
        Point rp=(Point)this.clone();
        rp.add(a);
        return rp;
    }
    
    public Point plus(final Point p) {
        Point rp=(Point)this.clone();
        rp.add(p);
        return rp;
    }
    
    public void subtract(final double s) {
        add(-s);
    }
    
    public void subtract(final Point p) {
        for (i=0;i<values.length;i++) values[i]-=p.values[i];
    }
    
    public static Point minus(final Point p, final double s) {
        return plus(p,-s);
    }
    
    public Point minus(final double s) {
        return plus(-s);
    }
    
    public Point minus(final Point p) {
        Point rp=(Point)this.clone();
        rp.subtract(p);
        return rp;
    }
    
    public void multiply(final double m) {
        for (i=0;i<values.length;i++) values[i]*=m;
    }
    
    public static Point times(final Point p, final double m) {
        Point rp=(Point)p.clone();
        rp.multiply(m);
        return rp;
    }
    
    public Point times(final double m) {
        Point rp=(Point)this.clone();
        rp.multiply(m);
        return rp;
    }
    
    public void divideBy(final double d) {
        for (i=0;i<values.length;i++) values[i]/=d;
    }
    
    public static Point divide(final Point p, final double d) {
        Point rp=(Point)p.clone();
        rp.divideBy(d);
        return rp;
    }
    
    public Point divide(final double d) {
        Point rp=(Point)this.clone();
        rp.divideBy(d);
        return rp;
    }
    
    public double length() {
        double l=0;
        for (i=0;i<values.length;i++)
            l+=values[i]*values[i];
        return java.lang.Math.sqrt(l);
    }
    
    public String toString() {
        StringBuffer s=new StringBuffer("Point"+values.length+"D[");
        for (i=0;i<values.length;i++) {
            if (i>0) s.append(",");
            s.append(values[i]);
        }
        s.append("]");
        return s.toString();
    }
    
    public Object clone() {
        return new Point(this.values);
    }
}
