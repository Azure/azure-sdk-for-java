// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.typed;

/**
 * Base class that defines decoder used for decoding multiple
 * elements of an array type. Implementations typically use
 * an embedded instance of {@link TypedValueDecoder} for
 * decoding individual values.
 *<p>
 * Set of concrete implementations is included in the reference
 * implementation of the Typed Access API.
 *
 * @author Tatu Saloranta
 *
 * @since 3.0
 */
public abstract class TypedArrayDecoder {
    /**
     * Method called to decode single (element) value that given textual
     * input contains  and store it in result array.
     *
     * @return True if decoding is complete, that is, no more
     *   elements can be added to contained array
     */
    public abstract boolean decodeValue(String input) throws IllegalArgumentException;

    public abstract boolean decodeValue(char[] buffer, int start, int end) throws IllegalArgumentException;

    /**
     * @return Number of elements decoded and contained
     */
    public abstract int getCount();

    public abstract boolean hasRoom();
}
