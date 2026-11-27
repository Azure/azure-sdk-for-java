// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.RandomScoringFunction;
import com.azure.search.documents.indexes.models.RandomScoringParameters;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedResponse;
import com.azure.search.documents.models.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Demonstrates seeded and unseeded random scoring, signed 64-bit seeds, paging, filtering, and counts.
 *
 * <p>Set {@code SEARCH_ENDPOINT} and {@code SEARCH_API_KEY}. Requires the latest preview API and one free index
 * slot; no model deployment is needed. Only this run's uniquely named index is created and cleaned up.</p>
 */
public class RandomScoringPreviewExample {
    /**
     * Runs the sample.
     * @param args Unused.
     */
    public static void main(String[] args) {
        SearchIndexClient indexes = PreviewSampleSupport.indexClient();
        String indexName = PreviewSampleSupport.name("random-scoring");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            // BEGIN: com.azure.search.documents.randomScoringPreview
            SearchIndex index = new SearchIndex(indexName,
                Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true).setFilterable(true),
                    new SearchField("title", SearchFieldDataType.STRING).setSearchable(true),
                    new SearchField("rating", SearchFieldDataType.INT32).setFilterable(true)));
            index.setScoringProfiles(
                new ScoringProfile("randomUnseeded").setFunctions(new RandomScoringFunction()),
                new ScoringProfile("randomSeeded").setFunctions(
                    new RandomScoringFunction().setParameters(new RandomScoringParameters("randomSeed"))));
            resources.createIndex(index);
            SearchClient search = indexes.getSearchClient(indexName);
            PreviewSampleSupport.upload(search, documents());
            PreviewSampleSupport.waitForDocuments(search, 20);
            demonstrateQueries(search);
            // END: com.azure.search.documents.randomScoringPreview
        }
    }

    static List<Map<String, Object>> documents() {
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("id", Integer.toString(i));
            // Identical searchable text isolates random scoring from lexical relevance differences.
            document.put("title", "hotel alpha");
            document.put("rating", (i - 1) % 5 + 1);
            documents.add(document);
        }
        return documents;
    }

    static void demonstrateQueries(SearchClient search) {
        Set<String> allKeys = documents().stream().map(document -> (String) document.get("id"))
            .collect(Collectors.toSet());
        List<SearchResult> first = seeded(search, 12345L, 20, 0, null);
        verifyKeys(first, allKeys);
        verifySameResults(first, seeded(search, 12345L, 20, 0, null));
        List<SearchResult> changedSeed = seeded(search, 98765L, 20, 0, null);
        verifyKeys(changedSeed, allKeys);
        System.out.println("Seed 12345 order: " + keys(first));
        System.out.println("Seed 98765 order: " + keys(changedSeed));
        System.out.println("These seeds produced different orders: " + !keys(first).equals(keys(changedSeed)));

        Set<List<String>> unseededOrders = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            List<SearchResult> rows = search.search(new SearchOptions().setSearchText("hotel")
                .setScoringProfile("randomUnseeded").setSelect("id").setTop(20)).stream()
                .collect(Collectors.toList());
            verifyKeys(rows, allKeys);
            unseededOrders.add(keys(rows));
        }
        System.out.println("Distinct unseeded orders across three requests: " + unseededOrders.size());
        for (long seed : new long[] {0L, -42L, Long.MAX_VALUE, Long.MIN_VALUE}) {
            List<SearchResult> rows = seeded(search, seed, 20, 0, null);
            verifyKeys(rows, allKeys);
            verifySameResults(rows, seeded(search, seed, 20, 0, null));
        }

        List<SearchResult> pages = new ArrayList<>();
        for (int skip = 0; skip < 20; skip += 5) {
            pages.addAll(seeded(search, 12345L, 5, skip, null));
        }
        verifySameResults(first, pages);
        Set<String> eligible = documents().stream()
            .filter(document -> (Integer) document.get("rating") >= 4)
            .map(document -> (String) document.get("id")).collect(Collectors.toSet());
        verifyKeys(seeded(search, 12345L, 20, 0, "rating ge 4"), eligible);

        SearchPagedResponse counted = search.search(seedOptions(12345L).setIncludeTotalCount(true))
            .iterableByPage().iterator().next();
        verifyKeys(counted.getValue(), allKeys);
        if (!Long.valueOf(20).equals(counted.getCount())) {
            throw new IllegalStateException("The total count must agree with the seeded result set.");
        }
        System.out.println("Verified repeatable seeded scores, boundary seeds, paging, filtering, and counts.");
    }

    static List<SearchResult> seeded(SearchClient search, long seed, int top, int skip, String filter) {
        return search.search(seedOptions(seed).setTop(top).setSkip(skip).setFilter(filter))
            .stream().collect(Collectors.toList());
    }

    private static SearchOptions seedOptions(long seed) {
        // A negative seed deliberately produces a double hyphen, for example randomSeed--42.
        return new SearchOptions().setSearchText("hotel").setScoringProfile("randomSeeded")
            .setScoringParameters("randomSeed-" + seed).setSelect("id").setTop(20);
    }

    static void verifyKeys(List<SearchResult> rows, Set<String> expected) {
        List<String> keys = keys(rows);
        if (keys.size() != expected.size() || !new HashSet<>(keys).equals(expected)
            || rows.stream().anyMatch(row -> !Double.isFinite(row.getScore()))) {
            throw new IllegalStateException(
                "Random scoring must return exactly the expected documents and finite scores.");
        }
    }

    private static List<String> keys(List<SearchResult> rows) {
        return rows.stream().map(row -> (String) row.getAdditionalProperties().get("id"))
            .collect(Collectors.toList());
    }

    private static void verifySameResults(List<SearchResult> first, List<SearchResult> repeated) {
        if (!keys(first).equals(keys(repeated))) {
            throw new IllegalStateException("The same seed must preserve document order.");
        }
        for (int i = 0; i < first.size(); i++) {
            if (Double.compare(first.get(i).getScore(), repeated.get(i).getScore()) != 0) {
                throw new IllegalStateException("The same seed must preserve document scores.");
            }
        }
    }
}
