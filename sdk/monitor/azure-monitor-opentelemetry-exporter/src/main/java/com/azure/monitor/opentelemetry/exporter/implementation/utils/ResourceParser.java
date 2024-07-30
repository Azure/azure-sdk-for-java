// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;
import io.opentelemetry.semconv.incubating.ServiceIncubatingAttributes;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class ResourceParser {

    private static final String DEFAULT_SERVICE_NAME = "unknown_service:java";

    // these are only used in App Services environment
    private final String websiteSiteName;
    private final String websiteSiteInstance;

    public ResourceParser() {
        this(System.getenv());
    }

    // visible for testing
    ResourceParser(Map<String, String> envVars) {
        websiteSiteName = getWebsiteSiteNameEnvVar(envVars::get);
        websiteSiteInstance = envVars.get("WEBSITE_INSTANCE_ID");
    }

    // visible for testing
    public void updateRoleNameAndInstance(
        AbstractTelemetryBuilder builder, Resource resource) {

        // update AKS role name and role instance
        if (AksResourceAttributes.isAks(resource)) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), AksResourceAttributes.getAksRoleName(resource));
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), AksResourceAttributes.getAksRoleInstance(resource));
            return;
        }

        Map<String, String> tags = builder.build().getTags();
        if (tags == null || !tags.containsKey(ContextTagKeys.AI_CLOUD_ROLE.toString())) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), getRoleName(resource));
        }

        if (tags == null || !tags.containsKey(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), getRoleInstance(resource));
        }
    }

    private String getRoleName(Resource resource) {
        String serviceName = resource.getAttribute(ServiceAttributes.SERVICE_NAME);
        if (serviceName == null || DEFAULT_SERVICE_NAME.equals(serviceName)) {
            if (websiteSiteName != null) {
                serviceName = websiteSiteName;
            }
        }
        String serviceNamespace = resource.getAttribute(ServiceIncubatingAttributes.SERVICE_NAMESPACE);
        if (serviceName != null && serviceNamespace != null) {
            return "[" + serviceNamespace + "]/" + serviceName;
        } else if (serviceName != null) {
            return serviceName;
        } else if (serviceNamespace != null) {
            return "[" + serviceNamespace + "]";
        }
        return DEFAULT_SERVICE_NAME; // service.name is required so shouldn't get here anyways
    }

    private String getRoleInstance(Resource resource) {
        String roleInstance = resource.getAttribute(ServiceIncubatingAttributes.SERVICE_INSTANCE_ID);
        if (roleInstance != null) {
            return roleInstance;
        }
        roleInstance = websiteSiteInstance;
        if (roleInstance != null) {
            return roleInstance;
        }
        return HostName.get(); // default hostname
    }

    public static String getWebsiteSiteNameEnvVar(Function<String, String> envVars) {
        String websiteSiteName = envVars.apply("WEBSITE_SITE_NAME");
        if (websiteSiteName != null && inAzureFunctionsWorker(envVars)) {
            // special case for Azure Functions
            return websiteSiteName.toLowerCase(Locale.ROOT);
        }
        return websiteSiteName;
    }

    public static boolean inAzureFunctionsWorker(Function<String, String> envVars) {
        return "java".equals(envVars.apply("FUNCTIONS_WORKER_RUNTIME"));
    }
}
