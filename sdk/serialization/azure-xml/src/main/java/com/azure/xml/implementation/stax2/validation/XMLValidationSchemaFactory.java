// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import java.util.*;

/**
 * Defines an abstract factory for constructing {@link XMLValidationSchema}
 * instances. This abstract base class has methods for instantiating the
 * actual implementation (similar to the way
 * {@link javax.xml.stream.XMLInputFactory} works, and defines the API to
 * use for configuring these instances, as well as factory methods concrete
 * classes implement for actually creating {@link XMLValidationSchema}
 * instances.
 *<p>
 * Note: this class is part of the second major revision of StAX 2 API
 * (StAX2, v2), and is optional for StAX2 implementations to support.
 *
 * @see javax.xml.stream.XMLInputFactory
 * @see org.codehaus.stax2.validation.XMLValidationSchema
 * @see org.codehaus.stax2.XMLInputFactory2
 */
public abstract class XMLValidationSchemaFactory {
    // // // Internal ids matching SCHEMA_ID_ constants from XMLValidationSchema

    public final static String INTERNAL_ID_SCHEMA_DTD = "dtd";
    public final static String INTERNAL_ID_SCHEMA_RELAXNG = "relaxng";
    public final static String INTERNAL_ID_SCHEMA_W3C = "w3c";
    public final static String INTERNAL_ID_SCHEMA_TREX = "trex";

    final static HashMap<String, String> sSchemaIds = new HashMap<>();
    static {
        sSchemaIds.put(XMLValidationSchema.SCHEMA_ID_DTD, INTERNAL_ID_SCHEMA_DTD);
        sSchemaIds.put(XMLValidationSchema.SCHEMA_ID_RELAXNG, INTERNAL_ID_SCHEMA_RELAXNG);
        sSchemaIds.put(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA, INTERNAL_ID_SCHEMA_W3C);
        sSchemaIds.put(XMLValidationSchema.SCHEMA_ID_TREX, INTERNAL_ID_SCHEMA_TREX);
    }

    // // // Properties for locating implementations

    // // // Names of standard configuration properties

    /**
     * Schema type this factory instance supports.
     */
    protected final String mSchemaType;

    /**
     * @param st Schema type this factory supports; one of
     *   <code>SCHEMA_ID_xxx</code> constants.
     */
    protected XMLValidationSchemaFactory(String st) {
        mSchemaType = st;
    }

    /*
    ////////////////////////////////////////////////////////
    // Factory methods
    ////////////////////////////////////////////////////////
    */

    // // // First creating the factory instances:

    // // // And then actual per-instance factory methods

    /*
    ////////////////////////////////////////////////////////
    // Configuration
    ////////////////////////////////////////////////////////
    */

    public abstract boolean isPropertySupported(String propName);

    /**
     * @param propName Name of property to set
     * @param value Value to set property to
     *
     * @return True if setting succeeded; false if property was recognized
     *   but could not be changed to specified value, or if it was not
     *   recognized but the implementation did not throw an exception.
     */
    public abstract boolean setProperty(String propName, Object value);

    public abstract Object getProperty(String propName);

    /*
    ///////////////////////////////////////////////////////
    // Internal methods
    ///////////////////////////////////////////////////////
     */

}
