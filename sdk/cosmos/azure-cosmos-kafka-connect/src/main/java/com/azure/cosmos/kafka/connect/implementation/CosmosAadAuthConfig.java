// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class CosmosAadAuthConfig implements CosmosAuthConfig {
    private static final Map<CosmosAzureEnvironment, String> ACTIVE_DIRECTORY_ENDPOINT_MAP;
    static {
        // for now we maintain a static list within the SDK these values do not change very frequently
        ACTIVE_DIRECTORY_ENDPOINT_MAP = new HashMap<>();
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironment.AZURE, "https://login.microsoftonline.com/");
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironment.AZURE_CHINA, "https://login.chinacloudapi.cn/");
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironment.AZURE_US_GOVERNMENT, "https://login.microsoftonline.us/");
        ACTIVE_DIRECTORY_ENDPOINT_MAP.put(CosmosAzureEnvironment.AZURE_GERMANY, "https://login.microsoftonline.de/");
    }

    private final String clientId;
    private final String clientSecret;
    private final String authEndpointOverride;
    private final String tenantId;
    private final CosmosAzureEnvironment azureEnvironment;

    public CosmosAadAuthConfig(
        String clientId,
        String clientSecret,
        String authEndpointOverride,
        String tenantId,
        CosmosAzureEnvironment azureEnvironment) {
        checkArgument(StringUtils.isNotEmpty(clientId), "Argument 'clientId' should not be null");
        checkArgument(StringUtils.isNotEmpty(clientSecret), "Argument 'clientSecret' should not be null");
        checkArgument(StringUtils.isNotEmpty(tenantId), "Argument 'tenantId' should not be null");

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authEndpointOverride = authEndpointOverride;
        this.tenantId = tenantId;
        this.azureEnvironment = azureEnvironment;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAuthEndpoint() {
        String defaultAuthEndpoint = ACTIVE_DIRECTORY_ENDPOINT_MAP.get(azureEnvironment);
        String authEndpoint = StringUtils.isNotEmpty(authEndpointOverride) ? authEndpointOverride : defaultAuthEndpoint;
        return authEndpoint.replaceAll("/$", "") + "/";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CosmosAadAuthConfig that = (CosmosAadAuthConfig) o;
        return Objects.equals(clientId, that.clientId)
            && Objects.equals(clientSecret, that.clientSecret)
            && Objects.equals(authEndpointOverride, that.authEndpointOverride)
            && Objects.equals(tenantId, that.tenantId)
            && azureEnvironment == that.azureEnvironment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, clientSecret, authEndpointOverride, tenantId, azureEnvironment);
    }
}
