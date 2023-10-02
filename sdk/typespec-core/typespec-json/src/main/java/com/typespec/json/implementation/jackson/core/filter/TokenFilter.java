// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.filter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.typespec.json.implementation.jackson.core.JsonGenerator;
import com.typespec.json.implementation.jackson.core.JsonParser;

/**
 * Strategy class that can be implemented to specify actual inclusion/exclusion
 * criteria for filtering, used by {@link FilteringGeneratorDelegate}.
 *
 * @since 2.6
 */
public class TokenFilter
{

    /**
     * Enumeration that controls how TokenFilter return values are interpreted.
     *
     * @since 2.12
     */
    public enum Inclusion {
        /**
         * Tokens will only be included if the filter returns TokenFilter.INCLUDE_ALL
         */
        ONLY_INCLUDE_ALL,
        /**
         * When TokenFilter.INCLUDE_ALL is returned, the corresponding token will
         * be included as well as enclosing tokens up to the root
         */
        INCLUDE_ALL_AND_PATH,
        /**
         * Tokens will be included if any non-null filter is returned.
         * The exception is if a field name returns a non-null filter,
         * but the field value returns a null filter. In this case the
         * field name and value will both be omitted.
         */
        INCLUDE_NON_NULL
    }

    // // Marker values

    /**
     * Marker value that should be used to indicate inclusion of a structured
     * value (sub-tree representing Object or Array), or value of a named
     * property (regardless of type).
     * Note that if this instance is returned, it will used as a marker, and 
     * no actual callbacks need to be made. For this reason, it is more efficient
     * to return this instance if the whole sub-tree is to be included, instead
     * of implementing similar filter functionality explicitly.
     */
    public final static TokenFilter INCLUDE_ALL = new TokenFilter();

    // Life-cycle

    protected TokenFilter() { }

    /*
    /**********************************************************
    /* API, structured values
    /**********************************************************
     */

    /**
     * Method called to check whether Object value at current output
     * location should be included in output.
     * Three kinds of return values may be used as follows:
     *<ul>
     * <li><code>null</code> to indicate that the Object should be skipped
     *   </li>
     * <li>{@link #INCLUDE_ALL} to indicate that the Object should be included
     * completely in output
     *   </li>
     * <li>Any other {@link TokenFilter} implementation (possibly this one) to mean
     *  that further inclusion calls on return filter object need to be made
     *  on contained properties, as necessary. {@link #filterFinishObject()} will
     *  also be called on returned filter object
     *   </li>
     * </ul>
     *<p>
     * Default implementation returns <code>this</code>, which means that checks
     * are made recursively for properties of the Object to determine possible inclusion.
     * 
     * @return TokenFilter to use for further calls within Array, unless return value
     *   is <code>null</code> or {@link #INCLUDE_ALL} (which have simpler semantics)
     */
    public TokenFilter filterStartObject() {
        return this;
    }

    /**
     * Method called to check whether Array value at current output
     * location should be included in output.
     * Three kinds of return values may be used as follows:
     *<ul>
     * <li><code>null</code> to indicate that the Array should be skipped
     *   </li>
     * <li>{@link #INCLUDE_ALL} to indicate that the Array should be included
     * completely in output
     *   </li>
     * <li>Any other {@link TokenFilter} implementation (possibly this one) to mean
     *  that further inclusion calls on return filter object need to be made
     *  on contained element values, as necessary. {@link #filterFinishArray()} will
     *  also be called on returned filter object
     *   </li>
     * </ul>
     *<p>
     * Default implementation returns <code>this</code>, which means that checks
     * are made recursively for elements of the array to determine possible inclusion.
     * 
     * @return TokenFilter to use for further calls within Array, unless return value
     *   is <code>null</code> or {@link #INCLUDE_ALL} (which have simpler semantics)
     */
    public TokenFilter filterStartArray() {
        return this;
    }

    /**
     * Method called to indicate that output of non-filtered Object (one that may
     * have been included either completely, or in part) is completed,
     * in cases where filter other that {@link #INCLUDE_ALL} was returned.
     * This occurs when {@link JsonGenerator#writeEndObject()} is called.
     */
    public void filterFinishObject() { }

    /**
     * Method called to indicate that output of non-filtered Array (one that may
     * have been included either completely, or in part) is completed,
     * in cases where filter other that {@link #INCLUDE_ALL} was returned.
     * This occurs when {@link JsonGenerator#writeEndArray()} is called.
     */
    public void filterFinishArray() { }

    /*
    /**********************************************************
    /* API, properties/elements
    /**********************************************************
     */

    /**
     * Method called to check whether property value with specified name,
     * at current output location, should be included in output.
     * Three kinds of return values may be used as follows:
     *<ul>
     * <li><code>null</code> to indicate that the property and its value should be skipped
     *   </li>
     * <li>{@link #INCLUDE_ALL} to indicate that the property and its value should be included
     * completely in output
     *   </li>
     * <li>Any other {@link TokenFilter} implementation (possibly this one) to mean
     *  that further inclusion calls on returned filter object need to be made
     *  as necessary, to determine inclusion.
     *   </li>
     * </ul>
     *<p>
     * The default implementation simply returns <code>this</code> to continue calling
     * methods on this filter object, without full inclusion or exclusion.
     * 
     * @param name Name of Object property to check
     *
     * @return TokenFilter to use for further calls within property value, unless return value
     *   is <code>null</code> or {@link #INCLUDE_ALL} (which have simpler semantics)
     */
    public TokenFilter includeProperty(String name) {
        return this;
    }

    /**
     * Method called to check whether array element with specified index (zero-based),
     * at current output location, should be included in output.
     * Three kinds of return values may be used as follows:
     *<ul>
     * <li><code>null</code> to indicate that the Array element should be skipped
     *   </li>
     * <li>{@link #INCLUDE_ALL} to indicate that the Array element should be included
     * completely in output
     *   </li>
     * <li>Any other {@link TokenFilter} implementation (possibly this one) to mean
     *  that further inclusion calls on returned filter object need to be made
     *  as necessary, to determine inclusion.
     *   </li>
     * </ul>
     *<p>
     * The default implementation simply returns <code>this</code> to continue calling
     * methods on this filter object, without full inclusion or exclusion.
     *
     * @param index Array element index (0-based) to check
     *
     * @return TokenFilter to use for further calls within element value, unless return value
     *   is <code>null</code> or {@link #INCLUDE_ALL} (which have simpler semantics)
     */
    public TokenFilter includeElement(int index) {
        return this;
    }

    /**
     * Method called to check whether root-level value,
     * at current output location, should be included in output.
     * Three kinds of return values may be used as follows:
     *<ul>
     * <li><code>null</code> to indicate that the root value should be skipped
     *   </li>
     * <li>{@link #INCLUDE_ALL} to indicate that the root value should be included
     * completely in output
     *   </li>
     * <li>Any other {@link TokenFilter} implementation (possibly this one) to mean
     *  that further inclusion calls on returned filter object need to be made
     *  as necessary, to determine inclusion.
     *   </li>
     * </ul>
     *<p>
     * The default implementation simply returns <code>this</code> to continue calling
     * methods on this filter object, without full inclusion or exclusion.
     *
     * @param index Index (0-based) of the root value to check
     *
     * @return TokenFilter to use for further calls within root value, unless return value
     *   is <code>null</code> or {@link #INCLUDE_ALL} (which have simpler semantics)
     */
    public TokenFilter includeRootValue(int index) {
        return this;
    }

    /*
    /**********************************************************
    /* API, scalar values (being read)
    /**********************************************************
     */

    /**
     * Call made when verifying whether a scalar value is being
     * read from a parser.
     *<p>
     * Default action is to call <code>_includeScalar()</code> and return
     * whatever it indicates.
     *
     * @param p Parser that points to the value (typically {@code delegate}
     *    parser, not filtering parser that wraps it)
     *
     * @return True if scalar value is to be included; false if not
     *
     * @throws IOException if there are any problems reading content (typically
     *   via calling passed-in {@code JsonParser})
     */
    public boolean includeValue(JsonParser p) throws IOException {
        return _includeScalar();
    }

    /*
    /**********************************************************
    /* API, scalar values (being written)
    /**********************************************************
     */

    /**
     * Call made to verify whether leaf-level
     * boolean value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeBoolean(boolean value) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * null value
     * should be included in output or not.
     *
     * @return True if ({@code null}) value is to be included; false if not
     */
    public boolean includeNull() {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * String value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeString(String value) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * "streaming" String value
     * should be included in output or not.
     *<p>
     * NOTE: note that any reads from passed in {@code Reader} may lead
     * to actual loss of content to write; typically method should NOT
     * access content passed via this method.
     *
     * @param r Reader used to pass String value to parser
     * @param maxLen indicated maximum length of String value
     *
     * @return True if value is to be included; false if not
     *
     * @since 2.11
     */
    public boolean includeString(java.io.Reader r, int maxLen) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * <code>int</code> value
     * should be included in output or not.
     * 
     * NOTE: also called for `short`, `byte`
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeNumber(int value) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * <code>long</code> value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeNumber(long value) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * <code>float</code> value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeNumber(float value) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * <code>double</code> value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeNumber(double value) {
        return _includeScalar();
    }
    
    /**
     * Call made to verify whether leaf-level
     * {@link BigDecimal} value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeNumber(BigDecimal value) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * {@link BigInteger} value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeNumber(BigInteger value) {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * Binary value
     * should be included in output or not.
     *<p>
     * NOTE: no binary payload passed; assumption is this won't be of much use.
     *
     * @return True if the binary value is to be included; false if not
     */
    public boolean includeBinary() {
        return _includeScalar();
    }

    /**
     * Call made to verify whether leaf-level
     * raw (pre-encoded, not quoted by generator) value
     * should be included in output or not.
     *<p>
     * NOTE: value itself not passed since it may come on multiple forms
     * and is unlikely to be of much use in determining inclusion
     * criteria.
     *
     * @return True if the raw value is to be included; false if not
     */
    public boolean includeRawValue() {
        return _includeScalar();
    }
    
    /**
     * Call made to verify whether leaf-level
     * embedded (Opaque) value
     * should be included in output or not.
     *
     * @param value Value to check
     *
     * @return True if value is to be included; false if not
     */
    public boolean includeEmbeddedValue(Object value) {
        return _includeScalar();
    }

    /*
    /**********************************************************
    /* Overrides
    /**********************************************************
     */

    @Override
    public String toString() {
        if (this == INCLUDE_ALL) {
            return "TokenFilter.INCLUDE_ALL";
        }
        return super.toString();
    }

    /*
    /**********************************************************
    /* Other methods
    /**********************************************************
     */
    
    /**
     * Overridable default implementation delegated to all scalar value
     * inclusion check methods.
     * The default implementation simply includes all leaf values.
     *
     * @return Whether all leaf scalar values should be included ({@code true})
     *    or not ({@code false})
     */
    protected boolean _includeScalar() {
        return true;
    }
}
