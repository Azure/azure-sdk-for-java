// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.User;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The type Cosmos user response. Contains methods to get properties
 */
public class CosmosUserResponse extends CosmosResponse<CosmosUserProperties> {

    CosmosUserResponse(ResourceResponse<User> response) {
        super(response);
        ObjectNode bodyAsJson = (ObjectNode)response.getBody();
        if (bodyAsJson == null) {
            super.setProperties(null);
        } else {
            CosmosUserProperties props = new CosmosUserProperties(bodyAsJson);
            super.setProperties(props);
        }
    }

    /**
     * Gets the cosmos user properties
     *
     * @return {@link CosmosUserProperties}
     */
    public CosmosUserProperties getProperties() {
        return super.getProperties();
    }
}
