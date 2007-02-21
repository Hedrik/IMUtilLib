/*
 * TaskExecutorPool.java
 * 
 * Created on May 9, 2004, 1:28 PM
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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;
import com.InfoMontage.common.Defaults;
import com.InfoMontage.util.AssertableLogger;

import java.util.Collections;
import java.util.Stack;

/**
 * A TaskExecutorPool is used to assign Tasks to TaskExecutors from a pool of
 * TaskExecutors which is allocated when the TaskExecutorPool is instantiated.
 * The number of Tasks that can be simultaneously executed is determined by the
 * size of the pool. In addition, the TaskExecutorPool will optionally allow the
 * thread that is calling it's doTask() function to block while awaiting a
 * TaskExecutor to become available.
 * <P>
 * A TaskExecutorPool may optionally be given a name, a number of TaskExecutors
 * to always retain in it's pool, a maximum number of TaskExecutors that may
 * execute Tasks simultaneously, and the stack size to use when creating
 * TaskExecutor threads.
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public final class TaskExecutorPool {

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
    public static CodeVersion implCodeVersion = com.InfoMontage.version.GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    /**
	 * Comment for <code>taskThreadPool</code>
	 */
    private final Stack taskThreadPool = new Stack();

    /**
	 * Comment for <code>taskThreadGroup</code>
	 */
    private final ThreadGroup taskThreadGroup;

    /**
	 * Comment for <code>activeTaskThreads</code>
	 */
    private java.util.Set activeTaskThreads;

    /**
	 * Comment for <code>taskThreadPoolMaxThreads</code>
	 */
    private int taskThreadPoolMaxThreads = Defaults.DEFAULT_TASK_EXECUTOR_POOL_RETAINED_EXECUTOR_THREADS;

    /**
	 * Comment for <code>absoluteMaxThreads</code>
	 */
    private int absoluteMaxThreads = Defaults.DEFAULT_TASK_EXECUTOR_POOL_MAXIMUM_EXECUTOR_THREADS;

    /**
	 * Comment for <code>taskThreadStackSize</code>
	 */
    private int taskThreadStackSize = Defaults.DEFAULT_TASK_EXECUTOR_THREAD_STACK_SIZE;

    /**
	 * Comment for <code>name</code>
	 */
    private final String name;

    /**
	 * Comment for <code>taskQueue</code>
	 */
    private TaskQueue taskQueue = null;

    /**
	 * Logger for this class
	 */
    private static final AssertableLogger log = new AssertableLogger(
	    TaskExecutorPool.class.getName());

    /**
	 * This constructor is private, since {@link TaskExecutorPool}s are
	 * intended to be created through a static method.
	 */
    private TaskExecutorPool() {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "TaskExecutorPool()", "start of method");
	}

	this.name = "Generic TaskExecutor factory";
	this.taskThreadGroup = new ThreadGroup("Generic TaskExecutor group");

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "TaskExecutorPool()", "end of method");
	}
    }

    /**
	 * This constructor is private, since {@link TaskExecutorPool}s are
	 * intended to be created through a static method.
	 *
	 * @param name
	 */
    private TaskExecutorPool(String name) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "TaskExecutorPool(String name = " + name + ")",
		    "start of method");
	}

	this.name = name;
	this.taskThreadGroup = new ThreadGroup(name + " TaskExecutor group");

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "TaskExecutorPool(String name = " + name + ")",
		    "end of method");
	}
    }

    /**
	 * @return a TaskExecutorPool with defaults for all parameters.
	 */
    public static TaskExecutorPool getPool() {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool", "getPool()",
		    "start of method");
	}

	TaskExecutorPool ttf = new TaskExecutorPool();
	initializeTaskExecutorPool(ttf,
		Defaults.DEFAULT_TASK_EXECUTOR_POOL_RETAINED_EXECUTOR_THREADS,
		Defaults.DEFAULT_TASK_EXECUTOR_POOL_MAXIMUM_EXECUTOR_THREADS,
		true, Defaults.DEFAULT_TASK_EXECUTOR_THREAD_STACK_SIZE,
		Defaults.DEFAULT_TASK_QUEUE_START_CAPACITY,
		Defaults.DEFAULT_TASK_QUEUE_CAPACITY_INCREMENT,
		Defaults.DEFAULT_TASK_QUEUE_MAXIMUM_CAPACITY);

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool", "getPool()",
		    "end of method - return value = " + ttf);
	}
	return ttf;
    }

    /**
	 * @param name
	 * @return a TaskExecutorPool with the specified name and default values
	 *         for all parameters.
	 */
    public static TaskExecutorPool getPool(String name) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name + ")", "start of method");
	}

	TaskExecutorPool ttf = new TaskExecutorPool(name);
	initializeTaskExecutorPool(ttf,
		Defaults.DEFAULT_TASK_EXECUTOR_POOL_RETAINED_EXECUTOR_THREADS,
		Defaults.DEFAULT_TASK_EXECUTOR_POOL_MAXIMUM_EXECUTOR_THREADS,
		true, Defaults.DEFAULT_TASK_EXECUTOR_THREAD_STACK_SIZE,
		Defaults.DEFAULT_TASK_QUEUE_START_CAPACITY,
		Defaults.DEFAULT_TASK_QUEUE_CAPACITY_INCREMENT,
		Defaults.DEFAULT_TASK_QUEUE_MAXIMUM_CAPACITY);

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name + ")",
		    "end of method - return value = " + ttf);
	}
	return ttf;
    }

    public static TaskExecutorPool getPool(String name, int retainedPoolSize) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ")", "start of method");
	}

	TaskExecutorPool ttf = new TaskExecutorPool(name);
	initializeTaskExecutorPool(ttf, retainedPoolSize,
		Defaults.DEFAULT_TASK_EXECUTOR_POOL_MAXIMUM_EXECUTOR_THREADS,
		true, Defaults.DEFAULT_TASK_EXECUTOR_THREAD_STACK_SIZE,
		Defaults.DEFAULT_TASK_QUEUE_START_CAPACITY,
		Defaults.DEFAULT_TASK_QUEUE_CAPACITY_INCREMENT,
		Defaults.DEFAULT_TASK_QUEUE_MAXIMUM_CAPACITY);

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ")", "end of method - return value = " + ttf);
	}
	return ttf;
    }

    public static TaskExecutorPool getPool(String name, int retainedPoolSize,
	    int absoluteMaxThreads) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ")", "start of method");
	}

	TaskExecutorPool ttf = new TaskExecutorPool(name);
	initializeTaskExecutorPool(ttf, retainedPoolSize, absoluteMaxThreads,
		true, Defaults.DEFAULT_TASK_EXECUTOR_THREAD_STACK_SIZE,
		Defaults.DEFAULT_TASK_QUEUE_START_CAPACITY,
		Defaults.DEFAULT_TASK_QUEUE_CAPACITY_INCREMENT,
		Defaults.DEFAULT_TASK_QUEUE_MAXIMUM_CAPACITY);

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ")",
		    "end of method - return value = " + ttf);
	}
	return ttf;
    }

    public static TaskExecutorPool getPool(String name, int retainedPoolSize,
	    int absoluteMaxThreads, boolean allowBlocking) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ", boolean allowBlocking = "
			    + allowBlocking + ")", "start of method");
	}

	TaskExecutorPool ttf = new TaskExecutorPool(name);
	initializeTaskExecutorPool(ttf, retainedPoolSize, absoluteMaxThreads,
		allowBlocking,
		Defaults.DEFAULT_TASK_EXECUTOR_THREAD_STACK_SIZE,
		Defaults.DEFAULT_TASK_QUEUE_START_CAPACITY,
		Defaults.DEFAULT_TASK_QUEUE_CAPACITY_INCREMENT,
		Defaults.DEFAULT_TASK_QUEUE_MAXIMUM_CAPACITY);

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ", boolean allowBlocking = "
			    + allowBlocking + ")",
		    "end of method - return value = " + ttf);
	}
	return ttf;
    }

    public static TaskExecutorPool getPool(String name, int retainedPoolSize,
	    int absoluteMaxThreads, boolean allowBlocking, int threadStackSize) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ", boolean allowBlocking = "
			    + allowBlocking + ", int threadStackSize = "
			    + threadStackSize + ")", "start of method");
	}

	TaskExecutorPool ttf = new TaskExecutorPool(name);
	initializeTaskExecutorPool(ttf, retainedPoolSize, absoluteMaxThreads,
		allowBlocking, threadStackSize,
		Defaults.DEFAULT_TASK_QUEUE_START_CAPACITY,
		Defaults.DEFAULT_TASK_QUEUE_CAPACITY_INCREMENT,
		Defaults.DEFAULT_TASK_QUEUE_MAXIMUM_CAPACITY);

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "getPool(String name = " + name
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ", boolean allowBlocking = "
			    + allowBlocking + ", int threadStackSize = "
			    + threadStackSize + ")",
		    "end of method - return value = " + ttf);
	}
	return ttf;
    }

    /**
	 * @param ttf
	 * @param retainedPoolSize
	 * @param absoluteMaxThreads
	 * @param allowBlocking
	 * @param threadStackSize
	 * @param taskQueueCapacity
	 * @param taskQueueCapacityIncrement
	 * @param taskQueueMaxCapacity
	 * @throws OutOfMemoryError
	 *                 TBD: do
	 */
    private static void initializeTaskExecutorPool(TaskExecutorPool ttf,
	    int retainedPoolSize, int absoluteMaxThreads,
	    boolean allowBlocking, int threadStackSize, int taskQueueCapacity,
	    int taskQueueCapacityIncrement, int taskQueueMaxCapacity)
	    throws OutOfMemoryError {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "initializeTaskExecutorPool(TaskExecutorPool ttf = " + ttf
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ", boolean allowBlocking = "
			    + allowBlocking + ", int threadStackSize = "
			    + threadStackSize + ", int taskQueueCapacity = "
			    + taskQueueCapacity
			    + ", int taskQueueCapacityIncrement = "
			    + taskQueueCapacityIncrement
			    + ", int taskQueueMaxCapacity = "
			    + taskQueueMaxCapacity + ")", "start of method");
	}

	ttf.taskThreadPoolMaxThreads = retainedPoolSize;
	ttf.absoluteMaxThreads = absoluteMaxThreads;
	ttf.taskThreadStackSize = threadStackSize;
	ttf.activeTaskThreads = Collections
		.synchronizedSet(new java.util.HashSet(retainedPoolSize));
	ttf.taskQueue = new TaskQueue(taskQueueCapacity,
		taskQueueCapacityIncrement, taskQueueMaxCapacity);

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "initializeTaskExecutorPool(TaskExecutorPool ttf = " + ttf
			    + ", int retainedPoolSize = " + retainedPoolSize
			    + ", int absoluteMaxThreads = "
			    + absoluteMaxThreads + ", boolean allowBlocking = "
			    + allowBlocking + ", int threadStackSize = "
			    + threadStackSize + ", int taskQueueCapacity = "
			    + taskQueueCapacity
			    + ", int taskQueueCapacityIncrement = "
			    + taskQueueCapacityIncrement
			    + ", int taskQueueMaxCapacity = "
			    + taskQueueMaxCapacity + ")", "end of method");
	}
    }

    private static void initializeTaskExecutor(TaskExecutor t) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "initializeTaskExecutor(TaskExecutor t = " + t + ")",
		    "start of method");
	}

	synchronized (t) {
	    if (!t.isAlive())
		t.start();
	}
	while (!t.isWaiting()) {
	    try {
		Thread.sleep(1);
	    } catch (InterruptedException e) {
		if (log.isLoggable(Level.FINER)) {
		    log.throwing("com.InfoMontage.task.TaskExecutorPool",
			    "initializeTaskExecutor(TaskExecutor t = " + t
				    + ")", e);
		}

		throw (RuntimeException) new RuntimeException().initCause(e);
	    }
	}
	t.validateTaskThread();

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "initializeTaskExecutor(TaskExecutor t = " + t + ")",
		    "end of method");
	}
    }

    /**
	 * This is the primary function of the TaskExecutorPool. Given a Task,
	 * this function will attempt to assign it to a TaskExecutor from the
	 * taskThreadPool. If no TaskExecutor is currently available, and if
	 * allowBlocking is true, then this function will block, to wait for a
	 * TaskExecutor to become available for the execution of Task t. A
	 * TaskExecutor is considered available if it is in a waiting state, as
	 * specified by it's isWaiting() function.
	 *
	 * @param t
	 *                The task to perform
	 * @param p
	 *                The parameters to be used by the task
	 * @return An ExecutionState object if execution of task has started;
	 *         <BR>
	 *         null if the task will not be executed.
	 * @throws InterruptedException
	 *                 if the thread calling this function is interrupted
	 *                 while waiting for the TaskExecutorPool's blocking
	 *                 task executor thread. This will only happen if
	 *                 allowBlocking is true.
	 */
    public ExecutionState doTask(Task t, Object[] p, boolean allowBlocking)
	    throws InterruptedException {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "doTask(Task t = " + t + ")", "start of method");
	}

	ExecutionState retVal = null;
	ExecutableTask et = new ExecutableTask(t);
	et.setTaskParameters(p);
	synchronized (taskThreadPool) {
	    synchronized (activeTaskThreads) {
		if (activeTaskThreads.size() < absoluteMaxThreads) {
		    retVal = getTaskExecutor().executeTask(et);
		}
	    }
	    if ((null == retVal) && allowBlocking) {
		taskThreadPool.wait();
		retVal = getTaskExecutor().executeTask(et);
	    }
	}

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "doTask(Task t = " + t + ")",
		    "end of method - return value = " + retVal);
	}
	return retVal;
    }

    // TBD: only allow thread getting TaskExecutor to set the Task?
    private TaskExecutor getTaskExecutor() {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "getTaskExecutor()", "start of method");
	}

	TaskExecutor rt = null;
	synchronized (taskThreadPool) {
	    if (taskThreadPool.isEmpty()) {
		synchronized (activeTaskThreads) {
		    if (activeTaskThreads.size() < absoluteMaxThreads) {
			rt = new TaskExecutor(this, this.taskThreadGroup,
				this.taskThreadStackSize);
			// rt.setPriority(Thread.NORM_PRIORITY + 1);
		    }
		}
	    } else {
		rt = (TaskExecutor) taskThreadPool.pop();
	    }
	    if (null != rt) {
		synchronized (activeTaskThreads) {
		    activeTaskThreads.add(rt);
		}
		initializeTaskExecutor(rt);
	    }
	}
	if (log.isLoggable(Level.FINER)) {
	    log
		    .exiting("com.InfoMontage.task.TaskExecutorPool",
			    "getTaskExecutor()",
			    "end of method - return value = " + rt);
	}
	return rt;
    }

    /**
	 * Only the TaskExecutor should be calling this method...
	 */
    void returnTaskExecutor(TaskExecutor mpt) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskExecutorPool",
		    "returnTaskExecutor(TaskExecutor mpt = " + mpt + ")",
		    "start of method");
	}

	synchronized (activeTaskThreads) {
	    activeTaskThreads.remove(mpt);
	}
	synchronized (taskThreadPool) {
	    if (taskThreadPool.size() <= taskThreadPoolMaxThreads) {
		taskThreadPool.addElement(mpt);
		taskThreadPool.notifyAll();
	    } else {
		mpt.stopRunning();
	    }
	}

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskExecutorPool",
		    "returnTaskExecutor(TaskExecutor mpt = " + mpt + ")",
		    "end of method");
	}
    }

}