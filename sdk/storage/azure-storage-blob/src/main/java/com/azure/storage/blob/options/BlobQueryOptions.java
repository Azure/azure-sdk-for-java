// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobQueryError;
import com.azure.storage.blob.models.BlobQueryProgress;
import com.azure.storage.blob.models.BlobQuerySerialization;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Optional parameters for Blob Query.
 */
@Fluent
public class BlobQueryOptions {

    private final String expression;
    private final OutputStream outputStream;
    private BlobQuerySerialization inputSerialization;
    private BlobQuerySerialization outputSerialization;
    private BlobRequestConditions requestConditions;
    private Consumer<BlobQueryError> errorConsumer;
    private Consumer<BlobQueryProgress> progressConsumer;

    /**
     * Constructs a {@link BlobQueryOptions}.
     * @param expression The query expression.
     */
    public BlobQueryOptions(String expression) {
        StorageImplUtils.assertNotNull("expression", expression);
        this.expression = expression;
        this.outputStream = null;
    }

    /**
     * Constructs a {@link BlobQueryOptions}.
     * @param expression The query expression.
     * @param outputStream The OutputStream where the downloaded data will be written.
     */
    public BlobQueryOptions(String expression, OutputStream outputStream) {
        StorageImplUtils.assertNotNull("expression", expression);
        StorageImplUtils.assertNotNull("outputStream", outputStream);
        this.expression = expression;
        this.outputStream = outputStream;
    }

    /**
     * Gets the query expression.
     *
     * @return the query expression.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Gets the outputStream where the downloaded data will be written.
     *
     * @return the outputStream.
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
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
