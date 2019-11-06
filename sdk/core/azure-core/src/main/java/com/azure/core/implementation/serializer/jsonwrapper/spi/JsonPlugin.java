// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper.spi;

import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;

/**
 * Interface to be implemented by all JSON libraries
 * that want to provide their services to the JSON Wrapper abstraction.
 */
public interface JsonPlugin {

    /**
     * The class type of the JSON Wrapper implementation
     * @return type of plugin
     */
    Class<? extends JsonApi> getType();

    /**
     * Returns a new instance of the {@link JsonApi} implementation.
     * @return JsonApi instance
     */
    JsonApi newInstance();
}
