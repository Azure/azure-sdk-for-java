// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.common.ErrorReceiver;
import com.azure.storage.common.ProgressReceiver;

/**
 * Optional parameters for Blob Query.
 */
public class BlobQueryOptions {

    private BlobQuerySerialization inputSerialization;
    private BlobQuerySerialization outputSerialization;
    private BlobRequestConditions requestConditions;
    private ErrorReceiver<BlobQueryError> errorReceiver;
    private ProgressReceiver progressReceiver;

    /**
     * Constructs a {@link BlobQueryOptions}.
     */
    public BlobQueryOptions() {
    }

    /**
     * Gets the input serialization.
     *
     * @return the input serialization.
     */
    public BlobQuerySerialization getInputSerialization() {
        return inputSerialization;
    }

    /**
     * Sets the input serialization.
     *
     * @param inputSerialization The input serialization.
     * @return the updated BlobQueryOptions object.
     */
    public BlobQueryOptions setInputSerialization(BlobQuerySerialization inputSerialization) {
        this.inputSerialization = inputSerialization;
        return this;
    }

    /**
     * Gets the output serialization.
     *
     * @return the output serialization.
     */
    public BlobQuerySerialization getOutputSerialization() {
        return outputSerialization;
    }

    /**
     * Sets the output serialization.
     *
     * @param outputSerialization The output serialization.
     * @return the updated BlobQueryOptions object.
     */
    public BlobQueryOptions setOutputSerialization(BlobQuerySerialization outputSerialization) {
        this.outputSerialization = outputSerialization;
        return this;
    }

    /**
     * Gets the request conditions.
     *
     * @return the request conditions.
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the request conditions.
     *
     * @param requestConditions The request conditions.
     * @return the updated BlobQueryOptions object.
     */
    public BlobQueryOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the error receiver.
     *
     * @return the error receiver.
     */
    public ErrorReceiver<BlobQueryError> getErrorReceiver() {
        return errorReceiver;
    }

    /**
     * Sets the error receiver.
     *
     * @param errorReceiver The error receiver.
     * @return the updated BlobQueryOptions object.
     */
    public BlobQueryOptions setErrorReceiver(ErrorReceiver<BlobQueryError> errorReceiver) {
        this.errorReceiver = errorReceiver;
        return this;
    }

    /**
     * Gets the progress receiver.
     *
     * @return the progress receiver.
     */
    public ProgressReceiver getProgressReceiver() {
        return progressReceiver;
    }

    /**
     * Sets the progress receiver.
     *
     * @param progressReceiver The progress receiver.
     * @return the updated BlobQueryOptions object.
     */
    public BlobQueryOptions setProgressReceiver(ProgressReceiver progressReceiver) {
        this.progressReceiver = progressReceiver;
        return this;
    }
}
