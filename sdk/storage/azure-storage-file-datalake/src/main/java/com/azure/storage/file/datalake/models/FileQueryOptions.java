// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.common.ErrorReceiver;
import com.azure.storage.common.ProgressReceiver;

/**
 * Optional parameters for File Query.
 */
public class FileQueryOptions {

    private FileQuerySerialization inputSerialization;
    private FileQuerySerialization outputSerialization;
    private DataLakeRequestConditions requestConditions;
    private ErrorReceiver<FileQueryError> errorReceiver;
    private ProgressReceiver progressReceiver;

    /**
     * Constructs a {@link FileQueryOptions}.
     */
    public FileQueryOptions() {
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
     * Gets the error receiver.
     *
     * @return the error receiver.
     */
    public ErrorReceiver<FileQueryError> getErrorReceiver() {
        return errorReceiver;
    }

    /**
     * Sets the error receiver.
     *
     * @param errorReceiver The error receiver.
     * @return the updated FileQueryOptions object.
     */
    public FileQueryOptions setErrorReceiver(ErrorReceiver<FileQueryError> errorReceiver) {
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
     * @return the updated FileQueryOptions object.
     */
    public FileQueryOptions setProgressReceiver(ProgressReceiver progressReceiver) {
        this.progressReceiver = progressReceiver;
        return this;
    }
}
