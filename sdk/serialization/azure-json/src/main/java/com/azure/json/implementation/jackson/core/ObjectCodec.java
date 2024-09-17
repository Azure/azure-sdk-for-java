// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

import java.io.IOException;

import com.azure.json.implementation.jackson.core.type.TypeReference;

/**
 * Abstract class that defines the interface that {@link JsonParser} and
 * {@link JsonGenerator} use to serialize and deserialize regular
 * Java objects (POJOs aka Beans).
 *<p>
 * The standard implementation of this class is
 * <code>com.fasterxml.jackson.databind.ObjectMapper</code>,
 * defined in the "jackson-databind".
 */
public abstract class ObjectCodec extends TreeCodec // since 2.3
    implements Versioned // since 2.3
{
    protected ObjectCodec() {
    }

    // Since 2.3
    @Override
    public abstract Version version();

    /*
     * /**********************************************************
     * /* API for de-serialization (JSON-to-Object)
     * /**********************************************************
     */

    /**
     * Method to deserialize JSON content into a non-container
     * type (it can be an array type, however): typically a bean, array
     * or a wrapper type (like {@link Boolean}).
     *<p>
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     *
     * @param <T> Nominal parameter for target type
     *
     * @param p Parser to use for decoding content to bind
     * @param valueType Java value type to bind content to
     *
     * @return Value deserialized
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract <T> T readValue(JsonParser p, Class<T> valueType) throws IOException;

    /**
     * Method to deserialize JSON content into a Java type, reference
     * to which is passed as argument. Type is passed using so-called
     * "super type token"
     * and specifically needs to be used if the root type is a
     * parameterized (generic) container type.
     *
     * @param <T> Nominal parameter for target type
     *
     * @param p Parser to use for decoding content to bind
     * @param valueTypeRef Java value type to bind content to
     *
     * @return Value deserialized
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract <T> T readValue(JsonParser p, TypeReference<T> valueTypeRef) throws IOException;

    /*
     * /**********************************************************
     * /* API for serialization (Object-to-JSON)
     * /**********************************************************
     */

    /**
     * Method to serialize given Java Object, using generator
     * provided.
     *
     * @param gen Generator to use for serializing value
     * @param value Value to serialize
     *
     * @throws IOException for low-level write issues, or
     *   {@link JsonGenerationException} for decoding problems
     */
    public abstract void writeValue(JsonGenerator gen, Object value) throws IOException;

    /*
     * /**********************************************************
     * /* TreeCodec pass-through methods
     * /**********************************************************
     */

    /**
     * Method for serializing JSON content from given Tree instance, using
     * specified generator.
     *
     * @param gen Generator to use for serializing value
     * @param tree Tree to serialize
     *
     * @throws IOException for low-level write issues, or
     *   {@link JsonGenerationException} for decoding problems
     */
    @Override
    public abstract void writeTree(JsonGenerator gen, TreeNode tree) throws IOException;

    /*
     * /**********************************************************
     * /* Extended tree conversions beyond TreeCodec
     * /**********************************************************
     */

    /*
     * /**********************************************************
     * /* Basic accessors
     * /**********************************************************
     */

    /**
     * @deprecated Use {@link #getFactory} instead.
     *
     * @return Underlying {@link JsonFactory} instance
     */
    @Deprecated
    public JsonFactory getJsonFactory() {
        return getFactory();
    }

    /**
     * Accessor for finding underlying data format factory
     * ({@link JsonFactory}) codec will use for data binding.
     *
     * @return Underlying {@link JsonFactory} instance
     */
    public JsonFactory getFactory() {
        return getJsonFactory();
    }
}
