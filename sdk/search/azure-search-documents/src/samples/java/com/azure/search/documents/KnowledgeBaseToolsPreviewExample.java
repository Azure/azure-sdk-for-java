// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.rest.RequestOptions;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseRetrieveDefaults;
import com.azure.search.documents.indexes.models.KnowledgeBaseTool;
import com.azure.search.documents.indexes.models.KnowledgeBaseToolKnowledgeSourceReference;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Demonstrates saved knowledge-base tools with source restrictions, filters, and retrieval defaults.
 *
 * <p>Set {@code SEARCH_ENDPOINT} and {@code SEARCH_API_KEY}. Requires the latest preview API, semantic search,
 * knowledge-base tools, and two free index slots. Minimal reasoning and extractive output need no chat model.
 * Only uniquely named resources created by this run are cleaned up. This uses direct retrieval, not MCP.</p>
 */
public class KnowledgeBaseToolsPreviewExample {
    /**
     * Runs the sample.
     * @param args Unused.
     */
    public static void main(String[] args) {
        SearchIndexClient indexes = PreviewSampleSupport.indexClient();
        String hotelIndex = PreviewSampleSupport.name("tool-hotels-index");
        String guideIndex = PreviewSampleSupport.name("tool-guides-index");
        String hotelSource = PreviewSampleSupport.name("tool-hotels-source");
        String guideSource = PreviewSampleSupport.name("tool-guides-source");
        String baseName = PreviewSampleSupport.name("tools-kb");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            createSource(indexes, resources, hotelIndex, hotelSource, PreviewSampleSupport.hotelDocuments());
            Map<String, Object> guide = new LinkedHashMap<>();
            guide.put("id", "hotel-guide");
            guide.put("title", "Hotel guest benefits guide");
            guide.put("content",
                "Hotel guest benefits include complimentary wireless internet, breakfast, and parking.");
            guide.put("country", "Canada");
            createSource(indexes, resources, guideIndex, guideSource, Collections.singletonList(guide));

            // BEGIN: com.azure.search.documents.knowledgeBaseToolsPreview
            resources.createKnowledgeBase(configuration(baseName, hotelSource, guideSource));

            KnowledgeBase stored = indexes.getKnowledgeBase(baseName);
            Set<String> toolNames = stored.getTools() == null ? Collections.emptySet()
                : stored.getTools().stream().map(KnowledgeBaseTool::getName).collect(Collectors.toSet());
            if (!toolNames.equals(new HashSet<>(Arrays.asList("canada-hotels", "hotel-guides")))) {
                throw new IllegalStateException("The stored knowledge base did not preserve the tool catalog.");
            }
            KnowledgeBaseRetrievalClient retrieval = PreviewSampleSupport.retrievalClient(baseName);
            Set<String> allKeys = new HashSet<>(Arrays.asList("hotel-ca-1", "hotel-ca-2", "hotel-us-1", "hotel-guide"));
            Set<String> bothSources = new HashSet<>(Arrays.asList(hotelSource, guideSource));
            retrieveScope(retrieval, null, allKeys, bothSources);
            retrieveScope(retrieval, "canada-hotels", new HashSet<>(Arrays.asList("hotel-ca-1", "hotel-ca-2")),
                Collections.singleton(hotelSource));
            retrieveScope(retrieval, "hotel-guides", Collections.singleton("hotel-guide"),
                Collections.singleton(guideSource));

            // Update only this run's owned KB, and require the ETag read immediately before changing it.
            stored = indexes.getKnowledgeBase(baseName);
            if (stored.getETag() == null || stored.getETag().isEmpty()) {
                throw new IllegalStateException("An ETag is required before clearing the tool catalog.");
            }
            stored.setTools(Collections.emptyList());
            indexes.createOrUpdateKnowledgeBaseWithResponse(stored,
                new RequestOptions().setHeader("If-Match", stored.getETag()));
            KnowledgeBase cleared = indexes.getKnowledgeBase(baseName);
            if (cleared.getTools() != null && !cleared.getTools().isEmpty()) {
                throw new IllegalStateException("The custom tools were not cleared.");
            }
            retrieveScope(retrieval, null, allKeys, bothSources);
            System.out.println("Clearing tools and omitting toolName restored retrieval from both sources.");
            // END: com.azure.search.documents.knowledgeBaseToolsPreview
        }
    }

    static KnowledgeBase configuration(String baseName, String hotelSource, String guideSource) {
        KnowledgeBaseTool canadaHotels = new KnowledgeBaseTool("canada-hotels",
            "Find guest benefits at Canadian hotels, excluding the separate hotel guide.",
            Collections.singletonList(new KnowledgeBaseToolKnowledgeSourceReference(hotelSource)
                .setFilterAddOn("country eq 'Canada'").setAlwaysQuerySource(true).setFailOnError(true)))
            .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort())
            .setRetrieveDefaults(new KnowledgeBaseRetrieveDefaults().setMaxRuntimeInSeconds(30)
                .setMaxOutputDocuments(3).setMaxOutputSizeInTokens(5000));
        KnowledgeBaseTool guides = new KnowledgeBaseTool("hotel-guides", "Find hotel guest benefits in the guide.",
            Collections.singletonList(new KnowledgeBaseToolKnowledgeSourceReference(guideSource)));
        return new KnowledgeBase(baseName, new KnowledgeSourceReference(hotelSource),
            new KnowledgeSourceReference(guideSource))
            .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort())
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA)
            .setTools(Arrays.asList(canadaHotels, guides));
    }

    private static void createSource(SearchIndexClient indexes, PreviewSampleSupport.Resources resources,
        String indexName, String sourceName, List<Map<String, Object>> documents) {
        resources.createIndex(PreviewSampleSupport.textIndex(indexName));
        SearchClient search = indexes.getSearchClient(indexName);
        PreviewSampleSupport.upload(search, documents);
        PreviewSampleSupport.waitForDocuments(search, documents.size());
        resources.createKnowledgeSource(new SearchIndexKnowledgeSource(sourceName,
            new SearchIndexKnowledgeSourceParameters(indexName).setSemanticConfigurationName("default")));
        waitForSource(indexes, sourceName);
    }

    static void waitForSource(SearchIndexClient indexes, String sourceName) {
        for (int attempt = 0; attempt < 36; attempt++) {
            KnowledgeSourceStatus status = indexes.getKnowledgeSourceStatus(sourceName);
            if ("active".equals(String.valueOf(status.getSynchronizationStatus()))) {
                return;
            }
            if (attempt < 35) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted waiting for the knowledge source.", ex);
                }
            }
        }
        throw new IllegalStateException("Knowledge source did not become active: " + sourceName);
    }

    static void retrieveScope(KnowledgeBaseRetrievalClient retrieval, String toolName,
        Set<String> expectedKeys, Set<String> expectedSources) {
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("hotel guest benefits"))
            .setIncludeActivity(true).setToolName(toolName);
        verifyScope(retrieval.retrieve(request), expectedKeys, expectedSources);
    }

    static List<KnowledgeBaseSearchIndexActivityRecord> verifyScope(KnowledgeBaseRetrievalResult result,
        Set<String> expectedKeys, Set<String> expectedSources) {
        if (result.getReferences() == null || result.getReferences().isEmpty() || result.getActivity() == null) {
            throw new IllegalStateException("Retrieval must include document references and activity.");
        }
        List<KnowledgeBaseSearchIndexActivityRecord> activities = new ArrayList<>();
        Map<Integer, String> activitySources = new HashMap<>();
        for (KnowledgeBaseActivityRecord activity : result.getActivity()) {
            if (activity.getError() != null) {
                throw new IllegalStateException("Retrieval reported an activity error.");
            }
            if (activity instanceof KnowledgeBaseSearchIndexActivityRecord) {
                KnowledgeBaseSearchIndexActivityRecord search = (KnowledgeBaseSearchIndexActivityRecord) activity;
                activities.add(search);
                activitySources.put(search.getId(), search.getKnowledgeSourceName());
            }
        }
        if (!new HashSet<>(activitySources.values()).equals(expectedSources)) {
            throw new IllegalStateException("Retrieval activity does not match the selected source scope.");
        }
        Set<String> actualKeys = new HashSet<>();
        for (KnowledgeBaseReference reference : result.getReferences()) {
            if (!(reference instanceof KnowledgeBaseSearchIndexReference)
                || !expectedSources.contains(activitySources.get(reference.getActivitySource()))) {
                throw new IllegalStateException("A document reference came from outside the selected source scope.");
            }
            actualKeys.add(((KnowledgeBaseSearchIndexReference) reference).getDocKey());
        }
        if (!actualKeys.equals(expectedKeys)) {
            throw new IllegalStateException("Unexpected referenced documents: " + actualKeys);
        }
        System.out.println("Referenced documents: " + actualKeys);
        return activities;
    }
}
