// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.monitor.selfdiagnostics.implementation;

import com.azure.spring.cloud.autoconfigure.monitor.selfdiagnostics.SelfDiagnosticsLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Locale;

/**
 * Main configuration entry point of the self-diagnostics
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "otel.sdk.disabled", havingValue = "false", matchIfMissing = true)
@Import({DefaultLogConfig.class, LogbackSelfDiagConfig.class})
public class SelfDiagAutoConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SelfDiagAutoConfig.class);
    static final String SELF_DIAGNOSTICS_LEVEL_ENV_VAR = "APPLICATIONINSIGHTS_SELF_DIAGNOSTICS_LEVEL";

    @Bean
    SelfDiagnosticsLevel selfDiagnosticsLevel() {
        String selfDiagLevelEnvVar = System.getenv(SELF_DIAGNOSTICS_LEVEL_ENV_VAR);
        if (selfDiagLevelEnvVar == null) {
            return SelfDiagnosticsLevel.INFO;
        }
        try {
            String upperCaseLevel = selfDiagLevelEnvVar.toUpperCase(Locale.ROOT);
            return SelfDiagnosticsLevel.valueOf(upperCaseLevel);
        } catch (IllegalArgumentException e) {
            LOG.warn("Unable to find the self-diagnostics level related to " + selfDiagLevelEnvVar + "defined with " + SELF_DIAGNOSTICS_LEVEL_ENV_VAR + " environment variable.", e);
            return SelfDiagnosticsLevel.INFO;
        }
    }

    @Bean
    OtelSelfDiag otelSelfDiag(ApplicationContext applicationContext, Logger selfDiagnosticsLogger) {
        return new OtelSelfDiag(applicationContext, selfDiagnosticsLogger);
    }

    @Bean
    ExecutionEnvSelfDiag executionEnvSelfDiag(Logger selfDiagnosticsLogger) {
        return new ExecutionEnvSelfDiag(selfDiagnosticsLogger);
    }

}
