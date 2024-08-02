// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.namespace.QName;
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

    /**
     * Method that validator can call to figure out which xml version
     * document being validated declared (if none, "1.0" is assumed as
     * per xml specifications 1.0 and 1.1).
     *
     * @return Xml version of the document: currently has to be either
     *   "1.0" or "1.1".
     */
    String getXmlVersion();

    /*
    ///////////////////////////////////////////////////////////////////////
    // Input element stack access
    ///////////////////////////////////////////////////////////////////////
     */

    /**
     * Method that can be used to access name information of the
     * innermost (top) element in the element stack.
     *
     * @return Name of the element at the top of the current element
     *   stack, if any. During validation calls it refers to the
     *   element being processed (start or end tag), or its parent
     *   (when processing text nodes), or null (in document prolog
     *   and epilog).
     */
    QName getCurrentElementName();

    /**
     * Method that can be called by the validator to resolve a namespace
     * prefix of the currently active top-level element. This may be
     * necessary for things like DTD validators (which may need to
     * heuristically guess proper namespace URI of attributes, esp.
     * ones with default values).
     */
    String getNamespaceURI(String prefix);

    /**
     * This method returns number of attributes accessible from within
     * currently active start element.
     *<p>
     * Note: this method is only guaranteed to be callable during execution
     * of {@link XMLValidator} methods
     * {@link XMLValidator#validateElementStart},
     * {@link XMLValidator#validateAttribute} and
     * {@link XMLValidator#validateElementAndAttributes}. At other times
     * implementations may choose to allow it to be called (for example,
     * with information regarding last start element processed), to throw
     * a {@link IllegalArgumentException}, or to return 0 to indicate no
     * attribute information is available.
     *<p>
     * Also note that whether defaulted attributes (attributes for which
     * values are only available via attribute defaulting) are accessible
     * depends on exact time when this method is called, and in general
     * can not be assumed to function reliably.
     *
     * @return Number of attributes accessible for the currently active
     *   start element.
     */
    int getAttributeCount();

    String getAttributeLocalName(int index);

    String getAttributeNamespace(int index);

    String getAttributePrefix(int index);

    String getAttributeValue(int index);

    String getAttributeValue(String nsURI, String localName);

    String getAttributeType(int index);

    /**
     * @return Index of the specified attribute, if one present;
     *   -1 otherwise.
     */
    int findAttributeIndex(String nsURI, String localName);

    /*
    ///////////////////////////////////////////////////////////////////////
    // Access to notation and unparsed entity information
    ///////////////////////////////////////////////////////////////////////
     */

    /**
     * @return True, if a notation with specified name has been declared
     *   in the document being validated; false if not.
     */
    boolean isNotationDeclared(String name);

    /**
     * @return True, if an unparsed entity with specified name has
     *   been declared
     *   in the document being validated; false if not.
     */
    boolean isUnparsedEntityDeclared(String name);

    /*
    ///////////////////////////////////////////////////////////////////////
    // Location information, error reporting
    //////////////////////////////////////////////////////
     */

    /**
     * @return Base URI active in the current location of the document
     *   being validated, if known; null to indicate no base URI known.
     */
    String getBaseUri();

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

    /**
     * An optional method that can be used to add a new attribute value for
     * an attribute
     * that was not yet contained by the container, as part of using attribute
     * default value mechanism. Optional here means that it is possible that
     * no operation is actually done by the context object. This would be
     * the case, for example, when validation is done on the writer side:
     * since default attributes are implied by a DTD, they should not be
     * added to the output.
     *<p>
     * Note: caller has to ensure that the addition would not introduce a
     * duplicate; attribute container implementation is not required to do
     * any validation on attribute name (local name, prefix, uri) or value.
     *
     * @return Index of the newly added attribute, if operation was
     *    succesful; -1 if not.
     * @throws XMLStreamException
     */
    int addDefaultAttribute(String localName, String uri, String prefix, String value) throws XMLStreamException;
}
