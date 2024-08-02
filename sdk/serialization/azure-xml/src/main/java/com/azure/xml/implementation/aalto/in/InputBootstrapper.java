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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import com.azure.xml.implementation.aalto.WFCException;
import com.azure.xml.implementation.aalto.util.XmlConsts;

/**
 * Abstract base class that defines shared functionality between different
 * bootstrappers (byte stream, char Readers, block input)
 */
public abstract class InputBootstrapper implements XmlConsts {
    /*
    /**********************************************************************
    /* Shared string consts
    /**********************************************************************
     */

    public final static String ERR_XMLDECL_KW_VERSION = "; expected keyword '" + XmlConsts.XML_DECL_KW_VERSION + "'";
    public final static String ERR_XMLDECL_KW_ENCODING = "; expected keyword '" + XmlConsts.XML_DECL_KW_ENCODING + "'";
    public final static String ERR_XMLDECL_KW_STANDALONE
        = "; expected keyword '" + XmlConsts.XML_DECL_KW_STANDALONE + "'";

    public final static String ERR_XMLDECL_END_MARKER = "; expected \"?>\" end marker";

    public final static String ERR_XMLDECL_EXP_SPACE = "; expected a white space";
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
    protected int _inputProcessed = 0;

    /**
     * Current row location of current point in input buffer, using
     * zero-based counting.
     */
    protected int _inputRow = 0;

    /**
     * Current index of the first character of the current row in input
     * buffer. Needed to calculate column position, if necessary; benefit
     * of not having column itself is that this only has to be updated
     * once per line.
     */
    protected int _inputRowStart = 0;

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

    //boolean mHadDeclaration = false;

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
    /* Life-cycle
    /**********************************************************************
     */

    protected InputBootstrapper(ReaderConfig cfg) {
        _config = cfg;
        mKeyword = cfg.allocSmallCBuffer(ReaderConfig.DEFAULT_SMALL_BUFFER_LEN);
    }

    /**
     * Main bootstrapping method, which will try to open the underlying
     * input source, check its encoding, read xml declaration if
     * there is one, and finally create a scanner for actual parsing.
     */
    public abstract XmlScanner bootstrap() throws XMLStreamException;

    /*
    /**********************************************************************
    /* Package methods, parsing
    /**********************************************************************
    */

    /**
     * Method that will parse xml declaration, which at this point is
     * known to exist.
     */
    protected void readXmlDeclaration() throws IOException, XMLStreamException {
        int c = getNextAfterWs(false);

        // First, version pseudo-attribute:

        if (c != 'v') {
            reportUnexpectedChar(c, ERR_XMLDECL_KW_VERSION);
        } else { // ok, should be version
            mDeclaredXmlVersion = readXmlVersion();
            c = getWsOrChar('?');
        }

        // Then, 'encoding'
        if (c == 'e') {
            mFoundEncoding = readXmlEncoding();
            c = getWsOrChar('?');
        }

        // Then, 'standalone' (for main doc)
        if (c == 's') {
            mStandalone = readXmlStandalone();
            c = getWsOrChar('?');
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
        int c = getNextAfterWs(false);
        if (c != '=') {
            reportUnexpectedChar(c, ERR_XMLDECL_EXP_EQ + "'" + attr + "'");
        }

        c = getNextAfterWs(false);
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
    private int getWsOrChar(int ok) throws IOException, XMLStreamException {
        int c = getNext();
        if (c == ok) {
            return c;
        }
        if (c > XmlConsts.CHAR_SPACE) {
            reportUnexpectedChar(c, "; expected either '" + ((char) ok) + "' or white space");
        }
        if (c == XmlConsts.CHAR_LF || c == XmlConsts.CHAR_CR) {
            // Need to push it back to be processed properly
            pushback();
        }
        return getNextAfterWs(false);
    }

    /*
    /**********************************************************************
    /* Abstract parsing methods for sub-classes to implement
    /**********************************************************************
     */

    protected abstract void pushback();

    protected abstract int getNext() throws IOException, XMLStreamException;

    protected abstract int getNextAfterWs(boolean reqWs) throws IOException, XMLStreamException;

    /**
     * @return First character that does not match expected, if any;
     *    CHAR_NULL if match succeeded
     */
    protected abstract int checkKeyword(String exp) throws IOException, XMLStreamException;

    protected abstract int readQuotedValue(char[] kw, int quoteChar) throws IOException, XMLStreamException;

    protected abstract Location getLocation();

    /*
    /**********************************************************************
    /* Error reporting
    /**********************************************************************
     */

    protected void reportXmlProblem(String msg) throws XMLStreamException {
        throw new WFCException(msg, getLocation());
    }

    protected void reportNull() throws XMLStreamException {
        reportXmlProblem("Illegal null byte/char in input stream");
    }

    protected void reportEof() throws XMLStreamException {
        reportXmlProblem("Unexpected end-of-input in xml declaration");
    }

    protected void reportUnexpectedChar(int i, String msg) throws XMLStreamException {
        String excMsg;

        if (Character.isISOControl((char) i)) {
            excMsg = "Unexpected character (CTRL-CHAR, code " + i + ")" + msg;
        } else {
            excMsg = "Unexpected character '" + ((char) i) + "' (code " + i + ")" + msg;
        }
        reportXmlProblem(excMsg);
    }

    protected final void reportPseudoAttrProblem(String attrName, String got, String expVal1, String expVal2)
        throws XMLStreamException {
        String expStr = (expVal1 == null) ? "" : ("; expected \"" + expVal1 + "\" or \"" + expVal2 + "\"");

        if (got == null || got.isEmpty()) {
            reportXmlProblem("Missing XML pseudo-attribute '" + attrName + "' value" + expStr);
        }
        reportXmlProblem("Invalid XML pseudo-attribute '" + attrName + "' value " + got + expStr);
    }
}
