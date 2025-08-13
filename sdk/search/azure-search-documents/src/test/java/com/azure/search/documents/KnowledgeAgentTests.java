// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeAgent;
import com.azure.search.documents.indexes.models.KnowledgeAgentAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeAgentModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

import static com.azure.search.documents.TestHelpers.HOTEL_INDEX_NAME;
import static com.azure.search.documents.TestHelpers.loadResource;
import static com.azure.search.documents.TestHelpers.uploadDocumentsJson;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.SAME_THREAD)
@Disabled("Disabled until the service is available.")
public class KnowledgeAgentTests extends SearchTestBase {
    private static final String HOTEL_KNOWLEDGE_SOURCE_NAME = "hotel-knowledge-source";
    private static final List<KnowledgeAgentModel> KNOWLEDGE_AGENT_MODELS
        = Collections.singletonList(new KnowledgeAgentAzureOpenAIModel(
            new AzureOpenAIVectorizerParameters().setModelName(AzureOpenAIModelName.GPT4O)
                .setDeploymentName("gpt-35-turbo")
                .setApiKey(OPENAI_API_KEY)
                .setResourceUrl(OPENAI_API_ENDPOINT)));
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
        searchIndexClient.createKnowledgeSource(new SearchIndexKnowledgeSource(HOTEL_KNOWLEDGE_SOURCE_NAME,
            new SearchIndexKnowledgeSourceParameters(HOTEL_INDEX_NAME)));

        waitForIndexing();

    }

    @AfterAll
    protected static void cleanupClass() {
        // Clean up any resources after all tests.
        if (TEST_MODE != TestMode.PLAYBACK) {
            // Delete the knowledge source created for the tests.
            searchIndexClient.deleteKnowledgeSource("hotel-knowledge-source", null, null);

            // list all remaining knowledge agents and delete them
            searchIndexClient.listKnowledgeAgents()
                .forEach(
                    knowledgeAgent -> searchIndexClient.deleteKnowledgeAgent(knowledgeAgent.getName(), null, null));

            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void testCreateKnowledgeAgent() {
        // Test creating a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);
    }

    @Test
    public void testGetKnowledgeAgent() {
        // Test getting a knowledge agent.

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);
        KnowledgeAgent retrieved = searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName());
        assertEquals(knowledgeAgent.getName(), retrieved.getName());
    }

    @Test
    public void testListKnowledgeAgents() {
        // Test listing knowledge agents.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        long currentCount = searchIndexClient.listKnowledgeAgents().stream().count();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        KnowledgeAgent knowledgeAgent2
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent2);
        long knowledgeAgentCount = searchIndexClient.listKnowledgeAgents().stream().count();
        assertEquals(2 + currentCount, knowledgeAgentCount);
    }

    @Test
    public void testDeleteKnowledgeAgent() {
        // Test deleting a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);
        waitForIndexing();
        assertNotNull(searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName()));
        searchIndexClient.deleteKnowledgeAgent(knowledgeAgent.getName(), null, null);
        assertThrows(HttpResponseException.class, () -> searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName()));
    }

    @Test
    public void testUpdateKnowledgeAgent() {
        // Test updating a knowledge agent.
        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        KnowledgeAgent knowledgeAgent
            = new KnowledgeAgent(randomKnowledgeAgentName(), KNOWLEDGE_AGENT_MODELS, KNOWLEDGE_SOURCE_REFERENCES);
        searchIndexClient.createKnowledgeAgent(knowledgeAgent);
        String newDescription = "Updated description";
        knowledgeAgent.setDescription(newDescription);
        searchIndexClient.createOrUpdateKnowledgeAgent(knowledgeAgent, null, null);
        KnowledgeAgent retrieved = searchIndexClient.getKnowledgeAgent(knowledgeAgent.getName());
        assertEquals(newDescription, retrieved.getDescription());
    }

    private String randomKnowledgeAgentName() {
        // Generate a random name for the knowledge agent.
        return testResourceNamer.randomName("knowledge-agent-", 63);
    }

    private static SearchIndexClient setupIndex() {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(HOTELS_TESTS_INDEX_DATA_JSON))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(ENDPOINT)
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
