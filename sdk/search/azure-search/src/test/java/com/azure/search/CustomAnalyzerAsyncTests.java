// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeRequest;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CharFilterName;
import com.azure.search.models.CustomAnalyzer;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.PatternAnalyzer;
import com.azure.search.models.PatternReplaceCharFilter;
import com.azure.search.models.RegexFlags;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.models.StopAnalyzer;
import com.azure.search.models.TokenFilterName;
import com.azure.search.models.TokenizerName;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomAnalyzerAsyncTests extends CustomAnalyzerTestsBase {
    private SearchServiceAsyncClient searchServiceClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchServiceClient = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Test
    public void canSearchWithCustomAnalyzer() {
        final String customAnalyzerName = "my_email_analyzer";
        final String customCharFilterName = "my_email_filter";

        Index index = new Index()
            .setName("testindex")
            .setFields(Arrays.asList(
                new Field()
                    .setName("id")
                    .setType(DataType.EDM_STRING)
                    .setKey(true),
                new Field()
                    .setName("message")
                    .setType(DataType.EDM_STRING)
                    .setAnalyzer(customAnalyzerName)
                    .setSearchable(true)
            ))
            .setAnalyzers(Collections.singletonList(
                new CustomAnalyzer()
                    .setTokenizer(TokenizerName.STANDARD.toString())
                    .setCharFilters(Collections.singletonList(customCharFilterName))
                    .setName(customAnalyzerName)
            ))
            .setCharFilters(Collections.singletonList(
                new PatternReplaceCharFilter()
                    .setPattern("@")
                    .setReplacement("_")
                    .setName(customCharFilterName)
            ));

        searchServiceClient.createIndex(index).block();

        SearchIndexAsyncClient searchIndexClient = searchServiceClient.getIndexClient(index.getName());

        Document document1 = new Document();
        document1.put("id", "1");
        document1.put("message", "My email is someone@somewhere.something.");
        Document document2 = new Document();
        document2.put("id", "2");
        document2.put("message", "His email is someone@nowhere.nothing.");
        List<Document> documents = Arrays.asList(
            document1,
            document2
        );

        searchIndexClient.uploadDocuments(documents).block();
        waitForIndexing();

        PagedFluxBase<SearchResult, SearchPagedResponse> results =
            searchIndexClient.search("someone@somewhere.something", new SearchOptions(), generateRequestOptions());

        StepVerifier.create(results.collectList())
            .assertNext(firstPage ->
                Assert.assertEquals("1", firstPage.get(0).getDocument().get("id"))
            )
            .verifyComplete();
    }

    @Test
    public void canUseAllAnalyzerNamesInIndexDefinition() {
        Index index = prepareIndexWithAllAnalyzerNames();

        StepVerifier
            .create(searchServiceClient.createIndex(index))
            .assertNext(res ->
                assertIndexesEqual(index, res)
            )
            .verifyComplete();

        // Add language analyzers to searchAnalyzer and indexAnalyzer properties and expect failure
        try {
            new Field()
                .setName("field")
                .setType(DataType.EDM_STRING)
                .setSearchAnalyzer(AnalyzerName.EN_LUCENE.toString());
        } catch (Exception ex) {
            Assert.assertEquals(IllegalArgumentException.class, ex.getClass());
            Assert.assertEquals("Only non-language analyzer can be used as search analyzer.", ex.getMessage());
        }
        try {
            new Field()
                .setName("field")
                .setType(DataType.EDM_STRING)
                .setIndexAnalyzer(AnalyzerName.AR_MICROSOFT.toString());
        } catch (Exception ex) {
            Assert.assertEquals(IllegalArgumentException.class, ex.getClass());
            Assert.assertEquals("Only non-language analyzer can be used as index analyzer.", ex.getMessage());
        }
    }

    @Test
    public void canAnalyze() {
        Index index = createTestIndex();
        searchServiceClient.createIndex(index).block();

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setAnalyzer(AnalyzerName.WHITESPACE);
        StepVerifier
            .create(searchServiceClient.analyzeText(index.getName(), request))
            .assertNext(firstTokenInfo -> assertTokenInfoEqual("One", 0, 3, 0, firstTokenInfo))
            .assertNext(secondTokenInfo -> assertTokenInfoEqual("two", 4, 7, 1, secondTokenInfo))
            .expectNextCount(0L)
            .verifyComplete();

        request = new AnalyzeRequest()
            .setText("One's <two/>")
            .setTokenizer(TokenizerName.WHITESPACE)
            .setTokenFilters(Collections.singletonList(TokenFilterName.APOSTROPHE))
            .setCharFilters(Collections.singletonList(CharFilterName.HTML_STRIP));
        StepVerifier
            .create(searchServiceClient.analyzeText(index.getName(), request))
            .assertNext(onlyTokenInfo -> {
                // End offset is based on the original token, not the one emitted by the filters.
                assertTokenInfoEqual("One", 0, 5, 0, onlyTokenInfo);
            })
            .verifyComplete();

        StepVerifier
            .create(searchServiceClient.analyzeText(index.getName(), request, generateRequestOptions()))
            .assertNext(onlyTokenInfo -> {
                // End offset is based on the original token, not the one emitted by the filters.
                assertTokenInfoEqual("One", 0, 5, 0, onlyTokenInfo);
            })
            .verifyComplete();
    }

    @Test
    public void canAnalyzeWithAllPossibleNames() {
        Index index = createTestIndex();
        searchServiceClient.createIndex(index).block();

        AnalyzerName.values()
            .stream()
            .map(an -> new AnalyzeRequest()
                .setText("One two")
                .setAnalyzer(an))
            .forEach(r ->
                searchServiceClient.analyzeText(index.getName(), r)
            );

        TokenizerName.values()
            .stream()
            .map(tn -> new AnalyzeRequest()
                .setText("One two")
                .setTokenizer(tn))
            .forEach(r -> searchServiceClient.analyzeText(index.getName(), r));

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setTokenizer(TokenizerName.WHITESPACE)
            .setTokenFilters(new ArrayList<>(TokenFilterName.values()))
            .setCharFilters(new ArrayList<>(CharFilterName.values()));
        searchServiceClient.analyzeText(index.getName(), request);
    }

    @Test
    public void addingCustomAnalyzerThrowsHttpExceptionByDefault() {
        Index index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new StopAnalyzer().setName("a1")
            ));
        searchServiceClient.createIndex(index).block();

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));

        assertHttpResponseExceptionAsync(
            searchServiceClient.createOrUpdateIndex(index),
            HttpResponseStatus.BAD_REQUEST,
            "Index update not allowed because it would cause downtime."
        );
    }

    @Test
    public void canAddCustomAnalyzerWithIndexDowntime() {
        Index index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new StopAnalyzer().setName("a1")
            ));
        searchServiceClient.createIndex(index).block();

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));

        StepVerifier
            .create(searchServiceClient.createOrUpdateIndexWithResponse(index,
                true, new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> assertAnalysisComponentsEqual(index, res.getValue()))
            .verifyComplete();
    }

    @Test
    public void canCreateAllAnalysisComponents() {
        Index index = prepareIndexWithAllAnalysisComponentTypes();

        StepVerifier
            .create(searchServiceClient.createIndex(index))
            .assertNext(res -> assertAnalysisComponentsEqual(index, res))
            .verifyComplete();
        searchServiceClient.deleteIndex(index.getName()).block();

        // We have to split up analysis components into two indexes, one where any components with optional properties
        // have defaults that are zero or null, and another where we need to specify the default values we
        // expect to get back from the REST API.
        Index indexWithSpecialDefaults = createIndexWithSpecialDefaults();
        Index expectedIndexWithSpecialDefaults = createExpectedIndexWithSpecialDefaults(indexWithSpecialDefaults);

        List<Index> splittedIndexWithSpecialDefaults = splitIndex(indexWithSpecialDefaults);
        List<Index> splittedExpectedIndexWithSpecialDefaults = splitIndex(expectedIndexWithSpecialDefaults);
        for (int j = 0; j < splittedIndexWithSpecialDefaults.size(); j++) {
            Index expected = splittedExpectedIndexWithSpecialDefaults.get(j);

            StepVerifier
                .create(searchServiceClient.createIndex(expected))
                .assertNext(res -> assertAnalysisComponentsEqual(expected, res))
                .verifyComplete();
            searchServiceClient.deleteIndex(expected.getName()).block();
        }
    }

    @Test
    public void canUseAllAnalysisComponentNames() {
        Index index = prepareIndexWithAllAnalysisComponentNames();

        StepVerifier
            .create(searchServiceClient.createIndex(index))
            .assertNext(res -> assertCustomAnalysisComponentsEqual(index, res))
            .verifyComplete();
    }

    @Test
    public void canUseAllRegexFlags() {
        Index index = createTestIndex()
            .setAnalyzers(RegexFlags.values()
                .stream()
                .map(rf -> new PatternAnalyzer()
                    .setLowerCaseTerms(true)
                    .setPattern(".*")
                    .setFlags(rf)
                    .setName(generateName()))
                .collect(Collectors.toList()));

        StepVerifier
            .create(searchServiceClient.createIndex(index))
            .assertNext(res -> assertAnalysisComponentsEqual(index, res))
            .verifyComplete();
    }

    @Test
    public void canUseAllAnalysisComponentOptions() {
        List<Index> indexes = prepareIndexesWithAllAnalysisComponentOptions();

        indexes.forEach(expectedIndex -> {
            StepVerifier
                .create(searchServiceClient.createIndex(expectedIndex))
                .assertNext(res -> assertAnalysisComponentsEqual(expectedIndex, res))
                .verifyComplete();
            searchServiceClient.deleteIndex(expectedIndex.getName()).block();
        });
    }
}
