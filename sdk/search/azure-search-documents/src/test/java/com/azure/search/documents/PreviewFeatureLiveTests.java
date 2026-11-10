// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSource;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.ChatCompletionSkill;
import com.azure.search.documents.indexes.models.FileKnowledgeSource;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FileUploadMetadata;
import com.azure.search.documents.indexes.models.IndexerExecutionResult;
import com.azure.search.documents.indexes.models.IndexerExecutionStatus;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeBaseMicrosoftRetriever1Model;
import com.azure.search.documents.indexes.models.KnowledgeBaseOpenAICompatibleModel;
import com.azure.search.documents.indexes.models.KnowledgeBaseRetrieveDefaults;
import com.azure.search.documents.indexes.models.KnowledgeBaseTool;
import com.azure.search.documents.indexes.models.KnowledgeBaseToolKnowledgeSourceReference;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.KnowledgeSourceFile;
import com.azure.search.documents.indexes.models.KnowledgeSourceFileIngestionOperation;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.OpenAICompatibleVectorizer;
import com.azure.search.documents.indexes.models.RandomScoringFunction;
import com.azure.search.documents.indexes.models.RandomScoringParameters;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHintAutoConfiguration;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceQueryHints;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.UpdateKnowledgeSourceFileRequest;
import com.azure.search.documents.indexes.models.UploadKnowledgeSourceFileMultipartRequest;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseGraphSearchActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceAzureOpenAIVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;
import com.azure.search.documents.models.AgenticSearchDebugInfo;
import com.azure.search.documents.models.AgenticSearchOptions;
import com.azure.search.documents.models.BalancedVectorQueryTopicGraph;
import com.azure.search.documents.models.ContentFileDetails;
import com.azure.search.documents.models.DiverseVectorQueryTopicGraph;
import com.azure.search.documents.models.GraphOptions;
import com.azure.search.documents.models.GraphQueryDiagnosticSeverity;
import com.azure.search.documents.models.GraphQueryExecutionMode;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.NoneVectorQueryTopicGraph;
import com.azure.search.documents.models.OperationState;
import com.azure.search.documents.models.QueryDebugMode;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedResponse;
import com.azure.search.documents.models.VectorQueryTopicGraph;
import com.azure.search.documents.models.VectorizableTextQuery;
import com.azure.search.documents.models.VectorizedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Opt-in service-behavior checks. See preview-feature-coverage.md for fixture prerequisites and remaining gaps.
 */
@LiveOnly
@EnabledIfEnvironmentVariable(named = "SEARCH_RUN_PREVIEW_FEATURE_TESTS", matches = "true")
@Timeout(value = 20, unit = TimeUnit.MINUTES)
public class PreviewFeatureLiveTests extends SearchTestBase {
    private final List<String> createdIndexes = new ArrayList<>();
    private final List<String> createdSources = new ArrayList<>();
    private final List<String> createdBases = new ArrayList<>();

    @Override
    protected void afterTest() {
        try {
            if (createdBases.isEmpty() && createdSources.isEmpty() && createdIndexes.isEmpty()) {
                return;
            }
            SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();
            List<RuntimeException> failures = new ArrayList<>();
            deleteOwnedResources(createdBases, client::deleteKnowledgeBase, failures);
            deleteOwnedResources(createdSources, client::deleteKnowledgeSource, failures);
            deleteOwnedResources(createdIndexes, client::deleteIndex, failures);
            if (!failures.isEmpty()) {
                IllegalStateException failure
                    = new IllegalStateException("Unable to clean up all owned preview fixtures.");
                failures.forEach(failure::addSuppressed);
                throw failure;
            }
        } finally {
            super.afterTest();
        }
    }

    @Test
    public void randomScoringIsRepeatableForOneSeedAndRotatesForAnother() throws InterruptedException {
        String index = createSeededIndex();
        SearchClient client = getSearchClientBuilder(index, true).buildClient();
        List<String> first = randomOrder(client, "42");
        assertEquals(first, randomOrder(client, "42"));
        List<String> rotated = randomOrder(client, "-9223372036854775808");
        assertEquals(new HashSet<>(first), new HashSet<>(rotated));
        assertEquals(32, first.size());
        assertNotEquals(first, rotated);
    }

    @ParameterizedTest
    @ValueSource(strings = { "diverse", "balanced" })
    public void topicGraphDiversifiesAndInterleavesWithPureVector(String mode) {
        String index = requiredSetting("SEARCH_PREVIEW_TOPIC_INDEX");
        String query = requiredSetting("SEARCH_PREVIEW_TOPIC_QUERY_JSON");
        String topicField = requiredSetting("SEARCH_PREVIEW_TOPIC_FIELD");
        VectorQueryTopicGraph topicGraph = "diverse".equals(mode)
            ? new DiverseVectorQueryTopicGraph().setDiversityFactor(1.0f).setMinDiversityPoolSize(1000)
            : new BalancedVectorQueryTopicGraph().setMinDiversityPoolSize(1000);
        VectorizedQuery diversified = BinaryData.fromString(query).toObject(VectorizedQuery.class);
        diversified.setTopicGraph(topicGraph);
        VectorizedQuery plain = BinaryData.fromString(query).toObject(VectorizedQuery.class);
        plain.setTopicGraph(new NoneVectorQueryTopicGraph());
        SearchClient client = getSearchClientBuilder(index, true).buildClient();
        List<Map<?, ?>> baseline = documents(client, vectorOptions(plain));
        List<Map<?, ?>> results = documents(client, vectorOptions(diversified));
        assertFalse(baseline.isEmpty());
        assertEquals(1, topics(baseline, topicField).size(),
            "Fixture must have a single-topic nearest-neighbor baseline.");
        assertTrue(topics(results, topicField).size() >= 2, "Fixture must exercise cross-topic retrieval.");
        List<Map<?, ?>> interleaved
            = documents(client, new SearchOptions().setTop(10).setVectorQueries(diversified, plain));
        assertFalse(interleaved.isEmpty());
        assertTrue(interleaved.size() <= 10);
        assertEquals(interleaved.size(), interleaved.stream().map(value -> value.get("id")).distinct().count());
    }

    @Test
    public void graphPlansAndExecutesAgainstExtractedEdges() {
        String index = requiredSetting("SEARCH_PREVIEW_GRAPH_INDEX");
        String gql = requiredSetting("SEARCH_PREVIEW_GRAPH_GQL");
        String field = requiredSetting("SEARCH_PREVIEW_GRAPH_RESULT_FIELD");
        String expected = requiredSetting("SEARCH_PREVIEW_GRAPH_RESULT_VALUE");
        SearchClient client = getSearchClientBuilder(index, true).buildClient();
        GraphQueryResult plan = client.graph(new GraphOptions(gql).setExecutionMode(GraphQueryExecutionMode.PLAN_ONLY)
            .setIncludeStatistics(true)
            .setIncludeDiagnostics(true));
        assertNotNull(plan.getStatistics());
        assertEquals(GraphQueryExecutionMode.PLAN_ONLY, plan.getStatistics().getExecutionMode());
        assertNotNull(plan.getDiagnostics());
        assertFalse(plan.getDiagnostics()
            .stream()
            .anyMatch(diagnostic -> GraphQueryDiagnosticSeverity.ERROR.equals(diagnostic.getSeverity())));
        GraphQueryResult result = client.graph(new GraphOptions(gql).setIncludeStatistics(true));
        assertNotNull(result.getValue());
        assertTrue(
            result.getValue().stream().anyMatch(row -> expected.equals(row.getAdditionalProperties().get(field))));
        assertNotNull(result.getStatistics());
    }

    @Test
    public void graphKnowledgeBaseRetrievalUsesGraphSearchActivity() {
        String base = requiredSetting("SEARCH_PREVIEW_GRAPH_KB");
        String query = requiredSetting("SEARCH_PREVIEW_GRAPH_KB_QUERY");
        KnowledgeBaseRetrievalResult result = retrieve(base, query);
        assertRetrieval(result);
        assertNotNull(result.getActivity());
        assertTrue(result.getActivity().stream().anyMatch(KnowledgeBaseGraphSearchActivityRecord.class::isInstance));
    }

    @Test
    public void graphIngestionReportsOntologyAndTriplesWithoutReturningCacheSecret() {
        String source = requiredSetting("SEARCH_PREVIEW_GRAPH_SOURCE");
        AzureBlobKnowledgeSource value = assertInstanceOf(AzureBlobKnowledgeSource.class,
            getSearchIndexClientBuilder(true).buildClient().getKnowledgeSource(source));
        assertNotNull(value.getAzureBlobParameters().getGraphExtraction());
        assertNotNull(value.getAzureBlobParameters().getGraphExtraction().getOntology());
        assertNotNull(value.getAzureBlobParameters().getGraphExtraction().getOntology().getName());
        assertNotNull(value.getAzureBlobParameters().getIngestionParameters().getCache());
        assertTrue(
            value.getAzureBlobParameters().getIngestionParameters().getCache().getStorageConnectionString() == null,
            "A source GET must not return the write-only cache connection string.");
        KnowledgeSourceStatus status = getSearchIndexClientBuilder(true).buildClient().getKnowledgeSourceStatus(source);
        assertNotNull(status.getLastSynchronizationState());
        assertNotNull(status.getLastSynchronizationState().getGraphStatistics());
        assertEquals(value.getAzureBlobParameters().getGraphExtraction().getOntology().getName(),
            status.getLastSynchronizationState().getGraphStatistics().getOntologyName());
        assertTrue(status.getLastSynchronizationState().getGraphStatistics().getTriplesWritten() > 0);
    }

    @Test
    public void agenticSearchReturnsReasoningUsageAndDocuments() {
        String index = requiredSetting("SEARCH_PREVIEW_AGENTIC_INDEX");
        String query = requiredSetting("SEARCH_PREVIEW_AGENTIC_QUERY");
        SearchOptions options = new SearchOptions().setSearchText(query)
            .setQueryType(QueryType.fromString("agentic"))
            .setAgenticOptions(new AgenticSearchOptions().setMaxRuntimeInSeconds(60))
            .setDebug(QueryDebugMode.fromString("agentic"))
            .setTop(10);
        SearchPagedResponse page
            = getSearchClientBuilder(index, true).buildClient().search(options).iterableByPage().iterator().next();
        assertTrue(page.getElements().iterator().hasNext());
        assertNotNull(page.getDebugInfo());
        AgenticSearchDebugInfo debug = page.getDebugInfo().getAgentic();
        assertNotNull(debug);
        assertNotNull(debug.getUsage());
        assertTrue(debug.getUsage().getAgenticPlanningTokens() > 0);
        assertTrue(debug.getUsage().getReasoningTokens() > 0);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void minimalAndMicrosoftRetrieverKnowledgeBasesRetrieve(boolean microsoftRetriever)
        throws InterruptedException {
        String index = createSeededIndex();
        String source = createSource(new SearchIndexKnowledgeSource(randomName("preview-source"),
            new SearchIndexKnowledgeSourceParameters(index).setSemanticConfigurationName("preview-semantic")))
                .getName();
        KnowledgeBase base = new KnowledgeBase(randomName("preview-base"), new KnowledgeSourceReference(source))
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA);
        if (microsoftRetriever) {
            base.setModels(new KnowledgeBaseMicrosoftRetriever1Model());
        } else {
            base.setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort());
        }
        createBase(base);
        KnowledgeBase persisted = getSearchIndexClientBuilder(true).buildClient().getKnowledgeBase(base.getName());
        if (microsoftRetriever) {
            assertInstanceOf(KnowledgeBaseMicrosoftRetriever1Model.class, persisted.getModels().get(0));
        } else {
            assertTrue(persisted.getModels() == null || persisted.getModels().isEmpty());
        }
        assertRetrieval(retrieve(base.getName(), "Find preview product documents."));
    }

    @Test
    public void fileIngestionSupportsUploadReplacementAndRetainedOperationLookup() {
        requiredSetting("SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME");
        String source = createSource(new FileKnowledgeSource(randomName("preview-file"),
            new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                .setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL)
                .setEmbeddingModel(
                    new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(embeddingParameters())))))
                        .getName();
        SearchIndexClient client = getSearchIndexClientBuilder(true).buildClient();
        SyncPoller<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> upload
            = client.beginIngestKnowledgeSourceFileMultipart(source, new UploadKnowledgeSourceFileMultipartRequest(
                new FileUploadMetadata().setFileName("preview.txt"), content("Original preview product.")));
        PollResponse<KnowledgeSourceFileIngestionOperation> uploaded = upload.waitForCompletion(Duration.ofMinutes(15));
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, uploaded.getStatus());
        KnowledgeSourceFile original = upload.getFinalResult();
        assertNotNull(original);
        assertEquals("preview.txt", original.getFileName());
        String uploadId = uploaded.getValue().getId();
        assertEquals(OperationState.SUCCEEDED,
            client.getKnowledgeSourceFileIngestionOperation(source, uploadId).getStatus());

        AsyncPollResponse<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> replacement
            = getSearchIndexClientBuilder(false).buildAsyncClient()
                .beginIngestKnowledgeSourceFileReplacement(source, original.getFileId(),
                    new UpdateKnowledgeSourceFileRequest(new FileUploadMetadata().setFileName("preview.txt"),
                        content("Replacement preview product with changed content.")))
                .last()
                .block(Duration.ofMinutes(15));
        assertNotNull(replacement);
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, replacement.getStatus());
        KnowledgeSourceFile updated = replacement.getFinalResult().block(Duration.ofMinutes(1));
        assertNotNull(updated);
        assertEquals(original.getFileId(), updated.getFileId());
        assertNotEquals(uploadId, replacement.getValue().getId());
        assertNotNull(original.getFileSizeBytes());
        assertNotNull(updated.getFileSizeBytes());
        assertTrue(updated.getFileSizeBytes() > original.getFileSizeBytes());
        Set<String> operations = client.listKnowledgeSourceFileIngestionOperations(source, 1, null)
            .stream()
            .map(KnowledgeSourceFileIngestionOperation::getId)
            .collect(Collectors.toSet());
        assertTrue(operations.contains(uploadId));
        assertTrue(operations.contains(replacement.getValue().getId()));
        HttpResponseException missing = assertThrows(HttpResponseException.class,
            () -> client.getKnowledgeSourceFileIngestionOperation(source, "00000000-0000-0000-0000-000000000000"));
        assertEquals(404, missing.getResponse().getStatusCode());
    }

    @Test
    public void oneLakePermissionsAllowOneIdentityAndExcludeAnother() {
        String index = requiredSetting("SEARCH_PREVIEW_ONELAKE_INDEX");
        String source = requiredSetting("SEARCH_PREVIEW_ONELAKE_SOURCE");
        String id = requiredSetting("SEARCH_PREVIEW_ONELAKE_RESTRICTED_ID");
        String allowed = requiredSetting("SEARCH_PREVIEW_ONELAKE_ALLOWED_TOKEN");
        String denied = requiredSetting("SEARCH_PREVIEW_ONELAKE_DENIED_TOKEN");
        assertFalse(allowed.equals(denied), "Use distinct, valid source identities.");
        SearchIndexClient indexes = getSearchIndexClientBuilder(true).buildClient();
        assertEquals("indexedOneLake", indexes.getKnowledgeSource(source).getKind().toString());
        assertTrue(indexes.getIndex(index)
            .getFields()
            .stream()
            .anyMatch(field -> field.getPermissionFilter() != null
                && "oneLakeParentFolders".equals(field.getPermissionFilter().toString())));
        SearchClient client = getSearchClientBuilder(index, true).buildClient();
        SearchOptions query = new SearchOptions().setFilter("id eq '" + id.replace("'", "''") + "'")
            .setQuerySourceAuthorization(allowed)
            .setEnableElevatedRead(false);
        assertEquals(1, documents(client, query).size(), "Allowed identity must see the restricted sentinel.");
        assertEquals(0, documents(client, query.setQuerySourceAuthorization(denied)).size(),
            "A valid identity without folder access must not see the same sentinel.");
    }

    @Test
    public void toolSelectionLimitsSourcesAndRequestDefaultsTakePrecedence() throws InterruptedException {
        String index = createSeededIndex();
        String source = createSource(new SearchIndexKnowledgeSource(randomName("preview-tool-source"),
            new SearchIndexKnowledgeSourceParameters(index).setSemanticConfigurationName("preview-semantic")))
                .getName();
        KnowledgeBaseTool tool = new KnowledgeBaseTool("red-documents", "Retrieve only red preview products.",
            Collections.singletonList(
                new KnowledgeBaseToolKnowledgeSourceReference(source).setFilterAddOn("category eq 'red'")))
                    .setRetrieveDefaults(new KnowledgeBaseRetrieveDefaults().setMaxOutputDocuments(2));
        KnowledgeBase base = new KnowledgeBase(randomName("preview-tools"), new KnowledgeSourceReference(source))
            .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort())
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA)
            .setRetrieveDefaults(new KnowledgeBaseRetrieveDefaults().setMaxOutputDocuments(4))
            .setTools(Collections.singletonList(tool));
        createBase(base);
        assertEquals("red-documents",
            getSearchIndexClientBuilder(true).buildClient()
                .getKnowledgeBase(base.getName())
                .getTools()
                .get(0)
                .getName());
        KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("Find preview product documents."))
            .setToolName("red-documents")
            .setMaxOutputDocuments(1)
            .setKnowledgeSourceParams(
                Collections.singletonList(new SearchIndexKnowledgeSourceParams(source).setIncludeReferences(true)
                    .setIncludeReferenceSourceData(true)));
        KnowledgeBaseRetrievalResult result
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(base.getName())
                .buildClient()
                .retrieve(options);
        assertRetrieval(result);
        assertEquals(1, result.getReferences().size());
        assertEquals("red", result.getReferences().get(0).getSourceData().get("category"));
        HttpResponseException invalid = assertThrows(HttpResponseException.class,
            () -> getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(base.getName())
                .buildClient()
                .retrieve(options.setToolName("missing-tool")));
        assertEquals(400, invalid.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "CHAT", "RESPONSES" })
    public void compatibleKnowledgeBaseModelsRetrieveThroughBothProtocols(String protocol) {
        String base = requiredSetting("SEARCH_PREVIEW_COMPATIBLE_" + protocol + "_KB");
        String query = requiredSetting("SEARCH_PREVIEW_COMPATIBLE_QUERY");
        KnowledgeBase persisted = getSearchIndexClientBuilder(true).buildClient().getKnowledgeBase(base);
        KnowledgeBaseOpenAICompatibleModel model
            = assertInstanceOf(KnowledgeBaseOpenAICompatibleModel.class, persisted.getModels().get(0));
        String path = URI.create(model.getOpenAICompatibleParameters().getUri()).getPath();
        assertTrue(path.endsWith("CHAT".equals(protocol) ? "/chat/completions" : "/responses"));
        assertRetrieval(retrieve(base, query));
    }

    @Test
    public void compatibleIngestionSkillsAndVectorizersProcessDocuments() {
        String source = requiredSetting("SEARCH_PREVIEW_COMPATIBLE_SOURCE");
        String index = requiredSetting("SEARCH_PREVIEW_COMPATIBLE_INDEX");
        String vectorField = requiredSetting("SEARCH_PREVIEW_COMPATIBLE_VECTOR_FIELD");
        String skillset = requiredSetting("SEARCH_PREVIEW_COMPATIBLE_SKILLSET");
        String indexer = requiredSetting("SEARCH_PREVIEW_COMPATIBLE_INDEXER");
        SearchIndexClient indexes = getSearchIndexClientBuilder(true).buildClient();
        AzureBlobKnowledgeSource blob
            = assertInstanceOf(AzureBlobKnowledgeSource.class, indexes.getKnowledgeSource(source));
        assertInstanceOf(KnowledgeBaseOpenAICompatibleModel.class,
            blob.getAzureBlobParameters().getIngestionParameters().getChatCompletionModel());
        KnowledgeSourceStatus status = indexes.getKnowledgeSourceStatus(source);
        assertNotNull(status.getLastSynchronizationState());
        assertNotNull(status.getStatistics());
        assertTrue(status.getStatistics().getAverageItemsProcessedPerSynchronization() > 0);
        assertTrue(indexes.getIndex(index)
            .getVectorSearch()
            .getVectorizers()
            .stream()
            .anyMatch(OpenAICompatibleVectorizer.class::isInstance));
        assertTrue(getSearchIndexerClientBuilder(true).buildClient()
            .getSkillset(skillset)
            .getSkills()
            .stream()
            .filter(ChatCompletionSkill.class::isInstance)
            .map(ChatCompletionSkill.class::cast)
            .anyMatch(skill -> skill.getUrl() != null && skill.getCommonModelParameters() != null));
        IndexerExecutionResult execution
            = getSearchIndexerClientBuilder(true).buildClient().getIndexerStatus(indexer).getLastResult();
        assertNotNull(execution);
        assertEquals(IndexerExecutionStatus.SUCCESS, execution.getStatus());
        assertEquals(0, execution.getFailedItemCount());
        assertTrue(execution.getItemCount() > 0);
        assertFalse(documents(getSearchClientBuilder(index, true).buildClient(),
            new SearchOptions().setVectorQueries(new VectorizableTextQuery("preview product").setFields(vectorField)))
                .isEmpty());
    }

    @Test
    public void gpt6KnowledgeBaseProducesAResponse() {
        String base = requiredSetting("SEARCH_PREVIEW_GPT6_KB");
        String query = requiredSetting("SEARCH_PREVIEW_GPT6_QUERY");
        KnowledgeBase persisted = getSearchIndexClientBuilder(true).buildClient().getKnowledgeBase(base);
        KnowledgeBaseAzureOpenAIModel model
            = assertInstanceOf(KnowledgeBaseAzureOpenAIModel.class, persisted.getModels().get(0));
        AzureOpenAIModelName name = model.getAzureOpenAIParameters().getModelName();
        assertTrue(AzureOpenAIModelName.GPT6ASTRA.equals(name)
            || AzureOpenAIModelName.GPT6SOL.equals(name)
            || AzureOpenAIModelName.GPT6LUNA.equals(name), "Fixture must use a supported GPT-6 family model.");
        assertRetrieval(retrieve(base, query));
    }

    @Test
    public void automaticFilterHintsGroundRetrievalAndRejectExplicitValues() throws InterruptedException {
        String index = createSeededIndex();
        SearchIndexKnowledgeSourceFilterHint hint = new SearchIndexKnowledgeSourceFilterHint("category")
            .setAutoConfiguration(
                new SearchIndexKnowledgeSourceFilterHintAutoConfiguration().setCacheTtl(900).setMaxFacetValueCount(4))
            .setFilterInstructions("Use the category filter whenever the request specifies a product category.");
        SearchIndexKnowledgeSourceParameters parameters
            = new SearchIndexKnowledgeSourceParameters(index).setSemanticConfigurationName("preview-semantic")
                .setQueryHints(new SearchIndexKnowledgeSourceQueryHints().setFilters(Collections.singletonList(hint)));
        String source = createSource(new SearchIndexKnowledgeSource(randomName("preview-hints"), parameters)).getName();
        SearchIndexKnowledgeSource persisted = assertInstanceOf(SearchIndexKnowledgeSource.class,
            getSearchIndexClientBuilder(true).buildClient().getKnowledgeSource(source));
        assertEquals(4,
            persisted.getSearchIndexParameters()
                .getQueryHints()
                .getFilters()
                .get(0)
                .getAutoConfiguration()
                .getMaxFacetValueCount());
        KnowledgeBase base = new KnowledgeBase(randomName("preview-filter-base"), new KnowledgeSourceReference(source))
            .setModels(new KnowledgeBaseAzureOpenAIModel(chatParameters()))
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA);
        createBase(base);
        KnowledgeBaseRetrievalResult result
            = getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(base.getName())
                .buildClient()
                .retrieve(new KnowledgeBaseRetrievalOptions()
                    .setIntents(new KnowledgeRetrievalSemanticIntent("Find documents in the red product category."))
                    .setKnowledgeSourceParams(Collections
                        .singletonList(new SearchIndexKnowledgeSourceParams(source).setIncludeReferences(true)
                            .setIncludeReferenceSourceData(true))));
        assertRetrieval(result);
        for (com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference reference : result
            .getReferences()) {
            assertInstanceOf(KnowledgeBaseSearchIndexReference.class, reference);
            assertNotNull(reference.getSourceData());
            assertEquals("red", reference.getSourceData().get("category"));
        }
        hint.setFieldValues(Collections.singletonList("red"));
        HttpResponseException invalid = assertThrows(HttpResponseException.class,
            () -> createSource(new SearchIndexKnowledgeSource(randomName("preview-invalid-hints"), parameters)));
        assertEquals(400, invalid.getResponse().getStatusCode());
    }

    private String createSeededIndex() throws InterruptedException {
        String name = randomName("preview-features");
        SearchIndex index = BinaryData.fromString("{\"name\":\"" + name + "\",\"fields\":["
            + "{\"name\":\"id\",\"type\":\"Edm.String\",\"key\":true,\"filterable\":true},"
            + "{\"name\":\"content\",\"type\":\"Edm.String\",\"searchable\":true},"
            + "{\"name\":\"category\",\"type\":\"Edm.String\",\"filterable\":true,\"facetable\":true}],"
            + "\"semantic\":{\"configurations\":[{\"name\":\"preview-semantic\",\"prioritizedFields\":"
            + "{\"prioritizedContentFields\":[{\"fieldName\":\"content\"}]}}]}}").toObject(SearchIndex.class);
        index.setScoringProfiles(new ScoringProfile("rotate")
            .setFunctions(new RandomScoringFunction().setParameters(new RandomScoringParameters("seed"))));
        getSearchIndexClientBuilder(true).buildClient().createIndex(index);
        createdIndexes.add(name);
        List<Map<String, Object>> values = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            Map<String, Object> document = new HashMap<>();
            document.put("id", Integer.toString(i));
            document.put("content", "Preview product document.");
            document.put("category", i % 2 == 0 ? "red" : "blue");
            values.add(document);
        }
        SearchClient client = getSearchClientBuilder(name, true).buildClient();
        assertTrue(client.indexDocuments(new IndexDocumentsBatch(values.stream()
            .map(document -> TestHelpers.createIndexAction(IndexActionType.UPLOAD, document))
            .collect(Collectors.toList()))).getResults().stream().allMatch(result -> result.isSucceeded()));
        long deadline = System.nanoTime() + Duration.ofMinutes(2).toNanos();
        while (client.getDocumentCount() != values.size() && System.nanoTime() < deadline) {
            Thread.sleep(1000);
        }
        assertEquals(values.size(), client.getDocumentCount(), "The owned fixture did not finish indexing.");
        return name;
    }

    private KnowledgeSource createSource(KnowledgeSource source) {
        KnowledgeSource created = getSearchIndexClientBuilder(true).buildClient().createKnowledgeSource(source);
        createdSources.add(created.getName());
        return created;
    }

    private static void deleteOwnedResources(List<String> names, Consumer<String> delete,
        List<RuntimeException> failures) {
        for (String name : names) {
            try {
                delete.accept(name);
            } catch (RuntimeException failure) {
                failures.add(failure);
            }
        }
    }

    private void createBase(KnowledgeBase base) {
        KnowledgeBase created = getSearchIndexClientBuilder(true).buildClient().createKnowledgeBase(base);
        createdBases.add(created.getName());
    }

    private String randomName(String prefix) {
        return testResourceNamer.randomName(prefix, 48);
    }

    private static String requiredSetting(String name) {
        String value = Configuration.getGlobalConfiguration().get(name);
        assumeTrue(value != null && !value.trim().isEmpty(), "Missing preview fixture setting: " + name);
        return value;
    }

    private static List<Map<?, ?>> documents(SearchClient client, SearchOptions options) {
        return client.search(options)
            .stream()
            .<Map<?, ?>>map(result -> result.getAdditionalProperties())
            .collect(Collectors.toList());
    }

    private static List<String> randomOrder(SearchClient client, String seed) {
        return documents(client,
            new SearchOptions().setTop(32)
                .setScoringProfile("rotate")
                .setScoringParameters(Collections.singletonList("seed:" + seed))).stream()
                    .map(document -> (String) document.get("id"))
                    .collect(Collectors.toList());
    }

    private static SearchOptions vectorOptions(VectorizedQuery query) {
        return new SearchOptions().setTop(10).setVectorQueries(query);
    }

    private static Set<Object> topics(List<Map<?, ?>> documents, String field) {
        Set<Object> values = new HashSet<>();
        for (Map<?, ?> document : documents) {
            assertNotNull(document.get(field), "Topic fixture documents must expose their topic label.");
            values.add(document.get(field));
        }
        return values;
    }

    private KnowledgeBaseRetrievalResult retrieve(String base, String query) {
        return getKnowledgeBaseRetrievalClientBuilder(true).knowledgeBaseName(base)
            .buildClient()
            .retrieve(new KnowledgeBaseRetrievalOptions().setIntents(new KnowledgeRetrievalSemanticIntent(query))
                .setIncludeActivity(true));
    }

    private static void assertRetrieval(KnowledgeBaseRetrievalResult result) {
        assertNotNull(result);
        assertNotNull(result.getResponse());
        assertFalse(result.getResponse().isEmpty());
        assertNotNull(result.getReferences());
        assertFalse(result.getReferences().isEmpty(),
            "The seeded request must retrieve evidence, not just an empty answer.");
    }

    private static ContentFileDetails content(String value) {
        return new ContentFileDetails(BinaryData.fromString(value)).setFilename("preview.txt")
            .setContentType("text/plain");
    }

    private static AzureOpenAIVectorizerParameters embeddingParameters() {
        return new AzureOpenAIVectorizerParameters().setResourceUrl(OPENAI_ENDPOINT)
            .setDeploymentName(OPENAI_EMBEDDING_DEPLOYMENT_NAME)
            .setModelName(AzureOpenAIModelName.fromString(OPENAI_EMBEDDING_MODEL_NAME))
            .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY));
    }

    private static AzureOpenAIVectorizerParameters chatParameters() {
        return new AzureOpenAIVectorizerParameters().setResourceUrl(OPENAI_ENDPOINT)
            .setDeploymentName(OPENAI_DEPLOYMENT_NAME)
            .setModelName(AzureOpenAIModelName.fromString(OPENAI_MODEL_NAME))
            .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(USER_ASSIGNED_IDENTITY));
    }
}
