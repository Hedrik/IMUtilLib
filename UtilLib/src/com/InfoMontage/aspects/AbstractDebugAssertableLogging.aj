/*
 * AbstractDebugAssertableLogging.aj
 * 
 * Created on May 28, 2005
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

package com.InfoMontage.aspects;

import com.InfoMontage.util.AssertableLogger;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public abstract aspect AbstractDebugAssertableLogging extends AbstractAssertableLogging pertypewithin(*..*) {
	
    before(): isInLoggableStaticInitializor() {
        assert (aLog.entering());
    }

    after(): isInLoggableStaticInitializor() {
        assert (aLog.exiting());
    }
	
    before(Object o): isAnLoggableConstructor(o) {
        assert (aLog.entering());
    }

    after(Object o): isAnLoggableConstructor(o) {
        assert (aLog.exiting());
    }
	
    before(Object o): isAnLoggableMethod(o) {
        assert (aLog.entering());
    }

    after(Object o): isAnLoggableMethod(o) {
        assert (aLog.exiting());
    }


    pointcut lockingObject(Object o): call(* synchronized(Object+)) && args(o);
    
    before(Object o): isInLoggableMethod() && lockingObject(o) {
        assert (aLog.gettingLock(o));
    }

}