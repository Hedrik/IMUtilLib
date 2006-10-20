/*
 * Task.java
 * 
 * Created on Jan 1, 2005
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
import com.InfoMontage.version.GenericCodeVersion;

/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public interface Task {

    /**
         * Interface file version. By convention, for use with
         * {@link com.InfoMontage.util.CodeVersion} methods, interface versions
         * are kept in a static field named <code>INTERFACE_CODE_VERSION</code>.
         * 
         * @see com.InfoMontage.util.CodeVersion
         *      com.InfoMontage.version.CodeVersion
         *      com.InfoMontage.version.GenericCodeVersion
         */
    static final CodeVersion INTERFACE_CODE_VERSION = GenericCodeVersion
	    .codeVersionFromCVSRevisionString("$Revision$");

    /**
         * @throws IllegalStateException
         *                 if called prior to having the {@link Task}s
         *                 parameters set (via {@link Task#setParameters}).
         */
    void doTask() throws IllegalStateException;

    /**
         * @return 0.0f if no processing has occurred, 100.0f if processing has
         *         completed, otherwise the percentage of the {@link Task} which
         *         has been completed.
         */
    float percentComplete();

    /**
         * @param pa
         *                The parameters for this {@link Task} to process, in
         *                the form of an array of {@link Object}s.
         * @throws IllegalArgumentException
         */
    void setParameters(final Object[] pa) throws IllegalArgumentException;

    /**
         * @param pa
         * @return null if parameters are valid for the subclass, otherwise an
         *         {@link Exception}explaining why validation failed.
         */
    Exception validateParameters(Object[] pa);

    /**
         * @throws IllegalStateException
         *                 if called while the {@link Task} is currently
         *                 processing (as defined by the
         *                 {@link Task#percentComplete()} method being
         *                 non-zero).
         */
    void clearParameters() throws IllegalStateException;

    /**
         * @return Results of task execution, or null if there are none.
         * @throws IllegalStateException
         *                 if called while the {@link Task} is currently
         *                 processing (as defined by the
         *                 {@link Task#percentComplete()} method being
         *                 non-zero).
         */
    Object[] getResults() throws IllegalStateException;

}