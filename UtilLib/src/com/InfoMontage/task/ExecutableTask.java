/*
 * ExecutableTask.java
 * 
 * Created on May 9, 2004, 1:14 PM
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

import com.InfoMontage.util.AssertableLogger;
import com.InfoMontage.util.BooleanState;
import com.InfoMontage.version.CodeVersion;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
class ExecutableTask {

    /**
         * Logger for this class
         */
    private static final AssertableLogger log = new AssertableLogger(
	    ExecutableTask.class.getName());

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

    /**
         * Comment for <code>params</code>
         */
    protected volatile Object[] params = null;

    protected volatile boolean paramsSet = false;

    /**
         * Comment for <code>processing</code>
         */
    private volatile BooleanState processing = new BooleanState(false);

    private volatile BooleanState joiner = new BooleanState(false);

    private final Task myTask;

    /**
         * 
         */
    protected ExecutableTask(Task t) {
	myTask = t;
    };

    /*
         * @return true if currently processing the {@link Task}, false
         * otherwise.
         */
    boolean isProcessing() {
	return processing.getState();
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#percentComplete()
         */
    float percentComplete() {
	// default is 0% unless processing
	return isProcessing() ? myTask.percentComplete() : 0f;
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#processTask()
         */
    synchronized void processTask() throws IllegalStateException {
	synchronized (joiner) {
	    if (this.paramsSet) {
		processing.setState(true);
		myTask.doTask();
		processing.setState(false);
		this.clearTaskParameters();
	    } else {
		IllegalStateException e = new IllegalStateException(
			"Attempt to process a Task that has not had it's parameters set first!");
		assert (log.throwing(e));
		throw e;
	    }
	    joiner.setState(false);
	    joiner.notifyAll();
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#setTaskParameters(java.lang.Object[])
         */
    synchronized void setTaskParameters(Object[] pa)
	    throws IllegalArgumentException, IllegalStateException {
	Exception e = myTask.validateParameters(pa);
	if (null != e) {
	    throw (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt" + " to set a Task parameter to an illegal value!")
		    .initCause(e);
	} else if (this.isProcessing()) {
	    throw new IllegalStateException(
		    "Task parameters being set while still processing!");
	} else {
	    this.params = pa;
	    myTask.setParameters(pa);
	    this.paramsSet = true;
	    joiner.setState(false);
	}
    }

    synchronized void join() {
	synchronized (joiner) {
	    joiner.setState(true);
	    while (this.isProcessing()) {
		try {
		    this.joiner.wait();
		} catch (InterruptedException e) {
		    throw (RuntimeException) new RuntimeException()
			    .initCause(e);
		}
	    }
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#clearTaskParameters()
         */
    synchronized void clearTaskParameters() throws IllegalStateException {
	if (this.processing.getState()) {
	    throw new IllegalStateException(
		    "Attempt to clear a ExecutableTask's parameters"
			    + " while ExecutableTask is still processing!");
	}
	this.params = null;
	myTask.clearParameters();
	this.paramsSet = false;
	joiner.setState(false);
    }

    /**
         * @return Results of task execution, or null if there are none.
         * @throws IllegalStateException
         *                 if called while the {@link Task}is currently
         *                 processing (as defined by the
         *                 {@link Task#isProcessing method}).
         */
    Object[] getResults() throws IllegalStateException {
	if (this.paramsSet) {
	    throw new IllegalStateException(
		    "Attempt to get an ExecutableTask's results"
			    + " while ExecutableTask's parameters are still set!");
	}
	return myTask.getResults();
    }

}