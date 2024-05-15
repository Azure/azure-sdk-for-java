// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class CosmosAadAuthConfig implements CosmosAuthConfig {
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final CosmosAzureEnvironment azureEnvironment;

    public CosmosAadAuthConfig(String clientId, String clientSecret, String tenantId, CosmosAzureEnvironment azureEnvironment) {
        checkArgument(StringUtils.isNotEmpty(clientId), "Argument 'clientId' should not be null");
        checkArgument(StringUtils.isNotEmpty(clientSecret), "Argument 'clientSecret' should not be null");
        checkArgument(StringUtils.isNotEmpty(tenantId), "Argument 'tenantId' should not be null");

        this.clientId = clientId;
        this.clientSecret = clientSecret;
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

    public CosmosAzureEnvironment getAzureEnvironment() {
        return azureEnvironment;
    }
}
