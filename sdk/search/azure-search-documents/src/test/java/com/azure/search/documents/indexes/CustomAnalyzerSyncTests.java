// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.AnalyzeRequest;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.AsciiFoldingTokenFilter;
import com.azure.search.documents.indexes.models.CharFilter;
import com.azure.search.documents.indexes.models.CharFilterName;
import com.azure.search.documents.indexes.models.CjkBigramTokenFilter;
import com.azure.search.documents.indexes.models.CjkBigramTokenFilterScripts;
import com.azure.search.documents.indexes.models.ClassicTokenizer;
import com.azure.search.documents.indexes.models.CommonGramTokenFilter;
import com.azure.search.documents.indexes.models.CustomAnalyzer;
import com.azure.search.documents.indexes.models.DictionaryDecompounderTokenFilter;
import com.azure.search.documents.indexes.models.EdgeNGramTokenFilter;
import com.azure.search.documents.indexes.models.EdgeNGramTokenFilterSide;
import com.azure.search.documents.indexes.models.EdgeNGramTokenizer;
import com.azure.search.documents.indexes.models.ElisionTokenFilter;
import com.azure.search.documents.indexes.models.KeepTokenFilter;
import com.azure.search.documents.indexes.models.KeywordMarkerTokenFilter;
import com.azure.search.documents.indexes.models.KeywordTokenizer;
import com.azure.search.documents.indexes.models.LengthTokenFilter;
import com.azure.search.documents.indexes.models.LexicalAnalyzer;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.LexicalTokenizer;
import com.azure.search.documents.indexes.models.LexicalTokenizerName;
import com.azure.search.documents.indexes.models.LimitTokenFilter;
import com.azure.search.documents.indexes.models.LuceneStandardAnalyzer;
import com.azure.search.documents.indexes.models.LuceneStandardTokenizer;
import com.azure.search.documents.indexes.models.MappingCharFilter;
import com.azure.search.documents.indexes.models.MicrosoftLanguageStemmingTokenizer;
import com.azure.search.documents.indexes.models.MicrosoftLanguageTokenizer;
import com.azure.search.documents.indexes.models.MicrosoftStemmingTokenizerLanguage;
import com.azure.search.documents.indexes.models.MicrosoftTokenizerLanguage;
import com.azure.search.documents.indexes.models.NGramTokenFilter;
import com.azure.search.documents.indexes.models.NGramTokenizer;
import com.azure.search.documents.indexes.models.PathHierarchyTokenizer;
import com.azure.search.documents.indexes.models.PatternAnalyzer;
import com.azure.search.documents.indexes.models.PatternCaptureTokenFilter;
import com.azure.search.documents.indexes.models.PatternReplaceCharFilter;
import com.azure.search.documents.indexes.models.PatternReplaceTokenFilter;
import com.azure.search.documents.indexes.models.PatternTokenizer;
import com.azure.search.documents.indexes.models.PhoneticEncoder;
import com.azure.search.documents.indexes.models.PhoneticTokenFilter;
import com.azure.search.documents.indexes.models.RegexFlags;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.ShingleTokenFilter;
import com.azure.search.documents.indexes.models.SnowballTokenFilter;
import com.azure.search.documents.indexes.models.SnowballTokenFilterLanguage;
import com.azure.search.documents.indexes.models.StemmerOverrideTokenFilter;
import com.azure.search.documents.indexes.models.StemmerTokenFilter;
import com.azure.search.documents.indexes.models.StemmerTokenFilterLanguage;
import com.azure.search.documents.indexes.models.StopAnalyzer;
import com.azure.search.documents.indexes.models.StopwordsList;
import com.azure.search.documents.indexes.models.StopwordsTokenFilter;
import com.azure.search.documents.indexes.models.SynonymTokenFilter;
import com.azure.search.documents.indexes.models.TokenCharacterKind;
import com.azure.search.documents.indexes.models.TokenFilter;
import com.azure.search.documents.indexes.models.TokenFilterName;
import com.azure.search.documents.indexes.models.TruncateTokenFilter;
import com.azure.search.documents.indexes.models.UaxUrlEmailTokenizer;
import com.azure.search.documents.indexes.models.UniqueTokenFilter;
import com.azure.search.documents.indexes.models.WordDelimiterTokenFilter;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.generateRequestOptions;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CustomAnalyzerSyncTests extends SearchTestBase {
    private static final String NAME_PREFIX = "azsmnet";
    private static final Collection<CharFilterName> CHAR_FILTER_NAMES = new ArrayList<>(CharFilterName.values());

    private SearchIndexClient searchIndexClient;
    private final List<String> indexesToCleanup = new ArrayList<>();

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchIndexClient = getSearchIndexClientBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        for (String index : indexesToCleanup) {
            searchIndexClient.deleteIndex(index);
        }
    }

    @Test
    public void canSearchWithCustomAnalyzer() {
        final LexicalAnalyzerName customLexicalAnalyzerName = LexicalAnalyzerName.fromString("my_email_analyzer");
        final CharFilterName customCharFilterName = CharFilterName.fromString("my_email_filter");

        SearchIndex index = new SearchIndex()
            .setName(randomIndexName("testindex"))
            .setFields(Arrays.asList(
                new SearchField()
                    .setName("id")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(true),
                new SearchField()
                    .setName("message")
                    .setType(SearchFieldDataType.STRING)
                    .setAnalyzerName(customLexicalAnalyzerName)
                    .setSearchable(true)
            ))
            .setAnalyzers(Collections.singletonList(
                new CustomAnalyzer()
                    .setTokenizer(LexicalTokenizerName.STANDARD)
                    .setCharFilters(Collections.singletonList(customCharFilterName))
                    .setName(customLexicalAnalyzerName.toString())
            ))
            .setCharFilters(Collections.singletonList(
                new PatternReplaceCharFilter()
                    .setPattern("@")
                    .setReplacement("_")
                    .setName(customCharFilterName.toString())
            ));

        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());
        SearchClient searchClient = searchIndexClient.getSearchClient(index.getName());

        SearchDocument document1 = new SearchDocument();
        document1.put("id", "1");
        document1.put("message", "My email is someone@somewhere.something.");
        SearchDocument document2 = new SearchDocument();
        document2.put("id", "2");
        document2.put("message", "His email is someone@nowhere.nothing.");
        List<SearchDocument> documents = Arrays.asList(document1, document2);

        searchClient.uploadDocuments(documents);
        waitForIndexing();

        Iterator<SearchResult> iterator = searchClient
            .search("someone@somewhere.something", new SearchOptions(), generateRequestOptions(), Context.NONE)
            .iterator();
        SearchResult searchResult = iterator.next();

        Assertions.assertEquals("1", searchResult.getDocument().get("id"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canUseAllAnalyzerNamesInIndexDefinition() {
        SearchIndex index = prepareIndexWithAllLexicalAnalyzerNames();
        indexesToCleanup.add(index.getName());
        SearchIndex res = searchIndexClient.createIndex(index);

        assertObjectEquals(index, res, true, "etag");

        // Add language analyzers to searchAnalyzer and indexAnalyzer properties and expect failure
        try {
            new SearchField()
                .setName("field")
                .setType(SearchFieldDataType.STRING)
                .setSearchAnalyzerName(LexicalAnalyzerName.EN_LUCENE);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("Only non-language analyzer can be used as search analyzer.", ex.getMessage());
        }
        try {
            new SearchField()
                .setName("field")
                .setType(SearchFieldDataType.STRING)
                .setIndexAnalyzerName(LexicalAnalyzerName.AR_MICROSOFT);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("Only non-language analyzer can be used as index analyzer.", ex.getMessage());
        }
    }

    @Test
    public void canAnalyze() {
        SearchIndex index = createTestIndex();
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setAnalyzer(LexicalAnalyzerName.WHITESPACE);
        PagedIterable<AnalyzedTokenInfo> results = searchIndexClient.analyzeText(index.getName(), request);
        Iterator<AnalyzedTokenInfo> iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 3, 0, iterator.next());
        assertTokenInfoEqual("two", 4, 7, 1, iterator.next());
        assertFalse(iterator.hasNext());

        request = new AnalyzeRequest()
            .setText("One's <two/>")
            .setTokenizer(LexicalTokenizerName.WHITESPACE)
            .setTokenFilters(Collections.singletonList(TokenFilterName.APOSTROPHE))
            .setCharFilters(Collections.singletonList(CharFilterName.HTML_STRIP));
        results = searchIndexClient.analyzeText(index.getName(), request);
        // End offset is based on the original token, not the one emitted by the filters.
        iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 5, 0, iterator.next());
        assertFalse(iterator.hasNext());

        results = searchIndexClient.analyzeText(index.getName(), request, generateRequestOptions(), Context.NONE);
        // End offset is based on the original token, not the one emitted by the filters.
        iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 5, 0, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canAnalyzeWithAllPossibleNames() {
        SearchIndex index = createTestIndex();
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        LexicalAnalyzerName.values()
            .stream()
            .map(an -> new AnalyzeRequest()
                .setText("One two")
                .setAnalyzer(an))
            .forEach(r -> searchIndexClient.analyzeText(index.getName(), r));

        LexicalTokenizerName.values()
            .stream()
            .map(tn -> new AnalyzeRequest()
                .setText("One two")
                .setTokenizer(tn))
            .forEach(r -> searchIndexClient.analyzeText(index.getName(), r));

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setTokenizer(LexicalTokenizerName.WHITESPACE)
            .setTokenFilters(new ArrayList<>(TokenFilterName.values()))
            .setCharFilters(new ArrayList<>(CharFilterName.values()));
        searchIndexClient.analyzeText(index.getName(), request);
    }

    @Test
    public void addingCustomAnalyzerThrowsHttpExceptionByDefault() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(new StopAnalyzer().setName("a1")));
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));

        assertHttpResponseException(
            () -> searchIndexClient.createOrUpdateIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Index update not allowed because it would cause downtime."
        );
    }

    @Test
    public void canAddCustomAnalyzerWithIndexDowntime() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(new StopAnalyzer().setName("a1")));
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));
        SearchIndex updatedIndex = searchIndexClient.createOrUpdateIndexWithResponse(index,
            true, false, generateRequestOptions(), Context.NONE).getValue();

        assertAnalysisComponentsEqual(index, updatedIndex);
    }

    @Test
    public void canCreateAllAnalysisComponents() {
        SearchIndex index = prepareIndexWithAllAnalysisComponentTypes();

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());
        assertAnalysisComponentsEqual(index, createdIndex);
        searchIndexClient.deleteIndex(index.getName());

        // We have to split up analysis components into two indexes, one where any components with optional properties
        // have defaults that are zero or null, and another where we need to specify the default values we
        // expect to get back from the REST API.

        SearchIndex indexWithSpecialDefaults = createIndexWithSpecialDefaults();
        SearchIndex expectedIndexWithSpecialDefaults = createExpectedIndexWithSpecialDefaults(indexWithSpecialDefaults);

        List<SearchIndex> splittedIndexWithSpecialDefaults = splitIndex(indexWithSpecialDefaults);
        List<SearchIndex> splittedExpectedIndexWithSpecialDefaults = splitIndex(expectedIndexWithSpecialDefaults);
        for (int j = 0; j < splittedIndexWithSpecialDefaults.size(); j++) {
            SearchIndex expected = splittedExpectedIndexWithSpecialDefaults.get(j);
            SearchIndex actual = searchIndexClient.createIndex(expected);
            assertAnalysisComponentsEqual(expected, actual);
            searchIndexClient.deleteIndex(actual.getName());
        }
    }

    @Test
    public void canUseAllAnalysisComponentNames() {
        SearchIndex index = prepareIndexWithAllAnalysisComponentNames();

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());
        assertCustomAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(new PatternAnalyzer()
                .setStopwords(Arrays.asList("stop1", "stop2"))
                .setLowerCaseTerms(true)
                .setPattern(".*")
                .setFlags(new ArrayList<>(RegexFlags.values()))
                .setName(generateName())));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(null);

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(new ArrayList<>());

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullNameAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new PatternAnalyzer().setName(null)
            ));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "The name field is required."
        );
    }

    @Test
    public void canUseAllRegexFlagsEmptyNameAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new PatternAnalyzer().setName("")
            ));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "The name field is required."
        );
    }

    @Test
    public void canUseAllRegexFlagsNullLowerCaseAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new PatternAnalyzer().setLowerCaseTerms(null).setName(generateName())
            ));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullPatternAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new PatternAnalyzer().setPattern(null).setName(generateName())
            ));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyPatternAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new PatternAnalyzer().setPattern("").setName(generateName())
            ));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullFlagsAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new PatternAnalyzer().setFlags(null).setName(generateName())
            ));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyFlagsAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(
                new PatternAnalyzer().setFlags(new ArrayList<>()).setName(generateName())
            ));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Values of property \\\"flags\\\" must belong to the set of allowed values"
        );
    }

    @Test
    public void canUseAllRegexFlagsNullStopwordsAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(new PatternAnalyzer()
                .setStopwords(null)
                .setName(generateName())));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyStopwordsAnalyzer() {
        SearchIndex index = createTestIndex()
            .setAnalyzers(Collections.singletonList(new PatternAnalyzer()
                .setStopwords(new ArrayList<>())
                .setName(generateName())));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setPattern(".*")
                .setFlags(new ArrayList<>(RegexFlags.values()))
                .setGroup(0)
                .setName(generateName())));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(null);

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(new ArrayList<>());

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullNameTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setName(null)
            ));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "The name field is required."
        );
    }

    @Test
    public void canUseAllRegexFlagsEmptyNameTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setName("")
            ));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "The name field is required."
        );
    }

    @Test
    public void canUseAllRegexFlagsNullPatternTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setPattern(null).setName(generateName())
            ));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyPatternTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setPattern("").setName(generateName())
            ));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullFlagsTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setFlags(null).setName(generateName())
            ));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
        System.out.println(RegexFlags.values());
    }

    @Test
    public void canUseAllRegexFlagsEmptyFlagsTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setFlags(new ArrayList<>()).setName(generateName())
            ));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Values of property \\\"flags\\\" must belong to the set of allowed values"
        );
    }

    @Test
    public void canUseAllRegexFlagsNullGroupTokenizer() {
        SearchIndex index = createTestIndex()
            .setTokenizers(Collections.singletonList(new PatternTokenizer()
                .setGroup(null)
                .setName(generateName())));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllAnalysisComponentOptions() {
        List<SearchIndex> indexes = prepareIndexesWithAllAnalysisComponentOptions();

        indexes.forEach(expectedIndex -> {
            SearchIndex createdIndex = searchIndexClient.createIndex(expectedIndex);
            indexesToCleanup.add(expectedIndex.getName());
            assertAnalysisComponentsEqual(expectedIndex, createdIndex);
            searchIndexClient.deleteIndex(createdIndex.getName());
        });
    }

    void addAnalyzerToIndex(SearchIndex index, LexicalAnalyzer analyzer) {
        List<LexicalAnalyzer> analyzers = new ArrayList<>(index.getAnalyzers());
        analyzers.add(analyzer);

        index.setAnalyzers(analyzers);
    }

    void assertAnalysisComponentsEqual(SearchIndex expected, SearchIndex actual) {
        // Compare analysis components directly so that test failures show better comparisons.
        // Analyzers
        assertAnalyzersEqual(expected.getAnalyzers(), actual.getAnalyzers());

        // LexicalTokenizer
        assertLexicalTokenizersEqual(expected.getTokenizers(), actual.getTokenizers());

        // Char filter
        assertCharFiltersEqual(expected.getCharFilters(), actual.getCharFilters());
    }

    void assertCustomAnalysisComponentsEqual(SearchIndex expected, SearchIndex actual) {
        // Compare analysis components directly so that test failures show better comparisons.
        // Analyzers - Sort according to their LexicalTokenizers before comparing:
        List<LexicalAnalyzer> expectedAnalyzers = expected.getAnalyzers();
        List<LexicalAnalyzer> actualAnalyzers = actual.getAnalyzers();

        if (expectedAnalyzers != null && actualAnalyzers != null) {
            Comparator<LexicalAnalyzer> customAnalyzerComparator = Comparator
                .comparing((LexicalAnalyzer a) -> ((CustomAnalyzer) a).getTokenizer().toString());

            expectedAnalyzers.sort(customAnalyzerComparator);
            actualAnalyzers.sort(customAnalyzerComparator);

            assertAnalyzersEqual(expectedAnalyzers, actualAnalyzers);
        }

        // LexicalTokenizer
        assertLexicalTokenizersEqual(expected.getTokenizers(), actual.getTokenizers());

        // Char filter
        assertCharFiltersEqual(expected.getCharFilters(), actual.getCharFilters());
    }

    private void assertAnalyzersEqual(List<LexicalAnalyzer> expected, List<LexicalAnalyzer> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertObjectEquals(expected.get(i).setName("none"), actual.get(i).setName("none"), true);
            }
        }
    }

    private void assertLexicalTokenizersEqual(List<LexicalTokenizer> expected, List<LexicalTokenizer> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertObjectEquals(expected.get(i).setName("none"), actual.get(i).setName("none"), true);
            }
        }
    }

    private void assertCharFiltersEqual(List<CharFilter> expected, List<CharFilter> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertObjectEquals(expected.get(i).setName("none"), actual.get(i).setName("none"), true);
            }
        }
    }

    String generateName() {
        return testResourceNamer.randomName(NAME_PREFIX, 24);
    }

    List<SearchIndex> prepareIndexesWithAllAnalysisComponentOptions() {
        SearchIndex index = createTestIndex();

        // Set tokenizers
        List<LexicalTokenizer> tokenizers = new ArrayList<>();
        tokenizers.add(
            new EdgeNGramTokenizer()
                .setMinGram(1)
                .setMaxGram(2)
                .setTokenChars(Arrays.asList(TokenCharacterKind.values()))
                .setName(generateName())
        );
        tokenizers.addAll(
            Arrays.stream(MicrosoftStemmingTokenizerLanguage.values())
                .map(mtl -> new MicrosoftLanguageStemmingTokenizer()
                    .setMaxTokenLength(200)
                    .setIsSearchTokenizer(false)
                    .setLanguage(mtl)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        index.setTokenizers(tokenizers);

        // Set token filters
        List<TokenFilter> tokenFilters = new ArrayList<>();
        tokenFilters.add(new CjkBigramTokenFilter()
            .setIgnoreScripts(Arrays.asList(CjkBigramTokenFilterScripts.values()))
            .setOutputUnigrams(true)
            .setName(generateName()));
        tokenFilters.addAll(
            Arrays.stream(EdgeNGramTokenFilterSide.values())
                .map(s -> new EdgeNGramTokenFilter()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setSide(s)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(PhoneticEncoder.values())
                .map(pe -> new PhoneticTokenFilter()
                    .setEncoder(pe)
                    .setReplaceOriginalTokens(false)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(SnowballTokenFilterLanguage.values())
                .map(l -> new SnowballTokenFilter()
                    .setLanguage(l)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(StemmerTokenFilterLanguage.values())
                .map(l -> new StemmerTokenFilter()
                    .setLanguage(l)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(StopwordsList.values())
                .map(l -> new StopwordsTokenFilter()
                    .setStopwordsList(l)
                    .setIgnoreCase(false)
                    .setRemoveTrailingStopWords(true)
                    .setName(generateName())
                )
                .collect(Collectors.toList())
        );
        index.setTokenFilters(tokenFilters);

        return splitIndex(index);
    }

    SearchIndex prepareIndexWithAllLexicalAnalyzerNames() {
        List<LexicalAnalyzerName> allLexicalAnalyzerNames = new ArrayList<>(LexicalAnalyzerName.values());
        allLexicalAnalyzerNames.sort(Comparator.comparing(LexicalAnalyzerName::toString));

        List<SearchField> fields = new ArrayList<>();
        int fieldNumber = 0;

        // All analyzer names can be set on the analyzer property.
        for (int i = 0; i < allLexicalAnalyzerNames.size(); i++) {
            SearchFieldDataType fieldType = (i % 2 == 0) ? SearchFieldDataType.STRING
                : SearchFieldDataType.collection(SearchFieldDataType.STRING);
            fields.add(new SearchField()
                .setName("field" + (fieldNumber++))
                .setType(fieldType)
                .setAnalyzerName(allLexicalAnalyzerNames.get(i)));
        }

        List<LexicalAnalyzerName> searchAnalyzersAndIndexAnalyzers = getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer();

        for (int i = 0; i < searchAnalyzersAndIndexAnalyzers.size(); i++) {
            SearchFieldDataType fieldType = (i % 2 == 0) ? SearchFieldDataType.STRING
                : SearchFieldDataType.collection(SearchFieldDataType.STRING);
            fields.add(new SearchField()
                .setName("field" + (fieldNumber++))
                .setType(fieldType)
                .setSearchable(true)
                .setSearchAnalyzerName(searchAnalyzersAndIndexAnalyzers.get(i))
                .setIndexAnalyzerName(searchAnalyzersAndIndexAnalyzers.get(i)));
        }

        fields.add(new SearchField()
            .setName("id")
            .setType(SearchFieldDataType.STRING)
            .setKey(true));

        return new SearchIndex()
            .setName(randomIndexName("hotel"))
            .setFields(fields);
    }

    SearchIndex prepareIndexWithAllAnalysisComponentNames() {
        LexicalAnalyzer analyzerWithAllTokenFilterAndCharFilters =
            new CustomAnalyzer()
                .setTokenizer(LexicalTokenizerName.LOWERCASE)
                .setTokenFilters(TokenFilterName.values()
                    .stream()
                    .sorted(Comparator.comparing(TokenFilterName::toString))
                    .collect(Collectors.toList()))
                .setCharFilters(CHAR_FILTER_NAMES
                    .stream()
                    .sorted(Comparator.comparing(CharFilterName::toString))
                    .collect(Collectors.toList()))
                .setName("abc");

        SearchIndex index = createTestIndex();
        List<LexicalAnalyzer> analyzers = new ArrayList<>();
        analyzers.add(analyzerWithAllTokenFilterAndCharFilters);
        analyzers.addAll(LexicalTokenizerName.values()
            .stream()
            .sorted(Comparator.comparing(LexicalTokenizerName::toString))
            .map(tn -> new CustomAnalyzer()
                .setTokenizer(tn)
                .setName(generateName()))
            .collect(Collectors.toList()));

        analyzers.sort(Comparator.comparing(LexicalAnalyzer::getName));
        index.setAnalyzers(analyzers);

        return index;
    }

    /**
     * Custom analysis components (analyzer/tokenzier/tokenFilter/charFilter) count in index must be between 0 and 50.
     * Split an Index into indexes, each of which has a total analysis components count within the limit.
     */
    List<SearchIndex> splitIndex(SearchIndex index) {
        Collection<List<LexicalAnalyzer>> analyzersLists = splitAnalysisComponents(index.getAnalyzers());
        List<SearchIndex> indexes = analyzersLists
            .stream()
            .map(a -> createTestIndex().setAnalyzers(a)).collect(Collectors.toList());

        Collection<List<LexicalTokenizer>> tokenizersLists = splitAnalysisComponents(index.getTokenizers());
        indexes.addAll(tokenizersLists
            .stream()
            .map(t -> createTestIndex().setTokenizers(t))
            .collect(Collectors.toList()));

        Collection<List<TokenFilter>> tokenFiltersLists = splitAnalysisComponents(index.getTokenFilters());
        indexes.addAll(tokenFiltersLists
            .stream()
            .map(tf -> createTestIndex().setTokenFilters(tf))
            .collect(Collectors.toList()));

        Collection<List<CharFilter>> charFiltersLists = splitAnalysisComponents(index.getCharFilters());
        indexes.addAll(charFiltersLists
            .stream()
            .map(cf -> createTestIndex().setCharFilters(cf))
            .collect(Collectors.toList()));

        return indexes;
    }

    /**
     * Custom analysis components (analyzer/tokenzier/tokenFilter/charFilter) count in index must be between 0 and 50.
     * Split a list of analysis components into lists within the limit.
     */
    private <T> Collection<List<T>> splitAnalysisComponents(List<T> list) {
        final int analysisComponentLimit = 50;
        Collection<List<T>> lists = new HashSet<>();

        if (list != null && !list.isEmpty()) {
            if (list.size() > analysisComponentLimit) {
                AtomicInteger counter = new AtomicInteger();
                lists = list.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / analysisComponentLimit))
                    .values();
            } else {
                lists.add(list);
            }
        }

        return lists;
    }

    SearchIndex prepareIndexWithAllAnalysisComponentTypes() {
        final LexicalTokenizerName customTokenizerName = LexicalTokenizerName.fromString("my_tokenizer");
        final TokenFilterName customTokenFilterName = TokenFilterName.fromString("my_tokenfilter");
        final CharFilterName customCharFilterName = CharFilterName.fromString("my_charfilter");

        return createTestIndex()
            .setAnalyzers(Arrays.asList(
                new CustomAnalyzer()
                    .setTokenizer(customTokenizerName)
                    .setTokenFilters(Collections.singletonList(customTokenFilterName))
                    .setCharFilters(Collections.singletonList(customCharFilterName))
                    .setName(generateName()),
                new CustomAnalyzer()
                    .setTokenizer(LexicalTokenizerName.EDGE_NGRAM)
                    .setName(generateName()),
                new PatternAnalyzer()
                    .setLowerCaseTerms(false)
                    .setPattern("abc")
                    .setFlags(Collections.singletonList(RegexFlags.DOT_ALL))
                    .setStopwords(Collections.singletonList("the"))
                    .setName(generateName()),
                new LuceneStandardAnalyzer()
                    .setMaxTokenLength(100)
                    .setStopwords(Collections.singletonList("the"))
                    .setName(generateName()),
                new StopAnalyzer()
                    .setStopwords(Collections.singletonList("the"))
                    .setName(generateName()),
                new StopAnalyzer()
                    .setName(generateName())
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(customTokenizerName.toString()),
                new EdgeNGramTokenizer()
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(Collections.singletonList(TokenCharacterKind.LETTER))
                    .setName(generateName()),
                new NGramTokenizer()
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(Collections.singletonList(TokenCharacterKind.LETTER))
                    .setName(generateName()),
                new ClassicTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new KeywordTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new MicrosoftLanguageStemmingTokenizer()
                    .setMaxTokenLength(100)
                    .setIsSearchTokenizer(true)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.CROATIAN)
                    .setName(generateName()),
                new MicrosoftLanguageTokenizer()
                    .setMaxTokenLength(100)
                    .setIsSearchTokenizer(true)
                    .setLanguage(MicrosoftTokenizerLanguage.THAI)
                    .setName(generateName()),
                new PathHierarchyTokenizer()
                    .setDelimiter(":")
                    .setReplacement("_")
                    .setMaxTokenLength(300)
                    .setReverseTokenOrder(true)
                    .setNumberOfTokensToSkip(2)
                    .setName(generateName()),
                new PatternTokenizer()
                    .setPattern(".*")
                    .setFlags(Collections.singletonList(RegexFlags.MULTILINE))
                    .setGroup(0)
                    .setName(generateName()),
                new LuceneStandardTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new UaxUrlEmailTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName())
            ))
            .setTokenFilters(Arrays.asList(
                new CjkBigramTokenFilter()
                    .setName(customTokenFilterName.toString()),  // One custom token filter for CustomAnalyzer above.
                new CjkBigramTokenFilter()
                    .setIgnoreScripts(Collections.singletonList(CjkBigramTokenFilterScripts.HAN))
                    .setOutputUnigrams(true)
                    .setName(generateName()),
                new CjkBigramTokenFilter()
                    .setName(generateName()),
                new AsciiFoldingTokenFilter()
                    .setPreserveOriginal(true)
                    .setName(generateName()),
                new AsciiFoldingTokenFilter()
                    .setName(generateName()),
                new CommonGramTokenFilter()
                    .setCommonWords(Arrays.asList("hello", "goodbye"))
                    .setIgnoreCase(true)
                    .setUseQueryMode(true)
                    .setName(generateName()),
                new CommonGramTokenFilter()
                    .setCommonWords(Collections.singletonList("at"))
                    .setName(generateName()),
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Collections.singletonList("Schadenfreude"))
                    .setMinWordSize(10)
                    .setMinSubwordSize(5)
                    .setMaxSubwordSize(13)
                    .setOnlyLongestMatch(true)
                    .setName(generateName()),
                new EdgeNGramTokenFilter()
                    .setMinGram(2)
                    .setMaxGram(10)
                    .setSide(EdgeNGramTokenFilterSide.BACK)
                    .setName(generateName()),
                new ElisionTokenFilter()
                    .setArticles(Collections.singletonList("a"))
                    .setName(generateName()),
                new ElisionTokenFilter()
                    .setName(generateName()),
                new KeepTokenFilter()
                    .setKeepWords(Collections.singletonList("aloha"))
                    .setName(generateName()),
                new KeepTokenFilter()
                    .setKeepWords(Arrays.asList("e", "komo", "mai"))
                    .setName(generateName()),
                new KeywordMarkerTokenFilter()
                    .setKeywords(Arrays.asList("key", "words"))
                    .setName(generateName()),
                new KeywordMarkerTokenFilter()
                    .setKeywords(Collections.singletonList("essential"))
                    .setName(generateName()),
                new LengthTokenFilter()
                    .setMinLength(5)
                    .setMaxLength(10)
                    .setName(generateName()),
                new LimitTokenFilter()
                    .setMaxTokenCount(10)
                    .setConsumeAllTokens(true)
                    .setName(generateName()),
                new NGramTokenFilter()
                    .setMinGram(2)
                    .setMaxGram(3)
                    .setName(generateName()),
                new PatternCaptureTokenFilter()
                    .setPatterns(Collections.singletonList(".*"))
                    .setPreserveOriginal(false)
                    .setName(generateName()),
                new PatternReplaceTokenFilter()
                    .setPattern("abc")
                    .setReplacement("123")
                    .setName(generateName()),
                new PhoneticTokenFilter()
                    .setEncoder(PhoneticEncoder.SOUNDEX)
                    .setReplaceOriginalTokens(false)
                    .setName(generateName()),
                new ShingleTokenFilter()
                    .setMaxShingleSize(10)
                    .setMinShingleSize(5)
                    .setOutputUnigrams(false)
                    .setOutputUnigramsIfNoShingles(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("|")
                    .setName(generateName()),
                new SnowballTokenFilter()
                    .setLanguage(SnowballTokenFilterLanguage.ENGLISH)
                    .setName(generateName()),
                new StemmerOverrideTokenFilter()
                    .setRules(Collections.singletonList("ran => run"))
                    .setName(generateName()),
                new StemmerTokenFilter()
                    .setLanguage(StemmerTokenFilterLanguage.FRENCH)
                    .setName(generateName()),
                new StopwordsTokenFilter()
                    .setStopwords(Arrays.asList("a", "the"))
                    .setIgnoreCase(true)
                    .setRemoveTrailingStopWords(false)
                    .setName(generateName()),
                new StopwordsTokenFilter()
                    .setStopwordsList(StopwordsList.ITALIAN)
                    .setIgnoreCase(true)
                    .setRemoveTrailingStopWords(false)
                    .setName(generateName()),
                new SynonymTokenFilter()
                    .setSynonyms(Collections.singletonList("great, good"))
                    .setIgnoreCase(true)
                    .setExpand(false)
                    .setName(generateName()),
                new TruncateTokenFilter()
                    .setLength(10)
                    .setName(generateName()),
                new UniqueTokenFilter()
                    .setOnlyOnSamePosition(true)
                    .setName(generateName()),
                new UniqueTokenFilter()
                    .setName(generateName()),
                new WordDelimiterTokenFilter()
                    .setGenerateWordParts(false)
                    .setGenerateNumberParts(false)
                    .setCatenateWords(true)
                    .setCatenateNumbers(true)
                    .setCatenateAll(true)
                    .setSplitOnCaseChange(false)
                    .setPreserveOriginal(true)
                    .setSplitOnNumerics(false)
                    .setStemEnglishPossessive(false)
                    .setProtectedWords(Collections.singletonList("protected"))
                    .setName(generateName())
            ))
            .setCharFilters(Arrays.asList(
                new MappingCharFilter()
                    .setMappings(Collections.singletonList("a => b")) // One custom char filter for CustomeAnalyer above.
                    .setName(customCharFilterName.toString()),
                new MappingCharFilter()
                    .setMappings(Arrays.asList("s => $", "S => $"))
                    .setName(generateName()),
                new PatternReplaceCharFilter()
                    .setPattern("abc")
                    .setReplacement("123")
                    .setName(generateName())
            ));
    }

    SearchIndex createIndexWithSpecialDefaults() {
        int i = 0;

        return createTestIndex()
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer()
                    .setName(generateSimpleName(i++)),
                new LuceneStandardAnalyzer()
                    .setName(generateSimpleName(i++))
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setName(generateSimpleName(i++)),
                new NGramTokenizer()
                    .setName(generateSimpleName(i++)),
                new ClassicTokenizer()
                    .setName(generateSimpleName(i++)),
                new KeywordTokenizer()
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageStemmingTokenizer()
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageTokenizer()
                    .setName(generateSimpleName(i++)),
                new PathHierarchyTokenizer()
                    .setName(generateSimpleName(i++)),
                new PatternTokenizer()
                    .setName(generateSimpleName(i++)),
                new LuceneStandardTokenizer()
                    .setName(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer()
                    .setName(generateSimpleName(i++))
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Collections.singletonList("Bahnhof"))
                    .setName(generateSimpleName(i++)),
                new EdgeNGramTokenFilter()
                    .setName(generateSimpleName(i++)),
                new LengthTokenFilter()
                    .setName(generateSimpleName(i++)),
                new LimitTokenFilter()
                    .setName(generateSimpleName(i++)),
                new NGramTokenFilter()
                    .setName(generateSimpleName(i++)),
                new PatternCaptureTokenFilter()
                    .setPatterns(Collections.singletonList("[a-z]*"))
                    .setName(generateSimpleName(i++)),
                new PhoneticTokenFilter()
                    .setName(generateSimpleName(i++)),
                new ShingleTokenFilter()
                    .setName(generateSimpleName(i++)),
                new StopwordsTokenFilter()
                    .setName(generateSimpleName(i++)),
                new SynonymTokenFilter()
                    .setSynonyms(Collections.singletonList("mutt, canine => dog"))
                    .setName(generateSimpleName(i++)),
                new TruncateTokenFilter()
                    .setName(generateSimpleName(i++)),
                new WordDelimiterTokenFilter()
                    .setName(generateSimpleName(i))
            ));
    }

    SearchIndex createExpectedIndexWithSpecialDefaults(SearchIndex index) {
        int i = 0;

        return createTestIndex()
            .setName(index.getName())
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer()
                    .setLowerCaseTerms(true)
                    .setPattern("\\W+")
                    .setName(generateSimpleName(i++)),
                new LuceneStandardAnalyzer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++))
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(generateSimpleName(i++)),
                new NGramTokenizer()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(generateSimpleName(i++)),
                new ClassicTokenizer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++)),
                new KeywordTokenizer()
                    .setMaxTokenLength(256)
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageStemmingTokenizer()
                    .setMaxTokenLength(255)
                    .setIsSearchTokenizer(false)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.ENGLISH)
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageTokenizer()
                    .setMaxTokenLength(255)
                    .setIsSearchTokenizer(false)
                    .setLanguage(MicrosoftTokenizerLanguage.ENGLISH)
                    .setName(generateSimpleName(i++)),
                new PathHierarchyTokenizer()
                    .setDelimiter("/")
                    .setReplacement("/")
                    .setMaxTokenLength(300)
                    .setName(generateSimpleName(i++)),
                new PatternTokenizer()
                    .setPattern("\\W+")
                    .setGroup(-1)
                    .setName(generateSimpleName(i++)),
                new LuceneStandardTokenizer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++))
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Collections.singletonList("Bahnhof"))
                    .setMinWordSize(5)
                    .setMinSubwordSize(2)
                    .setMaxSubwordSize(15)
                    .setName(generateSimpleName(i++)),
                new EdgeNGramTokenFilter()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setSide(EdgeNGramTokenFilterSide.FRONT)
                    .setName(generateSimpleName(i++)),
                new LengthTokenFilter()
                    .setMaxLength(300)
                    .setName(generateSimpleName(i++)),
                new LimitTokenFilter()
                    .setMaxTokenCount(1)
                    .setName(generateSimpleName(i++)),
                new NGramTokenFilter()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(generateSimpleName(i++)),
                new PatternCaptureTokenFilter()
                    .setPatterns(Collections.singletonList("[a-z]*"))
                    .setPreserveOriginal(true)
                    .setName(generateSimpleName(i++)),
                new PhoneticTokenFilter()
                    .setEncoder(PhoneticEncoder.METAPHONE)
                    .setReplaceOriginalTokens(true)
                    .setName(generateSimpleName(i++)),
                new ShingleTokenFilter()
                    .setMaxShingleSize(2)
                    .setMinShingleSize(2)
                    .setOutputUnigrams(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("_")
                    .setName(generateSimpleName(i++)),
                new StopwordsTokenFilter()
                    .setStopwordsList(StopwordsList.ENGLISH)
                    .setRemoveTrailingStopWords(true)
                    .setName(generateSimpleName(i++)),
                new SynonymTokenFilter()
                    .setExpand(true)
                    .setSynonyms(Collections.singletonList("mutt, canine => dog"))
                    .setName(generateSimpleName(i++)),
                new TruncateTokenFilter()
                    .setLength(300)
                    .setName(generateSimpleName(i++)),
                new WordDelimiterTokenFilter()
                    .setGenerateWordParts(true)
                    .setGenerateNumberParts(true)
                    .setSplitOnCaseChange(true)
                    .setSplitOnNumerics(true)
                    .setStemEnglishPossessive(true)
                    .setName(generateSimpleName(i))
            ));
    }

    void assertTokenInfoEqual(String expectedToken, Integer expectedStartOffset, Integer expectedEndOffset,
        Integer expectedPosition, AnalyzedTokenInfo actual) {
        assertEquals(expectedToken, actual.getToken());
        assertEquals(expectedStartOffset, actual.getStartOffset());
        assertEquals(expectedEndOffset, actual.getEndOffset());
        assertEquals(expectedPosition, actual.getPosition());
    }

    private String generateSimpleName(int n) {
        return String.format("a%d", n);
    }

    private List<LexicalAnalyzerName> getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer() {
        // Only non-language analyzer names can be set on the searchAnalyzer and indexAnalyzer properties.
        // ASSUMPTION: Only language analyzers end in .lucene or .microsoft.
        return LexicalAnalyzerName.values()
            .stream()
            .filter(an -> !an.toString().endsWith(".lucene") && !an.toString().endsWith(".microsoft"))
            .collect(Collectors.toList());
    }
}
