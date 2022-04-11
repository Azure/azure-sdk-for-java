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

    private JsonPatchDocumentHelper() {
    }

    /**
     * Interface defining the methods to access non-public properties of a {@link JsonPatchDocument} instance.
     */
    public interface JsonPatchDocumentAccessor {
        /**
         * Gets a representation of the {@link JsonPatchOperation JSON patch operations} in this JSON patch document.
         * <p>
         * Modifications to the returned list won't mutate the operations in the document.
         *
         * @param document The document to retrieve its operations.
         * @return The JSON patch operations in this JSON patch document.
         */
        List<JsonPatchOperation> getOperations(JsonPatchDocument document);
    }

    /**
     * The method called from {@link JsonPatchDocument} to set its accessor.
     *
     * @param jsonPatchDocumentAccessor The accessor.
     */
    public static void setAccessor(final JsonPatchDocumentAccessor jsonPatchDocumentAccessor) {
        accessor = jsonPatchDocumentAccessor;
    }

    /**
     * Gets a representation of the {@link JsonPatchOperation JSON patch operations} in this JSON patch document.
     * <p>
     * Modifications to the returned list won't mutate the operations in the document.
     *
     * @param document The document to retrieve its operations.
     * @return The JSON patch operations in this JSON patch document.
     */
    public static List<JsonPatchOperation> getOperations(JsonPatchDocument document) {
        return accessor.getOperations(document);
    }
}
