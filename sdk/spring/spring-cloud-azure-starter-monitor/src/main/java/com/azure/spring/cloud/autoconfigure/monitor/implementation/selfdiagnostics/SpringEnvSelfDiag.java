// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.monitor.implementation.selfdiagnostics;

import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.AbstractMap.SimpleEntry;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class SpringEnvSelfDiag implements CommandLineRunner {

    private final Environment environment;

    private final Logger selfDiagnosticsLogger;

    SpringEnvSelfDiag(Environment environment, Logger selfDiagnosticsLogger) {
        this.environment = environment;
        this.selfDiagnosticsLogger = selfDiagnosticsLogger;
    }

    @Override
    public void run(String... args) {
        if (!selfDiagnosticsLogger.isTraceEnabled()) {
            return;
        }

        try {
            executeSpringEnvSelfDiag();
        } catch (Exception e) {
            selfDiagnosticsLogger.warn("An unexpected issue has happened during Spring env self-diagnostics.", e);
        }
    }

    private void executeSpringEnvSelfDiag() {
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
            String env = buildEnvAsString(configurableEnvironment);
            selfDiagnosticsLogger.trace("Env: " + env);
        }
    }

    private static String buildEnvAsString(ConfigurableEnvironment configurableEnvironment) {
        return StreamSupport.stream(configurableEnvironment.getPropertySources().spliterator(), false)
            .map(PropertySource::getSource)
            .filter(source -> source instanceof Map)
            .flatMap(source -> ((Map<?, ?>) source).entrySet().stream())
            .map(entry -> {
                String value = isSensitive(entry) ? "***" : entry.getValue().toString();
                return new SimpleEntry<>(entry.getKey().toString(), value);
            })
            .map(SimpleEntry::toString)
            .collect(Collectors.joining(", "));
    }

    private static boolean isSensitive(Map.Entry<?, ?> entry) {
        String key = entry.getKey().toString().toLowerCase(Locale.ROOT);
        return key.contains("password") || key.contains("pwd") || key.contains("secret");
    }
}
