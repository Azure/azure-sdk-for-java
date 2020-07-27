// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.core.util.logging.ClientLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Type representing a directed graph data structure.
 * <p>
 * Each node in a graph is represented by {@link Node}
 *
 * @param <DataT> the type of the data stored in the graph's nodes
 * @param <NodeT> the type of the nodes in the graph
 */
public class Graph<DataT, NodeT extends Node<DataT, NodeT>> {
    /**
     * the nodes in the graph.
     */
    protected Map<String, NodeT> nodeTable;
    /**
     * to track the already visited node while performing DFS.
     */
    private final Set<String> visited;
    /**
     * to generate node entry and exit time while performing DFS.
     */
    private Integer time;
    /**
     * to track the entry time to each node while performing DFS.
     */
    private final Map<String, Integer> entryTime;
    /**
     * to track the exit time from each node while performing DFS.
     */
    private final Map<String, Integer> exitTime;
    /**
     * to track the immediate parent node of each node while performing DFS.
     */
    private final Map<String, String> parent;
    /**
     * to track already processed node while performing DFS.
     */
    private final Set<String> processed;

    private final ClientLogger logger = new ClientLogger(this.getClass());

    /**
     * Creates a directed graph.
     */
    public Graph() {
        this.nodeTable = new TreeMap<>();
        this.visited = new HashSet<>();
        this.time = 0;
        this.entryTime = new HashMap<>();
        this.exitTime = new HashMap<>();
        this.parent = new HashMap<>();
        this.processed = new HashSet<>();
    }

    /**
     * @return all nodes in the graph.
     */
    public Collection<NodeT> getNodes() {
        return nodeTable.values();
    }

    /**
     * Adds a node to this graph.
     *
     * @param node the node
     */
    public void addNode(NodeT node) {
        node.setOwner(this);
        nodeTable.put(node.key(), node);
    }

    /**
     * Perform DFS visit in this graph.
     * <p>
     * The directed graph will be traversed in DFS order and the visitor will be notified as
     * search explores each node and edge.
     *
     * @param visitor the graph visitor
     */
    public void visit(Visitor<Node<DataT, NodeT>> visitor) {
        for (Map.Entry<String, NodeT> item : nodeTable.entrySet()) {
            if (!visited.contains(item.getKey())) {
                this.dfs(visitor, item.getValue());
            }
        }
        visited.clear();
        time = 0;
        entryTime.clear();
        exitTime.clear();
        parent.clear();
        processed.clear();
    }

    private void dfs(Visitor<Node<DataT, NodeT>> visitor, Node<DataT, NodeT> node) {
        visitor.visitNode(node);

        String fromKey = node.key();
        visited.add(fromKey);
        entryTime.put(fromKey, ++time);
        for (String toKey : node.children()) {
            if (!visited.contains(toKey)) {
                parent.put(toKey, fromKey);
                visitor.visitEdge(fromKey, toKey, edgeType(fromKey, toKey));
                this.dfs(visitor, this.nodeTable.get(toKey));
            } else {
                visitor.visitEdge(fromKey, toKey, edgeType(fromKey, toKey));
            }
        }
        exitTime.put(fromKey, ++time);
        processed.add(fromKey);
    }

    private EdgeType edgeType(String fromKey, String toKey) {
        if (parent.containsKey(toKey) && parent.get(toKey).equals(fromKey)) {
            return EdgeType.TREE;
        }

        if (visited.contains(toKey) && !processed.contains(toKey)) {
            return EdgeType.BACK;
        }

        if (processed.contains(toKey) && entryTime.containsKey(toKey) && entryTime.containsKey(fromKey)) {
            if (entryTime.get(toKey) > entryTime.get(fromKey)) {
                return EdgeType.FORWARD;
            }

            if (entryTime.get(toKey) < entryTime.get(fromKey)) {
                return EdgeType.CROSS;
            }
        }

        throw logger.logExceptionAsError(
            new IllegalStateException("Internal Error: Unable to locate the edge type {" + fromKey + ", " + toKey + "}")
        );
    }

    /**
     * Find the path.
     *
     * @param start key of first node in the path
     * @param end key of last node in the path
     * @return string containing the nodes keys in the path separated by arrow symbol
     */
    protected String findPath(String start, String end) {
        if (start.equals(end)) {
            return start;
        } else {
            return findPath(start, parent.get(end)) + " -> " + end;
        }
    }

    /**
     * The edge types in a graph.
     */
    protected enum EdgeType {
        /**
         * An edge (u, v) is a tree edge if v is visited the first time.
         */
        TREE,
        /**
         * An edge (u, v) is a forward edge if v is descendant of u.
         */
        FORWARD,
        /**
         * An edge (u, v) is a back edge if v is ancestor of u.
         */
        BACK,
        /**
         * An edge (u, v) is a cross edge if v is neither ancestor or descendant of u.
         */
        CROSS
    }

    /**
     * Represents a visitor to be implemented by the consumer who want to visit the
     * graph's nodes in DFS order by calling visit method.
     *
     * @param <U> the type of the node
     */
    protected interface Visitor<U> {
        /**
         * visit a node.
         *
         * @param node the node to visited
         */
        void visitNode(U node);

        /**
         * visit an edge.
         *
         * @param fromKey key of the from node
         * @param toKey key of the to node
         * @param edgeType the edge type
         */
        void visitEdge(String fromKey, String toKey, EdgeType edgeType);
    }
}
