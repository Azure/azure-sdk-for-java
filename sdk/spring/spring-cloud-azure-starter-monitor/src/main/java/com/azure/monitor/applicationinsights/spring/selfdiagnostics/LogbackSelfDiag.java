// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring.selfdiagnostics;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

class LogbackSelfDiag implements CommandLineRunner {

    private final Logger selfDiagnosticsLogger;

    LogbackSelfDiag(Logger selfDiagnosticsLogger) {
        this.selfDiagnosticsLogger = selfDiagnosticsLogger;
    }

    @Override
    public void run(String... args) {
        try {
            applyLogbackSelfDiagnostics();
        } catch (Exception e) {
            selfDiagnosticsLogger.warn("An unexpected issue has happened during Logback self-diagnostics.", e);
        }
    }

    private void applyLogbackSelfDiagnostics() {
        if (selfDiagnosticsLogger.isDebugEnabled()) {
            ILoggerFactory loggerFactorySpi = LoggerFactory.getILoggerFactory();
            if (loggerFactorySpi instanceof LoggerContext) {
                List<Appender<ILoggingEvent>> logAppenders = findLogAppenders((LoggerContext) loggerFactorySpi);
                if (!hasOtelAppender(logAppenders)) {
                    selfDiagnosticsLogger.debug("To enable the logging instrumentation, add the OpenTelemetryAppender Logback appender.");
                }
                logAppendersAtTraceLevel(logAppenders);
            }
        }
    }

    private static List<Appender<ILoggingEvent>> findLogAppenders(LoggerContext loggerFactorySpi) {
        List<Appender<ILoggingEvent>> appenders = new ArrayList<>();
        for (ch.qos.logback.classic.Logger logger : loggerFactorySpi.getLoggerList()) {
            logger
                    .iteratorForAppenders()
                    .forEachRemaining(
                        appenders::add);
        }
        return appenders;
    }

    private static boolean hasOtelAppender(List<Appender<ILoggingEvent>> logAppenders) {
        return logAppenders.stream().anyMatch(appender -> appender.getClass().getName().equals("io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender"));
    }

    private void logAppendersAtTraceLevel(List<Appender<ILoggingEvent>> logAppenders) {
        if (selfDiagnosticsLogger.isTraceEnabled()) {
            String logAppendersAsString = logAppenders.stream().map(Object::toString).collect(Collectors.joining(", "));
            selfDiagnosticsLogger.trace("Logback appenders: " + logAppendersAsString);
        }
    }
}
