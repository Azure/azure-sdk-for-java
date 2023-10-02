// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.type;

import com.typespec.json.implementation.jackson.core.JsonToken;

/**
 * This is a simple value class used between core streaming and higher level
 * databinding to pass information about type ids to write.
 * Properties are exposed and mutable on purpose: they are only used for communication
 * over serialization of a single value, and neither retained across calls nor shared
 * between threads.
 *<p>
 * Usual usage pattern is such that instance of this class is passed on two calls that are
 * needed for outputting type id (and possible additional wrapping, depending on format;
 * JSON, for example, requires wrapping as type id is part of regular data): first, a "prefix"
 * write (which usually includes actual id), performed before value write; and then
 * matching "suffix" write after value serialization.
 *
 * @since 2.9
 */
public class WritableTypeId
{
    /**
     * Enumeration of values that matches enum `As` from annotation
     * `JsonTypeInfo`: separate definition to avoid dependency between
     * streaming core and annotations packages; also allows more flexibility
     * in case new values needed at this level of internal API.
     *<p>
     * NOTE: in most cases this only matters with formats that do NOT have native
     * type id capabilities, and require type id to be included within regular
     * data (whether exposed as Java properties or not). Formats with native
     * types usually use native type id functionality regardless, unless
     * overridden by a feature to use "non-native" type inclusion.
     */
    public enum Inclusion {
        /**
         * Inclusion as wrapper Array (1st element type id, 2nd element value).
         *<p>
         * Corresponds to <code>JsonTypeInfo.As.WRAPPER_ARRAY</code>.
         */
        WRAPPER_ARRAY,

        /**
         * Inclusion as wrapper Object that has one key/value pair where type id
         * is the key for typed value.
         *<p>
         * Corresponds to <code>JsonTypeInfo.As.WRAPPER_OBJECT</code>.
         */
        WRAPPER_OBJECT,

        /**
         * Inclusion as a property within Object to write, but logically as separate
         * metadata that is not exposed as payload to caller: that is, does not match
         * any of visible properties value object has.
         *<p>
         * NOTE: if shape of typed value to write is NOT Object, will instead use
         * {@link #WRAPPER_ARRAY} inclusion.
         *<p>
         * Corresponds to <code>JsonTypeInfo.As.PROPERTY</code>.
         */
        METADATA_PROPERTY,

        /**
         * Inclusion as a "regular" property within Object to write; this implies that
         * its value should come from regular POJO property on serialization, and
         * be deserialized into such property. This handling, however, is up to databinding.
         *<p>
         * Regarding handling, type id is ONLY written as native type id; if no native
         * type ids available, caller is assumed to handle output some other way.
         * This is different from {@link #METADATA_PROPERTY}.
         *<p>
         * NOTE: if shape of typed value to write is NOT Object, will instead use
         * {@link #WRAPPER_ARRAY} inclusion.
         *<p>
         * Corresponds to <code>JsonTypeInfo.As.EXISTING_PROPERTY</code>.
         */
        PAYLOAD_PROPERTY,

        /**
         * Inclusion as a property within "parent" Object of value Object to write.
         * This typically requires slightly convoluted processing in which property
         * that contains type id is actually written <b>after</b> typed value object
         * itself is written.
         *<br>
         * Note that it is illegal to call write method if the current (parent) write context
         * is not Object: no coercion is done for other inclusion types (unlike with
         * other <code>xxx_PROPERTY</code> choices.
         * This also means that root values MAY NOT use this type id inclusion mechanism
         * (as they have no parent context).
         *<p>
         * Corresponds to <code>JsonTypeInfo.As.EXTERNAL_PROPERTY</code>.
         */
        PARENT_PROPERTY;

        public boolean requiresObjectContext() {
            return (this == METADATA_PROPERTY) || (this == PAYLOAD_PROPERTY);
        }
    }
    
    /**
     * Java object for which type id is being written. Not needed by default handling,
     * but may be useful for customized format handling.
     */
    public Object forValue;

    /**
     * (optional) Super-type of {@link #forValue} to use for type id generation (if no
     * explicit id passed): used instead of actual class of {@link #forValue} in cases
     * where we do not want to use the "real" type but something more generic, usually
     * to work around specific problem with implementation type, or its deserializer.
     */
    public Class<?> forValueType;

    /**
     * Actual type id to use: usually {link java.lang.String}.
     */
    public Object id;

    /**
     * If type id is to be embedded as a regular property, name of the property;
     * otherwise `null`.
     *<p>
     * NOTE: if "wrap-as-Object" is used, this does NOT contain property name to
     * use but `null`.
     */
    public String asProperty;

    /**
     * Property used to indicate style of inclusion for this type id, in cases where
     * no native type id may be used (either because format has none, like JSON; or
     * because use of native type ids is disabled [with YAML]).
     */
    public Inclusion include;

    /**
     * Information about intended shape of the value being written (that is, {@link #forValue});
     * in case of structured values, start token of the structure; for scalars, value token.
     * Main difference is between structured values
     * ({@link JsonToken#START_ARRAY}, {@link JsonToken#START_OBJECT})
     * and scalars ({@link JsonToken#VALUE_STRING}): specific scalar type may not be
     * important for processing.
     */
    public JsonToken valueShape;

    /**
     * Flag that can be set to indicate that wrapper structure was written (during
     * prefix-writing); used to determine if suffix requires matching close markers.
     */
    public boolean wrapperWritten;

    /**
     * Optional additional information that generator may add during "prefix write",
     * to be available on matching "suffix write".
     */
    public Object extra;
    
    public WritableTypeId() { }

    /**
     * Constructor used when calling a method for generating and writing Type Id;
     * caller only knows value object and its intended shape.
     *
     * @param value Actual value for which type information is written
     * @param valueShape Serialize shape writer will use for value
     */
    public WritableTypeId(Object value, JsonToken valueShape) {
        this(value, valueShape, null);
    }

    /**
     * Constructor used when calling a method for generating and writing Type Id,
     * but where actual type to use for generating id is NOT the type of value
     * (but its supertype).
     *
     * @param value Actual value for which type information is written
     * @param valueType Effective type of {@code value} to use for Type Id generation
     * @param valueShape Serialize shape writer will use for value
     */
    public WritableTypeId(Object value, Class<?> valueType, JsonToken valueShape) {
        this(value, valueShape, null);
        forValueType = valueType;
    }

    /**
     * Constructor used when calling a method for writing Type Id;
     * caller knows value object, its intended shape as well as id to
     * use; but not details of wrapping (if any).
     *
     * @param value Actual value for which type information is written
     * @param valueShape Serialize shape writer will use for value
     * @param id Actual type id to use if known; {@code null} if not
     */
    public WritableTypeId(Object value, JsonToken valueShape, Object id)
    {
        forValue = value;
        this.id = id;
        this.valueShape = valueShape;
    }
}
