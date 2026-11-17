// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseMicrosoftRetriever1Model;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAgenticReasoningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.models.AgenticSearchDebugInfo;
import com.azure.search.documents.models.AgenticSearchOptions;
import com.azure.search.documents.models.QueryDebugMode;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedResponse;

/**
 * Demonstrates direct agentic search and a minimal Microsoft Retriever 1 knowledge base.
 *
 * <p>Set {@code SEARCH_ENDPOINT} and {@code SEARCH_API_KEY}. The Search service must support the November preview,
 * semantic search, and paid standard Knowledge Retrieval (Microsoft Retriever 1 has no free tier).
 * No customer chat deployment is configured by this example.
 * Pass {@code direct} or {@code retriever} to run one scenario, or omit arguments to run both. Unique sample
 * indexes, knowledge sources and knowledge bases are deleted after each scenario.</p>
 */
public final class AgenticSearchPreviewExample {
    /**
     * Runs agentic search against a small owned hotel corpus.
     *
     * @param args optional scenario, either direct or retriever.
     */
    public static void main(String[] args) {
        String scenario = args.length == 0 ? "both" : args[0];
        if (!"both".equals(scenario) && !"direct".equals(scenario) && !"retriever".equals(scenario)) {
            throw new IllegalArgumentException("Expected direct or retriever.");
        }
        SearchIndexClient client = PreviewSampleSupport.indexClient();
        if (!"retriever".equals(scenario)) {
            directSearch(client);
        }
        if (!"direct".equals(scenario)) {
            retriever(client);
        }
    }

    static void directSearch(SearchIndexClient client) {
        String indexName = PreviewSampleSupport.name("java-dp11-agentic");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(client)) {
            resources.createIndex(PreviewSampleSupport.textIndex(indexName));
            SearchClient search = client.getSearchClient(indexName);
            PreviewSampleSupport.upload(search, PreviewSampleSupport.hotelDocuments());
            SearchPagedResponse page = search.search(directOptions()).iterableByPage().iterator().next();
            inspectDirectResult(page);
        }
    }

    static SearchOptions directOptions() {
        return new SearchOptions().setSearchText("Which hotels offer breakfast and parking?")
            .setQueryType(QueryType.AGENTIC)
            .setAgenticOptions(new AgenticSearchOptions().setMaxRuntimeInSeconds(30))
            .setDebug(QueryDebugMode.AGENTIC)
            .setTop(3);
    }

    static void inspectDirectResult(SearchPagedResponse page) {
        if (page.getValue().isEmpty()) {
            throw new IllegalStateException("Agentic search returned no hotel documents.");
        }
        AgenticSearchDebugInfo debug = page.getDebugInfo() == null ? null : page.getDebugInfo().getAgentic();
        if (debug == null || debug.getUsage() == null) {
            throw new IllegalStateException("Agentic search did not return the requested usage diagnostics.");
        }
        System.out.printf("Agentic termination: %s; planning tokens: %d; reasoning tokens: %d%n",
            debug.getTerminationReason(), debug.getUsage().getAgenticPlanningTokens(),
            debug.getUsage().getReasoningTokens());
        page.getElements().forEach(result ->
            System.out.println("Hotel: " + result.getAdditionalProperties().get("id")));
    }

    private static void retriever(SearchIndexClient client) {
        String indexName = PreviewSampleSupport.name("java-dp11-retriever-index");
        String sourceName = PreviewSampleSupport.name("java-dp11-retriever-ks");
        String baseName = PreviewSampleSupport.name("java-dp11-retriever-kb");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(client)) {
            resources.createIndex(PreviewSampleSupport.textIndex(indexName));
            PreviewSampleSupport.upload(client.getSearchClient(indexName), PreviewSampleSupport.hotelDocuments());
            resources.createKnowledgeSource(new SearchIndexKnowledgeSource(sourceName,
                new SearchIndexKnowledgeSourceParameters(indexName).setSemanticConfigurationName("default")));
            resources.createKnowledgeBase(new KnowledgeBase(baseName, new KnowledgeSourceReference(sourceName))
                .setModels(new KnowledgeBaseMicrosoftRetriever1Model())
                .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort())
                .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA));
            KnowledgeBaseRetrievalResult result = PreviewSampleSupport.retrievalClient(baseName).retrieve(
                new KnowledgeBaseRetrievalOptions().setIntents(
                    new KnowledgeRetrievalSemanticIntent("Which hotels offer breakfast and parking?"))
                    .setIncludeActivity(true));
            if (result.getReferences() == null || result.getReferences().isEmpty()) {
                throw new IllegalStateException("Microsoft Retriever 1 returned no hotel evidence.");
            }
            if (result.getActivity() != null) {
                result.getActivity().forEach(activity -> {
                    if (activity.getError() != null) {
                        throw new IllegalStateException("Retrieval reported an activity error.");
                    }
                    if (activity instanceof KnowledgeBaseAgenticReasoningActivityRecord) {
                        System.out.println("KB reasoning tokens: "
                            + ((KnowledgeBaseAgenticReasoningActivityRecord) activity).getReasoningTokens());
                    }
                });
            }
            System.out.println("Microsoft Retriever 1 evidence references: " + result.getReferences().size());
        }
    }
}
