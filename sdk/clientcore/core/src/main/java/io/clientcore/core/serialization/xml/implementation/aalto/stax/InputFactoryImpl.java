// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.clientcore.core.serialization.xml.implementation.aalto.stax;

import io.clientcore.core.serialization.xml.implementation.aalto.in.CharSourceBootstrapper;
import io.clientcore.core.serialization.xml.implementation.aalto.in.ReaderConfig;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import java.io.InputStream;
import java.io.Reader;

/**
 * Aalto implementation of basic Stax factory ({@link javax.xml.stream.XMLInputFactory})
 *
 * @author Tatu Saloranta
 */
public final class InputFactoryImpl extends XMLInputFactory {
    /*
    /**********************************************************************
    /* Additional standard configuration properties
    /**********************************************************************
     */

    // // // Parsing settings

    /**
     * Whether cursor-based reader will ever generate CDATA events; if true,
     * CDATA events may be generated for non-coalesced CDATA sections. If
     * false, all CDATA sections are reported as CHARACTERS types. It may
     * still be possible for event methods to distinguish between underlying
     * type, but event type code will be reported as CHARACTERS.
     *<p>
     * State of property does not have any effect on performance.
     *<p>
     * Default value for this setting is implementation dependant.
     */
    public final static String P_REPORT_CDATA = "http://java.sun.com/xml/stream/properties/report-cdata-event";

    // // // Optimization settings

    /**
     * Whether name symbols (element, attribute, entity and notation names,
     * namespace prefixes)
     * stream reader returns are guaranteed to have been String.intern()ed.
     * Interning generally makes access faster (both internal and externally),
     * and saves memory, but can add some overhead for processing.
     * It may also be problematic for large symbol spaces; especially
     * if xml content has unbounded value space for names.
     *<p>
     * Default value for this setting is implementation dependant.
     * Additionally implementations may have use different default for
     * different types of stream readers.
     */
    public final static String P_INTERN_NAMES = "io.clientcore.core.serialization.xml.implementation.stax2.internNames";

    /**
     * Whether namespace URIs
     * stream reader returns are guaranteed to have been String.intern()ed.
     * Interning can make access by fully-qualified name faster as well
     * as save memory, but it can also add
     * some overhead when encountering a namespace URI for the first
     * time.
     *<p>
     * Default value for this setting is implementation dependant.
     */
    public final static String P_INTERN_NS_URIS
        = "io.clientcore.core.serialization.xml.implementation.stax2.internNsUris";

    /**
     * Property that determines whether stream reader instances are required
     * to try to keep track of the parser Location in the input documents.
     *<p>
     * When turned on, the stream reader should try to do its best to keep
     * track of the locations, to be able to properly create
     * <code>XMLEvent</code> objects with accurate Location information.
     * Similarly, implementation should keep track of the location for
     * error reporting purposes, and include this information within
     * <code>XMLStreamException</code> instances.
     *<p>
     * When turned off, implementations are allowed to optimize things,
     * and only keep/pass partial Location information, or even none at
     * all. Implementations are still encouraged to keep some location
     * information for error reporting purposes, even if they do not
     * maintain accurate
     * <code>XMLEvent</code> locations, or exact byte/character offsets.
     *<p>
     * Default value for this setting is true.
     */
    public final static String P_PRESERVE_LOCATION
        = "io.clientcore.core.serialization.xml.implementation.stax2.preserveLocation";

    /**
     * This is the currently active configuration that will be used
     * for readers created by this factory.
     */
    final ReaderConfig _config;

    /*
    /**********************************************************************
    /* Life-cycle:
    /**********************************************************************
     */

    public InputFactoryImpl() {
        _config = new ReaderConfig();
    }

    /*
    /**********************************************************************
    /* Stax, XMLInputFactory: filtered reader factory methods
    /**********************************************************************
     */

    // // // Filtered reader factory methods

    @Override
    public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) {
        throw new UnsupportedOperationException();
    }

    /*
    /**********************************************************************
    /* Stax, XMLInputFactory: XMLEventReader factory methods
    /**********************************************************************
     */

    @Override
    public XMLEventReader createXMLEventReader(InputStream in) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventReader createXMLEventReader(InputStream in, String enc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventReader createXMLEventReader(Reader r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventReader createXMLEventReader(javax.xml.transform.Source source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, InputStream in) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, Reader r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventReader createXMLEventReader(XMLStreamReader sr) {
        throw new UnsupportedOperationException();
    }

    /*
    /**********************************************************************
    /* Stax, XMLInputFactory: XMLStreamReader factory methods
    /**********************************************************************
     */

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream in) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream in, String enc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamReader createXMLStreamReader(Reader r) throws XMLStreamException {
        return constructSR(r);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, Reader r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamReader createXMLStreamReader(javax.xml.transform.Source src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream in) {
        throw new UnsupportedOperationException();
    }

    /*
    /**********************************************************************
    /* Stax, XMLInputFactory; generic accessors/mutators
    /**********************************************************************
     */

    @Override
    public Object getProperty(String name) {
        // false -> is mandatory, unrecognized will throw IllegalArgumentException
        return _config.getProperty(name, true);
    }

    @Override
    public void setProperty(String propName, Object value) {
        _config.setProperty(propName, value);
    }

    @Override
    public XMLEventAllocator getEventAllocator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLReporter getXMLReporter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLResolver getXMLResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPropertySupported(String name) {
        return _config.isPropertySupported(name);
    }

    @Override
    public void setEventAllocator(XMLEventAllocator allocator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setXMLReporter(XMLReporter r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setXMLResolver(XMLResolver r) {
        throw new UnsupportedOperationException();
    }

    /*
    /**********************************************************************
    /* Internal/package methods
    /**********************************************************************
     */

    private XMLStreamReader constructSR(Reader r) throws XMLStreamException {
        ReaderConfig cfg = _config.createNonShared();
        return StreamReaderImpl.construct(CharSourceBootstrapper.construct(cfg, r));
    }

}
