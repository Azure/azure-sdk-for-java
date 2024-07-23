// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosConnectionDetails;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

@DisabledIfEnvironmentVariable(named = "SPRING_CLOUD_AZURE_TEST_SUPPORTED_SPRING_BOOT_VERSION", matches = "3.0.")
public class CustomAzureCosmosConnectionDetails implements AzureCosmosConnectionDetails {

    @Override
    public String getEndpoint() {
        return "test-endpoint-by-connection-detail";
    }

    @Override
    public String getKey() {
        return "cosmos-key-by-connection-detail";
    }

    @Override
    public String getDatabase() {
        return "test-database-by-connection-detail";
    }

    @Override
    public Boolean getEndpointDiscoveryEnabled() {
        return true;
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return ConnectionMode.GATEWAY;
    }
}
