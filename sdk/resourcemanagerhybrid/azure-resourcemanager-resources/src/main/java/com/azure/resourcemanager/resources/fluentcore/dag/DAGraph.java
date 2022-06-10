// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Type representing a DAG (directed acyclic graph).
 * <p>
 * each node in a DAG is represented by {@link DAGNode}
 *
 * @param <DataT> the type of the data stored in the graph nodes
 * @param <NodeT> the type of the nodes in the graph
 */
public class DAGraph<DataT, NodeT extends DAGNode<DataT, NodeT>> extends Graph<DataT, NodeT> {
    /**
     * the root node in the graph.
     * {@link this#nodeTable} contains all the nodes in this graph with this as the root.
     */
    private final NodeT rootNode;
    /**
     * the immediate parent graphs of this graph. A parent graph is the one with it's root
     * depends on this graph's root.
     */
    protected List<DAGraph<DataT, NodeT>> parentDAGs;
    /**
     * to perform topological sort on the graph. During sorting queue contains the nodes which
     * are ready to invoke.
     */
    protected ConcurrentLinkedQueue<String> queue;

    private final ClientLogger logger = new ClientLogger(this.getClass());

    /**
     * Creates a new DAG.
     *
     * @param rootNode the root node of this DAG
     */
    public DAGraph(NodeT rootNode) {
        this.parentDAGs = new ArrayList<>();
        this.rootNode = rootNode;
        this.queue = new ConcurrentLinkedQueue<>();
        this.rootNode.setPreparer(true);
        this.addNode(rootNode);
    }

    /**
     * @return true if this DAG is merged with one or more DAG and hence has parents
     */
    public boolean hasParents() {
        return this.parentDAGs.size() > 0;
    }

    /**
     * @return the root node of the DAG.
     */
    protected NodeT root() {
        return this.rootNode;
    }

    /**
     * Checks whether the given node is root node of this DAG.
     *
     * @param node the node {@link DAGNode} to be checked
     * @return true if the given node is root node
     */
    public boolean isRootNode(NodeT node) {
        return this.rootNode == node;
    }

    /**
     * @return true if this dag is the preparer responsible for
     * preparing the DAG for traversal.
     */
    public boolean isPreparer() {
        return this.rootNode.isPreparer();
    }

    /**
     * Gets a node from the graph with the given key.
     *
     * @param key the key of the node
     * @return the node
     */
    public NodeT getNode(String key) {
        return nodeTable.get(key);
    }

    /**
     * Mark root of this DAG depends on given DAG's root.
     *
     * @param dependencyGraph the dependency DAG
     */
    public void addDependencyGraph(DAGraph<DataT, NodeT> dependencyGraph) {
        this.rootNode.addDependency(dependencyGraph.rootNode.key());
        Map<String, NodeT> sourceNodeTable = dependencyGraph.nodeTable;
        Map<String, NodeT> targetNodeTable = this.nodeTable;
        this.merge(sourceNodeTable, targetNodeTable);
        dependencyGraph.parentDAGs.add(this);
        if (this.hasParents()) {
            this.bubbleUpNodeTable(this, new LinkedList<String>());
        }
    }

    /**
     * Mark root of the given DAG depends on this DAG's root.
     *
     * @param dependentGraph the dependent DAG
     */
    public void addDependentGraph(DAGraph<DataT, NodeT> dependentGraph) {
        dependentGraph.addDependencyGraph(this);
    }

    /**
     * Prepares this DAG for node enumeration using getNext method, each call to getNext returns next node
     * in the DAG with no dependencies.
     */
    public void prepareForEnumeration() {
        if (isPreparer()) {
            for (NodeT node : nodeTable.values()) {
                // Prepare each node for traversal
                node.initialize();
                if (!this.isRootNode(node)) {
                    // Mark other sub-DAGs as non-preparer
                    node.setPreparer(false);
                }
            }
            initializeDependentKeys();
            initializeQueue();
        }
    }

    /**
     * Gets next node in the DAG which has no dependency or all of it's dependencies are resolved and
     * ready to be consumed.
     *
     * @return next node or null if all the nodes have been explored or no node is available at this moment.
     */
    public NodeT getNext() {
        String nextItemKey = queue.poll();
        if (nextItemKey == null) {
            return null;
        }
        return nodeTable.get(nextItemKey);
    }

    /**
     * Reports that a node is resolved hence other nodes depends on it can consume it.
     *
     * @param completed the node ready to be consumed
     */
    public void reportCompletion(NodeT completed) {
        completed.setPreparer(true);
        String dependency = completed.key();
        for (String dependentKey : nodeTable.get(dependency).dependentKeys()) {
            DAGNode<DataT, NodeT> dependent = nodeTable.get(dependentKey);
            dependent.lock().lock();
            try {
                dependent.onSuccessfulResolution(dependency);
                if (dependent.hasAllResolved()) {
                    queue.add(dependent.key());
                }
            } finally {
                dependent.lock().unlock();
            }
        }
    }

    /**
     * Reports that a node is faulted.
     *
     * @param faulted the node faulted
     * @param throwable the reason for fault
     */
    public void reportError(NodeT faulted, Throwable throwable) {
        faulted.setPreparer(true);
        String dependency = faulted.key();
        for (String dependentKey : nodeTable.get(dependency).dependentKeys()) {
            DAGNode<DataT, NodeT> dependent = nodeTable.get(dependentKey);
            dependent.lock().lock();
            try {
                dependent.onFaultedResolution(dependency, throwable);
                if (dependent.hasAllResolved()) {
                    queue.add(dependent.key());
                }
            } finally {
                dependent.lock().unlock();
            }
        }
    }

    /**
     * Initializes dependents of all nodes.
     * <p>
     * The DAG will be explored in DFS order and all node's dependents will be identified,
     * this prepares the DAG for traversal using getNext method, each call to getNext returns next node
     * in the DAG with no dependencies.
     */
    private void initializeDependentKeys() {
        visit(new Visitor<Node<DataT, NodeT>>() {
            @Override
            public void visitNode(Node<DataT, NodeT> node) {
                if (!(node instanceof DAGNode)) {
                    throw logger.logExceptionAsError(new IllegalStateException("Unexpected node type"));
                }
                DAGNode<DataT, NodeT> dagNode = (DAGNode<DataT, NodeT>) node;
                if (dagNode.dependencyKeys().isEmpty()) {
                    return;
                }

                String dependentKey = node.key();
                for (String dependencyKey : dagNode.dependencyKeys()) {
                    nodeTable.get(dependencyKey)
                            .addDependent(dependentKey);
                }
            }

            @Override
            public void visitEdge(String fromKey, String toKey, EdgeType edgeType) {
                if (edgeType == EdgeType.BACK) {
                    throw logger.logExceptionAsError(
                        new IllegalStateException("Detected circular dependency: " + findPath(fromKey, toKey)));
                }
            }
        });
    }

    /**
     * Initializes the queue that tracks the next set of nodes with no dependencies or
     * whose dependencies are resolved.
     */
    private void initializeQueue() {
        this.queue.clear();
        for (Map.Entry<String, NodeT> entry : nodeTable.entrySet()) {
            if (!entry.getValue().hasDependencies()) {
                this.queue.add(entry.getKey());
            }
        }
        if (queue.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalStateException("Detected circular dependency"));
        }
    }

    /**
     * Copies entries in the source map to target map.
     *
     * @param source source map
     * @param target target map
     */
    private void merge(Map<String, NodeT> source, Map<String, NodeT> target) {
        for (Map.Entry<String, NodeT> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!target.containsKey(key)) {
                target.put(key, entry.getValue());
            }
        }
    }

    /**
     * Propagates node table of given DAG to all of it ancestors.
     */
    private void bubbleUpNodeTable(DAGraph<DataT, NodeT> from, LinkedList<String> path) {
        if (path.contains(from.rootNode.key())) {
            path.push(from.rootNode.key()); // For better error message
            throw logger.logExceptionAsError(
                new IllegalStateException("Detected circular dependency: " + String.join(" -> ", path)));
        }
        path.push(from.rootNode.key());
        for (DAGraph<DataT, NodeT> to : from.parentDAGs) {
            this.merge(from.nodeTable, to.nodeTable);
            this.bubbleUpNodeTable(to, path);
        }
        path.pop();
    }
}
