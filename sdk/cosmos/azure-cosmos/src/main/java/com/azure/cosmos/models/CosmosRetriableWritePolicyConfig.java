// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public class CosmosRetriableWritePolicyConfig {
    public static CosmosRetriableWritePolicyConfig DEFAULT =
        new CosmosRetriableWritePolicyConfigBuilder().build();

    public static CosmosRetriableWritePolicyConfig DISABLED =
        new CosmosRetriableWritePolicyConfigBuilder(false).build();
}
