// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ResourceResponse;
import org.apache.commons.lang3.StringUtils;

public class CosmosAsyncItemResponse<T> extends CosmosResponse<CosmosItemProperties> {
    private final Class<T> itemClassType;
    
    CosmosAsyncItemResponse(ResourceResponse<Document> response, Class<T> klass) {
        super(response);
        this.itemClassType = klass;
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)){
            super.setProperties(null);
        } else {
            CosmosItemProperties props = new CosmosItemProperties(bodyAsString);
            super.setProperties(props);
        }
    }

    /**
     * Gets the resource .
     *
     * @return the resource
     */
    public T getResource(){
        return super.getProperties().toObject(itemClassType);
    }

    /**
     * Gets the itemProperties
     *
     * @return the itemProperties
     */
    public CosmosItemProperties getProperties() {
        return super.getProperties();
    }
    
}
