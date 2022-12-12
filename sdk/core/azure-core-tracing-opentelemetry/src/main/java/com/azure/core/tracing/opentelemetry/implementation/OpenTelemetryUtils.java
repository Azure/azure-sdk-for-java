// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.implementation;

import com.azure.core.tracing.opentelemetry.OpenTelemetrySchemaVersion;
import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;

public class OpenTelemetryUtils {
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetryUtils.class);

    private static final Map<String, String> ATTRIBUTE_MAPPING_V1_12_0 = getMappingsV1200();
    static final String HTTP_USER_AGENT = "http.user_agent";
    static final String HTTP_METHOD = "http.method";
    static final String HTTP_URL = "http.url";
    static final String HTTP_STATUS_CODE = "http.status_code";
    static final String SERVICE_REQUEST_ID_ATTRIBUTE = "serviceRequestId";
    static final String CLIENT_REQUEST_ID_ATTRIBUTE = "requestId";

    private static Map<String, String> getMappingsV1200() {
        Map<String, String> mappings = new HashMap<>();
        // messaging mapping, attributes are defined in com.azure.core.amqp.implementation.ClientConstants
        mappings.put(ENTITY_PATH_KEY, "messaging.destination");
        mappings.put(HOST_NAME_KEY, "net.peer.name");
        mappings.put(HTTP_USER_AGENT, "http.user_agent");
        mappings.put(HTTP_METHOD, "http.method");
        mappings.put(HTTP_URL, "http.url");
        mappings.put(HTTP_STATUS_CODE, "http.status_code");
        mappings.put(CLIENT_REQUEST_ID_ATTRIBUTE, "az.client_request_id");
        mappings.put(SERVICE_REQUEST_ID_ATTRIBUTE, "az.service_request_id");

        return Collections.unmodifiableMap(mappings);
    }

    public static Attributes convert(Map<String, Object> attributeMap, OpenTelemetrySchemaVersion schemaVersion) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            return Attributes.empty();
        }

        Map<String, String> mappings = getMappingsForVersion(schemaVersion);

        AttributesBuilder builder = Attributes.builder();
        for (Map.Entry<String, Object> kvp : attributeMap.entrySet()) {
            Objects.requireNonNull(kvp.getKey(), "'key' cannot be null.");
            if (kvp.getValue() == null) {
                continue;
            }
            OpenTelemetryUtils.addAttribute(builder, map(kvp.getKey(), mappings), kvp.getValue());
        }

        return builder.build();
    }

    public static void setAttribute(Span span, String key, Object value, OpenTelemetrySchemaVersion schemaVersion) {
        OpenTelemetryUtils.addAttribute(span, map(key, getMappingsForVersion(schemaVersion)), value);
    }

    private static Map<String, String> getMappingsForVersion(OpenTelemetrySchemaVersion version) {
        if (version == OpenTelemetrySchemaVersion.V1_12_0) {
            return ATTRIBUTE_MAPPING_V1_12_0;
        }

        // return latest mappings if version is not found.
        return ATTRIBUTE_MAPPING_V1_12_0;
    }

    private static String map(String propertyName, Map<String, String> mappings) {
        if (propertyName.startsWith("http.") || propertyName.startsWith("az.")) {
            return propertyName;
        }

        String otelKey = mappings.getOrDefault(propertyName, null);
        return otelKey != null ? otelKey : propertyName;
    }

    /**
     * Adds attribute key-value pair to OpenTelemetry {@link io.opentelemetry.api.common.AttributesBuilder}, if value type is not supported by
     * OpenTelemetry, drops the attribute.
     * @param attributesBuilder initialized {@link io.opentelemetry.api.common.AttributesBuilder} instance
     * @param key key of the attribute to be added
     * @param value value of the attribute to be added
     */
    static void addAttribute(io.opentelemetry.api.common.AttributesBuilder attributesBuilder, String key, Object value) {
        if (value instanceof String) {
            attributesBuilder.put(AttributeKey.stringKey(key), (String) value);
        } else if (value instanceof Long) {
            attributesBuilder.put(AttributeKey.longKey(key), (Long) value);
        } else if (value instanceof Integer) {
            attributesBuilder.put(AttributeKey.longKey(key), (Integer) value);
        } else if (value instanceof Boolean) {
            attributesBuilder.put(AttributeKey.booleanKey(key), (Boolean) value);
        } else if (value instanceof Double) {
            attributesBuilder.put(AttributeKey.doubleKey(key), (Double) value);
        } else if (value instanceof Float) {
            attributesBuilder.put(AttributeKey.doubleKey(key), ((Float) value).doubleValue());
        } else if (value instanceof Short) {
            attributesBuilder.put(AttributeKey.longKey(key), (Short) value);
        } else if (value instanceof Byte) {
            attributesBuilder.put(AttributeKey.longKey(key), (Byte) value);
        } else if (value instanceof String[]) {
            attributesBuilder.put(AttributeKey.stringArrayKey(key), (String[]) value);
        } else if (value instanceof Long[]) {
            attributesBuilder.put(AttributeKey.longArrayKey(key), (Long[]) value);
        } else if (value instanceof long[]) {
            long[] array = (long[]) value;
            List<Long> boxed = new ArrayList<>(array.length);
            for (int i = 0; i < array.length; i++) {
                boxed.add(array[i]);
            }
            attributesBuilder.put(AttributeKey.longArrayKey(key), boxed);
        } else if (value instanceof double[]) {
            double[] array = (double[]) value;
            List<Double> boxed = new ArrayList<>(array.length);
            for (int i = 0; i < array.length; i++) {
                boxed.add(array[i]);
            }
            attributesBuilder.put(AttributeKey.doubleArrayKey(key), boxed);
        } else if (value instanceof boolean[]) {
            boolean[] array = (boolean[]) value;
            List<Boolean> boxed = new ArrayList<>(array.length);
            for (int i = 0; i < array.length; i++) {
                boxed.add(array[i]);
            }
            attributesBuilder.put(AttributeKey.booleanArrayKey(key), boxed);
        } else {
            LOGGER.warning("Could not populate attribute with key '{}', type {} is not supported.", key, value.getClass().getName());
        }
    }

    static void addAttribute(Span span, String key, Object value) {
        if (value instanceof String) {
            span.setAttribute(AttributeKey.stringKey(key), (String) value);
        } else if (value instanceof Long) {
            span.setAttribute(AttributeKey.longKey(key), (Long) value);
        } else if (value instanceof Integer) {
            span.setAttribute(AttributeKey.longKey(key), (Integer) value);
        } else if (value instanceof Boolean) {
            span.setAttribute(AttributeKey.booleanKey(key), (Boolean) value);
        } else if (value instanceof Double) {
            span.setAttribute(AttributeKey.doubleKey(key), (Double) value);
        } else if (value instanceof Float) {
            span.setAttribute(AttributeKey.doubleKey(key), ((Float) value).doubleValue());
        } else if (value instanceof Short) {
            span.setAttribute(AttributeKey.longKey(key), (Short) value);
        } else if (value instanceof Byte) {
            span.setAttribute(AttributeKey.longKey(key), (Byte) value);
        } else {
            LOGGER.warning("Could not populate attribute with key '{}', type {} is not supported.", key, value.getClass().getName());
        }
    }
    /**
     * Parses an OpenTelemetry Status from AMQP Error Condition.
     *
     * @param span the span to set the status for.
     * @param statusMessage description for this error condition.
     * @param throwable the error occurred during response transmission (optional).
     * @return the corresponding OpenTelemetry {@link Span}.
     */
    public static Span setStatus(Span span, String statusMessage, Throwable throwable) {
        if (throwable != null) {
            span.recordException(throwable);
            return span.setStatus(StatusCode.ERROR);
        }
        if (statusMessage != null) {
            if ("success".equalsIgnoreCase(statusMessage)) {
                return span.setStatus(StatusCode.OK);
            } else if ("error".equalsIgnoreCase(statusMessage)) {
                return span.setStatus(StatusCode.ERROR);
            }
            return span.setStatus(StatusCode.UNSET, statusMessage);
        }

        return span.setStatus(StatusCode.UNSET);
    }

    public static Span setStatus(Span span, int httpStatusCode, Throwable throwable) {
        if (throwable != null) {
            span.recordException(throwable);
            return span.setStatus(StatusCode.ERROR);
        }

        return span.setStatus(httpStatusCode >= 400 ? StatusCode.ERROR : StatusCode.UNSET);
    }
}
