// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

/**
 * Specialized sub-class of {@link XMLStreamException}, to be used for
 * indicating fatal validation problems (when in mode in which exceptions
 * are to be thrown).
 *<p>
 * Note: constructors are protected, since direct instantiation should
 * be done using factory methods. Reason for this is that the base
 * {@link XMLStreamException} has less than robust handling of optional
 * arguments, and thus factory methods of this class can take care to
 * choose appropriate constructors to call, to make sure super-class does
 * not barf (NPE or such).
 */
public class XMLValidationException extends XMLStreamException {
    private static final long serialVersionUID = 1L;

    protected XMLValidationProblem mCause;

    // // Constructors are protected; sub-classes need to know what
    // // they are doing

    protected XMLValidationException(XMLValidationProblem cause, String msg) {
        super(msg);
        if (cause == null) {
            throwMissing();
        }
        mCause = cause;
    }

    protected XMLValidationException(XMLValidationProblem cause, String msg, Location loc) {
        super(msg, loc);
        if (cause == null) {
            throwMissing();
        }
        mCause = cause;
    }

    // // // Factory methods

    // // // Accessors

    /**
     * @return Object that has all information about details of the
     *   validation problem
     */
    public XMLValidationProblem getValidationProblem() {
        return mCause;
    }

    // // // Internal

    protected static void throwMissing() throws RuntimeException {
        throw new IllegalArgumentException("Validation problem argument can not be null");
    }
}
