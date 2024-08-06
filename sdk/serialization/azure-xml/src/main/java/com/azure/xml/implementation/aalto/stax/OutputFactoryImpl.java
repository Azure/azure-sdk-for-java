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

package com.azure.xml.implementation.aalto.stax;

import com.azure.xml.implementation.aalto.out.AsciiXmlWriter;
import com.azure.xml.implementation.aalto.out.CharXmlWriter;
import com.azure.xml.implementation.aalto.out.Latin1XmlWriter;
import com.azure.xml.implementation.aalto.out.NonRepairingStreamWriter;
import com.azure.xml.implementation.aalto.out.Utf8XmlWriter;
import com.azure.xml.implementation.aalto.out.WNameTable;
import com.azure.xml.implementation.aalto.out.WriterConfig;
import com.azure.xml.implementation.aalto.out.XmlWriter;
import com.azure.xml.implementation.aalto.util.CharsetNames;
import com.azure.xml.implementation.aalto.util.XmlConsts;
import com.azure.xml.implementation.stax2.XMLOutputFactory2;
import com.azure.xml.implementation.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Basic implementation of {@link XMLOutputFactory}.
 *
 * @author Tatu Saloranta
 */
public final class OutputFactoryImpl extends XMLOutputFactory2 {
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
    public XMLStreamWriter createXMLStreamWriter(OutputStream out) throws XMLStreamException {
        return createXMLStreamWriter(out, null);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream out, String enc) throws XMLStreamException {
        return createSW(out, null, enc);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(javax.xml.transform.Result result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(Writer w) throws XMLStreamException {
        return createSW(null, w, null);
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
    /* StAX2 extensions
    /**********************************************************************
     */

    // // // StAX2 additional (encoding-aware) factory methods

    // // // StAX2 "Profile" mutators

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
    private XMLStreamWriter2 createSW(OutputStream out, Writer w, String enc) throws XMLStreamException {
        // Need to ensure that the configuration object is not shared
        // any more; otherwise later changes via factory could be
        // visible half-way through output...
        WriterConfig cfg = _config.createNonShared();
        XmlWriter xw;
        WNameTable symbols;

        if (w == null) {
            if (enc == null) {
                enc = XmlConsts.STAX_DEFAULT_OUTPUT_ENCODING;
            } else {
                // Canonical ones are interned, so we may have
                // normalized encoding already...
                if (!enc.equals(CharsetNames.CS_UTF8)
                    && !enc.equals(CharsetNames.CS_ISO_LATIN1)
                    && !enc.equals(CharsetNames.CS_US_ASCII)) {
                    enc = CharsetNames.normalize(enc);
                }
            }

            cfg.setActualEncodingIfNotSet(enc);

            try {
                if (enc.equals(CharsetNames.CS_UTF8)) {
                    // !!! TEST-only:
                    /*
                    w = new com.fasterxml.aalto.io.UTF8Writer(cfg, out, autoCloseOutput);
                    xw = new CharXmlWriter(cfg, w);
                    */

                    xw = new Utf8XmlWriter(cfg, out);
                    symbols = _config.getUtf8Symbols(xw);
                } else if (enc.equals(CharsetNames.CS_ISO_LATIN1)) {
                    xw = new Latin1XmlWriter(cfg, out);
                    symbols = _config.getLatin1Symbols(xw);
                } else if (enc.equals(CharsetNames.CS_US_ASCII)) {
                    xw = new AsciiXmlWriter(cfg, out);
                    symbols = _config.getAsciiSymbols(xw);
                } else {
                    w = new OutputStreamWriter(out, enc);
                    xw = new CharXmlWriter(cfg, w);
                    symbols = _config.getCharSymbols(xw);
                }
            } catch (IOException ioe) {
                throw new XMLStreamException(ioe);
            }
            /*
            try {
                if (enc == CharsetNames.CS_UTF8) {
                    w = new UTF8Writer(cfg, out, autoCloseOutput);
                    xw = new BufferingXmlWriter(w, cfg, enc, true);
                } else if (enc == CharsetNames.CS_ISO_LATIN1) {
                    xw = new ISOLatin1XmlWriter(out, cfg, autoCloseOutput);
                } else if (enc == CharsetNames.CS_US_ASCII) {
                    xw = new ISOLatin1XmlWriter(out, cfg, autoCloseOutput);
                } else {
                    w = new OutputStreamWriter(out, enc);
                    xw = new BufferingXmlWriter(w, cfg, enc, autoCloseOutput);
                }
            } catch (IOException ex) {
                throw new XMLStreamException(ex);
            }
            */

        } else {
            // we may still be able to figure out the encoding:
            if (enc == null) {
                enc = CharsetNames.findEncodingFor(w);
            }
            if (enc != null) {
                cfg.setActualEncodingIfNotSet(enc);
            }
            xw = new CharXmlWriter(cfg, w);
            symbols = _config.getCharSymbols(xw);
        }

        return new NonRepairingStreamWriter(cfg, xw, symbols);
    }

}
