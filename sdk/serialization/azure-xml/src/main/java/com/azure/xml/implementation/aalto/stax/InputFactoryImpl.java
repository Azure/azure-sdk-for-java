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

package com.azure.xml.implementation.aalto.stax;

import com.azure.xml.implementation.aalto.in.ByteSourceBootstrapper;
import com.azure.xml.implementation.aalto.in.CharSourceBootstrapper;
import com.azure.xml.implementation.aalto.in.ReaderConfig;
import com.azure.xml.implementation.stax2.XMLInputFactory2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * Aalto implementation of basic Stax factory (both
 * {@link javax.xml.stream.XMLInputFactory} and {@link com.azure.xml.implementation.stax2.XMLInputFactory2})
 *
 * @author Tatu Saloranta
 */
public final class InputFactoryImpl extends XMLInputFactory2 {
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
    /* Stax, XMLInputFactory: XMLStreamReader factory methods
    /**********************************************************************
     */

    public XMLStreamReader createXMLStreamReader(InputStream in) throws XMLStreamException {
        return constructSR(in);
    }

    public XMLStreamReader createXMLStreamReader(Reader r) throws XMLStreamException {
        return constructSR(r);
    }

    /*
    /**********************************************************************
    /* Stax, XMLInputFactory; generic accessors/mutators
    /**********************************************************************
     */

    public Object getProperty(String name) {
        // false -> is mandatory, unrecognized will throw IllegalArgumentException
        return _config.getProperty(name, true);
    }

    public void setProperty(String propName, Object value) {
        _config.setProperty(propName, value);
    }

    /*
    /**********************************************************************
    /* Internal/package methods
    /**********************************************************************
     */

    /**
     * Method called when a non-shared copy of the current configuration
     * is needed. This is usually done when a new reader is constructed.
     */
    public ReaderConfig getNonSharedConfig(String extEncoding) {
        return _config.createNonShared(extEncoding);
    }

    private XMLStreamReader constructSR(InputStream in) throws XMLStreamException {
        ReaderConfig cfg = getNonSharedConfig(null);
        return StreamReaderImpl.construct(ByteSourceBootstrapper.construct(cfg, in));
    }

    private XMLStreamReader constructSR(Reader r) throws XMLStreamException {
        ReaderConfig cfg = getNonSharedConfig(null);
        return StreamReaderImpl.construct(CharSourceBootstrapper.construct(cfg, r));
    }

}
