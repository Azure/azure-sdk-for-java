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
 */
@Beta(Beta.SinceVersion.V4_7_0)
public class CosmosPatch {

    private final List<PatchOperation> patchOperations;

    CosmosPatch() {
        this.patchOperations = new ArrayList<>();
    }

    /**
     * Initializes a new instance of {@link CosmosPatch} that will contain operations to be performed on a item atomically.
     *
     * @return A new instance of {@link CosmosPatch}.
     */
    public static CosmosPatch createCosmosPatch() {
        return new CosmosPatch();
    }

    /**
     * This performs one of the following functions, depending upon what the target location references:
     *  1. Target location specifies an array index, a new value is inserted into the array at the specified index.
     *  2. Target location specifies an object member that does not already exist, a new member is added to the object.
     *  3. Target location specifies an object member that does exist, that member's value is replaced.
     */
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
     */
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
     */
    public <T> CosmosPatch replace(String path, T value) {
        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.REPLACE,
                path,
                value));

        return this;
    }

    /**
     * This sets the value at the target location with a new value.
     */
    public <T> CosmosPatch set(String path, T value) {
        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.SET,
                path,
                value));

        return this;
    }

    /**
     * This increment the value at the target location.
     */
    public CosmosPatch increment(String path, long value) {
        this.patchOperations.add(
            new PatchOperationCore<>(
                PatchOperationType.INCREMENT,
                path,
                value));

        return this;
    }

    /**
     * This increment the value at the target location.
     */
    public CosmosPatch increment(String path, double value) {
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
