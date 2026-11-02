// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.CreateSkillsetExample;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkill;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillChunkingUnit;
import com.azure.search.documents.indexes.models.DataSourceCredentials;
import com.azure.search.documents.indexes.models.IndexedSharePointContainerName;
import com.azure.search.documents.indexes.models.IndexedSharePointKnowledgeSource;
import com.azure.search.documents.indexes.models.IndexedSqlKnowledgeSource;
import com.azure.search.documents.indexes.models.IndexerPermissionOption;
import com.azure.search.documents.indexes.models.IndexerResyncOption;
import com.azure.search.documents.indexes.models.KnowledgeSourceIngestionPermissionOption;
import com.azure.search.documents.indexes.models.PermissionFilter;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexPermissionFilterOption;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.WebKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceNetworkAccessMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceParams;
import com.azure.search.documents.models.ContentUnderstandingSkillChunkingMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchGaSamplesTests {
    @Test
    @org.junit.jupiter.api.parallel.ResourceLock(org.junit.jupiter.api.parallel.Resources.SYSTEM_OUT)
    public void streamSampleLogsErrorPayloadBeforeThrowing() throws IOException {
        KnowledgeBaseRetrievalStreamEvent error = KnowledgeBaseRetrievalStreamEventConverter.convert("error",
            "{\"error\":{\"code\":\"BadRequest\",\"message\":\"invalid request\"}}");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8.name())) {
            System.setOut(capture);
            assertThrows(IllegalStateException.class, () -> KnowledgeBaseRetrievalStreamExample.inspectEvent(error,
                new AtomicBoolean(), new AtomicBoolean()));
        } finally {
            System.setOut(original);
        }
        String logged = output.toString(StandardCharsets.UTF_8.name());
        assertTrue(logged.contains("Received event: error"));
        assertTrue(logged.contains("Event data: "));
        assertTrue(logged.contains("\"code\":\"BadRequest\""));
        assertTrue(logged.contains("\"message\":\"invalid request\""));
    }

    @Test
    public void permissionSampleConfiguresSchemaIngestionAndFederation() throws IOException {
        SearchIndex index = DocumentPermissionsExample.permissionIndex("permissions", "application", "federation");
        assertEquals(SearchIndexPermissionFilterOption.ENABLED, index.getPermissionFilterOption());
        assertEquals(3, index.getFields().stream().filter(field -> field.getPermissionFilter() != null).count());
        assertTrue(index.getFields()
            .stream()
            .anyMatch(field -> PermissionFilter.USER_IDS.equals(field.getPermissionFilter())));
        assertTrue(
            index.getFields().stream().anyMatch(field -> Boolean.TRUE.equals(field.isOrganizationAccessExpiration())));
        assertTrue(index.getFields().stream().anyMatch(field -> Boolean.TRUE.equals(field.isSharepointSiteUrl())));
        assertEquals("federation", index.getSharePointConnectorAppRegistration().getFederatedCredentialId());

        SearchIndexerDataSourceConnection source
            = new SearchIndexerDataSourceConnection("source", SearchIndexerDataSourceType.fromString("sharepoint"),
                new DataSourceCredentials().setConnectionString("test"),
                new SearchIndexerDataContainer("defaultSiteLibrary"));
        DocumentPermissionsExample.permissionDataSource(source, "/subscriptions/test/identities/search", "client");
        assertEquals(Arrays.asList(IndexerPermissionOption.USER_IDS, IndexerPermissionOption.GROUP_IDS),
            source.getIndexerPermissionOptions());
        assertEquals("client", assertInstanceOf(SearchIndexerDataUserAssignedIdentity.class, source.getIdentity())
            .getFederatedIdentityClientId());
        assertTrue(DocumentPermissionsExample.permissionIndexer("indexer", "source", "permissions")
            .getParameters()
            .getConfiguration()
            .isRefreshAllAcls());
        assertTrue(index.toJsonString().contains("\"permissionFilterOption\":\"enabled\""));
    }

    @Test
    public void sqlAndSharePointSamplesConfigureTypedSources() throws IOException {
        KnowledgeSourceIngestionParameters ingestion
            = new KnowledgeSourceIngestionParameters().setNetworkAccessMode(KnowledgeSourceNetworkAccessMode.PRIVATE)
                .setIngestionPermissionOptions(KnowledgeSourceIngestionPermissionOption.USER_IDS,
                    KnowledgeSourceIngestionPermissionOption.GROUP_IDS);
        IndexedSqlKnowledgeSource sql = KnowledgeSourceCrudExample.sqlSource("sql", "test", "dbo.Documents", ingestion);
        assertEquals("LastModified", sql.getIndexedSqlParameters().getHighWaterMarkColumnName());
        assertEquals(2, sql.getIndexedSqlParameters().getContentColumns().size());
        assertEquals("Content", sql.getIndexedSqlParameters().getEmbeddingColumns().get(0).getSourceField());
        assertTrue(sql.toJsonString().contains("\"embeddingColumns\""));
        IndexedSharePointKnowledgeSource sharePoint
            = KnowledgeSourceCrudExample.sharePointSource("sharepoint", "test", ingestion);
        assertEquals(IndexedSharePointContainerName.DEFAULT_SITE_LIBRARY,
            sharePoint.getIndexedSharePointParameters().getContainerName());
        assertEquals(2,
            sharePoint.getIndexedSharePointParameters()
                .getIngestionParameters()
                .getIngestionPermissionOptions()
                .size());
    }

    @Test
    public void webSampleIncludesRetrievalDefaults() {
        WebKnowledgeSourceParameters parameters = KnowledgeSourceCrudExample.webSource("web").getWebParameters();
        assertEquals("en", parameters.getLanguage());
        assertEquals("en-US", parameters.getMarket());
        assertEquals(5, parameters.getCount());
        assertEquals("Month", parameters.getFreshness());
        assertEquals("learn.microsoft.com", parameters.getDomains().getAllowedDomains().get(0).getAddress());
    }

    @Test
    public void conversationSampleIncludesHistoryAndQueryPlanning() throws IOException {
        KnowledgeBaseRetrievalOptions request = KnowledgeRetrievalExample.conversationRequest(null);
        assertEquals(3, request.getMessages().size());
        assertEquals("assistant", request.getMessages().get(1).getRole());
        assertInstanceOf(KnowledgeRetrievalLowReasoningEffort.class, request.getRetrievalReasoningEffort());
        assertEquals(5, request.getMaxOutputDocuments());
        assertTrue(request.toJsonString().contains("\"messages\""));
    }

    @Test
    public void permissionResyncAndImageConversationSamplesUseTypedModels() throws IOException {
        assertEquals(Arrays.asList(IndexerResyncOption.PERMISSIONS),
            DocumentPermissionsExample.permissionResync().getOptions());
        KnowledgeBaseRetrievalOptions request
            = KnowledgeRetrievalExample.conversationRequest("https://example.com/hotel.jpg");
        assertEquals(2, request.getMessages().get(2).getContent().size());
        assertTrue(request.toJsonString().contains("https://example.com/hotel.jpg"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "file", "sql", "sharepoint" })
    public void sourceRetrievalSampleIncludesSourceControls(String kind) {
        KnowledgeSourceParams parameters = KnowledgeRetrievalExample.sourceParameters(kind, "source");
        assertTrue(parameters.isAlwaysQuerySource());
        assertEquals(3, parameters.getMaxOutputDocuments());
        assertEquals("source", parameters.getKnowledgeSourceName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "searchIndex", "azureBlob", "indexedOneLake", "indexedSharePoint", "indexedSql", "file" })
    public void responseSampleReadsEverySearchOwnedCitation(String type) {
        KnowledgeBaseReference reference = BinaryData
            .fromString("{\"type\":\"" + type + "\",\"id\":\"ref\","
                + "\"activitySource\":0,\"citationUrl\":\"https://example.search.windows.net/documents/ref\"}")
            .toObject(KnowledgeBaseReference.class);
        assertEquals("https://example.search.windows.net/documents/ref",
            KnowledgeRetrievalResponseExample.citationUrl(reference));
    }

    @Test
    public void responseSampleHandlesSourceAndModelActivities() {
        KnowledgeBaseRetrievalResult result = BinaryData.fromString("{\"response\":[],\"references\":[],\"activity\":["
            + "{\"id\":0,\"type\":\"searchIndex\",\"knowledgeSourceName\":\"source\",\"count\":1},"
            + "{\"id\":1,\"type\":\"modelQueryPlanning\",\"model\":{\"modelName\":\"gpt-5.4\",\"deploymentId\":\"planner\"}},"
            + "{\"id\":2,\"type\":\"modelAnswerSynthesis\",\"model\":{\"modelName\":\"gpt-5.4\",\"deploymentId\":\"answer\"}},"
            + "{\"id\":3,\"type\":\"modelWebSummarization\",\"model\":{\"modelName\":\"gpt-5.4\"}}]}")
            .toObject(KnowledgeBaseRetrievalResult.class);
        assertEquals(4, result.getActivity().size());
        KnowledgeRetrievalResponseExample.inspectResponse(result);
    }

    @Test
    public void streamSampleHandlesProgressCompletionAndErrors() {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean completed = new AtomicBoolean();
        String[] names = {
            "retrieval.started",
            "activity.started",
            "activity.completed",
            "answer.completed",
            "references.completed",
            "response.completed" };
        String[] payloads = {
            "{\"requestId\":\"id\",\"knowledgeBaseName\":\"kb\",\"reasoningEffort\":{\"kind\":\"low\"}}",
            "{\"id\":0,\"type\":\"searchIndex\"}",
            "{\"id\":0,\"type\":\"searchIndex\"}",
            "{\"messageIndex\":0,\"message\":{\"content\":[]}}",
            "[]",
            "{\"statusCode\":200,\"response\":{}}" };
        for (int i = 0; i < names.length; i++) {
            KnowledgeBaseRetrievalStreamEvent event
                = KnowledgeBaseRetrievalStreamEventConverter.convert(names[i], payloads[i]);
            KnowledgeBaseRetrievalStreamExample.inspectEvent(event, started, completed);
        }
        assertTrue(started.get());
        assertTrue(completed.get());
        KnowledgeBaseRetrievalStreamEvent error = KnowledgeBaseRetrievalStreamEventConverter.convert("error",
            "{\"error\":{\"code\":\"BadRequest\",\"message\":\"invalid request\"}}");
        assertThrows(IllegalStateException.class,
            () -> KnowledgeBaseRetrievalStreamExample.inspectEvent(error, started, completed));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void skillSampleSelectsSemanticTokensOrFixedSizeCharacters(boolean semantic) throws IOException {
        ContentUnderstandingSkill skill = CreateSkillsetExample.contentUnderstandingSkill("gpt-4.1", "content",
            semantic
                ? ContentUnderstandingSkillChunkingMethod.SEMANTIC
                : ContentUnderstandingSkillChunkingMethod.FIXED_SIZE);
        assertEquals(
            semantic ? ContentUnderstandingSkillChunkingUnit.TOKENS : ContentUnderstandingSkillChunkingUnit.CHARACTERS,
            skill.getChunkingProperties().getUnit());
        assertEquals("content", skill.getModelDeployment());
        assertTrue(skill.toJsonString().contains(semantic ? "\"method\":\"semantic\"" : "\"method\":\"fixedSize\""));
    }

    @Test
    public void newlySupportedModelNamesAreAvailableToConfigurationSamples() {
        assertEquals(Arrays.asList("gpt-5.4", "gpt-5.5", "gpt-5.6-sol", "gpt-5.6-terra", "gpt-5.6-luna"),
            Arrays.asList(AzureOpenAIModelName.GPT54.toString(), AzureOpenAIModelName.GPT55.toString(),
                AzureOpenAIModelName.GPT56SOL.toString(), AzureOpenAIModelName.GPT56TERRA.toString(),
                AzureOpenAIModelName.GPT56LUNA.toString()));
    }
}
