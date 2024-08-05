// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Aalto XML processor
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

package com.azure.xml.implementation.aalto.in;

import java.io.*;
import java.text.MessageFormat;

import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.impl.LocationImpl;
import com.azure.xml.implementation.aalto.util.CharsetNames;

/**
 * Class that takes care of bootstrapping main document input from
 * a Stream input source.
 */
public final class CharSourceBootstrapper extends InputBootstrapper {

    final static char CHAR_BOM_MARKER = (char) 0xFEFF;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Underlying Reader to use for reading content.
     */
    final Reader _in;

    /*
    /**********************************************************************
    /* Input buffering
    /**********************************************************************
     */

    final char[] _inputBuffer;

    private int _inputPtr;

    /**
     * Offset of the first character after the end of valid buffer
     * contents.
     */
    private int _inputLast;

    /*
    ///////////////////////////////////////////////////////////////
    // Life-cycle
    ///////////////////////////////////////////////////////////////
     */

    private CharSourceBootstrapper(ReaderConfig cfg, Reader r) {
        super(cfg);
        _in = r;
        _inputBuffer = cfg.allocFullCBuffer(ReaderConfig.DEFAULT_CHAR_BUFFER_LEN);
        _inputLast = _inputPtr = 0;
    }

    private CharSourceBootstrapper(ReaderConfig cfg, char[] buffer, int start, int len) {
        super(cfg);
        _in = null;
        _inputBuffer = buffer;
        _inputPtr = start;
        _inputLast = start + len;
    }

    public static CharSourceBootstrapper construct(ReaderConfig cfg, Reader r) throws XMLStreamException {
        return new CharSourceBootstrapper(cfg, r);
    }

    public static CharSourceBootstrapper construct(ReaderConfig cfg, char[] buffer, int start, int len)
        throws XMLStreamException {
        return new CharSourceBootstrapper(cfg, buffer, start, len);
    }

    @Override
    public XmlScanner bootstrap() throws XMLStreamException {
        try {
            return doBootstrap();
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        } finally {
            _config.freeSmallCBuffer(mKeyword);
        }
    }

    public XmlScanner doBootstrap() throws IOException, XMLStreamException {
        if (_inputPtr >= _inputLast) {
            initialLoad(7);
        }

        String normEnc = null;

        /* Only need 6 for signature ("<?xml\s"), but there may be a leading
         * BOM in there... and a valid xml declaration has to be longer
         * than 7 chars anyway (although, granted, shortest valid xml docl
         * is just 4 chars... "<a/>")
         */
        if ((_inputLast - _inputPtr) >= 7) {
            char c = _inputBuffer[_inputPtr];

            // BOM to skip?
            if (c == CHAR_BOM_MARKER) {
                c = _inputBuffer[++_inputPtr];
            }
            if (c == '<') {
                if (_inputBuffer[_inputPtr + 1] == '?'
                    && _inputBuffer[_inputPtr + 2] == 'x'
                    && _inputBuffer[_inputPtr + 3] == 'm'
                    && _inputBuffer[_inputPtr + 4] == 'l'
                    && _inputBuffer[_inputPtr + 5] <= 0x0020) {
                    // Yup, got the declaration ok!
                    _inputPtr += 6; // skip declaration
                    readXmlDeclaration();

                    if (mFoundEncoding != null) {
                        normEnc = verifyXmlEncoding(mFoundEncoding);
                    }
                }
            } else {
                /* We may also get something that would be invalid xml
                 * ("garbage" char; neither '<' nor space). If so, and
                 * it's one of "well-known" cases, we can not only throw
                 * an exception but also indicate a clue as to what is likely
                 * to be wrong.
                 */
                /* Specifically, UTF-8 read via, say, ISO-8859-1 reader, can
                 * "leak" marker (0xEF, 0xBB, 0xBF). While we could just eat
                 * it, there's bound to be other problems cropping up, so let's
                 * inform about the problem right away.
                 */
                if (c == 0xEF) {
                    throw new IoStreamException(
                        "Unexpected first character (char code 0xEF), not valid in xml document: could be mangled UTF-8 BOM marker. Make sure that the Reader uses correct encoding or pass an InputStream instead");
                }
            }
        }
        _config.setActualEncoding(normEnc);
        _config.setXmlDeclInfo(mDeclaredXmlVersion, mFoundEncoding, mStandalone);
        return new ReaderScanner(_config, _in, _inputBuffer, _inputPtr, _inputLast);
    }

    /*
    ////////////////////////////////////////////////////
    // Internal methods, main xml decl processing
    ////////////////////////////////////////////////////
     */

    /**
     * @return Normalized encoding name
     */
    private String verifyXmlEncoding(String enc) throws XMLStreamException {
        enc = CharsetNames.normalize(enc);

        // Probably no point in comparing at all... is there?
        // But we can report a possible problem?
        String extEnc = _config.getExternalEncoding();
        if (extEnc != null && enc != null && !extEnc.equalsIgnoreCase(enc)) {
            XMLReporter rep = _config.getXMLReporter();
            if (rep != null) {
                Location loc = getLocation();
                rep.report(MessageFormat.format(ErrorConsts.W_MIXED_ENCODINGS, extEnc, enc), ErrorConsts.WT_XML_DECL,
                    this, loc);
            }
        }

        return enc;
    }

    /*
    /////////////////////////////////////////////////////
    // Internal methods, loading input data
    /////////////////////////////////////////////////////
    */

    private boolean initialLoad(int minimum) throws IOException {
        _inputPtr = 0;
        _inputLast = 0;

        if (_in == null) { // for block sources
            return false;
        }

        while (_inputLast < minimum) {
            int count = _in.read(_inputBuffer, _inputLast, _inputBuffer.length - _inputLast);
            if (count < 1) {
                return false;
            }
            _inputLast += count;
        }
        return true;
    }

    private void loadMore() throws IOException, XMLStreamException {
        /* Need to make sure offsets are properly updated for error
         * reporting purposes, and do this now while previous amounts
         * are still known.
         */
        _inputProcessed += _inputLast;
        _inputRowStart -= _inputLast;

        if (_in == null) { // for block sources
            reportEof();
        }

        _inputPtr = 0;
        _inputLast = _in.read(_inputBuffer, 0, _inputBuffer.length);
        if (_inputLast < 1) {
            reportEof();
        }
    }

    /*
    /////////////////////////////////////////////////////
    // Implementations of abstract parsing methods
    /////////////////////////////////////////////////////
    */

    @Override
    protected void pushback() {
        --_inputPtr;
    }

    @Override
    protected int getNext() throws IOException, XMLStreamException {
        return (_inputPtr < _inputLast) ? _inputBuffer[_inputPtr++] : nextChar();
    }

    @Override
    protected int getNextAfterWs(boolean reqWs) throws IOException, XMLStreamException {
        int count = 0;

        while (true) {
            char c = (_inputPtr < _inputLast) ? _inputBuffer[_inputPtr++] : nextChar();

            if (c > CHAR_SPACE) {
                if (reqWs && count == 0) {
                    reportUnexpectedChar(c, ERR_XMLDECL_EXP_SPACE);
                }
                return c;
            }
            if (c == CHAR_CR || c == CHAR_LF) {
                skipCRLF(c);
            } else if (c == CHAR_NULL) {
                reportNull();
            }
            ++count;
        }
    }

    /**
     * @return First character that does not match expected, if any;
     *    CHAR_NULL if match succeeded
     */
    @Override
    protected int checkKeyword(String exp) throws IOException, XMLStreamException {
        int len = exp.length();

        for (int ptr = 1; ptr < len; ++ptr) {
            char c = (_inputPtr < _inputLast) ? _inputBuffer[_inputPtr++] : nextChar();

            if (c != exp.charAt(ptr)) {
                return c;
            }
            if (c == CHAR_NULL) {
                reportNull();
            }
        }

        return CHAR_NULL;
    }

    @Override
    protected int readQuotedValue(char[] kw, int quoteChar) throws IOException, XMLStreamException {
        int i = 0;
        int len = kw.length;

        while (true) {
            char c = (_inputPtr < _inputLast) ? _inputBuffer[_inputPtr++] : nextChar();
            if (c == CHAR_CR || c == CHAR_LF) {
                skipCRLF(c);
            } else if (c == CHAR_NULL) {
                reportNull();
            }
            if (c == quoteChar) {
                return (i < len) ? i : -1;
            }
            // Let's just truncate longer values, but match quote
            if (i < len) {
                kw[i++] = c;
            }
        }
    }

    @Override
    protected Location getLocation() {
        return LocationImpl.fromZeroBased(_config.getPublicId(), _config.getSystemId(), _inputProcessed + _inputPtr,
            _inputRow, _inputPtr - _inputRowStart);
    }

    /*
    /**********************************************************************
    /* Internal methods, single-byte access methods
    /**********************************************************************
     */

    private char nextChar() throws IOException, XMLStreamException {
        if (_inputPtr >= _inputLast) {
            loadMore();
        }
        return _inputBuffer[_inputPtr++];
    }

    private void skipCRLF(char lf) throws IOException, XMLStreamException {
        if (lf == '\r') {
            char c = (_inputPtr < _inputLast) ? _inputBuffer[_inputPtr++] : nextChar();
            if (c != '\n') {
                --_inputPtr; // pushback if not 2-char/byte lf
            }
        }
        ++_inputRow;
        _inputRowStart = _inputPtr;
    }
}
