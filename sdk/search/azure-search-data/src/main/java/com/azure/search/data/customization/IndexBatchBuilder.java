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

    public IndexBatchBuilder() {
        jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
    }

    public <T> IndexBatch upload(T document) {
        IndexAction action =
            new IndexAction().
                actionType(IndexActionType.UPLOAD).
                additionalProperties(entityToMap(document));

        return assembleBatch(action);
    }

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
        this.jsonApi.configureTimezone();

        return this.jsonApi.convertObjectToType(entity, Map.class);
    }
}
