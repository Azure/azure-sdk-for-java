/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Type represents a node in a {@link Graph}.
 *
 * @param <T> the type of the data stored in the node
 * @param <U> the type of the node
 */
public class Node<T, U extends Node<T, U>> {
    /**
     * The graph that owns this node.
     */
    private Graph<T, U> ownerGraph;
    /**
     * A key that uniquely identifies this node in the owner graph {@link this#ownerGraph}.
     */
    private final String key;
    /**
     * reference to the data stored in the node.
     */
    private final T data;
    /**
     * the collection of child node keys.
     */
    private List<String> children;

    /**
     * Creates a graph node.
     *
     * @param key unique id of the node
     * @param data data to be stored in the node
     */
    public Node(final String key, final T data) {
        this.key = key;
        this.data = data;
        this.children = new ArrayList<>();
    }

    /**
     * @return this node's unique id
     */
    public String key() {
        return this.key;
    }

    /**
     * @return data stored in this node
     */
    public T data() {
        return data;
    }

    /**
     * @return <tt>true</tt> if this node has any children
     */
    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    /**
     * @return children (neighbours) of this node
     */
    public List<String> children() {
        return Collections.unmodifiableList(this.children);
    }

    /**
     * @param childKey add a child (neighbour) of this node
     */
    public void addChild(String childKey) {
        this.children.add(childKey);
    }

    /**
     * Sets reference to the graph owning this node.
     *
     * @param ownerGraph the owning graph
     */
    public void setOwner(Graph<T, U> ownerGraph) {
        if (this.ownerGraph != null) {
            throw new RuntimeException("Changing owner graph is not allowed");
        }
        this.ownerGraph = ownerGraph;
    }

    /**
     * @return the owner (container) graph of this node.
     */
    public Graph<T, U> owner() {
        if (this.ownerGraph == null) {
            throw new RuntimeException("Required owner graph is not set");
        }
        return this.ownerGraph;
    }
}
