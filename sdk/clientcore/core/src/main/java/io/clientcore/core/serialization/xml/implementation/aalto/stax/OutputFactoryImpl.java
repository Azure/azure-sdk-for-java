// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/*
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

import io.clientcore.core.serialization.xml.implementation.aalto.out.CharXmlWriter;
import io.clientcore.core.serialization.xml.implementation.aalto.out.StreamWriterBase;
import io.clientcore.core.serialization.xml.implementation.aalto.out.WriterConfig;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Basic implementation of {@link XMLOutputFactory}.
 *
 * @author Tatu Saloranta
 */
public final class OutputFactoryImpl extends XMLOutputFactory {
    /*
    /**********************************************************************
    /* Additional standard configuration properties
    /**********************************************************************
     */

    // // General output options:

    /**
     * Whether stream writers are allowed to automatically output empty
     * elements, when a start element is immediately followed by matching
     * end element.
     * If true, will output empty elements; if false, will always create
     * separate end element (unless a specific method that produces empty
     * elements is called).
     *<p>
     * Default value for implementations should be 'true'; both values should
     * be recognized, and 'false' must be honored. However, 'true' value
     * is only a suggestion, and need not be implemented (since there is
     * the explicit 'writeEmptyElement()' method).
     */
    public final static String P_AUTOMATIC_EMPTY_ELEMENTS
        = "io.clientcore.core.serialization.xml.implementation.stax2.automaticEmptyElements";

    /*
    /**********************************************************************
    /* Actual storage of configuration settings
    /**********************************************************************
     */

    private final WriterConfig _config;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public OutputFactoryImpl() {
        _config = new WriterConfig();
    }

    /*
    /**********************************************************************
    /* XMLOutputFactory API
    /**********************************************************************
     */

    @Override
    public XMLEventWriter createXMLEventWriter(OutputStream out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventWriter createXMLEventWriter(OutputStream out, String enc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventWriter createXMLEventWriter(javax.xml.transform.Result result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEventWriter createXMLEventWriter(Writer w) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream out, String enc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(javax.xml.transform.Result result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(Writer w) {
        return createSW(w);
    }

    @Override
    public Object getProperty(String name) {
        // true -> is mandatory, unrecognized will throw IllegalArgumentException
        return _config.getProperty(name, true);
    }

    @Override
    public boolean isPropertySupported(String name) {
        return _config.isPropertySupported(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        _config.setProperty(name, value);
    }

    /*
    /**********************************************************************
    /* Internal methods:
    /**********************************************************************
     */

    // Bottleneck factory method used internally; needs to take care of passing
    // proper settings to stream writer.
    //
    // @param forceAutoClose Whether writer should automatically close the
    //   output stream or Writer, when close() is called on stream writer.
    private StreamWriterBase createSW(Writer w) {
        // Need to ensure that the configuration object is not shared
        // any more; otherwise later changes via factory could be
        // visible half-way through output...
        WriterConfig cfg = _config.createNonShared();
        CharXmlWriter xw = new CharXmlWriter(w);

        return new StreamWriterBase(cfg, xw, _config.getCharSymbols(xw));
    }
}
