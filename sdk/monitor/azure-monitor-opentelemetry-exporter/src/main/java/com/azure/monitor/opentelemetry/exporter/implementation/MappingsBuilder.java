// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Trie;
import io.opentelemetry.api.common.AttributeKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

class MappingsBuilder {

    // TODO need to keep this list in sync as new semantic conventions are defined
    private static final Set<String> STANDARD_ATTRIBUTE_PREFIXES =
        new HashSet<>(
            asList(
                "http.",
                "db.",
                "message.",
                "messaging.",
                "rpc.",
                "enduser.",
                "net.",
                "peer.",
                "exception.",
                "thread.",
                "faas.",
                "code.",
                "job.", // proposed semantic convention which we use for job,
                "applicationinsights.internal."));

    private final Map<String, ExactMapping> exactMappings = new HashMap<>();
    private final Trie.Builder<PrefixMapping> prefixMappings = Trie.newBuilder();

    MappingsBuilder() {
        // ignore all standard attribute prefixes
        for (String prefix : STANDARD_ATTRIBUTE_PREFIXES) {
            prefixMappings.put(prefix, (telemetryBuilder, key, value) -> {
            });
        }
    }

    MappingsBuilder ignoreExact(String key) {
        exactMappings.put(key, (telemetryBuilder, value) -> {
        });
        return this;
    }

    MappingsBuilder ignorePrefix(String prefix) {
        prefixMappings.put(prefix, (telemetryBuilder, key, value) -> {
        });
        return this;
    }

    MappingsBuilder exact(String key, ExactMapping mapping) {
        exactMappings.put(key, mapping);
        return this;
    }

    MappingsBuilder prefix(String prefix, PrefixMapping mapping) {
        prefixMappings.put(prefix, mapping);
        return this;
    }

    MappingsBuilder exactString(AttributeKey<String> attributeKey, String propertyName) {
        exactMappings.put(
            attributeKey.getKey(),
            (telemetryBuilder, value) -> {
                if (value instanceof String) {
                    telemetryBuilder.addProperty(propertyName, (String) value);
                }
            });
        return this;
    }

    MappingsBuilder exactLong(AttributeKey<Long> attributeKey, String propertyName) {
        exactMappings.put(
            attributeKey.getKey(),
            (telemetryBuilder, value) -> {
                if (value instanceof Long) {
                    telemetryBuilder.addProperty(propertyName, Long.toString((Long) value));
                }
            });
        return this;
    }

    public Mappings build() {
        return new Mappings(exactMappings, prefixMappings.build());
    }

    @FunctionalInterface
    interface ExactMapping {
        void map(AbstractTelemetryBuilder telemetryBuilder, Object value);
    }

    @FunctionalInterface
    interface PrefixMapping {
        void map(AbstractTelemetryBuilder telemetryBuilder, String key, Object value);
    }
}
