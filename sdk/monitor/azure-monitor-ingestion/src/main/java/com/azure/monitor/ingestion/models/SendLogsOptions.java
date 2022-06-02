package com.azure.monitor.ingestion.models;

import com.azure.core.util.serializer.ObjectSerializer;

/**
 *
 */
public final class SendLogsOptions {

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
    public SendLogsOptions setObjectSerializer(ObjectSerializer objectSerializer) {
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
    public SendLogsOptions setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        return this;
    }
}
