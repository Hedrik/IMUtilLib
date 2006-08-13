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
	
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(TaskExecutorPool_JUnitTest.class);
	}

	/**
	 * Constructor for TaskExecutorPool_JUnitTest.
	 * 
	 * @param arg0
	 */
	public TaskExecutorPool_JUnitTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.p = TaskExecutorPool.getPool();
		this.t1 = new TaskExecutorPoolTestTask();
		this.t2 = new TaskExecutorPoolTestTask();
	}

	/* (non-Javadoc)
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

		boolean joiner = false;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.InfoMontage.task.Task#processTask()
		 */
		public synchronized void processTask() throws IllegalStateException {
			if (parmsSet)
				taskDone = true;
			else
				throw new IllegalStateException(
						"Task parameters not set prior to calling processTask()!");
			joiner = false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.InfoMontage.task.Task#isProcessing()
		 */
		public boolean isProcessing() {
			return (parmsSet && !taskDone);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.InfoMontage.task.Task#percentComplete()
		 */
		public float percentComplete() {
			return (isProcessing()) ? 0 : 100;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.InfoMontage.task.Task#setTaskParameters(java.lang.Object[])
		 */
		public void setTaskParameters(Object[] pa)
				throws IllegalArgumentException, IllegalStateException {
			if (this.isProcessing()) {
				throw new IllegalStateException(
						"Task parameters being set while still processing!");
			} else {
				if ((null != pa) && (pa.length == 1) && (null == pa[0]))
					parmsSet = true;
				else
					throw new IllegalArgumentException(
							"Task parameters not being set to a valid value!");
				taskDone = false;
			}
		}

		public synchronized void join() {
			joiner = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.InfoMontage.task.Task#clearTaskParameters()
		 */
		public void clearTaskParameters() throws IllegalStateException {
			if (!this.isProcessing())
				parmsSet = false;
			else
				throw new IllegalStateException(
						"Task parameters being cleared while still processing task!");
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
	 * Test method for 'com.InfoMontage.task.TaskExecutorPool.getPool(String)'
	 */
	public void testGetPoolString() {
		TaskExecutorPool p = TaskExecutorPool.getPool("TestPool");
		assertNotNull(p);
	}

	/*
	 * Test method for 'com.InfoMontage.task.TaskExecutorPool.getPool(String,
	 * int)'
	 */
	public void testGetPoolStringInt() {
		TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2);
		assertNotNull(p);
	}

	/*
	 * Test method for 'com.InfoMontage.task.TaskExecutorPool.getPool(String,
	 * int, int)'
	 */
	public void testGetPoolStringIntInt() {
		TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2, 5);
		assertNotNull(p);
	}

	/*
	 * Test method for 'com.InfoMontage.task.TaskExecutorPool.getPool(String,
	 * int, int, boolean)'
	 */
	public void testGetPoolStringIntIntBoolean() {
		TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2, 5, false);
		assertNotNull(p);
	}

	/*
	 * Test method for 'com.InfoMontage.task.TaskExecutorPool.getPool(String,
	 * int, int, boolean, int)'
	 */
	public void testGetPoolStringIntIntBooleanInt() {
		TaskExecutorPool p = TaskExecutorPool.getPool("TestPool", 2, 5, false,
				42);
		assertNotNull(p);
	}

	/*
	 * /* Test method for 'com.InfoMontage.task.TaskExecutorPool.doTask(Task,
	 * boolean)'
	 */
	public void testDoMultiTask() {
		try {
			t1.setTaskParameters(new Object[] { null });
			p.doTask(t1, false);
			t2.setTaskParameters(new Object[] { null });
			p.doTask(t2, false);
			t1.join();
			t1.clearTaskParameters();
			t2.join();
			t2.clearTaskParameters();

			t1.setTaskParameters(new Object[] { null });
			p.doTask(t1, false);
			t1.join();
			t1.clearTaskParameters();
			t2.setTaskParameters(new Object[] { null });
			p.doTask(t2, false);
			t2.join();
			t2.clearTaskParameters();

			t1.setTaskParameters(new Object[] { null });
			t2.setTaskParameters(new Object[] { null });
			p.doTask(t1, false);
			p.doTask(t2, false);
			t1.clearTaskParameters();
			t2.clearTaskParameters();

			t1.setTaskParameters(new Object[] { null });
			t2.setTaskParameters(new Object[] { null });
			p.doTask(t1, false);
			t1.clearTaskParameters();
			p.doTask(t2, false);
			t2.clearTaskParameters();

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
			t.setTaskParameters(new Object[] { null });
			p.doTask(t, false);
			t.join();
			t.clearTaskParameters();
		} catch (Exception e) {
			fail(e.toString());
		}
	}

}
