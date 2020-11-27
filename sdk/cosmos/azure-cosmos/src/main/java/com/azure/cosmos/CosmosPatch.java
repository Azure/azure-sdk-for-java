// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.patch.PatchOperation;
import com.azure.cosmos.implementation.patch.PatchOperationCore;
import com.azure.cosmos.implementation.patch.PatchOperationType;
import com.azure.cosmos.util.Beta;

import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Grammar based on RFC: https://tools.ietf.org/html/rfc6902#section-4.1
 *
 * Contains a list of Patch operations to be applied on an item. It is applied in an atomic manner and the operations
 * grammar follows above RFC.
 *
 * This can be executed in 3 ways:
 *  1. Passing this to container in container.patchItem() which requires the id of the item to be patched, partition
 *      key, the CosmosPatch instance, any CosmosItemRequestOptions and the class type for which response will be parsed.
 *  2. Add CosmosPatch instance in TransactionalBatch using batch.patchItemOperation() which requires the id of the item
 *      to be patched, cosmos patch instance and TransactionalBatchItemRequestOptions(if-any) and follow remaining
 *      steps for batch for it's execution.
 *  3. Create a bulk item using BulkOperations.getPatchItemOperation which requires the id of the item to be patched,
 *      cosmos patch instance, partition key and BulkItemRequestOptions(if-any) and follow remaining steps to
 *      execute bulk operations.
 */
@Beta(Beta.SinceVersion.V4_9_0)
public final class CosmosPatch {

    private final List<PatchOperation> patchOperations;

    private CosmosPatch() {
        this.patchOperations = new ArrayList<>();
    }

    /**
     * Initializes a new instance of {@link CosmosPatch} that will contain operations to be performed on a item atomically.
     *
     * @return A new instance of {@link CosmosPatch}.
     */
    @Beta(Beta.SinceVersion.V4_9_0)
    public static CosmosPatch create() {
        return new CosmosPatch();
    }

    /**
     * This performs one of the following functions, depending upon what the target location references:
     *  1. Target location specifies an array index, a new value is inserted into the array at the specified index.
     *  2. Target location specifies an object member that does not already exist, a new member is added to the object.
     *  3. Target location specifies an object member that does exist, that member's value is replaced.
     *
     * @param <T> The type of item to be added.
     *
     * @param path the operation path.
     * @param value the value which will be added.
     *
     * @return same instance of {@link CosmosPatch}
     */
    @Beta(Beta.SinceVersion.V4_9_0)
    public <T> CosmosPatch add(String path, T value) {

        checkNotNull(value, "expected non-null value");
        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);

        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.ADD,
                path,
                value));

        return this;
    }

    /**
     * This removes the value at the target location.
     *
     * @param path the operation path.
     *
     * @return same instance of {@link CosmosPatch}
     */
    @Beta(Beta.SinceVersion.V4_9_0)
    public CosmosPatch remove(String path) {

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);

        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.REMOVE,
                path,
                null));

        return this;
    }

    /**
     * This replaces the value at the target location with a new value.
     *
     * @param <T> The type of item to be replaced.
     *
     * @param path the operation path.
     * @param value the value which will be replaced.
     *
     * @return same instance of {@link CosmosPatch}
     */
    @Beta(Beta.SinceVersion.V4_9_0)
    public <T> CosmosPatch replace(String path, T value) {

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);

        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.REPLACE,
                path,
                value));

        return this;
    }

    /**
     * This sets the value at the target location with a new value.
     *
     * @param <T> The type of item to be set.
     *
     * @param path the operation path.
     * @param value the value which will be set.
     *
     * @return same instance of {@link CosmosPatch}
     */
    @Beta(Beta.SinceVersion.V4_9_0)
    public <T> CosmosPatch set(String path, T value) {

        checkNotNull(value, "expected non-null value");
        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);

        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.SET,
                path,
                value));

        return this;
    }

    /**
     * This increment the value at the target location.
     *
     * @param path the operation path.
     * @param value the value which will be incremented.
     *
     * @return same instance of {@link CosmosPatch}
     */
    @Beta(Beta.SinceVersion.V4_9_0)
    public CosmosPatch increment(String path, long value) {

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);

        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.INCREMENT,
                path,
                value));

        return this;
    }

    /**
     * This increment the value at the target location.
     *
     * @param path the operation path.
     * @param value the value which will be incremented.
     *
     * @return same instance of {@link CosmosPatch}
     */
    @Beta(Beta.SinceVersion.V4_9_0)
    public CosmosPatch increment(String path, double value) {

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);

        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.INCREMENT,
                path,
                value));

        return this;
    }

    List<PatchOperation> getPatchOperations() {
        return patchOperations;
    }
}
