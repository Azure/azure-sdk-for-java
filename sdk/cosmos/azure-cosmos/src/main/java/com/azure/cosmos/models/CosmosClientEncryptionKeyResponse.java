// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ClientEncryptionKey;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.util.Beta;

/**
 * The type Cosmos client encryption key response.
 */
@Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosClientEncryptionKeyResponse extends CosmosResponse<CosmosClientEncryptionKeyProperties>  {
    CosmosClientEncryptionKeyResponse(ResourceResponse<ClientEncryptionKey> response) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
        } else {
            CosmosClientEncryptionKeyProperties props = new CosmosClientEncryptionKeyProperties(bodyAsString);
            super.setProperties(props);
        }
    }

    /**
     * Gets the cosmos client encryption key properties
     *
     * @return {@link CosmosUserProperties}
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosClientEncryptionKeyProperties getProperties() {
        return super.getProperties();
    }
}
