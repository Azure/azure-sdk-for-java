// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.monitor.implementation.selfdiagnostics;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ch.qos.logback.classic.LoggerContext.class)
class LogbackSelfDiagConfig {

    @Bean
    Logger selfDiagnosticsLogger(SelfDiagnosticsLevel selfDiagnosticsLevel) {
        Logger slf4jLog = LoggerFactory.getLogger(SelfDiagnostics.class);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) slf4jLog;
        Level logbackLevel = Level.toLevel(selfDiagnosticsLevel.name());
        logbackLogger.setLevel(logbackLevel);
        return logbackLogger;
    }

    @Bean
    CommandLineRunner autoConfigureDistroCommandLine(SelfDiagnosticsLevel selfDiagnosticsLevel) {
        return args -> {
            ILoggerFactory loggerFactorySpi = LoggerFactory.getILoggerFactory();
            if (!(loggerFactorySpi instanceof LoggerContext)) {
                return;
            }

            LoggerContext loggerContext = (LoggerContext) loggerFactorySpi;

            Level selfDiagLogbackLevel = Level.toLevel(selfDiagnosticsLevel.name());

            ch.qos.logback.classic.Logger azureMonitorOTelLogger = loggerContext.getLogger("com.azure.monitor.opentelemetry");
            azureMonitorOTelLogger.setLevel(selfDiagLogbackLevel);
        };
    }

}
