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

import java.io.*;

import javax.xml.stream.*;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import com.azure.xml.implementation.stax2.XMLOutputFactory2;
import com.azure.xml.implementation.stax2.XMLStreamWriter2;
import com.azure.xml.implementation.stax2.io.Stax2Result;
import com.azure.xml.implementation.stax2.ri.Stax2EventWriterImpl;
import com.azure.xml.implementation.stax2.ri.Stax2WriterAdapter;

import com.azure.xml.implementation.aalto.dom.DOMWriterImpl;
import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.impl.StreamExceptionBase;
import com.azure.xml.implementation.aalto.out.*;
import com.azure.xml.implementation.aalto.util.CharsetNames;
import com.azure.xml.implementation.aalto.util.URLUtil;
import com.azure.xml.implementation.aalto.util.XmlConsts;

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

    protected final WriterConfig _config;

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
    public XMLEventWriter createXMLEventWriter(OutputStream out) throws XMLStreamException {
        return createXMLEventWriter(out, null);
    }

    @Override
    public XMLEventWriter createXMLEventWriter(OutputStream out, String enc) throws XMLStreamException {
        return new Stax2EventWriterImpl(createSW(out, null, enc, false));
    }

    @Override
    public XMLEventWriter createXMLEventWriter(javax.xml.transform.Result result) throws XMLStreamException {
        return new Stax2EventWriterImpl(createSW(result));
    }

    @Override
    public XMLEventWriter createXMLEventWriter(Writer w) throws XMLStreamException {
        return new Stax2EventWriterImpl(createSW(null, w, null, false));
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream out) throws XMLStreamException {
        return createXMLStreamWriter(out, null);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(OutputStream out, String enc) throws XMLStreamException {
        return createSW(out, null, enc, false);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(javax.xml.transform.Result result) throws XMLStreamException {
        return createSW(result);
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter(Writer w) throws XMLStreamException {
        return createSW(null, w, null, false);
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

    @Override
    public XMLEventWriter createXMLEventWriter(Writer w, String enc) throws XMLStreamException {
        return new Stax2EventWriterImpl(createSW(null, w, enc, false));
    }

    @Override
    public XMLEventWriter createXMLEventWriter(XMLStreamWriter sw) throws XMLStreamException {
        XMLStreamWriter2 sw2 = Stax2WriterAdapter.wrapIfNecessary(sw);
        return new Stax2EventWriterImpl(sw2);
    }

    @Override
    public XMLStreamWriter2 createXMLStreamWriter(Writer w, String enc) throws XMLStreamException {
        return createSW(null, w, enc, false);
    }

    // // // StAX2 "Profile" mutators

    @Override
    public void configureForXmlConformance() {
        _config.configureForXmlConformance();
    }

    @Override
    public void configureForRobustness() {
        _config.configureForRobustness();
    }

    @Override
    public void configureForSpeed() {
        _config.configureForSpeed();
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
    private XMLStreamWriter2 createSW(OutputStream out, Writer w, String enc, boolean forceAutoClose)
        throws XMLStreamException {
        // Need to ensure that the configuration object is not shared
        // any more; otherwise later changes via factory could be
        // visible half-way through output...
        WriterConfig cfg = _config.createNonShared();
        if (forceAutoClose) {
            cfg.doAutoCloseOutput(true);
        }
        XmlWriter xw;
        WNameTable symbols;

        if (w == null) {
            if (enc == null) {
                enc = XmlConsts.STAX_DEFAULT_OUTPUT_ENCODING;
            } else {
                // Canonical ones are interned, so we may have
                // normalized encoding already...
                if (enc != CharsetNames.CS_UTF8
                    && enc != CharsetNames.CS_ISO_LATIN1
                    && enc != CharsetNames.CS_US_ASCII) {
                    enc = CharsetNames.normalize(enc);
                }
            }

            cfg.setActualEncodingIfNotSet(enc);

            try {
                if (enc == CharsetNames.CS_UTF8) {
                    // !!! TEST-only:
                    /*
                    w = new com.fasterxml.aalto.io.UTF8Writer(cfg, out, autoCloseOutput);
                    xw = new CharXmlWriter(cfg, w);
                    */

                    xw = new Utf8XmlWriter(cfg, out);
                    symbols = _config.getUtf8Symbols(xw);
                } else if (enc == CharsetNames.CS_ISO_LATIN1) {
                    xw = new Latin1XmlWriter(cfg, out);
                    symbols = _config.getLatin1Symbols(xw);
                } else if (enc == CharsetNames.CS_US_ASCII) {
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

        if (cfg.willRepairNamespaces()) {
            return new RepairingStreamWriter(cfg, xw, symbols);
        }
        return new NonRepairingStreamWriter(cfg, xw, symbols);
    }

    private XMLStreamWriter2 createSW(Result res) throws XMLStreamException {
        OutputStream out = null;
        Writer w = null;
        String encoding = null;
        boolean autoclose;
        String sysId = null;

        if (res instanceof Stax2Result) {
            Stax2Result sr = (Stax2Result) res;
            try {
                out = sr.constructOutputStream();
                if (out == null) {
                    w = sr.constructWriter();
                }
            } catch (IOException ioe) {
                throw new StreamExceptionBase(ioe);
            }
            autoclose = true;
        } else if (res instanceof StreamResult) {
            StreamResult sr = (StreamResult) res;
            sysId = sr.getSystemId();
            out = sr.getOutputStream();
            if (out == null) {
                w = sr.getWriter();
            }
            autoclose = false; // caller still owns it, no automatic close
        } else if (res instanceof SAXResult) {
            SAXResult sr = (SAXResult) res;
            sysId = sr.getSystemId();
            if (sysId == null || sysId.length() == 0) {
                throw new StreamExceptionBase(
                    "Can not create a stream writer for a SAXResult that does not have System Id (support for using SAX input source not implemented)");
            }
            autoclose = true;
        } else if (res instanceof DOMResult) {
            return DOMWriterImpl.createFrom(_config.createNonShared(), (DOMResult) res);
        } else {
            throw new IllegalArgumentException(
                "Can not create XMLStreamWriter for Result type " + res.getClass() + " (unrecognized type)");
        }

        if (out != null) {
            return createSW(out, null, encoding, autoclose);
        }
        if (w != null) {
            return createSW(null, w, encoding, autoclose);
        }
        if (sysId != null && sysId.length() > 0) {
            /* 26-Dec-2008, tatu: If we must construct URL from system id,
             *   it means caller will not have access to resulting
             *   stream, thus we will force auto-closing.
             */
            autoclose = true;
            try {
                out = URLUtil.outputStreamFromURL(URLUtil.urlFromSystemId(sysId));
            } catch (IOException ioe) {
                throw new IoStreamException(ioe);
            }
            return createSW(out, null, encoding, autoclose);
        }
        throw new StreamExceptionBase(
            "Can not create XMLStreamWriter for passed-in Result -- neither writer, output stream nor system id (to create one) was accessible");
    }
}
