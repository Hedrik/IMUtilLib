/*
 * AssertableLogger.java
 * 
 * Created on July 16, 2004, 11:04 PM
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

package com.InfoMontage.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 
 * @author Richard A. Mead <BR>
 *         Information Montage
 */
public class AssertableLogger {

    private Logger logger = null;

    /** Constructs an AssertableLogger for a named subsystem. */
    public AssertableLogger(String name) {
        this.logger = Logger.getLogger(name);
    }

    /**
     * Constructs an AssertableLogger for a named subsystem and a resource
     * bundle.
     */
    public AssertableLogger(String name, String resourceBundleName) {
        this.logger = Logger.getLogger(name, resourceBundleName);
    }

    /** Public method to construct an AssertableLogger from a Logger. */
    public AssertableLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Log a message, specifying source class, method, and resource bundle
     * name, with associated Throwable information.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given arguments are stored in a LogRecord which is forwarded to all
     * registered output handlers.
     * <p>
     * The msg string is localized using the named resource bundle. If the
     * resource bundle name is null, then the msg string is not localized.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property. Thus is it
     * processed specially by output Formatters and is not treated as a
     * formatting parameter to the LogRecord message property.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param bundleName name of resource bundle to localize msg
     * @param msg The string message (or a key in the message catalog)
     * @param thrown Throwable associated with log message.
     * @throws MissingResourceException if no suitable ResourceBundle can be
     *             found.
     */
    public boolean logrb(Level level, String sourceClass,
        String sourceMethod, String bundleName, String msg, Throwable thrown)
    {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg,
            thrown);
        return true;
    }

    /**
     * Log a CONFIG message.
     * <p>
     * If the logger is currently enabled for the CONFIG message level then
     * the given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean config(String msg) {
        logger.config(msg);
        return true;
    }

    /**
     * Log throwing an exception.
     * <p>
     * This is a convenience method to log that a method is terminating by
     * throwing an exception. The logging is done using the FINER level.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given arguments are stored in a LogRecord which is forwarded to all
     * registered output handlers. The LogRecord's message is set to "THROW".
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property. Thus is it
     * processed specially by output Formatters and is not treated as a
     * formatting parameter to the LogRecord message property.
     * <p>
     * 
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of the method.
     * @param thrown The Throwable that is being thrown.
     */
    public boolean throwing(String sourceClass, String sourceMethod,
        Throwable thrown)
    {
        logger.throwing(sourceClass, sourceMethod, thrown);
        return true;
    }

    /**
     * Log a SEVERE message.
     * <p>
     * If the logger is currently enabled for the SEVERE message level then
     * the given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean severe(String msg) {
        logger.severe(msg);
        return true;
    }

    /**
     * Specify whether or not this logger should send its output to it's
     * parent Logger. This means that any LogRecords will also be written to
     * the parent's Handlers, and potentially to its parent, recursively up
     * the namespace.
     * 
     * @param useParentHandlers true if output is to be sent to the logger's
     *            parent.
     * @exception SecurityException if a security manager exists and if the
     *                caller does not have LoggingPermission("control").
     */
    public boolean setUseParentHandlers(boolean useParentHandlers) {
        logger.setUseParentHandlers(useParentHandlers);
        return true;
    }

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message level then a
     * corresponding LogRecord is created and forwarded to all the registered
     * output Handler objects.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message (or a key in the message catalog)
     * @param params array of parameters to the message
     */
    public boolean log(Level level, String msg, Object[] params) {
        logger.log(level, msg, params);
        return true;
    }

    /**
     * Log a message, with associated Throwable information.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given arguments are stored in a LogRecord which is forwarded to all
     * registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property. Thus is it
     * processed specially by output Formatters and is not treated as a
     * formatting parameter to the LogRecord message property.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message (or a key in the message catalog)
     * @param thrown Throwable associated with log message.
     */
    public boolean log(Level level, String msg, Throwable thrown) {
        logger.log(level, msg, thrown);
        return true;
    }

    /**
     * Log a WARNING message.
     * <p>
     * If the logger is currently enabled for the WARNING message level then
     * the given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean warning(String msg) {
        logger.warning(msg);
        return true;
    }

    /**
     * Set the parent for this Logger. This method is used by the LogManager
     * to update a Logger when the namespace changes.
     * <p>
     * It should not be called from application code.
     * <p>
     * 
     * @param parent the new parent logger
     * @exception SecurityException if a security manager exists and if the
     *                caller does not have LoggingPermission("control").
     */
    public boolean setParent(Logger parent) {
        logger.setParent(parent);
        return true;
    }

    /**
     * Log a message, specifying source class, method, and resource bundle
     * name with no arguments.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * The msg string is localized using the named resource bundle. If the
     * resource bundle name is null, then the msg string is not localized.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param bundleName name of resource bundle to localize msg
     * @param msg The string message (or a key in the message catalog)
     * @throws MissingResourceException if no suitable ResourceBundle can be
     *             found.
     */
    public boolean logrb(Level level, String sourceClass,
        String sourceMethod, String bundleName, String msg)
    {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg);
        return true;
    }

    /**
     * Set a filter to control output on this Logger.
     * <P>
     * After passing the initial "level" check, the Logger will call this
     * Filter to check if a log record should really be published.
     * 
     * @param newFilter a filter object (may be null)
     * @exception SecurityException if a security manager exists and if the
     *                caller does not have LoggingPermission("control").
     */
    public boolean setFilter(Filter newFilter) throws SecurityException {
        logger.setFilter(newFilter);
        return true;
    }

    /**
     * Return the parent for this Logger.
     * <p>
     * This method returns the nearest extant parent in the namespace. Thus if
     * a Logger is called "a.b.c.d", and a Logger called "a.b" has been
     * created but no logger "a.b.c" exists, then a call of getParent on the
     * Logger "a.b.c.d" will return the Logger "a.b".
     * <p>
     * The result will be null if it is called on the root Logger in the
     * namespace.
     * 
     * @return nearest existing parent Logger
     */
    public Logger getParent() {
        Logger retValue;

        retValue = logger.getParent();
        return retValue;
    }

    /**
     * Log a method entry, with an array of parameters.
     * <p>
     * This is a convenience method that can be used to log entry to a method.
     * A LogRecord with message "ENTRY" (followed by a format {Nreturn true; }
     * indicator for each entry in the parameter array), log level FINER, and
     * the given sourceMethod, sourceClass, and parameters is logged.
     * <p>
     * 
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that is being entered
     * @param params array of parameters to the method being entered
     */
    public boolean entering(String sourceClass, String sourceMethod,
        Object[] params)
    {
        logger.entering(sourceClass, sourceMethod, params);
        return true;
    }

    /**
     * Log a message, with one object parameter.
     * <p>
     * If the logger is currently enabled for the given message level then a
     * corresponding LogRecord is created and forwarded to all the registered
     * output Handler objects.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message (or a key in the message catalog)
     * @param param1 parameter to the message
     */
    public boolean log(Level level, String msg, Object param1) {
        logger.log(level, msg, param1);
        return true;
    }

    /**
     * Set the log level specifying which message levels will be logged by
     * this logger. Message levels lower than this value will be discarded.
     * The level value Level.OFF can be used to turn off logging.
     * <p>
     * If the new level is null, it means that this node should inherit its
     * level from its nearest ancestor with a specific (non-null) level value.
     * 
     * @param newLevel the new value for the log level (may be null)
     * @exception SecurityException if a security manager exists and if the
     *                caller does not have LoggingPermission("control").
     */
    public boolean setLevel(Level newLevel) throws SecurityException {
        logger.setLevel(newLevel);
        return true;
    }

    /**
     * Log a message, specifying source class and method, with associated
     * Throwable information.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given arguments are stored in a LogRecord which is forwarded to all
     * registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property. Thus is it
     * processed specially by output Formatters and is not treated as a
     * formatting parameter to the LogRecord message property.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param msg The string message (or a key in the message catalog)
     * @param thrown Throwable associated with log message.
     */
    public boolean logp(Level level, String sourceClass,
        String sourceMethod, String msg, Throwable thrown)
    {
        logger.logp(level, sourceClass, sourceMethod, msg, thrown);
        return true;
    }

    /**
     * Check if a message of the given level would actually be logged by this
     * logger. This check is based on the Loggers effective level, which may
     * be inherited from its parent.
     * 
     * @param level a message logging level
     * @return true if the given message level is currently being logged.
     */
    public boolean isLoggable(Level level) {
        boolean retValue;

        retValue = logger.isLoggable(level);
        return retValue;
    }

    /**
     * Log a message, specifying source class and method, with no arguments.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean logp(Level level, String sourceClass,
        String sourceMethod, String msg)
    {
        logger.logp(level, sourceClass, sourceMethod, msg);
        return true;
    }

    /**
     * Log a message, specifying source class, method, and resource bundle
     * name, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message level then a
     * corresponding LogRecord is created and forwarded to all the registered
     * output Handler objects.
     * <p>
     * The msg string is localized using the named resource bundle. If the
     * resource bundle name is null, then the msg string is not localized.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param bundleName name of resource bundle to localize msg
     * @param msg The string message (or a key in the message catalog)
     * @param params Array of parameters to the message
     * @throws MissingResourceException if no suitable ResourceBundle can be
     *             found.
     */
    public boolean logrb(Level level, String sourceClass,
        String sourceMethod, String bundleName, String msg, Object[] params)
    {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg,
            params);
        return true;
    }

    /**
     * Get the Handlers associated with this logger.
     * <p>
     * 
     * @return an array of all registered Handlers
     */
    public Handler[] getHandlers() {
        Handler[] retValue;

        retValue = logger.getHandlers();
        return retValue;
    }

    /**
     * Log a method entry, with one parameter.
     * <p>
     * This is a convenience method that can be used to log entry to a method.
     * A LogRecord with message "ENTRY {0return true; }", log level FINER, and
     * the given sourceMethod, sourceClass, and parameter is logged.
     * <p>
     * 
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that is being entered
     * @param param1 parameter to the method being entered
     */
    public boolean entering(String sourceClass, String sourceMethod,
        Object param1)
    {
        logger.entering(sourceClass, sourceMethod, param1);
        return true;
    }

    /**
     * Retrieve the localization resource bundle for this logger for the
     * current default locale. Note that if the result is null, then the
     * Logger will use a resource bundle inherited from its parent.
     * 
     * @return localization bundle (may be null)
     */
    public ResourceBundle getResourceBundle() {
        ResourceBundle retValue;

        retValue = logger.getResourceBundle();
        return retValue;
    }

    /**
     * Log a message, specifying source class, method, and resource bundle
     * name, with a single object parameter to the log message.
     * <p>
     * If the logger is currently enabled for the given message level then a
     * corresponding LogRecord is created and forwarded to all the registered
     * output Handler objects.
     * <p>
     * The msg string is localized using the named resource bundle. If the
     * resource bundle name is null, then the msg string is not localized.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param bundleName name of resource bundle to localize msg
     * @param msg The string message (or a key in the message catalog)
     * @param param1 Parameter to the log message.
     * @throws MissingResourceException if no suitable ResourceBundle can be
     *             found.
     */
    public boolean logrb(Level level, String sourceClass,
        String sourceMethod, String bundleName, String msg, Object param1)
    {
        logger.logrb(level, sourceClass, sourceMethod, bundleName, msg,
            param1);
        return true;
    }

    /**
     * Discover whether or not this logger is sending its output to its parent
     * logger.
     * 
     * @return true if output is to be sent to the logger's parent
     */
    public boolean getUseParentHandlers() {
        boolean retValue;

        retValue = logger.getUseParentHandlers();
        return retValue;
    }

    /**
     * Remove a log Handler.
     * <P>
     * Returns silently if the given Handler is not found.
     * 
     * @param handler a logging Handler
     * @exception SecurityException if a security manager exists and if the
     *                caller does not have LoggingPermission("control").
     */
    public boolean removeHandler(Handler handler) throws SecurityException {
        logger.removeHandler(handler);
        return true;
    }

    /**
     * Log a message, specifying source class and method, with an array of
     * object arguments.
     * <p>
     * If the logger is currently enabled for the given message level then a
     * corresponding LogRecord is created and forwarded to all the registered
     * output Handler objects.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param msg The string message (or a key in the message catalog)
     * @param params Array of parameters to the message
     */
    public boolean logp(Level level, String sourceClass,
        String sourceMethod, String msg, Object[] params)
    {
        logger.logp(level, sourceClass, sourceMethod, msg, params);
        return true;
    }

    /**
     * Log a message, specifying source class and method, with a single object
     * parameter to the log message.
     * <p>
     * If the logger is currently enabled for the given message level then a
     * corresponding LogRecord is created and forwarded to all the registered
     * output Handler objects.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that issued the logging request
     * @param msg The string message (or a key in the message catalog)
     * @param param1 Parameter to the log message.
     */
    public boolean logp(Level level, String sourceClass,
        String sourceMethod, String msg, Object param1)
    {
        logger.logp(level, sourceClass, sourceMethod, msg, param1);
        return true;
    }

    /**
     * Log an INFO message.
     * <p>
     * If the logger is currently enabled for the INFO message level then the
     * given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean info(String msg) {
        logger.info(msg);
        return true;
    }

    /**
     * Log a FINER message.
     * <p>
     * If the logger is currently enabled for the FINER message level then the
     * given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean finer(String msg) {
        logger.finer(msg);
        return true;
    }

    /**
     * Log a method return.
     * <p>
     * This is a convenience method that can be used to log returning from a
     * method. A LogRecord with message "RETURN", log level FINER, and the
     * given sourceMethod and sourceClass is logged.
     * <p>
     * 
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of the method
     */
    public boolean exiting(String sourceClass, String sourceMethod) {
        logger.exiting(sourceClass, sourceMethod);
        return true;
    }

    /**
     * Get the name for this logger.
     * 
     * @return logger name. Will be null for anonymous Loggers.
     */
    public String getName() {
        String retValue;

        retValue = logger.getName();
        return retValue;
    }

    /**
     * Log a LogRecord.
     * <p>
     * All the other logging methods in this class call through this method to
     * actually perform any logging. Subclasses can override this single
     * method to capture all log activity.
     * 
     * @param record the LogRecord to be published
     */
    public boolean log(java.util.logging.LogRecord record) {
        logger.log(record);
        return true;
    }

    /**
     * Log a FINEST message.
     * <p>
     * If the logger is currently enabled for the FINEST message level then
     * the given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean finest(String msg) {
        logger.finest(msg);
        return true;
    }

    /**
     * Get the current filter for this Logger.
     * 
     * @return a filter object (may be null)
     */
    public Filter getFilter() {
        Filter retValue;

        retValue = logger.getFilter();
        return retValue;
    }

    /**
     * Add a log Handler to receive logging messages.
     * <p>
     * By default, Loggers also send their output to their parent logger.
     * Typically the root Logger is configured with a set of Handlers that
     * essentially act as default handlers for all loggers.
     * 
     * @param handler a logging Handler
     * @exception SecurityException if a security manager exists and if the
     *                caller does not have LoggingPermission("control").
     */
    public boolean addHandler(Handler handler) throws SecurityException {
        logger.addHandler(handler);
        return true;
    }

    /**
     * Log a method return, with result object.
     * <p>
     * This is a convenience method that can be used to log returning from a
     * method. A LogRecord with message "RETURN {0return true; }", log level
     * FINER, and the gives sourceMethod, sourceClass, and result object is
     * logged.
     * <p>
     * 
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of the method
     * @param result Object that is being returned
     */
    public boolean exiting(String sourceClass, String sourceMethod,
        Object result)
    {
        logger.exiting(sourceClass, sourceMethod, result);
        return true;
    }

    /**
     * Log a message, with no arguments.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param level One of the message level identifiers, e.g. SEVERE
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean log(Level level, String msg) {
        logger.log(level, msg);
        return true;
    }

    /**
     * Log a method entry.
     * <p>
     * This is a convenience method that can be used to log entry to a method.
     * A LogRecord with message "ENTRY", log level FINER, and the given
     * sourceMethod and sourceClass is logged.
     * <p>
     * 
     * @param sourceClass name of class that issued the logging request
     * @param sourceMethod name of method that is being entered
     */
    public boolean entering(String sourceClass, String sourceMethod) {
        logger.entering(sourceClass, sourceMethod);
        return true;
    }

    /**
     * Log a FINE message.
     * <p>
     * If the logger is currently enabled for the FINE message level then the
     * given message is forwarded to all the registered output Handler
     * objects.
     * <p>
     * 
     * @param msg The string message (or a key in the message catalog)
     */
    public boolean fine(String msg) {
        logger.fine(msg);
        return true;
    }

    /**
     * Retrieve the localization resource bundle name for this logger. Note
     * that if the result is null, then the Logger will use a resource bundle
     * name inherited from its parent.
     * 
     * @return localization bundle name (may be null)
     */
    public String getResourceBundleName() {
        String retValue;

        retValue = logger.getResourceBundleName();
        return retValue;
    }

    /**
     * Get the log Level that has been specified for this Logger. The result
     * may be null, which means that this logger's effective level will be
     * inherited from its parent.
     * 
     * @return this Logger's level
     */
    public Level getLevel() {
        Level retValue;

        retValue = logger.getLevel();
        return retValue;
    }

    /**
     * Log a method entry.
     * <p>
     * This is a convenience method that can be used to log entry to a method.
     * A LogRecord with message "ENTRY", log level FINER, and the method and
     * class of the calling method is logged.
     */
    public boolean entering() {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.entering(ste.getClassName(), ste.getMethodName());
        return true;
    }

    /**
     * Log a method entry, with an array of parameters.
     * <p>
     * This is a convenience method that can be used to log entry to a method.
     * A LogRecord with message "ENTRY" (followed by a format {N} indicator
     * for each entry in the parameter array), log level FINER, and the method
     * and class of the calling method, and parameters is logged.
     * <p>
     * 
     * @param params array of parameters to the method being entered
     */
    public boolean entering(Object[] params) {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.entering(ste.getClassName(), ste.getMethodName(), params);
        return true;
    }

    /**
     * Log a method entry, with one parameter.
     * <p>
     * This is a convenience method that can be used to log entry to a method.
     * A LogRecord with message "ENTRY {0}", log level FINER, and the method
     * and class of the calling method, and parameter is logged.
     * <p>
     * 
     * @param param1 parameter to the method being entered
     */
    public boolean entering(Object param1) {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.entering(ste.getClassName(), ste.getMethodName(), param1);
        return true;
    }

    /**
     * Log a method return, with no result.
     * <p>
     * This is a convenience method that can be used to log returning from a
     * method. A LogRecord with message "RETURN {0}", log level FINER, and the
     * given sourceMethod, sourceClass, and result object is logged.
     * <p>
     * 
     * @param result Object that is being returned
     */
    public boolean exiting(Object result) {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.exiting(ste.getClassName(), ste.getMethodName(), result);
        return true;
    }

    /**
     * Log a method return, with no result.
     * <p>
     * This is a convenience method that can be used to log returning from a
     * method. A LogRecord with message "RETURN", log level FINER, and the
     * method and class of the calling method is logged.
     * <p>
     *  
     */
    public boolean exiting() {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.exiting(ste.getClassName(), ste.getMethodName());
        return true;
    }

    /**
     * Log throwing an exception.
     * <p>
     * This is a convenience method to log that a method is terminating by
     * throwing an exception. The logging is done using the FINER level.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * method and class of the calling method are stored in a LogRecord which
     * is forwarded to all registered output handlers. The LogRecord's message
     * is set to "THROW".
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property. Thus is it
     * processed specially by output Formatters and is not treated as a
     * formatting parameter to the LogRecord message property.
     * <p>
     * 
     * @param thrown The Throwable that is being thrown.
     */
    public boolean throwing(Throwable thrown) {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.throwing(ste.getClassName(), ste.getMethodName(), thrown);
        return true;
    }

    /**
     * Log lock aquisition attempt.
     * <p>
     * This is a convenience method to log that a method is attempting to
     * aquire a lock on an object. The logging is done using the FINEST level.
     * <p>
     * If the logger is currently enabled for the given message level then the
     * method and class of the calling method are stored in a LogRecord which
     * is forwarded to all registered output handlers.
     * <p>
     * 
     * @param lockObj The Object on which the lock is being attempted.
     */
    public boolean gettingLock(Object lockObj) {
        Thread t = Thread.currentThread();
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.logp(Level.FINEST, ste.getClassName(), ste.getMethodName(),
            t+" attempting to aquire lock on \"{0}\"", lockObj);
        return true;
    }

    /**
     * Log successful lock aquisition.
     * <p>
     * This is a convenience method to log that a method has successfully
     * aquired a lock on an object. The logging is done using the FINEST
     * level.
     * <p>
     * The method name and class of the calling method are stored in a
     * LogRecord which is forwarded to all registered output handlers.
     * <p>
     * 
     * @param lockObj The Object on which the lock is being attempted.
     */
    public boolean gotLock(Object lockObj) {
        Thread t = Thread.currentThread();
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.logp(Level.FINEST, ste.getClassName(), ste.getMethodName(),
            t+" successfully aquired lock on \"{0}\"", lockObj);
        return true;
    }

    /**
     * Log lock release.
     * <p>
     * This is a convenience method to log that a method is about to release a
     * lock on an object. The logging is done using the FINEST level.
     * <p>
     * The method name and class of the calling method are stored in a
     * LogRecord which is forwarded to all registered output handlers.
     * <p>
     * 
     * @param lockObj The Object on which the lock is about to be released.
     */
    public boolean releasedLock(Object lockObj) {
        Thread t = Thread.currentThread();
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        logger.logp(Level.FINEST, ste.getClassName(), ste.getMethodName(),
            t+" released lock on \"{0}\"", lockObj);
        return true;
    }

}