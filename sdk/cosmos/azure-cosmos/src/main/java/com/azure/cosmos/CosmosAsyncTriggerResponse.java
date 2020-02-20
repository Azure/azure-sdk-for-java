// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.Trigger;
import org.apache.commons.lang3.StringUtils;

public class CosmosAsyncTriggerResponse extends CosmosResponse<CosmosTriggerProperties> {

    private final CosmosTriggerProperties cosmosTriggerProperties;
    private final CosmosAsyncTrigger cosmosTrigger;

    CosmosAsyncTriggerResponse(ResourceResponse<Trigger> response, CosmosAsyncContainer container) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            cosmosTriggerProperties = null;
            cosmosTrigger = null;
        } else {
            cosmosTriggerProperties = new CosmosTriggerProperties(bodyAsString);
            cosmosTrigger = new CosmosAsyncTrigger(cosmosTriggerProperties.getId(), container);
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

    /**
     * Gets the cosmos trigger object or null
     *
     * @return {@link CosmosAsyncTrigger}
     */
    public CosmosAsyncTrigger getTrigger() {
        return cosmosTrigger;
    }
}
