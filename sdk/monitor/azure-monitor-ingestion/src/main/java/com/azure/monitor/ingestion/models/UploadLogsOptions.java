// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.ObjectSerializer;

/**
 * The options model to configure the request to upload logs to Azure Monitor.
 */
@Fluent
public final class UploadLogsOptions {
    private ObjectSerializer objectSerializer;
    private Integer maxConcurrency;

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
     * @return the update {@link UploadLogsOptions} instance.
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
     * @return the update {@link UploadLogsOptions} instance.
     */
    public UploadLogsOptions setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        return this;
    }
}
