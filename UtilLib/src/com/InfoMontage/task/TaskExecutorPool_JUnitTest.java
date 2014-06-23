/*
 * TaskExecutorPool_JUnitTest.java
 * 
 * Created on Mar 19, 2006
 */

/*
 * 
 * Part of the "Client/Server Helper" library, a project from Information
 * Montage. Copyright (C) 2004 Richard A. Mead
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

import com.InfoMontage.util.BooleanState;

import junit.framework.TestCase;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class TaskExecutorPool_JUnitTest extends TestCase {

    volatile TaskExecutorPool p;
    volatile Task t1;
    volatile Task t2;
    volatile ExecutionState tes1;
    volatile ExecutionState tes2;

    public static void main(String[] args) {
	//junit.swingui.TestRunner.run(TaskExecutorPool_JUnitTest.class);
	junit.textui.TestRunner.run(TaskExecutorPool_JUnitTest.class);
    }

    /**
         * Constructor for TaskExecutorPool_JUnitTest.
         * 
         * @param arg0
         */
    public TaskExecutorPool_JUnitTest(String arg0) {
	super(arg0);
    }

    /*
         * (non-Javadoc)
         * 
         * @see junit.framework.TestCase#setUp()
         */
    protected void setUp() throws Exception {
	super.setUp();
	this.p = TaskExecutorPool.getPool();
	this.t1 = new TaskExecutorPoolTestTask();
	this.t2 = new TaskExecutorPoolTestTask();
    }

    /*
         * (non-Javadoc)
         * 
         * @see junit.framework.TestCase#tearDown()
         */
    protected void tearDown() throws Exception {
	this.p = null;
	this.t1 = null;
	this.t2 = null;
	super.tearDown();
    }

    public static class TaskExecutorPoolTestTask implements Task {

	volatile boolean parmsSet = false;
	volatile boolean taskDone = true;
	volatile BooleanState joiner = new BooleanState(false);

	/*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#processTask()
         */
	public synchronized void doTask() throws IllegalStateException {
	    synchronized (joiner) {
		if (parmsSet)
		    taskDone = true;
		else
		    throw new IllegalStateException(
			    "Task parameters not set prior to calling processTask()!");
		joiner.setState(false);
		joiner.notifyAll();
	    }
	}

	/*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#percentComplete()
         */
	public float percentComplete() {
	    return (parmsSet && !taskDone) ? 0 : 100;
	}

	/*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#setTaskParameters(java.lang.Object[])
         */
	public void setParameters(Object[] pa)
		throws IllegalArgumentException, IllegalStateException {
	    
		if ((null != pa) && (pa.length == 1) && (null == pa[0]))
		    parmsSet = true;
		else
		    throw new IllegalArgumentException(
			    "Task parameters not being set to a valid value!");
		taskDone = false;
	    
	}

	/*
         * (non-Javadoc)
         * 
         * @see com.InfoMontage.task.Task#clearTaskParameters()
         */
	public void clearParameters() throws IllegalStateException {
	    
		parmsSet = false;
	    
	}

	/* (non-Javadoc)
	 * @see com.InfoMontage.task.Task#validateParameters(java.lang.Object[])
	 */
	public Exception validateParameters(Object[] pa) {
	    return null;
	}

	/* (non-Javadoc)
	 * @see com.InfoMontage.task.Task#getResults()
	 */
	public Object[] getResults() throws IllegalStateException {
	    return null;
	}

    }

    /*
         * Test method for 'com.InfoMontage.task.TaskExecutorPool.getPool()'
         */
    public void testGetPool() {
	TaskExecutorPool p = TaskExecutorPool.getPool();
	assertNotNull(p);
    }

    /*
         * Test method for
         * 'com.InfoMontage.task.TaskExecutorPool.getPool(String)'
         */
    public void testGetPoolString() {
	TaskExecutorPool p = TaskExecutorPool.getPool("TestPool");
	assertNotNull(p);
    }

    /*
         * Test method for
         * 'com.InfoMontage.task.TaskExecutorPool.getPool(String, int)'
         */
    public void testGetPoolStringInt() {
	TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2);
	assertNotNull(p);
    }

    /*
         * Test method for
         * 'com.InfoMontage.task.TaskExecutorPool.getPool(String, int, int)'
         */
    public void testGetPoolStringIntInt() {
	TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2, 5);
	assertNotNull(p);
    }

    /*
         * Test method for
         * 'com.InfoMontage.task.TaskExecutorPool.getPool(String, int, int,
         * boolean)'
         */
    public void testGetPoolStringIntIntBoolean() {
	TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2, 5, false);
	assertNotNull(p);
    }

    /*
         * Test method for
         * 'com.InfoMontage.task.TaskExecutorPool.getPool(String, int, int,
         * boolean, int)'
         */
    public void testGetPoolStringIntIntBooleanInt() {
	TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2, 5, false,
		42);
	assertNotNull(p);
    }

    /*
         * /* Test method for
         * 'com.InfoMontage.task.TaskExecutorPool.doTask(Task, boolean)'
         */
    public void testDoMultiTask() {
	try {
	    tes1=p.doTask(t1, new Object[] { null }, false);
	    tes2=p.doTask(t2, new Object[] { null }, false);
	    tes1.waitForCompletion();
	    tes2.waitForCompletion();

	    tes1=p.doTask(t1, new Object[] { null }, false);
	    tes1.waitForCompletion();
	    tes2=p.doTask(t2, new Object[] { null }, false);
	    tes2.waitForCompletion();

	    tes1=p.doTask(t1, new Object[] { null }, true);
	    tes2=p.doTask(t2, new Object[] { null }, true);
	    tes1.waitForCompletion();
	    tes2.waitForCompletion();

	    tes1=p.doTask(t1, new Object[] { null }, true);
	    tes1.waitForCompletion();
	    tes2=p.doTask(t2, new Object[] { null }, true);
	    tes2.waitForCompletion();

	    tes1=p.doTask(t1, new Object[] { null }, false);
	    tes2=p.doTask(t2, new Object[] { null }, true);
	    tes1.waitForCompletion();
	    tes2.waitForCompletion();

	    tes1=p.doTask(t1, new Object[] { null }, false);
	    tes1.waitForCompletion();
	    tes2=p.doTask(t2, new Object[] { null }, true);
	    tes2.waitForCompletion();

	    tes1=p.doTask(t1, new Object[] { null }, true);
	    tes2=p.doTask(t2, new Object[] { null }, false);
	    tes1.waitForCompletion();
	    tes2.waitForCompletion();

	    tes1=p.doTask(t1, new Object[] { null }, true);
	    tes1.waitForCompletion();
	    tes2=p.doTask(t2, new Object[] { null }, false);
	    tes2.waitForCompletion();

	} catch (Exception e) {
	    fail(e.toString());
	}
    }

    /*
         * Test method for 'com.InfoMontage.task.TaskExecutorPool.doTask(Task,
         * boolean)'
         */
    public void testDoTask() {
	TaskExecutorPool p = TaskExecutorPool.getPool();
	Task t = new TaskExecutorPoolTestTask();
	try {
	    ExecutionState tes=p.doTask(t, new Object[] { null }, false);
	    tes.waitForCompletion();
	} catch (Exception e) {
	    fail(e.toString());
	}
    }

}
