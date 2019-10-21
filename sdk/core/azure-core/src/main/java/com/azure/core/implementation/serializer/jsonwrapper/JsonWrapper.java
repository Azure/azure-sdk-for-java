// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper;

import com.azure.core.implementation.serializer.jsonwrapper.api.Config;
import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.implementation.serializer.jsonwrapper.spi.JsonPlugin;

import java.util.Iterator;
import java.util.ServiceLoader;

@SuppressWarnings("unchecked")
public class JsonWrapper {

    private static final ServiceLoader<JsonPlugin> PLUGIN_LOADER = ServiceLoader.load(JsonPlugin.class);

    /**
     * Create new instance of JsonApi
     *
     * @return JsonApi
     */
    @SuppressWarnings("unchecked")
    public static JsonApi newInstance() {

        JsonApi jsonApi = newInstance((Class) null);
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonApi.configureTimezone();

        return jsonApi;
    }

    /**
     * Create new instance of JsonApi
     *
     * @param type type of instance to create
     * @return JsonAPI instance
     * @throws ClassNotFoundException if the class type is invalid
     */
    @SuppressWarnings("unchecked")
    public static JsonApi newInstance(String type) throws ClassNotFoundException {
        Class<? extends JsonApi> cls = (Class<? extends JsonApi>) Class.forName(type);
        return newInstance(cls);
    }

    /**
     * Create new instance of JsonApi
     *
     * @param type type of JsonApi
     * @return JsonApi instance
     */
    @SuppressWarnings("unchecked")
    public static JsonApi newInstance(Class<? extends JsonApi> type) {
        Iterator<JsonPlugin> it = PLUGIN_LOADER.iterator();
        while (it.hasNext()) {
            JsonPlugin plugin = it.next();
            if (type == null || plugin.getType().equals(type)) {
                JsonApi jsonApi = plugin.newInstance();
                jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
                jsonApi.configureTimezone();

                return jsonApi;
            }
        }
        return null;
    }
}
