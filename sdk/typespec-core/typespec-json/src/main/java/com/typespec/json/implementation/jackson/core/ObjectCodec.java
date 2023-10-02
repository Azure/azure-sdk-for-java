// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

import java.io.IOException;
import java.util.Iterator;

import com.typespec.json.implementation.jackson.core.type.ResolvedType;
import com.typespec.json.implementation.jackson.core.type.TypeReference;

/**
 * Abstract class that defines the interface that {@link JsonParser} and
 * {@link JsonGenerator} use to serialize and deserialize regular
 * Java objects (POJOs aka Beans).
 *<p>
 * The standard implementation of this class is
 * <code>com.fasterxml.jackson.databind.ObjectMapper</code>,
 * defined in the "jackson-databind".
 */
public abstract class ObjectCodec
    extends TreeCodec // since 2.3
    implements Versioned // since 2.3
{
    protected ObjectCodec() { }

    // Since 2.3
    @Override
    public abstract Version version();
    
    /*
    /**********************************************************
    /* API for de-serialization (JSON-to-Object)
    /**********************************************************
     */

    /**
     * Method to deserialize JSON content into a non-container
     * type (it can be an array type, however): typically a bean, array
     * or a wrapper type (like {@link java.lang.Boolean}).
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
    public abstract <T> T readValue(JsonParser p, Class<T> valueType)
        throws IOException;

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
    public abstract <T> T readValue(JsonParser p, TypeReference<T> valueTypeRef)
        throws IOException;

    /**
     * Method to deserialize JSON content into a POJO, type specified
     * with fully resolved type object (so it can be a generic type,
     * including containers like {@link java.util.Collection} and
     * {@link java.util.Map}).
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
    public abstract <T> T readValue(JsonParser p, ResolvedType valueType)
        throws IOException;

    /**
     * Method for reading sequence of Objects from parser stream,
     * all with same specified value type.
     *
     * @param <T> Nominal parameter for target type
     *
     * @param p Parser to use for decoding content to bind
     * @param valueType Java value type to bind content to
     *
     * @return Iterator for incrementally deserializing values
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract <T> Iterator<T> readValues(JsonParser p, Class<T> valueType)
        throws IOException;

    /**
     * Method for reading sequence of Objects from parser stream,
     * all with same specified value type.
     *
     * @param <T> Nominal parameter for target type
     *
     * @param p Parser to use for decoding content to bind
     * @param valueTypeRef Java value type to bind content to
     *
     * @return Iterator for incrementally deserializing values
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract <T> Iterator<T> readValues(JsonParser p, TypeReference<T> valueTypeRef)
        throws IOException;
    
    /**
     * Method for reading sequence of Objects from parser stream,
     * all with same specified value type.
     *
     * @param <T> Nominal parameter for target type
     *
     * @param p Parser to use for decoding content to bind
     * @param valueType Java value type to bind content to
     *
     * @return Iterator for incrementally deserializing values
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract <T> Iterator<T> readValues(JsonParser p, ResolvedType valueType)
        throws IOException;

    /*
    /**********************************************************
    /* API for serialization (Object-to-JSON)
    /**********************************************************
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
    /**********************************************************
    /* TreeCodec pass-through methods
    /**********************************************************
     */

    /**
     * Method for deserializing JSON content as tree expressed
     * using set of {@link TreeNode} instances. Returns
     * root of the resulting tree (where root can consist
     * of just a single node if the current event is a
     * value event, not container). Empty or whitespace
     * documents return null.
     *
     * @return next tree from {@code p}, or {@code null} if empty.
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    @Override
    public abstract <T extends TreeNode> T readTree(JsonParser p) throws IOException;

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
    
    /**
     * Method for construct root level Object nodes
     * for Tree Model instances.
     *
     * @return Object node created
     */
    @Override
    public abstract TreeNode createObjectNode();

    /**
     * Method for construct root level Array nodes
     * for Tree Model instances.
     *
     * @return Array node created
     */
    @Override
    public abstract TreeNode createArrayNode();

    /**
     * Method for constructing a {@link JsonParser} for reading
     * contents of a JSON tree, as if it was external serialized
     * JSON content.
     *
     * @param n Content to traverse over
     *
     * @return Parser constructed for traversing over contents of specified node
     */
    @Override
    public abstract JsonParser treeAsTokens(TreeNode n);

    /*
    /**********************************************************
    /* Extended tree conversions beyond TreeCodec
    /**********************************************************
     */
    
    /**
     * Convenience method for converting given JSON tree into instance of specified
     * value type. This is equivalent to first constructing a {@link JsonParser} to
     * iterate over contents of the tree, and using that parser for data binding.
     *
     * @param <T> Nominal parameter for target type
     *
     * @param n Tree to convert
     * @param valueType Java target value type to convert content to
     *
     * @return Converted value instance
     *
     * @throws JsonProcessingException if structural conversion fails
     */
    public abstract <T> T treeToValue(TreeNode n, Class<T> valueType)
        throws JsonProcessingException;

    /*
    /**********************************************************
    /* Basic accessors
    /**********************************************************
     */

    /**
     * @deprecated Use {@link #getFactory} instead.
     *
     * @return Underlying {@link JsonFactory} instance
     */
    @Deprecated
    public JsonFactory getJsonFactory() { return getFactory(); }

    /**
     * Accessor for finding underlying data format factory
     * ({@link JsonFactory}) codec will use for data binding.
     *
     * @return Underlying {@link JsonFactory} instance
     */
    public JsonFactory getFactory() { return getJsonFactory(); }
}
