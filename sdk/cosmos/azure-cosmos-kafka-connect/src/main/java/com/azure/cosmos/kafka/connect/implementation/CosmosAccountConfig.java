// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import java.util.List;

public class CosmosAccountConfig {
    private final String endpoint;
    private final String accountKey;
    private final String applicationName;
    private final boolean useGatewayMode;
    private final List<String> preferredRegionsList;

    public CosmosAccountConfig(
        String endpoint,
        String accountKey,
        String applicationName,
        boolean useGatewayMode,
        List<String> preferredRegionsList) {
        this.endpoint = endpoint;
        this.accountKey = accountKey;
        this.applicationName = applicationName;
        this.useGatewayMode = useGatewayMode;
        this.preferredRegionsList = preferredRegionsList;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public boolean isUseGatewayMode() {
        return useGatewayMode;
    }

    public List<String> getPreferredRegionsList() {
        return preferredRegionsList;
    }
}
