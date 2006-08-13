/*
 * TaskQueue.java
 * 
 * Created on Nov 29, 2004
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
import com.InfoMontage.util.AssertableLogger;

import java.util.Vector;

import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class TaskQueue {

    /**
         * Logger for this class
         */
    private static final AssertableLogger log = new AssertableLogger(
	    TaskQueue.class.getName());

    public static CodeVersion implCodeVersion = new GenericCodeVersion("0.9");

    private Vector queue;

    private int minQueuesize;

    private int queueSizeIncrement;

    private int maxQueueSize;

    /**
         * @param minimumQueueSize
         * @param sizeIncrement
         * @param maximumQueueSize
         */
    TaskQueue(int minimumQueueSize, int sizeIncrement, int maximumQueueSize) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskQueue",
		    "TaskQueue(int minimumQueueSize = " + minimumQueueSize
			    + ", int sizeIncrement = " + sizeIncrement
			    + ", int maximumQueueSize = " + maximumQueueSize
			    + ")", "start of method");
	}

	minQueuesize = minimumQueueSize;
	queueSizeIncrement = sizeIncrement;
	queue = new Vector(minimumQueueSize, sizeIncrement);
	maxQueueSize = maximumQueueSize;

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskQueue",
		    "TaskQueue(int minimumQueueSize = " + minimumQueueSize
			    + ", int sizeIncrement = " + sizeIncrement
			    + ", int maximumQueueSize = " + maximumQueueSize
			    + ")", "end of method");
	}
    }

    /**
         * @param q
         */
    TaskQueue(final TaskQueue q) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskQueue",
		    "TaskQueue(TaskQueue q = " + q + ")", "start of method");
	}

	queue = new Vector(q.queue.capacity(), q.queueSizeIncrement);
	maxQueueSize = q.maxQueueSize;

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskQueue",
		    "TaskQueue(TaskQueue q = " + q + ")", "end of method");
	}
    }

    boolean enqueue(Task t) {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskQueue", "enqueue(Task t = "
		    + t + ")", "start of method");
	}

	boolean queued = false;
	try {
	    queued = enqueue(t, false);
	} catch (InterruptedException e) {
	    if (log.isLoggable(Level.FINER)) {
		log.throwing("com.InfoMontage.task.TaskQueue",
			"enqueue(Task t = " + t + ")", e);
	    }

	    // cannot happen since we called with blocking=false
	    e.printStackTrace();
	}

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskQueue", "enqueue(Task t = "
		    + t + ")", "end of method - return value = " + queued);
	}
	return queued;
    }

    synchronized boolean enqueue(Task t, boolean blocking)
	    throws InterruptedException {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskQueue", "enqueue(Task t = "
		    + t + ", boolean blocking = " + blocking + ")",
		    "start of method");
	}

	boolean queued = false;

	if (t.isProcessing()) {
	    log.severe("com.InfoMontage.task.TaskQueue attempt to enqeue a"
		    + "task that is still processing: " + t);
	} else {
	    synchronized (queue) {
		while (maxQueueSize <= queue.size()) {
		    if (!blocking)
			break;
		    else
			queue.wait();
		}
		queued = queue.add(t);
	    }
	}

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskQueue", "enqueue(Task t = "
		    + t + ", boolean blocking = " + blocking + ")",
		    "end of method - return value = " + queued);
	}
	return queued;
    }

    Task dequeue() {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskQueue", "dequeue()",
		    "start of method");
	}

	Task t = null;
	synchronized (queue) {
	    t = (Task) queue.firstElement();
	    queue.remove(t);
	    queue.notify();
	}

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskQueue", "dequeue()",
		    "end of method - return value = " + t);
	}
	return t;
    }

    /**
         * @return Returns the maximum queue size.
         */
    int getMaxQueueSize() {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskQueue", "getMaxQueueSize()",
		    "start of method");
	}

	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskQueue", "getMaxQueueSize()",
		    "end of method - return value = " + maxQueueSize);
	}
	return maxQueueSize;
    }

    /**
         * @see java.lang.Object#toString()
         */
    public synchronized String toString() {
	if (log.isLoggable(Level.FINER)) {
	    log.entering("com.InfoMontage.task.TaskQueue", "toString()",
		    "start of method");
	}

	String returnString = super.toString();
	if (log.isLoggable(Level.FINER)) {
	    log.exiting("com.InfoMontage.task.TaskQueue", "toString()",
		    "end of method - return value = " + returnString);
	}
	return returnString;
    }
}