package com.microsoft.windowsazure.services.core.storage;

/**
 * Enumeration representing the state of metrics collection in a service.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum MetricsLevel {
    /**
     * Metrics collection is disabled.
     */
    DISABLED,

    /**
     * Service-level metrics collection is enabled.
     */
    SERVICE,

    /**
     * Service-level and API metrics collection are enabled.
     */
    SERVICE_AND_API;
}
