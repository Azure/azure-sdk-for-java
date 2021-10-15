// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.models.JsonPatchDocument;

import java.util.List;

/**
 * Helper class to access private values of {@link JsonPatchDocument} across package boundaries.
 */
public final class JsonPatchDocumentHelper {
    private static JsonPatchDocumentAccessor accessor;

    private JsonPatchDocumentHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link JsonPatchDocument} instance.
     */
    public interface JsonPatchDocumentAccessor {
        List<JsonPatchOperation> getOperations(JsonPatchDocument jsonPatchDocument);
    }

    /**
     * The method called from {@link JsonPatchDocument} to set it's accessor.
     *
     * @param jsonPatchDocumentAccessor The accessor.
     */
    public static void setAccessor(final JsonPatchDocumentAccessor jsonPatchDocumentAccessor) {
        accessor = jsonPatchDocumentAccessor;
    }

    public static List<JsonPatchOperation> getOperations(JsonPatchDocument jsonPatchDocument) {
        return accessor.getOperations(jsonPatchDocument);
    }
}
