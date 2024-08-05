// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import javax.xml.stream.XMLStreamException;

/**
 * Defines the API that validator schema instances have to implement. Schema
 * objects are results of parsing of input that defines validation rules;
 * things like DTD files, W3c Schema input documents and so on. Schema
 * instances can not be directly used for validation; they are blueprints
 * for constructing such validator Objects. Because of this, they are
 * also guaranteed to be thread-safe and reusable. One way to think of this
 * is that schemas are actual validator factories instead of
 * {@link XMLValidationSchemaFactory} instances.
 *<p>
 * One note about creation of validator instances: since the validation
 * may be invoked from wide variety of contexts (from parser, from serializer,
 * from processing pipeline etc), the validation context is abstracted
 * as {@link ValidationContext}. Instances may make use of additional
 * knowledge about actual implementing classes if they can safely determine
 * the type runtime, but should gracefully handle the cases where
 * the context is created by a caller that is not part of the same
 * StAX implementation as the validator.
 */
public interface XMLValidationSchema {
    // // // Constants defining standard Schema types:

    String SCHEMA_ID_DTD = "http://www.w3.org/XML/1998/namespace";
    String SCHEMA_ID_RELAXNG = "http://relaxng.org/ns/structure/0.9";
    String SCHEMA_ID_W3C_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    String SCHEMA_ID_TREX = "http://www.thaiopensource.com/trex";

    XMLValidator createValidator(ValidationContext ctxt) throws XMLStreamException;

    /*
    ///////////////////////////////////////////////////
    // Configuration, properties
    ///////////////////////////////////////////////////
     */

}
