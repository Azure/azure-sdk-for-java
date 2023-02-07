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
public final class UploadLogsOptions {
    private ObjectSerializer objectSerializer;
    private Integer maxConcurrency;
    private Consumer<UploadLogsError> uploadLogsErrorConsumer;

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
     * @return the updated {@link UploadLogsOptions} instance.
     */
    public UploadLogsOptions setObjectSerializer(ObjectSerializer objectSerializer) {
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
     * @return the updated {@link UploadLogsOptions} instance.
     */
    public UploadLogsOptions setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        return this;
    }

    /**
     * Returns the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     * @return the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     */
    public Consumer<UploadLogsError> getUploadLogsErrorConsumer() {
        return uploadLogsErrorConsumer;
    }

    /**
     * Sets  the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     * @param uploadLogsErrorConsumer the error handler that is called when a request to the Azure Monitor service to upload logs fails.
     * @return the updated {@link UploadLogsOptions} instance.
     */
    public UploadLogsOptions setUploadLogsErrorConsumer(Consumer<UploadLogsError> uploadLogsErrorConsumer) {
        this.uploadLogsErrorConsumer = uploadLogsErrorConsumer;
        return this;
    }
}
