// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.Strings;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.Map;

final class ResourceParser {

    private static final String DEFAULT_SERVICE_NAME = "unknown_service:java";

    private ResourceParser() {
    }

    static void updateRoleNameAndInstance(AbstractTelemetryBuilder builder, Resource resource) {
        Map<String, String> existingTags = builder.build().getTags();
        if (existingTags == null
            || !existingTags.containsKey(ContextTagKeys.AI_CLOUD_ROLE.toString())) {
            String serviceName = resource.getAttribute(ResourceAttributes.SERVICE_NAME);
            if (serviceName == null || DEFAULT_SERVICE_NAME.equals(serviceName)) {
                String websiteSiteName = Strings.trimAndEmptyToNull(System.getenv("WEBSITE_SITE_NAME"));
                if (websiteSiteName != null) {
                    serviceName = websiteSiteName;
                }
            }
            String serviceNamespace = resource.getAttribute(ResourceAttributes.SERVICE_NAMESPACE);
            String roleName = null;
            if (serviceName != null && serviceNamespace != null) {
                roleName = "[" + serviceNamespace + "]/" + serviceName;
            } else if (serviceName != null) {
                roleName = serviceName;
            } else if (serviceNamespace != null) {
                roleName = "[" + serviceNamespace + "]";
            }
            if (roleName != null) {
                builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), roleName);
            }
        }

        if (existingTags == null
            || !existingTags.containsKey(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())) {
            String roleInstance = resource.getAttribute(ResourceAttributes.SERVICE_INSTANCE_ID);
            if (roleInstance == null) {
                roleInstance = Strings.trimAndEmptyToNull(System.getenv("WEBSITE_INSTANCE_ID"));
            }
            if (roleInstance == null) {
                roleInstance = System.getenv("HOSTNAME"); // default hostname
            }
            if (roleInstance != null) {
                builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), roleInstance);
            }
        }
    }
}
