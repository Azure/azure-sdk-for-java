// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.TelemetryAttributes;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.AZ_NAMESPACE_KEY;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.MESSAGE_BUS_DESTINATION;
import static com.azure.core.tracing.opentelemetry.OpenTelemetryTracer.PEER_ENDPOINT;

/**
 * OpenTelemetry-specific implementation of {@link TelemetryAttributes}
 */
class OpenTelemetryAttributes implements TelemetryAttributes {
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
        mappings.put(MESSAGE_BUS_DESTINATION, "messaging.destination");
        mappings.put(PEER_ENDPOINT, "net.peer.name");
        mappings.put(HTTP_USER_AGENT, "http.user_agent");
        mappings.put(HTTP_METHOD, "http.method");
        mappings.put(HTTP_URL, "http.url");
        mappings.put(HTTP_STATUS_CODE, "http.status_code");
        mappings.put(AZ_NAMESPACE_KEY, "az.namespace");
        mappings.put(CLIENT_REQUEST_ID_ATTRIBUTE, "az.client_request_id");
        mappings.put(SERVICE_REQUEST_ID_ATTRIBUTE, "az.service_request_id");

        return Collections.unmodifiableMap(mappings);
    }

    private final Attributes attributes;
    OpenTelemetryAttributes(Map<String, Object> attributeMap, OpenTelemetrySchemaVersion schemaVersion) {
        if (attributeMap == null) {
            attributes = Attributes.empty();
            return;
        }

        AttributesBuilder builder = Attributes.builder();
        Map<String, String> mappings = getMappingsForVersion(schemaVersion);
        for (Map.Entry<String, Object> kvp : attributeMap.entrySet()) {
            Objects.requireNonNull(kvp.getKey(), "'key' cannot be null.");
            if (kvp.getValue() == null) {
                continue;
            }

            String azKey = kvp.getKey();
            String otelKey = mappings.getOrDefault(azKey, null);
            if (otelKey == null) {
                otelKey = mapUnknown(azKey);
            }

            OpenTelemetryUtils.addAttribute(builder, otelKey, kvp.getValue());
        }

        attributes = builder.build();
    }

    Attributes get() {
        return attributes;
    }

    private static Map<String, String> getMappingsForVersion(OpenTelemetrySchemaVersion version) {
        if (version.equals(OpenTelemetrySchemaVersion.V1_12_0)) {
            return ATTRIBUTE_MAPPING_V1_12_0;
        }

        // TODO (limolkova) log warning
        // return latest mappings if version is not found.
        return ATTRIBUTE_MAPPING_V1_12_0;
    }

    private static String mapUnknown(String propertyName) {
        // TODO: by GA we need to figure out default naming (when no mapping is defined)
        // and follow otel attributes conventions if we can or make sure mapping is defined
        // for all attributes

        // TODO: to az.snake_case
        // log verbose
        return propertyName;
    }
}
