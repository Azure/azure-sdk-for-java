// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.osgi;

import com.azure.xml.implementation.stax2.XMLStreamProperties;
import com.azure.xml.implementation.stax2.validation.XMLValidationSchemaFactory;

/**
 * Simple interface to be used for registering objects that
 * can construct {@link XMLValidationSchemaFactory} instances with OSGi framework.
 * The added indirection (provider constructing factory) is needed because
 * of impedance between OSGi service objects (which are essentially
 * singletons) and Stax/Stax2 factories which are not.
 *<p>
 * Note: implementations of provider should <b>NOT</b> use introspection
 * via {@link XMLValidationSchemaFactory#newInstance} as it will
 * not work with OSGi. Instead, providers should directly construct
 * instances of concrete factory they represent. That is, there will
 * be one provider implementation per concrete Stax/Stax2 implementation
 */
public interface Stax2ValidationSchemaFactoryProvider {
    /*
    ///////////////////////////////////////////////////////////////
    // Service property names to use with the provider
    ///////////////////////////////////////////////////////////////
     */

    /**
     * Service property that defines name of Stax2 implementation that
     * this provider represents.
     */
    String OSGI_SVC_PROP_IMPL_NAME = XMLStreamProperties.XSP_IMPLEMENTATION_NAME;

    /**
     * Service property that defines version of Stax2 implementation that
     * this provider represents.
     */
    String OSGI_SVC_PROP_IMPL_VERSION = XMLStreamProperties.XSP_IMPLEMENTATION_VERSION;

    /**
     * Service property that defines type of Schemas (one of constants from
     * {@link com.azure.xml.implementation.stax2.validation.XMLValidationSchema},
     * such as {@link com.azure.xml.implementation.stax2.validation.XMLValidationSchema#SCHEMA_ID_DTD})
     * that the schema factory this provider handles supports. Can be used
     * to locate proper provider for the schema type.
     */
    String OSGI_SVC_PROP_SCHEMA_TYPE = "com.azure.xml.implementation.stax2.validation.schemaType";

    /*
    ///////////////////////////////////////////////////////////////
    // Public provider API
    ///////////////////////////////////////////////////////////////
     */

    /**
     * Method that can be used to determine which schema type this
     * provider (or, rather, factory instances of which provider
     * constructs) supports.
     *
     * @return Id of schema type that the factory instantiated by this
     *   provider will support.
     */
    String getSchemaType();

    /**
     * Method called to create a new {@link XMLValidationSchemaFactory}
     * instance. Each schema factory supports a single schema type;
     * so caller has to ensure that the factory it is using supports
     * schema it needs to instantiate. This can be done either by using
     * service properties, or by inspecting provider instances for
     * schema type they support (see {@link #getSchemaType}).
     *
     * @return ValidationSchema factory configured to implementation-specific
     *   default settings, if type is supported by this provider; null
     *   if not.
     */
    XMLValidationSchemaFactory createValidationSchemaFactory();
}
