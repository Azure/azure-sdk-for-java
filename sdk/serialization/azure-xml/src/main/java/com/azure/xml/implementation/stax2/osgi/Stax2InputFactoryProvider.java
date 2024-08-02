// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.osgi;

import com.azure.xml.implementation.stax2.XMLInputFactory2;
import com.azure.xml.implementation.stax2.XMLStreamProperties;

/**
 * Simple interface to be used for registering objects that
 * can construct {@link XMLInputFactory2} instances with OSGi framework.
 * The added indirection (provider constructing factory) is needed because
 * of impedance between OSGi service objects (which are essentially
 * singletons) and Stax/Stax2 factories which are not.
 *<p>
 * Note: implementations of provider should <b>NOT</b> use introspection
 * via {@link javax.xml.stream.XMLInputFactory#newInstance} as it will
 * not work with OSGi. Instead, providers should directly construct
 * instances of concrete factory they represent. That is, there will
 * be one provider implementation per concrete Stax/Stax2 implementation
 */
public interface Stax2InputFactoryProvider {
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

    /*
    ///////////////////////////////////////////////////////////////
    // Public provider API
    ///////////////////////////////////////////////////////////////
     */

    /**
     * Method called to create a new {@link XMLInputFactory2} instance.
     *
     * @return Input factory configured to implementation-specific
     *   default settings (some of which are mandated by Stax and Stax2
     *   specifications)
     */
    XMLInputFactory2 createInputFactory();
}
