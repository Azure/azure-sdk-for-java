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

package com.azure.xml.implementation.aalto.out;

import java.io.*;
import java.text.MessageFormat;
import java.util.Objects;

import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.ri.typed.AsciiValueEncoder;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;
import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.azure.xml.implementation.aalto.util.CharsetNames;
import com.azure.xml.implementation.aalto.util.XmlChars;
import com.azure.xml.implementation.aalto.util.XmlConsts;

/**
 * Base class for output type / encoding-specific serializers
 * used to do actual physical output of serialized xml content.
 * At this level, no namespace handling is done, and only those
 * checks directly related to encoding (including optional validity
 * checks for xml content) are implemented.
 */
public abstract class XmlWriter extends WNameFactory {
    protected final static int SURR1_FIRST = 0xD800;
    protected final static int SURR2_FIRST = 0xDC00;
    protected final static int SURR2_LAST = 0xDFFF;

    protected final static int DEFAULT_COPYBUFFER_LEN = 512;

    /*
    /**********************************************************************
    /* Basic configuration
    /**********************************************************************
     */

    final protected WriterConfig _config;

    /**
     * Intermediate buffer, in which content (esp. Strings) can be
     * copied to, before being output.
     */
    protected char[] _copyBuffer;

    protected final int _copyBufferLen;

    /**
     * Indicates whether output is to be compliant; if false, is to be
     * xml 1.0 compliant, if true, xml 1.1 compliant.
     */
    protected boolean _xml11 = false;

    protected final boolean _cfgNsAware;

    /*
    /**********************************************************************
    /* Output location info
    /**********************************************************************
     */

    /**
     * Number of characters output prior to currently buffered output
     */
    protected int _locPastChars = 0;

    protected int _locRowNr = 1;

    /**
     * Offset of the first character on this line. May be negative, if
     * the offset was in a buffer that has been flushed out.
     */
    protected int _locRowStartOffset = 0;

    /*
    /**********************************************************************
    /* Validation
    /**********************************************************************
     */

    final protected boolean _checkContent;

    final protected boolean _checkNames;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected XmlWriter(WriterConfig cfg) {
        _config = cfg;
        _copyBuffer = cfg.allocMediumCBuffer(DEFAULT_COPYBUFFER_LEN);
        _copyBufferLen = _copyBuffer.length;

        _cfgNsAware = cfg.isNamespaceAware();
        _checkContent = cfg.willCheckContent();
        _checkNames = cfg.willCheckNames();
    }

    /*
    /**********************************************************************
    /* Abstract methods for WNameFactory
    /**********************************************************************
     */

    @Override
    public abstract WName constructName(String localName) throws XMLStreamException;

    @Override
    public abstract WName constructName(String prefix, String localName) throws XMLStreamException;

    /*
    /**********************************************************************
    /* Extra configuration
    /**********************************************************************
     */

    public void enableXml11() {
        _xml11 = true;
    }

    protected abstract int getOutputPtr();

    /**
     * Method called by error reporting code, to figure out if a given
     * character is encodable (without using character entities) with
     * the current encoding or not.
     *
     * @return Character code of the highest character that can be
     *   natively encoded.
     */
    public abstract int getHighestEncodable();

    /*
    /**********************************************************************
    /* Basic methods for communicating with underlying stream or writer
    /**********************************************************************
     */

    /**
     * Method called to flush the buffer(s), and close the output
     * sink (stream or writer).
     */
    public final void close(boolean forceTargetClose) throws IOException {
        flush();
        _releaseBuffers();
        _closeTarget(forceTargetClose || _config.willAutoCloseOutput());
    }

    public void _releaseBuffers() {
        char[] buf = _copyBuffer;
        if (buf != null) {
            _copyBuffer = null;
            _config.freeMediumCBuffer(buf);
        }
    }

    public abstract void _closeTarget(boolean doClose) throws IOException;

    public abstract void flush() throws IOException;

    /*
    /**********************************************************************
    /* Write methods, non-elem/attr, textual
    /**********************************************************************
     */

    /**
     * @param data Contents of the CDATA section to write out
    
     * @return offset of the (first) illegal content segment ("]]&gt;") in
     *   passed content, if not in repairing mode; or -1 if none
     */
    public abstract int writeCData(String data) throws IOException, XMLStreamException;

    public abstract int writeCData(char[] cbuf, int offset, int len) throws IOException, XMLStreamException;

    public abstract void writeCharacters(String data) throws IOException, XMLStreamException;

    public abstract void writeCharacters(char[] cbuf, int offset, int len) throws IOException, XMLStreamException;

    public abstract void writeSpace(String data) throws IOException, XMLStreamException;

    public abstract void writeSpace(char[] cbuf, int offset, int len) throws IOException, XMLStreamException;

    /**
     * Method that will try to output the content as specified. If
     * the content passed in has embedded "--" in it, it will either
     * add an intervening space between consequtive hyphens (if content
     * fixing is enabled), or return the offset of the first hyphen in
     * multi-hyphen sequence.
     */
    public abstract int writeComment(String data) throws IOException, XMLStreamException;

    /**
     * Older "legacy" output method for outputting DOCTYPE declaration.
     * Assumes that the passed-in String contains a complete DOCTYPE
     * declaration properly quoted.
     */
    public abstract void writeDTD(String data) throws IOException, XMLStreamException;

    public abstract void writeDTD(WName rootName, String systemId, String publicId, String internalSubset)
        throws IOException, XMLStreamException;

    public abstract void writeEntityReference(WName name) throws IOException, XMLStreamException;

    public abstract int writePI(WName target, String data) throws IOException, XMLStreamException;

    public abstract void writeRaw(String str, int offset, int len) throws IOException, XMLStreamException;

    public abstract void writeRaw(char[] cbuf, int offset, int len) throws IOException, XMLStreamException;

    public abstract void writeXmlDeclaration(String version, String enc, String standalone)
        throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Write methods, elements
    /**********************************************************************
     */

    /**
     *<p>
     * Note: can throw XMLStreamException, if name checking is enabled,
     * and name is invalid (name check has to be in this writer, not
     * caller, since it depends not only on xml limitations, but also
     * on encoding limitations)
     */
    public abstract void writeStartTagStart(WName name) throws IOException, XMLStreamException;

    public abstract void writeStartTagEnd() throws IOException, XMLStreamException;

    public abstract void writeStartTagEmptyEnd() throws IOException, XMLStreamException;

    public abstract void writeEndTag(WName name) throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Write methods, attributes/ns, textual
    /**********************************************************************
     */

    /**
     *<p>
     * Note: can throw XMLStreamException, if name checking is enabled,
     * and name is invalid (name check has to be in this writer, not
     * caller, since it depends not only on xml limitations, but also
     * on encoding limitations)
     */
    public abstract void writeAttribute(WName name, String value) throws IOException, XMLStreamException;

    public abstract void writeAttribute(WName name, char[] value, int offset, int len)
        throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Write methods, Typed
    /**********************************************************************
     */

    public abstract void writeTypedValue(AsciiValueEncoder enc) throws IOException, XMLStreamException;

    public abstract void writeAttribute(WName name, AsciiValueEncoder enc) throws IOException, XMLStreamException;

    /*
    /**********************************************************************
    /* Location information
    /**********************************************************************
     */

    public int getRow() {
        return _locRowNr;
    }

    public int getColumn() {
        return (getOutputPtr() - _locRowStartOffset) + 1;
    }

    public int getAbsOffset() {
        return _locPastChars + getOutputPtr();
    }

    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */

    /**
     * Method used to figure out which part of the Unicode char set the
     * encoding can natively support. Values returned are 7, 8 and 16,
     * to indicate (respectively) "ascii", "ISO-Latin" and "native Unicode".
     * These just best guesses, but should work ok for the most common
     * encodings.
     */
    public static int guessEncodingBitSize(WriterConfig cfg) {
        String enc = cfg.getPreferredEncoding();

        if (enc == null || enc.isEmpty()) { // let's assume default is UTF-8...
            return 16;
        }
        // Let's see if we can find a normalized name, first:
        enc = CharsetNames.normalize(enc);

        // Ok, first, do we have known ones; starting with most common:
        if (Objects.equals(enc, CharsetNames.CS_UTF8)) {
            return 16; // meaning up to 2^16 can be represented natively
        } else if (Objects.equals(enc, CharsetNames.CS_ISO_LATIN1)) {
            return 8;
        } else if (Objects.equals(enc, CharsetNames.CS_US_ASCII)) {
            return 7;
        } else if (Objects.equals(enc, CharsetNames.CS_UTF16)
            || Objects.equals(enc, CharsetNames.CS_UTF16BE)
            || Objects.equals(enc, CharsetNames.CS_UTF16LE)
            || Objects.equals(enc, CharsetNames.CS_UTF32BE)
            || Objects.equals(enc, CharsetNames.CS_UTF32LE)) {
            return 16;
        }

        /* Above and beyond well-recognized names, it might still be
         * good to have more heuristics for as-of-yet unhandled cases...
         * But, it's probably easier to only assume 8-bit clean (could
         * even make it just 7, let's see how this works out)
         */
        return 8;
    }

    /**
     * This is the method called when an output method call violates
     * name well-formedness checks
     * and name validation is enabled.
     */
    protected void reportNwfName(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    protected void reportNwfContent(String msg) throws XMLStreamException {
        throwOutputError(msg);
    }

    protected void reportNwfContent(String format, Object arg1, Object arg2) throws XMLStreamException {
        String msg = MessageFormat.format(format, arg1, arg2);
        reportNwfContent(msg);
    }

    protected void reportFailedEscaping(String type, int ch) throws XMLStreamException {
        // Quick separation of high-range invalid chars:
        if (ch == 0xFFFE || ch == 0xFFFF || (ch >= SURR1_FIRST && ch <= SURR2_LAST)) {
            reportInvalidChar(ch);
        }
        // One more check: is it only escapable in xml 1.1?
        if (ch < 0x0020) {
            if (ch == 0 || !_config.isXml11()) {
                reportInvalidChar(ch);
            }
        }
        String msg = MessageFormat.format(ErrorConsts.WERR_NO_ESCAPING, type, ch);
        reportNwfContent(msg);
    }

    protected void reportInvalidChar(int c) throws XMLStreamException {
        // First, let's flush any output we may have, to make debugging easier
        try {
            flush();
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }

        if (c == 0) {
            reportNwfContent("Invalid null character in text to output");
        }
        if (c < ' ' || (c >= 0x7F && c <= 0x9F)) {
            String msg = "Invalid white space character (0x" + Integer.toHexString(c) + ") in text to output";
            if (_xml11) {
                msg += " (can only be output using character entity)";
            }
            reportNwfContent(msg);
        }
        if (c > XmlConsts.MAX_UNICODE_CHAR) {
            reportNwfContent("Illegal unicode character point (0x" + Integer.toHexString(c)
                + ") to output; max is 0x10FFFF as per RFC 3629");
        }
        /* Surrogate pair in non-quotable (not text or attribute value)
         * content, and non-unicode encoding (ISO-8859-x, Ascii)?
         */
        if (c >= SURR1_FIRST && c <= SURR2_LAST) {
            reportNwfContent(
                "Illegal surrogate pair -- can only be output via character entities (for current encoding), which are not allowed in this content");
        }
        // Just something that the encoding can not express natively?

        reportNwfContent("Invalid XML character " + XmlChars.getCharDesc(c) + " in text to output");
    }

    protected void throwOutputError(String msg) throws XMLStreamException {
        // First, let's flush any output we may have, to make debugging easier
        try {
            flush();
        } catch (IOException ioe) {
            throw new IoStreamException(ioe);
        }
        throw new XMLStreamException(msg);
    }

}
