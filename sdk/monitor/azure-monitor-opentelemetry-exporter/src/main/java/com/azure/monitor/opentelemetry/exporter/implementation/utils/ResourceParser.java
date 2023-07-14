// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AksResourceAttributes.getAksRoleInstance;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AksResourceAttributes.getAksRoleName;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AksResourceAttributes.isAks;

import com.azure.core.util.Configuration;
import com.azure.monitor.opentelemetry.exporter.implementation.ResourceAttributes;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import io.opentelemetry.sdk.resources.Resource;

import java.util.Map;

public final class ResourceParser {

    private static final String DEFAULT_SERVICE_NAME = "unknown_service:java";

    // visible for testing
    public static void updateRoleNameAndInstance(
        AbstractTelemetryBuilder builder, Resource resource, Configuration configuration) {

        // update AKS role name and role instance
        if (isAks()) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), getAksRoleName());
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), getAksRoleInstance());
            return;
        }

        Map<String, String> existingTags = builder.build().getTags();
        if (existingTags == null
            || !existingTags.containsKey(ContextTagKeys.AI_CLOUD_ROLE.toString())) {
            String serviceName = resource.getAttribute(ResourceAttributes.SERVICE_NAME);
            if (serviceName == null || DEFAULT_SERVICE_NAME.equals(serviceName)) {
                String websiteSiteName = Strings.trimAndEmptyToNull(configuration.get("WEBSITE_SITE_NAME"));
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
                roleInstance = Strings.trimAndEmptyToNull(configuration.get("WEBSITE_INSTANCE_ID"));
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
