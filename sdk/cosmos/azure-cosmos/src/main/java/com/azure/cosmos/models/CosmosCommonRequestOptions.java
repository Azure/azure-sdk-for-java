package com.azure.cosmos.models;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;

public class CosmosCommonRequestOptions implements ICosmosCommonRequestOptions {

    protected CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyConfig;

    public CosmosCommonRequestOptions setCosmosEndToEndLatencyPolicyConfig(CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        this.endToEndOperationLatencyConfig = endToEndOperationLatencyPolicyConfig;
        return this;
    }
    public CosmosEndToEndOperationLatencyPolicyConfig getCosmosEndToEndLatencyPolicyConfig(){
        return this.endToEndOperationLatencyConfig;
    }
}
