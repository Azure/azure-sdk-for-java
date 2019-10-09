// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.common.jsonwrapper.jacksonwrapper;

import com.azure.search.common.jsonwrapper.api.JsonApi;
import com.azure.search.common.jsonwrapper.spi.JsonPlugin;

public class JacksonPlugin implements JsonPlugin {
    @Override
    public Class<? extends JsonApi> getType() {
        return JacksonDeserializer.class;
    }

    @Override
    public JsonApi newInstance() {
        return new JacksonDeserializer();
    }
}
