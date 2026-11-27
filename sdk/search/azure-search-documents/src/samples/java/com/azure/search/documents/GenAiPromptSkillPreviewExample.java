// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.ChatCompletionSkill;
import com.azure.search.documents.indexes.models.DataSourceCredentials;
import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.FieldMappingFunction;
import com.azure.search.documents.indexes.models.IndexerExecutionResult;
import com.azure.search.documents.indexes.models.IndexerExecutionStatus;
import com.azure.search.documents.indexes.models.IndexerStatus;
import com.azure.search.documents.indexes.models.IndexingParameters;
import com.azure.search.documents.indexes.models.IndexingParametersConfiguration;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;

/**
 * Demonstrates integrated GenAI Prompt execution through supported Azure OpenAI/Foundry/APIM Chat Completions
 * endpoints using {@code ChatCompletionSkill}.
 *
 * <p>Unlike compatible retrieval and vectorizers, the pinned November contract has no direct compatible embedding
 * skill or compatible {@code headers}/{@code managedIdentityAudience} skill configuration. GenAI is named
 * {@code ChatCompletionSkill} on the wire and uses {@code uri} plus {@code apiKey} or an Azure AI identity.
 * This example is the supported chat-skill alternative, not a claim that arbitrary compatible providers,
 * embedding endpoints, or Responses endpoints work as skills. It does not promise compatible-provider URI
 * passthrough semantics. See the pinned
 * <a href="https://github.com/Azure/azure-rest-api-specs/blob/b41ba04c0219f6622b7f5214fc020f90bad339ae/specification/search/data-plane/Search/models-service.tsp">
 * skill contract</a> and the
 * <a href="https://learn.microsoft.com/azure/search/cognitive-search-skill-genai-prompt#skill-inputs">
 * GenAI inputs</a>.</p>
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_SKILL_CHAT_URI} to the complete trusted
 * deployed Chat Completions HTTPS URL (including API version), and {@code SEARCH_SKILL_CHAT_API_KEY} to the key
 * accepted by that endpoint. APIM must front your Azure OpenAI/Foundry deployment and accept this skill's API-key
 * authentication; arbitrary compatible header maps and third-party identity audiences are not supported here.
 * No provider model name is guessed or deployment created. Use a deployed non-GPT-6 Chat Completions model.</p>
 *
 * <p>First place exactly one UTF-8 text/plain blob named {@code hotel_graph.txt}, containing
 * {@link #SYNTHETIC_HOTELS}, in a dedicated small Blob folder.
 * Set {@code AZURE_SEARCH_COMPATIBLE_BLOB_CONNECTION_STRING},
 * {@code AZURE_SEARCH_COMPATIBLE_BLOB_CONTAINER}, and {@code AZURE_SEARCH_COMPATIBLE_BLOB_FOLDER}.
 * Set {@code AZURE_SEARCH_COMPATIBLE_BLOB_SOURCE_APPROVED=true} only after confirming that folder contains only this
 * approved synthetic fixture. Root ingestion is rejected. Source blobs are never created, modified, or deleted.</p>
 *
 * <p>The existing Search service needs indexer/skillset capacity and access to both Blob and the model. Existing model
 * billing applies; this sample enables no paid features, storage settings, or roles. Creating the owned indexer
 * automatically starts its first execution. The sample polls that execution, verifies enriched indexed text,
 * and cleans up only its own successfully created indexer, skillset, data source, and index, in reverse order.
 * It does not merely serialize a configuration and does not print source content or provider secrets.</p>
 */
public final class GenAiPromptSkillPreviewExample {
    /** Approved synthetic fixture; upload manually before running this example. */
    public static final String SYNTHETIC_HOTELS
        = "Historic Harbor Hotel is a fictional hotel in Seattle, Washington.\n"
        + "Contoso Hospitality operates Historic Harbor Hotel.\n"
        + "Historic Harbor Hotel was founded in 1984 and has a waterfront spa.\n\n"
        + "Mountain View Inn is a fictional hotel in Denver, Colorado.\n"
        + "Alpine Hospitality operates Mountain View Inn.\n"
        + "Mountain View Inn was founded in 2002.\n\n"
        + "The hotels, organizations, and operational details in this file are synthetic test data.";

    private GenAiPromptSkillPreviewExample() {
    }

    /**
     * Runs actual ChatCompletionSkill indexing over an explicitly approved read-only Blob fixture.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        ChatCompletionSkill skill = skill(PreviewSampleSupport.required("SEARCH_SKILL_CHAT_URI"),
            PreviewSampleSupport.required("SEARCH_SKILL_CHAT_API_KEY"));
        SearchIndexerClient indexers = new SearchIndexerClientBuilder()
            .endpoint(PreviewSampleSupport.required("SEARCH_ENDPOINT"))
            .credential(new AzureKeyCredential(PreviewSampleSupport.required("SEARCH_API_KEY")))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW).buildClient();
        run(PreviewSampleSupport.indexClient(), indexers, skill,
            PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_BLOB_CONNECTION_STRING"),
            PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_BLOB_CONTAINER"),
            PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_BLOB_FOLDER"),
            PreviewSampleSupport.optional("AZURE_SEARCH_COMPATIBLE_BLOB_SOURCE_APPROVED", "false"));
        System.out.println("Executed the GenAI chat skill and verified indexed synthetic hotel facts.");
    }

    static ChatCompletionSkill skill(String uri, String key) {
        String fullUri = OpenAICompatibleRetrievalPreviewExample.fullUri(uri);
        if (!URI.create(fullUri).getPath().endsWith("/chat/completions") || key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Configure a deployed Chat Completions HTTPS URI and its API key.");
        }
        return new ChatCompletionSkill(Arrays.asList(
            new InputFieldMappingEntry("systemMessage")
                .setSource("='Extract only hotel facts explicitly in the text.'"),
            new InputFieldMappingEntry("userMessage")
                .setSource("='List each hotel with its city, founding year, operator and amenities, if given.'"),
            new InputFieldMappingEntry("text").setSource("/document/content")),
            Collections.singletonList(new OutputFieldMappingEntry("response").setTargetName("hotelFacts")), fullUri)
                .setContext("/document").setName("hotel-facts").setApiKey(key);
    }

    static String approvedPrefix(String folder, String approved) {
        String prefix = OpenAICompatibleEmbeddingsPreviewExample.approvedFolder(folder, approved);
        if (prefix.contains("\\") || prefix.contains("//")) {
            throw new IllegalArgumentException("Use a dedicated nonempty Blob folder with ordinary path segments.");
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    static void run(SearchIndexClient indexes, SearchIndexerClient indexers, ChatCompletionSkill skill,
        String connection, String container, String folder, String approved) {
        String prefix = approvedPrefix(folder, approved);
        String name = PreviewSampleSupport.name("chat-skill-hotels");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes);
            IndexerResources owned = new IndexerResources()) {
            resources.createIndex(new SearchIndex(name, Arrays.asList(
                new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("content", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("hotelFacts", SearchFieldDataType.STRING).setSearchable(true))));
            indexers.createDataSourceConnection(new SearchIndexerDataSourceConnection(name,
                SearchIndexerDataSourceType.AZURE_BLOB, new DataSourceCredentials().setConnectionString(connection),
                new SearchIndexerDataContainer(container).setQuery(prefix)));
            owned.add(() -> indexers.deleteDataSourceConnection(name));
            indexers.createSkillset(new SearchIndexerSkillset(name, skill));
            owned.add(() -> indexers.deleteSkillset(name));
            indexers.createIndexer(new SearchIndexer(name, name, name).setSkillsetName(name)
                .setParameters(new IndexingParameters().setBatchSize(1).setMaxFailedItems(0)
                    .setMaxFailedItemsPerBatch(0).setConfiguration(new IndexingParametersConfiguration()
                        .setIndexedFileNameExtensions(".txt").setFailOnUnsupportedContentType(true)
                        .setFailOnUnprocessableDocument(true)))
                .setFieldMappings(new FieldMapping("metadata_storage_path").setTargetFieldName("id")
                    .setMappingFunction(new FieldMappingFunction("base64Encode")))
                .setOutputFieldMappings(new FieldMapping("/document/hotelFacts").setTargetFieldName("hotelFacts")));
            owned.add(() -> indexers.deleteIndexer(name));
            waitForExecution(indexers, name);
            SearchClient search = indexes.getSearchClient(name);
            PreviewSampleSupport.waitForDocuments(search, 1);
            verifyOutput(search);
        }
    }

    static void waitForExecution(SearchIndexerClient indexers, String name) {
        for (int attempt = 0; attempt < 120; attempt++) {
            SearchIndexerStatus status = indexers.getIndexerStatus(name);
            if (IndexerStatus.ERROR.equals(status.getStatus())) {
                throw new IllegalStateException("The skill indexer requires intervention before it can run.");
            }
            IndexerExecutionResult result = status.getLastResult();
            if (result != null && !IndexerExecutionStatus.IN_PROGRESS.equals(result.getStatus())
                && !IndexerExecutionStatus.RESET.equals(result.getStatus())) {
                if (!IndexerExecutionStatus.SUCCESS.equals(result.getStatus()) || result.getItemCount() != 1
                    || result.getFailedItemCount() != 0
                    || (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty())
                    || (result.getErrors() != null && !result.getErrors().isEmpty())
                    || (result.getWarnings() != null && !result.getWarnings().isEmpty())) {
                    throw new IllegalStateException(
                        "The skill indexer must process exactly one fixture without errors.");
                }
                return;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for skill execution.");
            }
        }
        throw new IllegalStateException("Skill execution did not finish within ten minutes.");
    }

    static void verifyOutput(SearchClient search) {
        int count = 0;
        SearchOptions options = new SearchOptions().setTop(2).setSelect("id", "content", "hotelFacts");
        for (SearchResult result : search.search(options)) {
            Map<String, Object> document = result.getAdditionalProperties();
            Object content = document.get("content");
            Object facts = document.get("hotelFacts");
            if (!(document.get("id") instanceof String) || ((String) document.get("id")).isEmpty()
                || !(content instanceof String)
                || !SYNTHETIC_HOTELS.equals(((String) content).replace("\r\n", "\n").trim())
                || !(facts instanceof String)
                || !Arrays.asList("historic harbor", "seattle", "1984", "spa", "contoso",
                    "mountain view", "denver", "2002", "alpine").stream()
                    .allMatch(term -> ((String) facts).toLowerCase(Locale.ROOT).contains(term))) {
                throw new IllegalStateException(
                    "The index must contain the approved fixture and generated hotel facts.");
            }
            count++;
        }
        if (count != 1) {
            throw new IllegalStateException("Expected exactly one enriched synthetic fixture in the owned index.");
        }
    }

    private static final class IndexerResources implements AutoCloseable {
        private final Deque<Runnable> cleanup = new ArrayDeque<>();

        void add(Runnable action) {
            cleanup.push(action);
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
