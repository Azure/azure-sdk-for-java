package com.azure.monitor.ingestion.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.ObjectSerializer;

/**
 *
 */
@Fluent
public final class UploadLogsOptions {
    private ObjectSerializer objectSerializer;
    private Integer maxConcurrency;

    /**
     * @return
     */
    public ObjectSerializer getObjectSerializer() {
        return objectSerializer;
    }

    /**
     * @param objectSerializer
     */
    public UploadLogsOptions setObjectSerializer(ObjectSerializer objectSerializer) {
        this.objectSerializer = objectSerializer;
        return this;
    }

    /**
     * @return
     */
    public Integer getMaxConcurrency() {
        return maxConcurrency;
    }

    /**
     * @param maxConcurrency
     */
    public UploadLogsOptions setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        return this;
    }
}
