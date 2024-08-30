// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEV_ENV_TRACING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.KEY_VAULT_CONFIGURED_TRACING;

import org.springframework.util.StringUtils;

import com.azure.core.util.Configuration;
import com.azure.spring.cloud.appconfiguration.config.implementation.HostType;
import com.azure.spring.cloud.appconfiguration.config.implementation.RequestTracingConstants;
import com.azure.spring.cloud.appconfiguration.config.implementation.RequestType;

public class TracingInfo {

    private boolean isDev = false;

    private boolean isKeyVaultConfigured = false;

    private int replicaCount;

    private final FeatureFlagTracing featureFlagTracing;

    private final Configuration configuration;

    public TracingInfo(boolean isDev, boolean isKeyVaultConfigured, int replicaCount, Configuration configuration) {
        this.isDev = isDev;
        this.isKeyVaultConfigured = isKeyVaultConfigured;
        this.replicaCount = replicaCount;
        this.featureFlagTracing = new FeatureFlagTracing();
        this.configuration = configuration;
    }

    public String getValue(boolean watchRequests) {
        String track = configuration
            .get(RequestTracingConstants.REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE.toString());
        if (track != null && Boolean.valueOf(track)) {
            return "";
        }

        RequestType requestTypeValue = watchRequests ? RequestType.WATCH : RequestType.STARTUP;
        StringBuilder sb = new StringBuilder();

        sb.append(RequestTracingConstants.REQUEST_TYPE_KEY).append("=" + requestTypeValue);

        if (featureFlagTracing != null && featureFlagTracing.usesAnyFilter()) {
            sb.append(",Filter=").append(featureFlagTracing.toString());
        }

        String hostType = getHostType();
        if (!hostType.isEmpty()) {
            sb.append(",").append(RequestTracingConstants.HOST_TYPE_KEY).append("=").append(hostType);
        }

        if (isDev) {
            sb.append(",Env=").append(DEV_ENV_TRACING);
        }
        if (isKeyVaultConfigured) {
            sb.append(",").append(KEY_VAULT_CONFIGURED_TRACING);
        }

        if (replicaCount > 0) {
            sb.append(",").append(RequestTracingConstants.REPLICA_COUNT).append("=").append(replicaCount);
        }
        
        sb = getFeatureManagementUsage(sb);

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
            sb.append(",FMSpVer=").append(ff.getImplementationVersion());
        }
        return sb;
    }

    /**
     * @return the featureFlagTracing
     */
    public FeatureFlagTracing getFeatureFlagTracing() {
        return featureFlagTracing;
    }

}
