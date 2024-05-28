package com.azure.cosmos;

import com.azure.cosmos.models.CosmosRequestDetails;

@FunctionalInterface
public interface CosmosRequestPolicy {
    void process(CosmosRequestDetails requestDetails);
}
