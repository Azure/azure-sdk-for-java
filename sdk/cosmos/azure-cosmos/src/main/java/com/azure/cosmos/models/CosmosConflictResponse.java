// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The type Cosmos conflict response.
 */
public class CosmosConflictResponse extends CosmosResponse<CosmosConflictProperties> {

    CosmosConflictResponse(ResourceResponse<Conflict> response) {
        super(response);
        ObjectNode bodyAsJson = (ObjectNode)response.getBody();
        if (bodyAsJson == null) {
            super.setProperties(null);
        } else {
            CosmosConflictProperties props = new CosmosConflictProperties(bodyAsJson);
            super.setProperties(props);
        }
    }

    /**
     * Get conflict properties object representing the resource on the server
     *
     * @return the conflict properties
     */
    public CosmosConflictProperties getProperties() {
        return super.getProperties();
    }
}
