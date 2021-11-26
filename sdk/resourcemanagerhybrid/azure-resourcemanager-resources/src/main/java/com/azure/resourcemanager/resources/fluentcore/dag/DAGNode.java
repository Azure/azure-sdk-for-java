// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The type representing node in a {@link DAGraph}.
 *
 * @param <DataT> the type of the data stored in the node
 * @param <NodeT> the type of the node
 */
public class DAGNode<DataT, NodeT extends DAGNode<DataT, NodeT>> extends Node<DataT, NodeT> {
    /**
     * keys of other nodes those dependents on this node.
     */
    private final List<String> dependentKeys;
    /**
     * to track the dependency resolution count.
     */
    private int toBeResolved;
    /**
     * indicates this node is the preparer or not.
     */
    private boolean isPreparer;
    /**
     * lock used while performing concurrent safe operation on the node.
     */
    private final ReentrantLock lock;

    private final ClientLogger logger = new ClientLogger(this.getClass());
    private static final String ERROR_MESSAGE_FORMAT =
        "invalid state - %s: The dependency '%s' is already reported or there is no such dependencyKey";

    /**
     * Creates a DAG node.
     *
     * @param key unique id of the node
     * @param data data to be stored in the node
     */
    public DAGNode(final String key, final DataT data) {
        super(key, data);
        dependentKeys = new ArrayList<>();
        lock = new ReentrantLock();
    }

    /**
     * @return the lock to be used while performing thread safe operation on this node.
     */
    public ReentrantLock lock() {
        return this.lock;
    }

    /**
     * @return a list of keys of nodes in {@link DAGraph} those are dependents on this node
     */
    List<String> dependentKeys() {
        return Collections.unmodifiableList(this.dependentKeys);
    }

    /**
     * Mark the node identified by the given key as dependent of this node.
     *
     * @param key the id of the dependent node
     */
    public void addDependent(String key) {
        this.dependentKeys.add(key);
    }

    /**
     * @return a list of keys of nodes in {@link DAGraph} that this node depends on
     */
    public List<String> dependencyKeys() {
        return this.children();
    }

    /**
     * Mark the node identified by the given key as this node's dependency.
     *
     * @param dependencyKey the id of the dependency node
     */
    public void addDependency(String dependencyKey) {
        super.addChild(dependencyKey);
    }

    /**
     * Remove the dependency node identified by the given key from the dependencies.
     *
     * @param dependencyKey the id of the dependency node
     */
    public void removeDependency(String dependencyKey) {
        super.removeChild(dependencyKey);
    }

    /**
     * @return true if this node has any dependency
     */
    public boolean hasDependencies() {
        return this.hasChildren();
    }

    /**
     * Mark or un-mark this node as preparer.
     *
     * @param isPreparer true if this node needs to be marked as preparer, false otherwise.
     */
    public void setPreparer(boolean isPreparer) {
        this.isPreparer = isPreparer;
    }

    /**
     * @return true if this node is marked as preparer
     */
    public boolean isPreparer() {
        return isPreparer;
    }

    /**
     * Initialize the node so that traversal can be performed on the parent DAG.
     */
    public void initialize() {
        this.toBeResolved = this.dependencyKeys().size();
        this.dependentKeys.clear();
    }

    /**
     * @return true if all dependencies of this node are resolved
     */
    boolean hasAllResolved() {
        return toBeResolved == 0;
    }

    /**
     * Reports a dependency of this node has been successfully resolved.
     *
     * @param dependencyKey the id of the dependency node
     */
    protected void onSuccessfulResolution(String dependencyKey) {
        if (toBeResolved == 0) {
            throw logger.logExceptionAsError(new RuntimeException(
                String.format(ERROR_MESSAGE_FORMAT, key(), dependencyKey)));
        }
        toBeResolved--;
    }

    /**
     * Reports a dependency of this node has been faulted.
     *
     * @param dependencyKey the id of the dependency node
     * @param throwable the reason for unsuccessful resolution
     */
    protected void onFaultedResolution(String dependencyKey, Throwable throwable) {
        if (toBeResolved == 0) {
            throw logger.logExceptionAsError(new RuntimeException(
                String.format(ERROR_MESSAGE_FORMAT, key(), dependencyKey)));
        }
        toBeResolved--;
    }
}
