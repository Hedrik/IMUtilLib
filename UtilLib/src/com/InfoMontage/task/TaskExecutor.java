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

import java.util.logging.Level;
import java.util.logging.Logger;
import com.InfoMontage.util.AssertableLogger;

import com.InfoMontage.util.BooleanState;
import com.InfoMontage.version.CodeVersion;
import com.InfoMontage.version.GenericCodeVersion;

import java.util.Stack;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
final class TaskExecutor
    extends Thread
{

    /**
     * Logger for this class
     */
    private static final AssertableLogger log = new AssertableLogger(
        TaskExecutor.class.getName());

    public static CodeVersion implCodeVersion = new GenericCodeVersion(
        "0.7");

    private final BooleanState running = new BooleanState(false);
    private final BooleanState validExecutor = new BooleanState(false);
    private final TaskExecutorPool myFactory;
    private final BooleanState waiting = new BooleanState(false);
    private volatile Task myTask = null;

    /** Creates a new instance of a TaskExecutor - package access only */
    TaskExecutor(TaskExecutorPool factory, ThreadGroup group, int stackSize)
    {
        super(group, null, "Task processor", stackSize);
        if (log.isLoggable(Level.FINER)) {
            log.entering("com.InfoMontage.task.TaskExecutor",
                "TaskExecutor(TaskExecutorPool factory = " + factory
                    + ", ThreadGroup group = " + group
                    + ", int stackSize = " + stackSize + ")",
                "start of method");
        }

        if (null == factory) { throw (IllegalArgumentException) new IllegalArgumentException(
            "Attempt to create a TaskExecutor with a null TaskExecutorPool!")
            .initCause(new NullPointerException()); }
        myFactory = factory;

        if (log.isLoggable(Level.FINER)) {
            log.exiting("com.InfoMontage.task.TaskExecutor",
                "TaskExecutor(TaskExecutorPool factory = " + factory
                    + ", ThreadGroup group = " + group
                    + ", int stackSize = " + stackSize + ")",
                "end of method");
        }
    }

    synchronized void validateTaskThread() {
        if (log.isLoggable(Level.WARNING)) {
            log.warning("Validating TaskExecutor. -  : exception: "
                + null);
        }
        validExecutor.setState(true);
        if (log.isLoggable(Level.WARNING)) {
            log.warning("TaskExecutor Validated. -  : exception: "
                + null);
        }
    }

    synchronized void executeTask(Task t)
        throws IllegalStateException, IllegalMonitorStateException
    {
        if (log.isLoggable(Level.FINER)) {
            log.entering("com.InfoMontage.task.TaskExecutor",
                "executeTask(Task t = " + t + ")", "start of method");
        }

        if (!validExecutor.getState()) {
            if (log.isLoggable(Level.WARNING)) {
                log
                    .warning("executeTask: ARGH! not validExecutor. -  : exception: "
                        + null);
            }
            throw new IllegalStateException(
                "Attempt to use a TaskExecutor that is still a member of the"
                    + " TaskExecutorPool's TaskExecutor pool!");
        }
        if (!waiting.getState()) {
            if (log.isLoggable(Level.WARNING)) {
                log
                    .warning("executeTask: ARGH! not waiting. -  : exception: "
                        + null);
            }
            throw new IllegalStateException(
                "Attempt to use a TaskExecutor that"
                    + " is already executing a Task!");
        }
        if (!this.running.getState()) {
            if (log.isLoggable(Level.WARNING)) {
                log
                    .warning("executeTask: ARGH! not running. -  : exception: "
                        + null);
            }
            throw new IllegalStateException(
                "Attempt to use a TaskExecutor that"
                    + " has not been started!");
        }
        myTask = t;
        synchronized (this.waiting) {
            this.waiting.notify();
        }

        if (log.isLoggable(Level.FINER)) {
            log.exiting("com.InfoMontage.task.TaskExecutor",
                "executeTask(Task t = " + t + ")", "end of method");
        }
    }

    synchronized boolean isWaiting() {
        if (log.isLoggable(Level.FINER)) {
            log.entering("com.InfoMontage.task.TaskExecutor",
                "isWaiting()", "start of method");
        }


        boolean returnboolean = waiting.getState();
        if (log.isLoggable(Level.FINER)) {
            log.exiting("com.InfoMontage.task.TaskExecutor", "isWaiting()",
                "end of method - return value = " + returnboolean);
        }
        return returnboolean;
    }

    public void run() throws IllegalStateException {
        if (log.isLoggable(Level.FINER)) {
            log.entering("com.InfoMontage.task.TaskExecutor", "run()",
                "start of method");
        }

        if (running.getState()) {
            if (log.isLoggable(Level.WARNING)) {
                log
                    .warning("run:ARGH! running. -  : exception: " + null);
            }
            throw new IllegalStateException(
                "Attempt to run a ThreadTask that" + " is already running!");
        }
        running.setState(true);
        while (running.getState()) {
            try {
                synchronized (waiting) {
                    waiting.setState(true);
                    waiting.wait();
                    waiting.setState(false);
                }
            } catch (InterruptedException e) {
                if (log.isLoggable(Level.FINER)) {
                    log.throwing("com.InfoMontage.task.TaskExecutor",
                        "run()", e);
                }

                // No idea why this would happen...
                throw (RuntimeException) new RuntimeException()
                    .initCause(e);
            }
            synchronized (validExecutor) {
                if (myTask != null) {
                    synchronized (myTask) {
                        myTask.processTask();
                        myTask.clearTaskParameters();
                    }
                }
                validExecutor.setState(false);
                myTask = null;
                myFactory.returnTaskExecutor(this);
            }
        }

        if (log.isLoggable(Level.FINER)) {
            log.exiting("com.InfoMontage.task.TaskExecutor", "run()",
                "end of method");
        }
    }

    public synchronized void stopRunning()
        throws IllegalMonitorStateException
    {
        if (log.isLoggable(Level.FINER)) {
            log.entering("com.InfoMontage.task.TaskExecutor",
                "stopRunning()", "start of method");
        }

        running.setState(false);
        waiting.notify();

        if (log.isLoggable(Level.FINER)) {
            log.exiting("com.InfoMontage.task.TaskExecutor",
                "stopRunning()", "end of method");
        }
    }

}