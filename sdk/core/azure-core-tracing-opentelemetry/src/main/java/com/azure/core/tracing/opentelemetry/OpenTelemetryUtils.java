// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.azure.core.tracing.opentelemetry.ExceptionUtils.unwrapError;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;

class OpenTelemetryUtils {
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetryUtils.class);

    static final String SERVICE_REQUEST_ID_ATTRIBUTE = "serviceRequestId";
    static final String AZ_SERVICE_REQUEST_ID_ATTRIBUTE = "az.service_request_id";
    static final String CLIENT_REQUEST_ID_ATTRIBUTE = "requestId";
    static final AttributeKey<String> ERROR_TYPE_ATTRIBUTE = AttributeKey.stringKey("error.type");

    public static Attributes convert(Map<String, Object> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            return Attributes.empty();
        }

        AttributesBuilder builder = Attributes.builder();
        for (Map.Entry<String, Object> kvp : attributeMap.entrySet()) {
            if (kvp.getValue() == null) {
                continue;
            }

            addAttribute(builder, kvp.getKey(), kvp.getValue());
        }

        return builder.build();
    }

    private static void mapKeyAndConsume(String key, Consumer<String> mappedKey) {
        // TODO (limolkova) remove all these mappings prior to plugin stability
        if ("http.method".equals(key)) {
            mappedKey.accept("http.request.method");
        } else if ("http.status_code".equals(key)) {
            mappedKey.accept("http.response.status_code");
        } else if ("http.url".equals(key)) {
            mappedKey.accept("url.full");
        } else if (ENTITY_PATH_KEY.equals(key)) {
            mappedKey.accept("messaging.destination.name");
        } else if (HOST_NAME_KEY.equals(key)) {
            mappedKey.accept("server.address");
        } else if (CLIENT_REQUEST_ID_ATTRIBUTE.equals(key)) {
            mappedKey.accept("az.client_request_id");
        } else if (SERVICE_REQUEST_ID_ATTRIBUTE.equals(key) || AZ_SERVICE_REQUEST_ID_ATTRIBUTE.equals(key)) {
            mappedKey.accept("az.service_request_id");
            mappedKey.accept("azure.service.request.id");
        } else if ("az.namespace".equals(key)) {
            mappedKey.accept("az.namespace");
            mappedKey.accept("azure.resource_provider.namespace");
        } else {
            mappedKey.accept(key);
        }
    }

    /**
     * Adds attribute key-value pair to OpenTelemetry {@link AttributesBuilder}, if value type is not supported by
     * OpenTelemetry, drops the attribute.
     *
     * @param attributesBuilder initialized {@link AttributesBuilder} instance
     * @param key key of the attribute to be added
     * @param value value of the attribute to be added
     */
    static void addAttribute(AttributesBuilder attributesBuilder, String key, Object value) {
        Objects.requireNonNull(key, "OpenTelemetry attribute name cannot be null.");
        if (value instanceof String) {
            mapKeyAndConsume(key,
                mappedKey -> attributesBuilder.put(AttributeKey.stringKey(mappedKey), (String) value));
        } else if (value instanceof Long) {
            mapKeyAndConsume(key, mappedKey -> attributesBuilder.put(AttributeKey.longKey(mappedKey), (Long) value));
        } else if (value instanceof Integer) {
            mapKeyAndConsume(key, mappedKey -> attributesBuilder.put(AttributeKey.longKey(mappedKey), (Integer) value));
        } else if (value instanceof Boolean) {
            mapKeyAndConsume(key,
                mappedKey -> attributesBuilder.put(AttributeKey.booleanKey(mappedKey), (Boolean) value));
        } else if (value instanceof Double) {
            mapKeyAndConsume(key,
                mappedKey -> attributesBuilder.put(AttributeKey.doubleKey(mappedKey), (Double) value));
        } else if (value instanceof Float) {
            mapKeyAndConsume(key,
                mappedKey -> attributesBuilder.put(AttributeKey.doubleKey(mappedKey), ((Float) value).doubleValue()));
        } else if (value instanceof Short) {
            mapKeyAndConsume(key, mappedKey -> attributesBuilder.put(AttributeKey.longKey(mappedKey), (Short) value));
        } else if (value instanceof Byte) {
            mapKeyAndConsume(key, mappedKey -> attributesBuilder.put(AttributeKey.longKey(mappedKey), (Byte) value));
        } else {
            LOGGER.warning("Could not populate attribute with key '{}', type {} is not supported.", key,
                value.getClass().getName());
        }
    }

    /**
     * Adds attribute key-value pair to OpenTelemetry {@link Span}, if value type is not supported by
     * OpenTelemetry, drops the attribute.
     *
     * @param span {@link Span} instance
     * @param key key of the attribute to be added
     * @param value value of the attribute to be added
     */
    static void addAttribute(Span span, String key, Object value) {
        Objects.requireNonNull(key, "OpenTelemetry attribute name cannot be null.");

        if (value instanceof String) {
            mapKeyAndConsume(key, mappedKey -> span.setAttribute(AttributeKey.stringKey(mappedKey), (String) value));
        } else if (value instanceof Long) {
            mapKeyAndConsume(key, mappedKey -> span.setAttribute(AttributeKey.longKey(mappedKey), (Long) value));
        } else if (value instanceof Integer) {
            mapKeyAndConsume(key, mappedKey -> span.setAttribute(AttributeKey.longKey(mappedKey), (Integer) value));
        } else if (value instanceof Boolean) {
            mapKeyAndConsume(key, mappedKey -> span.setAttribute(AttributeKey.booleanKey(mappedKey), (Boolean) value));
        } else if (value instanceof Double) {
            mapKeyAndConsume(key, mappedKey -> span.setAttribute(AttributeKey.doubleKey(mappedKey), (Double) value));
        } else if (value instanceof Float) {
            mapKeyAndConsume(key,
                mappedKey -> span.setAttribute(AttributeKey.doubleKey(mappedKey), ((Float) value).doubleValue()));
        } else if (value instanceof Short) {
            mapKeyAndConsume(key, mappedKey -> span.setAttribute(AttributeKey.longKey(mappedKey), (Short) value));
        } else if (value instanceof Byte) {
            mapKeyAndConsume(key, mappedKey -> span.setAttribute(AttributeKey.longKey(mappedKey), (Byte) value));
        } else {
            LOGGER.warning("Could not populate attribute with key '{}', type {} is not supported.", key,
                value.getClass().getName());
        }
    }

    /**
     * Parses an OpenTelemetry status from error description.
     *
     * @param span the span to set the status for.
     * @param statusMessage description for this error condition. Any non-null {@code statusMessage} indicates an error.
     *                      Must be of a low-cardinality.
     * @param throwable the error occurred during response transmission (optional).
     * @return the corresponding OpenTelemetry {@link Span}.
     */
    static Span setError(Span span, String statusMessage, Throwable throwable) {
        if (!span.isRecording()) {
            return span;
        }

        // "success" is needed for back compat with older Event Hubs and Service Bus, don't use it.
        if ("success".equals(statusMessage)) {
            statusMessage = null;
        }

        throwable = unwrapError(throwable);
        if (statusMessage == null && throwable == null) {
            return span;
        }

        span.setAttribute(ERROR_TYPE_ATTRIBUTE, statusMessage != null ? statusMessage : throwable.getClass().getName());

        return span.setStatus(StatusCode.ERROR, throwable != null ? throwable.getMessage() : statusMessage);
    }
}
