/*
 * AbstractAssertableLogging.aj
 * 
 * Created on June 5, 2005
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

import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.InfoMontage.util.AssertableLogger;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
//public abstract aspect AbstractAssertableLogging pertypewithin(*..* && !(java..* || javax..* || sun..* || junit..*+ || com.InfoMontage.aspects.AbstractAssertableLogging+ || com.InfoMontage.util.AssertableLogger+ || org.aspectj..*)) {
public abstract aspect AbstractAssertableLogging pertypewithin(*..* && !(com.InfoMontage.aspects..*+ || com.InfoMontage.util.AssertableLogger+)) {

	abstract pointcut isInConcreteLoggableClass();
	abstract pointcut isAnConcreteLoggableClass(Object o);

    pointcut isInAssertableLogger(): within(com.InfoMontage.util.AssertableLogger+);
    pointcut isAnAssertableLogger(Object o): this(o) && isInAssertableLogger();

	pointcut isInAssertableLogging(): within(com.InfoMontage.aspects.AbstractAssertableLogging+);
	pointcut isAnAssertableLogging(Object o): this(o) && isInAssertableLogging();


	pointcut isInJUnit(): within(junit..*+);
	pointcut isAnJUnit(Object o): this(o) && isInJUnit();

	pointcut isAnInterface(Object o): this(o) && if(o.getClass().isInterface());
	pointcut isNotAnInterface(Object o): this(o) && !if(o.getClass().isInterface());

    pointcut isInLoggableClass(): isInConcreteLoggableClass() && !(isInAssertableLogger() || isInAssertableLogging() || isInJUnit());
    pointcut isAnLoggableClass(Object o): this(o) && isInLoggableClass() && !if(o.getClass().isInterface());

    pointcut callToAnAssertableLogger(): call(* com.InfoMontage.util.AssertableLogger+.*(..));

    // there is no instantiated(ing) object context for the
    // staticinitilization pointcut
    // pointcut isAnLoggableStaticInitializor(Object o): isAnLoggableClass(o)
    // && staticinitialization(*);
    pointcut isInLoggableStaticInitializor(): isInLoggableClass() && staticinitialization(*);
    
    // there is no instantiated(ing) object context for the preinitialization
    // pointcut
    // pointcut isAnLoggablePreinitialization(Object o): isAnLoggableClass(o)
    // && preinitialization(new(..));
    pointcut isInLoggablePreinitialization(): isInLoggableClass() && (preinitialization(new(..)) || preinitialization(new()));

    pointcut isInLoggableInitialization(): isInLoggableClass() && (initialization(new(..)) || initialization(new()));
    pointcut isAnLoggableInitialization(Object o): isAnLoggableClass(o) && isInLoggableInitialization();

    pointcut isInLoggableConstructor(): isInLoggableClass() && (execution(new(..)) || execution(new()));
    pointcut isAnLoggableConstructor(Object o): isAnLoggableClass(o) && isInLoggableConstructor();

    pointcut isInLoggableMethod(): isInLoggableClass() && execution(* *(..));
    pointcut isAnLoggableMethod(Object o): isAnLoggableClass(o) && isInLoggableMethod();

    /**
     * {@link java.util.logging.Logger} for the aspect.
     */
    static public AssertableLogger aLog = null;

    before(): isInLoggableStaticInitializor() {
        if (null==aLog) {
        	aLog = new AssertableLogger(thisJoinPointStaticPart.getSignature().getDeclaringType().getName());
        }
    }

}