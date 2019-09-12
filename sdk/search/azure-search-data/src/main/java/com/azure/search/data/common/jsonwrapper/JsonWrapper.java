// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.common.jsonwrapper;

import com.azure.search.data.common.jsonwrapper.api.Config;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.spi.JsonPlugin;

import java.util.Iterator;
import java.util.ServiceLoader;

public class JsonWrapper {

    private static ServiceLoader<JsonPlugin> pluginLoader = ServiceLoader.load(JsonPlugin.class);

    /**
     * Create new instance of JsonApi
     * @return JsonApi
     */
    public static JsonApi newInstance() {

        JsonApi jsonApi = newInstance((Class) null);
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonApi.configureTimezone();

        return jsonApi;
    }

    /**
     * Create new instance of JsonApi
     * @param type type of instance to create
     * @return JsonAPI instance
     */
    public static JsonApi newInstance(String type) {
        try {
            Class<? extends JsonApi> cls = (Class<? extends JsonApi>) Class.forName(type);
            JsonApi jsonApi = newInstance(cls);
            jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jsonApi.configureTimezone();

            return jsonApi;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create new instance of JsonApi
     * @param type type of JsonApi
     * @return JsonApi instance
     */
    public static JsonApi newInstance(Class<? extends JsonApi> type) {
        Iterator<JsonPlugin> it = pluginLoader.iterator();
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
