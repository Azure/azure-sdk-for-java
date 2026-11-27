// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHintAutoConfiguration;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceQueryHints;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessage;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.models.SearchOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Demonstrates automatic country-facet grounding, generated query-hint filters, and selected document references.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code AZURE_OPENAI_ENDPOINT},
 * {@code AZURE_OPENAI_CHAT_DEPLOYMENT}, and {@code AZURE_OPENAI_CHAT_MODEL}. Set {@code AZURE_OPENAI_API_KEY},
 * or grant the search service identity access to the model. Requires the latest preview API, automatic query hints,
 * semantic search, native chat-model access, and one free index slot. Only this run's unique resources are cleaned up.
 * Optionally set {@code SEARCH_HINT_REFRESH=true} to add Japan and wait beyond the 60-second grounding TTL.</p>
 */
public class QueryHintsAutoConfigurationPreviewExample {
    /**
     * Runs the sample.
     * @param args Unused.
     */
    public static void main(String[] args) {
        AzureOpenAIVectorizerParameters model = new AzureOpenAIVectorizerParameters()
            .setResourceUrl(PreviewSampleSupport.required("AZURE_OPENAI_ENDPOINT"))
            .setDeploymentName(PreviewSampleSupport.required("AZURE_OPENAI_CHAT_DEPLOYMENT"))
            .setModelName(AzureOpenAIModelName.fromString(PreviewSampleSupport.required("AZURE_OPENAI_CHAT_MODEL")))
            .setApiKey(PreviewSampleSupport.optional("AZURE_OPENAI_API_KEY", null));
        SearchIndexClient indexes = PreviewSampleSupport.indexClient();
        String indexName = PreviewSampleSupport.name("auto-hints-index");
        String sourceName = PreviewSampleSupport.name("auto-hints-source");
        String baseName = PreviewSampleSupport.name("auto-hints-kb");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createIndex(PreviewSampleSupport.textIndex(indexName));
            SearchClient search = indexes.getSearchClient(indexName);
            PreviewSampleSupport.upload(search, PreviewSampleSupport.hotelDocuments());
            PreviewSampleSupport.waitForDocuments(search, 3);
            System.out.println("Stored country facets: " + search
                .search(new SearchOptions().setSearchText("*").setFacets("country,count:16").setTop(0))
                .iterableByPage().iterator().next().getFacets());

            // BEGIN: com.azure.search.documents.queryHintsAutoConfigurationPreview
            resources.createKnowledgeSource(configuration(sourceName, indexName));
            KnowledgeBaseToolsPreviewExample.waitForSource(indexes, sourceName);
            verifyStoredHint(indexes.getKnowledgeSource(sourceName));
            resources.createKnowledgeBase(new KnowledgeBase(baseName, new KnowledgeSourceReference(sourceName))
                .setModels(new KnowledgeBaseAzureOpenAIModel(model))
                .setRetrievalReasoningEffort(new KnowledgeRetrievalLowReasoningEffort())
                .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA));

            KnowledgeBaseRetrievalClient retrieval = PreviewSampleSupport.retrievalClient(baseName);
            retrieveCountry(retrieval, "Canada", sourceName, new HashSet<>(Arrays.asList("hotel-ca-1", "hotel-ca-2")));
            // END: com.azure.search.documents.queryHintsAutoConfigurationPreview

            if (Boolean.parseBoolean(PreviewSampleSupport.optional("SEARCH_HINT_REFRESH", "false"))) {
                Map<String, Object> document = new LinkedHashMap<>();
                document.put("id", "hotel-jp-1");
                document.put("title", "Garden Hotel");
                document.put("content", "Hotel guest benefits include complimentary internet and breakfast.");
                document.put("country", "Japan");
                PreviewSampleSupport.upload(search, Collections.singletonList(document));
                PreviewSampleSupport.waitForDocuments(search, 4);
                System.out.println("Waiting 65 seconds before querying the newly added facet value.");
                try {
                    Thread.sleep(65000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted waiting for the grounding TTL.", ex);
                }
                retrieveCountry(retrieval, "Japan", sourceName, Collections.singleton("hotel-jp-1"));
            }
            System.out.println("No request-time filter was supplied. TTL configuration is not cache-hit telemetry.");
        }
    }

    static SearchIndexKnowledgeSource configuration(String sourceName, String indexName) {
        SearchIndexKnowledgeSourceFilterHint hint = new SearchIndexKnowledgeSourceFilterHint("country")
            .setAutoConfiguration(new SearchIndexKnowledgeSourceFilterHintAutoConfiguration()
                .setCacheTtl(60).setMaxFacetValueCount(16))
            .setFilterInstructions("When a country is specified, filter country using the exact country name "
                + "from the resolved facet values.");
        return new SearchIndexKnowledgeSource(sourceName,
            new SearchIndexKnowledgeSourceParameters(indexName).setSemanticConfigurationName("default")
                .setQueryHints(new SearchIndexKnowledgeSourceQueryHints().setFilters(Collections.singletonList(hint))));
    }

    static void verifyStoredHint(KnowledgeSource source) {
        if (!(source instanceof SearchIndexKnowledgeSource)) {
            throw new IllegalStateException("The created source must reference the sample index.");
        }
        SearchIndexKnowledgeSourceQueryHints hints
            = ((SearchIndexKnowledgeSource) source).getSearchIndexParameters().getQueryHints();
        if (hints == null || hints.getFilters() == null || hints.getFilters().size() != 1
            || hints.getFilters().get(0).getAutoConfiguration() == null
            || hints.getFilters().get(0).getFieldValues() != null) {
            throw new IllegalStateException("The stored source must use automatic, not manual, facet grounding.");
        }
        SearchIndexKnowledgeSourceFilterHint hint = hints.getFilters().get(0);
        if (!"country".equals(hint.getField())
            || !Integer.valueOf(60).equals(hint.getAutoConfiguration().getCacheTtl())
            || !Integer.valueOf(16).equals(hint.getAutoConfiguration().getMaxFacetValueCount())) {
            throw new IllegalStateException("The stored source did not preserve the country grounding configuration.");
        }
    }

    static void retrieveCountry(KnowledgeBaseRetrievalClient retrieval, String country, String sourceName,
        Set<String> expectedKeys) {
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setMessages(Collections.singletonList(new KnowledgeBaseMessage(new KnowledgeBaseMessageTextContent(
                "Find hotel guest benefits for hotels in " + country + " only.")).setRole("user")))
            .setIncludeActivity(true);
        KnowledgeBaseRetrievalResult result = retrieval.retrieve(request);
        List<KnowledgeBaseSearchIndexActivityRecord> activities = KnowledgeBaseToolsPreviewExample.verifyScope(result,
            expectedKeys, Collections.singleton(sourceName));
        boolean generatedFilter = false;
        for (KnowledgeBaseSearchIndexActivityRecord activity : activities) {
            if (activity.getQueryHintProcessing() != null) {
                String filter = activity.getQueryHintProcessing().getGeneratedFilter();
                if (filter != null) {
                    System.out.println("Generated query-hint filter: " + filter);
                    generatedFilter |= filter.contains("country") && filter.contains(country);
                }
            }
        }
        if (!generatedFilter) {
            throw new IllegalStateException("The planner did not report the automatically grounded country filter.");
        }
    }
}
