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

package io.clientcore.core.serialization.xml.implementation.aalto.in;

import io.clientcore.core.serialization.xml.implementation.aalto.impl.LocationImpl;
import io.clientcore.core.serialization.xml.implementation.aalto.impl.StreamExceptionBase;
import io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;

import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts.CHAR_CR;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts.CHAR_LF;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts.CHAR_NULL;
import static io.clientcore.core.serialization.xml.implementation.aalto.util.XmlConsts.CHAR_SPACE;

/**
 * Class that takes care of bootstrapping main document input from
 * a Stream input source.
 */
public final class CharSourceBootstrapper {

    final static char CHAR_BOM_MARKER = (char) 0xFEFF;

    /*
    /**********************************************************************
    /* Shared string consts
    /**********************************************************************
     */

    public final static String ERR_XMLDECL_KW_VERSION = "; expected keyword '" + XmlConsts.XML_DECL_KW_VERSION + "'";

    public final static String ERR_XMLDECL_END_MARKER = "; expected \"?>\" end marker";

    public final static String ERR_XMLDECL_EXP_EQ = "; expected '=' after ";
    public final static String ERR_XMLDECL_EXP_ATTRVAL = "; expected a quote character enclosing value for ";

    /*
    /**********************************************************************
    /* Input location data
    /**********************************************************************
     */

    /**
     * Current number of input units (bytes or chars) that were processed in
     * previous blocks,
     * before contents of current input buffer.
     *<p>
     * Note: includes possible BOMs, if those were part of the input.
     */
    private int _inputProcessed = 0;

    /**
     * Current row location of current point in input buffer, using
     * zero-based counting.
     */
    private int _inputRow = 0;

    /**
     * Current index of the first character of the current row in input
     * buffer. Needed to calculate column position, if necessary; benefit
     * of not having column itself is that this only has to be updated
     * once per line.
     */
    private int _inputRowStart = 0;

    /*
    /**********************************************************************
    /* Info passed by the caller
    /**********************************************************************
     */

    final ReaderConfig _config;

    /*
    /**********************************************************************
    /* Info from XML declaration
    /**********************************************************************
     */

    /**
     * XML declaration from the input (1.0, 1.1 or 'unknown')
     */
    int mDeclaredXmlVersion = XmlConsts.XML_V_UNKNOWN;

    /**
     * Value of encoding pseudo-attribute from xml declaration, if
     * one was found; null otherwise.
     */
    String mFoundEncoding;

    String mStandalone;

    /*
    /**********************************************************************
    //* Temporary data
    /**********************************************************************
    */

    /**
     * Need a short buffer to read in values of pseudo-attributes (version,
     * encoding, standalone). Don't really need tons of space; just enough
     * for the longest anticipated encoding id... and maybe few chars just
     * in case (for additional white space that we ignore)
     */
    final char[] mKeyword;

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
        _config = cfg;
        mKeyword = new char[ReaderConfig.DEFAULT_SMALL_BUFFER_LEN];
        _in = r;
        _inputBuffer = new char[ReaderConfig.DEFAULT_CHAR_BUFFER_LEN];
        _inputLast = _inputPtr = 0;
    }

    public static CharSourceBootstrapper construct(ReaderConfig cfg, Reader r) throws XMLStreamException {
        return new CharSourceBootstrapper(cfg, r);
    }

    /*
    /**********************************************************************
    /* Package methods, parsing
    /**********************************************************************
    */

    /**
     * Method that will parse xml declaration, which at this point is
     * known to exist.
     */
    private void readXmlDeclaration() throws IOException, XMLStreamException {
        int c = getNextAfterWs();

        // First, version pseudo-attribute:

        if (c != 'v') {
            reportUnexpectedChar(c, ERR_XMLDECL_KW_VERSION);
        } else { // ok, should be version
            mDeclaredXmlVersion = readXmlVersion();
            c = getWsOrChar();
        }

        // Then, 'encoding'
        if (c == 'e') {
            mFoundEncoding = readXmlEncoding();
            c = getWsOrChar();
        }

        // Then, 'standalone' (for main doc)
        if (c == 's') {
            mStandalone = readXmlStandalone();
            c = getWsOrChar();
        }

        // And finally, need to have closing markers

        if (c != '?') {
            reportUnexpectedChar(c, ERR_XMLDECL_END_MARKER);
        }
        c = getNext();
        if (c != '>') {
            reportUnexpectedChar(c, ERR_XMLDECL_END_MARKER);
        }
    }

    /**
     * @return Xml version declaration read
     */
    private int readXmlVersion() throws IOException, XMLStreamException {
        int c = checkKeyword(XmlConsts.XML_DECL_KW_VERSION);
        if (c != CHAR_NULL) {
            reportUnexpectedChar(c, XmlConsts.XML_DECL_KW_VERSION);
        }
        c = handleEq(XmlConsts.XML_DECL_KW_VERSION);
        int len = readQuotedValue(mKeyword, c);

        if (len == 3) {
            if (mKeyword[0] == '1' && mKeyword[1] == '.') {
                c = mKeyword[2];
                if (c == '0') {
                    return XmlConsts.XML_V_10;
                }
                if (c == '1') {
                    return XmlConsts.XML_V_11;
                }
            }
        }

        // Nope; error. -1 indicates run off...
        String got;

        if (len < 0) {
            got = "'" + new String(mKeyword) + "[..]'";
        } else if (len == 0) {
            got = "<empty>";
        } else {
            got = "'" + new String(mKeyword, 0, len) + "'";
        }
        reportPseudoAttrProblem(XmlConsts.XML_DECL_KW_VERSION, got, XmlConsts.XML_V_10_STR, XmlConsts.XML_V_11_STR);
        return XmlConsts.XML_V_UNKNOWN; // never gets here, but compiler needs it
    }

    private String readXmlEncoding() throws IOException, XMLStreamException {
        int c = checkKeyword(XmlConsts.XML_DECL_KW_ENCODING);
        if (c != CHAR_NULL) {
            reportUnexpectedChar(c, XmlConsts.XML_DECL_KW_ENCODING);
        }
        c = handleEq(XmlConsts.XML_DECL_KW_ENCODING);

        int len = readQuotedValue(mKeyword, c);

        /* Hmmh. How about "too long" encodings? Maybe just truncate them,
         * for now?
         */
        if (len == 0) { // let's still detect missing value...
            reportPseudoAttrProblem(XmlConsts.XML_DECL_KW_ENCODING, null, null, null);
        }

        if (len < 0) { // will be truncated...
            return new String(mKeyword);
        }
        return new String(mKeyword, 0, len);
    }

    private String readXmlStandalone() throws IOException, XMLStreamException {
        int c = checkKeyword(XmlConsts.XML_DECL_KW_STANDALONE);
        if (c != CHAR_NULL) {
            reportUnexpectedChar(c, XmlConsts.XML_DECL_KW_STANDALONE);
        }
        c = handleEq(XmlConsts.XML_DECL_KW_STANDALONE);
        int len = readQuotedValue(mKeyword, c);

        if (len == 2) {
            if (mKeyword[0] == 'n' && mKeyword[1] == 'o') {
                return XmlConsts.XML_SA_NO;
            }
        } else if (len == 3) {
            if (mKeyword[0] == 'y' && mKeyword[1] == 'e' && mKeyword[2] == 's') {
                return XmlConsts.XML_SA_YES;
            }
        }

        // Nope; error. -1 indicates run off...
        String got;

        if (len < 0) {
            got = "'" + new String(mKeyword) + "[..]'";
        } else if (len == 0) {
            got = "<empty>";
        } else {
            got = "'" + new String(mKeyword, 0, len) + "'";
        }

        reportPseudoAttrProblem(XmlConsts.XML_DECL_KW_STANDALONE, got, XmlConsts.XML_SA_YES, XmlConsts.XML_SA_NO);
        return got; // never gets here, but compiler can't figure it out
    }

    private int handleEq(String attr) throws IOException, XMLStreamException {
        int c = getNextAfterWs();
        if (c != '=') {
            reportUnexpectedChar(c, ERR_XMLDECL_EXP_EQ + "'" + attr + "'");
        }

        c = getNextAfterWs();
        if (c != '"' && c != '\'') {
            reportUnexpectedChar(c, ERR_XMLDECL_EXP_ATTRVAL + "'" + attr + "'");
        }
        return c;
    }

    /**
     * Method that should get next character, which has to be either specified
     * character (usually end marker), OR, any character as long as there'
     * at least one space character before it.
     */
    private int getWsOrChar() throws IOException, XMLStreamException {
        int c = getNext();
        if (c == (int) '?') {
            return c;
        }
        if (c > CHAR_SPACE) {
            reportUnexpectedChar(c, "; expected either '" + ((char) (int) '?') + "' or white space");
        }
        if (c == CHAR_LF || c == CHAR_CR) {
            // Need to push it back to be processed properly
            pushback();
        }
        return getNextAfterWs();
    }

    public ReaderScanner bootstrap() throws XMLStreamException {
        try {
            return doBootstrap();
        } catch (IOException ioe) {
            throw new StreamExceptionBase(ioe);
        }
    }

    public ReaderScanner doBootstrap() throws IOException, XMLStreamException {
        if (_inputPtr >= _inputLast) {
            initialLoad();
        }

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
                    throw new StreamExceptionBase(
                        "Unexpected first character (char code 0xEF), not valid in xml document: could be mangled UTF-8 BOM marker. Make sure that the Reader uses correct encoding or pass an InputStream instead");
                }
            }
        }
        _config.setXmlDeclInfo(mDeclaredXmlVersion, mFoundEncoding, mStandalone);
        return new ReaderScanner(_config, _in, _inputBuffer, _inputPtr, _inputLast);
    }

    /*
    /////////////////////////////////////////////////////
    // Internal methods, loading input data
    /////////////////////////////////////////////////////
    */

    private void initialLoad() throws IOException {
        _inputPtr = 0;
        _inputLast = 0;

        while (_inputLast < 7) {
            int count = _in.read(_inputBuffer, _inputLast, _inputBuffer.length - _inputLast);
            if (count < 1) {
                return;
            }
            _inputLast += count;
        }
    }

    private void loadMore() throws IOException, XMLStreamException {
        /* Need to make sure offsets are properly updated for error
         * reporting purposes, and do this now while previous amounts
         * are still known.
         */
        _inputProcessed += _inputLast;
        _inputRowStart -= _inputLast;

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

    private void pushback() {
        --_inputPtr;
    }

    private int getNext() throws IOException, XMLStreamException {
        return (_inputPtr < _inputLast) ? _inputBuffer[_inputPtr++] : nextChar();
    }

    private int getNextAfterWs() throws IOException, XMLStreamException {

        while (true) {
            char c = (_inputPtr < _inputLast) ? _inputBuffer[_inputPtr++] : nextChar();

            if (c > CHAR_SPACE) {
                return c;
            }
            if (c == CHAR_CR || c == CHAR_LF) {
                skipCRLF(c);
            } else if (c == CHAR_NULL) {
                reportNull();
            }
        }
    }

    /**
     * @return First character that does not match expected, if any;
     *    CHAR_NULL if match succeeded
     */
    private int checkKeyword(String exp) throws IOException, XMLStreamException {
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

    private int readQuotedValue(char[] kw, int quoteChar) throws IOException, XMLStreamException {
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

    private Location getLocation() {
        return LocationImpl.fromZeroBased(_inputProcessed + _inputPtr, _inputRow, _inputPtr - _inputRowStart);
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

    /*
    /**********************************************************************
    /* Error reporting
    /**********************************************************************
     */

    private void reportXmlProblem(String msg) throws XMLStreamException {
        throw new StreamExceptionBase(msg, getLocation());
    }

    private void reportNull() throws XMLStreamException {
        reportXmlProblem("Illegal null byte/char in input stream");
    }

    private void reportEof() throws XMLStreamException {
        reportXmlProblem("Unexpected end-of-input in xml declaration");
    }

    private void reportUnexpectedChar(int i, String msg) throws XMLStreamException {
        String excMsg;

        if (Character.isISOControl((char) i)) {
            excMsg = "Unexpected character (CTRL-CHAR, code " + i + ")" + msg;
        } else {
            excMsg = "Unexpected character '" + ((char) i) + "' (code " + i + ")" + msg;
        }
        reportXmlProblem(excMsg);
    }

    private void reportPseudoAttrProblem(String attrName, String got, String expVal1, String expVal2)
        throws XMLStreamException {
        String expStr = (expVal1 == null) ? "" : ("; expected \"" + expVal1 + "\" or \"" + expVal2 + "\"");

        if (got == null || got.isEmpty()) {
            reportXmlProblem("Missing XML pseudo-attribute '" + attrName + "' value" + expStr);
        }
        reportXmlProblem("Invalid XML pseudo-attribute '" + attrName + "' value " + got + expStr);
    }
}
