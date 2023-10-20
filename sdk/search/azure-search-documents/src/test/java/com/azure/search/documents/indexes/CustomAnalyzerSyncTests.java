// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
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

import java.lang.reflect.Field;
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
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomAnalyzerSyncTests extends SearchTestBase {
    private static final String NAME_PREFIX = "azsmnet";

    private static final List<TokenFilterName> TOKEN_FILTER_NAMES = getExpandableEnumValues(TokenFilterName.class);
    private static final List<CharFilterName> CHAR_FILTER_NAMES = getExpandableEnumValues(CharFilterName.class);
    private static final List<LexicalAnalyzerName> LEXICAL_ANALYZER_NAMES =
        getExpandableEnumValues(LexicalAnalyzerName.class);
    private static final List<LexicalTokenizerName> LEXICAL_TOKENIZER_NAMES =
        getExpandableEnumValues(LexicalTokenizerName.class);
    private static final List<RegexFlags> REGEX_FLAGS = getExpandableEnumValues(RegexFlags.class);

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

        SearchIndex index = new SearchIndex(randomIndexName("testindex"))
            .setFields(Arrays.asList(
                new SearchField("id", SearchFieldDataType.STRING)
                    .setKey(true),
                new SearchField("message", SearchFieldDataType.STRING)
                    .setAnalyzerName(customLexicalAnalyzerName)
                    .setSearchable(true)
            ))
            .setAnalyzers(new CustomAnalyzer(customLexicalAnalyzerName.toString(), LexicalTokenizerName.STANDARD)
                .setCharFilters(customCharFilterName))
            .setCharFilters(new PatternReplaceCharFilter(customCharFilterName.toString(), "@", "_"));

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
            .search("someone@somewhere.something", new SearchOptions(), Context.NONE)
            .iterator();
        SearchResult searchResult = iterator.next();

        Assertions.assertEquals("1", searchResult.getDocument(SearchDocument.class).get("id"));
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
            new SearchField("field", SearchFieldDataType.STRING)
                .setSearchAnalyzerName(LexicalAnalyzerName.EN_LUCENE);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("Only non-language analyzer can be used as search analyzer.", ex.getMessage());
        }
        try {
            new SearchField("field", SearchFieldDataType.STRING)
                .setIndexAnalyzerName(LexicalAnalyzerName.AR_MICROSOFT);
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
            assertEquals("Only non-language analyzer can be used as index analyzer.", ex.getMessage());
        }
    }

    @Test
    public void canAnalyze() {
        SearchIndex index = createTestIndex(null);
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        AnalyzeTextOptions request = new AnalyzeTextOptions("One two", LexicalAnalyzerName.WHITESPACE);
        PagedIterable<AnalyzedTokenInfo> results = searchIndexClient.analyzeText(index.getName(), request);
        Iterator<AnalyzedTokenInfo> iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 3, 0, iterator.next());
        assertTokenInfoEqual("two", 4, 7, 1, iterator.next());
        assertFalse(iterator.hasNext());

        request = new AnalyzeTextOptions("One's <two/>", LexicalTokenizerName.WHITESPACE)
            .setTokenFilters(TokenFilterName.APOSTROPHE)
            .setCharFilters(CharFilterName.HTML_STRIP);
        results = searchIndexClient.analyzeText(index.getName(), request);
        // End offset is based on the original token, not the one emitted by the filters.
        iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 5, 0, iterator.next());
        assertFalse(iterator.hasNext());

        results = searchIndexClient.analyzeText(index.getName(), request, Context.NONE);
        // End offset is based on the original token, not the one emitted by the filters.
        iterator = results.iterator();
        assertTokenInfoEqual("One", 0, 5, 0, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canAnalyzeWithAllPossibleNames() {
        SearchIndex index = createTestIndex(null);
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        LEXICAL_ANALYZER_NAMES.stream()
            .map(an -> new AnalyzeTextOptions("One two", an))
            .forEach(r -> searchIndexClient.analyzeText(index.getName(), r));

        LEXICAL_TOKENIZER_NAMES.stream()
            .map(tn -> new AnalyzeTextOptions("One two", tn))
            .forEach(r -> searchIndexClient.analyzeText(index.getName(), r));

        AnalyzeTextOptions request = new AnalyzeTextOptions("One two", LexicalTokenizerName.WHITESPACE)
            .setTokenFilters(TOKEN_FILTER_NAMES.toArray(new TokenFilterName[0]))
            .setCharFilters(CHAR_FILTER_NAMES.toArray(new CharFilterName[0]));
        searchIndexClient.analyzeText(index.getName(), request);
    }

    @Test
    public void addingCustomAnalyzerThrowsHttpExceptionByDefault() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new StopAnalyzer("a1"));
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        addAnalyzerToIndex(index, new StopAnalyzer("a2"));

        assertHttpResponseException(
            () -> searchIndexClient.createOrUpdateIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Index update not allowed because it would cause downtime."
        );
    }

    @Test
    public void canAddCustomAnalyzerWithIndexDowntime() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new StopAnalyzer("a1"));
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        addAnalyzerToIndex(index, new StopAnalyzer("a2"));
        SearchIndex updatedIndex = searchIndexClient.createOrUpdateIndexWithResponse(index,
            true, false, Context.NONE).getValue();

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
        SearchIndex index = createTestIndex(null).setAnalyzers(new PatternAnalyzer(generateName())
            .setStopwords("stop1", "stop2")
            .setLowerCaseTerms(true)
            .setPattern(".*")
            .setFlags(REGEX_FLAGS));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullAnalyzer() {
        SearchIndex index = createTestIndex(null)
            .setAnalyzers((List<LexicalAnalyzer>) null);

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyAnalyzer() {
        SearchIndex index = createTestIndex(null)
            .setAnalyzers(new ArrayList<>());

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullNameAnalyzer() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new PatternAnalyzer(null));

        assertThrows(HttpResponseException.class, () ->
            searchIndexClient.createIndex(index), "Missing required property name in model LexicalAnalyzer");

    }

    @Test
    public void canUseAllRegexFlagsEmptyNameAnalyzer() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new PatternAnalyzer(""));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "The name field is required."
        );
    }

    @Test
    public void canUseAllRegexFlagsNullLowerCaseAnalyzer() {
        SearchIndex index = createTestIndex(null)
            .setAnalyzers(new PatternAnalyzer(generateName()).setLowerCaseTerms(null));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullPatternAnalyzer() {
        SearchIndex index = createTestIndex(null)
            .setAnalyzers(new PatternAnalyzer(generateName()).setPattern(null));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyPatternAnalyzer() {
        SearchIndex index = createTestIndex(null)
            .setAnalyzers(new PatternAnalyzer(generateName()).setPattern(""));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyFlagsAnalyzer() {
        SearchIndex index = createTestIndex(null)
            .setAnalyzers(new PatternAnalyzer(generateName()).setFlags());

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Values of property \\\"flags\\\" must belong to the set of allowed values"
        );
    }

    @Test
    public void canUseAllRegexFlagsEmptyStopwordsAnalyzer() {
        SearchIndex index = createTestIndex(null)
            .setAnalyzers(new PatternAnalyzer(generateName()).setStopwords());

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new PatternTokenizer(generateName())
                .setPattern(".*")
                .setFlags(REGEX_FLAGS)
                .setGroup(0));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers((List<LexicalTokenizer>) null);

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new ArrayList<>());

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsNullNameTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new PatternTokenizer(null));

        assertThrows(HttpResponseException.class, () ->
            searchIndexClient.createIndex(index), "Missing required property name in model SearchIndexer");

    }

    @Test
    public void canUseAllRegexFlagsEmptyNameTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new PatternTokenizer(""));

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "The name field is required."
        );
    }

    @Test
    public void canUseAllRegexFlagsNullPatternTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new PatternTokenizer(generateName()).setPattern(null));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyPatternTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new PatternTokenizer(generateName()).setPattern(""));

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyFlagsTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new PatternTokenizer(generateName()).setFlags());

        assertHttpResponseException(
            () -> searchIndexClient.createIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Values of property \\\"flags\\\" must belong to the set of allowed values"
        );
    }

    @Test
    public void canUseAllRegexFlagsNullGroupTokenizer() {
        SearchIndex index = createTestIndex(null)
            .setTokenizers(new PatternTokenizer(generateName()).setGroup(null));

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
                assertObjectEquals(expected.get(i), actual.get(i), true, "name");
            }
        }
    }

    private void assertLexicalTokenizersEqual(List<LexicalTokenizer> expected, List<LexicalTokenizer> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertObjectEquals(expected.get(i), actual.get(i), true, "name");
            }
        }
    }

    private void assertCharFiltersEqual(List<CharFilter> expected, List<CharFilter> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertObjectEquals(expected.get(i), actual.get(i), true, "name");
            }
        }
    }

    String generateName() {
        return testResourceNamer.randomName(NAME_PREFIX, 24);
    }

    List<SearchIndex> prepareIndexesWithAllAnalysisComponentOptions() {
        SearchIndex index = createTestIndex(null);

        // Set tokenizers
        List<LexicalTokenizer> tokenizers = new ArrayList<>();
        tokenizers.add(
            new EdgeNGramTokenizer(generateName())
                .setMinGram(1)
                .setMaxGram(2)
                .setTokenChars(TokenCharacterKind.values()));
        tokenizers.addAll(
            Arrays.stream(MicrosoftStemmingTokenizerLanguage.values())
                .map(mtl -> new MicrosoftLanguageStemmingTokenizer(generateName())
                    .setMaxTokenLength(200)
                    .setIsSearchTokenizerUsed(false)
                    .setLanguage(mtl)
                )
                .collect(Collectors.toList())
        );
        index.setTokenizers(tokenizers);

        // Set token filters
        List<TokenFilter> tokenFilters = new ArrayList<>();
        tokenFilters.add(new CjkBigramTokenFilter(generateName())
            .setIgnoreScripts(CjkBigramTokenFilterScripts.values())
            .setOutputUnigrams(true));
        tokenFilters.addAll(
            Arrays.stream(EdgeNGramTokenFilterSide.values())
                .map(s -> new EdgeNGramTokenFilter(generateName())
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setSide(s)
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(PhoneticEncoder.values())
                .map(pe -> new PhoneticTokenFilter(generateName())
                    .setEncoder(pe)
                    .setOriginalTokensReplaced(false)
                )
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(SnowballTokenFilterLanguage.values())
                .map(l -> new SnowballTokenFilter(generateName(), l))
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(StemmerTokenFilterLanguage.values())
                .map(l -> new StemmerTokenFilter(generateName(), l))
                .collect(Collectors.toList())
        );
        tokenFilters.addAll(
            Arrays.stream(StopwordsList.values())
                .map(l -> new StopwordsTokenFilter(generateName())
                    .setStopwordsList(l)
                    .setCaseIgnored(false)
                    .setTrailingStopWordsRemoved(true)
                )
                .collect(Collectors.toList())
        );
        index.setTokenFilters(tokenFilters);

        return splitIndex(index);
    }

    SearchIndex prepareIndexWithAllLexicalAnalyzerNames() {
        List<LexicalAnalyzerName> allLexicalAnalyzerNames = LEXICAL_ANALYZER_NAMES;
        allLexicalAnalyzerNames.sort(Comparator.comparing(LexicalAnalyzerName::toString));

        List<SearchField> fields = new ArrayList<>();
        int fieldNumber = 0;

        // All analyzer names can be set on the analyzer property.
        for (int i = 0; i < allLexicalAnalyzerNames.size(); i++) {
            SearchFieldDataType fieldType = (i % 2 == 0) ? SearchFieldDataType.STRING
                : SearchFieldDataType.collection(SearchFieldDataType.STRING);
            fields.add(new SearchField("field" + (fieldNumber++), fieldType)
                .setAnalyzerName(allLexicalAnalyzerNames.get(i)));
        }

        List<LexicalAnalyzerName> searchAnalyzersAndIndexAnalyzers = getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer();

        for (int i = 0; i < searchAnalyzersAndIndexAnalyzers.size(); i++) {
            SearchFieldDataType fieldType = (i % 2 == 0) ? SearchFieldDataType.STRING
                : SearchFieldDataType.collection(SearchFieldDataType.STRING);
            fields.add(new SearchField("field" + (fieldNumber++), fieldType)
                .setSearchable(true)
                .setSearchAnalyzerName(searchAnalyzersAndIndexAnalyzers.get(i))
                .setIndexAnalyzerName(searchAnalyzersAndIndexAnalyzers.get(i)));
        }

        fields.add(new SearchField("id", SearchFieldDataType.STRING)
            .setKey(true));

        return new SearchIndex(randomIndexName("hotel"))
            .setFields(fields);
    }

    SearchIndex prepareIndexWithAllAnalysisComponentNames() {
        List<TokenFilterName> tokenFilters = TOKEN_FILTER_NAMES;
        tokenFilters.sort(Comparator.comparing(TokenFilterName::toString));

        List<CharFilterName> charFilters = CHAR_FILTER_NAMES;
        charFilters.sort(Comparator.comparing(CharFilterName::toString));

        LexicalAnalyzer analyzerWithAllTokenFilterAndCharFilters =
            new CustomAnalyzer("abc", LexicalTokenizerName.LOWERCASE)
                .setTokenFilters(tokenFilters)
                .setCharFilters(charFilters);

        SearchIndex index = createTestIndex(null);
        List<LexicalAnalyzer> analyzers = new ArrayList<>();
        analyzers.add(analyzerWithAllTokenFilterAndCharFilters);
        String nameBase = generateName();

        List<LexicalTokenizerName> analyzerNames = LEXICAL_TOKENIZER_NAMES;
        analyzerNames.sort(Comparator.comparing(LexicalTokenizerName::toString));

        analyzers.addAll(analyzerNames.stream()
            .map(tn -> new CustomAnalyzer(nameBase + tn, tn))
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
            .map(a -> createTestIndex(null).setAnalyzers(a)).collect(Collectors.toList());

        Collection<List<LexicalTokenizer>> tokenizersLists = splitAnalysisComponents(index.getTokenizers());
        indexes.addAll(tokenizersLists
            .stream()
            .map(t -> createTestIndex(null).setTokenizers(t))
            .collect(Collectors.toList()));

        Collection<List<TokenFilter>> tokenFiltersLists = splitAnalysisComponents(index.getTokenFilters());
        indexes.addAll(tokenFiltersLists
            .stream()
            .map(tf -> createTestIndex(null).setTokenFilters(tf))
            .collect(Collectors.toList()));

        Collection<List<CharFilter>> charFiltersLists = splitAnalysisComponents(index.getCharFilters());
        indexes.addAll(charFiltersLists
            .stream()
            .map(cf -> createTestIndex(null).setCharFilters(cf))
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

        return createTestIndex(null)
            .setAnalyzers(
                new CustomAnalyzer(generateName(), customTokenizerName)
                    .setTokenFilters(customTokenFilterName)
                    .setCharFilters(customCharFilterName),
                new CustomAnalyzer(generateName(), LexicalTokenizerName.EDGE_NGRAM),
                new PatternAnalyzer(generateName())
                    .setLowerCaseTerms(false)
                    .setPattern("abc")
                    .setFlags(RegexFlags.DOT_ALL)
                    .setStopwords("the"),
                new LuceneStandardAnalyzer(generateName())
                    .setMaxTokenLength(100)
                    .setStopwords("the"),
                new StopAnalyzer(generateName())
                    .setStopwords("the"),
                new StopAnalyzer(generateName()))
            .setTokenizers(
                new EdgeNGramTokenizer(customTokenizerName.toString())
                    .setMinGram(1)
                    .setMaxGram(2),
                new EdgeNGramTokenizer(generateName())
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(TokenCharacterKind.LETTER),
                new NGramTokenizer(generateName())
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(TokenCharacterKind.LETTER),
                new ClassicTokenizer(generateName())
                    .setMaxTokenLength(100),
                new KeywordTokenizer(generateName())
                    .setMaxTokenLength(100),
                new MicrosoftLanguageStemmingTokenizer(generateName())
                    .setMaxTokenLength(100)
                    .setIsSearchTokenizerUsed(true)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.CROATIAN),
                new MicrosoftLanguageTokenizer(generateName())
                    .setMaxTokenLength(100)
                    .setIsSearchTokenizer(true)
                    .setLanguage(MicrosoftTokenizerLanguage.THAI),
                new PathHierarchyTokenizer(generateName())
                    .setDelimiter(':')
                    .setReplacement('_')
                    .setMaxTokenLength(300)
                    .setTokenOrderReversed(true)
                    .setNumberOfTokensToSkip(2),
                new PatternTokenizer(generateName())
                    .setPattern(".*")
                    .setFlags(RegexFlags.MULTILINE)
                    .setGroup(0),
                new LuceneStandardTokenizer(generateName())
                    .setMaxTokenLength(100),
                new UaxUrlEmailTokenizer(generateName())
                    .setMaxTokenLength(100))
            .setTokenFilters(
                new CjkBigramTokenFilter(customTokenFilterName.toString()),  // One custom token filter for CustomAnalyzer above.
                new CjkBigramTokenFilter(generateName())
                    .setIgnoreScripts(CjkBigramTokenFilterScripts.HAN)
                    .setOutputUnigrams(true),
                new CjkBigramTokenFilter(generateName()),
                new AsciiFoldingTokenFilter(generateName())
                    .setPreserveOriginal(true),
                new AsciiFoldingTokenFilter(generateName()),
                new CommonGramTokenFilter(generateName(), Arrays.asList("hello", "goodbye"))
                    .setCaseIgnored(true)
                    .setQueryModeUsed(true),
                new CommonGramTokenFilter(generateName(), Collections.singletonList("at")),
                new DictionaryDecompounderTokenFilter(generateName(), Collections.singletonList("Schadenfreude"))
                    .setMinWordSize(10)
                    .setMinSubwordSize(5)
                    .setMaxSubwordSize(13)
                    .setOnlyLongestMatched(true),
                new EdgeNGramTokenFilter(generateName())
                    .setMinGram(2)
                    .setMaxGram(10)
                    .setSide(EdgeNGramTokenFilterSide.BACK),
                new ElisionTokenFilter(generateName())
                    .setArticles("a"),
                new ElisionTokenFilter(generateName()),
                new KeepTokenFilter(generateName(), Collections.singletonList("aloha")),
                new KeepTokenFilter(generateName(), Arrays.asList("e", "komo", "mai")),
                new KeywordMarkerTokenFilter(generateName(), Arrays.asList("key", "words")),
                new KeywordMarkerTokenFilter(generateName(), Collections.singletonList("essential")),
                new LengthTokenFilter(generateName())
                    .setMinLength(5)
                    .setMaxLength(10),
                new LimitTokenFilter(generateName())
                    .setMaxTokenCount(10)
                    .setAllTokensConsumed(true),
                new NGramTokenFilter(generateName())
                    .setMinGram(2)
                    .setMaxGram(3),
                new PatternCaptureTokenFilter(generateName(), Collections.singletonList(".*"))
                    .setPreserveOriginal(false),
                new PatternReplaceTokenFilter(generateName(), "abc", "123"),
                new PhoneticTokenFilter(generateName())
                    .setEncoder(PhoneticEncoder.SOUNDEX)
                    .setOriginalTokensReplaced(false),
                new ShingleTokenFilter(generateName())
                    .setMaxShingleSize(10)
                    .setMinShingleSize(5)
                    .setOutputUnigrams(false)
                    .setOutputUnigramsIfNoShingles(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("|"),
                new SnowballTokenFilter(generateName(), SnowballTokenFilterLanguage.ENGLISH),
                new StemmerOverrideTokenFilter(generateName(), Collections.singletonList("ran => run")),
                new StemmerTokenFilter(generateName(), StemmerTokenFilterLanguage.FRENCH),
                new StopwordsTokenFilter(generateName())
                    .setStopwords(Arrays.asList("a", "the"))
                    .setCaseIgnored(true)
                    .setTrailingStopWordsRemoved(false),
                new StopwordsTokenFilter(generateName())
                    .setStopwordsList(StopwordsList.ITALIAN)
                    .setCaseIgnored(true)
                    .setTrailingStopWordsRemoved(false),
                new SynonymTokenFilter(generateName(), Collections.singletonList("great, good"))
                    .setCaseIgnored(true)
                    .setExpand(false),
                new TruncateTokenFilter(generateName())
                    .setLength(10),
                new UniqueTokenFilter(generateName())
                    .setOnlyOnSamePosition(true),
                new UniqueTokenFilter(generateName()),
                new WordDelimiterTokenFilter(generateName())
                    .setGenerateWordParts(false)
                    .setGenerateNumberParts(false)
                    .setWordsCatenated(true)
                    .setNumbersCatenated(true)
                    .setCatenateAll(true)
                    .setSplitOnCaseChange(false)
                    .setPreserveOriginal(true)
                    .setSplitOnNumerics(false)
                    .setStemEnglishPossessive(false)
                    .setProtectedWords("protected"))
            .setCharFilters(new MappingCharFilter(customCharFilterName.toString(),
                    Collections.singletonList("a => b")), // One custom char filter for CustomeAnalyer above.
                new MappingCharFilter(generateName(), Arrays.asList("s => $", "S => $")),
                new PatternReplaceCharFilter(generateName(), "abc", "123"));
    }

    SearchIndex createIndexWithSpecialDefaults() {
        int i = 0;

        return createTestIndex(null)
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer(generateSimpleName(i++)),
                new LuceneStandardAnalyzer(generateSimpleName(i++))
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer(generateSimpleName(i++)),
                new NGramTokenizer(generateSimpleName(i++)),
                new ClassicTokenizer(generateSimpleName(i++)),
                new KeywordTokenizer(generateSimpleName(i++)),
                new MicrosoftLanguageStemmingTokenizer(generateSimpleName(i++)),
                new MicrosoftLanguageTokenizer(generateSimpleName(i++)),
                new PathHierarchyTokenizer(generateSimpleName(i++)),
                new PatternTokenizer(generateSimpleName(i++)),
                new LuceneStandardTokenizer(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer(generateSimpleName(i++))
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter(generateSimpleName(i++), Collections.singletonList("Bahnhof")),
                new EdgeNGramTokenFilter(generateSimpleName(i++)),
                new LengthTokenFilter(generateSimpleName(i++)),
                new LimitTokenFilter(generateSimpleName(i++)),
                new NGramTokenFilter(generateSimpleName(i++)),
                new PatternCaptureTokenFilter(generateSimpleName(i++), Collections.singletonList("[a-z]*")),
                new PhoneticTokenFilter(generateSimpleName(i++)),
                new ShingleTokenFilter(generateSimpleName(i++)),
                new StopwordsTokenFilter(generateSimpleName(i++)),
                new SynonymTokenFilter(generateSimpleName(i++), Collections.singletonList("mutt, canine => dog")),
                new TruncateTokenFilter(generateSimpleName(i++)),
                new WordDelimiterTokenFilter(generateSimpleName(i))
            ));
    }

    SearchIndex createExpectedIndexWithSpecialDefaults(SearchIndex index) {
        int i = 0;

        return createTestIndex(index.getName())
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer(generateSimpleName(i++))
                    .setLowerCaseTerms(true)
                    .setPattern("\\W+"),
                new LuceneStandardAnalyzer(generateSimpleName(i++))
                    .setMaxTokenLength(255)
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer(generateSimpleName(i++))
                    .setMinGram(1)
                    .setMaxGram(2),
                new NGramTokenizer(generateSimpleName(i++))
                    .setMinGram(1)
                    .setMaxGram(2),
                new ClassicTokenizer(generateSimpleName(i++))
                    .setMaxTokenLength(255),
                new KeywordTokenizer(generateSimpleName(i++))
                    .setMaxTokenLength(256),
                new MicrosoftLanguageStemmingTokenizer(generateSimpleName(i++))
                    .setMaxTokenLength(255)
                    .setIsSearchTokenizerUsed(false)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.ENGLISH),
                new MicrosoftLanguageTokenizer(generateSimpleName(i++))
                    .setMaxTokenLength(255)
                    .setIsSearchTokenizer(false)
                    .setLanguage(MicrosoftTokenizerLanguage.ENGLISH),
                new PathHierarchyTokenizer(generateSimpleName(i++))
                    .setDelimiter('/')
                    .setReplacement('/')
                    .setMaxTokenLength(300),
                new PatternTokenizer(generateSimpleName(i++))
                    .setPattern("\\W+")
                    .setGroup(-1),
                new LuceneStandardTokenizer(generateSimpleName(i++))
                    .setMaxTokenLength(255),
                new UaxUrlEmailTokenizer(generateSimpleName(i++))
                    .setMaxTokenLength(255)
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter(generateSimpleName(i++), Collections.singletonList("Bahnhof"))
                    .setMinWordSize(5)
                    .setMinSubwordSize(2)
                    .setMaxSubwordSize(15),
                new EdgeNGramTokenFilter(generateSimpleName(i++))
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setSide(EdgeNGramTokenFilterSide.FRONT),
                new LengthTokenFilter(generateSimpleName(i++))
                    .setMaxLength(300),
                new LimitTokenFilter(generateSimpleName(i++))
                    .setMaxTokenCount(1),
                new NGramTokenFilter(generateSimpleName(i++))
                    .setMinGram(1)
                    .setMaxGram(2),
                new PatternCaptureTokenFilter(generateSimpleName(i++), Collections.singletonList("[a-z]*"))
                    .setPreserveOriginal(true),
                new PhoneticTokenFilter(generateSimpleName(i++))
                    .setEncoder(PhoneticEncoder.METAPHONE)
                    .setOriginalTokensReplaced(true),
                new ShingleTokenFilter(generateSimpleName(i++))
                    .setMaxShingleSize(2)
                    .setMinShingleSize(2)
                    .setOutputUnigrams(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("_"),
                new StopwordsTokenFilter(generateSimpleName(i++))
                    .setStopwordsList(StopwordsList.ENGLISH)
                    .setTrailingStopWordsRemoved(true),
                new SynonymTokenFilter(generateSimpleName(i++), Collections.singletonList("mutt, canine => dog"))
                    .setExpand(true),
                new TruncateTokenFilter(generateSimpleName(i++))
                    .setLength(300),
                new WordDelimiterTokenFilter(generateSimpleName(i))
                    .setGenerateWordParts(true)
                    .setGenerateNumberParts(true)
                    .setSplitOnCaseChange(true)
                    .setSplitOnNumerics(true)
                    .setStemEnglishPossessive(true)
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
        return LEXICAL_ANALYZER_NAMES.stream()
            .filter(an -> !an.toString().endsWith(".lucene") && !an.toString().endsWith(".microsoft"))
            .collect(Collectors.toList());
    }

    /*
     * This helper method is used when we want to retrieve all declared ExpandableStringEnum values. Using the
     * '.values()' method isn't consistently safe as that would include any custom names that have been added into
     * the enum during runtime.
     */
    private static <T extends ExpandableStringEnum<T>> List<T> getExpandableEnumValues(Class<T> clazz) {
        List<T> fieldValues = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != clazz) {
                continue;
            }

            try {
                fieldValues.add(clazz.cast(field.get(null)));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        return fieldValues;
    }
}
