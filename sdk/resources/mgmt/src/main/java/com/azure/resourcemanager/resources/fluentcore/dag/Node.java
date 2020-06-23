// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Type represents a node in a {@link Graph}.
 *
 * @param <DataT> the type of the data stored in the node
 * @param <NodeT> the type of the node
 */
public class Node<DataT, NodeT extends Node<DataT, NodeT>> {
    /**
     * The graph that owns this node.
     */
    private Graph<DataT, NodeT> ownerGraph;
    /**
     * A key that uniquely identifies this node in the owner graph {@link this#ownerGraph}.
     */
    private final String key;
    /**
     * reference to the data stored in the node.
     */
    private final DataT data;
    /**
     * the collection of child node keys.
     */
    private final List<String> children;

    private final ClientLogger logger = new ClientLogger(this.getClass());

    /**
     * Creates a graph node.
     *
     * @param key unique id of the node
     * @param data data to be stored in the node
     */
    public Node(final String key, final DataT data) {
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
    public DataT data() {
        return data;
    }

    /**
     * @return true if this node has any children
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
     * @param childKey remove child (neighbour) of this node
     */
    public void removeChild(String childKey) {
        this.children.remove(childKey);
    }

    /**
     * Sets reference to the graph owning this node.
     *
     * @param ownerGraph the owning graph
     */
    public void setOwner(Graph<DataT, NodeT> ownerGraph) {
        if (this.ownerGraph != null) {
            throw logger.logExceptionAsError(new RuntimeException("Changing owner graph is not allowed"));
        }
        this.ownerGraph = ownerGraph;
    }

    /**
     * @return the owner (container) graph of this node.
     */
    public Graph<DataT, NodeT> owner() {
        if (this.ownerGraph == null) {
            throw logger.logExceptionAsError(new RuntimeException("Required owner graph is not set"));
        }
        return this.ownerGraph;
    }
}
