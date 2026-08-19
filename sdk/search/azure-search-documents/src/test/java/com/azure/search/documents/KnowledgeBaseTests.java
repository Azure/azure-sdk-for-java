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
import com.azure.search.documents.indexes.models.KnowledgeSourceResultsProcessing;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFieldValueBoost;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceQueryHints;
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
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAgenticReasoningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAzureBlobReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseFileReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedOneLakeReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedSharePointReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedSqlReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelAnswerSynthesisActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelQueryPlanningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalAutoReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalReasoningEffortKind;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceAzureOpenAIVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.PurviewSensitivityLabelInfo;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;
import com.azure.search.documents.models.QueryType;
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
    public void knowledgeBaseCostAttributionTagsPersistSync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        Map<String, String> tags = createCostAttributionTags("FIN-042");
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setTags(tags);

        KnowledgeBase created = indexClient.createKnowledgeBase(knowledgeBase);
        KnowledgeBase retrieved = indexClient.getKnowledgeBase(created.getName());
        KnowledgeBase listed = indexClient.listKnowledgeBases()
            .stream()
            .filter(item -> created.getName().equals(item.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected tagged knowledge base in list response"));

        assertEquals(tags, created.getTags());
        assertEquals(tags, retrieved.getTags());
        assertEquals(tags, listed.getTags());
    }

    @Test
    public void knowledgeBaseCostAttributionTagsPersistAsync() {
        SearchIndexAsyncClient indexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        Map<String, String> tags = createCostAttributionTags("FIN-042");
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setTags(tags);

        StepVerifier
            .create(indexClient.createKnowledgeBase(knowledgeBase)
                .flatMap(created -> indexClient.getKnowledgeBase(created.getName())))
            .assertNext(retrieved -> assertEquals(tags, retrieved.getTags()))
            .verifyComplete();
    }

    @Test
    public void knowledgeBaseCostAttributionTagsCanBeUpdatedSync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setTags(createCostAttributionTags("FIN-042"));
        indexClient.createKnowledgeBase(knowledgeBase);
        Map<String, String> updatedTags = createCostAttributionTags("FIN-108");
        updatedTags.put("environment", "production");

        indexClient.createOrUpdateKnowledgeBase(knowledgeBase.setTags(updatedTags));

        assertEquals(updatedTags, indexClient.getKnowledgeBase(knowledgeBase.getName()).getTags());
    }

    @Test
    public void knowledgeBaseCostAttributionTagsCanBeUpdatedAsync() {
        SearchIndexAsyncClient indexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setTags(createCostAttributionTags("FIN-042"));
        Map<String, String> updatedTags = createCostAttributionTags("FIN-108");
        updatedTags.put("environment", "production");

        StepVerifier
            .create(indexClient.createKnowledgeBase(knowledgeBase)
                .flatMap(created -> indexClient.createOrUpdateKnowledgeBase(created.setTags(updatedTags)))
                .flatMap(updated -> indexClient.getKnowledgeBase(updated.getName())))
            .assertNext(retrieved -> assertEquals(updatedTags, retrieved.getTags()))
            .verifyComplete();
    }

    @Test
    public void taggedKnowledgeBaseRetrievalRemainsUnchangedSync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setTags(createCostAttributionTags("FIN-042"));
        indexClient.createKnowledgeBase(knowledgeBase);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setIncludeActivity(true);

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(request);

        assertNotNull(response.getResponse());
        assertNotNull(response.getActivity());
        assertEquals(createCostAttributionTags("FIN-042"),
            indexClient.getKnowledgeBase(knowledgeBase.getName()).getTags());
    }

    @Test
    public void knowledgeBaseCostAttributionTagsRoundTripJson() throws IOException {
        Map<String, String> tags = createCostAttributionTags("FIN-042");
        KnowledgeBase knowledgeBase = new KnowledgeBase("capital-markets-kb", KNOWLEDGE_SOURCE_REFERENCE).setTags(tags);

        KnowledgeBase deserialized;
        try (JsonReader reader = JsonProviders.createReader(knowledgeBase.toJsonString())) {
            deserialized = KnowledgeBase.fromJson(reader);
        }

        assertEquals(tags, deserialized.getTags());
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
    public void retrievalWithPersistedAutoReasoningEffortSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL)
                .setOutputMode(KnowledgeRetrievalOutputMode.ANSWER_SYNTHESIS)
                .setRetrievalReasoningEffort(new KnowledgeRetrievalAutoReasoningEffort());
        KnowledgeBase created = searchIndexClient.createKnowledgeBase(knowledgeBase);

        assertInstanceOf(KnowledgeRetrievalAutoReasoningEffort.class, created.getRetrievalReasoningEffort());
        assertEquals(KnowledgeRetrievalReasoningEffortKind.AUTO, created.getRetrievalReasoningEffort().getKind());

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(created.getName()).buildClient();
        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Which hotel has an infinity pool, spa, and concierge?"))
            .setIncludeActivity(true);

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertAutoReasoningActivity(response);
    }

    @Test
    public void retrievalWithRequestAutoReasoningEffortAsync() {
        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);

        Mono<KnowledgeBaseRetrievalResult> createAndRetrieveMono
            = searchIndexClient.createKnowledgeBase(knowledgeBase).flatMap(created -> {
                KnowledgeBaseRetrievalAsyncClient knowledgeBaseClient
                    = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(created.getName())
                        .buildAsyncClient();
                KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
                    .setIntents(
                        new KnowledgeRetrievalSemanticIntent("Which hotel has an infinity pool, spa, and concierge?"))
                    .setRetrievalReasoningEffort(new KnowledgeRetrievalAutoReasoningEffort())
                    .setIncludeActivity(true);

                return knowledgeBaseClient.retrieve(retrievalRequest);
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(response -> {
            assertNotNull(response);
            assertNotNull(response.getResponse());
            assertAutoReasoningActivity(response);
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
    public void retrievalNeverQueriesExcludedSourceSync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        String excludedSourceName = randomKnowledgeBaseName() + "-excluded";
        indexClient.createKnowledgeSource(new SearchIndexKnowledgeSource(excludedSourceName,
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME)));
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME),
                new KnowledgeSourceReference(excludedSourceName)).setModels(KNOWLEDGE_BASE_MODEL);
        indexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalOptions request = createNeverQuerySourceRequest(excludedSourceName);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(request);

        assertNeverQuerySourceResult(response, excludedSourceName);
        KnowledgeBase persisted = indexClient.getKnowledgeBase(knowledgeBase.getName());
        assertEquals(2, persisted.getKnowledgeSources().size());
        assertTrue(
            persisted.getKnowledgeSources().stream().anyMatch(source -> excludedSourceName.equals(source.getName())));
    }

    @Test
    public void retrievalNeverQueriesExcludedSourceAsync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        String excludedSourceName = randomKnowledgeBaseName() + "-excluded";
        indexClient.createKnowledgeSource(new SearchIndexKnowledgeSource(excludedSourceName,
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME)));
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(HOTEL_KNOWLEDGE_SOURCE_NAME),
                new KnowledgeSourceReference(excludedSourceName)).setModels(KNOWLEDGE_BASE_MODEL);
        indexClient.createKnowledgeBase(knowledgeBase);
        KnowledgeBaseRetrievalAsyncClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(knowledgeBase.getName())
                .buildAsyncClient();

        StepVerifier.create(retrievalClient.retrieve(createNeverQuerySourceRequest(excludedSourceName)))
            .assertNext(response -> assertNeverQuerySourceResult(response, excludedSourceName))
            .verifyComplete();
    }

    @Test
    public void retrievalRejectsConflictingSourceSelectionSync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        indexClient.createKnowledgeBase(knowledgeBase);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setNeverQuerySource(true);
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> retrievalClient.retrieve(request));

        assertEquals(400, exception.getResponse().getStatusCode());
        assertTrue(exception.getMessage().contains("alwaysQuerySource"));
        assertTrue(exception.getMessage().contains("neverQuerySource"));
    }

    @Test
    public void retrievalRejectsConflictingSourceSelectionAsync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        indexClient.createKnowledgeBase(knowledgeBase);
        KnowledgeBaseRetrievalAsyncClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(knowledgeBase.getName())
                .buildAsyncClient();
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setNeverQuerySource(true);
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        StepVerifier.create(retrievalClient.retrieve(request)).verifyErrorSatisfies(error -> {
            HttpResponseException exception = assertInstanceOf(HttpResponseException.class, error);
            assertEquals(400, exception.getResponse().getStatusCode());
            assertTrue(exception.getMessage().contains("alwaysQuerySource"));
            assertTrue(exception.getMessage().contains("neverQuerySource"));
        });
    }

    @Test
    public void neverQuerySourceRoundTripsThroughPolymorphicModel() throws IOException {
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setNeverQuerySource(true)
                .setFailOnError(true);

        KnowledgeSourceParams deserialized
            = KnowledgeSourceParams.fromJson(JsonProviders.createReader(sourceParams.toJsonString()));

        SearchIndexKnowledgeSourceParams actual
            = assertInstanceOf(SearchIndexKnowledgeSourceParams.class, deserialized);
        assertEquals(Boolean.TRUE, actual.isNeverQuerySource());
        assertEquals(Boolean.TRUE, actual.isFailOnError());
    }

    @Test
    public void knowledgeSourceResultsProcessingNonePersistsSync() {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        SearchIndexKnowledgeSource knowledgeSource
            = createRerankerKnowledgeSource(KnowledgeSourceResultsProcessing.NONE);

        SearchIndexKnowledgeSource created
            = assertInstanceOf(SearchIndexKnowledgeSource.class, indexClient.createKnowledgeSource(knowledgeSource));
        SearchIndexKnowledgeSource retrieved
            = assertInstanceOf(SearchIndexKnowledgeSource.class, indexClient.getKnowledgeSource(created.getName()));

        assertEquals(KnowledgeSourceResultsProcessing.NONE, created.getResultsProcessing());
        assertEquals(KnowledgeSourceResultsProcessing.NONE, retrieved.getResultsProcessing());
        assertEquals("semantic-config", retrieved.getSearchIndexParameters().getSemanticConfigurationName());
    }

    @Test
    public void knowledgeSourceResultsProcessingNonePersistsAsync() {
        SearchIndexAsyncClient indexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        SearchIndexKnowledgeSource knowledgeSource
            = createRerankerKnowledgeSource(KnowledgeSourceResultsProcessing.NONE);

        StepVerifier.create(indexClient.createKnowledgeSource(knowledgeSource)
            .flatMap(created -> indexClient.getKnowledgeSource(created.getName()))).assertNext(retrieved -> {
                SearchIndexKnowledgeSource actual = assertInstanceOf(SearchIndexKnowledgeSource.class, retrieved);
                assertEquals(KnowledgeSourceResultsProcessing.NONE, actual.getResultsProcessing());
                assertEquals("semantic-config", actual.getSearchIndexParameters().getSemanticConfigurationName());
            }).verifyComplete();
    }

    @Test
    public void storedResultsProcessingNoneSkipsRerankerSync() {
        RerankerTestResources resources = createRerankerTestResources(KnowledgeSourceResultsProcessing.NONE);
        KnowledgeBaseRetrievalResult response
            = resources.client.retrieve(createRerankerRequest(resources.knowledgeSourceName, null, null));

        assertRerankerDisabled(response, resources.knowledgeSourceName);
    }

    @Test
    public void runtimeResultsProcessingNoneOverridesStoredRerankAsync() {
        RerankerTestResources resources = createRerankerTestResources(KnowledgeSourceResultsProcessing.RERANK);
        KnowledgeBaseRetrievalAsyncClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(resources.knowledgeBaseName)
                .buildAsyncClient();

        StepVerifier
            .create(retrievalClient.retrieve(
                createRerankerRequest(resources.knowledgeSourceName, KnowledgeSourceResultsProcessing.NONE, null)))
            .assertNext(response -> assertRerankerDisabled(response, resources.knowledgeSourceName))
            .verifyComplete();
    }

    @Test
    public void runtimeResultsProcessingRerankOverridesStoredNoneSync() {
        RerankerTestResources resources = createRerankerTestResources(KnowledgeSourceResultsProcessing.NONE);
        KnowledgeBaseRetrievalResult response = resources.client.retrieve(
            createRerankerRequest(resources.knowledgeSourceName, KnowledgeSourceResultsProcessing.RERANK, 0.0F));

        KnowledgeBaseSearchIndexActivityRecord activity
            = getSearchIndexActivity(response, resources.knowledgeSourceName);
        assertEquals("semantic-config", activity.getSearchIndexArguments().getSemanticConfigurationName());
    }

    @Test
    public void rerankerThresholdRejectedWhenStoredProcessingIsNoneSync() {
        RerankerTestResources resources = createRerankerTestResources(KnowledgeSourceResultsProcessing.NONE);

        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> resources.client.retrieve(createRerankerRequest(resources.knowledgeSourceName, null, 2.5F)));

        assertEquals(400, exception.getResponse().getStatusCode());
        assertTrue(exception.getMessage().toLowerCase().contains("rerankerthreshold"));
    }

    @Test
    public void rerankerThresholdRejectedWhenRuntimeProcessingIsNoneAsync() {
        RerankerTestResources resources = createRerankerTestResources(KnowledgeSourceResultsProcessing.RERANK);
        KnowledgeBaseRetrievalAsyncClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(false).knowledgeBaseName(resources.knowledgeBaseName)
                .buildAsyncClient();

        StepVerifier
            .create(retrievalClient.retrieve(
                createRerankerRequest(resources.knowledgeSourceName, KnowledgeSourceResultsProcessing.NONE, 2.5F)))
            .verifyErrorSatisfies(error -> {
                HttpResponseException exception = assertInstanceOf(HttpResponseException.class, error);
                assertEquals(400, exception.getResponse().getStatusCode());
                assertTrue(exception.getMessage().toLowerCase().contains("rerankerthreshold"));
            });
    }

    @Test
    public void resultsProcessingRoundTripsResourceAndRuntimeModels() throws IOException {
        SearchIndexKnowledgeSource source = createRerankerKnowledgeSource(KnowledgeSourceResultsProcessing.NONE);
        SearchIndexKnowledgeSourceParams runtimeParams = new SearchIndexKnowledgeSourceParams(source.getName())
            .setResultsProcessing(KnowledgeSourceResultsProcessing.RERANK);

        SearchIndexKnowledgeSource deserializedSource;
        try (JsonReader reader = JsonProviders.createReader(source.toJsonString())) {
            deserializedSource = SearchIndexKnowledgeSource.fromJson(reader);
        }
        KnowledgeSourceParams deserializedParams
            = KnowledgeSourceParams.fromJson(JsonProviders.createReader(runtimeParams.toJsonString()));

        assertEquals(KnowledgeSourceResultsProcessing.NONE, deserializedSource.getResultsProcessing());
        assertEquals(KnowledgeSourceResultsProcessing.RERANK, deserializedParams.getResultsProcessing());
    }

    @Test
    public void retrievalReturnsCitationUrlWithoutSourceDataSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        KnowledgeBaseRetrievalClient knowledgeBaseClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setIncludeReferences(true)
                .setIncludeReferenceSourceData(false)
                .setMaxOutputDocuments(50);
        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Which hotel has an infinity pool, spa, and concierge?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));

        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest);

        assertNotNull(response.getReferences());
        assertFalse(response.getReferences().isEmpty());
        response.getReferences().forEach(reference -> {
            KnowledgeBaseSearchIndexReference searchIndexReference
                = assertInstanceOf(KnowledgeBaseSearchIndexReference.class, reference);
            assertNotNull(searchIndexReference.getDocKey());
            assertNotNull(searchIndexReference.getCitationUrl());
            assertTrue(searchIndexReference.getCitationUrl().contains(searchIndexReference.getDocKey()));
            assertNull(searchIndexReference.getSourceData());
        });
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

    @Test
    public void retrievalUsesKnowledgeSourceFilterHintsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        String knowledgeSourceName = testResourceNamer.randomName("query-hint-source-", 63);
        SearchIndexKnowledgeSourceFilterHint filterHint
            = new SearchIndexKnowledgeSourceFilterHint("Category", Collections.singletonList("Luxury"))
                .setFilterInstructions("Filter by the hotel category requested by the user.");
        SearchIndexKnowledgeSourceQueryHints queryHints
            = new SearchIndexKnowledgeSourceQueryHints().setFilters(Collections.singletonList(filterHint));
        SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(knowledgeSourceName,
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME).setQueryHints(queryHints));

        SearchIndexKnowledgeSource created = assertInstanceOf(SearchIndexKnowledgeSource.class,
            searchIndexClient.createKnowledgeSource(knowledgeSource));
        assertNotNull(created.getSearchIndexParameters().getQueryHints());
        assertEquals("Category", created.getSearchIndexParameters().getQueryHints().getFilters().get(0).getField());

        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(knowledgeSourceName))
                .setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(knowledgeSourceName).setAlwaysQuerySource(true);
        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Find a luxury hotel with an infinity pool."))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams))
            .setIncludeActivity(true);

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(retrievalRequest);

        assertNotNull(response.getResponse());
        KnowledgeBaseSearchIndexActivityRecord activity = getSearchIndexActivity(response, knowledgeSourceName);
        assertNotNull(activity.getQueryHintProcessing());
        assertNotNull(activity.getQueryHintProcessing().getGeneratedFilter());
        assertTrue(activity.getQueryHintProcessing().getGeneratedFilter().contains("Luxury"));
        assertNotNull(activity.getSearchIndexArguments());
        assertTrue(activity.getSearchIndexArguments().getFilter().contains("Luxury"));
    }

    @Test
    public void retrievalUsesQueryHintBoostOverrideSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        SearchIndexKnowledgeSourceFieldValueBoost boost = new SearchIndexKnowledgeSourceFieldValueBoost("Category", 2.0)
            .setFieldValues(Collections.singletonList("Luxury"))
            .setBoostInstructions("Prefer the hotel category requested by the user.");
        SearchIndexKnowledgeSourceQueryHints queryHintOverrides
            = new SearchIndexKnowledgeSourceQueryHints().setBoosts(Collections.singletonList(boost));
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setQueryHintOverrides(queryHintOverrides);
        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Prefer luxury hotels with an infinity pool."))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams))
            .setIncludeActivity(true);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(retrievalRequest);

        assertNotNull(response.getResponse());
        KnowledgeBaseSearchIndexActivityRecord activity = getSearchIndexActivity(response, HOTEL_KNOWLEDGE_SOURCE_NAME);
        assertNotNull(activity.getQueryHintProcessing());
        assertNotNull(activity.getQueryHintProcessing().getGeneratedBoost());
        assertTrue(activity.getQueryHintProcessing().getGeneratedBoost().contains("Luxury"));
        assertNotNull(activity.getSearchIndexArguments());
        assertEquals(QueryType.FULL, activity.getSearchIndexArguments().getQueryType());
        assertTrue(activity.getSearchIndexArguments().getSearch().contains("Luxury"));
    }

    @Test
    public void retrievalCombinesQueryHintBoostWithFilterAddOnSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        SearchIndexKnowledgeSourceFieldValueBoost boost = new SearchIndexKnowledgeSourceFieldValueBoost("Category", 2.0)
            .setFieldValues(Collections.singletonList("Budget"))
            .setBoostInstructions("Prefer the hotel category requested by the user.");
        SearchIndexKnowledgeSourceQueryHints queryHintOverrides
            = new SearchIndexKnowledgeSourceQueryHints().setBoosts(Collections.singletonList(boost));
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setFilterAddOn("ParkingIncluded eq true")
                .setQueryHintOverrides(queryHintOverrides);
        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Find a budget hotel with parking."))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams))
            .setIncludeActivity(true);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(retrievalRequest);

        assertNotNull(response.getResponse());
        KnowledgeBaseSearchIndexActivityRecord activity = getSearchIndexActivity(response, HOTEL_KNOWLEDGE_SOURCE_NAME);
        assertNotNull(activity.getQueryHintProcessing());
        assertNotNull(activity.getQueryHintProcessing().getGeneratedBoost());
        assertTrue(activity.getQueryHintProcessing().getGeneratedBoost().contains("Budget"));
        assertNull(activity.getQueryHintProcessing().getGeneratedFilter());
        assertNotNull(activity.getSearchIndexArguments());
        assertEquals(QueryType.FULL, activity.getSearchIndexArguments().getQueryType());
        assertTrue(activity.getSearchIndexArguments().getFilter().contains("ParkingIncluded eq true"));
    }

    @Test
    public void emptyQueryHintOverrideSuppressesKnowledgeSourceHintsSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        String knowledgeSourceName = testResourceNamer.randomName("query-hint-source-", 63);
        SearchIndexKnowledgeSourceFilterHint filterHint
            = new SearchIndexKnowledgeSourceFilterHint("Category", Collections.singletonList("Luxury"))
                .setFilterInstructions("Filter by the hotel category requested by the user.");
        SearchIndexKnowledgeSourceQueryHints queryHints
            = new SearchIndexKnowledgeSourceQueryHints().setFilters(Collections.singletonList(filterHint));
        searchIndexClient.createKnowledgeSource(new SearchIndexKnowledgeSource(knowledgeSourceName,
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME).setQueryHints(queryHints)));

        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(knowledgeSourceName))
                .setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);
        SearchIndexKnowledgeSourceQueryHints emptyOverride
            = new SearchIndexKnowledgeSourceQueryHints().setFilters(Collections.emptyList())
                .setBoosts(Collections.emptyList());
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(knowledgeSourceName).setAlwaysQuerySource(true)
                .setQueryHintOverrides(emptyOverride);
        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Find a luxury hotel with an infinity pool."))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams))
            .setIncludeActivity(true);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(retrievalRequest);

        assertNotNull(response.getResponse());
        KnowledgeBaseSearchIndexActivityRecord activity = getSearchIndexActivity(response, knowledgeSourceName);
        assertNotNull(activity.getSearchIndexArguments());
        assertNull(activity.getSearchIndexArguments().getFilter());
        if (activity.getQueryHintProcessing() != null) {
            assertNull(activity.getQueryHintProcessing().getGeneratedFilter());
            assertNull(activity.getQueryHintProcessing().getGeneratedBoost());
        }
    }

    @Test
    public void retrievalRejectsInvalidQueryHintBoostSync() {
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), KNOWLEDGE_SOURCE_REFERENCE).setModels(KNOWLEDGE_BASE_MODEL);
        searchIndexClient.createKnowledgeBase(knowledgeBase);

        SearchIndexKnowledgeSourceFieldValueBoost invalidBoost
            = new SearchIndexKnowledgeSourceFieldValueBoost("Category", 1.0)
                .setFieldValues(Collections.singletonList("Luxury"));
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setQueryHintOverrides(
                    new SearchIndexKnowledgeSourceQueryHints().setBoosts(Collections.singletonList(invalidBoost)));
        KnowledgeBaseRetrievalOptions retrievalRequest = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Find a luxury hotel."))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams));
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();

        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> retrievalClient.retrieve(retrievalRequest));

        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @Test
    public void queryHintsDeserializePolymorphicBoosts() throws IOException {
        SearchIndexKnowledgeSourceFieldValueBoost boost = new SearchIndexKnowledgeSourceFieldValueBoost("Category", 2.0)
            .setFieldValues(Collections.singletonList("Luxury"))
            .setBoostInstructions("Prefer luxury hotels.");
        SearchIndexKnowledgeSourceQueryHints queryHints
            = new SearchIndexKnowledgeSourceQueryHints().setBoosts(Collections.singletonList(boost));

        try (JsonReader reader = JsonProviders.createReader(queryHints.toJsonString())) {
            SearchIndexKnowledgeSourceQueryHints deserialized = SearchIndexKnowledgeSourceQueryHints.fromJson(reader);

            assertEquals(1, deserialized.getBoosts().size());
            SearchIndexKnowledgeSourceFieldValueBoost deserializedBoost
                = assertInstanceOf(SearchIndexKnowledgeSourceFieldValueBoost.class, deserialized.getBoosts().get(0));
            assertEquals("Category", deserializedBoost.getField());
            assertEquals(Collections.singletonList("Luxury"), deserializedBoost.getFieldValues());
            assertEquals(2.0, deserializedBoost.getBoost());
            assertEquals("Prefer luxury hotels.", deserializedBoost.getBoostInstructions());
        }
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
                return ((KnowledgeBaseModelQueryPlanningActivityRecord) record).getModel() != null
                    && ((KnowledgeBaseModelQueryPlanningActivityRecord) record).getModel().getModelName() != null;
            } else if (record instanceof KnowledgeBaseModelAnswerSynthesisActivityRecord) {
                return ((KnowledgeBaseModelAnswerSynthesisActivityRecord) record).getModel() != null
                    && ((KnowledgeBaseModelAnswerSynthesisActivityRecord) record).getModel().getModelName() != null;
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
                    return ((KnowledgeBaseModelQueryPlanningActivityRecord) record).getModel() != null
                        && ((KnowledgeBaseModelQueryPlanningActivityRecord) record).getModel().getModelName() != null;
                } else if (record instanceof KnowledgeBaseModelAnswerSynthesisActivityRecord) {
                    return ((KnowledgeBaseModelAnswerSynthesisActivityRecord) record).getModel() != null
                        && ((KnowledgeBaseModelAnswerSynthesisActivityRecord) record).getModel().getModelName() != null;
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
        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest, querySourceToken, null);
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

                return knowledgeBaseClient.retrieve(retrievalRequest, authToken, null);
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
        KnowledgeBaseRetrievalResult response = knowledgeBaseClient.retrieve(retrievalRequest, querySourceToken, null);
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

                return knowledgeBaseClient.retrieve(retrievalRequest, authToken, null);
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
    public void retrievalResultDeserializesCitationUrls() throws IOException {
        String citationBaseUrl = "https://contoso.search.windows.net/knowledgesources/source/docs/";
        String json
            = "{" + "\"response\":[{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"Answer.\"}]}],"
                + "\"references\":["
                + "{\"type\":\"searchIndex\",\"id\":\"0\",\"activitySource\":1,\"docKey\":\"search-doc\","
                + "\"citationUrl\":\"" + citationBaseUrl + "search-doc/citation?api-version=2026-08-01-preview\"},"
                + "{\"type\":\"azureBlob\",\"id\":\"1\",\"activitySource\":1,\"blobUrl\":\"https://blob/doc.pdf\","
                + "\"citationUrl\":\"" + citationBaseUrl + "blob-doc/citation?api-version=2026-08-01-preview\"},"
                + "{\"type\":\"indexedOneLake\",\"id\":\"2\",\"activitySource\":1," + "\"citationUrl\":\""
                + citationBaseUrl + "onelake-doc/citation?api-version=2026-08-01-preview\"},"
                + "{\"type\":\"indexedSharePoint\",\"id\":\"3\",\"activitySource\":1," + "\"citationUrl\":\""
                + citationBaseUrl + "sharepoint-doc/citation?api-version=2026-08-01-preview\"},"
                + "{\"type\":\"indexedSql\",\"id\":\"4\",\"activitySource\":1,\"docUrl\":\"sql-doc\","
                + "\"citationUrl\":\"" + citationBaseUrl + "sql-doc/citation?api-version=2026-08-01-preview\"},"
                + "{\"type\":\"file\",\"id\":\"5\",\"activitySource\":1,\"docName\":\"file.md\"," + "\"citationUrl\":\""
                + citationBaseUrl + "file-doc/citation?api-version=2026-08-01-preview\"}" + "]}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            KnowledgeBaseRetrievalResult result = KnowledgeBaseRetrievalResult.fromJson(reader);

            assertEquals(6, result.getReferences().size());
            assertEquals(citationBaseUrl + "search-doc/citation?api-version=2026-08-01-preview",
                assertInstanceOf(KnowledgeBaseSearchIndexReference.class, result.getReferences().get(0))
                    .getCitationUrl());
            assertEquals(citationBaseUrl + "blob-doc/citation?api-version=2026-08-01-preview",
                assertInstanceOf(KnowledgeBaseAzureBlobReference.class, result.getReferences().get(1))
                    .getCitationUrl());
            assertEquals(citationBaseUrl + "onelake-doc/citation?api-version=2026-08-01-preview",
                assertInstanceOf(KnowledgeBaseIndexedOneLakeReference.class, result.getReferences().get(2))
                    .getCitationUrl());
            assertEquals(citationBaseUrl + "sharepoint-doc/citation?api-version=2026-08-01-preview",
                assertInstanceOf(KnowledgeBaseIndexedSharePointReference.class, result.getReferences().get(3))
                    .getCitationUrl());
            assertEquals(citationBaseUrl + "sql-doc/citation?api-version=2026-08-01-preview",
                assertInstanceOf(KnowledgeBaseIndexedSqlReference.class, result.getReferences().get(4))
                    .getCitationUrl());
            assertEquals(citationBaseUrl + "file-doc/citation?api-version=2026-08-01-preview",
                assertInstanceOf(KnowledgeBaseFileReference.class, result.getReferences().get(5)).getCitationUrl());
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

    private static Map<String, String> createCostAttributionTags(String costCenter) {
        Map<String, String> tags = new java.util.LinkedHashMap<>();
        tags.put("businessUnit", "CapitalMarkets");
        tags.put("costCenter", costCenter);
        tags.put("owner", "RBC Capital Markets");
        return tags;
    }

    private SearchIndexKnowledgeSource
        createRerankerKnowledgeSource(KnowledgeSourceResultsProcessing resultsProcessing) {
        return new SearchIndexKnowledgeSource(randomKnowledgeBaseName() + "-source",
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME).setSemanticConfigurationName("semantic-config"))
                .setResultsProcessing(resultsProcessing);
    }

    private RerankerTestResources
        createRerankerTestResources(KnowledgeSourceResultsProcessing storedResultsProcessing) {
        SearchIndexClient indexClient = getSearchIndexClientBuilder(true).buildClient();
        SearchIndexKnowledgeSource source = createRerankerKnowledgeSource(storedResultsProcessing);
        indexClient.createKnowledgeSource(source);
        KnowledgeBase knowledgeBase
            = new KnowledgeBase(randomKnowledgeBaseName(), new KnowledgeSourceReference(source.getName()))
                .setModels(KNOWLEDGE_BASE_MODEL);
        indexClient.createKnowledgeBase(knowledgeBase);
        KnowledgeBaseRetrievalClient retrievalClient
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(knowledgeBase.getName()).buildClient();
        return new RerankerTestResources(source.getName(), knowledgeBase.getName(), retrievalClient);
    }

    private static KnowledgeBaseRetrievalOptions createRerankerRequest(String knowledgeSourceName,
        KnowledgeSourceResultsProcessing resultsProcessing, Float rerankerThreshold) {
        SearchIndexKnowledgeSourceParams sourceParams
            = new SearchIndexKnowledgeSourceParams(knowledgeSourceName).setAlwaysQuerySource(true)
                .setIncludeReferences(true)
                .setMaxOutputDocuments(50)
                .setResultsProcessing(resultsProcessing)
                .setRerankerThreshold(rerankerThreshold);
        return new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Which hotel has an infinity pool, spa, and concierge?"))
            .setKnowledgeSourceParams(Collections.singletonList(sourceParams))
            .setIncludeActivity(true);
    }

    private static void assertRerankerDisabled(KnowledgeBaseRetrievalResult response, String knowledgeSourceName) {
        KnowledgeBaseSearchIndexActivityRecord activity = getSearchIndexActivity(response, knowledgeSourceName);
        assertNotNull(activity.getSearchIndexArguments());
        assertNull(activity.getSearchIndexArguments().getSemanticConfigurationName());
        assertNotNull(activity.getCount());
        assertNotNull(response.getReferences());
        assertFalse(response.getReferences().isEmpty());
        response.getReferences().forEach(reference -> assertNull(reference.getRerankerScore()));
    }

    private static final class RerankerTestResources {
        private final String knowledgeSourceName;
        private final String knowledgeBaseName;
        private final KnowledgeBaseRetrievalClient client;

        private RerankerTestResources(String knowledgeSourceName, String knowledgeBaseName,
            KnowledgeBaseRetrievalClient client) {
            this.knowledgeSourceName = knowledgeSourceName;
            this.knowledgeBaseName = knowledgeBaseName;
            this.client = client;
        }
    }

    private static void assertAutoReasoningActivity(KnowledgeBaseRetrievalResult response) {
        assertNotNull(response.getActivity());
        List<KnowledgeBaseAgenticReasoningActivityRecord> reasoningActivity = response.getActivity()
            .stream()
            .filter(KnowledgeBaseAgenticReasoningActivityRecord.class::isInstance)
            .map(KnowledgeBaseAgenticReasoningActivityRecord.class::cast)
            .collect(Collectors.toList());
        assertFalse(reasoningActivity.isEmpty());

        reasoningActivity.forEach(record -> {
            assertNotNull(record.getLogicalReasoningEffort());
            assertEquals(KnowledgeRetrievalReasoningEffortKind.AUTO, record.getLogicalReasoningEffort().getKind());
            assertNotNull(record.getRetrievalReasoningEffort());
            KnowledgeRetrievalReasoningEffortKind executedKind = record.getRetrievalReasoningEffort().getKind();
            assertTrue(KnowledgeRetrievalReasoningEffortKind.MINIMAL.equals(executedKind)
                || KnowledgeRetrievalReasoningEffortKind.LOW.equals(executedKind)
                || KnowledgeRetrievalReasoningEffortKind.MEDIUM.equals(executedKind));
        });
    }

    private static KnowledgeBaseRetrievalOptions createNeverQuerySourceRequest(String excludedSourceName) {
        SearchIndexKnowledgeSourceParams includedSource
            = new SearchIndexKnowledgeSourceParams(HOTEL_KNOWLEDGE_SOURCE_NAME).setAlwaysQuerySource(true)
                .setIncludeReferences(true)
                .setMaxOutputDocuments(50);
        SearchIndexKnowledgeSourceParams excludedSource
            = new SearchIndexKnowledgeSourceParams(excludedSourceName).setNeverQuerySource(true).setFailOnError(true);
        return new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the pet policies at the hotel?"))
            .setKnowledgeSourceParams(java.util.Arrays.asList(includedSource, excludedSource))
            .setIncludeActivity(true);
    }

    private static void assertNeverQuerySourceResult(KnowledgeBaseRetrievalResult response, String excludedSourceName) {
        assertNotNull(response.getResponse());
        KnowledgeBaseSearchIndexActivityRecord includedActivity
            = getSearchIndexActivity(response, HOTEL_KNOWLEDGE_SOURCE_NAME);
        assertNotNull(includedActivity.getSearchIndexArguments());

        KnowledgeBaseSearchIndexActivityRecord excludedActivity = response.getActivity()
            .stream()
            .filter(KnowledgeBaseSearchIndexActivityRecord.class::isInstance)
            .map(KnowledgeBaseSearchIndexActivityRecord.class::cast)
            .filter(activity -> excludedSourceName.equals(activity.getKnowledgeSourceName()))
            .findFirst()
            .orElse(null);
        if (excludedActivity != null) {
            assertNull(excludedActivity.getSearchIndexArguments());
            if (response.getReferences() != null) {
                response.getReferences()
                    .forEach(reference -> assertFalse(reference.getActivitySource() == excludedActivity.getId()));
            }
        }
    }

    private static KnowledgeBaseSearchIndexActivityRecord getSearchIndexActivity(KnowledgeBaseRetrievalResult response,
        String knowledgeSourceName) {
        assertNotNull(response.getActivity());
        return response.getActivity()
            .stream()
            .filter(KnowledgeBaseSearchIndexActivityRecord.class::isInstance)
            .map(KnowledgeBaseSearchIndexActivityRecord.class::cast)
            .filter(activity -> knowledgeSourceName.equals(activity.getKnowledgeSourceName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected search index activity for " + knowledgeSourceName));
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
