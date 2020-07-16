// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * The json serializer options.
 */
public class JsonOptions {
    private JsonInclusion jsonInclusion;

    /**
     * Gets the json inclusion value.
     * @return The enum field json inclusion.
     */
    public JsonInclusion getJsonInclusion() {
        return jsonInclusion;
    }

    /**
     * Sets the json inclusion value.
     * @param jsonInclusion The enum field json inclusion.
     * @return The {@link JsonOptions} object itself.
     */
    public JsonOptions setJsonInclusion(JsonInclusion jsonInclusion) {
        this.jsonInclusion = jsonInclusion;
        return this;
    }
}
