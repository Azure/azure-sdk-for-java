// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.accesshelpers.CosmosConflictResponseHelper;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

/**
 * The type Cosmos conflict response.
 */
public class CosmosConflictResponse extends CosmosResponse<CosmosConflictProperties> {

    CosmosConflictResponse(ResourceResponse<Conflict> response) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
        } else {
            CosmosConflictProperties props = new CosmosConflictProperties(bodyAsString);
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

    // Static initializer to set up the helper.
    static {
        CosmosConflictResponseHelper.setAccessor(new CosmosConflictResponseHelper.CosmosConflictResponseAccessor() {
            @Override
            public CosmosConflictResponse createCosmosConflictResponse(ResourceResponse<Conflict> response) {
                return new CosmosConflictResponse(response);
            }
        });
    }
}
