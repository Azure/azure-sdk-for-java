// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.typed;

/**
 * Base class that defines generic typed value decoder API used
 * by {@link TypedXMLStreamReader} to allow for efficient customized
 * decoding of textual xml content into actual typed values.
 * Set of concrete decoders is also included in the reference
 * implementation of the Typed Access API.
 *<p>
 * Details of how value decoded is to be accessed is NOT defined
 * as part of this interface: since decoders are explicitly passed
 * by callers, they can (and need to) use more specific sub-classes
 * with value access method or methods.
 *<p>
 * Note: to allow for optimal efficiency, there are multiple
 * decode methods, one of which gets called during decoding process.
 * This is necessary since the stream reader implementations may
 * use different internal representations, either in general (an
 * implementation might stored everyting as Strings; another
 * as character arrays).
 *
 * @author Tatu Saloranta
 *
 * @since 3.0
 */
public abstract class TypedValueDecoder {
    /**
     * Method used to invoke decoding functionality, for decoding
     * the value encoded in given substring.
     * It is to try decoding value, and either store decoded value
     * for later access (using method(s) caller knows about), or
     * throw an exception to indicate problem encountered.
     *<p>
     * Note: method will get called with "trimmed" input, i.e. input
     * will never have any leading or trailing white space.
     * It will also never be called with empty content
     * ({@link #handleEmptyValue} is called instead for such cases)
     */
    public abstract void decode(String input) throws IllegalArgumentException;

    /**
     * Method used to invoke decoding functionality, for decoding
     * the value encoded in given portion of character array
     * It is to try decoding value, and either store decoded value
     * for later access (using method(s) caller knows about), or
     * throw an exception to indicate problem encountered.
     *<p>
     * Note: method will get called with "trimmed" input, i.e. input
     * will never have any leading or trailing white space.
     * It will also never be called with empty content
     * ({@link #handleEmptyValue} is called instead for such cases)
     */
    public abstract void decode(char[] buffer, int start, int end) throws IllegalArgumentException;

    /**
     * Method called in cases where value to decode would be empty, after
     * trimming leading and trailing white space. Decoder can
     * then either change its state (to contain value to return)
     * or throw appropriate exception 
     */
    public abstract void handleEmptyValue() throws IllegalArgumentException;
}
