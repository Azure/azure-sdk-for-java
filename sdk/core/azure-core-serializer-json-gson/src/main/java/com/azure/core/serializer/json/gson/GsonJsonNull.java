// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.JsonNull;

import java.util.Objects;

/**
 * GSON specific implementation of {@link JsonNull}.
 */
public final class GsonJsonNull implements JsonNull {
    private final com.google.gson.JsonNull jsonNull;

    /**
     * Constructs a {@link JsonNull} backed by GSON {@link com.google.gson.JsonNull#INSTANCE}
     */
    public GsonJsonNull() {
        this.jsonNull = com.google.gson.JsonNull.INSTANCE;
    }

    /**
     * Constructs a {@link JsonNull} backed by the passed GSON {@link com.google.gson.JsonNull}.
     *
     * @param jsonNull The backing GSON {@link com.google.gson.JsonNull}.
     */
    public GsonJsonNull(com.google.gson.JsonNull jsonNull) {
        this.jsonNull = jsonNull;
    }

    com.google.gson.JsonNull getJsonNull() {
        return jsonNull;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof GsonJsonNull)) {
            return false;
        }

        return Objects.equals(jsonNull, ((GsonJsonNull) obj).jsonNull);
    }

    @Override
    public int hashCode() {
        return jsonNull.hashCode();
    }
}
