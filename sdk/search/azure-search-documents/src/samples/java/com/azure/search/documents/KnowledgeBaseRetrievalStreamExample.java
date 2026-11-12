// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.TokenCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.core.util.CloseableIterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReferencesCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseErrorStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Demonstrates streaming a knowledge base retrieval response as server-sent events.
 *
 * <p>Set {@code SEARCH_SERVICE_ENDPOINT}, {@code SEARCH_OPENAI_ENDPOINT}, {@code SEARCH_OPENAI_DEPLOYMENT_NAME},
 * and {@code SEARCH_OPENAI_MODEL_NAME}. Authenticate locally with {@code az login}. The example uses
 * {@link com.azure.identity.DefaultAzureCredential}, which also supports managed identity when hosted in Azure. Run
 * without arguments for the asynchronous client or pass {@code sync} to use the synchronous client.</p>
 *
 * <p>Enable RBAC authentication on the Search service and grant the caller Search Service Contributor and
 * Search Index Data Contributor. Enable the Search service's system-assigned managed identity and grant it
 * Cognitive Services OpenAI User on the Azure OpenAI resource. Omitting the model API key and authentication
 * identity lets Search use its system-assigned identity for Azure OpenAI.</p>
 *
 * <p>The example prints each event's deserialized JSON payload, which can contain document content and citation
 * URLs. Use sample data and avoid sharing output containing sensitive information.</p>
 */
public class KnowledgeBaseRetrievalStreamExample {
    private static final String SEMANTIC_CONFIGURATION_NAME = "sample-semantic-config";
    private static final ObjectMapper EVENT_MAPPER = new ObjectMapper().findAndRegisterModules();

    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_SERVICE_ENDPOINT");
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Set SEARCH_SERVICE_ENDPOINT before running this example. "
                + "Load your environment script in the same PowerShell session as Maven.");
        }
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
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
        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> events = client.retrieveStream(request)) {
            for (KnowledgeBaseRetrievalStreamEvent event : events) {
                inspectEvent(event, started, completed);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to close the knowledge base retrieval stream.", exception);
        }
        verifyStream(started, completed);
    }

    static void inspectEvent(KnowledgeBaseRetrievalStreamEvent event, AtomicBoolean started,
        AtomicBoolean completed) {
        System.out.println("Received event: " + event.getEventName());
        if (event instanceof KnowledgeBaseAnswerCompletedStreamEvent) {
            KnowledgeBaseAnswerCompletedStreamEvent answerEvent = (KnowledgeBaseAnswerCompletedStreamEvent) event;
            printEventData(answerEvent.getValue());
            answerEvent.getValue().getMessage().getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println("Answer: " + ((KnowledgeBaseMessageTextContent) content).getText());
                }
            });
        }
        if (event instanceof KnowledgeBaseRetrievalStartedStreamEvent) {
            started.set(true);
            KnowledgeBaseRetrievalStartedStreamEvent retrieval = (KnowledgeBaseRetrievalStartedStreamEvent) event;
            printEventData(retrieval.getValue());
            if (retrieval.getValue().getReasoningEffort() != null) {
                System.out.println("Reasoning effort: " + retrieval.getValue().getReasoningEffort().getKind());
            }
        }
        if (event instanceof KnowledgeBaseActivityStartedStreamEvent) {
            printEventData(((KnowledgeBaseActivityStartedStreamEvent) event).getValue());
            System.out.println("Activity started: "
                + ((KnowledgeBaseActivityStartedStreamEvent) event).getValue().getId());
        }
        if (event instanceof KnowledgeBaseActivityCompletedStreamEvent) {
            printEventData(((KnowledgeBaseActivityCompletedStreamEvent) event).getValue());
            System.out.println("Activity completed: "
                + ((KnowledgeBaseActivityCompletedStreamEvent) event).getValue().getType());
        }
        if (event instanceof KnowledgeBaseReferencesCompletedStreamEvent) {
            printEventData(((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue());
            ((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue().forEach(reference ->
                System.out.println("Citation URL: " + KnowledgeRetrievalResponseExample.citationUrl(reference)));
        }
        if (event instanceof KnowledgeBaseErrorStreamEvent) {
            printEventData(((KnowledgeBaseErrorStreamEvent) event).getValue());
            throw new IllegalStateException("Knowledge retrieval failed: "
                + ((KnowledgeBaseErrorStreamEvent) event).getValue().getError().getMessage());
        }
        if (event instanceof KnowledgeBaseResponseCompletedStreamEvent) {
            KnowledgeBaseResponseCompletedStreamEvent completionEvent
                = (KnowledgeBaseResponseCompletedStreamEvent) event;
            printEventData(completionEvent.getValue());
            if (!completionEvent.isTerminal() || completionEvent.getValue().getResponse() == null) {
                throw new IllegalStateException("The terminal event didn't contain the completed response.");
            }
            System.out.println("Retrieval completed with status: " + completionEvent.getValue().getStatusCode());
            completed.set(true);
        }
    }

    private static void printEventData(Object value) {
        try {
            // Serialize getters so read-only response properties are included in diagnostic output.
            System.out.println("Event data: " + EVENT_MAPPER.writeValueAsString(value));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to format the received event data.", exception);
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
