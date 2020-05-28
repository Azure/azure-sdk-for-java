// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;

import java.util.function.Consumer;

/**
 * Optional parameters for Blob Query.
 */
@Fluent
public class BlobQueryOptions {

    private BlobQuerySerialization inputSerialization;
    private BlobQuerySerialization outputSerialization;
    private BlobRequestConditions requestConditions;
    private Consumer<BlobQueryError> errorConsumer;
    private Consumer<BlobQueryProgress> progressConsumer;

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
     * Gets the error consumer.
     *
     * @return the error consumer.
     */
    public Consumer<BlobQueryError> getErrorConsumer() {
        return errorConsumer;
    }

    /**
     * Sets the error consumer.
     *
     * @param errorConsumer The error consumer.
     * @return the updated BlobQueryOptions object.
     */
    public BlobQueryOptions setErrorConsumer(Consumer<BlobQueryError> errorConsumer) {
        this.errorConsumer = errorConsumer;
        return this;
    }

    /**
     * Gets the progress consumer.
     *
     * @return the progress consumer.
     */
    public Consumer<BlobQueryProgress> getProgressConsumer() {
        return progressConsumer;
    }

    /**
     * Sets the progress consumer.
     *
     * @param progressConsumer The progress consumer.
     * @return the updated BlobQueryOptions object.
     */
    public BlobQueryOptions setProgressConsumer(Consumer<BlobQueryProgress> progressConsumer) {
        this.progressConsumer = progressConsumer;
        return this;
    }
}
