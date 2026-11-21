// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class PreviewSampleSupport {
    private PreviewSampleSupport() {
    }

    static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Set " + name + " before running this sample.");
        }
        return value;
    }

    static String optional(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    static String name(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    static SearchIndexClient indexClient() {
        return new SearchIndexClientBuilder().endpoint(required("SEARCH_ENDPOINT"))
            .credential(new AzureKeyCredential(required("SEARCH_API_KEY")))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .buildClient();
    }

    static KnowledgeBaseRetrievalClient retrievalClient(String knowledgeBaseName) {
        return new KnowledgeBaseRetrievalClientBuilder().endpoint(required("SEARCH_ENDPOINT"))
            .credential(new AzureKeyCredential(required("SEARCH_API_KEY")))
            .knowledgeBaseName(knowledgeBaseName)
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW)
            .buildClient();
    }

    static SearchIndex textIndex(String indexName) {
        return new SearchIndex(indexName, Arrays.asList(
            new SearchField("id", SearchFieldDataType.STRING).setKey(true).setFilterable(true),
            new SearchField("title", SearchFieldDataType.STRING).setSearchable(true),
            new SearchField("content", SearchFieldDataType.STRING).setSearchable(true),
            new SearchField("country", SearchFieldDataType.STRING).setFilterable(true).setFacetable(true)))
                .setSemanticSearch(new SemanticSearch().setDefaultConfigurationName("default")
                    .setConfigurations(new SemanticConfiguration("default", new SemanticPrioritizedFields()
                        .setTitleField(new SemanticField("title"))
                        .setContentFields(new SemanticField("content")))));
    }

    static List<Map<String, Object>> hotelDocuments() {
        String content = "Hotel guest benefits include complimentary wireless internet, breakfast, and parking.";
        return Arrays.asList(
            document("hotel-ca-1", "Historic Harbor Hotel", content, "Canada"),
            document("hotel-ca-2", "Mountain View Inn", content, "Canada"),
            document("hotel-us-1", "Lakeside Hotel", content, "United States"));
    }

    static Map<String, Object> document(String id, String title, String content, String country) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("id", id);
        document.put("title", title);
        document.put("content", content);
        document.put("country", country);
        return document;
    }

    static void upload(SearchClient client, List<Map<String, Object>> documents) {
        IndexAction[] actions = documents.stream()
            .map(document -> new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(document))
            .toArray(IndexAction[]::new);
        IndexDocumentsResult result = client.indexDocuments(new IndexDocumentsBatch(actions));
        if (result.getResults().size() != documents.size()
            || result.getResults().stream().anyMatch(item -> !item.isSucceeded())) {
            throw new IllegalStateException("Some sample documents could not be uploaded.");
        }
        waitForDocuments(client, documents.size());
    }

    static void waitForDocuments(SearchClient client, long count) {
        for (int attempt = 0; attempt < 30; attempt++) {
            if (client.getDocumentCount() >= count) {
                return;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for sample documents.", ex);
            }
        }
        throw new IllegalStateException("Sample documents did not become searchable within 60 seconds.");
    }

    static final class Resources implements AutoCloseable {
        private final SearchIndexClient client;
        private final Deque<Runnable> cleanup = new ArrayDeque<>();

        Resources(SearchIndexClient client) {
            this.client = client;
        }

        SearchIndex createIndex(SearchIndex index) {
            SearchIndex created = client.createIndex(index);
            cleanup.push(() -> client.deleteIndex(index.getName()));
            return created;
        }

        KnowledgeSource createKnowledgeSource(KnowledgeSource source) {
            KnowledgeSource created = client.createKnowledgeSource(source);
            cleanup.push(() -> client.deleteKnowledgeSource(source.getName()));
            return created;
        }

        KnowledgeBase createKnowledgeBase(KnowledgeBase knowledgeBase) {
            KnowledgeBase created = client.createKnowledgeBase(knowledgeBase);
            cleanup.push(() -> client.deleteKnowledgeBase(knowledgeBase.getName()));
            return created;
        }

        @Override
        public void close() {
            RuntimeException failure = null;
            while (!cleanup.isEmpty()) {
                try {
                    cleanup.pop().run();
                } catch (RuntimeException ex) {
                    if (ex instanceof HttpResponseException
                        && ((HttpResponseException) ex).getResponse().getStatusCode() == 404) {
                        continue;
                    }
                    if (failure == null) {
                        failure = ex;
                    } else {
                        failure.addSuppressed(ex);
                    }
                }
            }
            if (failure != null) {
                throw failure;
            }
        }
    }
}
