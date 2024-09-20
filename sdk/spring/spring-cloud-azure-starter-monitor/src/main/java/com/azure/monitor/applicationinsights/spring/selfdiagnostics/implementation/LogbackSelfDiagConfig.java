// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring.selfdiagnostics.implementation;

import ch.qos.logback.classic.Level;
import com.azure.monitor.applicationinsights.spring.selfdiagnostics.SelfDiagnosticsLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Logback self-diagnostics features.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ch.qos.logback.classic.LoggerContext.class)
public class LogbackSelfDiagConfig {


    /**
     * To define a logger for self-diagnostics.
     *
     * @param selfDiagnosticsLevel The self-diagnostics level
     * @return A logger for self-diagnostics
     */
    @Bean
    public Logger selfDiagnosticsLogger(SelfDiagnosticsLevel selfDiagnosticsLevel) {
        Logger slf4jLog = LoggerFactory.getLogger(SelfDiagnostics.class);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) slf4jLog;
        Level logbackLevel = findLogbackLevelFrom(selfDiagnosticsLevel, slf4jLog);
        logbackLogger.setLevel(logbackLevel);
        return logbackLogger;
    }

    private static Level findLogbackLevelFrom(SelfDiagnosticsLevel selfDiagnosticsLevel, Logger slf4jLog) {
        try {
            return Level.valueOf(selfDiagnosticsLevel.name());
        } catch (IllegalArgumentException e) {
            slf4jLog.warn("Unable to find Logback " + selfDiagnosticsLevel.name() + " level.", e);
            return Level.INFO;
        }
    }

}
