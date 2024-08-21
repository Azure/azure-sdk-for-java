// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Trie;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class Mappings {

    private static final ClientLogger logger = new ClientLogger(Mappings.class);
    private static final Set<AttributeType> unexpectedTypesLogged = ConcurrentHashMap.newKeySet();

    private final Map<String, MappingsBuilder.ExactMapping> exactMappings;
    private final Trie<MappingsBuilder.PrefixMapping> prefixMappings;

    Mappings(Map<String, MappingsBuilder.ExactMapping> exactMappings,
        Trie<MappingsBuilder.PrefixMapping> prefixMappings) {
        this.exactMappings = exactMappings;
        this.prefixMappings = prefixMappings;
    }

    void map(Attributes attributes, AbstractTelemetryBuilder telemetryBuilder) {
        attributes.forEach((attributeKey, value) -> map(telemetryBuilder, attributeKey, value));
    }

    private void map(AbstractTelemetryBuilder telemetryBuilder, AttributeKey<?> attributeKey, Object value) {
        String key = attributeKey.getKey();
        MappingsBuilder.ExactMapping exactMapping = exactMappings.get(key);
        if (exactMapping != null) {
            exactMapping.map(telemetryBuilder, value);
            return;
        }
        MappingsBuilder.PrefixMapping prefixMapping = prefixMappings.getOrNull(key);
        if (prefixMapping != null) {
            prefixMapping.map(telemetryBuilder, key, value);
            return;
        }
        String val = convertToString(value, attributeKey.getType());
        if (val != null) {
            telemetryBuilder.addProperty(attributeKey.getKey(), val);
        }
    }

    @Nullable
    public static String convertToString(Object value, AttributeType type) {
        switch (type) {
            case STRING:
            case BOOLEAN:
            case LONG:
            case DOUBLE:
                return String.valueOf(value);

            case STRING_ARRAY:
            case BOOLEAN_ARRAY:
            case LONG_ARRAY:
            case DOUBLE_ARRAY:
                return join((List<?>) value);
        }
        if (unexpectedTypesLogged.add(type)) {
            logger.warning("unexpected attribute type: {}", type);
        }
        return null;
    }

    static <T> String join(List<T> values) {
        StringBuilder sb = new StringBuilder();
        for (Object val : values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(val);
        }
        return sb.toString();
    }
}
