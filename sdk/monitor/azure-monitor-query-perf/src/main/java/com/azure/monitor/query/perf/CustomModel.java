// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.perf;

import java.time.OffsetDateTime;

/**
 * A custom model to read the logs query result.
 */
public final class CustomModel {
    private OffsetDateTime timeGenerated;
    private String tenantId;
    private String id;
    private String source;
    private Boolean success;
    private Double durationMs;
    private Object properties;
    private String operationName;
    private String operationId;


    /**
     * Returns the time the log event was generated.
     * @return the time the log event was generated.
     */
    public OffsetDateTime getTimeGenerated() {
        return timeGenerated;
    }

    /**
     * Returns the tenant id of the resource for which this log was recorded.
     * @return the tenant id of the resource for which this log was recorded.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Returns the unique identifier of this log.
     * @return the unique identifier of this log.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the source of this log.
     * @return the source of this log.
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns {@code true} if the logged request returned a successful response.
     * @return {@code true} if the logged request returned a successful response.
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Returns the time duration the service took to process the request.
     * @return the time duration the service took to process the request.
     */
    public Double getDurationMs() {
        return durationMs;
    }

    /**
     * Returns additional properties of the request.
     * @return additional properties of the request.
     */
    public Object getProperties() {
        return properties;
    }

    /**
     * Returns the name of the operation.
     * @return the name of the operation.
     */
    public String getOperationName() {
        return operationName;
    }
}
