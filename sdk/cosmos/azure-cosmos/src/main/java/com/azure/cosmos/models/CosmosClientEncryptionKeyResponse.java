// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ClientEncryptionKey;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The type Cosmos client encryption key response.
 */
public final class CosmosClientEncryptionKeyResponse extends CosmosResponse<CosmosClientEncryptionKeyProperties>  {
    CosmosClientEncryptionKeyResponse(ResourceResponse<ClientEncryptionKey> response) {
        super(response);
        ObjectNode bodyAsJson = (ObjectNode)response.getBody();
        if (bodyAsJson == null) {
            super.setProperties(null);
        } else {
            CosmosClientEncryptionKeyProperties props = new CosmosClientEncryptionKeyProperties(bodyAsJson);
            super.setProperties(props);
        }
    }

    /**
     * Gets the cosmos client encryption key properties
     *
     * @return {@link CosmosUserProperties}
     */
    public CosmosClientEncryptionKeyProperties getProperties() {
        return super.getProperties();
    }
}
