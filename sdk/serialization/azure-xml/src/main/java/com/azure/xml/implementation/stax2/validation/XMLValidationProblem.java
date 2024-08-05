// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.Location;

/**
 * Simple container class used to store a non-fatal problem
 * either to be returned as is, or to use for creating and throwing
 * a validation exception.
 */
public class XMLValidationProblem {
    public final static int SEVERITY_ERROR = 2;

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

}
