// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.*;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.createIndexAction;
import static com.azure.search.documents.TestHelpers.ifMatch;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Execution(ExecutionMode.CONCURRENT)
public class CustomAnalyzerTests extends SearchTestBase {
    private static final String NAME_PREFIX = "azsmnet";

    private static final List<TokenFilterName> TOKEN_FILTER_NAMES = getExpandableEnumValues(TokenFilterName.class);
    private static final List<CharFilterName> CHAR_FILTER_NAMES = getExpandableEnumValues(CharFilterName.class);
    private static final List<LexicalAnalyzerName> LEXICAL_ANALYZER_NAMES
        = getExpandableEnumValues(LexicalAnalyzerName.class);
    private static final List<LexicalTokenizerName> LEXICAL_TOKENIZER_NAMES
        = getExpandableEnumValues(LexicalTokenizerName.class);
    private static final List<RegexFlags> REGEX_FLAGS = getExpandableEnumValues(RegexFlags.class);

    private SearchIndexAsyncClient searchIndexAsyncClient;
    private SearchIndexClient searchIndexClient;
    private final List<String> indexesToCleanup = new ArrayList<>();

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchIndexAsyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        for (String index : indexesToCleanup) {
            searchIndexClient.deleteIndex(index);
        }
    }

    @Test
    public void canSearchWithCustomAnalyzerSyncAndAsync() {
        SearchClient searchClient = setupSearchIndexForCustomAnalyzerSearch(searchIndexClient::getSearchClient);
        SearchAsyncClient searchAsyncClient = searchIndexAsyncClient.getSearchAsyncClient(searchClient.getIndexName());

        Iterator<SearchResult> iterator
            = searchClient.search(new SearchOptions().setSearchText("someone@somewhere.something")).iterator();

        assertEquals("1", iterator.next().getAdditionalProperties().get("id"));
        assertFalse(iterator.hasNext());

        StepVerifier.create(searchAsyncClient.search(new SearchOptions().setSearchText("someone@somewhere.something")))
            .assertNext(searchResult -> assertEquals("1", searchResult.getAdditionalProperties().get("id")))
            .verifyComplete();
    }

    private <T> T setupSearchIndexForCustomAnalyzerSearch(Function<String, T> clientCreator) {
        final LexicalAnalyzerName customLexicalAnalyzerName = LexicalAnalyzerName.fromString("my_email_analyzer");
        final CharFilterName customCharFilterName = CharFilterName.fromString("my_email_filter");

        SearchIndex index = new SearchIndex(randomIndexName("testindex"),
            new SearchField("id", SearchFieldDataType.STRING).setKey(true),
            new SearchField("message", SearchFieldDataType.STRING).setAnalyzerName(customLexicalAnalyzerName)
                .setSearchable(true))
                    .setAnalyzers(
                        new CustomAnalyzer(customLexicalAnalyzerName.toString(), LexicalTokenizerName.STANDARD)
                            .setCharFilters(customCharFilterName))
                    .setCharFilters(new PatternReplaceCharFilter(customCharFilterName.toString(), "@", "_"));

        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());
        SearchClient searchClient = searchIndexClient.getSearchClient(index.getName());

        Map<String, Object> document1 = new HashMap<>();
        document1.put("id", "1");
        document1.put("message", "My email is someone@somewhere.something.");
        Map<String, Object> document2 = new HashMap<>();
        document2.put("id", "2");
        document2.put("message", "His email is someone@nowhere.nothing.");

        IndexDocumentsBatch batch = new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, document1),
            createIndexAction(IndexActionType.UPLOAD, document2));

        searchClient.indexDocuments(batch);
        waitForIndexing();

        return clientCreator.apply(index.getName());
    }

    @Test
    public void canUseAllAnalyzerNamesInIndexDefinitionSync() {
        SearchIndex index = prepareIndexWithAllLexicalAnalyzerNames();
        indexesToCleanup.add(index.getName());
        SearchIndex res = searchIndexClient.createIndex(index);

        assertObjectEquals(index, res, true, "etag");
    }

    @Test
    public void canUseAllAnalyzerNamesInIndexDefinitionAsync() {
        SearchIndex index = prepareIndexWithAllLexicalAnalyzerNames();
        indexesToCleanup.add(index.getName());

        StepVerifier.create(searchIndexAsyncClient.createIndex(index))
            .assertNext(result -> assertObjectEquals(index, result, true, "etag"))
            .verifyComplete();
    }

    @Test
    public void canAnalyzeSyncAndAsync() {
        SearchIndex index = createTestIndex(null);
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        AnalyzeTextOptions request = new AnalyzeTextOptions("One two").setAnalyzerName(LexicalAnalyzerName.WHITESPACE);

        // sync
        AnalyzeResult results = searchIndexClient.analyzeText(index.getName(), request);
        assertEquals(2, results.getTokens().size());
        assertTokenInfoEqual("One", 0, 3, 0, results.getTokens().get(0));
        assertTokenInfoEqual("two", 4, 7, 1, results.getTokens().get(1));

        // async
        StepVerifier.create(searchIndexAsyncClient.analyzeText(index.getName(), request)).assertNext(analyzeResults -> {
            assertEquals(2, analyzeResults.getTokens().size());
            assertTokenInfoEqual("One", 0, 3, 0, analyzeResults.getTokens().get(0));
            assertTokenInfoEqual("two", 4, 7, 1, analyzeResults.getTokens().get(1));
        }).verifyComplete();

        request = new AnalyzeTextOptions("One's <two/>").setTokenizerName(LexicalTokenizerName.WHITESPACE)
            .setTokenFilters(TokenFilterName.APOSTROPHE)
            .setCharFilters(CharFilterName.HTML_STRIP);

        // sync
        results = searchIndexClient.analyzeText(index.getName(), request);
        // End offset is based on the original token, not the one emitted by the filters.
        assertEquals(1, results.getTokens().size());
        assertTokenInfoEqual("One", 0, 5, 0, results.getTokens().get(0));

        // async
        StepVerifier.create(searchIndexAsyncClient.analyzeText(index.getName(), request)).assertNext(analyzeResults -> {
            assertEquals(1, analyzeResults.getTokens().size());
            assertTokenInfoEqual("One", 0, 5, 0, analyzeResults.getTokens().get(0));
        }).verifyComplete();
    }

    @Test
    public void canAnalyzeWithAllPossibleNamesSyncAndAsync() {
        SearchIndex index = createTestIndex(null);
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        // Since the tests in this class are ran in parallel the lists could be modified while iterating over them.
        List<LexicalAnalyzerName> threadSafeLexicalAnalyzerNames = new ArrayList<>(LEXICAL_ANALYZER_NAMES);
        List<LexicalTokenizerName> threadSafeLexicalTokenizerNames = new ArrayList<>(LEXICAL_TOKENIZER_NAMES);

        threadSafeLexicalAnalyzerNames.parallelStream()
            .map(an -> new AnalyzeTextOptions("One two").setAnalyzerName(an))
            .forEach(r -> searchIndexClient.analyzeText(index.getName(), r));

        Flux.fromIterable(threadSafeLexicalAnalyzerNames)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(an -> searchIndexAsyncClient.analyzeText(index.getName(),
                new AnalyzeTextOptions("One two").setAnalyzerName(an)))
            .then()
            .block();

        threadSafeLexicalTokenizerNames.parallelStream()
            .map(tn -> new AnalyzeTextOptions("One two").setTokenizerName(tn))
            .forEach(r -> searchIndexClient.analyzeText(index.getName(), r));

        Flux.fromIterable(threadSafeLexicalTokenizerNames)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(tn -> searchIndexAsyncClient.analyzeText(index.getName(),
                new AnalyzeTextOptions("One two").setTokenizerName(tn)))
            .then()
            .block();
    }

    @Test
    public void addingCustomAnalyzerThrowsHttpExceptionByDefaultSyncAndAsync() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new StopAnalyzer("a1"));
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        addAnalyzerToIndex(index, new StopAnalyzer("a2"));

        assertHttpResponseException(() -> searchIndexClient.createOrUpdateIndex(index),
            HttpURLConnection.HTTP_BAD_REQUEST, "Index update not allowed because it would cause downtime.");

        StepVerifier.create(searchIndexAsyncClient.createOrUpdateIndex(index))
            .verifyErrorSatisfies(exception -> verifyHttpResponseError(exception, HttpURLConnection.HTTP_BAD_REQUEST,
                "Index update not allowed because it would cause downtime."));
    }

    @Test
    public void canAddCustomAnalyzerWithIndexDowntimeSync() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new StopAnalyzer("a1"));
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        addAnalyzerToIndex(index, new StopAnalyzer("a2"));
        SearchIndex updatedIndex = searchIndexClient
            .createOrUpdateIndexWithResponse(index.getName(), BinaryData.fromObject(index),
                ifMatch(index.getETag()).addQueryParam("allowIndexDowntime", "true"))
            .getValue()
            .toObject(SearchIndex.class);

        assertAnalysisComponentsEqual(index, updatedIndex);
    }

    @Test
    public void canAddCustomAnalyzerWithIndexDowntimeAsync() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new StopAnalyzer("a1"));
        searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        addAnalyzerToIndex(index, new StopAnalyzer("a2"));

        StepVerifier
            .create(searchIndexAsyncClient.createOrUpdateIndexWithResponse(index.getName(),
                BinaryData.fromObject(index), ifMatch(index.getETag()).addQueryParam("allowIndexDowntime", "true")))
            .assertNext(
                response -> assertAnalysisComponentsEqual(index, response.getValue().toObject(SearchIndex.class)))
            .verifyComplete();
    }

    @Test
    public void canCreateAllAnalysisComponentsSync() {
        SearchIndex index = prepareIndexWithAllAnalysisComponentTypes();
        createAndValidateIndexSync(searchIndexClient, index);
        searchIndexClient.deleteIndex(index.getName());

        // We have to split up analysis components into two indexes, one where any components with optional properties
        // have defaults that are zero or null, and another where we need to specify the default values we
        // expect to get back from the REST API.
        SearchIndex indexWithSpecialDefaults = createIndexWithSpecialDefaults();
        SearchIndex expectedIndexWithSpecialDefaults = createExpectedIndexWithSpecialDefaults(indexWithSpecialDefaults);

        List<SearchIndex> splitIndexWithSpecialDefaults = splitIndex(indexWithSpecialDefaults);
        List<SearchIndex> splitExpectedIndexWithSpecialDefaults = splitIndex(expectedIndexWithSpecialDefaults);
        for (int j = 0; j < splitIndexWithSpecialDefaults.size(); j++) {
            index = splitExpectedIndexWithSpecialDefaults.get(j);
            createAndValidateIndexSync(searchIndexClient, index);
            searchIndexClient.deleteIndex(index.getName());
        }
    }

    @Test
    public void canCreateAllAnalysisComponentsAsync() {
        SearchIndex index = prepareIndexWithAllAnalysisComponentTypes();
        createAndValidateIndexAsync(searchIndexAsyncClient, index);
        searchIndexClient.deleteIndex(index.getName());

        // We have to split up analysis components into two indexes, one where any components with optional properties
        // have defaults that are zero or null, and another where we need to specify the default values we
        // expect to get back from the REST API.
        SearchIndex indexWithSpecialDefaults = createIndexWithSpecialDefaults();
        SearchIndex expectedIndexWithSpecialDefaults = createExpectedIndexWithSpecialDefaults(indexWithSpecialDefaults);

        List<SearchIndex> splitIndexWithSpecialDefaults = splitIndex(indexWithSpecialDefaults);
        List<SearchIndex> splitExpectedIndexWithSpecialDefaults = splitIndex(expectedIndexWithSpecialDefaults);
        for (int j = 0; j < splitIndexWithSpecialDefaults.size(); j++) {
            index = splitExpectedIndexWithSpecialDefaults.get(j);
            createAndValidateIndexAsync(searchIndexAsyncClient, index);
            searchIndexClient.deleteIndex(index.getName());
        }
    }

    @Test
    public void canUseAllAnalysisComponentNamesSync() {
        SearchIndex index = prepareIndexWithAllAnalysisComponentNames();

        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());
        assertCustomAnalysisComponentsEqual(index, createdIndex);
    }

    @Test
    public void canUseAllAnalysisComponentNamesAsync() {
        SearchIndex index = prepareIndexWithAllAnalysisComponentNames();

        StepVerifier.create(searchIndexAsyncClient.createIndex(index)).assertNext(createdIndex -> {
            indexesToCleanup.add(index.getName());
            assertCustomAnalysisComponentsEqual(index, createdIndex);
        }).verifyComplete();
    }

    @Test
    public void canUsePatternAnalyzerInManyWaysSync() {
        SearchIndex expectedIndex
            = createTestIndex(null).setAnalyzers(new PatternAnalyzer(generateName()).setStopwords("stop1", "stop2")
                .setLowerCaseTerms(true)
                .setPattern(".*")
                .setFlags(REGEX_FLAGS), new PatternAnalyzer(generateName()).setPattern(""));

        createAndValidateIndexSync(searchIndexClient, expectedIndex);
    }

    @Test
    public void canUsePatternAnalyzerInManyWaysAsync() {
        SearchIndex expectedIndex
            = createTestIndex(null).setAnalyzers(new PatternAnalyzer(generateName()).setStopwords("stop1", "stop2")
                .setLowerCaseTerms(true)
                .setPattern(".*")
                .setFlags(REGEX_FLAGS), new PatternAnalyzer(generateName()).setPattern(""));

        createAndValidateIndexAsync(searchIndexAsyncClient, expectedIndex);
    }

    @Test
    public void canUseAllRegexFlagsEmptyFlagsAnalyzerSyncAndAsync() {
        SearchIndex index = createTestIndex(null).setAnalyzers(new PatternAnalyzer(generateName()).setFlags());

        assertHttpResponseException(() -> searchIndexClient.createIndex(index), HttpURLConnection.HTTP_BAD_REQUEST,
            "Values of property \\\"flags\\\" must belong to the set of allowed values");

        StepVerifier.create(searchIndexAsyncClient.createIndex(index))
            .verifyErrorSatisfies(exception -> verifyHttpResponseError(exception, HttpURLConnection.HTTP_BAD_REQUEST,
                "Values of property \\\"flags\\\" must belong to the set of allowed values"));
    }

    @Test
    public void canUseAllRegexFlagsTokenizerSync() {
        createAndValidateIndexSync(searchIndexClient, createTestIndex(null).setTokenizers(
            new PatternTokenizer(generateName()).setPattern(".*").setFlags(REGEX_FLAGS).setGroup(0),
            new PatternTokenizer(generateName()).setPattern(""), new PatternTokenizer(generateName()).setGroup(null)));
    }

    @Test
    public void canUseAllRegexFlagsTokenizerAsync() {
        createAndValidateIndexAsync(searchIndexAsyncClient, createTestIndex(null).setTokenizers(
            new PatternTokenizer(generateName()).setPattern(".*").setFlags(REGEX_FLAGS).setGroup(0),
            new PatternTokenizer(generateName()).setPattern(""), new PatternTokenizer(generateName()).setGroup(null)));
    }

    @Test
    public void canUseAllRegexFlagsEmptyTokenizerSync() {
        createAndValidateIndexSync(searchIndexClient, createTestIndex(null).setTokenizers(new ArrayList<>()));
    }

    @Test
    public void canUseAllRegexFlagsEmptyTokenizerAsync() {
        createAndValidateIndexAsync(searchIndexAsyncClient, createTestIndex(null).setTokenizers(new ArrayList<>()));
    }

    @Test
    public void canUseAllAnalysisComponentOptionsSync() {
        List<SearchIndex> indexes = prepareIndexesWithAllAnalysisComponentOptions();

        indexes.forEach(expectedIndex -> {
            createAndValidateIndexSync(searchIndexClient, expectedIndex);
            searchIndexClient.deleteIndex(expectedIndex.getName());
        });
    }

    @Test
    public void canUseAllAnalysisComponentOptionsAsync() {
        List<SearchIndex> indexes = prepareIndexesWithAllAnalysisComponentOptions();

        indexes.forEach(expectedIndex -> {
            createAndValidateIndexAsync(searchIndexAsyncClient, expectedIndex);
            searchIndexClient.deleteIndex(expectedIndex.getName());
        });
    }

    private void createAndValidateIndexSync(SearchIndexClient searchIndexClient, SearchIndex index) {
        SearchIndex createdIndex = searchIndexClient.createIndex(index);
        indexesToCleanup.add(index.getName());

        assertAnalysisComponentsEqual(index, createdIndex);
    }

    private void createAndValidateIndexAsync(SearchIndexAsyncClient searchIndexAsyncClient, SearchIndex index) {
        StepVerifier.create(searchIndexAsyncClient.createIndex(index)).assertNext(createdIndex -> {
            indexesToCleanup.add(index.getName());
            assertAnalysisComponentsEqual(index, createdIndex);
        }).verifyComplete();
    }

    static void addAnalyzerToIndex(SearchIndex index, LexicalAnalyzer analyzer) {
        List<LexicalAnalyzer> analyzers = new ArrayList<>(index.getAnalyzers());
        analyzers.add(analyzer);

        index.setAnalyzers(analyzers);
    }

    static void assertAnalysisComponentsEqual(SearchIndex expected, SearchIndex actual) {
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
            Comparator<LexicalAnalyzer> customAnalyzerComparator
                = Comparator.comparing((LexicalAnalyzer a) -> ((CustomAnalyzer) a).getTokenizer().toString());

            expectedAnalyzers.sort(customAnalyzerComparator);
            actualAnalyzers.sort(customAnalyzerComparator);

            assertAnalyzersEqual(expectedAnalyzers, actualAnalyzers);
        }

        // LexicalTokenizer
        assertLexicalTokenizersEqual(expected.getTokenizers(), actual.getTokenizers());

        // Char filter
        assertCharFiltersEqual(expected.getCharFilters(), actual.getCharFilters());
    }

    private static void assertAnalyzersEqual(List<LexicalAnalyzer> expected, List<LexicalAnalyzer> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertObjectEquals(expected.get(i), actual.get(i), true, "name");
            }
        }
    }

    private static void assertLexicalTokenizersEqual(List<LexicalTokenizer> expected, List<LexicalTokenizer> actual) {
        if (expected != null && actual != null) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertObjectEquals(expected.get(i), actual.get(i), true, "name");
            }
        }
    }

    private static void assertCharFiltersEqual(List<CharFilter> expected, List<CharFilter> actual) {
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
        tokenizers.add(new EdgeNGramTokenizer(generateName()).setMinGram(1)
            .setMaxGram(2)
            .setTokenChars(TokenCharacterKind.values()));
        for (MicrosoftStemmingTokenizerLanguage tokenizer : MicrosoftStemmingTokenizerLanguage.values()) {
            tokenizers.add(new MicrosoftLanguageStemmingTokenizer(generateName()).setMaxTokenLength(200)
                .setIsSearchTokenizer(false)
                .setLanguage(tokenizer));
        }
        index.setTokenizers(tokenizers);

        // Set token filters
        List<TokenFilter> tokenFilters = new ArrayList<>();
        tokenFilters.add(new CjkBigramTokenFilter(generateName()).setIgnoreScripts(CjkBigramTokenFilterScripts.values())
            .setOutputUnigrams(true));

        for (EdgeNGramTokenFilterSide filter : EdgeNGramTokenFilterSide.values()) {
            tokenFilters.add(new EdgeNGramTokenFilterV2(generateName()).setMinGram(1).setMaxGram(2).setSide(filter));
        }

        for (PhoneticEncoder filter : PhoneticEncoder.values()) {
            tokenFilters
                .add(new PhoneticTokenFilter(generateName()).setEncoder(filter).setReplaceOriginalTokens(false));
        }

        for (SnowballTokenFilterLanguage filter : SnowballTokenFilterLanguage.values()) {
            tokenFilters.add(new SnowballTokenFilter(generateName(), filter));
        }

        for (StemmerTokenFilterLanguage filter : StemmerTokenFilterLanguage.values()) {
            tokenFilters.add(new StemmerTokenFilter(generateName(), filter));
        }

        for (StopwordsList stopwordsList : StopwordsList.values()) {
            tokenFilters.add(new StopwordsTokenFilter(generateName()).setStopwordsList(stopwordsList)
                .setIgnoreCase(false)
                .setRemoveTrailingStopWords(true));
        }

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
            SearchFieldDataType fieldType = (i % 2 == 0)
                ? SearchFieldDataType.STRING
                : SearchFieldDataType.collection(SearchFieldDataType.STRING);
            fields.add(
                new SearchField("field" + (fieldNumber++), fieldType).setAnalyzerName(allLexicalAnalyzerNames.get(i)));
        }

        List<LexicalAnalyzerName> searchAnalyzersAndIndexAnalyzers
            = getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer();

        for (int i = 0; i < searchAnalyzersAndIndexAnalyzers.size(); i++) {
            SearchFieldDataType fieldType = (i % 2 == 0)
                ? SearchFieldDataType.STRING
                : SearchFieldDataType.collection(SearchFieldDataType.STRING);
            fields.add(new SearchField("field" + (fieldNumber++), fieldType).setSearchable(true)
                .setSearchAnalyzerName(searchAnalyzersAndIndexAnalyzers.get(i))
                .setIndexAnalyzerName(searchAnalyzersAndIndexAnalyzers.get(i)));
        }

        fields.add(new SearchField("id", SearchFieldDataType.STRING).setKey(true));

        return new SearchIndex(randomIndexName("hotel"), fields);
    }

    SearchIndex prepareIndexWithAllAnalysisComponentNames() {
        List<TokenFilterName> tokenFilters = new ArrayList<>(TOKEN_FILTER_NAMES);
        tokenFilters.sort(Comparator.comparing(TokenFilterName::toString));

        List<CharFilterName> charFilters = new ArrayList<>(CHAR_FILTER_NAMES);
        charFilters.sort(Comparator.comparing(CharFilterName::toString));

        LexicalAnalyzer analyzerWithAllTokenFilterAndCharFilters
            = new CustomAnalyzer("abc", LexicalTokenizerName.LOWERCASE).setTokenFilters(tokenFilters)
                .setCharFilters(charFilters);

        SearchIndex index = createTestIndex(null);
        List<LexicalAnalyzer> analyzers = new ArrayList<>();
        analyzers.add(analyzerWithAllTokenFilterAndCharFilters);
        String nameBase = generateName();

        List<LexicalTokenizerName> analyzerNames = new ArrayList<>(LEXICAL_TOKENIZER_NAMES);
        analyzerNames.sort(Comparator.comparing(LexicalTokenizerName::toString));

        analyzerNames.stream().map(tn -> new CustomAnalyzer(nameBase + tn, tn)).forEach(analyzers::add);

        analyzers.sort(Comparator.comparing(LexicalAnalyzer::getName));
        index.setAnalyzers(analyzers);

        return index;
    }

    /**
     * Custom analysis components (analyzer/tokenzier/tokenFilter/charFilter) count in index must be between 0 and 50.
     * Split an Index into indexes, each of which has a total analysis components count within the limit.
     */
    List<SearchIndex> splitIndex(SearchIndex index) {
        List<SearchIndex> indices = new ArrayList<>();

        for (List<LexicalAnalyzer> analyzerList : splitAnalysisComponents(index.getAnalyzers())) {
            indices.add(createTestIndex(null).setAnalyzers(analyzerList));
        }

        for (List<LexicalTokenizer> tokenizerList : splitAnalysisComponents(index.getTokenizers())) {
            indices.add(createTestIndex(null).setTokenizers(tokenizerList));
        }

        for (List<TokenFilter> tokenFilterList : splitAnalysisComponents(index.getTokenFilters())) {
            indices.add(createTestIndex(null).setTokenFilters(tokenFilterList));
        }

        for (List<CharFilter> charFilterList : splitAnalysisComponents(index.getCharFilters())) {
            indices.add(createTestIndex(null).setCharFilters(charFilterList));
        }

        return indices;
    }

    /**
     * Custom analysis components (analyzer/tokenzier/tokenFilter/charFilter) count in index must be between 0 and 50.
     * Split a list of analysis components into lists within the limit.
     */
    private <T> List<List<T>> splitAnalysisComponents(List<T> list) {
        final int analysisComponentLimit = 50;
        List<List<T>> lists = new ArrayList<>();

        if (CoreUtils.isNullOrEmpty(list)) {
            return lists;
        }

        if (list.size() > analysisComponentLimit) {
            int subLists = list.size() / analysisComponentLimit;
            int remainder = list.size() % analysisComponentLimit;

            for (int i = 0; i < subLists; i++) {
                lists.add(new ArrayList<>(list.subList(i * analysisComponentLimit, (i + 1) * analysisComponentLimit)));
            }

            if (remainder > 0) {
                lists.add(new ArrayList<>(list.subList(subLists * analysisComponentLimit, list.size())));
            }
        } else {
            lists.add(list);
        }

        return lists;
    }

    SearchIndex prepareIndexWithAllAnalysisComponentTypes() {
        final LexicalTokenizerName customTokenizerName = LexicalTokenizerName.fromString("my_tokenizer");
        final TokenFilterName customTokenFilterName = TokenFilterName.fromString("my_tokenfilter");
        final CharFilterName customCharFilterName = CharFilterName.fromString("my_charfilter");

        return createTestIndex(null)
            .setAnalyzers(
                new CustomAnalyzer(generateName(), customTokenizerName).setTokenFilters(customTokenFilterName)
                    .setCharFilters(customCharFilterName),
                new CustomAnalyzer(generateName(), LexicalTokenizerName.EDGE_NGRAM),
                new PatternAnalyzer(generateName()).setLowerCaseTerms(false)
                    .setPattern("abc")
                    .setFlags(RegexFlags.DOT_ALL)
                    .setStopwords("the"),
                new LuceneStandardAnalyzer(generateName()).setMaxTokenLength(100).setStopwords("the"),
                new StopAnalyzer(generateName()).setStopwords("the"), new StopAnalyzer(generateName()))
            .setTokenizers(new EdgeNGramTokenizer(customTokenizerName.toString()).setMinGram(1).setMaxGram(2),
                new EdgeNGramTokenizer(generateName()).setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(TokenCharacterKind.LETTER),
                new NGramTokenizer(generateName()).setMinGram(2).setMaxGram(4).setTokenChars(TokenCharacterKind.LETTER),
                new ClassicTokenizer(generateName()).setMaxTokenLength(100),
                new KeywordTokenizerV2(generateName()).setMaxTokenLength(100),
                new MicrosoftLanguageStemmingTokenizer(generateName()).setMaxTokenLength(100)
                    .setIsSearchTokenizer(true)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.CROATIAN),
                new MicrosoftLanguageTokenizer(generateName()).setMaxTokenLength(100)
                    .setIsSearchTokenizer(true)
                    .setLanguage(MicrosoftTokenizerLanguage.THAI),
                new PathHierarchyTokenizerV2(generateName()).setDelimiter(":")
                    .setReplacement("_")
                    .setMaxTokenLength(300)
                    .setReverseTokenOrder(true)
                    .setNumberOfTokensToSkip(2),
                new PatternTokenizer(generateName()).setPattern(".*").setFlags(RegexFlags.MULTILINE).setGroup(0),
                new LuceneStandardTokenizerV2(generateName()).setMaxTokenLength(100),
                new UaxUrlEmailTokenizer(generateName()).setMaxTokenLength(100))
            .setTokenFilters(new CjkBigramTokenFilter(customTokenFilterName.toString()),  // One custom token filter for CustomAnalyzer above.
                new CjkBigramTokenFilter(generateName()).setIgnoreScripts(CjkBigramTokenFilterScripts.HAN)
                    .setOutputUnigrams(true),
                new CjkBigramTokenFilter(generateName()),
                new AsciiFoldingTokenFilter(generateName()).setPreserveOriginal(true),
                new AsciiFoldingTokenFilter(generateName()),
                new CommonGramTokenFilter(generateName(), "hello", "goodbye").setIgnoreCase(true).setUseQueryMode(true),
                new CommonGramTokenFilter(generateName(), "at"),
                new DictionaryDecompounderTokenFilter(generateName(), "Schadenfreude").setMinWordSize(10)
                    .setMinSubwordSize(5)
                    .setMaxSubwordSize(13)
                    .setOnlyLongestMatch(true),
                new EdgeNGramTokenFilterV2(generateName()).setMinGram(2)
                    .setMaxGram(10)
                    .setSide(EdgeNGramTokenFilterSide.BACK),
                new ElisionTokenFilter(generateName()).setArticles("a"), new ElisionTokenFilter(generateName()),
                new KeepTokenFilter(generateName(), "aloha"), new KeepTokenFilter(generateName(), "e", "komo", "mai"),
                new KeywordMarkerTokenFilter(generateName(), "key", "words"),
                new KeywordMarkerTokenFilter(generateName(), "essential"),
                new LengthTokenFilter(generateName()).setMinLength(5).setMaxLength(10),
                new LimitTokenFilter(generateName()).setMaxTokenCount(10).setConsumeAllTokens(true),
                new NGramTokenFilterV2(generateName()).setMinGram(2).setMaxGram(3),
                new PatternCaptureTokenFilter(generateName(), ".*").setPreserveOriginal(false),
                new PatternReplaceTokenFilter(generateName(), "abc", "123"),
                new PhoneticTokenFilter(generateName()).setEncoder(PhoneticEncoder.SOUNDEX)
                    .setReplaceOriginalTokens(false),
                new ShingleTokenFilter(generateName()).setMaxShingleSize(10)
                    .setMinShingleSize(5)
                    .setOutputUnigrams(false)
                    .setOutputUnigramsIfNoShingles(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("|"),
                new SnowballTokenFilter(generateName(), SnowballTokenFilterLanguage.ENGLISH),
                new StemmerOverrideTokenFilter(generateName(), "ran => run"),
                new StemmerTokenFilter(generateName(), StemmerTokenFilterLanguage.FRENCH),
                new StopwordsTokenFilter(generateName()).setStopwords("a", "the")
                    .setIgnoreCase(true)
                    .setRemoveTrailingStopWords(false),
                new StopwordsTokenFilter(generateName()).setStopwordsList(StopwordsList.ITALIAN)
                    .setIgnoreCase(true)
                    .setRemoveTrailingStopWords(false),
                new SynonymTokenFilter(generateName(), "great, good").setIgnoreCase(true).setExpand(false),
                new TruncateTokenFilter(generateName()).setLength(10),
                new UniqueTokenFilter(generateName()).setOnlyOnSamePosition(true),
                new UniqueTokenFilter(generateName()),
                new WordDelimiterTokenFilter(generateName()).setGenerateWordParts(false)
                    .setGenerateNumberParts(false)
                    .setCatenateWords(true)
                    .setCatenateNumbers(true)
                    .setCatenateAll(true)
                    .setSplitOnCaseChange(false)
                    .setPreserveOriginal(true)
                    .setSplitOnNumerics(false)
                    .setStemEnglishPossessive(false)
                    .setProtectedWords("protected"))
            .setCharFilters(new MappingCharFilter(customCharFilterName.toString(), "a => b"), // One custom char filter for CustomAnalyzer above.
                new MappingCharFilter(generateName(), "s => $", "S => $"),
                new PatternReplaceCharFilter(generateName(), "abc", "123"));
    }

    SearchIndex createIndexWithSpecialDefaults() {
        int i = 0;

        return createTestIndex(null)
            .setAnalyzers(new PatternAnalyzer(generateSimpleName(i++)),
                new LuceneStandardAnalyzer(generateSimpleName(i++)))
            .setTokenizers(new EdgeNGramTokenizer(generateSimpleName(i++)), new NGramTokenizer(generateSimpleName(i++)),
                new ClassicTokenizer(generateSimpleName(i++)), new KeywordTokenizerV2(generateSimpleName(i++)),
                new MicrosoftLanguageStemmingTokenizer(generateSimpleName(i++)),
                new MicrosoftLanguageTokenizer(generateSimpleName(i++)),
                new PathHierarchyTokenizerV2(generateSimpleName(i++)), new PatternTokenizer(generateSimpleName(i++)),
                new LuceneStandardTokenizerV2(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer(generateSimpleName(i++)))
            .setTokenFilters(new DictionaryDecompounderTokenFilter(generateSimpleName(i++), "Bahnhof"),
                new EdgeNGramTokenFilterV2(generateSimpleName(i++)), new LengthTokenFilter(generateSimpleName(i++)),
                new LimitTokenFilter(generateSimpleName(i++)), new NGramTokenFilterV2(generateSimpleName(i++)),
                new PatternCaptureTokenFilter(generateSimpleName(i++), "[a-z]*"),
                new PhoneticTokenFilter(generateSimpleName(i++)), new ShingleTokenFilter(generateSimpleName(i++)),
                new StopwordsTokenFilter(generateSimpleName(i++)),
                new SynonymTokenFilter(generateSimpleName(i++), "mutt, canine => dog"),
                new TruncateTokenFilter(generateSimpleName(i++)), new WordDelimiterTokenFilter(generateSimpleName(i)));
    }

    SearchIndex createExpectedIndexWithSpecialDefaults(SearchIndex index) {
        int i = 0;

        return createTestIndex(index.getName())
            .setAnalyzers(new PatternAnalyzer(generateSimpleName(i++)).setLowerCaseTerms(true).setPattern("\\W+"),
                new LuceneStandardAnalyzer(generateSimpleName(i++)).setMaxTokenLength(255))
            .setTokenizers(new EdgeNGramTokenizer(generateSimpleName(i++)).setMinGram(1).setMaxGram(2),
                new NGramTokenizer(generateSimpleName(i++)).setMinGram(1).setMaxGram(2),
                new ClassicTokenizer(generateSimpleName(i++)).setMaxTokenLength(255),
                new KeywordTokenizerV2(generateSimpleName(i++)).setMaxTokenLength(256),
                new MicrosoftLanguageStemmingTokenizer(generateSimpleName(i++)).setMaxTokenLength(255)
                    .setIsSearchTokenizer(false)
                    .setLanguage(MicrosoftStemmingTokenizerLanguage.ENGLISH),
                new MicrosoftLanguageTokenizer(generateSimpleName(i++)).setMaxTokenLength(255)
                    .setIsSearchTokenizer(false)
                    .setLanguage(MicrosoftTokenizerLanguage.ENGLISH),
                new PathHierarchyTokenizerV2(generateSimpleName(i++)).setDelimiter("/")
                    .setReplacement("/")
                    .setMaxTokenLength(300),
                new PatternTokenizer(generateSimpleName(i++)).setPattern("\\W+").setGroup(-1),
                new LuceneStandardTokenizerV2(generateSimpleName(i++)).setMaxTokenLength(255),
                new UaxUrlEmailTokenizer(generateSimpleName(i++)).setMaxTokenLength(255))
            .setTokenFilters(
                new DictionaryDecompounderTokenFilter(generateSimpleName(i++), "Bahnhof").setMinWordSize(5)
                    .setMinSubwordSize(2)
                    .setMaxSubwordSize(15),
                new EdgeNGramTokenFilterV2(generateSimpleName(i++)).setMinGram(1)
                    .setMaxGram(2)
                    .setSide(EdgeNGramTokenFilterSide.FRONT),
                new LengthTokenFilter(generateSimpleName(i++)).setMaxLength(300),
                new LimitTokenFilter(generateSimpleName(i++)).setMaxTokenCount(1),
                new NGramTokenFilterV2(generateSimpleName(i++)).setMinGram(1).setMaxGram(2),
                new PatternCaptureTokenFilter(generateSimpleName(i++), "[a-z]*").setPreserveOriginal(true),
                new PhoneticTokenFilter(generateSimpleName(i++)).setEncoder(PhoneticEncoder.METAPHONE)
                    .setReplaceOriginalTokens(true),
                new ShingleTokenFilter(generateSimpleName(i++)).setMaxShingleSize(2)
                    .setMinShingleSize(2)
                    .setOutputUnigrams(true)
                    .setTokenSeparator(" ")
                    .setFilterToken("_"),
                new StopwordsTokenFilter(generateSimpleName(i++)).setStopwordsList(StopwordsList.ENGLISH)
                    .setRemoveTrailingStopWords(true),
                new SynonymTokenFilter(generateSimpleName(i++), "mutt, canine => dog").setExpand(true),
                new TruncateTokenFilter(generateSimpleName(i++)).setLength(300),
                new WordDelimiterTokenFilter(generateSimpleName(i)).setGenerateWordParts(true)
                    .setGenerateNumberParts(true)
                    .setSplitOnCaseChange(true)
                    .setSplitOnNumerics(true)
                    .setStemEnglishPossessive(true));
    }

    static void assertTokenInfoEqual(String expectedToken, Integer expectedStartOffset, Integer expectedEndOffset,
        Integer expectedPosition, AnalyzedTokenInfo actual) {
        assertEquals(expectedToken, actual.getToken());
        assertEquals(expectedStartOffset, actual.getStartOffset());
        assertEquals(expectedEndOffset, actual.getEndOffset());
        assertEquals(expectedPosition, actual.getPosition());
    }

    private static String generateSimpleName(int n) {
        return "a" + n;
    }

    private static List<LexicalAnalyzerName> getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer() {
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
