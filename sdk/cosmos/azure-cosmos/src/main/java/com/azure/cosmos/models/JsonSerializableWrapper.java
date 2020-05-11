// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.JsonSerializable;

/**
 * Represents a base class that contains a {@link JsonSerializable}.
 */
public abstract class JsonSerializableWrapper {

    JsonSerializable jsonSerializable;

    /**
     * Gets the jsonSerializable.
     *
     * @return {@link JsonSerializable}.
     */
    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }
}
