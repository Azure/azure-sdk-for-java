// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.CosmosRequestOptions;

public interface OverridableRequestOptions extends ReadOnlyRequestOptions {
    void override(CosmosRequestOptions cosmosRequestOptions);

    default <T> T overrideOption(T source, T target) {
        if (source != null) {
            target = source;
        }
        return target;
    }
}
