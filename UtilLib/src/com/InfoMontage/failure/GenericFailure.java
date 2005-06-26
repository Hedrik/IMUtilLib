/*
 * GenericFailure.java
 *
 * Created on July 28, 2003, 1:12 AM
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
import com.InfoMontage.failure.AbstractFailure;

/**
 *
 * @author Richard A. Mead <BR> Information Montage
 */
public final class GenericFailure extends AbstractFailure {
    
    public static CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
    .codeVersionFromCVSRevisionString("$Revision$");
    
    static Exception thisFailureClassExceptionType = DEFAULT_EXCEPTION;
    
    GenericFailure(String reason) {
        super(reason);
    }
        
}
