package com.azure.cosmos.models;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;

public interface ICosmosCommonRequestOptions {

    CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig();
}
