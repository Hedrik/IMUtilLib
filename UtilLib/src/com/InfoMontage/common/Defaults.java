/*
 * Defaults.java
 * 
 * Created on Aug 28, 2004
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

package com.InfoMontage.common;

import java.nio.charset.Charset;

import com.InfoMontage.util.AssertableLogger;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class Defaults {

    public static com.InfoMontage.version.CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    public static AssertableLogger dbg;

    static {
	assert ((dbg = new AssertableLogger("com.InfoMontage")) != null);
    }

    public final static Charset DEFAULT_CHARSET = Charset
	    .forName(new java.io.InputStreamReader(System.in).getEncoding());

    protected Defaults() {
    }

    public static final AssertableLogger getDebugLogger() {
	return dbg;
    }

    public static final AssertableLogger dbg() {
	return dbg;
    }

    public static final void setDebugLogger(AssertableLogger logger) {
	assert (logger != null);
	dbg = logger;
    }

    public final static int DEFAULT_TASK_EXECUTOR_POOL_RETAINED_EXECUTOR_THREADS = 100;

    public final static int DEFAULT_TASK_EXECUTOR_POOL_MAXIMUM_EXECUTOR_THREADS = 250;

    public final static int DEFAULT_TASK_EXECUTOR_THREAD_STACK_SIZE = 0;

    public final static int DEFAULT_TASK_QUEUE_START_CAPACITY = 10;

    public final static int DEFAULT_TASK_QUEUE_CAPACITY_INCREMENT = 3;

    public final static int DEFAULT_TASK_QUEUE_MAXIMUM_CAPACITY = 40;

}