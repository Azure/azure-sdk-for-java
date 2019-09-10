// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class IndexingAction {
    private static JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);

    /**
     * Creates a new IndexAction for deleting a document from an index.
     *
     * @param document The document to be deleted from the index
     * @param <T>
     * @return A new IndexAction.
     */
    public static <T> IndexAction delete(T document){
        return new IndexAction()
            .actionType(IndexActionType.DELETE)
            .additionalProperties(convertToMapObject(document));
    }

    /**
     * Creates a new IndexAction for deleting a document in an index.
     *
     * @param keyName The name of the key field of the index.
     * @param keyValue The key of the document to delete.
     * @return A new IndexAction.
     */
    public static IndexAction delete(String keyName, Object keyValue){

        if (StringUtils.isEmpty(keyName)) {
            throw new IllegalArgumentException("Invalid keyName");
        }

        if (keyValue == null) {
            throw new IllegalArgumentException("Invalid keyValue");
        }

        Map<String, Object> document = new HashMap<>();
        document.put(keyName, keyValue);

        return new IndexAction()
            .actionType(IndexActionType.DELETE)
            .additionalProperties(document);
    }

    /**
     * Creates a new IndexAction for merging a document to an existing document in the index.
     *
     * @param document The document to merge. Set only the properties that you want to change.
     * @param <T>
     * @return A new IndexAction.
     */
    public static <T> IndexAction merge(T document){
        return new IndexAction()
            .actionType(IndexActionType.MERGE)
            .additionalProperties(convertToMapObject(document));
    }

    /**
     * Creates a new IndexAction for uploading a document to the index, or merging it into an existing document if it
     * already exists in the index.
     *
     * @param document The document to merge or upload.
     * @param <T>
     * @return A new IndexAction.
     */
    public static <T> IndexAction mergeOrUpload(T document){
        return new IndexAction()
            .actionType(IndexActionType.MERGE_OR_UPLOAD)
            .additionalProperties(convertToMapObject(document));
    }

    /**
     * Creates a new IndexAction for uploading a document to the index.
     *
     * @param document The document to upload.
     * @param <T>
     * @return A new IndexAction.
     */
    public static <T> IndexAction upload(T document){
        return new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(convertToMapObject(document));
    }

    private static <T> Map<String, Object> convertToMapObject(T document) {
        jsonApi.configureTimezone();
        return jsonApi.convertObjectToType(document, Map.class);
    }
}
