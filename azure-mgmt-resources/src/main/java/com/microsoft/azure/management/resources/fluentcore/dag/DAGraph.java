/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

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
     * to perform topological sort on the graph.
     */
    private ConcurrentLinkedQueue<String> queue;
    /**
     * indicates this graph has a parent graph.
     */
    private boolean hasParent;
    /**
     * the root node in the graph.
     */
    private NodeT rootNode;

    /**
     * Creates a new DAG.
     *
     * @param rootNode the root node of this DAG
     */
    public DAGraph(NodeT rootNode) {
        this.rootNode = rootNode;
        this.queue = new ConcurrentLinkedQueue<>();
        this.rootNode.setPreparer(true);
        this.addNode(rootNode);
    }

    /**
     * @return <tt>true</tt> if this DAG is merged with another DAG and hence has a parent
     */
    public boolean hasParent() {
        return hasParent;
    }

    /**
     * Checks whether the given node is root node of this DAG.
     *
     * @param node the node {@link DAGNode} to be checked
     * @return <tt>true</tt> if the given node is root node
     */
    public boolean isRootNode(NodeT node) {
        return this.rootNode == node;
    }

    /**
     * @return <tt>true</tt> if this dag is the preparer responsible for
     * preparing the DAG for traversal.
     */
    public boolean isPreparer() {
        return this.rootNode.isPreparer();
    }

    /**
     * Merge this DAG with another DAG.
     * <p>
     * This will mark this DAG as a child DAG, the dependencies of nodes in this DAG will be merged
     * with (copied to) the parent DAG
     *
     * @param parent the parent DAG
     */
    public void merge(DAGraph<DataT, NodeT> parent) {
        this.hasParent = true;
        parent.rootNode.addDependency(this.rootNode.key());
        for (Map.Entry<String, NodeT> entry: graph.entrySet()) {
            String key = entry.getKey();
            if (!parent.graph.containsKey(key)) {
                parent.graph.put(key, entry.getValue());
            }
        }
    }

    /**
     * Merges DAG associated with childNode to DAG associated with a parent node
     * of given key.
     *
     * @param parentKey the key of the parent node
     * @param childNode the child node
     */
    public void mergeChildToParent(String parentKey, NodeT childNode) {
        NodeT parentNode = this.graph.get(parentKey);
        Map<String, NodeT> parentGraph = parentNode.owner().graph;
        Map<String, NodeT> childGraph = childNode.owner().graph;
        for (Map.Entry<String, NodeT> entry : childGraph.entrySet()) {
            String key = entry.getKey();
            if (!parentGraph.containsKey(key)) {
                parentGraph.put(key, entry.getValue());
            }
        }
    }

    /**
     * Prepares this DAG for traversal using getNext method, each call to getNext returns next node
     * in the DAG with no dependencies.
     */
    public void prepare() {
        if (isPreparer()) {
            for (NodeT node : graph.values()) {
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
        return graph.get(nextItemKey);
    }

    /**
     * Gets a node from the graph with the given key.
     * @param key the key of the node
     * @return the node
     */
    public NodeT getNode(String key) {
        return graph.get(key);
    }

    /**
     * Reports that a node is resolved hence other nodes depends on it can consume it.
     *
     * @param completed the node ready to be consumed
     */
    public void reportCompletion(NodeT completed) {
        completed.setPreparer(true);
        String dependency = completed.key();
        for (String dependentKey : graph.get(dependency).dependentKeys()) {
            DAGNode<DataT, NodeT> dependent = graph.get(dependentKey);
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
        for (String dependentKey : graph.get(dependency).dependentKeys()) {
            DAGNode<DataT, NodeT> dependent = graph.get(dependentKey);
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
        visit(new Visitor<NodeT>() {
            @Override
            public void visitNode(NodeT node) {
                if (node.dependencyKeys().isEmpty()) {
                    return;
                }

                String dependentKey = node.key();
                for (String dependencyKey : node.dependencyKeys()) {
                    graph.get(dependencyKey)
                            .addDependent(dependentKey);
                }
            }

            @Override
            public  void visitEdge(String fromKey, String toKey, EdgeType edgeType) {
                if (edgeType == EdgeType.BACK) {
                    throw new IllegalStateException("Detected circular dependency: " + findPath(fromKey, toKey));
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
        for (Map.Entry<String, NodeT> entry: graph.entrySet()) {
            if (!entry.getValue().hasDependencies()) {
                this.queue.add(entry.getKey());
            }
        }
        if (queue.isEmpty()) {
            throw new IllegalStateException("Detected circular dependency");
        }
    }
}
