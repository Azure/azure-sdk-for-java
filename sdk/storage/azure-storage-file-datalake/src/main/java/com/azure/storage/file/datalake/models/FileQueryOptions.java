// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.function.Consumer;

/**
 * Optional parameters for File Query.
 */
@Fluent
public class FileQueryOptions {

    private final String expression;
    private FileQuerySerialization inputSerialization;
    private FileQuerySerialization outputSerialization;
    private DataLakeRequestConditions requestConditions;
    private Consumer<FileQueryError> errorConsumer;
    private Consumer<FileQueryProgress> progressConsumer;

    /**
     * Constructs a {@link FileQueryOptions}.
     */
    public FileQueryOptions(String expression) {
        StorageImplUtils.assertNotNull("expression", expression);
        this.expression = expression;
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
     * Gets the input serialization.
     *
     * @return the input serialization.
     */
    public FileQuerySerialization getInputSerialization() {
        return inputSerialization;
    }

    /**
     * Sets the input serialization.
     *
     * @param inputSerialization The input serialization.
     * @return the updated FileQueryOptions object.
     */
    public FileQueryOptions setInputSerialization(FileQuerySerialization inputSerialization) {
        this.inputSerialization = inputSerialization;
        return this;
    }

    /**
     * Gets the output serialization.
     *
     * @return the output serialization.
     */
    public FileQuerySerialization getOutputSerialization() {
        return outputSerialization;
    }

    /**
     * Sets the output serialization.
     *
     * @param outputSerialization The output serialization.
     * @return the updated FileQueryOptions object.
     */
    public FileQueryOptions setOutputSerialization(FileQuerySerialization outputSerialization) {
        this.outputSerialization = outputSerialization;
        return this;
    }

    /**
     * Gets the request conditions.
     *
     * @return the request conditions.
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the request conditions.
     *
     * @param requestConditions The request conditions.
     * @return the updated FileQueryOptions object.
     */
    public FileQueryOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the error consumer.
     *
     * @return the error consumer.
     */
    public Consumer<FileQueryError> getErrorConsumer() {
        return errorConsumer;
    }

    /**
     * Sets the error consumer.
     *
     * @param errorConsumer The error consumer.
     * @return the updated FileQueryOptions object.
     */
    public FileQueryOptions setErrorConsumer(Consumer<FileQueryError> errorConsumer) {
        this.errorConsumer = errorConsumer;
        return this;
    }

    /**
     * Gets the progress consumer.
     *
     * @return the progress consumer.
     */
    public Consumer<FileQueryProgress> getProgressConsumer() {
        return progressConsumer;
    }

    /**
     * Sets the progress consumer.
     *
     * @param progressConsumer The progress consumer.
     * @return the updated FileQueryOptions object.
     */
    public FileQueryOptions setProgressConsumer(Consumer<FileQueryProgress> progressConsumer) {
        this.progressConsumer = progressConsumer;
        return this;
    }
}
