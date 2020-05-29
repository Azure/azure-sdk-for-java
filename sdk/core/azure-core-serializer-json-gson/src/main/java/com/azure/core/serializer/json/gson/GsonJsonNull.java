// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNull;

/**
 * GSON specific implementation of {@link JsonNull}.
 */
public final class GsonJsonNull implements JsonNull {
    private final com.google.gson.JsonNull jsonNull;

    public GsonJsonNull() {
        this.jsonNull = com.google.gson.JsonNull.INSTANCE;
    }

    public GsonJsonNull(com.google.gson.JsonNull jsonNull) {
        this.jsonNull = jsonNull;
    }

    com.google.gson.JsonNull getJsonNull() {
        return jsonNull;
    }
}
