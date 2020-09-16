// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.models.AccessControlChanges;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Optional parameters for Update Access Control Recursive.
 */
public class PathUpdateAccessControlRecursiveOptions {
    private final List<PathAccessControlEntry> accessControlList;
    private Integer batchSize;
    private Integer maxBatches;
    private Consumer<Response<AccessControlChanges>> progressHandler;
    private String continuationToken;
    private boolean continueOnFailure;

    /**
     * Constructs a new options object.
     * @param accessControlList The POSIX access control list for the file or directory.
     */
    public PathUpdateAccessControlRecursiveOptions(List<PathAccessControlEntry> accessControlList) {
        StorageImplUtils.assertNotNull("accessControllList", accessControlList);
        this.accessControlList = accessControlList;
    }

    /**
     * Returns the POSIX access control list for the file or directory.
     *
     * @return The POSIX access control list for the file or directory.
     */
    public List<PathAccessControlEntry> getAccessControlList() {
        return this.accessControlList;
    }

    /**
     * Gets the batch size.
     * <p>
     * If data set size exceeds batch size then operation will be split into multiple requests so that progress can be
     * tracked. Batch size should be between 1 and 2000. The default when unspecified is 2000.
     *
     * @return The size of the batch.
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Sets the batch size.
     * <p>
     * If data set size exceeds batch size then operation will be split into multiple requests so that progress can be
     * tracked. Batch size should be between 1 and 2000. The default when unspecified is 2000.
     *
     * @param batchSize The size of the batch.
     * @return The updated object.
     */
    public PathUpdateAccessControlRecursiveOptions setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Gets the maximum number of batches that single change Access Control operation can execute.
     * <p>
     * If maximum is reached before all subpaths are processed then continuation token can be used to resume operation.
     * Empty value indicates that maximum number of batches in unbound and operation continues till end. Operation may
     * also halt if an error is hit and {@code continueOnFailure} is false.
     *
     * @return The maximum number of batches.
     */
    public Integer getMaxBatches() {
        return maxBatches;
    }

    /**
     * Sets the maximum number of batches that single change Access Control operation can execute.
     * <p>
     * If maximum is reached before all subpaths are processed then continuation token can be used to resume operation.
     * Empty value indicates that maximum number of batches in unbound and operation continues till end. Operation may
     *      * also halt if an error is hit and {@code continueOnFailure} is false.
     *
     * @param maxBatches The maximum number of batches.
     * @return The updated object.
     */
    public PathUpdateAccessControlRecursiveOptions setMaxBatches(Integer maxBatches) {
        this.maxBatches = maxBatches;
        return this;
    }

    /**
     * Gets a callback where caller can track progress of the operation as well as collect paths that failed to change
     * Access Control.
     *
     * @return The progress handler.
     */
    public Consumer<Response<AccessControlChanges>> getProgressHandler() {
        return progressHandler;
    }

    /**
     * Sets a callback where caller can track progress of the operation as well as collect paths that failed to change
     * Access Control.
     *
     * @param progressHandler The progress handler.
     * @return The updated object.
     */
    public PathUpdateAccessControlRecursiveOptions setProgressHandler(
        Consumer<Response<AccessControlChanges>> progressHandler) {
        this.progressHandler = progressHandler;
        return this;
    }

    /**
     * Returns a token that can be used to resume previously stopped operation.
     *
     * @return A token that can be used to resume previously stopped operation.
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Sets a token that can be used to resume previously stopped operation.
     *
     * @param continuationToken A token that can be used to resume previously stopped operation.
     * @return The updated object.
     */
    public PathUpdateAccessControlRecursiveOptions setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    /**
     * Returns if the operation should continue on user failure.
     * <p>
     * If set to false, the operation will terminate quickly on encountering user failures. If true, the operation will
     * ignore user failures and proceed with the operation on other sub-entities of the directory.
     *
     * @return If the operation should continue on user failure.
     */
    public boolean isContinueOnFailure() {
        return continueOnFailure;
    }

    /**
     * Sets if the operation should continue on user failure.
     * <p>
     * If set to false, the operation will terminate quickly on encountering user failures. If true, the operation will
     * ignore user failures and proceed with the operation on other sub-entities of the directory.
     *
     * @param continueOnFailure Whether the operation should continue on user failure.
     * @return The updated object.
     */
    public PathUpdateAccessControlRecursiveOptions setContinueOnFailure(boolean continueOnFailure) {
        this.continueOnFailure = continueOnFailure;
        return this;
    }
}
