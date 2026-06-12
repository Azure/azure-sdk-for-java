// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import org.springframework.util.StringUtils;

import com.azure.core.util.Configuration;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.AI_CHAT_COMPLETION_FEATURE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.AI_CONFIGURATION_FEATURE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.APP_CONFIG_AICC_MIME_PROFILE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.APP_CONFIG_AI_MIME_PROFILE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.KEY_VAULT_CONFIGURED_TRACING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.LOAD_BALANCING_FEATURE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.PUSH_REFRESH;
import com.azure.spring.cloud.appconfiguration.config.implementation.HostType;
import com.azure.spring.cloud.appconfiguration.config.implementation.JsonConfigurationParser;
import com.azure.spring.cloud.appconfiguration.config.implementation.RequestTracingConstants;
import com.azure.spring.cloud.appconfiguration.config.implementation.RequestType;

public class TracingInfo {

    private static final String DELIMITER = "+";

    private boolean isKeyVaultConfigured = false;

    private int replicaCount;

    private FeatureFlagTracing featureFlagTracing;

    private final Configuration configuration;

    private boolean usesLoadBalancing = false;

    private boolean usesAiConfiguration = false;

    private boolean usesAiccConfiguration = false;

    private boolean isFailoverRequest = false;

    public TracingInfo(boolean isKeyVaultConfigured, int replicaCount, Configuration configuration) {
        this.isKeyVaultConfigured = isKeyVaultConfigured;
        this.replicaCount = replicaCount;
        this.featureFlagTracing = new FeatureFlagTracing();
        this.configuration = configuration;
    }

    /**
     * Marks load balancing as enabled.
     */
    public void setUsesLoadBalancing() {
        this.usesLoadBalancing = true;
    }

    /**
     * Marks the next request as a failover request.
     */
    public void setFailoverRequest() {
        this.isFailoverRequest = true;
    }

    /**
     * Resets AI configuration tracing flags.
     */
    public void resetAiConfigurationTracing() {
        this.usesAiConfiguration = false;
        this.usesAiccConfiguration = false;
    }

    /**
     * Updates AI configuration tracing based on content type.
     * @param contentType the content type to analyze
     */
    public void updateAiConfigurationTracing(String contentType) {
        if (contentType == null || usesAiccConfiguration) {
            return;
        }

        if (!JsonConfigurationParser.isJsonContentType(contentType)) {
            return;
        }

        String profileValue = extractProfileParameter(contentType);
        if (profileValue == null) {
            return;
        }

        if (profileValue.contains(APP_CONFIG_AI_MIME_PROFILE)) {
            usesAiConfiguration = true;
            if (profileValue.contains(APP_CONFIG_AICC_MIME_PROFILE)) {
                usesAiccConfiguration = true;
            }
        }
    }

    /**
     * Extracts the value of the "profile" parameter from a content type string.
     * @param contentType the full content type string (e.g. "application/json; profile=\"https://...\"")
     * @return the profile parameter value, or null if not present
     */
    private static String extractProfileParameter(String contentType) {
        String[] parts = contentType.split(";");
        for (int i = 1; i < parts.length; i++) {
            String param = parts[i].strip();
            if (param.toLowerCase().startsWith("profile=")) {
                String value = param.substring("profile=".length()).strip();
                // Remove surrounding quotes if present
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }

    String getValue(boolean watchRequests, boolean pushRefresh, FeatureFlagTracing featureFlagTracing) {
        if (featureFlagTracing != null) {
            this.featureFlagTracing = featureFlagTracing;
        }
        String track = configuration.get(RequestTracingConstants.REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE.toString());
        if (track != null && Boolean.valueOf(track)) {
            return "";
        }

        RequestType requestTypeValue = watchRequests ? RequestType.WATCH : RequestType.STARTUP;
        StringBuilder sb = new StringBuilder();

        // Key-value pairs
        sb.append(RequestTracingConstants.REQUEST_TYPE_KEY).append("=").append(requestTypeValue);

        if (replicaCount > 0) {
            sb.append(",").append(RequestTracingConstants.REPLICA_COUNT).append("=").append(replicaCount);
        }

        String hostType = getHostType();
        if (!hostType.isEmpty()) {
            sb.append(",").append(RequestTracingConstants.HOST_TYPE_KEY).append("=").append(hostType);
        }

        if (this.featureFlagTracing.usesAnyFilter()) {
            sb.append(",").append(RequestTracingConstants.FILTER_KEY).append("=").append(this.featureFlagTracing.toString());
        }

        // Max variants
        if (this.featureFlagTracing.getMaxVariants() != null && this.featureFlagTracing.getMaxVariants() > 0) {
            sb.append(",").append(RequestTracingConstants.MAX_VARIANTS_KEY).append("=").append(this.featureFlagTracing.getMaxVariants());
        }

        // FFFeatures
        String ffFeatures = this.featureFlagTracing.createFFFeaturesString();
        if (StringUtils.hasText(ffFeatures)) {
            sb.append(",").append(RequestTracingConstants.FF_FEATURES_KEY).append("=").append(ffFeatures);
        }

        // Features
        String features = createFeaturesString();
        if (StringUtils.hasText(features)) {
            sb.append(",").append(RequestTracingConstants.FEATURES_KEY).append("=").append(features);
        }

        // Feature management version
        sb = getFeatureManagementUsage(sb);

        // Tags
        if (isKeyVaultConfigured) {
            sb.append(",").append(KEY_VAULT_CONFIGURED_TRACING);
        }

        if (pushRefresh) {
            sb.append(",").append(PUSH_REFRESH);
        }

        if (isFailoverRequest) {
            sb.append(",").append(RequestTracingConstants.FAILOVER_TAG);
            isFailoverRequest = false;
        }

        return sb.toString();
    }

    /**
     * Creates the Features string for correlation context.
     * @return Features string with load balancing, AI, and snapshot reference tags
     */
    private String createFeaturesString() {
        StringBuilder sb = new StringBuilder();
        if (usesLoadBalancing) {
            sb.append(LOAD_BALANCING_FEATURE);
        }
        if (usesAiConfiguration) {
            if (sb.length() > 0) {
                sb.append(DELIMITER);
            }
            sb.append(AI_CONFIGURATION_FEATURE);
        }
        if (usesAiccConfiguration) {
            if (sb.length() > 0) {
                sb.append(DELIMITER);
            }
            sb.append(AI_CHAT_COMPLETION_FEATURE);
        }
        return sb.toString();
    }

    /**
     * Gets the current host machines type; Azure Function, Azure Web App, Kubernetes, or Empty.
     *
     * @return String of Host Type
     */
    private static String getHostType() {
        HostType hostType = HostType.UNIDENTIFIED;

        if (System.getenv(RequestTracingConstants.AZURE_FUNCTIONS_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.AZURE_FUNCTION;
        } else if (System.getenv(RequestTracingConstants.AZURE_WEB_APP_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.AZURE_WEB_APP;
        } else if (System.getenv(RequestTracingConstants.KUBERNETES_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.KUBERNETES;
        } else if (System.getenv(RequestTracingConstants.CONTAINER_APP_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.CONTAINER_APP;
        } else if (System.getenv(RequestTracingConstants.SERVICE_FABRIC_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.SERVICE_FABRIC;
        }

        return hostType.toString();
    }

    private static StringBuilder getFeatureManagementUsage(StringBuilder sb) {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Package ff = loader.getDefinedPackage("com.azure.spring.cloud.feature.management.models");
        if (ff != null && StringUtils.hasText(ff.getImplementationVersion())) {
            sb.append(",").append(RequestTracingConstants.FM_SPRING_VER_KEY).append("=").append(ff.getImplementationVersion());
        }
        return sb;
    }

}
