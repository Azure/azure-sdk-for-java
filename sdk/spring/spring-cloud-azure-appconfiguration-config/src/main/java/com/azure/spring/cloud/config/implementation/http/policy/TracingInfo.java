// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation.http.policy;

import static com.azure.spring.cloud.config.implementation.AppConfigurationConstants.DEV_ENV_TRACING;
import static com.azure.spring.cloud.config.implementation.AppConfigurationConstants.KEY_VAULT_CONFIGURED_TRACING;

import com.azure.spring.cloud.config.implementation.HostType;
import com.azure.spring.cloud.config.implementation.RequestTracingConstants;
import com.azure.spring.cloud.config.implementation.RequestType;

public class TracingInfo {

    private Boolean isDev = false;

    private Boolean isKeyVaultConfigured = false;

    private int replicaCount = 0;

    private FeatureFlagTracing featureFlagTracing;

    public TracingInfo(boolean isDev, boolean isKeyVaultConfigured, int replicaCount) {
        this.isDev = isDev;
        this.isKeyVaultConfigured = isKeyVaultConfigured;
        this.replicaCount = replicaCount;
        this.featureFlagTracing = new FeatureFlagTracing();
    }

    public String getValue(Boolean watchRequests) {
        String track = System.getenv(RequestTracingConstants.REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE.toString());
        if ("false".equalsIgnoreCase(track)) {
            return "";
        }

        RequestType requestTypeValue = watchRequests ? RequestType.WATCH : RequestType.STARTUP;
        StringBuilder sb = new StringBuilder();

        sb.append(RequestTracingConstants.REQUEST_TYPE_KEY + "=" + requestTypeValue);

        if (featureFlagTracing != null && featureFlagTracing.usesAnyFilter()) {
            sb.append(",Filter=").append(featureFlagTracing.toString());
        }

        String hostType = getHostType();
        if (!hostType.isEmpty()) {
            sb.append("," + RequestTracingConstants.HOST_TYPE_KEY + "=" + hostType);
        }

        if (isDev) {
            sb.append(",Env=" + DEV_ENV_TRACING);
        }
        if (isKeyVaultConfigured) {
            sb.append("," + KEY_VAULT_CONFIGURED_TRACING);
        }

        if (replicaCount > 0) {
            sb.append("," + RequestTracingConstants.REPLICA_COUNT + "=" + replicaCount);
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
        }

        return hostType.toString();
    }

    /**
     * @return the featureFlagTracing
     */
    public FeatureFlagTracing getFeatureFlagTracing() {
        return featureFlagTracing;
    }

}
