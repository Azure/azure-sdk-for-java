// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.azure.json.implementation.jackson.core;

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
public interface TreeNode {
    /*
     * /**********************************************************
     * /* Minimal introspection methods
     * /**********************************************************
     */

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
     * /**********************************************************
     * /* Basic traversal through structured entries (Arrays, Objects)
     * /**********************************************************
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
}
