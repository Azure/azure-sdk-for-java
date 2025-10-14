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
import com.azure.search.documents.agents.SearchKnowledgeAgentAsyncClient;
import com.azure.search.documents.agents.SearchKnowledgeAgentClient;
import com.azure.search.documents.agents.models.KnowledgeAgentMessage;
import com.azure.search.documents.agents.models.KnowledgeAgentMessageTextContent;
import com.azure.search.documents.agents.models.KnowledgeAgentRetrievalRequest;
import com.azure.search.documents.agents.models.KnowledgeAgentRetrievalResponse;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeAgent;
import com.azure.search.documents.indexes.models.KnowledgeAgentAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeAgentModel;
import com.azure.search.documents.indexes.models.KnowledgeAgentOutputConfiguration;
import com.azure.search.documents.indexes.models.KnowledgeAgentOutputConfigurationModality;
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
 * Tests for Knowledge Agent operations.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class KnowledgeAgentTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME = "shared-knowledge-agent-index";
    private static final String HOTEL_KNOWLEDGE_SOURCE_NAME = "shared-knowledge-agent-source";
    private static final KnowledgeAgentAzureOpenAIModel OPEN_AI_AGENT_MODEL = new KnowledgeAgentAzureOpenAIModel(
        new AzureOpenAIVectorizerParameters().setModelName(AzureOpenAIModelName.fromString(OPENAI_MODEL_NAME))
            .setDeploymentName(OPENAI_DEPLOYMENT_NAME)
            .setResourceUrl(OPENAI_ENDPOINT)
            .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY)));
    private static final List<KnowledgeAgentModel> KNOWLEDGE_AGENT_MODELS
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
            // Delete Knowledge Agents created during tests.
            searchIndexClient.listKnowledgeAgents()
                .forEach(agent -> searchIndexClient.deleteKnowledgeAgent(agent.getName()));
        }
    }

    @AfterAll
    protected static void cleanupClass() {
        // Clean up any resources after all tests.
        if (TEST_MODE != TestMode.PLAYBACK) {
            // Delete all knowledge agents.
            searchIndexClient.listKnowledgeAgents()
                .forEach(agent -> searchIndexClient.deleteKnowledgeAgent(agent.getName()));

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
    public void createKnowledgeAgentSync() {
        // Test creating a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        KnowledgeAgent created = searchIndexClient.createKnowledgeAgent(knowledgeAgent);

        assertEquals(knowledgeAgent.getName(), created.getName());

        assertEquals(1, created.getModels().size());
        KnowledgeAgentAzureOpenAIModel createdModel
            = assertInstanceOf(KnowledgeAgentAzureOpenAIModel.class, created.getModels().get(0));
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
    public void createKnowledgeAgentAsync() {
        // Test creating a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        StepVerifier.create(searchIndexClient.createKnowledgeAgent(knowledgeAgent)).assertNext(created -> {
            assertEquals(knowledgeAgent.getName(), created.getName());

            assertEquals(1, created.getModels().size());
            KnowledgeAgentAzureOpenAIModel createdModel
                = assertInstanceOf(KnowledgeAgentAzureOpenAIModel.class, created.getModels().get(0));
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
    public void getKnowledgeAgentSync() {
        // Test getting a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);

        KnowledgeAgent retrieved = searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName());
        assertEquals(knowledgeAgent.getName(), retrieved.getName());

        assertEquals(1, retrieved.getModels().size());
        KnowledgeAgentAzureOpenAIModel retrievedModel
            = assertInstanceOf(KnowledgeAgentAzureOpenAIModel.class, retrieved.getModels().get(0));
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
    public void getKnowledgeAgentAsync() {
        // Test getting a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<KnowledgeAgent> createAndGet = searchIndexClient.createKnowledgeAgent(knowledgeAgent)
            .flatMap(created -> searchIndexClient.getKnowledgeAgent(created.getName()));

        StepVerifier.create(createAndGet).assertNext(retrieved -> {
            assertEquals(knowledgeAgent.getName(), retrieved.getName());

            assertEquals(1, retrieved.getModels().size());
            KnowledgeAgentAzureOpenAIModel retrievedModel
                = assertInstanceOf(KnowledgeAgentAzureOpenAIModel.class, retrieved.getModels().get(0));
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
    public void listKnowledgeAgentsSync() {
        // Test listing knowledge agents.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        long currentCount = searchIndexClient.listKnowledgeAgents().stream().count();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        KnowledgeAgent knowledgeAgent2
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent2);
        Map<String, KnowledgeAgent> knowledgeAgentsByName = searchIndexClient.listKnowledgeAgents()
            .stream()
            .collect(Collectors.toMap(KnowledgeAgent::getName, Function.identity()));

        assertEquals(2, knowledgeAgentsByName.size() - currentCount);
        KnowledgeAgent listedAgent1 = knowledgeAgentsByName.get(knowledgeAgent.getName());
        assertNotNull(listedAgent1);
        KnowledgeAgent listedAgent2 = knowledgeAgentsByName.get(knowledgeAgent2.getName());
        assertNotNull(listedAgent2);
    }

    @Test
    public void listKnowledgeAgentsAsync() {
        // Test listing knowledge agents.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        KnowledgeAgent knowledgeAgent2
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<Tuple2<Long, Map<String, KnowledgeAgent>>> tuple2Mono = searchIndexClient.listKnowledgeAgents()
            .count()
            .flatMap(currentCount -> Mono
                .when(searchIndexClient.createKnowledgeAgent(knowledgeAgent),
                    searchIndexClient.createKnowledgeAgent(knowledgeAgent2))
                .then(searchIndexClient.listKnowledgeAgents().collectMap(KnowledgeAgent::getName))
                .map(map -> Tuples.of(currentCount, map)));

        StepVerifier.create(tuple2Mono).assertNext(tuple -> {
            Map<String, KnowledgeAgent> knowledgeAgentsByName = tuple.getT2();
            assertEquals(2, knowledgeAgentsByName.size() - tuple.getT1());
            KnowledgeAgent listedAgent1 = knowledgeAgentsByName.get(knowledgeAgent.getName());
            assertNotNull(listedAgent1);
            KnowledgeAgent listedAgent2 = knowledgeAgentsByName.get(knowledgeAgent2.getName());
            assertNotNull(listedAgent2);
        }).verifyComplete();
    }

    @Test
    public void deleteKnowledgeAgentSync() {
        // Test deleting a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);

        assertEquals(knowledgeAgent.getName(), searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName()).getName());
        searchIndexClient.deleteKnowledgeAgent(knowledgeAgent.getName());
        assertThrows(HttpResponseException.class, () -> searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName()));
    }

    @Test
    public void deleteKnowledgeAgentAsync() {
        // Test deleting a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<KnowledgeAgent> createAndGetMono = searchIndexClient.createKnowledgeAgent(knowledgeAgent)
            .flatMap(created -> searchIndexClient.getKnowledgeAgent(created.getName()));

        StepVerifier.create(createAndGetMono)
            .assertNext(retrieved -> assertEquals(knowledgeAgent.getName(), retrieved.getName()))
            .verifyComplete();

        StepVerifier.create(searchIndexClient.deleteKnowledgeAgent(knowledgeAgent.getName())).verifyComplete();

        StepVerifier.create(searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName()))
            .verifyError(HttpResponseException.class);
    }

    @Test
    public void updateKnowledgeAgentSync() {
        // Test updating a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);
        String newDescription = "Updated description";
        knowledgeAgent.setDescription(newDescription);
        searchIndexClient.createOrUpdateKnowledgeAgent(knowledgeAgent);
        KnowledgeAgent retrieved = searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName());
        assertEquals(newDescription, retrieved.getDescription());
    }

    @Test
    public void updateKnowledgeAgentAsync() {
        // Test updating a knowledge agent.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        String newDescription = "Updated description";

        Mono<KnowledgeAgent> createUpdateAndGetMono = searchIndexClient.createKnowledgeAgent(knowledgeAgent)
            .flatMap(created -> searchIndexClient.createOrUpdateKnowledgeAgent(created.setDescription(newDescription)))
            .flatMap(updated -> searchIndexClient.getKnowledgeAgent(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono)
            .assertNext(retrieved -> assertEquals(newDescription, retrieved.getDescription()))
            .verifyComplete();
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void basicRetrievalSync() {
        // Test knowledge agent retrieval functionality.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);

        SearchKnowledgeAgentClient knowledgeAgentClient
            = getSearchKnowledgeAgentClientBuilder(true).agentName(knowledgeAgent.getName()).buildClient();

        KnowledgeAgentMessageTextContent messageTextContent
            = new KnowledgeAgentMessageTextContent("What are the pet policies at the hotel?");
        KnowledgeAgentMessage message
            = new KnowledgeAgentMessage(Collections.singletonList(messageTextContent)).setRole("user");
        KnowledgeAgentRetrievalRequest retrievalRequest
            = new KnowledgeAgentRetrievalRequest(Collections.singletonList(message));

        KnowledgeAgentRetrievalResponse response = knowledgeAgentClient.retrieve(retrievalRequest, null);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void basicRetrievalAsync() {
        // Test knowledge agent retrieval functionality.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);

        Mono<KnowledgeAgentRetrievalResponse> createAndRetrieveMono
            = searchIndexClient.createKnowledgeAgent(knowledgeAgent).flatMap(created -> {
                SearchKnowledgeAgentAsyncClient knowledgeAgentClient
                    = getSearchKnowledgeAgentClientBuilder(false).agentName(created.getName()).buildAsyncClient();

                KnowledgeAgentMessageTextContent messageTextContent
                    = new KnowledgeAgentMessageTextContent("What are the pet policies at the hotel?");
                KnowledgeAgentMessage message
                    = new KnowledgeAgentMessage(Collections.singletonList(messageTextContent)).setRole("user");
                KnowledgeAgentRetrievalRequest retrievalRequest
                    = new KnowledgeAgentRetrievalRequest(Collections.singletonList(message));

                return knowledgeAgentClient.retrieve(retrievalRequest, null);
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
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES)
                .setRetrievalInstructions("Only include well reviewed hotels.")
                .setOutputConfiguration(new KnowledgeAgentOutputConfiguration()
                    .setModality(KnowledgeAgentOutputConfigurationModality.ANSWER_SYNTHESIS)
                    .setAnswerInstructions("Provide a concise answer based on the provided information.")
                    .setAttemptFastPath(true)
                    .setIncludeActivity(true));
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);

        SearchKnowledgeAgentClient knowledgeAgentClient
            = getSearchKnowledgeAgentClientBuilder(true).agentName(knowledgeAgent.getName()).buildClient();

        KnowledgeAgentMessageTextContent messageTextContent
            = new KnowledgeAgentMessageTextContent("What are the pet policies at the hotel?");
        KnowledgeAgentMessage message
            = new KnowledgeAgentMessage(Collections.singletonList(messageTextContent)).setRole("user");
        KnowledgeAgentRetrievalRequest retrievalRequest
            = new KnowledgeAgentRetrievalRequest(Collections.singletonList(message));

        KnowledgeAgentRetrievalResponse response = knowledgeAgentClient.retrieve(retrievalRequest, null);
        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertNotNull(response.getActivity());
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void answerSynthesisRetrievalAsync() {
        // Test knowledge agent retrieval functionality.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES)
                .setRetrievalInstructions("Only include well reviewed hotels.")
                .setOutputConfiguration(new KnowledgeAgentOutputConfiguration()
                    .setModality(KnowledgeAgentOutputConfigurationModality.ANSWER_SYNTHESIS)
                    .setAnswerInstructions("Provide a concise answer based on the provided information.")
                    .setAttemptFastPath(true)
                    .setIncludeActivity(true));

        Mono<KnowledgeAgentRetrievalResponse> createAndRetrieveMono
            = searchIndexClient.createKnowledgeAgent(knowledgeAgent).flatMap(created -> {
                SearchKnowledgeAgentAsyncClient knowledgeAgentClient
                    = getSearchKnowledgeAgentClientBuilder(false).agentName(created.getName()).buildAsyncClient();

                KnowledgeAgentMessageTextContent messageTextContent
                    = new KnowledgeAgentMessageTextContent("What are the pet policies at the hotel?");
                KnowledgeAgentMessage message
                    = new KnowledgeAgentMessage(Collections.singletonList(messageTextContent)).setRole("user");
                KnowledgeAgentRetrievalRequest retrievalRequest
                    = new KnowledgeAgentRetrievalRequest(Collections.singletonList(message));

                return knowledgeAgentClient.retrieve(retrievalRequest, null);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
            assertNotNull(response.getActivity());
        }).verifyComplete();
    }

    private String randomKnowledgeAgentName() {
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
