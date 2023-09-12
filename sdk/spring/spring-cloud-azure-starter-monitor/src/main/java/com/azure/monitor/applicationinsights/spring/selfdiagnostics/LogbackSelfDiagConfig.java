// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring.selfdiagnostics;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ch.qos.logback.classic.LoggerContext.class)
public class LogbackSelfDiagConfig {

    @Bean
    public Logger selfDiagnosticsLogger(SelfDiagnosticsLevel selfDiagnosticsLevel) {
        Logger sl4jLoggger = LoggerFactory.getLogger(SelfDiagnostics.class);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) sl4jLoggger;
        Level logbackLevel = findLogbackLevelFrom(selfDiagnosticsLevel, sl4jLoggger);
        logbackLogger.setLevel(logbackLevel);
        return logbackLogger;
    }

    private static Level findLogbackLevelFrom(SelfDiagnosticsLevel selfDiagnosticsLevel, Logger sl4jLoggger) {
        try {
            return Level.valueOf(selfDiagnosticsLevel.name());
        } catch (IllegalArgumentException e) {
            sl4jLoggger.warn("Unable to find Logback " + selfDiagnosticsLevel.name() + " level.", e);
            return Level.INFO;
        }
    }

    @Bean
    public CommandLineRunner logbackSelfDiagnostics(Logger selfDiagnosticsLogger) {
        return new LogbackSelfDiag(selfDiagnosticsLogger);
    }
}
