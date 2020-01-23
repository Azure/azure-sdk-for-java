// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.Utils;
import org.apache.commons.lang3.StringUtils;

public class CosmosAsyncItemResponse<T> extends CosmosResponse<CosmosItemProperties> {
    private final Class<T> itemClassType;
    private final String responseBodyString;
    
    CosmosAsyncItemResponse(ResourceResponse<Document> response, Class<T> klass) {
        super(response);
        this.itemClassType = klass;
        responseBodyString = response.getBodyAsString();
        if (StringUtils.isEmpty(responseBodyString)){
            super.setProperties(null);
        } else {
            CosmosItemProperties props = new CosmosItemProperties(responseBodyString);
            super.setProperties(props);
        }
    }

    /**
     * Gets the resource .
     *
     * @return the resource
     */
    public T getResource(){
        return Utils.parse(responseBodyString, itemClassType);
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
