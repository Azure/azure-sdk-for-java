// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;

import java.util.LinkedHashMap;

/**
 * Represents a document
 * A property bag is used for scenarios where the index schema is only known at run-time
 * If the schema is known, user can convert the properties to a specific object type using the as method
 */
public class Document extends LinkedHashMap<String, Object> {

    /**
     * If the document schema is known, user can convert the properties to a specific object type
     * @param cls Class type of the document object to convert to
     * @param <T> type
     * @return an object of the request type
     */
    public <T> T as(Class<T> cls) {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        return jsonApi.convertObjectToType(this, cls);
    }
}
