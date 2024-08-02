// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.validation;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

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

    /**
     * Name of resource file that contains JAXP properties.
     */
    final static String JAXP_PROP_FILENAME = "jaxp.properties";

    /**
     * Defines the system property that can be set to explicitly specify
     * which implementation to use (in case there are multiple StAX2
     * implementations; or the one used does not specify other mechanisms
     * for the loader to find the implementation class).
     */
    public final static String SYSTEM_PROPERTY_FOR_IMPL
        = "com.azure.xml.implementation.stax2.validation.XMLValidationSchemaFactory.";

    /**
     * Path to resource that should contain implementation class definition.
     */
    public final static String SERVICE_DEFINITION_PATH = "META-INF/services/" + SYSTEM_PROPERTY_FOR_IMPL;

    // // // Names of standard configuration properties

    /**
     * Property that determines whether schemas constructed are namespace-aware,
     * in cases where schema supports both namespace-aware and non-namespace
     * aware modes. In general this only applies to DTDs, since namespace
     * support for DTDs is both optional, and not well specified.
     *<p>
     * Default value is TRUE. For schema types for which only one value
     * (usually TRUE) is allowed, this property will be ignored.
     */
    public static final String P_IS_NAMESPACE_AWARE = "org.codehaus2.stax2.validation.isNamespaceAware";

    /**
     * Property that determines whether schema instances created by this
     * factory instance can be cached by the factory; if false, no caching
     * is allowed to be doe; if true, factory can do caching if it wants to.
     * The exact rules used to determine unique id of schema instances is
     * factory dependant; it is expected that the implementations use
     * implementation based on unified system ids (serialized URLs or such).
     */
    public static final String P_ENABLE_CACHING = "org.codehaus2.stax2.validation.enableCaching";

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

    /**
     * Creates a new XMLValidationFactory instance, using the default
     * instance configuration mechanism.
     */
    public static XMLValidationSchemaFactory newInstance(String schemaType) throws FactoryConfigurationError {
        return newInstance(schemaType, Thread.currentThread().getContextClassLoader());
    }

    public static XMLValidationSchemaFactory newInstance(String schemaType, ClassLoader classLoader)
        throws FactoryConfigurationError {
        // Let's check and map schema type to the shorter internal id:
        String internalId = sSchemaIds.get(schemaType);
        if (internalId == null) {
            throw new FactoryConfigurationError("Unrecognized schema type (id '" + schemaType + "')");
        }

        String propertyId = SYSTEM_PROPERTY_FOR_IMPL + internalId;
        SecurityException secEx = null;

        // First, let's see if there's a system property (overrides other settings)
        try {
            String clsName = System.getProperty(propertyId);
            if (clsName != null && !clsName.isEmpty()) {
                return createNewInstance(classLoader, clsName);
            }
        } catch (SecurityException se) {
            // May happen on sandbox envs, like applets?
            secEx = se;
        }

        /* try to read from $java.home/lib/xml.properties (simulating
         * the way XMLInputFactory does this... not sure if this should
         * be done, as this is not [yet?] really jaxp specified)
         */
        try {
            String home = System.getProperty("java.home");
            File f = new File(home);

            // Let's not hard-code separators...
            f = new File(f, "lib");
            f = new File(f, JAXP_PROP_FILENAME);
            if (f.exists()) {
                Properties props = new Properties();
                try {
                    // TODO: 15-Jan-2020, tatu -- when upgrading baseline to Java 7+,
                    // use try-with-resource instead
                    try (FileInputStream in = new FileInputStream(f)) {
                        props.load(in);
                    }
                    String clsName = props.getProperty(propertyId);
                    if (clsName != null && !clsName.isEmpty()) {
                        return createNewInstance(classLoader, clsName);
                    }
                } catch (IOException ioe) {
                    // can also happen quite easily...
                }
            }
        } catch (SecurityException se) {
            // Fine, as above
            secEx = se;
        }

        /* Ok, no match; how about a service def from the impl jar?
         */
        // try to find services in CLASSPATH
        String path = SERVICE_DEFINITION_PATH + internalId;
        try {
            Enumeration<?> en;

            if (classLoader == null) {
                en = ClassLoader.getSystemResources(path);
            } else {
                en = classLoader.getResources(path);
            }

            if (en != null) {
                while (en.hasMoreElements()) {
                    URL url = (URL) en.nextElement();
                    InputStream is = url.openStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
                    String clsName = null;
                    String line;

                    try {
                        while ((line = rd.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty() && line.charAt(0) != '#') {
                                clsName = line;
                                break;
                            }
                        }
                    } finally {
                        rd.close();
                    }
                    if (clsName != null) {
                        return createNewInstance(classLoader, clsName);
                    }
                }
            }
        } catch (SecurityException se) {
            secEx = se;
        } catch (IOException ex) {
            /* Let's assume these are mostly ok, too (missing jar ie.)
             */
        }

        String msg = "No XMLValidationSchemaFactory implementation class specified or accessible (via system property '"
            + propertyId + "', or service definition under '" + path + "')";

        if (secEx != null) {
            throw new FactoryConfigurationError(msg + " (possibly caused by: " + secEx + ")", secEx);
        }
        throw new FactoryConfigurationError(msg);
    }

    // // // And then actual per-instance factory methods

    public XMLValidationSchema createSchema(InputStream in) throws XMLStreamException {
        return createSchema(in, null);
    }

    public XMLValidationSchema createSchema(InputStream in, String encoding) throws XMLStreamException {
        return createSchema(in, encoding, null, null);
    }

    public abstract XMLValidationSchema createSchema(InputStream in, String encoding, String publicId, String systemId)
        throws XMLStreamException;

    public XMLValidationSchema createSchema(Reader r) throws XMLStreamException {
        return createSchema(r, null, null);
    }

    public abstract XMLValidationSchema createSchema(Reader r, String publicId, String systemId)
        throws XMLStreamException;

    public abstract XMLValidationSchema createSchema(URL url) throws XMLStreamException;

    public abstract XMLValidationSchema createSchema(File f) throws XMLStreamException;

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

    /**
     * @return Name of schema type (one of <code>SCHEMA_ID_xxx</code>
     *    constants) that this factory supports.
     */
    public final String getSchemaType() {
        return mSchemaType;
    }

    /*
    ///////////////////////////////////////////////////////
    // Internal methods
    ///////////////////////////////////////////////////////
     */

    private static XMLValidationSchemaFactory createNewInstance(ClassLoader cloader, String clsName)
        throws FactoryConfigurationError {
        try {
            Class<?> factoryClass;

            if (cloader == null) {
                factoryClass = Class.forName(clsName);
            } else {
                factoryClass = cloader.loadClass(clsName);
            }
            return (XMLValidationSchemaFactory) factoryClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new FactoryConfigurationError(
                "XMLValidationSchemaFactory implementation '" + clsName + "' not found (missing jar in classpath?)", x);
        } catch (Exception x) {
            throw new FactoryConfigurationError(
                "XMLValidationSchemaFactory implementation '" + clsName + "' could not be instantiated: " + x, x);
        }
    }
}
