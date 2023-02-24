// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import org.slf4j.event.Level;

/**
 * Configuration options that determine how the Cosmos diagnostics logger decides whether to log diagnostics
 * for an operation or not
 */
public final class CosmosDiagnosticsLoggerConfig {

    private final Level levelForSuccessfulOperationsWithRequestDiagnostics = Level.TRACE;
    private Level levelForSuccessfulOperations = Level.DEBUG;
    private Level levelForFailures = Level.WARN;
    private Level levelForThresholdViolations = Level.INFO;

    /**
     * Creates an instance of the CosmosDiagnosticsLoggerConfig class with default parameters
     */
    public CosmosDiagnosticsLoggerConfig() {
    }

    /**
     * Sets the level used for logging successful operations (when latency, request charge and payload size
     * are all within the configured threshold). This level should always be
     * equal or lower than failures or threshold violations.
     * @param level the log level to be used
     * @return the current {@link CosmosDiagnosticsLogger} instance
     */
    public CosmosDiagnosticsLoggerConfig setLevelForSuccessfulOperations(Level level) {
        this.levelForSuccessfulOperations = level;

        return this;
    }

    /**
     * Sets the level used for logging request diagnostics of successful operations (when latency, request charge
     * and payload size are all within the configured threshold). This level should always be
     * equal or lower than for successful operations, failures or threshold violations.
     * @param level level the log level to be used
     * @return the current {@link CosmosDiagnosticsLogger} instance
     */
    public CosmosDiagnosticsLoggerConfig setLevelForRequestDiagnosticsOfSuccessfulOperations(Level level) {
        this.levelForSuccessfulOperations = level;

        return this;
    }

    /**
     * Sets the level used for logging request diagnostics of failed operations (when the (sub)status code indicates
     * the operation to have failed). The level for failed operations should always be equal or higher (more critical)
     * than for threshold violations or successful operations.
     * @param level level the log level to be used
     * @return the current {@link CosmosDiagnosticsLogger} instance
     */
    public CosmosDiagnosticsLoggerConfig setLevelForFailedOperations(Level level) {
        this.levelForFailures = level;

        return this;
    }

    /**
     * Sets the level used for logging request diagnostics of successful operations violating one of the latency,
     * request charge or payload size thresholds. The level for successful operations violating thresholds should
     * not exceed the level for failures and not be lower than fow successful operations.
     * @param level level the log level to be used
     * @return the current {@link CosmosDiagnosticsLogger} instance
     */
    public CosmosDiagnosticsLoggerConfig setLevelForThresholdViolations(Level level) {
        this.levelForThresholdViolations = level;

        return this;
    }

    Level getLevelForSuccessfulOperationsWithRequestDiagnostics() {
        return this.levelForSuccessfulOperationsWithRequestDiagnostics;
    }

    Level getLevelForSuccessfulOperations() {
        return this.levelForSuccessfulOperations;
    }

    Level getLevelForFailures() {
        return this.levelForFailures;
    }

    Level getLevelForThresholdViolations() {
        return this.levelForThresholdViolations;
    }
}