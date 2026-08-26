// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalAsyncClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.ServerSentEvent;
import com.azure.search.documents.models.ServerSentEventListener;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Demonstrates streaming a knowledge base retrieval response as server-sent events.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_OPENAI_ENDPOINT},
 * {@code SEARCH_OPENAI_API_KEY}, {@code SEARCH_OPENAI_DEPLOYMENT_NAME}, and {@code SEARCH_OPENAI_MODEL_NAME}. Run
 * without arguments for the asynchronous client or pass {@code sync} to use the synchronous client.</p>
 */
public class KnowledgeBaseRetrievalStreamExample {
    private static final String SEMANTIC_CONFIGURATION_NAME = "sample-semantic-config";

    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        AzureKeyCredential credential = new AzureKeyCredential(System.getenv("SEARCH_API_KEY"));
        SearchIndexClient indexClient
            = new SearchIndexClientBuilder().endpoint(endpoint).credential(credential).buildClient();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String indexName = "stream-index-" + suffix;
        String knowledgeSourceName = "stream-source-" + suffix;
        String knowledgeBaseName = "stream-kb-" + suffix;
        boolean indexCreated = false;
        boolean knowledgeSourceCreated = false;
        boolean knowledgeBaseCreated = false;
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setIncludeActivity(true);

        try {
            indexClient.createIndex(createSampleIndex(indexName));
            indexCreated = true;
            uploadSampleDocument(indexClient.getSearchClient(indexName));

            indexClient.createKnowledgeSource(new SearchIndexKnowledgeSource(knowledgeSourceName,
                new SearchIndexKnowledgeSourceParameters(indexName)
                    .setSemanticConfigurationName(SEMANTIC_CONFIGURATION_NAME)));
            knowledgeSourceCreated = true;
            indexClient.createKnowledgeBase(
                new KnowledgeBase(knowledgeBaseName, new KnowledgeSourceReference(knowledgeSourceName))
                    .setModels(new KnowledgeBaseAzureOpenAIModel(new AzureOpenAIVectorizerParameters()
                        .setResourceUrl(System.getenv("SEARCH_OPENAI_ENDPOINT"))
                        .setApiKey(System.getenv("SEARCH_OPENAI_API_KEY"))
                        .setDeploymentName(System.getenv("SEARCH_OPENAI_DEPLOYMENT_NAME"))
                        .setModelName(AzureOpenAIModelName.fromString(System.getenv("SEARCH_OPENAI_MODEL_NAME"))))));
            knowledgeBaseCreated = true;

            KnowledgeBaseRetrievalClientBuilder builder = new KnowledgeBaseRetrievalClientBuilder().endpoint(endpoint)
                .credential(credential)
                .knowledgeBaseName(knowledgeBaseName);
            if (args.length > 0 && "sync".equalsIgnoreCase(args[0])) {
                streamSynchronously(builder.buildClient(), request);
            } else {
                streamAsynchronously(builder.buildAsyncClient(), request);
            }
        } finally {
            if (knowledgeBaseCreated) {
                indexClient.deleteKnowledgeBase(knowledgeBaseName);
            }
            if (knowledgeSourceCreated) {
                indexClient.deleteKnowledgeSource(knowledgeSourceName);
            }
            if (indexCreated) {
                indexClient.deleteIndex(indexName);
            }
        }
    }

    private static void streamAsynchronously(KnowledgeBaseRetrievalAsyncClient client,
        KnowledgeBaseRetrievalOptions request) {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean completed = new AtomicBoolean();
        client.retrieveStream(request).doOnNext(event -> inspectEvent(event, started, completed)).blockLast();
        verifyStream(started, completed);
    }

    private static void streamSynchronously(KnowledgeBaseRetrievalClient client, KnowledgeBaseRetrievalOptions request) {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean completed = new AtomicBoolean();
        client.retrieveStream(request, new ServerSentEventListener<KnowledgeBaseRetrievalStreamEvent>() {
            @Override
            public void onEvent(ServerSentEvent<KnowledgeBaseRetrievalStreamEvent> event) {
                inspectEvent(event, started, completed);
            }

            @Override
            public void onError(Throwable error) {
                throw new IllegalStateException("Knowledge base retrieval stream failed.", error);
            }

            @Override
            public void onClose() {
                System.out.println("Stream closed.");
            }
        });
        verifyStream(started, completed);
    }

    private static void inspectEvent(ServerSentEvent<KnowledgeBaseRetrievalStreamEvent> event, AtomicBoolean started,
        AtomicBoolean completed) {
        System.out.println("Received event: " + event.getEvent());
        if (event.getData() instanceof KnowledgeBaseRetrievalStartedStreamEvent) {
            started.set(true);
        }
        if (event.getData() instanceof KnowledgeBaseResponseCompletedStreamEvent) {
            KnowledgeBaseResponseCompletedStreamEvent completionEvent
                = (KnowledgeBaseResponseCompletedStreamEvent) event.getData();
            if (!completionEvent.isTerminal() || completionEvent.getValue().getResponse() == null) {
                throw new IllegalStateException("The terminal event didn't contain the completed response.");
            }
            System.out.println("Retrieval completed with status: " + completionEvent.getValue().getStatusCode());
            completed.set(true);
        }
    }

    private static void verifyStream(AtomicBoolean started, AtomicBoolean completed) {
        if (!started.get() || !completed.get()) {
            throw new IllegalStateException("The stream didn't emit both retrieval.started and response.completed.");
        }
    }

    private static SearchIndex createSampleIndex(String indexName) {
        return new SearchIndex(indexName,
            Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("title", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("content", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("category", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true)))
                    .setSemanticSearch(new SemanticSearch().setConfigurations(new SemanticConfiguration(
                        SEMANTIC_CONFIGURATION_NAME,
                        new SemanticPrioritizedFields().setTitleField(new SemanticField("title"))
                            .setContentFields(new SemanticField("content"))
                            .setKeywordsFields(new SemanticField("category")))));
    }

    private static void uploadSampleDocument(SearchClient searchClient) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("id", "1");
        document.put("title", "Contoso Hotel");
        document.put("content", "The Contoso Hotel welcomes pets and provides free Wi-Fi.");
        document.put("category", "Luxury");
        IndexDocumentsResult result = searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(document)));
        if (result.getResults().size() != 1 || !result.getResults().get(0).isSucceeded()) {
            throw new IllegalStateException("The sample document wasn't indexed successfully.");
        }

        for (int attempt = 0; attempt < 30; attempt++) {
            if (searchClient.getDocumentCount() > 0) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for the sample document.", ex);
            }
        }
        throw new IllegalStateException("The sample document wasn't available for retrieval.");
    }
}
