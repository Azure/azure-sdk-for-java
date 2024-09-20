// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.monitor.selfdiagnostics;

/**
 * This enum allows you to define a self-diagnostics level.
 */
public enum SelfDiagnosticsLevel {

    /**
     * Error self-diagnostics level
     */
    ERROR,
    /**
     * Warn self-diagnostics level
     */
    WARN,
    /**
     * Info self-diagnostics level
     */
    INFO,

    /**
     * Debug self-diagnostics level
     */
    DEBUG,

    /**
     * Trace self-diagnostics level
     */
    TRACE
}
