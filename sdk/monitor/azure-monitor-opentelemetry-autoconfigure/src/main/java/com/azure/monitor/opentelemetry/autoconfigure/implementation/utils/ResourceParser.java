// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.ContextTagKeys;
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
    public void updateRoleNameAndInstanceAndVersion(AbstractTelemetryBuilder builder, Resource resource) {

        Map<String, String> tags = builder.build().getTags();
        if (tags == null || !tags.containsKey(ContextTagKeys.AI_CLOUD_ROLE.toString())) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), getRoleName(resource));
        }

        if (tags == null || !tags.containsKey(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString())) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), getRoleInstance(resource));
        }

        if (tags == null || !tags.containsKey(ContextTagKeys.AI_APPLICATION_VER.toString())) {
            String applicationVersion = resource.getAttribute(ServiceAttributes.SERVICE_VERSION);
            if (applicationVersion != null) {
                builder.addTag(ContextTagKeys.AI_APPLICATION_VER.toString(), applicationVersion);
            }
        }
    }

    private String getRoleName(Resource resource) {
        if (AksResourceAttributes.isAks(resource)) {
            return AksResourceAttributes.getAksRoleName(resource);
        }

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
        if (AksResourceAttributes.isAks(resource)) {
            return AksResourceAttributes.getAksRoleInstance(resource);
        }

        String roleInstance = resource.getAttribute(ServiceIncubatingAttributes.SERVICE_INSTANCE_ID);
        if (roleInstance != null) {
            return roleInstance;
        }
        roleInstance = websiteSiteInstance;
        if (roleInstance != null) {
            return roleInstance;
        }
        roleInstance = HostName.get(); // default hostname
        if (roleInstance != null) {
            return roleInstance;
        }
        return "unknown"; // this is for backwards compatibility in the Java agent
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
