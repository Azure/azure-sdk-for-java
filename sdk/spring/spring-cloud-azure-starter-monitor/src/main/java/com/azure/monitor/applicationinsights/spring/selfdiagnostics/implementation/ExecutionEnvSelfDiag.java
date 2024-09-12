// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring.selfdiagnostics.implementation;

import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;

class ExecutionEnvSelfDiag implements CommandLineRunner {
    private final Logger selfDiagnosticsLogger;

    ExecutionEnvSelfDiag(Logger selfDiagnosticsLogger) {
        this.selfDiagnosticsLogger = selfDiagnosticsLogger;
    }

    @Override
    public void run(String... args) {
        try {
            executeExecutionEnvSelfDiagnostics();
        } catch (Exception e) {
            selfDiagnosticsLogger.warn("An unexpected issue has happened during execution env self-diagnostics.", e);
        }
    }

    private void executeExecutionEnvSelfDiagnostics() {
        if (selfDiagnosticsLogger.isDebugEnabled()) {
            boolean nativeRuntimeExecution = isNativeRuntimeExecution();
            selfDiagnosticsLogger.debug("GraalVM native: " + nativeRuntimeExecution);
        }
        if (selfDiagnosticsLogger.isTraceEnabled()) {
            selfDiagnosticsLogger.trace("OS: " + System.getProperty("os.name"));
            selfDiagnosticsLogger.trace("Env: " + System.getenv());
            selfDiagnosticsLogger.trace("System properties: " + System.getProperties());
        }
    }

    private static boolean isNativeRuntimeExecution() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }
}
