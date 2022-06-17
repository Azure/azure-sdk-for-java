// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.accesshelpers;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.models.CosmosItemResponse;

/**
 * Helper class to access non-public APIs of {@link CosmosItemResponse}
 */
public final class CosmosItemResponseHelper {
    private static CosmosItemResponseAccessor accessor;

    /*
     * Since CosmosItemResponseAccessor has an API that will call the constructor of CosmosItemResponse it will
     * need to ensure that CosmosItemResponse has been loaded.
     */
    static {
        // Access the CosmosItemResponse class to ensure it has been loaded.
        try {
            Class<?> ensureLoaded = Class.forName(CosmosItemResponse.class.getName());
        } catch (ClassNotFoundException ex) {
            // This should never happen.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Type defining the methods that access non-public APIs of {@link CosmosItemResponse}.
     */
    public interface CosmosItemResponseAccessor {
        /**
         * Creates a new instance of {@link CosmosItemResponse}.
         *
         * @param response The {@link ResourceResponse} the item response is based on.
         * @param classType The response item class type.
         * @param itemDeserializer The item deserializer.
         * @param <T> The item class type.
         * @return A new instance of {@link CosmosItemResponse}.
         */
        <T> CosmosItemResponse<T> createCosmosAsyncItemResponse(ResourceResponse<Document> response,
            Class<T> classType, ItemDeserializer itemDeserializer);

        /**
         * Creates a new instance of {@link CosmosItemResponse}.
         *
         * @param response The {@link ResourceResponse} the item response is based on.
         * @param contentAsByteArray The response content as a byte array.
         * @param classType The response item class type.
         * @param itemDeserializer The item deserializer.
         * @param <T> The item class type.
         * @return A new instance of {@link CosmosItemResponse}.
         */
        <T> CosmosItemResponse<T> createCosmosItemResponse(ResourceResponse<Document> response,
            byte[] contentAsByteArray, Class<T> classType, ItemDeserializer itemDeserializer);

        /**
         * Gets the internal object node of the item response.
         *
         * @param cosmosItemResponse The item response.
         * @param <T> The item class type.
         * @return Internal object node of the item response.
         */
        <T> InternalObjectNode getInternalObjectNode(CosmosItemResponse<T> cosmosItemResponse);

        /**
         * Gets the payload length of the item response.
         *
         * @param cosmosItemResponse The item response.
         * @param <T> The item class type.
         * @return Payload length of the item response.
         */
        <T> int getPayloadLength(CosmosItemResponse<T> cosmosItemResponse);

        /**
         * Gets the content byte array of a {@link CosmosItemResponse}.
         *
         * @param response The {@link CosmosItemResponse}.
         * @return The content byte array.
         */
        byte[] getByteArrayContent(CosmosItemResponse<byte[]> response);

        /**
         * Sets the content byte array of a {@link CosmosItemResponse}.
         *
         * @param response The {@link CosmosItemResponse}.
         * @param content The content byte array.
         */
        void setByteArrayContent(CosmosItemResponse<byte[]> response, byte[] content);

        /**
         * Gets the {@link ResourceResponse} the {@link CosmosItemResponse} is based on.
         *
         * @param response The {@link CosmosItemResponse}.
         * @return The {@link ResourceResponse} the {@link CosmosItemResponse} is based on.
         */
        ResourceResponse<Document> getResourceResponse(CosmosItemResponse<byte[]> response);
    }

    /**
     * The method called from {@link CosmosItemResponse} to set its accessor.
     *
     * @param cosmosItemResponseAccessor The accessor.
     */
    public static void setAccessor(final CosmosItemResponseAccessor cosmosItemResponseAccessor) {
        accessor = cosmosItemResponseAccessor;
    }

    /**
     * Creates a new instance of {@link CosmosItemResponse}.
     *
     * @param response The {@link ResourceResponse} the conflict response is based on.
     * @return A new instance of {@link CosmosItemResponse}.
     */
    public static CosmosItemResponse<Object> createCosmosAsyncItemResponseWithObjectType(
        ResourceResponse<Document> response) {
        return createCosmosAsyncItemResponse(response, Object.class, null);
    }

    /**
     * Creates a new instance of {@link CosmosItemResponse}.
     *
     * @param response The {@link ResourceResponse} the item response is based on.
     * @param classType The response item class type.
     * @param itemDeserializer The item deserializer.
     * @param <T> The item class type.
     * @return A new instance of {@link CosmosItemResponse}.
     */
    public static <T> CosmosItemResponse<T> createCosmosAsyncItemResponse(ResourceResponse<Document> response,
        Class<T> classType, ItemDeserializer itemDeserializer) {
        return accessor.createCosmosAsyncItemResponse(response, classType, itemDeserializer);
    }

    /**
     * Creates a new instance of {@link CosmosItemResponse}.
     *
     * @param response The {@link ResourceResponse} the item response is based on.
     * @param contentAsByteArray The response content as a byte array.
     * @param classType The response item class type.
     * @param itemDeserializer The item deserializer.
     * @param <T> The item class type.
     * @return A new instance of {@link CosmosItemResponse}.
     */
    public static <T> CosmosItemResponse<T> createCosmosItemResponse(ResourceResponse<Document> response,
        byte[] contentAsByteArray, Class<T> classType, ItemDeserializer itemDeserializer) {
        return accessor.createCosmosItemResponse(response, contentAsByteArray, classType, itemDeserializer);
    }

    /**
     * Gets the internal object node of the item response.
     *
     * @param cosmosItemResponse The item response.
     * @param <T> The item class type.
     * @return Internal object node of the item response.
     */
    public static <T> InternalObjectNode getInternalObjectNode(CosmosItemResponse<T> cosmosItemResponse) {
        return accessor.getInternalObjectNode(cosmosItemResponse);
    }

    /**
     * Gets the payload length of the item response.
     *
     * @param cosmosItemResponse The item response.
     * @param <T> The item class type.
     * @return Payload length of the item response.
     */
    public static <T> int getPayloadLength(CosmosItemResponse<T> cosmosItemResponse) {
        return accessor.getPayloadLength(cosmosItemResponse);
    }

    /**
     * Gets the content byte array of a {@link CosmosItemResponse}.
     *
     * @param response The {@link CosmosItemResponse}.
     * @return The content byte array.
     */
    public static byte[] getByteArrayContent(CosmosItemResponse<byte[]> response) {
        return accessor.getByteArrayContent(response);
    }

    /**
     * Sets the content byte array of a {@link CosmosItemResponse}.
     *
     * @param response The {@link CosmosItemResponse}.
     * @param content The content byte array.
     */
    public static void setByteArrayContent(CosmosItemResponse<byte[]> response, byte[] content) {
        accessor.setByteArrayContent(response, content);
    }

    /**
     * Gets the {@link ResourceResponse} the {@link CosmosItemResponse} is based on.
     *
     * @param response The {@link CosmosItemResponse}.
     * @return The {@link ResourceResponse} the {@link CosmosItemResponse} is based on.
     */
    public static ResourceResponse<Document> getResourceResponse(CosmosItemResponse<byte[]> response) {
        return accessor.getResourceResponse(response);
    }
}
