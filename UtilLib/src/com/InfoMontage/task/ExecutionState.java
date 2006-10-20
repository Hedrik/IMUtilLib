/**
 * ExecutionState.java
 *
 * Created Aug 21, 2006
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

package com.InfoMontage.task;

import com.InfoMontage.version.CodeVersion;

/**
 * ExecutionState
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 * 
 */
final class ExecutionState {

    /**
         * Implementation file version. By convention, for use with
         * {@link com.InfoMontage.util.CodeVersion} methods, implementation
         * versions are kept in a public static field named
         * <code>implCodeVersion</code>.
         * 
         * @see com.InfoMontage.util.CodeVersion
         *      com.InfoMontage.version.CodeVersion
         *      com.InfoMontage.version.GenericCodeVersion
         */
    public static final CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    private final ExecutableTask myTask;

    /**
         * 
         */
    ExecutionState(ExecutableTask t) {
	myTask = t;
    }

    public boolean isComplete() {
	return !myTask.isProcessing();
    }

    public float percentComplete() {
	return myTask.percentComplete();
    }

    public void waitForCompletion() {
	myTask.join();
    }

    public Object[] getResults() {
	this.waitForCompletion();
	return myTask.getResults();
    }
}
