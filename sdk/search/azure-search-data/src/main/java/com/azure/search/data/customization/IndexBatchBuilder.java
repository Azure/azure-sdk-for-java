// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class IndexBatchBuilder {
    private JsonApi jsonApi;

    /**
     * Package private constructor to be used by {@link SearchIndexClientImpl} or {@link SearchIndexAsyncClientImpl}
     */
    IndexBatchBuilder() {
        jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();
    }

    /**
     * Uploads a document to the index.
     *
     * @param document The document to be uploaded.
     * @param <T> The type of object to serialize
     * @return An IndexBatch with the desired actions.
     */
    public <T> IndexBatch upload(T document) {
        IndexAction action =
            new IndexAction().
                actionType(IndexActionType.UPLOAD).
                additionalProperties(entityToMap(document));

        return assembleBatch(action);
    }

    /**
     * Uploads a collection of documents to the index.
     *
     * @param documents The document collection to be uploaded.
     * @param <T> The type of object to serialize
     * @return An IndexBatch with the desired actions.
     */
    public <T> IndexBatch upload(List<T> documents) {
        IndexAction[] actions = documents.stream()
            .map(doc -> new IndexAction()
                .actionType(IndexActionType.UPLOAD)
                .additionalProperties(entityToMap(doc))).toArray(IndexAction[]::new);

        return assembleBatch(actions);
    }

    private <T> IndexBatch assembleBatch(IndexAction... actions) {
        List<IndexAction> actionList = Arrays.asList(actions);
        return new IndexBatch().actions(actionList);
    }

    private <T> Map<String, Object> entityToMap(T entity) {
        return this.jsonApi.convertObjectToType(entity, Map.class);
    }
}
