package com.azure.cosmos.implementation;

import com.azure.cosmos.models.CosmosCommonRequestOptions;
import com.azure.cosmos.models.ICosmosCommonRequestOptions;

public interface OverridableRequestOptions extends ICosmosCommonRequestOptions {
    void override(CosmosCommonRequestOptions cosmosCommonRequestOptions);

    default <T> T overrideOption(T source, T target) {
        if (source != null) {
            target = source;
        }
        return target;
    }
}
