// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Interface that defines how Jackson package can interact with efficient
 * pre-serialized or lazily-serialized and reused String representations.
 * Typically implementations store possible serialized version(s) so that
 * serialization of String can be done more efficiently, especially when
 * used multiple times.
 *<p>
 * Note that "quoted" in methods means quoting of 'special' characters using
 * JSON backlash notation (and not use of actual double quotes).
 * 
 * @see com.typespec.json.implementation.jackson.core.io.SerializedString
 */
public interface SerializableString
{
    /**
     * Returns unquoted String that this object represents (and offers
     * serialized forms for)
     *
     * @return Unquoted String
     */
    String getValue();
    
    /**
     * Returns length of the (unquoted) String as characters.
     * Functionally equivalent to:
     *<pre>
     *   getValue().length();
     *</pre>
     *
     * @return Length of the String in characters
     */
    int charLength();

    /*
    /**********************************************************
    /* Accessors for byte sequences
    /**********************************************************
     */
    
    /**
     * Returns JSON quoted form of the String, as character array.
     * Result can be embedded as-is in textual JSON as property name or JSON String.
     *
     * @return JSON quoted form of the String as {@code char[]}
     */
    char[] asQuotedChars();

    /**
     * Returns UTF-8 encoded version of unquoted String.
     * Functionally equivalent to (but more efficient than):
     *<pre>
     * getValue().getBytes("UTF-8");
     *</pre>
     *
     * @return UTF-8 encoded version of String, without any escaping
     */
    byte[] asUnquotedUTF8();

    /**
     * Returns UTF-8 encoded version of JSON-quoted String.
     * Functionally equivalent to (but more efficient than):
     *<pre>
     * new String(asQuotedChars()).getBytes("UTF-8");
     *</pre>
     *
     * @return UTF-8 encoded version of JSON-escaped String
     */
    byte[] asQuotedUTF8();

    /*
    /**********************************************************
    /* Helper methods for appending byte/char sequences
    /**********************************************************
     */

    /**
     * Method that will append quoted UTF-8 bytes of this String into given
     * buffer, if there is enough room; if not, returns -1.
     * Functionally equivalent to:
     *<pre>
     *  byte[] bytes = str.asQuotedUTF8();
     *  System.arraycopy(bytes, 0, buffer, offset, bytes.length);
     *  return bytes.length;
     *</pre>
     *
     * @param buffer Buffer to append JSON-escaped String into
     * @param offset Offset in {@code buffer} to append String at
     *
     * @return Number of bytes appended, if successful, otherwise -1
     */
    int appendQuotedUTF8(byte[] buffer, int offset);

    /**
     * Method that will append quoted characters of this String into given
     * buffer. Functionally equivalent to:
     *<pre>
     *  char[] ch = str.asQuotedChars();
     *  System.arraycopy(ch, 0, buffer, offset, ch.length);
     *  return ch.length;
     *</pre>
     *
     * @param buffer Buffer to append JSON-escaped String into
     * @param offset Offset in {@code buffer} to append String at
     * 
     * @return Number of characters appended, if successful, otherwise -1
     */
    int appendQuoted(char[] buffer, int offset);
    
    /**
     * Method that will append unquoted ('raw') UTF-8 bytes of this String into given
     * buffer. Functionally equivalent to:
     *<pre>
     *  byte[] bytes = str.asUnquotedUTF8();
     *  System.arraycopy(bytes, 0, buffer, offset, bytes.length);
     *  return bytes.length;
     *</pre>
     *
     * @param buffer Buffer to append literal (unescaped) String into
     * @param offset Offset in {@code buffer} to append String at
     * 
     * @return Number of bytes appended, if successful, otherwise -1
     */
    int appendUnquotedUTF8(byte[] buffer, int offset);

    /**
     * Method that will append unquoted characters of this String into given
     * buffer. Functionally equivalent to:
     *<pre>
     *  char[] ch = str.getValue().toCharArray();
     *  System.arraycopy(bytes, 0, buffer, offset, ch.length);
     *  return ch.length;
     *</pre>
     *
     * @param buffer Buffer to append literal (unescaped) String into
     * @param offset Offset in {@code buffer} to append String at
     * 
     * @return Number of characters appended, if successful, otherwise -1
     */
    int appendUnquoted(char[] buffer, int offset);

    /*
    /**********************************************************
    /* Helper methods for writing out byte sequences
    /**********************************************************
     */

    /**
     * Method for writing JSON-escaped UTF-8 encoded String value using given
     * {@link java.io.OutputStream}.
     *
     * @param out {@link java.io.OutputStream} to write String into
     *
     * @return Number of bytes written
     *
     * @throws IOException if underlying stream write fails
     */
    int writeQuotedUTF8(OutputStream out) throws IOException;

    /**
     * Method for writing unescaped UTF-8 encoded String value using given
     * {@link java.io.OutputStream}.
     *
     * @param out {@link java.io.OutputStream} to write String into
     *
     * @return Number of bytes written
     *
     * @throws IOException if underlying stream write fails
     */
    int writeUnquotedUTF8(OutputStream out) throws IOException;

    /**
     * Method for appending JSON-escaped UTF-8 encoded String value into given
     * {@link java.nio.ByteBuffer}, if it fits.
     *
     * @param buffer {@link java.nio.ByteBuffer} to append String into
     *
     * @return Number of bytes put, if contents fit, otherwise -1
     *
     * @throws IOException if underlying buffer append operation fails
     */
    int putQuotedUTF8(ByteBuffer buffer) throws IOException;

    /**
     * Method for appending unquoted ('raw') UTF-8 encoded String value into given
     * {@link java.nio.ByteBuffer}, if it fits.
     *
     * @param buffer {@link java.nio.ByteBuffer} to append String into
     *
     * @return Number of bytes put, if contents fit, otherwise -1
     *
     * @throws IOException if underlying buffer append operation fails
     */
    int putUnquotedUTF8(ByteBuffer buffer) throws IOException;
}
