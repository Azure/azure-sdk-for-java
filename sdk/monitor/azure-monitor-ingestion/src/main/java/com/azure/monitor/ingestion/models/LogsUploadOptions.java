// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.ObjectSerializer;

import java.util.function.Consumer;

/**
 * The options model to configure the request to upload logs to Azure Monitor.
 */
@Fluent
public final class LogsUploadOptions {
    private ObjectSerializer objectSerializer;
    private Integer maxConcurrency;
    private Consumer<LogsUploadError> logsUploadErrorConsumer;

    /**
     * Creates an instance of {@link LogsUploadOptions}.
     */
    public LogsUploadOptions() {

    }

    /**
     * Returns the serializer to use to convert the log objects to JSON.
     * @return the serializer to use to convert the log objects to JSON.
     */
    public ObjectSerializer getObjectSerializer() {
        return objectSerializer;
    }

    /**
     * Sets the serializer to use to convert the log objects to JSON.
     * @param objectSerializer the serializer to use to convert the log objects to JSON.
     * @return the updated {@link LogsUploadOptions} instance.
     */
    public LogsUploadOptions setObjectSerializer(ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
        return this;
    }

    /**
     * Returns the max concurrent requests to send to the Azure Monitor service when uploading logs.
     * @return the max concurrent requests to send to the Azure Monitor service when uploading logs.
     */
    public Integer getMaxConcurrency() {
        return maxConcurrency;
    }

    /**
     * Sets the max concurrent requests to send to the Azure Monitor service when uploading logs.
     * @param maxConcurrency the max concurrent requests to send to the Azure Monitor service when uploading logs.
     * @return the updated {@link LogsUploadOptions} instance.
     */
    public LogsUploadOptions setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        return this;
    }

    /**
     * Returns the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     * @return the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     */
    public Consumer<LogsUploadError> getLogsUploadErrorConsumer() {
        return logsUploadErrorConsumer;
    }

    /**
     * Sets  the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     * @param logsUploadErrorConsumer the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     * @return the updated {@link LogsUploadOptions} instance.
     */
    public LogsUploadOptions setLogsUploadErrorConsumer(Consumer<LogsUploadError> logsUploadErrorConsumer) {
        this.logsUploadErrorConsumer = logsUploadErrorConsumer;
        return this;
    }
}
