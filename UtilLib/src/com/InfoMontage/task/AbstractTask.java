/*
 * AbstractTask.java
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

import com.InfoMontage.version.*;
import com.InfoMontage.util.BooleanState;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public abstract class AbstractTask
    implements Task
{

    /**
     * Logger for this class
     */
    private static final AssertableLogger log = new AssertableLogger(
        AbstractTask.class.getName());

    static final CodeVersion implCodeVersion = new GenericCodeVersion(
        "0.9.0.1b");

    /**
     * Comment for <code>params</code>
     */
    protected volatile Object[] params = null;
    protected volatile boolean paramsSet = false;
    
    /**
     * Comment for <code>processing</code>
     */
    private BooleanState processing = new BooleanState(false);

    /**
     *  
     */
    protected AbstractTask() {};

    /*
     * (non-Javadoc)
     * 
     * @see com.InfoMontage.task.Task#isProcessing()
     */
    public boolean isProcessing() {
        return processing.getState();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.InfoMontage.task.Task#percentComplete()
     */
    public float percentComplete() {
        // default is 0% until done
        return (isProcessing() && paramsSet) ? 0.0f : 100.0f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.InfoMontage.task.Task#processTask()
     */
    public synchronized void processTask() throws IllegalStateException {
        if (this.paramsSet) {
        processing.setState(true);
        this.doTask();
        this.params = null;
        this.paramsSet=false;
        processing.setState(false);
        } else {
            IllegalStateException e=new IllegalStateException("Attempt to"
                +" process a Task that has not had it's parameters set first!");
            assert (log.throwing(e));
            throw e;
        }
    }

    /**
     * @throws IllegalStateException
     */
    abstract protected void doTask() throws IllegalStateException;

    /*
     * (non-Javadoc)
     * 
     * @see com.InfoMontage.task.Task#setTaskParameters(java.lang.Object[])
     */
    public synchronized void setTaskParameters(Object[] pa)
        throws IllegalArgumentException
    {
        Exception e = this.validateParameters(pa);
        if (null != e) {
            throw (IllegalArgumentException) new IllegalArgumentException(
                "Attempt" + " to set a Task parameter to an illegal value!")
                .initCause(e);
        } else {
            this.params = pa;
            this.paramsSet=true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.InfoMontage.task.Task#clearTaskParameters()
     */
    public synchronized void clearTaskParameters()
        throws IllegalStateException
    {
        if (this.processing.getState()) { throw new IllegalStateException(
            "Attempt to clear a AbstractTask's parameters"
                + " while AbstractTask is still processing!"); }
        this.params = null;
    }

    /**
     * @param pa
     * @return null if parameters are valid for the subclass, otherwise an
     *         {@link Exception}explaining why validation failed.
     */
    abstract protected Exception validateParameters(Object[] pa);

}