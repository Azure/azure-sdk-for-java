package com.azure.cosmos.models;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;

public final class CosmosCommonRequestOptions implements ICosmosCommonRequestOptions {

    private CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyConfig;

    public CosmosCommonRequestOptions setCosmosEndToEndLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyConfig = endToEndOperationLatencyPolicyConfig;
        return this;
    }

    @Override
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig(){
        return this.endToEndOperationLatencyConfig;
    }
    // Same for the other configs
}
