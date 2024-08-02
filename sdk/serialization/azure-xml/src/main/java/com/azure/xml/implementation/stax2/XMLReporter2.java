// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2;

import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.stax2.validation.XMLValidationProblem;

/**
 * Extension of {@link XMLReporter} to allow for better access to
 * information about the actual problem.
 *<p>
 * Note on type of problems reported: although type is
 * {@link XMLValidationProblem}, it is not strictly related to (optional)
 * validation process. That is, non-fatal problems related to well-formedness
 * (mostly in areas of DTD definition, or in some cases problems that would
 * be fatal normally but have been suppressed by the calling app) will
 * also be reported through this interface if registered.
 *<p>
 * Stax2 implementations are encouraged to always try to call the improved
 * <code>report</code> method, and only call the base interface version if
 * registered report is not of type {@link XMLReporter2}.
 *
 * @since 3.0
 */
public interface XMLReporter2 extends XMLReporter {
    // From base interface:
    //public void report(String message, String errorType, Object relatedInformation, Location location)

    /**
     * Reporting method called with reference to object that defines
     * exact problem being encountered. Implementor is free to
     * quietly handle the problem, or to throw an exception
     * to cause abnormal termination of xml processing.
     */
    void report(XMLValidationProblem problem) throws XMLStreamException;
}
