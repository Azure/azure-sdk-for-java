// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.typespec.json.implementation.jackson.core;

import java.util.Arrays;

import com.typespec.json.implementation.jackson.core.util.ByteArrayBuilder;

/**
 * Class used to define specific details of which
 * variant of Base64 encoding/decoding is to be used. Although there is
 * somewhat standard basic version (so-called "MIME Base64"), other variants
 * exists, see <a href="http://en.wikipedia.org/wiki/Base64">Base64 Wikipedia entry</a> for details.
 * 
 * @author Tatu Saloranta
 */
public final class Base64Variant
    implements java.io.Serializable
{
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
        ;
    }

    private final static int INT_SPACE = 0x20;
    
    // We'll only serialize name
    private static final long serialVersionUID = 1L;

    /**
     * Placeholder used by "no padding" variant, to be used when a character
     * value is needed.
     */
    protected final static char PADDING_CHAR_NONE = '\0';

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
     * Character used for padding, if any ({@link #PADDING_CHAR_NONE} if not).
     */
    private final char _paddingChar;

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
     * Whether this variant uses padding when writing out content or not.
     *
     * @since 2.12
     */
    private final boolean _writePadding;

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

    public Base64Variant(String name, String base64Alphabet, boolean writePadding, char paddingChar, int maxLineLength)
    {
        _name = name;
        _writePadding = writePadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;

        // Ok and then we need to create codec tables.

        // First the main encoding table:
        int alphaLen = base64Alphabet.length();
        if (alphaLen != 64) {
            throw new IllegalArgumentException("Base64Alphabet length must be exactly 64 (was "+alphaLen+")");
        }

        // And then secondary encoding table and decoding table:
        base64Alphabet.getChars(0, alphaLen, _base64ToAsciiC, 0);
        Arrays.fill(_asciiToBase64, BASE64_VALUE_INVALID);
        for (int i = 0; i < alphaLen; ++i) {
            char alpha = _base64ToAsciiC[i];
            _base64ToAsciiB[i] = (byte) alpha;
            _asciiToBase64[alpha] = i;
        }

        // Plus if we use padding, add that in too
        if (writePadding) {
            _asciiToBase64[(int) paddingChar] = BASE64_VALUE_PADDING;
        }

        // By default, require padding on input if written on output; do not
        // accept if padding not written
        _paddingReadBehaviour = writePadding
                ? PaddingReadBehaviour.PADDING_REQUIRED
                : PaddingReadBehaviour.PADDING_FORBIDDEN;
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
    public Base64Variant(Base64Variant base,
            String name, int maxLineLength)
    {
        this(base, name, base._writePadding, base._paddingChar, maxLineLength);
    }

    /**
     * "Copy constructor" that can be used when the base alphabet is identical
     * to one used by another variant, but other details (padding, maximum
     * line length) differ
     *
     * @param base Variant to use for settings not specific by other parameters
     * @param name Name of this variant
     * @param writePadding Whether variant will use padding when encoding
     * @param paddingChar Padding character used for encoding, excepted on reading, if any
     * @param maxLineLength Maximum length (in characters) of lines to output before
     *    using linefeed
     */
    public Base64Variant(Base64Variant base,
            String name, boolean writePadding, char paddingChar, int maxLineLength)
    {
        this(base, name, writePadding, paddingChar, base._paddingReadBehaviour, maxLineLength);
    }

    private Base64Variant(Base64Variant base,
            String name, boolean writePadding, char paddingChar, PaddingReadBehaviour paddingReadBehaviour, int maxLineLength)
    {
        _name = name;
        byte[] srcB = base._base64ToAsciiB;
        System.arraycopy(srcB, 0, this._base64ToAsciiB, 0, srcB.length);
        char[] srcC = base._base64ToAsciiC;
        System.arraycopy(srcC, 0, this._base64ToAsciiC, 0, srcC.length);
        int[] srcV = base._asciiToBase64;
        System.arraycopy(srcV, 0, this._asciiToBase64, 0, srcV.length);

        _writePadding = writePadding;
        _paddingChar = paddingChar;
        _maxLineLength = maxLineLength;
        _paddingReadBehaviour = paddingReadBehaviour;
    }

    private Base64Variant(Base64Variant base, PaddingReadBehaviour paddingReadBehaviour) {
        this(base, base._name, base._writePadding, base._paddingChar, paddingReadBehaviour, base._maxLineLength);
    }

    /**
     * @return Base64Variant which does not require padding on read
     *
     * @since 2.12
     */
    public Base64Variant withPaddingAllowed() {
        return withReadPadding(PaddingReadBehaviour.PADDING_ALLOWED);
    }

    /**
     * @return Base64Variant which requires padding on read
     * @since 2.12
     */
    public Base64Variant withPaddingRequired() {
        return withReadPadding(PaddingReadBehaviour.PADDING_REQUIRED);
    }

    /**
     * @return Base64Variant which does not accept padding on read
     * @since 2.12
     */
    public Base64Variant withPaddingForbidden() {
        return withReadPadding(PaddingReadBehaviour.PADDING_FORBIDDEN);
    }

    /**
     * @param readPadding Padding read behavior desired
     *
     * @return Instance with desired padding read behavior setting (this
     *   if already has setting; new instance otherwise)
     *
     * @since 2.12
     */
    public Base64Variant withReadPadding(PaddingReadBehaviour readPadding) {
        return (readPadding == _paddingReadBehaviour) ? this
                : new Base64Variant(this, readPadding);
    }

    /**
     * @param writePadding Determines if padding is output on write or not
     *
     * @return Base64Variant which writes padding or not depending on writePadding
     *
     * @since 2.12
     */
    public Base64Variant withWritePadding(boolean writePadding) {
        return (writePadding == _writePadding) ? this
                : new Base64Variant(this, _name, writePadding, _paddingChar, _maxLineLength);
    }

    /*
    /**********************************************************
    /* Serializable overrides
    /**********************************************************
     */

    // 26-Oct-2020, tatu: Much more complicated with 2.12 as it is
    //   possible to create differently configured instances.
    //   Need to start with name to regenerate tables etc but then
    //   handle overrides
    protected Object readResolve() {
        Base64Variant base = Base64Variants.valueOf(_name);
        if ((_writePadding != base._writePadding)
                || (_paddingChar != base._paddingChar)
                || (_paddingReadBehaviour != base._paddingReadBehaviour)
                || (_maxLineLength != base._maxLineLength)
                || (_writePadding != base._writePadding)
                ) {
            return new Base64Variant(base,
                    _name, _writePadding, _paddingChar, _paddingReadBehaviour, _maxLineLength);
        }
        return base;
    }

    /*
    /**********************************************************
    /* Public accessors
    /**********************************************************
     */

    public String getName() { return _name; }

    /**
     * @return True if this Base64 encoding will <b>write</b> padding on output
     *   (note: before Jackson 2.12 also dictated whether padding was accepted on read)
     */
    public boolean usesPadding() { return _writePadding; }

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

    public boolean usesPaddingChar(char c) { return c == _paddingChar; }
    public boolean usesPaddingChar(int ch) { return ch == (int) _paddingChar; }

    /**
     * @return Indicator on how this Base64 encoding will handle possible padding
     *   in content when reading.
     *
     * @since 2.12
     */
    public PaddingReadBehaviour paddingReadBehaviour() { return _paddingReadBehaviour; }

    public char getPaddingChar() { return _paddingChar; }
    public byte getPaddingByte() { return (byte)_paddingChar; }

    public int getMaxLineLength() { return _maxLineLength; }

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
    public int decodeBase64Char(char c)
    {
        int ch = (int) c;
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }

    public int decodeBase64Char(int ch)
    {
        return (ch <= 127) ? _asciiToBase64[ch] : BASE64_VALUE_INVALID;
    }

    public int decodeBase64Byte(byte b)
    {
        int ch = (int) b;
        // note: cast retains sign, so it's from -128 to +127
        if (ch < 0) {
            return BASE64_VALUE_INVALID;
        }
        return _asciiToBase64[ch];
    }

    /*
    /**********************************************************
    /* Encoding support
    /**********************************************************
     */

    public char encodeBase64BitsAsChar(int value)
    {
        // Let's assume caller has done necessary checks; this
        // method must be fast and inlinable
        return _base64ToAsciiC[value];
    }

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
    public int encodeBase64Chunk(int b24, char[] buffer, int outPtr)
    {
        buffer[outPtr++] = _base64ToAsciiC[(b24 >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(b24 >> 12) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(b24 >> 6) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[b24 & 0x3F];
        return outPtr;
    }

    public void encodeBase64Chunk(StringBuilder sb, int b24)
    {
        sb.append(_base64ToAsciiC[(b24 >> 18) & 0x3F]);
        sb.append(_base64ToAsciiC[(b24 >> 12) & 0x3F]);
        sb.append(_base64ToAsciiC[(b24 >> 6) & 0x3F]);
        sb.append(_base64ToAsciiC[b24 & 0x3F]);
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
    public int encodeBase64Partial(int bits, int outputBytes, char[] buffer, int outPtr)
    {
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiC[(bits >> 12) & 0x3F];
        if (usesPadding()) {
            buffer[outPtr++] = (outputBytes == 2) ?
                _base64ToAsciiC[(bits >> 6) & 0x3F] : _paddingChar;
            buffer[outPtr++] = _paddingChar;
        } else {
            if (outputBytes == 2) {
                buffer[outPtr++] = _base64ToAsciiC[(bits >> 6) & 0x3F];
            }
        }
        return outPtr;
    }

    public void encodeBase64Partial(StringBuilder sb, int bits, int outputBytes)
    {
        sb.append(_base64ToAsciiC[(bits >> 18) & 0x3F]);
        sb.append(_base64ToAsciiC[(bits >> 12) & 0x3F]);
        if (usesPadding()) {
            sb.append((outputBytes == 2) ?
                      _base64ToAsciiC[(bits >> 6) & 0x3F] : _paddingChar);
            sb.append(_paddingChar);
        } else {
            if (outputBytes == 2) {
                sb.append(_base64ToAsciiC[(bits >> 6) & 0x3F]);
            }
        }
    }

    public byte encodeBase64BitsAsByte(int value)
    {
        // As with above, assuming it is 6-bit value
        return _base64ToAsciiB[value];
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
    public int encodeBase64Chunk(int b24, byte[] buffer, int outPtr)
    {
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
    public int encodeBase64Partial(int bits, int outputBytes, byte[] buffer, int outPtr)
    {
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 18) & 0x3F];
        buffer[outPtr++] = _base64ToAsciiB[(bits >> 12) & 0x3F];
        if (usesPadding()) {
            byte pb = (byte) _paddingChar;
            buffer[outPtr++] = (outputBytes == 2) ?
                _base64ToAsciiB[(bits >> 6) & 0x3F] : pb;
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
     * Convenience method for converting given byte array as base64 encoded
     * String using this variant's settings.
     * Resulting value is "raw", that is, not enclosed in double-quotes.
     * 
     * @param input Byte array to encode
     *
     * @return Base64 encoded String of encoded {@code input} bytes, not surrounded by quotes
     */
    public String encode(byte[] input)
    {
        return encode(input, false);
    }

    /**
     * Convenience method for converting given byte array as base64 encoded String
     * using this variant's settings, optionally enclosed in double-quotes.
     * Linefeeds added, if needed, are expressed as 2-character JSON (and Java source)
     * escape sequence of backslash + `n`.
     * 
     * @param input Byte array to encode
     * @param addQuotes Whether to surround resulting value in double quotes or not
     *
     * @return Base64 encoded String of encoded {@code input} bytes, possibly
     *     surrounded by quotes (if {@code addQuotes} enabled)
     */
    public String encode(byte[] input, boolean addQuotes)
    {
        final int inputEnd = input.length;
        final StringBuilder sb = new StringBuilder(inputEnd + (inputEnd >> 2) + (inputEnd >> 3));
        if (addQuotes) {
            sb.append('"');
        }

        int chunksBeforeLF = getMaxLineLength() >> 2;

        // Ok, first we loop through all full triplets of data:
        int inputPtr = 0;
        int safeInputEnd = inputEnd-3; // to get only full triplets

        while (inputPtr <= safeInputEnd) {
            // First, mash 3 bytes into lsb of 32-bit int
            int b24 = ((int) input[inputPtr++]) << 8;
            b24 |= ((int) input[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) input[inputPtr++]) & 0xFF);
            encodeBase64Chunk(sb, b24);
            if (--chunksBeforeLF <= 0) {
                // note: must quote in JSON value, so not really useful...
                sb.append('\\');
                sb.append('n');
                chunksBeforeLF = getMaxLineLength() >> 2;
            }
        }

        // And then we may have 1 or 2 leftover bytes to encode
        int inputLeft = inputEnd - inputPtr; // 0, 1 or 2
        if (inputLeft > 0) { // yes, but do we have room for output?
            int b24 = ((int) input[inputPtr++]) << 16;
            if (inputLeft == 2) {
                b24 |= (((int) input[inputPtr++]) & 0xFF) << 8;
            }
            encodeBase64Partial(sb, b24, inputLeft);
        }

        if (addQuotes) {
            sb.append('"');
        }
        return sb.toString();
    }

    /**
     * Convenience method for converting given byte array as base64 encoded String
     * using this variant's settings, optionally enclosed in double-quotes.
     * Linefeed character to use is passed explicitly.
     * 
     * @param input Byte array to encode
     * @param addQuotes Whether to surround resulting value in double quotes or not
     * @param linefeed Linefeed to use for encoded content
     *
     * @return Base64 encoded String of encoded {@code input} bytes
     *
     * @since 2.10
     */
    public String encode(byte[] input, boolean addQuotes, String linefeed)
    {
        final int inputEnd = input.length;
        final StringBuilder sb = new StringBuilder(inputEnd + (inputEnd >> 2) + (inputEnd >> 3));
        if (addQuotes) {
            sb.append('"');
        }

        int chunksBeforeLF = getMaxLineLength() >> 2;

        int inputPtr = 0;
        int safeInputEnd = inputEnd-3;

        while (inputPtr <= safeInputEnd) {
            int b24 = ((int) input[inputPtr++]) << 8;
            b24 |= ((int) input[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) input[inputPtr++]) & 0xFF);
            encodeBase64Chunk(sb, b24);
            if (--chunksBeforeLF <= 0) {
                sb.append(linefeed);
                chunksBeforeLF = getMaxLineLength() >> 2;
            }
        }
        int inputLeft = inputEnd - inputPtr;
        if (inputLeft > 0) {
            int b24 = ((int) input[inputPtr++]) << 16;
            if (inputLeft == 2) {
                b24 |= (((int) input[inputPtr++]) & 0xFF) << 8;
            }
            encodeBase64Partial(sb, b24, inputLeft);
        }

        if (addQuotes) {
            sb.append('"');
        }
        return sb.toString();
    }

    /**
     * Convenience method for decoding contents of a Base64-encoded String,
     * using this variant's settings.
     *
     * @param input Base64-encoded input String to decode
     *
     * @return Byte array of decoded contents
     * 
     * @since 2.3
     *
     * @throws IllegalArgumentException if input is not valid base64 encoded data
     */
    @SuppressWarnings("resource")
    public byte[] decode(String input) throws IllegalArgumentException
    {
        ByteArrayBuilder b = new ByteArrayBuilder();
        decode(input, b);
        return b.toByteArray();
    }

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
    public void decode(String str, ByteArrayBuilder builder) throws IllegalArgumentException
    {
        int ptr = 0;
        int len = str.length();

    main_loop:
        while (true) {
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
                    _reportInvalidBase64(ch, 3, "expected padding character '"+getPaddingChar()+"'");
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
    public String toString() { return _name; }

    @Override
    public boolean equals(Object o) {
        // identity comparison should be fine
        // 26-Oct-2020, tatu: ... not any more with 2.12
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;

        Base64Variant other = (Base64Variant) o;
        return (other._paddingChar == _paddingChar)
                && (other._maxLineLength == _maxLineLength)
                && (other._writePadding == _writePadding)
                && (other._paddingReadBehaviour == _paddingReadBehaviour)
                && (_name.equals(other._name))
                ;
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
    protected void _reportInvalidBase64(char ch, int bindex, String msg)
        throws IllegalArgumentException
    {
        String base;
        if (ch <= INT_SPACE) {
            base = "Illegal white space character (code 0x"+Integer.toHexString(ch)+") as character #"+(bindex+1)+" of 4-char base64 unit: can only used between units";
        } else if (usesPaddingChar(ch)) {
            base = "Unexpected padding character ('"+getPaddingChar()+"') as character #"+(bindex+1)+" of 4-char base64 unit: padding only legal as 3rd or 4th character";
        } else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            // Not sure if we can really get here... ? (most illegal xml chars are caught at lower level)
            base = "Illegal character (code 0x"+Integer.toHexString(ch)+") in base64 content";
        } else {
            base = "Illegal character '"+ch+"' (code 0x"+Integer.toHexString(ch)+") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        throw new IllegalArgumentException(base);
    }

    protected void _reportBase64EOF() throws IllegalArgumentException {
        throw new IllegalArgumentException(missingPaddingMessage());
    }

    protected void _reportBase64UnexpectedPadding() throws IllegalArgumentException {
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
    protected String unexpectedPaddingMessage() {
        return String.format("Unexpected end of base64-encoded String: base64 variant '%s' expects no padding at the end while decoding. This Base64Variant might have been incorrectly configured",
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
        return String.format("Unexpected end of base64-encoded String: base64 variant '%s' expects padding (one or more '%c' characters) at the end. This Base64Variant might have been incorrectly configured",
                getName(), getPaddingChar());
    }
}
