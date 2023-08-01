// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.Trigger;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

/**
 * The type Cosmos trigger response.
 */
public class CosmosTriggerResponse extends CosmosResponse<CosmosTriggerProperties> {

    private final CosmosTriggerProperties cosmosTriggerProperties;

    CosmosTriggerResponse(ResourceResponse<Trigger> response) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            cosmosTriggerProperties = null;
        } else {
            cosmosTriggerProperties = new CosmosTriggerProperties(bodyAsString);
        }
    }

    /**
     * Gets the cosmos trigger properties or null
     *
     * @return {@link CosmosTriggerProperties}
     */
    public CosmosTriggerProperties getProperties() {
        return cosmosTriggerProperties;
    }
}
