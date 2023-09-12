// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring.selfdiagnostics;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Default self-diagnostics features for logging when Logback is not found.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingClass({"ch.qos.logback.classic.LoggerContext"})
public class DefaultLogConfig {

    /**
     * To define a logger for self-diagnostics.
     * @return A logger for self-diagnostics
     */
    @Bean
    public Logger selfDiagnosticsLogger() {
        Logger logger = LoggerFactory.getLogger(SelfDiagnostics.class);
        Optional<SelfDiagnosticsLevel> selfDiagnosticsLevel = findSelfDiagnosticsLevel(logger);
        logger.warn("Logback don't seem to be used." + (selfDiagnosticsLevel.isPresent() ? " The self-diagnostics level is " + selfDiagnosticsLevel.get().name() : ""));
        return logger;
    }

    private static Optional<SelfDiagnosticsLevel> findSelfDiagnosticsLevel(Logger logger) {
        if (logger.isErrorEnabled()) {
            return Optional.of(SelfDiagnosticsLevel.ERROR);
        }
        if (logger.isWarnEnabled()) {
            return Optional.of(SelfDiagnosticsLevel.WARN);
        }
        if (logger.isInfoEnabled()) {
            return Optional.of(SelfDiagnosticsLevel.INFO);
        }
        if (logger.isDebugEnabled()) {
            return Optional.of(SelfDiagnosticsLevel.DEBUG);
        }
        if (logger.isTraceEnabled()) {
            return Optional.of(SelfDiagnosticsLevel.TRACE);
        }
        return Optional.empty();
    }
}
