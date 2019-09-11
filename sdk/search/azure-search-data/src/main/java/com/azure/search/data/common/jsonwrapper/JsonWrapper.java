// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.common.jsonwrapper;

import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.spi.JsonPlugin;

import java.util.Iterator;
import java.util.ServiceLoader;

public class JsonWrapper {

    private static final ServiceLoader<JsonPlugin> PLUGIN_LOADER = ServiceLoader.load(JsonPlugin.class);

    /**
     * Create new instance of JsonApi
     * @return JsonApi
     */
    public static JsonApi newInstance() {
        return newInstance((Class) null);
    }

    /**
     * Create new instance of JsonApi
     * @param type type of instance to create
     * @return JsonAPI instance
     */
    public static JsonApi newInstance(String type) {
        try {
            Class<? extends JsonApi> cls = (Class<? extends JsonApi>) Class.forName(type);
            return newInstance(cls);
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
        Iterator<JsonPlugin> it = PLUGIN_LOADER.iterator();
        while (it.hasNext()) {
            JsonPlugin plugin = it.next();
            if (type == null) {
                return plugin.newInstance();
            }
            if (plugin.getType().equals(type)) {
                return plugin.newInstance();
            }
        }
        return null;
    }
}
