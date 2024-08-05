// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

/**
 * Interface that defines functionality exposed by the "owner" of the
 * content to validate (usually a stream reader or stream writer) to
 * validators, needed in addition to actually validatable content, for
 * succesful validation. It also defines methods needed for infoset
 * augmentation some validators do, such as adding default values
 * to attributes.
 * Some of functionality is optional (for example, writer
 * may not have any useful location information).
 *<p>
 * The functionality included is close to the minimal subset of
 * functionality needed to support 3 main streamable schema languages
 * (DTD, W3C Schema, Relax NG).
 */
public interface ValidationContext {
    /*
    ///////////////////////////////////////////////////////////////////////
    // Basic configuration
    ///////////////////////////////////////////////////////////////////////
     */

    /*
    ///////////////////////////////////////////////////////////////////////
    // Input element stack access
    ///////////////////////////////////////////////////////////////////////
     */

    /**
     * Method that can be called by the validator to resolve a namespace
     * prefix of the currently active top-level element. This may be
     * necessary for things like DTD validators (which may need to
     * heuristically guess proper namespace URI of attributes, esp.
     * ones with default values).
     */
    String getNamespaceURI(String prefix);

    /*
    ///////////////////////////////////////////////////////////////////////
    // Access to notation and unparsed entity information
    ///////////////////////////////////////////////////////////////////////
     */

    /*
    ///////////////////////////////////////////////////////////////////////
    // Location information, error reporting
    //////////////////////////////////////////////////////
     */

    /**
     * Method that will return the location that best represents current
     * location within document to be validated, if such information
     * is available.
     *<p>
     * Note: it is likely that even when a location is known, it may not
     * be very accurate; for example, when attributes are validated, it
     * is possible that they all would point to a single location that
     * may point to the start of the element that contains attributes.
     */
    Location getValidationLocation();

    /**
     * Method called by the validator, upon encountering a validation
     * problem. Implementations are encouraged to allow an optional
     * {@link ValidationProblemHandler} be set by the application,
     * to define handling.
     *<p>
     * Note: Stax2 version 2 only allowed throwing instances
     * of {@link XMLValidationProblem}; version 3 allows generic
     * base class to be thrown, to support other interfaces such
     * as basic Stax interface {@link javax.xml.stream.XMLReporter}.
     */
    void reportProblem(XMLValidationProblem problem) throws XMLStreamException;

    /*
    ///////////////////////////////////////////////////////////////////////
    // Infoset modifiers
    ///////////////////////////////////////////////////////////////////////
     */

}
