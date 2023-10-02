// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

import java.util.Iterator;

/**
 * Marker interface used to denote JSON Tree nodes, as far as
 * the core package knows them (which is very little): mostly
 * needed to allow {@link ObjectCodec} to have some level
 * of interoperability.
 * Most functionality is within <code>JsonNode</code>
 * base class in <code>mapper</code> package.
 *<p>
 * Note that in Jackson 1.x <code>JsonNode</code> itself
 * was part of core package: Jackson 2.x refactored this
 * since conceptually Tree Model is part of mapper package,
 * and so part visible to <code>core</code> package should
 * be minimized.
 *<p>
 * NOTE: starting with Jackson 2.2, there is more functionality
 * available via this class, and the intent was that this should
 * form actual base for multiple alternative tree representations;
 * for example, immutable trees could use different implementation
 * than mutable trees.
 * 
 * @since 2.2
 */
public interface TreeNode
{
    /*
    /**********************************************************
    /* Minimal introspection methods
    /**********************************************************
     */
    
    /**
     * Method that can be used for efficient type detection
     * when using stream abstraction for traversing nodes.
     * Will return the first {@link JsonToken} that equivalent
     * stream event would produce (for most nodes there is just
     * one token but for structured/container types multiple)
     *
     * @return {@link JsonToken} that is most closely associated with the node type
     */
    JsonToken asToken();

    /**
     * If this node is a numeric type (as per {@link JsonToken#isNumeric}),
     * returns native type that node uses to store the numeric value;
     * otherwise returns null.
     * 
     * @return Type of number contained, if any; or null if node does not
     *  contain numeric value.
     */
    JsonParser.NumberType numberType();

    /**
     * Method that returns number of child nodes this node contains:
     * for Array nodes, number of child elements, for Object nodes,
     * number of fields, and for all other nodes 0.
     *
     * @return For non-container nodes returns 0; for arrays number of
     *   contained elements, and for objects number of fields.
     * 
     * @since 2.2
     */
    int size();

    /**
     * Method that returns true for all value nodes: ones that 
     * are not containers, and that do not represent "missing" nodes
     * in the path. Such value nodes represent String, Number, Boolean
     * and null values from JSON.
     *<p>
     * Note: one and only one of methods {@link #isValueNode},
     * {@link #isContainerNode} and {@link #isMissingNode} ever
     * returns true for any given node.
     *
     * @return True if this node is considered a value node; something that
     *    represents either a scalar value or explicit {@code null}
     * 
     * @since 2.2
     */
    boolean isValueNode();

    /**
     * Method that returns true for container nodes: Arrays and Objects.
     *<p>
     * Note: one and only one of methods {@link #isValueNode},
     * {@link #isContainerNode} and {@link #isMissingNode} ever
     * returns true for any given node.
     *
     * @return {@code True} for Array and Object nodes, {@code false} otherwise
     * 
     * @since 2.2
     */
    boolean isContainerNode();
    
    /**
     * Method that returns true for "virtual" nodes which represent
     * missing entries constructed by path accessor methods when
     * there is no actual node matching given criteria.
     *<p>
     * Note: one and only one of methods {@link #isValueNode},
     * {@link #isContainerNode} and {@link #isMissingNode} ever
     * returns true for any given node.
     *
     * @return {@code True} if this node represents a "missing" node
     * 
     * @since 2.2
     */
    boolean isMissingNode();
    
    /**
     * Method that returns true if this node is an Array node, false
     * otherwise.
     * Note that if true is returned, {@link #isContainerNode}
     * must also return true.
     *
     * @return {@code True} for Array nodes, {@code false} for everything else
     *
     * @since 2.2
     */
    boolean isArray();

    /**
     * Method that returns true if this node is an Object node, false
     * otherwise.
     * Note that if true is returned, {@link #isContainerNode}
     * must also return true.
     *
     * @return {@code True} for Object nodes, {@code false} for everything else
     * 
     * @since 2.2
     */
    boolean isObject();

    /*
    /**********************************************************
    /* Basic traversal through structured entries (Arrays, Objects)
    /**********************************************************
     */

    /**
     * Method for accessing value of the specified field of
     * an object node. If this node is not an object (or it
     * does not have a value for specified field name), or
     * if there is no field with such name, null is returned.
     *<p>
     * NOTE: handling of explicit null values may vary between
     * implementations; some trees may retain explicit nulls, others
     * not.
     *
     * @param fieldName Name of the field (of Object node) to access
     *
     * @return Node that represent value of the specified field,
     *   if this node is an Object and has value for the specified
     *   field; {@code null} otherwise.
     * 
     * @since 2.2
     */
    TreeNode get(String fieldName);

    /**
     * Method for accessing value of the specified element of
     * an array node. For other nodes, null is returned.
     *<p>
     * For array nodes, index specifies
     * exact location within array and allows for efficient iteration
     * over child elements (underlying storage is guaranteed to
     * be efficiently indexable, i.e. has random-access to elements).
     * If index is less than 0, or equal-or-greater than
     * <code>node.size()</code>, null is returned; no exception is
     * thrown for any index.
     *
     * @param index Index of the Array node element to access
     *
     * @return Node that represent value of the specified element,
     *   if this node is an array and has specified element;
     *   {@code null} otherwise.
     * 
     * @since 2.2
     */
    TreeNode get(int index);

    /**
     * Method for accessing value of the specified field of
     * an object node.
     * For other nodes, a "missing node" (virtual node
     * for which {@link #isMissingNode} returns true) is returned.
     *
     * @param fieldName Name of the field (of Object node) to access
     *
     * @return Node that represent value of the specified field,
     *   if this node is an object and has value for the specified field;
     *   otherwise "missing node" is returned.
     * 
     * @since 2.2
     */
    TreeNode path(String fieldName);

    /**
     * Method for accessing value of the specified element of
     * an array node.
     * For other nodes, a "missing node" (virtual node
     * for which {@link #isMissingNode} returns true) is returned.
     *<p>
     * For array nodes, index specifies
     * exact location within array and allows for efficient iteration
     * over child elements (underlying storage is guaranteed to
     * be efficiently indexable, i.e. has random-access to elements).
     * If index is less than 0, or equal-or-greater than
     * <code>node.size()</code>, "missing node" is returned; no exception is
     * thrown for any index.
     *
     * @param index Index of the Array node element to access
     *
     * @return Node that represent value of the specified element,
     *   if this node is an array and has specified element;
     *   otherwise "missing node" is returned.
     * 
     * @since 2.2
     */
    TreeNode path(int index);
    
    /**
     * Method for accessing names of all fields for this node, iff
     * this node is an Object node. Number of field names accessible
     * will be {@link #size}.
     *
     * @return An iterator for traversing names of all fields this Object node
     *   has (if Object node); empty {@link Iterator} otherwise (never {@code null}).
     *
     * @since 2.2
     */
    Iterator<String> fieldNames();

    /**
     * Method for locating node specified by given JSON pointer instances.
     * Method will never return null; if no matching node exists, 
     * will return a node for which {@link TreeNode#isMissingNode()} returns true.
     *
     * @param ptr {@link JsonPointer} expression for descendant node to return
     *
     * @return Node that matches given JSON Pointer, if any: if no match exists,
     *   will return a "missing" node (for which {@link TreeNode#isMissingNode()}
     *   returns {@code true}).
     * 
     * @since 2.3
     */
    TreeNode at(JsonPointer ptr);

    /**
     * Convenience method that is functionally equivalent to:
     *<pre>
     *   return at(JsonPointer.valueOf(jsonPointerExpression));
     *</pre>
     *<p>
     * Note that if the same expression is used often, it is preferable to construct
     * {@link JsonPointer} instance once and reuse it: this method will not perform
     * any caching of compiled expressions.
     * 
     * @param jsonPointerExpression Expression to compile as a {@link JsonPointer}
     *   instance
     *
     * @return Node that matches given JSON Pointer, if any: if no match exists,
     *   will return a "missing" node (for which {@link TreeNode#isMissingNode()}
     *   returns {@code true}).
     * 
     * @since 2.3
     */
    TreeNode at(String jsonPointerExpression) throws IllegalArgumentException;
    
    /*
    /**********************************************************
    /* Converting to/from Streaming API
    /**********************************************************
     */

    /**
     * Method for constructing a {@link JsonParser} instance for
     * iterating over contents of the tree that this node is root of.
     * Functionally equivalent to first serializing tree using
     * {@link ObjectCodec} and then re-parsing but
     * more efficient.
     *<p>
     * NOTE: constructed parser instance will NOT initially point to a token,
     * so before passing it to deserializers, it is typically necessary to
     * advance it to the first available token by calling {@link JsonParser#nextToken()}.
     *<p>
     * Also note that calling this method will <b>NOT</b> pass {@link ObjectCodec}
     * reference, so data-binding callback methods like {@link JsonParser#readValueAs(Class)}
     * will not work with calling {@link JsonParser#setCodec}).
     * It is often better to call {@link #traverse(ObjectCodec)} to pass the codec explicitly.
     *
     * @return {@link JsonParser} that will stream over contents of this node
     */
    JsonParser traverse();

    /**
     * Same as {@link #traverse()}, but additionally passes {@link com.typespec.json.implementation.jackson.core.ObjectCodec}
     * to use if {@link JsonParser#readValueAs(Class)} is used (otherwise caller must call
     * {@link JsonParser#setCodec} on response explicitly).
     *<p>
     * NOTE: constructed parser instance will NOT initially point to a token,
     * so before passing it to deserializers, it is typically necessary to
     * advance it to the first available token by calling {@link JsonParser#nextToken()}.
     *
     * @param codec {@link ObjectCodec} to associate with parser constructed
     *
     * @return {@link JsonParser} that will stream over contents of this node
     * 
     * @since 2.1
     */
    JsonParser traverse(ObjectCodec codec);
}
