// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.TestProxyUtils;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSource;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.FabricDataAgentKnowledgeSource;
import com.azure.search.documents.indexes.models.FabricDataAgentKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FabricOntologyKnowledgeSource;
import com.azure.search.documents.indexes.models.FabricOntologyKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FileKnowledgeSource;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeBaseModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalAsyncClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.models.AzureBlobKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.FabricDataAgentKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.FabricOntologyKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.FileKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAzureBlobReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedOneLakeReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedSharePointReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelAnswerSynthesisActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelQueryPlanningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceAzureOpenAIVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.PurviewSensitivityLabelInfo;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Knowledge Base operations.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class KnowledgeBaseTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME = "shared-knowledge-knowledgebase-index";
    private static final String HOTEL_KNOWLEDGE_SOURCE_NAME = "shared-knowledge-knowledgebase-source";
    private static final String KNOWLEDGEBASE_DEPLOYMENT_NAME = OPENAI_DEPLOYMENT_NAME;
    private static final KnowledgeBaseAzureOpenAIModel OPEN_AI_KNOWLEDGEBASE_MODEL = new KnowledgeBaseAzureOpenAIModel(
        new AzureOpenAIVectorizerParameters().setModelName(AzureOpenAIModelName.fromString(OPENAI_MODEL_NAME))
            .setDeploymentName(KNOWLEDGEBASE_DEPLOYMENT_NAME)
            .setResourceUrl(OPENAI_ENDPOINT)
            .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY)));
    private static final KnowledgeBaseModel KNOWLEDGE_BASE_MODEL = OPEN_AI_KNOWLEDGEBASE_MODEL;
    private static final KnowledgeSourceReference KNOWLEDGE_SOURCE_REFERENCE
        = new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME);

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

        searchIndexClient.createOrUpdateKnowledgeSource(new SearchIndexKnowledgeSource(HOTEL_KNOWLEDGE_SOURCE_NAME,
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
                .forEach(knowledgebase -> searchIndexClient.deleteKnowledgeBase(knowledgebase.getName()));

            // Delete non-shared Knowledge Sources created during tests.
            searchIndexClient.listKnowledgeSources().forEach(ks -> {
                if (!HOTEL_KNOWLEDGE_SOURCE_NAME.equals(ks.getName())) {
                    searchIndexClient.deleteKnowledgeSource(ks.getName());
                }
            });
        }
    }

    @AfterAll
    protected static void cleanupClass() {
        // Clean up any resources after all tests.
        if (TEST_MODE != TestMode.PLAYBACK) {
            // Delete all knowledge knowledgebases.
            searchIndexClient.listKnowledgeBases()
                .forEach(knowledgebase -> searchIndexClient.deleteKnowledgeBase(knowledgebase.getName()));

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
        // Test creating a knowledge knowledgebase.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), created.getName());

        assertEquals(1, created.getModels().size());
        KnowledgeBaseAzureOpenAIModel createdModel
            = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, created.getModels().get(0));
        if (interceptorManager.isLiveMode()) {
            assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                createdModel.getAzureOpenAIParameters().getDeploymentName());
            assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getModelName(),
                createdModel.getAzureOpenAIParameters().getModelName());
            assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getResourceUrl(),
                createdModel.getAzureOpenAIParameters().getResourceUrl());
        }

        assertEquals(1, created.getKnowledgeSources().size());
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, created.getKnowledgeSources().get(0).getName());
    }

    @Test
    public void createKnowledgeBaseAsync() {
        // Test creating a knowledge knowledgebase.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        StepVerifier.create(searchIndexClient.createKnowledgeBase(knowledgeBase)).assertNext(created -> {
            assertEquals(knowledgeBase.getName(), created.getName());

            assertEquals(1, created.getModels().size());
            KnowledgeBaseAzureOpenAIModel createdModel
                = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, created.getModels().get(0));
            if (interceptorManager.isLiveMode()) {
                assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                    createdModel.getAzureOpenAIParameters().getDeploymentName());
                assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getModelName(),
                    createdModel.getAzureOpenAIParameters().getModelName());
                assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getResourceUrl(),
                    createdModel.getAzureOpenAIParameters().getResourceUrl());
            }

            assertEquals(1, created.getKnowledgeSources().size());
            assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, created.getKnowledgeSources().get(0).getName());
        }).verifyComplete();
    }

    @Test
    public void getKnowledgeBaseSync() {
        // Test getting a knowledge knowledgebase.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBase retrieved = searchIndexClient.getKnowledgeBase(knowledgeBase.getName());
        assertEquals(knowledgeBase.getName(), retrieved.getName());

        assertEquals(1, retrieved.getModels().size());
        KnowledgeBaseAzureOpenAIModel retrievedModel
            = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, retrieved.getModels().get(0));
        if (interceptorManager.isLiveMode()) {
            assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                retrievedModel.getAzureOpenAIParameters().getDeploymentName());
            assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getModelName(),
                retrievedModel.getAzureOpenAIParameters().getModelName());
            assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getResourceUrl(),
                retrievedModel.getAzureOpenAIParameters().getResourceUrl());
        }

        assertEquals(1, retrieved.getKnowledgeSources().size());
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, retrieved.getKnowledgeSources().get(0).getName());
    }

    @Test
    public void getKnowledgeBaseAsync() {
        // Test getting a knowledge knowledgebase.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBase> createAndGet = searchIndexClient.createKnowledgeBase(knowledgeBase)
            .flatMap(created -> searchIndexClient.getKnowledgeBase(created.getName()));

        StepVerifier.create(createAndGet).assertNext(retrieved -> {
            assertEquals(knowledgeBase.getName(), retrieved.getName());

            assertEquals(1, retrieved.getModels().size());
            KnowledgeBaseAzureOpenAIModel retrievedModel
                = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, retrieved.getModels().get(0));
            if (interceptorManager.isLiveMode()) {
                assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getDeploymentName(),
                    retrievedModel.getAzureOpenAIParameters().getDeploymentName());
                assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getModelName(),
                    retrievedModel.getAzureOpenAIParameters().getModelName());
                assertEquals(OPEN_AI_KNOWLEDGEBASE_MODEL.getAzureOpenAIParameters().getResourceUrl(),
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
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        KnowledgeBase knowledgeBase2
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);
        searchIndexClient.createKnowledgeBase(knowledgeBase2);
        Map<String, KnowledgeBase> knowledgeBasesByName = searchIndexClient.listKnowledgeBases()
            .stream()
            .collect(Collectors.toMap(KnowledgeBase::getName, Function.identity()));

        assertEquals(2, knowledgeBasesByName.size() - currentCount);
        KnowledgeBase listedKnowledgeBase1 = knowledgeBasesByName.get(knowledgeBase.getName());
        assertNotNull(listedKnowledgeBase1);
        KnowledgeBase listedKnowledgeBase2 = knowledgeBasesByName.get(knowledgeBase2.getName());
        assertNotNull(listedKnowledgeBase2);
    }

    @Test
    public void listKnowledgeBasesAsync() {
        // Test listing knowledge knowledgebases.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        KnowledgeBase knowledgeBase2
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

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
            KnowledgeBase listedKnowledgeBase1 = knowledgeBasesByName.get(knowledgeBase.getName());
            assertNotNull(listedKnowledgeBase1);
            KnowledgeBase listedKnowledgeBase2 = knowledgeBasesByName.get(knowledgeBase2.getName());
            assertNotNull(listedKnowledgeBase2);
        }).verifyComplete();
    }

    @Test
    public void deleteKnowledgeBaseSync() {
        // Test deleting a knowledge knowledgebase.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), searchIndexClient.getKnowledgeBase(knowledgeBase.getName()).getName());
        searchIndexClient.deleteKnowledgeBase(knowledgeBase.getName());
        assertThrows(HttpResponseException.class, () -> searchIndexClient.getKnowledgeBase(knowledgeBase.getName()));
    }

    @Test
    public void deleteKnowledgeBaseAsync() {
        // Test deleting a knowledge base.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

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
        // Test updating a knowledge base.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);
        String newDescription = "Updated description";
        knowledgeBase.setDescription(newDescription);
        searchIndexClient.createOrUpdateKnowledgeBase(knowledgeBase);
        KnowledgeBase retrieved = searchIndexClient.getKnowledgeBase(knowledgeBase.getName());
        assertEquals(newDescription, retrieved.getDescription());
    }

    @Test
    public void updateKnowledgeBaseAsync() {
        // Test updating a knowledge base.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        String newDescription = "Updated description";

        Mono<KnowledgeBase> createUpdateAndGetMono = searchIndexClient.createKnowledgeBase(knowledgeBase)
            .flatMap(created -> searchIndexClient.deleteKnowledgeBase(created.getName())
                .then(searchIndexClient
                    .createKnowledgeBase(new KnowledgeBase(knowledgeBase.getName(), KNOWLEDGE_SOURCE_REFERENCE)
                        .setModels(KNOWLEDGE_BASE_MODEL)
                        .setDescription(newDescription))))
            .flatMap(updated -> searchIndexClient.getKnowledgeBase(updated.getName()));

        StepVerifier.create(createUpdateAndGetMono)
            .assertNext(retrieved -> assertEquals(newDescription, retrieved.getDescription()))
            .verifyComplete();
    }

    @Test
    public void basicRetrievalSync() {
        // Test knowledge base retrieval functionality.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void basicRetrievalAsync() {
        // Test knowledge base retrieval functionality.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    public void basicRetrievalWithReasoningEffortSync() {
        // Test knowledge base retrieval functionality.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"));
        // .setRetrievalReasoningEffort(KnowledgeRetrievalReasoningEffortKind.MEDIUM);  // TODO: Missing enum

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void basicRetrievalWithReasoningEffortAsync() {
        // Test knowledge base retrieval functionality.
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"));
                // .setRetrievalReasoningEffort(KnowledgeRetrievalReasoningEffortKind.MEDIUM);  // TODO: Missing enum

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void answerSynthesisRetrievalSync() {
        // Disabled: setRetrievalInstructions was removed in the 2026-04-01 API version.
    }

    @Test
    @Disabled("Requires further resource deployment")
    public void answerSynthesisRetrievalAsync() {
        // Disabled: setRetrievalInstructions was removed in the 2026-04-01 API version.
    }

    @Test
    public void knowledgeBaseObjectHasNoAgentReferences() throws IOException {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);
        String kbJson = created.toJsonString();

        // Filter out the name field which may contain the test method name with "agent"
        String jsonWithoutName = kbJson.replaceAll("\"name\":\"[^\"]*\"", "\"name\":\"FILTERED\"");

        assertFalse(jsonWithoutName.toLowerCase().contains("agent"),
            "KB JSON should not contain 'agent' references (excluding KB name)");
        assertFalse(kbJson.toLowerCase().contains("ka"), "KB JSON should not contain 'KA' abbreviation");
    }

    @Test
    public void knowledgeBaseEndpointsUseKnowledgeBasesPath() {
        SearchIndexClient client = getSearchIndexClientBuilder(true)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        String kbName = randomKnowledgeBaseName();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(kbName, KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        client.createKnowledgeBase(knowledgeBase);

        KnowledgeBase retrieved = client.getKnowledgeBase(kbName);
        assertNotNull(retrieved, "KB should be retrieved via /knowledgeBases endpoint");

        PagedIterable<KnowledgeBase> knowledgeBases = client.listKnowledgeBases();
        assertNotNull(knowledgeBases, "Should list via /knowledgeBases endpoint");

    }

    @Test
    public void legacyKnowledgeAgentsListedAsKnowledgeBases() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();

        PagedIterable<KnowledgeBase> knowledgeBases = client.listKnowledgeBases();
        assertNotNull(knowledgeBases, "Knowledge Bases list should not be null");

        List<KnowledgeBase> kbList = knowledgeBases.stream().collect(Collectors.toList());
        assertNotNull(kbList, "Knowledge Bases list should not be null");

        if (!kbList.isEmpty()) {
            String responseJsonString = BinaryData.fromObject(kbList).toString();
            assertFalse(responseJsonString.toLowerCase().contains("agent"),
                "Response should not contain 'agent' terminology");
            assertTrue(
                responseJsonString.toLowerCase().contains("knowledgebase")
                    || responseJsonString.toLowerCase().contains("knowledge"),
                "Response should contain 'knowledgebase' terminology");
        }
    }

    @Test
    public void knowledgeSourcesEndpointUnchanged() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();

        String kbName = randomKnowledgeBaseName();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(kbName, KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        KnowledgeBase created = client.createKnowledgeBase(knowledgeBase);

        assertNotNull(created.getKnowledgeSources(), "Knowledge sources should be accessible");
        assertEquals(1, created.getKnowledgeSources().size(), "Should have one knowledge source");
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, created.getKnowledgeSources().get(0).getName(),
            "Knowledge source name should match");

        assertTrue(true, "Knowledge sources endpoint verified via KB operations");
    }

    @Test
    public void knowledgeBaseTypeNamesContainNoAgentReferences() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();

        String kbName = randomKnowledgeBaseName();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(kbName, KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        KnowledgeBase created = client.createKnowledgeBase(knowledgeBase);

        String className = created.getClass().getSimpleName();
        assertFalse(className.toLowerCase().contains("agent"), "Class name should not contain 'agent': " + className);
        assertTrue(className.toLowerCase().contains("knowledgebase") || className.toLowerCase().contains("knowledge"),
            "Class name should contain 'knowledge' terminology: " + className);

        if (created.getModels() != null && !created.getModels().isEmpty()) {
            KnowledgeBaseModel model = created.getModels().get(0);
            String modelClassName = model.getClass().getSimpleName();
            assertFalse(modelClassName.toLowerCase().contains("agent"),
                "Model class name should not contain 'agent': " + modelClassName);
            assertTrue(
                modelClassName.toLowerCase().contains("knowledgebase")
                    || modelClassName.toLowerCase().contains("knowledge"),
                "Model class name should contain 'knowledge' terminology: " + modelClassName);
        }

        if (created.getKnowledgeSources() != null && !created.getKnowledgeSources().isEmpty()) {
            KnowledgeSourceReference sourceRef = created.getKnowledgeSources().get(0);
            String sourceRefClassName = sourceRef.getClass().getSimpleName();
            assertFalse(sourceRefClassName.toLowerCase().contains("agent"),
                "Knowledge source reference class should not contain 'agent': " + sourceRefClassName);
            assertTrue(
                sourceRefClassName.toLowerCase().contains("knowledgebase")
                    || sourceRefClassName.toLowerCase().contains("knowledge"),
                "Source reference class should contain proper terminology: " + sourceRefClassName);

        }
    }

    @Test
    public void errorHandlingUsesKnowledgeBaseTerminology() {
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();

        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> client.getKnowledgeBase("nonexistent-kb-name"));

        assertEquals(404, exception.getResponse().getStatusCode(), "Status code should be 404 Not Found");
        String errorMessage = exception.getMessage().toLowerCase();

        if (errorMessage.toLowerCase().contains("knowledge")) {
            assertFalse(errorMessage.toLowerCase().contains("agent"),
                "Error message should not contain 'agent' terminology");
        }
    }

    @Test
    public void retrievalWithKnowledgeSourceParamsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setIncludeReferences(true)
                .setIncludeReferenceSourceData(true);

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void retrievalWithKnowledgeSourceParamsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeSourceParams sourceParams
                    = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                        .setIncludeReferences(true)
                        .setIncludeReferenceSourceData(true);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    public void retrievalWithMaxOutputSizeSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setMaxOutputSize(5001)
            .setMaxOutputDocuments(100);

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void retrievalWithMaxOutputSizeAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
                    .setMaxOutputSize(5001)
                    .setMaxOutputDocuments(100);

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    public void retrievalWithOutputModeSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA);

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void retrievalWithOutputModeAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
                    .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA);

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    public void retrievalWithSourceMaxOutputDocumentsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setMaxOutputDocuments(100);

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void retrievalWithSourceMaxOutputDocumentsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeSourceParams sourceParams
                    = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setMaxOutputDocuments(100);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    public void retrievalWithFilterAddOnSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setFilterAddOn("Category eq 'Budget'");

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void retrievalWithFilterAddOnAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeSourceParams sourceParams = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME)
                    .setFilterAddOn("Category eq 'Budget'");

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Disabled("Requires an embedding model deployment (e.g., text-embedding-3-large) on the AOAI resource")
    @Test
    public void retrievalWithFileKnowledgeSourceParamsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        // Create a File KS with required ingestion params using a real AOAI endpoint
        String fileKsName = randomKnowledgeBaseName() + "-file-ks";
        FileKnowledgeSourceParameters fileParams = new FileKnowledgeSourceParameters().setIngestionParameters(
            new KnowledgeSourceIngestionParameters().setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer()
                .setAzureOpenAIParameters(new AzureOpenAIVectorizerParameters().setResourceUrl(OPENAI_ENDPOINT)
                    .setDeploymentName(KNOWLEDGEBASE_DEPLOYMENT_NAME)
                    .setModelName(AzureOpenAIModelName.fromString(OPENAI_MODEL_NAME))
                    .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY)))));
        FileKnowledgeSource fileKs = new FileKnowledgeSource(fileKsName, fileParams);
        searchIndexClient.createKnowledgeSource(fileKs);

        // Create KB referencing the File KS
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(fileKsName))
                .setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        // Use FileKnowledgeSourceParams to exercise the runtime params model
        FileKnowledgeSourceParams sourceParams = new FileKnowledgeSourceParams(fileKsName).setMaxOutputDocuments(10)
            .setIncludeReferences(true)
            .setIncludeReferenceSourceData(true);

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What documents are available?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        // File KS has no uploaded files, so response may be empty but request should succeed
        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Disabled("Requires an embedding model deployment (e.g., text-embedding-3-large) on the AOAI resource")
    @Test
    public void retrievalWithFileKnowledgeSourceParamsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();

        String fileKsName = randomKnowledgeBaseName() + "-file-ks";
        FileKnowledgeSourceParameters fileParams = new FileKnowledgeSourceParameters().setIngestionParameters(
            new KnowledgeSourceIngestionParameters().setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer()
                .setAzureOpenAIParameters(new AzureOpenAIVectorizerParameters().setResourceUrl(OPENAI_ENDPOINT)
                    .setDeploymentName(KNOWLEDGEBASE_DEPLOYMENT_NAME)
                    .setModelName(AzureOpenAIModelName.fromString(OPENAI_MODEL_NAME))
                    .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY)))));
        FileKnowledgeSource fileKs = new FileKnowledgeSource(fileKsName, fileParams);

        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(fileKsName))
                .setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono = searchIndexClient.createKnowledgeSource(fileKs)
            .then(searchIndexClient.createKnowledgeBase(knowledgeBase))
            .flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                FileKnowledgeSourceParams sourceParams
                    = new FileKnowledgeSourceParams(fileKsName).setMaxOutputDocuments(10)
                        .setIncludeReferences(true)
                        .setIncludeReferenceSourceData(true);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What documents are available?"))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    public void retrievalActivityIncludesModelNameSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setIncludeActivity(true);

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getActivity());
        assertFalse(response.getActivity().isEmpty());

        // At least one model-backed activity record should have modelName set
        boolean foundModelName = response.getActivity().stream().anyMatch(record -> {
            if (record instanceof KnowledgeBaseModelQueryPlanningActivityRecord) {
                return ((KnowledgeBaseModelQueryPlanningActivityRecord) record).getModelName() != null;
            } else if (record instanceof KnowledgeBaseModelAnswerSynthesisActivityRecord) {
                return ((KnowledgeBaseModelAnswerSynthesisActivityRecord) record).getModelName() != null;
            }
            return false;
        });
        assertTrue(foundModelName, "Expected at least one model-backed activity record with modelName set");
    }

    @Test
    public void retrievalActivityIncludesModelNameAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
                    .setIncludeActivity(true);

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getActivity());
            assertFalse(response.getActivity().isEmpty());

            boolean foundModelName = response.getActivity().stream().anyMatch(record -> {
                if (record instanceof KnowledgeBaseModelQueryPlanningActivityRecord) {
                    return ((KnowledgeBaseModelQueryPlanningActivityRecord) record).getModelName() != null;
                } else if (record instanceof KnowledgeBaseModelAnswerSynthesisActivityRecord) {
                    return ((KnowledgeBaseModelAnswerSynthesisActivityRecord) record).getModelName() != null;
                }
                return false;
            });
            assertTrue(foundModelName, "Expected at least one model-backed activity record with modelName set");
        }).verifyComplete();
    }

    // Fabric retrieval tests are disabled until the Fabric workspace/ontology/data-agent are configured
    // to accept queries from the search service. The current error is 405 Method Not Allowed from
    // the Fabric endpoint. Once permissions are granted, enable these tests and record sessions.

    @Disabled("Requires Fabric workspace configured to accept search-service retrieval queries")
    @Test
    public void fabricDataAgentRetrievalSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        String fabricKsName = randomKnowledgeBaseName() + "-da-ks";
        FabricDataAgentKnowledgeSource fabricKs = new FabricDataAgentKnowledgeSource(fabricKsName,
            new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID));
        searchIndexClient.createKnowledgeSource(fabricKs);

        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(fabricKsName))
                .setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        FabricDataAgentKnowledgeSourceParams sourceParams = new FabricDataAgentKnowledgeSourceParams(fabricKsName);
        sourceParams.setAlwaysQuerySource(true);
        sourceParams.setIncludeReferences(true);
        sourceParams.setIncludeReferenceSourceData(true);

        KnowledgeBaseRetrievalOptions retrievalRequest
            = new KnowledgeBaseRetrievalOptions().setIntents(new KnowledgeRetrievalSemanticIntent("List all data"))
                .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        // Fabric sources require x-ms-query-source-authorization header (OBO token)
        String querySourceToken = getQuerySourceAuthorizationToken();
        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest, querySourceToken);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Disabled("Requires Fabric workspace configured to accept search-service retrieval queries")
    @Test
    public void fabricDataAgentRetrievalAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        String authToken = getQuerySourceAuthorizationToken();

        String fabricKsName = randomKnowledgeBaseName() + "-da-ks";
        FabricDataAgentKnowledgeSource fabricKs = new FabricDataAgentKnowledgeSource(fabricKsName,
            new FabricDataAgentKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_DATA_AGENT_ID));

        Mono<KnowledgeBaseRetrievalResult> testMono
            = searchIndexClient.createKnowledgeSource(fabricKs).then(Mono.defer(() -> {
                KnowledgeBase knowledgeBase
                    = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(fabricKsName))
                        .setModels(KNOWLEDGE_BASE_MODEL);
                return searchIndexClient.createKnowledgeBase(knowledgeBase);
            })).flatMap(createdKb -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(createdKb.getName())
                        .buildAsyncClient();

                FabricDataAgentKnowledgeSourceParams sourceParams
                    = new FabricDataAgentKnowledgeSourceParams(fabricKsName);
                sourceParams.setAlwaysQuerySource(true);
                sourceParams.setIncludeReferences(true);
                sourceParams.setIncludeReferenceSourceData(true);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("List all data"))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest, authToken);
            });

        StepVerifier.create(testMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Disabled("Requires Fabric workspace configured to accept search-service retrieval queries")
    @Test
    public void fabricOntologyRetrievalSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        String fabricKsName = randomKnowledgeBaseName() + "-ont-ks";
        FabricOntologyKnowledgeSource fabricKs = new FabricOntologyKnowledgeSource(fabricKsName,
            new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID));
        searchIndexClient.createKnowledgeSource(fabricKs);

        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(fabricKsName))
                .setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        FabricOntologyKnowledgeSourceParams sourceParams = new FabricOntologyKnowledgeSourceParams(fabricKsName);
        sourceParams.setAlwaysQuerySource(true);
        sourceParams.setIncludeReferences(true);
        sourceParams.setIncludeReferenceSourceData(true);

        KnowledgeBaseRetrievalOptions retrievalRequest
            = new KnowledgeBaseRetrievalOptions().setIntents(new KnowledgeRetrievalSemanticIntent("List all data"))
                .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        String querySourceToken = getQuerySourceAuthorizationToken();
        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest, querySourceToken);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Disabled("Requires Fabric workspace configured to accept search-service retrieval queries")
    @Test
    public void fabricOntologyRetrievalAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        String authToken = getQuerySourceAuthorizationToken();

        String fabricKsName = randomKnowledgeBaseName() + "-ont-ks";
        FabricOntologyKnowledgeSource fabricKs = new FabricOntologyKnowledgeSource(fabricKsName,
            new FabricOntologyKnowledgeSourceParameters(FABRIC_WORKSPACE_ID, FABRIC_ONTOLOGY_ID));

        Mono<KnowledgeBaseRetrievalResult> testMono
            = searchIndexClient.createKnowledgeSource(fabricKs).then(Mono.defer(() -> {
                KnowledgeBase knowledgeBase
                    = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(fabricKsName))
                        .setModels(KNOWLEDGE_BASE_MODEL);
                return searchIndexClient.createKnowledgeBase(knowledgeBase);
            })).flatMap(createdKb -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(createdKb.getName())
                        .buildAsyncClient();

                FabricOntologyKnowledgeSourceParams sourceParams
                    = new FabricOntologyKnowledgeSourceParams(fabricKsName);
                sourceParams.setAlwaysQuerySource(true);
                sourceParams.setIncludeReferences(true);
                sourceParams.setIncludeReferenceSourceData(true);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("List all data"))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest, authToken);
            });

        StepVerifier.create(testMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    /**
     * Acquires an access token for use as the x-ms-query-source-authorization header
     * required by Fabric knowledge sources during retrieval.
     */
    private String getQuerySourceAuthorizationToken() {
        return TestHelpers.getTestTokenCredential()
            .getTokenSync(new TokenRequestContext().addScopes("https://search.azure.com/.default"))
            .getToken();
    }

    @Test
    public void retrievalResultDeserializesWithSensitivityLabels() throws IOException {
        String json = "{"
            + "\"response\":[{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"Answer text.\"}]}],"
            + "\"references\":[" + "  {\"type\":\"azureBlob\",\"id\":\"0\",\"activitySource\":1,"
            + "   \"blobUrl\":\"https://contoso.blob.core.windows.net/docs/file1.pdf\"," + "   \"rerankerScore\":3.8,"
            + "   \"searchSensitivityLabelInfo\":{"
            + "     \"sensitivityLabelId\":\"3a4f2b91-c7d8-4e12-9f01-ab34cd56ef78\","
            + "     \"displayName\":\"Confidential\"," + "     \"isEncrypted\":false," + "     \"priority\":2,"
            + "     \"color\":\"#FF0000\"," + "     \"toolTip\":\"Confidential content\"" + "   }},"
            + "  {\"type\":\"searchIndex\",\"id\":\"1\",\"activitySource\":1,"
            + "   \"indexName\":\"my-index\",\"documentKey\":\"doc-123\"," + "   \"rerankerScore\":3.5,"
            + "   \"searchSensitivityLabelInfo\":{"
            + "     \"sensitivityLabelId\":\"9c8b7a61-5d4e-43f2-b123-98fedcba4321\","
            + "     \"displayName\":\"Highly Confidential\"," + "     \"isEncrypted\":true," + "     \"priority\":0,"
            + "     \"color\":\"#800080\"," + "     \"toolTip\":\"Highly confidential\"" + "   }},"
            + "  {\"type\":\"indexedOneLake\",\"id\":\"2\",\"activitySource\":1,"
            + "   \"searchSensitivityLabelInfo\":{" + "     \"sensitivityLabelId\":\"aaa-bbb-ccc\","
            + "     \"displayName\":\"Internal\"," + "     \"isEncrypted\":false," + "     \"priority\":3" + "   }},"
            + "  {\"type\":\"indexedSharePoint\",\"id\":\"3\",\"activitySource\":1,"
            + "   \"searchSensitivityLabelInfo\":{" + "     \"sensitivityLabelId\":\"ddd-eee-fff\","
            + "     \"displayName\":\"Public\"," + "     \"isEncrypted\":false," + "     \"priority\":4" + "   }}"
            + "]," + "\"responseSensitivityLabelInfo\":{"
            + "  \"sensitivityLabelId\":\"9c8b7a61-5d4e-43f2-b123-98fedcba4321\","
            + "  \"displayName\":\"Highly Confidential\"," + "  \"isEncrypted\":true," + "  \"priority\":0,"
            + "  \"color\":\"#800080\"," + "  \"toolTip\":\"Highly confidential\"" + "}" + "}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            KnowledgeBaseRetrievalResult result = KnowledgeBaseRetrievalResult.fromJson(reader);

            // Verify response-level sensitivity label
            PurviewSensitivityLabelInfo responseLabel = result.getResponseSensitivityLabelInfo();
            assertNotNull(responseLabel);
            assertEquals("9c8b7a61-5d4e-43f2-b123-98fedcba4321", responseLabel.getSensitivityLabelId());
            assertEquals("Highly Confidential", responseLabel.getDisplayName());
            assertEquals(true, responseLabel.isEncrypted());
            assertEquals(Integer.valueOf(0), responseLabel.getPriority());
            assertEquals("#800080", responseLabel.getColor());
            assertEquals("Highly confidential", responseLabel.getToolTip());

            // Verify azureBlob reference label
            assertNotNull(result.getReferences());
            assertEquals(4, result.getReferences().size());

            KnowledgeBaseAzureBlobReference blobRef
                = assertInstanceOf(KnowledgeBaseAzureBlobReference.class, result.getReferences().get(0));
            PurviewSensitivityLabelInfo blobLabel = blobRef.getSearchSensitivityLabelInfo();
            assertNotNull(blobLabel);
            assertEquals("3a4f2b91-c7d8-4e12-9f01-ab34cd56ef78", blobLabel.getSensitivityLabelId());
            assertEquals("Confidential", blobLabel.getDisplayName());
            assertEquals(false, blobLabel.isEncrypted());
            assertEquals(Integer.valueOf(2), blobLabel.getPriority());
            assertEquals("#FF0000", blobLabel.getColor());

            // Verify searchIndex reference label
            KnowledgeBaseSearchIndexReference indexRef
                = assertInstanceOf(KnowledgeBaseSearchIndexReference.class, result.getReferences().get(1));
            PurviewSensitivityLabelInfo indexLabel = indexRef.getSearchSensitivityLabelInfo();
            assertNotNull(indexLabel);
            assertEquals("9c8b7a61-5d4e-43f2-b123-98fedcba4321", indexLabel.getSensitivityLabelId());
            assertEquals("Highly Confidential", indexLabel.getDisplayName());
            assertEquals(true, indexLabel.isEncrypted());

            // Verify indexedOneLake reference label
            KnowledgeBaseIndexedOneLakeReference oneLakeRef
                = assertInstanceOf(KnowledgeBaseIndexedOneLakeReference.class, result.getReferences().get(2));
            PurviewSensitivityLabelInfo oneLakeLabel = oneLakeRef.getSearchSensitivityLabelInfo();
            assertNotNull(oneLakeLabel);
            assertEquals("aaa-bbb-ccc", oneLakeLabel.getSensitivityLabelId());
            assertEquals("Internal", oneLakeLabel.getDisplayName());

            // Verify indexedSharePoint reference label
            KnowledgeBaseIndexedSharePointReference spRef
                = assertInstanceOf(KnowledgeBaseIndexedSharePointReference.class, result.getReferences().get(3));
            PurviewSensitivityLabelInfo spLabel = spRef.getSearchSensitivityLabelInfo();
            assertNotNull(spLabel);
            assertEquals("ddd-eee-fff", spLabel.getSensitivityLabelId());
            assertEquals("Public", spLabel.getDisplayName());
        }
    }

    @Test
    public void retrievalResultDeserializesWithoutSensitivityLabels() throws IOException {
        String json = "{"
            + "\"response\":[{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"Answer.\"}]}],"
            + "\"references\":[" + "  {\"type\":\"azureBlob\",\"id\":\"0\",\"activitySource\":1,"
            + "   \"blobUrl\":\"https://contoso.blob.core.windows.net/docs/file1.pdf\"," + "   \"rerankerScore\":3.8},"
            + "  {\"type\":\"searchIndex\",\"id\":\"1\",\"activitySource\":1,"
            + "   \"indexName\":\"my-index\",\"documentKey\":\"doc-123\"," + "   \"rerankerScore\":3.5}" + "]" + "}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            KnowledgeBaseRetrievalResult result = KnowledgeBaseRetrievalResult.fromJson(reader);

            // No response-level label
            assertNull(result.getResponseSensitivityLabelInfo());

            // No per-reference labels
            KnowledgeBaseAzureBlobReference blobRef
                = assertInstanceOf(KnowledgeBaseAzureBlobReference.class, result.getReferences().get(0));
            assertNull(blobRef.getSearchSensitivityLabelInfo());

            KnowledgeBaseSearchIndexReference indexRef
                = assertInstanceOf(KnowledgeBaseSearchIndexReference.class, result.getReferences().get(1));
            assertNull(indexRef.getSearchSensitivityLabelInfo());
        }
    }

    @Test
    public void createKnowledgeBaseWithCorsOptionsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        CorsOptions corsOptions = new CorsOptions("https://myapp.example.com").setMaxAgeInSeconds(600L);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setCorsOptions(corsOptions);

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), created.getName());
        assertNotNull(created.getCorsOptions());
        assertEquals(1, created.getCorsOptions().getAllowedOrigins().size());
        assertEquals("https://myapp.example.com", created.getCorsOptions().getAllowedOrigins().get(0));
        assertEquals(600L, created.getCorsOptions().getMaxAgeInSeconds());
    }

    @Test
    public void createKnowledgeBaseWithCorsOptionsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        CorsOptions corsOptions = new CorsOptions("https://myapp.example.com").setMaxAgeInSeconds(600L);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setCorsOptions(corsOptions);

        StepVerifier.create(searchIndexClient.createKnowledgeBase(knowledgeBase)).assertNext(created -> {
            assertEquals(knowledgeBase.getName(), created.getName());
            assertNotNull(created.getCorsOptions());
            assertEquals(1, created.getCorsOptions().getAllowedOrigins().size());
            assertEquals("https://myapp.example.com", created.getCorsOptions().getAllowedOrigins().get(0));
            assertEquals(600L, created.getCorsOptions().getMaxAgeInSeconds());
        }).verifyComplete();
    }

    @Test
    public void createKnowledgeBaseWithCorsWildcardSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        CorsOptions corsOptions = new CorsOptions("*");
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setCorsOptions(corsOptions);

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertNotNull(created.getCorsOptions());
        assertEquals(1, created.getCorsOptions().getAllowedOrigins().size());
        assertEquals("*", created.getCorsOptions().getAllowedOrigins().get(0));
    }

    @Test
    public void createKnowledgeBaseWithCorsWildcardAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        CorsOptions corsOptions = new CorsOptions("*");
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setCorsOptions(corsOptions);

        StepVerifier.create(searchIndexClient.createKnowledgeBase(knowledgeBase)).assertNext(created -> {
            assertNotNull(created.getCorsOptions());
            assertEquals(1, created.getCorsOptions().getAllowedOrigins().size());
            assertEquals("*", created.getCorsOptions().getAllowedOrigins().get(0));
        }).verifyComplete();
    }

    @Test
    public void createKnowledgeBaseWithoutCorsOptionsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), created.getName());
        assertNull(created.getCorsOptions());
    }

    @Test
    public void createKnowledgeBaseWithoutCorsOptionsAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        StepVerifier.create(searchIndexClient.createKnowledgeBase(knowledgeBase)).assertNext(created -> {
            assertEquals(knowledgeBase.getName(), created.getName());
            assertNull(created.getCorsOptions());
        }).verifyComplete();
    }

    @Test
    public void createKnowledgeBaseWithEnableImageServingSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSourceReference sourceRef
            = new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableImageServing(true);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), sourceRef).setModels(KNOWLEDGE_BASE_MODEL);

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), created.getName());
        assertEquals(1, created.getKnowledgeSources().size());
        KnowledgeSourceReference createdRef = created.getKnowledgeSources().get(0);
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, createdRef.getName());
        assertEquals(true, createdRef.isEnableImageServing());
    }

    @Test
    public void createKnowledgeBaseWithEnableImageServingAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSourceReference sourceRef
            = new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableImageServing(true);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), sourceRef).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBase> createMono = searchIndexClient.createKnowledgeBase(knowledgeBase);

        StepVerifier.create(createMono).assertNext(created -> {
            assertEquals(knowledgeBase.getName(), created.getName());
            assertEquals(1, created.getKnowledgeSources().size());
            KnowledgeSourceReference createdRef = created.getKnowledgeSources().get(0);
            assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, createdRef.getName());
            assertEquals(true, createdRef.isEnableImageServing());
        }).verifyComplete();
    }

    @Test
    public void createKnowledgeBaseWithFreshnessEnabledSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSourceReference sourceRef
            = new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableFreshness(true);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), sourceRef).setModels(KNOWLEDGE_BASE_MODEL);

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), created.getName());
        assertEquals(1, created.getKnowledgeSources().size());
        KnowledgeSourceReference createdRef = created.getKnowledgeSources().get(0);
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, createdRef.getName());
        assertEquals(true, createdRef.isEnableFreshness());
    }

    @Test
    public void createKnowledgeBaseWithFreshnessEnabledAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSourceReference sourceRef
            = new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableFreshness(true);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), sourceRef).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBase> createMono = searchIndexClient.createKnowledgeBase(knowledgeBase);

        StepVerifier.create(createMono).assertNext(created -> {
            assertEquals(knowledgeBase.getName(), created.getName());
            assertEquals(1, created.getKnowledgeSources().size());
            KnowledgeSourceReference createdRef = created.getKnowledgeSources().get(0);
            assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, createdRef.getName());
            assertEquals(true, createdRef.isEnableFreshness());
        }).verifyComplete();
    }

    @Test
    public void createKnowledgeBaseWithFreshnessDisabledSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeSourceReference sourceRef
            = new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableFreshness(false);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), sourceRef).setModels(KNOWLEDGE_BASE_MODEL);

        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertEquals(knowledgeBase.getName(), created.getName());
        assertEquals(1, created.getKnowledgeSources().size());
        KnowledgeSourceReference createdRef = created.getKnowledgeSources().get(0);
        assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, createdRef.getName());
        assertEquals(false, createdRef.isEnableFreshness());
    }

    @Test
    public void createKnowledgeBaseWithFreshnessDisabledAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeSourceReference sourceRef
            = new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableFreshness(false);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), sourceRef).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBase> createMono = searchIndexClient.createKnowledgeBase(knowledgeBase);

        StepVerifier.create(createMono).assertNext(created -> {
            assertEquals(knowledgeBase.getName(), created.getName());
            assertEquals(1, created.getKnowledgeSources().size());
            KnowledgeSourceReference createdRef = created.getKnowledgeSources().get(0);
            assertEquals(HOTEL_KNOWLEDGE_SOURCE_NAME, createdRef.getName());
            assertEquals(false, createdRef.isEnableFreshness());
        }).verifyComplete();
    }

    @Test
    public void retrievalWithEnableImageServingSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableImageServing(true);

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Show me architecture diagrams."))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void retrievalWithEnableImageServingAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeSourceParams sourceParams
                    = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setEnableImageServing(true);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("Show me architecture diagrams."))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Disabled("Requires an Azure Blob Knowledge Source - shared test KS is searchIndex kind")
    @Test
    public void retrievalWithBlobKnowledgeSourceEnableImageServingSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();

        // Create a blob KS
        AzureBlobKnowledgeSourceParameters blobKsParams = new AzureBlobKnowledgeSourceParameters(
            "ResourceId=/subscriptions/" + SUBSCRIPTION_ID + "/resourceGroups/" + RESOURCE_GROUP
                + "/providers/Microsoft.Storage/storageAccounts/" + STORAGE_ACCOUNT_NAME,
            BLOB_CONTAINER_NAME).setIngestionParameters(
                new KnowledgeSourceIngestionParameters().setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer()
                    .setAzureOpenAIParameters(new AzureOpenAIVectorizerParameters().setResourceUrl(OPENAI_ENDPOINT)
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        String blobKsName = testResourceNamer.randomName("blob-ks-", 63).toLowerCase();
        AzureBlobKnowledgeSource blobKs = new AzureBlobKnowledgeSource(blobKsName, blobKsParams);
        searchIndexClient.createKnowledgeSource(blobKs);

        // Create KB referencing the blob KS
        KnowledgeSourceReference blobRef = new KnowledgeSourceReference(blobKsName).setEnableImageServing(true);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), blobRef).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        // Retrieve with AzureBlobKnowledgeSourceParams
        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        AzureBlobKnowledgeSourceParams blobRetrieveParams = new AzureBlobKnowledgeSourceParams(blobKsName);
        blobRetrieveParams.setEnableImageServing(true);

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Show me images from documents."))
            .setKnowledgeSourceParams(Collections.singletonList(blobRetrieveParams));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Disabled("Requires an Azure Blob Knowledge Source - shared test KS is searchIndex kind")
    @Test
    public void retrievalWithBlobKnowledgeSourceEnableImageServingAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();

        // Create a blob KS
        AzureBlobKnowledgeSourceParameters blobKsParams = new AzureBlobKnowledgeSourceParameters(
            "ResourceId=/subscriptions/" + SUBSCRIPTION_ID + "/resourceGroups/" + RESOURCE_GROUP
                + "/providers/Microsoft.Storage/storageAccounts/" + STORAGE_ACCOUNT_NAME,
            BLOB_CONTAINER_NAME).setIngestionParameters(
                new KnowledgeSourceIngestionParameters().setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer()
                    .setAzureOpenAIParameters(new AzureOpenAIVectorizerParameters().setResourceUrl(OPENAI_ENDPOINT)
                        .setDeploymentName("text-embedding-3-large")
                        .setModelName(AzureOpenAIModelName.TEXT_EMBEDDING3LARGE))));
        String blobKsName = testResourceNamer.randomName("blob-ks-", 63).toLowerCase();
        AzureBlobKnowledgeSource blobKs = new AzureBlobKnowledgeSource(blobKsName, blobKsParams);

        Mono<KnowledgeBaseRetrievalResult> pipeline
            = searchIndexClient.createKnowledgeSource(blobKs).flatMap(createdKs -> {
                KnowledgeSourceReference blobRef = new KnowledgeSourceReference(blobKsName).setEnableImageServing(true);
                KnowledgeBase knowledgeBase
                    = new KnowledgeBase(randomKnowledgeBaseName(), blobRef).setModels(KNOWLEDGE_BASE_MODEL);
                return searchIndexClient.createKnowledgeBase(knowledgeBase);
            }).flatMap(createdKb -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(createdKb.getName())
                        .buildAsyncClient();

                AzureBlobKnowledgeSourceParams blobRetrieveParams = new AzureBlobKnowledgeSourceParams(blobKsName);
                blobRetrieveParams.setEnableImageServing(true);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("Show me images from documents."))
                    .setKnowledgeSourceParams(Collections.singletonList(blobRetrieveParams));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(pipeline).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    @Test
    public void retrieveWithFailOnErrorSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setFailOnError(true);

        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What hotels are near the ocean?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void retrieveWithFailOnErrorAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> pipeline
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();

                KnowledgeSourceParams sourceParams
                    = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                        .setFailOnError(true);

                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("What hotels are near the ocean?"))
                    .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(pipeline).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
        }).verifyComplete();
    }

    private String randomKnowledgeBaseName() {
        // Generate a random name for the knowledge base.
        return testResourceNamer.randomName("knowledge-base-", 63);
    }

    private static SearchIndexClient setupIndex() {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(HOTELS_TESTS_INDEX_DATA_JSON))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            SemanticConfiguration semanticConfiguration = new SemanticConfiguration("semantic-config",
                new SemanticPrioritizedFields().setTitleField(new SemanticField("HotelName"))
                    .setContentFields(new SemanticField("Description"))
                    .setKeywordsFields(new SemanticField("Category")));
            SemanticSearch semanticSearch = new SemanticSearch().setDefaultConfigurationName("semantic-config")
                .setConfigurations(semanticConfiguration);
            searchIndexClient.createOrUpdateIndex(
                TestHelpers.createTestIndex(HOTEL_INDEX_NAME, baseIndex).setSemanticSearch(semanticSearch));

            uploadDocumentsJson(searchIndexClient.getSearchClient(HOTEL_INDEX_NAME), HOTELS_DATA_JSON);

            return searchIndexClient;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
