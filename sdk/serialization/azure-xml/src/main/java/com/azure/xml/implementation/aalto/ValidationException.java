// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.stax2.validation.XMLValidationException;
import com.azure.xml.implementation.stax2.validation.XMLValidationProblem;

/**
 * Specific exception thrown when document has validation (DTD, W3C Schema)
 * errors; things that are not well-formedness problems.
 *<p>
 * The current implementation does not add much beyond basic
 * {@link XMLValidationException}, except for fixing some problems that
 * underlying {@link XMLStreamException} has.
 *<p>
 * Note that some of the code is shared with other sub-classes.
 * Unfortunately it is not possible to extend it, since they extend
 * {@link XMLStreamException}, not {@link XMLValidationException}.
 */
@SuppressWarnings("serial")
public class ValidationException extends XMLValidationException {
    protected ValidationException(XMLValidationProblem cause, String msg) {
        super(cause, msg);
    }

    protected ValidationException(XMLValidationProblem cause, String msg, Location loc) {
        super(cause, msg, loc);
    }

    public static ValidationException create(XMLValidationProblem cause) {
        // Should always get a message
        Location loc = cause.getLocation();
        if (loc == null) {
            return new ValidationException(cause, cause.getMessage());
        }
        return new ValidationException(cause, cause.getMessage(), loc);
    }

    /*
    /////////////////////////////////////////////////////////
    // Overridden methods from XMLStreamException
    /////////////////////////////////////////////////////////
     */

    /**
     * Method is overridden for two main reasons: first, default method
     * does not display public/system id information, even if it exists, and
     * second, default implementation can not handle nested Location
     * information.
     */
    @Override
    public String getMessage() {
        String locMsg = getLocationDesc();
        /* Better not use super's message if we do have location information,
         * since parent's message contains (part of) Location
         * info; something we can regenerate better...
         */
        if (locMsg == null) {
            return super.getMessage();
        }
        String msg = getValidationProblem().getMessage();
        return msg + '\n' + " at " + locMsg;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + getMessage();
    }

    /*
    ////////////////////////////////////////////////////////
    // Internal methods:
    ////////////////////////////////////////////////////////
     */

    protected String getLocationDesc() {
        Location loc = getLocation();
        return (loc == null) ? null : loc.toString();
    }
}
