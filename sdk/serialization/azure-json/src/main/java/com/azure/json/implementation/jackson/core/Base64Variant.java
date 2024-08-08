// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

import java.util.Arrays;

import com.azure.json.implementation.jackson.core.util.ByteArrayBuilder;

import static com.azure.json.implementation.jackson.core.Base64Variants.STD_BASE64_ALPHABET;

/**
 * Class used to define specific details of which
 * variant of Base64 encoding/decoding is to be used. Although there is
 * somewhat standard basic version (so-called "MIME Base64"), other variants
 * exists, see <a href="http://en.wikipedia.org/wiki/Base64">Base64 Wikipedia entry</a> for details.
 *
 * @author Tatu Saloranta
 */
public final class Base64Variant implements java.io.Serializable {
    /**
     * Defines how the Base64Variant deals with Padding while reading
     *
     * @since 2.12
     */
    public enum PaddingReadBehaviour {
        /**
         * Padding is not allowed in Base64 content being read (finding something
         * that looks like padding at the end of content results in an exception)
         */
        PADDING_FORBIDDEN,

        /**
         * Padding is required in Base64 content being read
         * (missing padding for incomplete ending quartet results in an exception)
         */
        PADDING_REQUIRED,

        /**
         * Padding is allowed but not required in Base64 content being read: no
         * exception thrown based on existence or absence, as long as proper
         * padding characters are used.
         */
        PADDING_ALLOWED
    }

    private final static int INT_SPACE = 0x20;

    // We'll only serialize name
    private static final long serialVersionUID = 1L;

    /**
     * Marker used to denote ascii characters that do not correspond
     * to a 6-bit value (in this variant), and is not used as a padding
     * character.
     */
    public final static int BASE64_VALUE_INVALID = -1;

    /**
     * Marker used to denote ascii character (in decoding table) that
     * is the padding character using this variant (if any).
     */
    public final static int BASE64_VALUE_PADDING = -2;

    /*
    /**********************************************************
    /* Encoding/decoding tables
    /**********************************************************
     */

    /**
     * Decoding table used for base 64 decoding.
     */
    private final transient int[] _asciiToBase64 = new int[128];

    /**
     * Encoding table used for base 64 decoding when output is done
     * as characters.
     */
    private final transient char[] _base64ToAsciiC = new char[64];

    /**
     * Alternative encoding table used for base 64 decoding when output is done
     * as ascii bytes.
     */
    private final transient byte[] _base64ToAsciiB = new byte[64];

    /*
    /**********************************************************
    /* Other configuration
    /**********************************************************
     */

    /**
     * Symbolic name of variant; used for diagnostics/debugging.
     *<p>
     * Note that this is the only non-transient field; used when reading
     * back from serialized state.
     *<p>
     * Also: must not be private, accessed from `BaseVariants`
     */
    final String _name;

    /**
     * Maximum number of encoded base64 characters to output during encoding
     * before adding a linefeed, if line length is to be limited
     * ({@link java.lang.Integer#MAX_VALUE} if not limited).
     *<p>
     * Note: for some output modes (when writing attributes) linefeeds may
     * need to be avoided, and this value ignored.
     */
    private final int _maxLineLength;

    /**
     * Whether padding characters should be required or not while decoding
     *
     * @since 2.12
     */
    private final PaddingReadBehaviour _paddingReadBehaviour;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public Base64Variant(String name, int maxLineLength) {
        _name = name;
        _maxLineLength = maxLineLength;

        // Ok and then we need to create codec tables.

        // First the main encoding table:
        int alphaLen = STD_BASE64_ALPHABET.length();

        // And then secondary encoding table and decoding table:
        STD_BASE64_ALPHABET.getChars(0, alphaLen, _base64ToAsciiC, 0);
        Arrays.fill(_asciiToBase64, BASE64_VALUE_INVALID);
        for (int i = 0; i < alphaLen; ++i) {
            char alpha = _base64ToAsciiC[i];
            _base64ToAsciiB[i] = (byte) alpha;
            _asciiToBase64[alpha] = i;
        }

        // Plus if we use padding, add that in too
        _asciiToBase64['='] = BASE64_VALUE_PADDING;

        // By default, require padding on input if written on output; do not
        // accept if padding not written
        _paddingReadBehaviour = PaddingReadBehaviour.PADDING_REQUIRED;
    }

    /**
     * "Copy constructor" that can be used when the base alphabet is identical
     * to one used by another variant except for the maximum line length
     * (and obviously, name).
     *
     * @param base Variant to use for settings not specific by other parameters
     * @param name Name of this variant
     * @param maxLineLength Maximum length (in characters) of lines to output before
     *    using linefeed
     */
    public Base64Variant(Base64Variant base, String name, int maxLineLength) {
        this(base, name, base._paddingReadBehaviour, maxLineLength);
    }

    private Base64Variant(Base64Variant base, String name, PaddingReadBehaviour paddingReadBehaviour,
        int maxLineLength) {
        _name = name;
        byte[] srcB = base._base64ToAsciiB;
        System.arraycopy(srcB, 0, this._base64ToAsciiB, 0, srcB.length);
        char[] srcC = base._base64ToAsciiC;
        System.arraycopy(srcC, 0, this._base64ToAsciiC, 0, srcC.length);
        int[] srcV = base._asciiToBase64;
        System.arraycopy(srcV, 0, this._asciiToBase64, 0, srcV.length);

        _maxLineLength = maxLineLength;
        _paddingReadBehaviour = paddingReadBehaviour;
    }

    /*
    /**********************************************************
    /* Public accessors
    /**********************************************************
     */

    public String getName() {
        return _name;
    }

    /**
     * @return True if this Base64 encoding will <b>write</b> padding on output
     *   (note: before Jackson 2.12 also dictated whether padding was accepted on read)
     */
    public boolean usesPadding() {
        return true;
    }

    /**
     * @return {@code True} if this variant requires padding on content decoded; {@code false} if not.
     *
     * @since 2.12
     */
    public boolean requiresPaddingOnRead() {
        return _paddingReadBehaviour == PaddingReadBehaviour.PADDING_REQUIRED;
    }

    /**
     * @return {@code True} if this variant accepts padding on content decoded; {@code false} if not.
     *
     * @since 2.12
     */
    public boolean acceptsPaddingOnRead() {
        return _paddingReadBehaviour != PaddingReadBehaviour.PADDING_FORBIDDEN;
    }

    public boolean usesPaddingChar(char c) {
        return c == '=';
    }

    public boolean usesPaddingChar(int ch) {
        return ch == '=';
    }

    public char getPaddingChar() {
        return '=';
    }

    public int getMaxLineLength() {
        return _maxLineLength;
    }

    /*
    /**********************************************************
    /* Decoding support
    /**********************************************************
     */

    /**
     * @param c Character to decode
     *
     * @return 6-bit decoded value, if valid character;
     */
    public int decodeBase64Char(char c) {
        return ((int) c <= 127) ? _asciiToBase64[c] : BASE64_VALUE_INVALID;
    }

    public int decodeBase64Char(int ch) {
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }

    /*
    /**********************************************************
    /* Encoding support
    /**********************************************************
     */

    /**
     * Method that encodes given right-aligned (LSB) 24-bit value
     * into 4 base64 characters, stored in given result buffer.
     * Caller must ensure there is sufficient space for 4 encoded characters
     * at specified position.
     *
     * @param b24 3-byte value to encode
     * @param buffer Output buffer to append characters to
     * @param outPtr Starting position within {@code buffer} to append encoded characters
     *
     * @return Pointer in output buffer after appending 4 encoded characters
     */
    public int encodeBase64Chunk(int b24, char[] buffer, int outPtr) {
        buffer[outPtr++] = _base64ToAsciiC[(b24 >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(b24 >> 12) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(b24 >> 6) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[b24 & 0x3F];
        return outPtr;
    }

    /**
     * Method that outputs partial chunk (which only encodes one
     * or two bytes of data). Data given is still aligned same as if
     * it as full data; that is, missing data is at the "right end"
     * (LSB) of int.
     *
     * @param bits 24-bit chunk containing 1 or 2 bytes to encode
     * @param outputBytes Number of input bytes to encode (either 1 or 2)
     * @param buffer Output buffer to append characters to
     * @param outPtr Starting position within {@code buffer} to append encoded characters
     *
     * @return Pointer in output buffer after appending encoded characters (2, 3 or 4)
     */
    public int encodeBase64Partial(int bits, int outputBytes, char[] buffer, int outPtr) {
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 12) & 0x3F];
        if (usesPadding()) {
            buffer[outPtr++] = (outputBytes == 2) ? _base64ToAsciiC[(bits >> 6) & 0x3F] : '=';
            buffer[outPtr++] = '=';
        } else {
            if (outputBytes == 2) {
                buffer[outPtr++] = _base64ToAsciiC[(bits >> 6) & 0x3F];
            }
        }
        return outPtr;
    }

    /**
     * Method that encodes given right-aligned (LSB) 24-bit value
     * into 4 base64 bytes (ascii), stored in given result buffer.
     *
     * @param b24 3-byte value to encode
     * @param buffer Output buffer to append characters (as bytes) to
     * @param outPtr Starting position within {@code buffer} to append encoded characters
     *
     * @return Pointer in output buffer after appending 4 encoded characters
     */
    public int encodeBase64Chunk(int b24, byte[] buffer, int outPtr) {
        buffer[outPtr++] = _base64ToAsciiB[(b24 >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiB[(b24 >> 12) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiB[(b24 >> 6) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiB[b24 & 0x3F];
        return outPtr;
    }

    /**
     * Method that outputs partial chunk (which only encodes one
     * or two bytes of data). Data given is still aligned same as if
     * it as full data; that is, missing data is at the "right end"
     * (LSB) of int.
     *
     * @param bits 24-bit chunk containing 1 or 2 bytes to encode
     * @param outputBytes Number of input bytes to encode (either 1 or 2)
     * @param buffer Output buffer to append characters to
     * @param outPtr Starting position within {@code buffer} to append encoded characters
     *
     * @return Pointer in output buffer after appending encoded characters (2, 3 or 4)
     */
    public int encodeBase64Partial(int bits, int outputBytes, byte[] buffer, int outPtr) {
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 12) & 0x3F];
        if (usesPadding()) {
            byte pb = (byte) '=';
            buffer[outPtr++] = (outputBytes == 2) ? _base64ToAsciiB[(bits >> 6) & 0x3F] : pb;
            buffer[outPtr++] = pb;
        } else {
            if (outputBytes == 2) {
                buffer[outPtr++] = _base64ToAsciiB[(bits >> 6) & 0x3F];
            }
        }
        return outPtr;
    }

    /*
    /**********************************************************
    /* Convenience conversion methods for String to/from bytes use case
    /**********************************************************
     */

    /**
     * Convenience method for decoding contents of a Base64-encoded String,
     * using this variant's settings
     * and appending decoded binary data using provided {@link ByteArrayBuilder}.
     *<p>
     * NOTE: builder will NOT be reset before decoding (nor cleared afterwards);
     * assumption is that caller will ensure it is given in proper state, and
     * used as appropriate afterwards.
     *
     * @param str Input to decode
     * @param builder Builder used for assembling decoded content
     *
     * @since 2.3
     *
     * @throws IllegalArgumentException if input is not valid base64 encoded data
     */
    public void decode(String str, ByteArrayBuilder builder) throws IllegalArgumentException {
        int ptr = 0;
        int len = str.length();

        main_loop: while (true) {
            // first, we'll skip preceding white space, if any
            char ch;
            do {
                if (ptr >= len) {
                    break main_loop;
                }
                ch = str.charAt(ptr++);
            } while (ch <= INT_SPACE);
            int bits = decodeBase64Char(ch);
            if (bits < 0) {
                _reportInvalidBase64(ch, 0, null);
            }
            int decodedData = bits;
            // then second base64 char; can't get padding yet, nor ws
            if (ptr >= len) {
                _reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = decodeBase64Char(ch);
            if (bits < 0) {
                _reportInvalidBase64(ch, 1, null);
            }
            decodedData = (decodedData << 6) | bits;
            // third base64 char; can be padding, but not ws
            if (ptr >= len) {
                // but as per [JACKSON-631] can be end-of-input, iff padding is not required
                if (!requiresPaddingOnRead()) {
                    decodedData >>= 4;
                    builder.append(decodedData);
                    break;
                }
                _reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = decodeBase64Char(ch);

            // First branch: can get padding (-> 1 byte)
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    _reportInvalidBase64(ch, 2, null);
                }
                if (!acceptsPaddingOnRead()) {
                    _reportBase64UnexpectedPadding();
                }
                // Ok, must get padding
                if (ptr >= len) {
                    _reportBase64EOF();
                }
                ch = str.charAt(ptr++);
                if (!usesPaddingChar(ch)) {
                    _reportInvalidBase64(ch, 3, "expected padding character '" + getPaddingChar() + "'");
                }
                // Got 12 bits, only need 8, need to shift
                decodedData >>= 4;
                builder.append(decodedData);
                continue;
            }
            // Nope, 2 or 3 bytes
            decodedData = (decodedData << 6) | bits;
            // fourth and last base64 char; can be padding, but not ws
            if (ptr >= len) {
                // but as per [JACKSON-631] can be end-of-input, iff padding on read is not required
                if (!requiresPaddingOnRead()) {
                    decodedData >>= 2;
                    builder.appendTwoBytes(decodedData);
                    break;
                }
                _reportBase64EOF();
            }
            ch = str.charAt(ptr++);
            bits = decodeBase64Char(ch);
            if (bits < 0) {
                if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                    _reportInvalidBase64(ch, 3, null);
                }
                if (!acceptsPaddingOnRead()) {
                    _reportBase64UnexpectedPadding();
                }
                decodedData >>= 2;
                builder.appendTwoBytes(decodedData);
            } else {
                // otherwise, our triple is now complete
                decodedData = (decodedData << 6) | bits;
                builder.appendThreeBytes(decodedData);
            }
        }
    }

    /*
    /**********************************************************
    /* Overridden standard methods
    /**********************************************************
     */

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        // identity comparison should be fine
        // 26-Oct-2020, tatu: ... not any more with 2.12
        if (o == this)
            return true;
        if (o == null || o.getClass() != getClass())
            return false;

        Base64Variant other = (Base64Variant) o;
        return (other._maxLineLength == _maxLineLength)
            && (other._paddingReadBehaviour == _paddingReadBehaviour)
            && (_name.equals(other._name));
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    /*
    /**********************************************************
    /* Internal helper methods
    /**********************************************************
     */

    /**
     * @param ch Character to report on
     * @param bindex Relative index within base64 character unit; between 0
     *   and 3 (as unit has exactly 4 characters)
     * @param msg Base message to use for exception
     */
    private void _reportInvalidBase64(char ch, int bindex, String msg) throws IllegalArgumentException {
        String base;
        if (ch <= INT_SPACE) {
            base = "Illegal white space character (code 0x" + Integer.toHexString(ch) + ") as character #"
                + (bindex + 1) + " of 4-char base64 unit: can only used between units";
        } else if (usesPaddingChar(ch)) {
            base = "Unexpected padding character ('" + getPaddingChar() + "') as character #" + (bindex + 1)
                + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
        } else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            // Not sure if we can really get here... ? (most illegal xml chars are caught at lower level)
            base = "Illegal character (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        } else {
            base = "Illegal character '" + ch + "' (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        throw new IllegalArgumentException(base);
    }

    private void _reportBase64EOF() throws IllegalArgumentException {
        throw new IllegalArgumentException(missingPaddingMessage());
    }

    private void _reportBase64UnexpectedPadding() throws IllegalArgumentException {
        throw new IllegalArgumentException(unexpectedPaddingMessage());
    }

    /**
     * Helper method that will construct a message to use in exceptions for cases where input ends
     * prematurely in place where padding is not expected.
     *
     * @return Exception message for indicating "unexpected padding" case
     *
     * @since 2.12
     */
    private String unexpectedPaddingMessage() {
        return String.format(
            "Unexpected end of base64-encoded String: base64 variant '%s' expects no padding at the end while decoding. This Base64Variant might have been incorrectly configured",
            getName());
    }

    /**
     * Helper method that will construct a message to use in exceptions for cases where input ends
     * prematurely in place where padding would be expected.
     *
     * @return Exception message for indicating "missing padding" case
     *
     * @since 2.10
     */
    public String missingPaddingMessage() { // !!! TODO: why is this 'public'?
        return String.format(
            "Unexpected end of base64-encoded String: base64 variant '%s' expects padding (one or more '%c' characters) at the end. This Base64Variant might have been incorrectly configured",
            getName(), getPaddingChar());
    }
}
