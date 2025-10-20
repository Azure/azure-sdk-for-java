// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.knowledgebases.SearchKnowledgeBaseAsyncClient;
import com.azure.search.documents.knowledgebases.SearchKnowledgeBaseClient;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessage;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalRequest;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResponse;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeBaseModel;
import com.azure.search.documents.indexes.models.KnowledgeBaseOutputConfiguration;
import com.azure.search.documents.indexes.models.KnowledgeBaseOutputConfigurationModality;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.loadResource;
import static com.azure.search.documents.TestHelpers.uploadDocumentsJson;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Knowledge Base operations.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class KnowledgeBaseTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME = "shared-knowledge-agent-index";
    private static final String HOTEL_KNOWLEDGE_SOURCE_NAME = "shared-knowledge-agent-source";
    private static final KnowledgeBaseAzureOpenAIModel OPEN_AI_AGENT_MODEL = new KnowledgeBaseAzureOpenAIModel(
        new AzureOpenAIVectorizerParameters().setModelName(AzureOpenAIModelName.fromString(OPENAI_MODEL_NAME))
            .setDeploymentName(OPENAI_DEPLOYMENT_NAME)
            .setResourceUrl(OPENAI_ENDPOINT)
            .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY)));
    private static final List<KnowledgeBaseModel> KNOWLEDGE_AGENT_MODELS
        = Collections.singletonList(OPEN_AI_AGENT_MODEL);
    private static final List<KnowledgeSourceReference> KNOWLEDGE_SOURCE_REFERENCES
        = Collections.singletonList(new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME));

    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        // Set up any necessary configurations or resources before all tests.
        TestProxyTestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupIndex();
        waitForIndexing();

        searchIndexClient.createKnowledgeSource(new SearchIndexKnowledgeSource(HOTEL_KNOWLEDGE_SOURCE_NAME,
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME)));
    }

    @BeforeEach
    public void setup() {
        interceptorManager.addMatchers(new BodilessMatcher());

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(
                new TestProxySanitizer("$..userAssignedIdentity", null, "REDACTED", TestProxySanitizerType.BODY_KEY),
                new TestProxySanitizer("$..azureOpenAIParameters.resourceUri", TestProxyUtils.HOST_NAME_REGEX,
                    "REDACTED", TestProxySanitizerType.BODY_KEY));
        }
    }

    @AfterEach
    public void cleanup() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            // Delete Knowledge Bases created during tests.
            searchIndexClient.listKnowledgeBases()
                .forEach(agent -> searchIndexClient.deleteKnowledgeBase(agent.getName()));
        }
    }

    @AfterAll
    protected static void cleanupClass() {
        // Clean up any resources after all tests.
        if (TEST_MODE != TestMode.PLAYBACK) {
            // Delete all knowledge knowledgebases.
            searchIndexClient.listKnowledgeBases()
                .forEach(agent -> searchIndexClient.deleteKnowledgeBase(agent.getName()));

            // Delete the knowledge source created for the tests.
            searchIndexClient.deleteKnowledgeSource(HOTEL_KNOWLEDGE_SOURCE_NAME);

            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void createKnowledgeBaseSync() {
        // Test creating a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), created.getName());

        assertEquals(1, created.getModels().size());
        KnowledgeBaseAzureOpenAIModel createdModel
            = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, created.getModels().get(0));
        if (interceptorManager.isLiveMode()) {
            assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                createdModel.getAzureOpenAIParameters().getDeploymentName());
            assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getModelName(),
                createdModel.getAzureOpenAIParameters().getModelName());
            assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getResourceUrl(),
                createdModel.getAzureOpenAIParameters().getResourceUrl());
        }

        assertEquals(1, created.getKnowledgeSources().size());
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, created.getKnowledgeSources().get(0).getName());
    }

    @Test
    public void createKnowledgeBaseAsync() {
        // Test creating a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        StepVerifier.create(searchIndexClient.createKnowledgeBase(knowledgeBase)).assertNext(created -> {
            assertEquals(knowledgeBase.getName(), created.getName());

            assertEquals(1, created.getModels().size());
            KnowledgeBaseAzureOpenAIModel createdModel
                = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, created.getModels().get(0));
            if (interceptorManager.isLiveMode()) {
                assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                    createdModel.getAzureOpenAIParameters().getDeploymentName());
                assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getModelName(),
                    createdModel.getAzureOpenAIParameters().getModelName());
                assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getResourceUrl(),
                    createdModel.getAzureOpenAIParameters().getResourceUrl());
            }

            assertEquals(1, created.getKnowledgeSources().size());
            assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, created.getKnowledgeSources().get(0).getName());
        }).verifyComplete();
    }

    @Test
    public void getKnowledgeBaseSync() {
        // Test getting a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBase retrieved = searchIndexClient.getKnowledgeBase(knowledgeBase.getName());
        assertEquals(knowledgeBase.getName(), retrieved.getName());

        assertEquals(1, retrieved.getModels().size());
        KnowledgeBaseAzureOpenAIModel retrievedModel
            = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, retrieved.getModels().get(0));
        if (interceptorManager.isLiveMode()) {
            assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                retrievedModel.getAzureOpenAIParameters().getDeploymentName());
            assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getModelName(),
                retrievedModel.getAzureOpenAIParameters().getModelName());
            assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getResourceUrl(),
                retrievedModel.getAzureOpenAIParameters().getResourceUrl());
        }

        assertEquals(1, retrieved.getKnowledgeSources().size());
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, retrieved.getKnowledgeSources().get(0).getName());
    }

    @Test
    public void getKnowledgeBaseAsync() {
        // Test getting a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<KnowledgeBase> createAndGet = searchIndexClient.createKnowledgeBase(knowledgeBase)
            .flatMap(created -> searchIndexClient.getKnowledgeBase(created.getName()));

        StepVerifier.create(createAndGet).assertNext(retrieved -> {
            assertEquals(knowledgeBase.getName(), retrieved.getName());

            assertEquals(1, retrieved.getModels().size());
            KnowledgeBaseAzureOpenAIModel retrievedModel
                = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, retrieved.getModels().get(0));
            if (interceptorManager.isLiveMode()) {
                assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                    retrievedModel.getAzureOpenAIParameters().getDeploymentName());
                assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getModelName(),
                    retrievedModel.getAzureOpenAIParameters().getModelName());
                assertEquals(OPEN_AI_AGENT_MODEL.getAzureOpenAIParameters().getResourceUrl(),
                    retrievedModel.getAzureOpenAIParameters().getResourceUrl());
            }

            assertEquals(1, retrieved.getKnowledgeSources().size());
            assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, retrieved.getKnowledgeSources().get(0).getName());
        }).verifyComplete();
    }

    @Test
    public void listKnowledgeBasesSync() {
        // Test listing knowledge knowledgebases.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        long currentCount = searchIndexClient.listKnowledgeBases().stream().count();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        KnowledgeBase knowledgeBase2
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeBase(knowledgeBase);
        searchIndexClient.createKnowledgeBase(knowledgeBase2);
        Map<String, KnowledgeBase> knowledgeBasesByName = searchIndexClient.listKnowledgeBases()
            .stream()
            .collect(Collectors.toMap(KnowledgeBase::getName, Function.identity()));

        assertEquals(2, knowledgeBasesByName.size() - currentCount);
        KnowledgeBase listedAgent1 = knowledgeBasesByName.get(knowledgeBase.getName());
        assertNotNull(listedAgent1);
        KnowledgeBase listedAgent2 = knowledgeBasesByName.get(knowledgeBase2.getName());
        assertNotNull(listedAgent2);
    }

    @Test
    public void listKnowledgeBasesAsync() {
        // Test listing knowledge knowledgebases.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        KnowledgeBase knowledgeBase2
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<Tuple2<Long, Map<String, KnowledgeBase>>> tuple2Mono = searchIndexClient.listKnowledgeBases()
            .count()
            .flatMap(currentCount -> Mono
                .when(searchIndexClient.createKnowledgeBase(knowledgeBase),
                    searchIndexClient.createKnowledgeBase(knowledgeBase2))
                .then(searchIndexClient.listKnowledgeBases().collectMap(KnowledgeBase::getName))
                .map(map -> Tuples.of(currentCount, map)));

        StepVerifier.create(tuple2Mono).assertNext(tuple -> {
            Map<String, KnowledgeBase> knowledgeBasesByName = tuple.getT2();
            assertEquals(2, knowledgeBasesByName.size() - tuple.getT1());
            KnowledgeBase listedAgent1 = knowledgeBasesByName.get(knowledgeBase.getName());
            assertNotNull(listedAgent1);
            KnowledgeBase listedAgent2 = knowledgeBasesByName.get(knowledgeBase2.getName());
            assertNotNull(listedAgent2);
        }).verifyComplete();
    }

    @Test
    public void deleteKnowledgeBaseSync() {
        // Test deleting a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), searchIndexClient.getKnowledgeBase(knowledgeBase.getName()).getName());
        searchIndexClient.deleteKnowledgeBase(knowledgeBase.getName());
        assertThrows(HttpResponseException.class, () -> searchIndexClient.getKnowledgeBase(knowledgeBase.getName()));
    }

    @Test
    public void deleteKnowledgeBaseAsync() {
        // Test deleting a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<KnowledgeBase> createAndGetMono = searchIndexClient.createKnowledgeBase(knowledgeBase)
            .flatMap(created -> searchIndexClient.getKnowledgeBase(created.getName()));

        StepVerifier.create(createAndGetMono)
            .assertNext(retrieved -> assertEquals(knowledgeBase.getName(), retrieved.getName()))
            .verifyComplete();

        StepVerifier.create(searchIndexClient.deleteKnowledgeBase(knowledgeBase.getName())).verifyComplete();

        StepVerifier.create(searchIndexClient.getKnowledgeBase(knowledgeBase.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void updateKnowledgeBaseSync() {
        // Test updating a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeBase(knowledgeBase);
        String newDescription = "Updated description";
        knowledgeBase.setDescription(newDescription);
        searchIndexClient.createOrUpdateKnowledgeBase(knowledgeBase);
        KnowledgeBase retrieved = searchIndexClient.getKnowledgeBase(knowledgeBase.getName());
        assertEquals(newDescription, retrieved.getDescription());
    }

    @Test
    public void updateKnowledgeBaseAsync() {
        // Test updating a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        String newDescription = "Updated description";

        Mono<KnowledgeBase> createUpdateAndGetMono = searchIndexClient.createKnowledgeBase(knowledgeBase)
            .flatMap(created -> searchIndexClient.createOrUpdateKnowledgeBase(created.setDescription(newDescription)))
            .flatMap(updated -> searchIndexClient.getKnowledgeBase(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono)
            .assertNext(retrieved -> assertEquals(newDescription, retrieved.getDescription()))
            .verifyComplete();
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void basicRetrievalSync() {
        // Test knowledge agent retrieval functionality.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        SearchKnowledgeBaseClient knowledgeBaseClient
            = getSearchKnowledgeBaseClientBuilder(true).agentName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseMessageTextContent messageTextContent
            = new KnowledgeBaseMessageTextContent("What are the pet policies at the hotel?");
        KnowledgeBaseMessage message
            = new KnowledgeBaseMessage(Collections.singletonList(messageTextContent)).setRole("user");
        KnowledgeBaseRetrievalRequest retrievalRequest
            = new KnowledgeBaseRetrievalRequest(Collections.singletonList(message));

        KnowledgeBaseRetrievalResponse response = knowledgeBaseClient.retrieve(retrievalRequest, null);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void basicRetrievalAsync() {
        // Test knowledge agent retrieval functionality.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<KnowledgeBaseRetrievalResponse> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                SearchKnowledgeBaseAsyncClient knowledgeBaseClient
                    = getSearchKnowledgeBaseClientBuilder(false).agentName(created.getName()).buildAsyncClient();

                KnowledgeBaseMessageTextContent messageTextContent
                    = new KnowledgeBaseMessageTextContent("What are the pet policies at the hotel?");
                KnowledgeBaseMessage message
                    = new KnowledgeBaseMessage(Collections.singletonList(messageTextContent)).setRole("user");
                KnowledgeBaseRetrievalRequest retrievalRequest
                    = new KnowledgeBaseRetrievalRequest(Collections.singletonList(message));

                return knowledgeBaseClient.retrieve(retrievalRequest, null);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void answerSynthesisRetrievalSync() {
        // Test knowledge agent retrieval functionality.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES)
                .setRetrievalInstructions("Only include well reviewed hotels.")
                .setOutputConfiguration(new KnowledgeBaseOutputConfiguration()
                    .setModality(KnowledgeBaseOutputConfigurationModality.ANSWER_SYNTHESIS)
                    .setAnswerInstructions("Provide a concise answer based on the provided information.")
                    .setAttemptFastPath(true)
                    .setIncludeActivity(true));
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        SearchKnowledgeBaseClient knowledgeBaseClient
            = getSearchKnowledgeBaseClientBuilder(true).agentName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseMessageTextContent messageTextContent
            = new KnowledgeBaseMessageTextContent("What are the pet policies at the hotel?");
        KnowledgeBaseMessage message
            = new KnowledgeBaseMessage(Collections.singletonList(messageTextContent)).setRole("user");
        KnowledgeBaseRetrievalRequest retrievalRequest
            = new KnowledgeBaseRetrievalRequest(Collections.singletonList(message));

        KnowledgeBaseRetrievalResponse response = knowledgeBaseClient.retrieve(retrievalRequest, null);
        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertNotNull(response.getActivity());
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void answerSynthesisRetrievalAsync() {
        // Test knowledge agent retrieval functionality.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES)
                .setRetrievalInstructions("Only include well reviewed hotels.")
                .setOutputConfiguration(new KnowledgeBaseOutputConfiguration()
                    .setModality(KnowledgeBaseOutputConfigurationModality.ANSWER_SYNTHESIS)
                    .setAnswerInstructions("Provide a concise answer based on the provided information.")
                    .setAttemptFastPath(true)
                    .setIncludeActivity(true));

        Mono<KnowledgeBaseRetrievalResponse> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                SearchKnowledgeBaseAsyncClient knowledgeBaseClient
                    = getSearchKnowledgeBaseClientBuilder(false).agentName(created.getName()).buildAsyncClient();

                KnowledgeBaseMessageTextContent messageTextContent
                    = new KnowledgeBaseMessageTextContent("What are the pet policies at the hotel?");
                KnowledgeBaseMessage message
                    = new KnowledgeBaseMessage(Collections.singletonList(messageTextContent)).setRole("user");
                KnowledgeBaseRetrievalRequest retrievalRequest
                    = new KnowledgeBaseRetrievalRequest(Collections.singletonList(message));

                return knowledgeBaseClient.retrieve(retrievalRequest, null);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
            assertNotNull(response.getActivity());
        }).verifyComplete();
    }

    private String randomKnowledgeBaseName() {
        // Generate a random name for the knowledge agent.
        return testResourceNamer.randomName("knowledge-agent-", 63);
    }

    private static SearchIndexClient setupIndex() {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(HOTELS_TESTS_INDEX_DATA_JSON))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            List<SemanticConfiguration> semanticConfigurations
                = Collections.singletonList(new SemanticConfiguration("semantic-config",
                    new SemanticPrioritizedFields().setTitleField(new SemanticField("HotelName"))
                        .setContentFields(new SemanticField("Description"))
                        .setKeywordsFields(new SemanticField("Category"))));
            SemanticSearch semanticSearch = new SemanticSearch().setDefaultConfigurationName("semantic-config")
                .setConfigurations(semanticConfigurations);
            searchIndexClient.createOrUpdateIndex(
                TestHelpers.createTestIndex(HOTEL_INDEX_NAME, baseIndex).setSemanticSearch(semanticSearch));

            uploadDocumentsJson(searchIndexClient.getSearchClient(HOTEL_INDEX_NAME), HOTELS_DATA_JSON);

            return searchIndexClient;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
