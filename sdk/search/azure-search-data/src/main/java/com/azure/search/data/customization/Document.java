// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.Config;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.customization.models.GeoPointDeserializer;

import java.util.LinkedHashMap;

/**
 * Represents a document
 * A property bag is used for scenarios where the index schema is only known at run-time
 * If the schema is known, user can convert the properties to a specific object type using the as method
 */
public class Document extends LinkedHashMap<String, Object> {

    private JsonApi jsonApi;

    /**
     * Constructor
     */
    public Document() {
        jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonApi.configureTimezone();
        jsonApi.registerCustomDeserializer(new GeoPointDeserializer());
    }

    /**
     * If the document schema is known, user can convert the properties to a specific object type
     * @param cls Class type of the document object to convert to
     * @param <T> type
     * @return an object of the request type
     */
    public <T> T as(Class<T> cls) {
        return jsonApi.convertObjectToType(this, cls);
    }
}
