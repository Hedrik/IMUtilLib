/*
 * TaskExecutor.java
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
 * NOTE: made public solely for purposes of javadoc type resolution.
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public final class TaskExecutor extends Thread {

    /**
         * Logger for this class
         */
    private static final AssertableLogger log = new AssertableLogger(
	    TaskExecutor.class.getName());

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

    private final BooleanState running = new BooleanState(false);

    private final BooleanState validExecutor = new BooleanState(false);

    private final TaskExecutorPool myFactory;

    private final BooleanState waiting = new BooleanState(false);

    private volatile ExecutableTask myTask = null;

    /** Creates a new instance of a TaskExecutor - package access only */
    TaskExecutor(TaskExecutorPool factory, ThreadGroup group, int stackSize) {
	super(group, null, "Task processor", stackSize);
	assert (log.entering("com.InfoMontage.task.TaskExecutor",
		"TaskExecutor(TaskExecutorPool factory = " + factory
			+ ", ThreadGroup group = " + group
			+ ", int stackSize = " + stackSize + ")",
		"start of method"));

	if (null == factory) {
	    IllegalArgumentException e = (IllegalArgumentException) new IllegalArgumentException(
		    "Attempt to create a TaskExecutor with a null TaskExecutorPool!")
		    .initCause(new NullPointerException());
	    assert (log.throwing(e));
	    throw e;
	}
	myFactory = factory;

	assert (log.exiting("com.InfoMontage.task.TaskExecutor",
		"TaskExecutor(TaskExecutorPool factory = " + factory
			+ ", ThreadGroup group = " + group
			+ ", int stackSize = " + stackSize + ")",
		"end of method"));
    }

    synchronized void validateTaskThread() {
	assert (log.finer("Validating TaskExecutor."));
	validExecutor.setState(true);
	assert (log.finer("TaskExecutor Validated."));
    }

    synchronized ExecutionState executeTask(ExecutableTask t)
	    throws IllegalStateException, IllegalMonitorStateException {

	ExecutionState retVal = null;

	assert (log.entering("com.InfoMontage.task.TaskExecutor",
		"executeTask(ExecutableTask t = " + t + ")", "start of method"));

	if (!this.running.getState()) {
	    IllegalStateException e = new IllegalStateException(
		    "Attempt to use a TaskExecutor that"
			    + " has not been started!");
	    assert (log.throwing(e));
	    throw e;
	}
	if (!waiting.getState()) {
	    IllegalStateException e = new IllegalStateException(
		    "Attempt to use a TaskExecutor that"
			    + " is already executing an ExecutableTask!");
	    assert (log.throwing(e));
	    throw e;
	}
	if (!validExecutor.getState()) {
	    IllegalStateException e = new IllegalStateException(
		    "Attempt to use a TaskExecutor that is still a member of the"
			    + " TaskExecutorPool's TaskExecutor pool!");
	    assert (log.throwing(e));
	    throw e;
	}

	retVal = new ExecutionState(t);
	myTask = t;

	assert (log.gettingLock(validExecutor));
	synchronized (validExecutor) {
	    assert (log.gotLock(validExecutor));
	    assert (log.gettingLock(this.waiting));
	    synchronized (this.waiting) {
		assert (log.gotLock(this.waiting));
		this.waiting.notify();
		assert (log.releasedLock(this.waiting));
	    }
	    while (this.isWaiting()) {
		try {
		    this.validExecutor.wait();
		} catch (InterruptedException e) {
		    assert (log.throwing(e));
		    throw (RuntimeException) new RuntimeException()
			    .initCause(e);
		}
	    }
	    assert (log.releasedLock(validExecutor));
	}

	assert (log.exiting("com.InfoMontage.task.TaskExecutor",
		"executeTask(ExecutableTask t = " + t + ")", "end of method"));
	return retVal;
    }

    synchronized boolean isWaiting() {
	assert (log.entering("start of method"));

	boolean returnboolean = waiting.getState();
	assert (log.exiting("end of method - return value = " + returnboolean));
	return returnboolean;
    }

    /**
         * The run method
         * 
         * @throws IllegalStateException
         *                 if called when already running.
         * @see java.lang.Thread#run()
         */
    public void run() throws IllegalStateException {
	assert (log.entering("start of method"));

	if (running.getState()) {
	    IllegalStateException e = new IllegalStateException(
		    "Attempt to run a ThreadTask that" + " is already running!");
	    assert (log.throwing(e));
	    throw e;
	}
	running.setState(true);
	while (running.getState()) {
	    try {
		assert (log.gettingLock(waiting));
		synchronized (waiting) {
		    assert (log.gotLock(waiting));
		    waiting.setState(true);
		    waiting.wait();
		    waiting.setState(false);
		    assert (log.releasedLock(waiting));
		}
	    } catch (InterruptedException e) {
		assert (log.throwing(e));
		this.stopRunning();
		throw (RuntimeException) new RuntimeException().initCause(e);
	    }
	    assert (log.gettingLock(validExecutor));
	    synchronized (validExecutor) {
		assert (log.gotLock(validExecutor));
		validExecutor.notify();
		assert (log.releasedLock(validExecutor));
	    }
	    if (myTask != null) {
		Thread.yield();
		myTask.processTask();
		myTask.clearTaskParameters();
		myTask = null;
	    }
	    validExecutor.setState(false);
	    myFactory.returnTaskExecutor(this);
	}

	assert (log.exiting("end of method"));
    }

    synchronized void stopRunning() throws IllegalMonitorStateException {
	assert (log.entering("start of method"));

	running.setState(false);
	waiting.notify();

	assert (log.exiting("end of method"));
    }

}