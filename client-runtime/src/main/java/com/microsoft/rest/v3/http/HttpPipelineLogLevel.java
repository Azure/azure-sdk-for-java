/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

/**
 * The different levels of logs from HttpPipeline's policies.
 */
public enum HttpPipelineLogLevel {
    /**
     * A log level that indicates that no logs will be logged.
     */
    OFF,

    /**
     * An error log.
     */
    ERROR,

    /**
     * A warning log.
     */
    WARNING,

    /**
     * An information log.
     */
    INFO
}