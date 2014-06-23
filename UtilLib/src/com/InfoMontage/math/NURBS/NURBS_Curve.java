/*
 * NURBS_Curve.java
 *
 * Created on May 19, 2002, 1:31 PM
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

package com.InfoMontage.math.NURBS;

import com.InfoMontage.math.*;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public class NURBS_Curve {
    
    public short order;
    public WeightedPoint[] points;
    public double[] knots;
    private boolean preCalcValid=false;
    private double[][] preCalcBasis;
    private Point[][] preCalcPoints;
    
    /** Creates a new instance of NURBS_Curve */
    public NURBS_Curve() {
        order = 0;
        points = new WeightedPoint[0];
        knots = new double[0];
    }
    
    public NURBS_Curve(final short np) {
        order = 1;
        points = new WeightedPoint[np];
        knots = new double[np];
        if (np>1)
            for (int i=0;i<np;i++) {
                points[i]=new WeightedPoint();
                knots[i]=(double)i/(double)(np-1);
            }
        else {
            points[0]=new WeightedPoint();
            knots[0]=0;
        }
    }
    
    public NURBS_Curve(final WeightedPoint[] pa) {
        order = 1;
        points = clonePointArray(pa);
        knots = new double[pa.length];
        if (pa.length>1)
            for (int i=0;i<pa.length;i++)
                knots[i]=(double)i/(double)(pa.length-1);
        else
            knots[0]=0;
    }
    
    public NURBS_Curve(final WeightedPoint[] pa, double[] da) {
        order = 1;
        clonePointAndDoubleArrays(points, pa, knots, da);
    }
    
    public NURBS_Curve(final NURBS_Curve n) {
        order = n.order;
        points = clonePointArray(n.points);
        knots = cloneDoubleArray(n.knots);
    }
    
    public NURBS_Curve(final short np, final short o) {
        order = o;
        points = new WeightedPoint[np];
        knots = new double[np+order+order-2];
        if (np>1)
            for (int i=0;i<np;i++) {
                points[i]=new WeightedPoint();
                knots[i]=(double)i/(double)(np-1);
            }
        else {
            points[0]=new WeightedPoint();
            for (int i=0;i<order;i++)
                knots[i]=0;
        }
    }
    
    public NURBS_Curve(final WeightedPoint[] pa, final short o) {
        order = o;
        points = clonePointArray(pa);
        knots = new double[pa.length+order+order-2];
        if (pa.length>1) {
            int i;
            for (i=0;i<order-1;i++)
                knots[i]=0;
            for (i=0;i<pa.length;i++)
                knots[i+order-1]=(double)i/(double)(pa.length-1);
            for (i=pa.length+order;i<knots.length;i++)
                knots[i]=1;
        }
        else
            knots[0]=0;
    }
    
    private double[] newStdKnotArray(int numPts, int o) {
        if (o<2) throw new IllegalArgumentException("Attempt to create an order "+o+" knot array!");
        if (numPts<2) throw new IllegalArgumentException("Attempt to create a knot array for "+numPts+" control points!");
        int i;
        double[] ra=new double[numPts+o+o-2];
        for (i=0;i<o-1;i++)
            ra[i]=0;
        for (i=0;i<numPts-1;i++)
            ra[i+o-1]=(double)i/(double)(numPts-1);
        for (i=numPts+o-2;i<ra.length;i++)
            ra[i]=1;
        return ra;
    }
    
    public int determineIndex(final double t) {
        double tt=(t<0)?0:((t>1)?1:t);
        int i=0;
        while ((i<knots.length-order) && (knots[i+1]<=tt))
            i++;
        // knots[i+1]>t or t==knots[knots.length-1]
        return i;
    }
    
    public Point valueAt(final double t) { // use NURBS
        double tt=(t<0)?0:((t>1)?1:t);
        int i=determineIndex(tt);
        Point rp=new Point(points[i].values);
        if (tt<1)
            rp.add(dAt(tt).times(tt-knots[i])); // actually prevP+d(PrevP*(t-prevt))
        return rp;
    }
    
    public Point dAt(final double t) { // use NURBS
        int i=determineIndex(t);
        int i2=(t<1)?i+1:i;
        Point rp=new Point(
        points[i2].values[0]-points[i].values[0]
        ,points[i2].values[1]-points[i].values[1]);
        return rp;
    }
    
    public Point ddAt(final double t) { // use NURBS
        Point rp=new Point(0,0);
        return rp;
    }
    
    public Point valueAt(final double t, final int d) {
        // p=degree of curve=order-1
        // n=knots.length-1
        // N(i,0,u)=((u>=knots[i])&&(u<knots[i+1]))?1:0
        // NOTE: if (!(knots[i]<=u<knots[i+1])) then N(i,p-1,u)=0
        // THUS: if (!knots[i+1]<=u<knots[i+2])) then N(i+1,p-1,u)=0
        // N(i,p,u)=(((u-knots[i])/(knots[i+p]-knots[i]))*N(i,p-1,u))
        //          +(((knots[i+p+1]-u)/(knots[i+p+1]-knots[i+1]))*N(i+1,p-1,u))
        // THEREFOR: if ((!(knots[i]<=u<knots[i+1]))&&(!(knots[i+1]<=u<knots[i+2])))
        //           then N(i,p,u)=0
        // ALSO: if (knots[i]<=u<knots[i+1])&&(!knots[i+1]<=u<knots[i+2]))
        //       then N(i,p,u)=((u-knots[i])/(knots[i+p+]-knots[i]))*N(i,p-1,u)
        // AND: if (!(knots[i]<=u<knots[i+1]))&&(knots[i+1]<=u<knots[i+2])
        //      then N(i,p,u)=((knots[i+p+1]-u)/(knots[i+p+1]-knots[i+1]))*N(i+1,p-1,u)
        // P(i,0)=points[i]
        // P(i,d)=((p-d+1)/(knots[i+p+1]-knots[i+d]))*(P(i+1,d-1)-P(i,d-1))
        // C(t)=sum(i=0,n)[N(i,p,t)*points[i]]
        // AND: C(t,d)=sum(i=0,n-d)[N(i,p-d,t)*P(i,d)]
        if (d<0) throw new IllegalArgumentException("Attempt to determine a negative derivative ("+d+")!");
        Point rp;
        int i;
        int p=order-1;
        Point tPid;
        double tPm;
        if ((t<0)||(t>1)||(d>=order))
            rp=new Point(points[0].values.length);
        else {
            i=determineIndex(t);
            tPid=new Point(points[i].values); // for d==0
            // for d==1
            Point tPi=(Point)tPid.clone();
            Point tPii=new Point(points[i+1].values);
            Point tPiii=new Point(points[i+2].values);
            Point tPiid;
            for (int ii=1;ii<=d;ii++) {
                tPid=tPii.minus(tPi).times((p+1-ii)/(knots[i+p+1]-knots[i+ii]));
                if (ii<d) {
                    tPiid=tPiii.minus(tPid).times((p+1-ii)/(knots[i+1+p+1]-knots[i+1+ii]));
                    //tPii=tPiid.minus(tP
                }
            }
            rp=tPid;
        }
        return rp;
    }
    
    public void displayIn(final javax.swing.JComponent c, final java.awt.Color col) {
        double pixelSpacing=(double)1;
        if (points.length>1) {
            java.awt.Graphics g=c.getGraphics();
            g.setColor(col);
            Point sp=points[0];
            Point ep;
            int sx=new Double(sp.values[0]).intValue();
            int sy=new Double(sp.values[1]).intValue();
            int ex,ey;
            if ((points.length>2) || (order>1)) {
                double t=(pixelSpacing)/(dAt(0).length()+ddAt(0).length());
                while (t<1) {
                    ep=valueAt(t);
                    ex=new Double(ep.values[0]).intValue();
                    ey=new Double(ep.values[1]).intValue();
                    g.drawLine(sx,sy, ex,ey);
                    sp=ep; // not needed
                    sx=ex;
                    sy=ey;
                    t+=(pixelSpacing)/(dAt(t).length()+ddAt(t).length());
                }
            }
            ep=points[points.length-1];
            ex=new Double(ep.values[0]).intValue();
            ey=new Double(ep.values[1]).intValue();
            g.drawLine(sx,sy, ex,ey);
        }
    }
    
    private WeightedPoint[] clonePointArray(final WeightedPoint[] pa) {
        WeightedPoint[] rpa=new WeightedPoint[pa.length];
        for (int i=0;i<pa.length;i++)
            rpa[i]=(WeightedPoint)pa[i].clone();
        return rpa;
    }
    
    private double[] cloneDoubleArray(final double[] da) {
        double[] rpa=new double[da.length];
        for (int i=0;i<da.length;i++)
            rpa[i]=da[i];
        return rpa;
    }
    
    private void clonePointAndDoubleArrays(
    WeightedPoint[] ps, final WeightedPoint[] pa, double[] ts, final double[] da) {
        int i=(pa.length>da.length)?pa.length:da.length;
        ps=new WeightedPoint[i];
        ts=new double[i];
        for (int ii=0;ii<pa.length;ii++)
            ps[ii]=(WeightedPoint)pa[ii].clone();
        for (int ii=pa.length;ii<i;ii++)
            ps[ii]=new WeightedPoint();
        for (int ii=0;ii<da.length;ii++)
            ts[ii]=da[ii];
        for (int ii=da.length;ii<i;ii++)
            ts[ii]=1;
    }
    
    public String toString() {
        StringBuffer s=new StringBuffer("NURBS_Curve[order="+order+"; points={");
        for (int i=0;i<points.length;i++) {
            if (i>0)
                s.append(", ");
            s.append(points[i]+"@"+knots[i]);
        }
        s.append("}; ]");
        return s.toString();
    }
    
}
