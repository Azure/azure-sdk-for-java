// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.monitor.opentelemetry.exporter.implementation.ResourceAttributes;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;

import java.util.Map;

public final class ResourceParser {

    private static final String DEFAULT_SERVICE_NAME = "unknown_service:java";

    // visible for testing
    public static void updateRoleNameAndInstance(
        AbstractTelemetryBuilder builder, Resource resource, ConfigProperties configProperties) {

        // update AKS role name and role instance
        if (AksResourceAttributes.isAks(resource)) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), AksResourceAttributes.getAksRoleName(resource));
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), AksResourceAttributes.getAksRoleInstance(resource));
            return;
        }

        Map<String, String> existingTags = builder.build().getTags();
        if (existingTags == null
            || !existingTags.containsKey(ContextTagKeys.AI_CLOUD_ROLE.toString())) {
            String serviceName = resource.getAttribute(ResourceAttributes.SERVICE_NAME);
            if (serviceName == null || DEFAULT_SERVICE_NAME.equals(serviceName)) {
                String websiteSiteName = Strings.trimAndEmptyToNull(configProperties.getString("WEBSITE_SITE_NAME"));
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
                roleInstance = Strings.trimAndEmptyToNull(configProperties.getString("WEBSITE_INSTANCE_ID"));
            }
            if (roleInstance == null) {
                roleInstance = HostName.get(); // default hostname
            }
            if (roleInstance != null) {
                builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), roleInstance);
            }
        }
    }

    private ResourceParser() {
    }
}
