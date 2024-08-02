// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.Location;

/**
 * Simple container class used to store a non-fatal problem
 * either to be returned as is, or to use for creating and throwing
 * a validation exception.
 */
public class XMLValidationProblem {
    public final static int SEVERITY_WARNING = 1;
    public final static int SEVERITY_ERROR = 2;
    public final static int SEVERITY_FATAL = 3;

    /**
     * Location within validated xml stream where the problem occured.
     */
    protected Location mLocation;

    protected final String mMessage;

    protected final int mSeverity;

    /**
     * Type of the problem, available types implementation specified.
     *
     * @since 3.0
     */
    protected String mType;

    /**
     * Validator instance that reported the problem, if any (may be
     * null if stream reader or writer itself reported the problem)
     *
     * @since 3.0
     */
    protected XMLValidator mReporter;

    public XMLValidationProblem(Location loc, String msg) {
        this(loc, msg, SEVERITY_ERROR);
    }

    public XMLValidationProblem(Location loc, String msg, int severity) {
        this(loc, msg, severity, null);
    }

    public XMLValidationProblem(Location loc, String msg, int severity, String type) {
        mLocation = loc;
        mMessage = msg;
        mSeverity = severity;
        mType = type;
    }

    /**
     * Convenience method for constructing a {@link XMLValidationException}
     * to throw based on information contained in this object.
     * Base implementation is equivalent to:
     *<pre>
     *  return XMLValidationException.createException(this);
     *</pre>
     *
     * @since 3.0
     */
    public XMLValidationException toException() {
        return XMLValidationException.createException(this);
    }

    /**
     * @since 3.0
     */
    public void setType(String t) {
        mType = t;
    }

    /**
     * @since 3.0
     */
    public void setLocation(Location l) {
        mLocation = l;
    }

    /**
     * Set the validator object that reported this problem, if known.
     *
     * @since 3.0
     */
    public void setReporter(XMLValidator v) {
        mReporter = v;
    }

    /**
     * @return Reference to location where problem was encountered.
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * @return Human-readable message describing the problem 
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * @return One of <code>SEVERITY_</code> constants
     *   (such as {@link #SEVERITY_WARNING}
     */
    public int getSeverity() {
        return mSeverity;
    }

    /**
     * @return Generic type (class) of the problem; may be null
     *   if validator does not provide such details
     *
     * @since 3.0
     */
    public String getType() {
        return mType;
    }

    /**
     * Returns the validator that reported the problem if known.
     *
     * @return Validator that reported the problem; null if unknown or N/A.
     *
     * @since 3.0
     */
    public XMLValidator getReporter() {
        return mReporter;
    }
}
