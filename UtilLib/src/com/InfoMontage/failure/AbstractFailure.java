/*
 * AbstractFailure.java
 *
 * Created on July 28, 2003, 10:21 PM
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

package com.InfoMontage.failure;

import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public abstract class AbstractFailure extends Exception implements Failure {
    
    public static CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
    .codeVersionFromCVSRevisionString("$Revision$");
    
    protected String failureReason = null;
    
    protected static Exception thisFailureClassExceptionType = DEFAULT_EXCEPTION;
    
    /** Creates a new instance of AbstractFailure */
    protected AbstractFailure(String reason) {
        failureReason=reason;
    }
        
    public String getReason() {
        return failureReason;
    }

    public void throwException() throws Exception {
        this.throwException(null, null);
    }
    
    public void throwException(String extra) throws Exception {
        this.throwException(null, extra);
    }
    
    public void throwException(Exception cause) throws Exception {
        this.throwException(cause, null);
    }
    
    public void throwException(Exception cause, String extra) throws Exception {
        StringBuffer r=new StringBuffer();
        if ((extra==null) || ("".equals(extra)) || (extra.equals(failureReason)))
            r.append(failureReason);
        else
            if ((failureReason==null) || ("".equals(failureReason)))
                r.append(extra);
            else
                r.append(failureReason).append("\n").append(extra);
        Exception t=((Exception)thisFailureClassExceptionType.getClass()
        .getConstructor(new Class[] {String.class})
        .newInstance(new Object[] {r.toString()}));
        if (cause!=null)
            t.initCause(cause);
        throw t;
    }
    
    public String toString() {
        return "["+this.getClass().getName()+", Reason='"+failureReason+"']";
    }
    
}
