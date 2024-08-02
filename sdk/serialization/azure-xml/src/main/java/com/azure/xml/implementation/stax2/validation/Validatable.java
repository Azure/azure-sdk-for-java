// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.XMLStreamException;

/**
 * Interface that defines API that Stax2 validation system exposes to the
 * applications. It is implemented by stream readers and writers.
 *<p>
 * Interface defines methods for starting and stopping
 * validation against specific schemas, or validator instances, as well
 * as method(s) for adding an optional custom problem handler.
 */
public interface Validatable {
    /**
     * Method that will construct a {@link XMLValidator} instance from the
     * given schema (unless a validator for that schema has already been
     * added),
     * initialize it if necessary, and make validatable object (reader,
     * writer)
     * call appropriate validation methods from this point on until the
     * end of the document (that is, it's not scoped with sub-trees), or until
     * validator is removed by an explicit call to
     * {@link #stopValidatingAgainst}.
     *<p>
     * Note that while this method can be called at any point in output
     * processing, validator instances are not required to be able to handle
     * addition at other points than right before outputting the root element.
     *
     * @return Validator instance constructed, if validator was added, or null
     *   if a validator for the schema has already been constructed.
     */
    XMLValidator validateAgainst(XMLValidationSchema schema) throws XMLStreamException;

    /**
     * Method that can be called by application to stop validating
     * output against a schema, for which {@link #validateAgainst}
     * was called earlier.
     *
     * @return Validator instance created from the schema that was removed,
     *   if one was in use; null if no such schema in use.
     */
    XMLValidator stopValidatingAgainst(XMLValidationSchema schema) throws XMLStreamException;

    /**
     * Method that can be called by application to stop validating
     * output using specified validator. The validator passed should be
     * an earlier return value for a call to {@link #validateAgainst}.
     *<p>
     * Note: the specified validator is compared for identity with validators
     * in use, not for equality.
     *
     * @return Validator instance found (ie. argument <code>validator</code>)
     *   if it was being used for validating current document; null if not.
     */
    XMLValidator stopValidatingAgainst(XMLValidator validator) throws XMLStreamException;

    /**
     * Method that application can call to define a custom handler for
     * validation problems encountered during validation process.
     *
     * @param h Handler to install, if non null; if null, indicates that
     *   the default (implementation-specific) handling should be used
     *
     * @return Previously set validation problem handler, if any; null
     *   if none was set
     */
    ValidationProblemHandler setValidationProblemHandler(ValidationProblemHandler h);
}
