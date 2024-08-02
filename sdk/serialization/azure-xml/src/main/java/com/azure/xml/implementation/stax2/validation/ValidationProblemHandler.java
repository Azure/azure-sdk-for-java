// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

/**
 * This interface defines method(s) needed to implement a custom validation
 * problem (warning, error, fatal error) handler. Such handlers can be used,
 * for example, to collect validation problems without automatically
 * throwing exceptions. It can also be used to throw exception for problems
 * that by default would only be reported as warnings.
 */
public interface ValidationProblemHandler {
    /**
     * Method called by validator, when a validation problem is encountered.
     * Impementations can choose to ignore the problem, log something about
     * the problem, store it for later processing, or throw
     * a {@link XMLValidationException}.
     *
     * @param problem Validation problem encountered.
     */
    void reportProblem(XMLValidationProblem problem) throws XMLValidationException;
}
