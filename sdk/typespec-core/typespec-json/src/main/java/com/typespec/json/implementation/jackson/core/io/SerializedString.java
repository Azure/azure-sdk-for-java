// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.io;

import java.io.*;
import java.nio.ByteBuffer;

import com.typespec.json.implementation.jackson.core.SerializableString;

/**
 * String token that can lazily serialize String contained and then reuse that
 * serialization later on. This is similar to JDBC prepared statements, for example,
 * in that instances should only be created when they are used more than use;
 * prime candidates are various serializers.
 *<p>
 * Class is final for performance reasons and since this is not designed to
 * be extensible or customizable (customizations would occur in calling code)
 */
public class SerializedString
    implements SerializableString, java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    private static final JsonStringEncoder JSON_ENCODER = JsonStringEncoder.getInstance();
    
    protected final String _value;

    /* 13-Dec-2010, tatu: Whether use volatile or not is actually an important
     *   decision for multi-core use cases. Cost of volatility can be non-trivial
     *   for heavy use cases, and serialized-string instances are accessed often.
     *   Given that all code paths with common Jackson usage patterns go through
     *   a few memory barriers (mostly with cache/reuse pool access) it seems safe
     *   enough to omit volatiles here, given how simple lazy initialization is.
     *   This can be compared to how {@link String#hashCode} works; lazily and
     *   without synchronization or use of volatile keyword.
     *
     *   Change to remove volatile was a request by implementors of a high-throughput
     *   search framework; and they believed this is an important optimization for
     *   heaviest, multi-core deployed use cases.
     */
    /*
     * 22-Sep-2013, tatu: FWIW, there have been no reports of problems in this
     *   area, or anything pointing to it. So I think we are safe up to JDK7
     *   and hopefully beyond.
     */
    
    protected /*volatile*/ byte[] _quotedUTF8Ref;

    protected /*volatile*/ byte[] _unquotedUTF8Ref;

    protected /*volatile*/ char[] _quotedChars;

    public SerializedString(String v) {
        if (v == null) {
            throw new IllegalStateException("Null String illegal for SerializedString");
        }
        _value = v;
    }
    
    /*
    /**********************************************************
    /* Serializable overrides
    /**********************************************************
     */

    /**
     * Ugly hack, to work through the requirement that _value is indeed final,
     * and that JDK serialization won't call ctor(s).
     * 
     * @since 2.1
     */
    protected transient String _jdkSerializeValue;

    private void readObject(ObjectInputStream in) throws IOException {
        _jdkSerializeValue = in.readUTF();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(_value);
    }

    protected Object readResolve() {
        return new SerializedString(_jdkSerializeValue);
    }

    /*
    /**********************************************************
    /* API
    /**********************************************************
     */

    @Override
    public final String getValue() { return _value; }
    
    /**
     * Returns length of the String as characters
     */
    @Override
    public final int charLength() { return _value.length(); }

    /**
     * Accessor for accessing value that has been quoted (escaped) using JSON
     * quoting rules (using backslash-prefixed codes) into a char array.
     */
    @Override
    public final char[] asQuotedChars() {
        char[] result = _quotedChars;
        if (result == null) {
            _quotedChars = result = JSON_ENCODER.quoteAsString(_value);
        }
        return result;
    }

    /**
     * Accessor for accessing value that has been quoted (escaped) using JSON
     * quoting rules (using backslash-prefixed codes), and encoded using
     * UTF-8 encoding into a byte array.
     */
    @Override
    public final byte[] asQuotedUTF8() {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            _quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(_value);
        }
        return result;
    }

    /**
     * Accessor for accessing value as is (without JSON quoting (ecaping))
     * encoded as UTF-8 byte array.
     */
    @Override
    public final byte[] asUnquotedUTF8() {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            _unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(_value);
        }
        return result;
    }

    /*
    /**********************************************************
    /* Additional 2.0 methods for appending/writing contents
    /**********************************************************
     */

    @Override
    public int appendQuoted(char[] buffer, int offset) {
        char[] result = _quotedChars;
        if (result == null) {
            _quotedChars = result = JSON_ENCODER.quoteAsString(_value);
        }
        final int length = result.length;
        if ((offset + length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

    @Override
    public int appendQuotedUTF8(byte[] buffer, int offset) {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            _quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(_value);
        }
        final int length = result.length;
        if ((offset + length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

    @Override
    public int appendUnquoted(char[] buffer, int offset) {
        String str = _value;
        final int length = str.length();
        if ((offset + length) > buffer.length) {
            return -1;
        }
        str.getChars(0,  length, buffer, offset);
        return length;
    }

    @Override
    public int appendUnquotedUTF8(byte[] buffer, int offset) {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            _unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(_value);
        }
        final int length = result.length;
        if ((offset + length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

    @Override
    public int writeQuotedUTF8(OutputStream out) throws IOException {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            _quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(_value);
        }
        final int length = result.length;
        out.write(result, 0, length);
        return length;
    }

    @Override
    public int writeUnquotedUTF8(OutputStream out) throws IOException {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            _unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(_value);
        }
        final int length = result.length;
        out.write(result, 0, length);
        return length;
    }

    @Override
    public int putQuotedUTF8(ByteBuffer buffer) {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            _quotedUTF8Ref = result = JSON_ENCODER.quoteAsUTF8(_value);
        }
        final int length = result.length;
        if (length > buffer.remaining()) {
            return -1;
        }
        buffer.put(result, 0, length);
        return length;
    }

    @Override
    public int putUnquotedUTF8(ByteBuffer buffer) {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            _unquotedUTF8Ref = result = JSON_ENCODER.encodeAsUTF8(_value);
        }
        final int length = result.length;
        if (length > buffer.remaining()) {
            return -1;
        }
        buffer.put(result, 0, length);
        return length;
    }

    /*
    /**********************************************************
    /* Standard method overrides
    /**********************************************************
     */

    @Override
    public final String toString() { return _value; }
    
    @Override
    public final int hashCode() { return _value.hashCode(); }

    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;
        SerializedString other = (SerializedString) o;
        return _value.equals(other._value);
    }
}
