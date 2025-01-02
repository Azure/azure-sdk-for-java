// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;

import java.io.IOException;

/**
 * Functional interface that aids in simplifying deserialization.
 *
 * @param <T> The type being deserialized.
 */
public interface ReadValue<T> {
    /**
     * Reads a value from the {@link JsonReader} into the {@code object}.
     *
     * @param reader The {@link JsonReader} being read.
     * @param fieldName The current field name.
     * @param object The current object being deserialized.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    void read(JsonReader reader, String fieldName, T object) throws IOException;
}
