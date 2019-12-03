// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeRequest;
import com.azure.search.models.Analyzer;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.AsciiFoldingTokenFilter;
import com.azure.search.models.CharFilterName;
import com.azure.search.models.CjkBigramTokenFilter;
import com.azure.search.models.CjkBigramTokenFilterScripts;
import com.azure.search.models.ClassicTokenizer;
import com.azure.search.models.CommonGramTokenFilter;
import com.azure.search.models.CustomAnalyzer;
import com.azure.search.models.DataType;
import com.azure.search.models.DictionaryDecompounderTokenFilter;
import com.azure.search.models.EdgeNGramTokenFilterSide;
import com.azure.search.models.EdgeNGramTokenFilterV2;
import com.azure.search.models.EdgeNGramTokenizer;
import com.azure.search.models.ElisionTokenFilter;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.KeepTokenFilter;
import com.azure.search.models.KeywordMarkerTokenFilter;
import com.azure.search.models.KeywordTokenizerV2;
import com.azure.search.models.LengthTokenFilter;
import com.azure.search.models.LimitTokenFilter;
import com.azure.search.models.MappingCharFilter;
import com.azure.search.models.MicrosoftLanguageStemmingTokenizer;
import com.azure.search.models.MicrosoftLanguageTokenizer;
import com.azure.search.models.MicrosoftStemmingTokenizerLanguage;
import com.azure.search.models.MicrosoftTokenizerLanguage;
import com.azure.search.models.NGramTokenFilterV2;
import com.azure.search.models.NGramTokenizer;
import com.azure.search.models.PathHierarchyTokenizerV2;
import com.azure.search.models.PatternAnalyzer;
import com.azure.search.models.PatternCaptureTokenFilter;
import com.azure.search.models.PatternReplaceCharFilter;
import com.azure.search.models.PatternReplaceTokenFilter;
import com.azure.search.models.PatternTokenizer;
import com.azure.search.models.PhoneticEncoder;
import com.azure.search.models.PhoneticTokenFilter;
import com.azure.search.models.RegexFlags;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.models.ShingleTokenFilter;
import com.azure.search.models.SnowballTokenFilter;
import com.azure.search.models.SnowballTokenFilterLanguage;
import com.azure.search.models.StandardAnalyzer;
import com.azure.search.models.StandardTokenizerV2;
import com.azure.search.models.StemmerOverrideTokenFilter;
import com.azure.search.models.StemmerTokenFilter;
import com.azure.search.models.StemmerTokenFilterLanguage;
import com.azure.search.models.StopAnalyzer;
import com.azure.search.models.StopwordsList;
import com.azure.search.models.StopwordsTokenFilter;
import com.azure.search.models.SynonymTokenFilter;
import com.azure.search.models.TokenCharacterKind;
import com.azure.search.models.TokenFilter;
import com.azure.search.models.TokenFilterName;
import com.azure.search.models.Tokenizer;
import com.azure.search.models.TokenizerName;
import com.azure.search.models.TruncateTokenFilter;
import com.azure.search.models.UaxUrlEmailTokenizer;
import com.azure.search.models.UniqueTokenFilter;
import com.azure.search.models.WordDelimiterTokenFilter;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomAnalyzerAsyncTests extends CustomAnalyzerTestsBase {
    private SearchServiceAsyncClient searchServiceClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchServiceClient = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Override
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
            .setAnalyzers(Arrays.asList(
                new CustomAnalyzer()
                    .setTokenizer(TokenizerName.STANDARD_V2.toString())
                    .setCharFilters(Arrays.asList(customCharFilterName))
                    .setName(customAnalyzerName)
            ))
            .setCharFilters(Arrays.asList(
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
            .assertNext(firstPage -> {
                Assert.assertEquals("1", firstPage.get(0).getDocument().get("id"));
            })
            .verifyComplete();
    }

    @Override
    public void canUseAllAnalyzerNamesInIndexDefinition() {
        List<AnalyzerName> allAnalyzerNames = Arrays.asList(AnalyzerName.values());

        List<Field> fields = new ArrayList<>();
        int fieldNumber = 0;

        // All analyzer names can be set on the analyzer property.
        for (int i = 0; i < allAnalyzerNames.size(); i++) {
            DataType fieldType = (i % 2 == 0) ? DataType.EDM_STRING : DataType.COLLECTION_EDM_STRING;
            fields.add(new Field()
                .setName("field" + (fieldNumber++))
                .setType(fieldType)
                .setAnalyzer(allAnalyzerNames.get(i).toString()));
        }

        List<AnalyzerName> searchAnalyzersAndIndexAnalyzers = getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer();

        for (int i = 0; i < searchAnalyzersAndIndexAnalyzers.size(); i++) {
            DataType fieldType = (i % 2 == 0) ? DataType.EDM_STRING : DataType.COLLECTION_EDM_STRING;
            fields.add(new Field()
                .setName("field" + (fieldNumber++))
                .setType(fieldType)
                .setSearchable(true)
                .setSearchAnalyzer(searchAnalyzersAndIndexAnalyzers.get(i).toString())
                .setIndexAnalyzer(searchAnalyzersAndIndexAnalyzers.get(i).toString()));
        }

        fields.add(new Field()
            .setName("id")
            .setType(DataType.EDM_STRING)
            .setKey(true));
        Index index = new Index()
            .setName("hotel")
            .setFields(fields);

        StepVerifier
            .create(searchServiceClient.createIndex(index))
            .assertNext(res -> {
                assertIndexesEqual(index, res);
            })
            .verifyComplete();

        // Add language analyzers to searchAnalyzer and indexAnalyzer properties and expect failure
        try {
            new Field()
                .setName("field")
                .setType(DataType.EDM_STRING)
                .setSearchAnalyzer(AnalyzerName.EN_LUCENE.toString());
        } catch (Exception ex) {
            Assert.assertEquals(IllegalArgumentException.class, ex.getClass());
            Assert.assertTrue(ex.getMessage().equals("Only non-language analyzer can be used as search analyzer."));
        }
        try {
            new Field()
                .setName("field")
                .setType(DataType.EDM_STRING)
                .setIndexAnalyzer(AnalyzerName.AR_MICROSOFT.toString());
        } catch (Exception ex) {
            Assert.assertEquals(IllegalArgumentException.class, ex.getClass());
            Assert.assertTrue(ex.getMessage().equals("Only non-language analyzer can be used as index analyzer."));
        }
    }

    @Override
    public void canAnalyze() {
        Index index = createTestIndex();
        searchServiceClient.createIndex(index).block();

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setAnalyzer(AnalyzerName.WHITESPACE);
        StepVerifier
            .create(searchServiceClient.analyzeIndex(index.getName(), request))
            .assertNext(firstTokenInfo -> assertTokenInfoEqual("One", 0, 3, 0, firstTokenInfo))
            .assertNext(secondTokenInfo -> assertTokenInfoEqual("two", 4, 7, 1, secondTokenInfo))
            .expectNextCount(0L)
            .verifyComplete();

        request = new AnalyzeRequest()
            .setText("One's <two/>")
            .setTokenizer(TokenizerName.WHITESPACE)
            .setTokenFilters(Arrays.asList(TokenFilterName.APOSTROPHE))
            .setCharFilters(Arrays.asList(CharFilterName.HTML_STRIP));
        StepVerifier
            .create(searchServiceClient.analyzeIndex(index.getName(), request))
            .assertNext(onlyTokenInfo -> {
                // End offset is based on the original token, not the one emitted by the filters.
                assertTokenInfoEqual("One", 0, 5, 0, onlyTokenInfo);
            })
            .verifyComplete();

        StepVerifier
            .create(searchServiceClient.analyzeIndex(index.getName(), request, generateRequestOptions()))
            .assertNext(onlyTokenInfo -> {
                // End offset is based on the original token, not the one emitted by the filters.
                assertTokenInfoEqual("One", 0, 5, 0, onlyTokenInfo);
            })
            .verifyComplete();
    }

    @Override
    public void canAnalyzeWithAllPossibleNames() {
        Index index = createTestIndex();
        searchServiceClient.createIndex(index).block();

        Arrays.stream(AnalyzerName.values())
            .map(an -> new AnalyzeRequest()
                .setText("One two")
                .setAnalyzer(an))
            .forEach(r -> {
                searchServiceClient.analyzeIndex(index.getName(), r);
            });

        Arrays.stream(TokenizerName.values())
            .map(tn -> new AnalyzeRequest()
                .setText("One two")
                .setTokenizer(tn))
            .forEach(r -> {
                searchServiceClient.analyzeIndex(index.getName(), r);
            });

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setTokenizer(TokenizerName.WHITESPACE)
            .setTokenFilters(Arrays.asList(TokenFilterName.values()))
            .setCharFilters(Arrays.asList(CharFilterName.values()));
        searchServiceClient.analyzeIndex(index.getName(), request);
    }

    @Override
    public void addingCustomAnalyzerThrowsHttpExceptionByDefault() {
        Index index = createTestIndex()
            .setAnalyzers(Arrays.asList(
                new StopAnalyzer().setName("a1")
            ));
        searchServiceClient.createIndex(index).block();

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));

        StepVerifier
            .create(searchServiceClient.createOrUpdateIndex(index))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage()
                    .contains("Index update not allowed because it would cause downtime."));
            });
    }


    @Override
    public void canAddCustomAnalyzerWithIndexDowntime() {
        Index index = createTestIndex()
            .setAnalyzers(Arrays.asList(
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

    @Override
    public void canCreateAllAnalysisComponents() {
        final String customTokenizerName = "my_tokenizer";
        final String customTokenFilterName = "my_tokenfilter";
        final String customCharFilterName = "my_charfilter";

        Index index = createTestIndex()
            .setAnalyzers(Arrays.asList(
                new CustomAnalyzer()
                    .setTokenizer(customTokenizerName)
                    .setTokenFilters(Arrays.asList(customTokenFilterName))
                    .setCharFilters(Arrays.asList(customCharFilterName))
                    .setName(generateName()),
                new CustomAnalyzer()
                    .setTokenizer(TokenizerName.EDGE_NGRAM.toString())
                    .setName(generateName()),
                new PatternAnalyzer()
                    .setLowerCaseTerms(false)
                    .setPattern("abc")
                    .setFlags(RegexFlags.DOTALL)
                    .setStopwords(Arrays.asList("the"))
                    .setName(generateName()),
                new StandardAnalyzer()
                    .setMaxTokenLength(100)
                    .setStopwords(Arrays.asList("the"))
                    .setName(generateName()),
                new StopAnalyzer()
                    .setStopwords(Arrays.asList("the"))
                    .setName(generateName()),
                new StopAnalyzer()
                    .setName(generateName())
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(customTokenizerName),
                new EdgeNGramTokenizer()
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(Arrays.asList(TokenCharacterKind.LETTER))
                    .setName(generateName()),
                new NGramTokenizer()
                    .setMinGram(2)
                    .setMaxGram(4)
                    .setTokenChars(Arrays.asList(TokenCharacterKind.LETTER))
                    .setName(generateName()),
                new ClassicTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new KeywordTokenizerV2()
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
                new PathHierarchyTokenizerV2()
                    .setDelimiter(":")
                    .setReplacement("_")
                    .setMaxTokenLength(300)
                    .setReverseTokenOrder(true)
                    .setNumberOfTokensToSkip(2)
                    .setName(generateName()),
                new PatternTokenizer()
                    .setPattern(".*")
                    .setFlags(RegexFlags.MULTILINE)
                    .setGroup(0)
                    .setName(generateName()),
                new StandardTokenizerV2()
                    .setMaxTokenLength(100)
                    .setName(generateName()),
                new UaxUrlEmailTokenizer()
                    .setMaxTokenLength(100)
                    .setName(generateName())
            ))
            .setTokenFilters(Arrays.asList(
                new CjkBigramTokenFilter()
                    .setName(customTokenFilterName),  // One custom token filter for CustomAnalyzer above.
                new CjkBigramTokenFilter()
                    .setIgnoreScripts(Arrays.asList(CjkBigramTokenFilterScripts.HAN))
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
                    .setCommonWords(Arrays.asList("at"))
                    .setName(generateName()),
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Arrays.asList("Schadenfreude"))
                    .setMinWordSize(10)
                    .setMinSubwordSize(5)
                    .setMaxSubwordSize(13)
                    .setOnlyLongestMatch(true)
                    .setName(generateName()),
                new EdgeNGramTokenFilterV2()
                    .setMinGram(2)
                    .setMaxGram(10)
                    .setSide(EdgeNGramTokenFilterSide.BACK)
                    .setName(generateName()),
                new ElisionTokenFilter()
                    .setArticles(Arrays.asList("a"))
                    .setName(generateName()),
                new ElisionTokenFilter()
                    .setName(generateName()),
                new KeepTokenFilter()
                    .setKeepWords(Arrays.asList("aloha"))
                    .setName(generateName()),
                new KeepTokenFilter()
                    .setKeepWords(Arrays.asList("e", "komo", "mai"))
                    .setName(generateName()),
                new KeywordMarkerTokenFilter()
                    .setKeywords(Arrays.asList("key", "words"))
                    .setName(generateName()),
                new KeywordMarkerTokenFilter()
                    .setKeywords(Arrays.asList("essential"))
                    .setName(generateName()),
                new LengthTokenFilter()
                    .setMin(5)
                    .setMax(10)
                    .setName(generateName()),
                new LimitTokenFilter()
                    .setMaxTokenCount(10)
                    .setConsumeAllTokens(true)
                    .setName(generateName()),
                new NGramTokenFilterV2()
                    .setMinGram(2)
                    .setMaxGram(3)
                    .setName(generateName()),
                new PatternCaptureTokenFilter()
                    .setPatterns(Arrays.asList(".*"))
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
                    .setRules(Arrays.asList("ran => run"))
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
                    .setSynonyms(Arrays.asList("great, good"))
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
                    .setProtectedWords(Arrays.asList("protected"))
                    .setName(generateName())
            ))
            .setCharFilters(Arrays.asList(
                new MappingCharFilter()
                    .setMappings(Arrays.asList("a => b")) // One custom char filter for CustomeAnalyer above.
                    .setName(customCharFilterName),
                new MappingCharFilter()
                    .setMappings(Arrays.asList("s => $", "S => $"))
                    .setName(generateName()),
                new PatternReplaceCharFilter()
                    .setPattern("abc")
                    .setReplacement("123")
                    .setName(generateName())
            ));

        // We have to split up analysis components into two indexes, one where any components with optional properties
        // have defaults that are zero or null, and another where we need to specify the default values we
        // expect to get back from the REST API.

        int i = 0;

        Index indexWithSpecialDefaults = createTestIndex()
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer()
                    .setName(generateSimpleName(i++)),
                new StandardAnalyzer()
                    .setName(generateSimpleName(i++))
            ))
            .setTokenizers(Arrays.asList(
                new EdgeNGramTokenizer()
                    .setName(generateSimpleName(i++)),
                new NGramTokenizer()
                    .setName(generateSimpleName(i++)),
                new ClassicTokenizer()
                    .setName(generateSimpleName(i++)),
                new KeywordTokenizerV2()
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageStemmingTokenizer()
                    .setName(generateSimpleName(i++)),
                new MicrosoftLanguageTokenizer()
                    .setName(generateSimpleName(i++)),
                new PathHierarchyTokenizerV2()
                    .setName(generateSimpleName(i++)),
                new PatternTokenizer()
                    .setName(generateSimpleName(i++)),
                new StandardTokenizerV2()
                    .setName(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer()
                    .setName(generateSimpleName(i++))
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Arrays.asList("Bahnhof"))
                    .setName(generateSimpleName(i++)),
                new EdgeNGramTokenFilterV2()
                    .setName(generateSimpleName(i++)),
                new LengthTokenFilter()
                    .setName(generateSimpleName(i++)),
                new LimitTokenFilter()
                    .setName(generateSimpleName(i++)),
                new NGramTokenFilterV2()
                    .setName(generateSimpleName(i++)),
                new PatternCaptureTokenFilter()
                    .setPatterns(Arrays.asList("[a-z]*"))
                    .setName(generateSimpleName(i++)),
                new PhoneticTokenFilter()
                    .setName(generateSimpleName(i++)),
                new ShingleTokenFilter()
                    .setName(generateSimpleName(i++)),
                new StopwordsTokenFilter()
                    .setName(generateSimpleName(i++)),
                new SynonymTokenFilter()
                    .setSynonyms(Arrays.asList("mutt, canine => dog"))
                    .setName(generateSimpleName(i++)),
                new TruncateTokenFilter()
                    .setName(generateSimpleName(i++)),
                new WordDelimiterTokenFilter()
                    .setName(generateSimpleName(i++))
            ));

        i = 0;

        Index expectedIndexWithSpecialDefaults = createTestIndex()
            .setName(indexWithSpecialDefaults.getName())
            .setAnalyzers(Arrays.asList(
                new PatternAnalyzer()
                    .setLowerCaseTerms(true)
                    .setPattern("\\W+")
                    .setName(generateSimpleName(i++)),
                new StandardAnalyzer()
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
                new KeywordTokenizerV2()
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
                new PathHierarchyTokenizerV2()
                    .setDelimiter("/")
                    .setReplacement("/")
                    .setMaxTokenLength(300)
                    .setName(generateSimpleName(i++)),
                new PatternTokenizer()
                    .setPattern("\\W+")
                    .setGroup(-1)
                    .setName(generateSimpleName(i++)),
                new StandardTokenizerV2()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++)),
                new UaxUrlEmailTokenizer()
                    .setMaxTokenLength(255)
                    .setName(generateSimpleName(i++))
            ))
            .setTokenFilters(Arrays.asList(
                new DictionaryDecompounderTokenFilter()
                    .setWordList(Arrays.asList("Bahnhof"))
                    .setMinWordSize(5)
                    .setMinSubwordSize(2)
                    .setMaxSubwordSize(15)
                    .setName(generateSimpleName(i++)),
                new EdgeNGramTokenFilterV2()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setSide(EdgeNGramTokenFilterSide.FRONT)
                    .setName(generateSimpleName(i++)),
                new LengthTokenFilter()
                    .setMax(300)
                    .setName(generateSimpleName(i++)),
                new LimitTokenFilter()
                    .setMaxTokenCount(1)
                    .setName(generateSimpleName(i++)),
                new NGramTokenFilterV2()
                    .setMinGram(1)
                    .setMaxGram(2)
                    .setName(generateSimpleName(i++)),
                new PatternCaptureTokenFilter()
                    .setPatterns(Arrays.asList("[a-z]*"))
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
                    .setSynonyms(Arrays.asList("mutt, canine => dog"))
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
                    .setName(generateSimpleName(i++))
            ));

        StepVerifier
            .create(searchServiceClient.createIndex(index))
            .assertNext(res -> assertAnalysisComponentsEqual(index, res))
            .verifyComplete();
        searchServiceClient.deleteIndex(index.getName()).block();

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

    @Override
    public void canUseAllAnalysisComponentNames() {
        Analyzer analyzerWithAllTokenFilterAndCharFilters =
            new CustomAnalyzer()
                .setTokenizer(TokenizerName.LOWERCASE.toString())
                .setTokenFilters(Stream.of(TokenFilterName.values())
                    .map(tf -> tf.toString())
                    .collect(Collectors.toList()))
                .setCharFilters(Stream.of(CharFilterName.values())
                    .map(cf -> cf.toString())
                    .collect(Collectors.toList()))
                .setName("abc");

        Index index = createTestIndex();
        List<Analyzer> analyzers = new ArrayList<>();
        analyzers.add(analyzerWithAllTokenFilterAndCharFilters);
        analyzers.addAll(Arrays.asList(TokenizerName.values())
            .stream()
            .map(tn -> new CustomAnalyzer()
                .setTokenizer(tn.toString())
                .setName(generateName()))
            .collect(Collectors.toList()));
        index.setAnalyzers(analyzers);

        StepVerifier
            .create(searchServiceClient.createIndex(index))
            .assertNext(res -> assertAnalysisComponentsEqual(index, res))
            .verifyComplete();
    }

    @Override
    public void canUseAllRegexFlags() {
        Index index = createTestIndex()
            .setAnalyzers(Arrays.asList(RegexFlags.values())
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

    @Override
    public void canUseAllAnalysisComponentOptions() {
        Index index = createTestIndex();

        // Set tokenizers
        List<Tokenizer> tokenizers = new ArrayList<>();
        tokenizers.add(
            new EdgeNGramTokenizer()
                .setMinGram(1)
                .setMaxGram(2)
                .setTokenChars(Arrays.asList(TokenCharacterKind.values()))
                .setName(generateName())
        );
        tokenizers.addAll(
            Arrays.stream(MicrosoftTokenizerLanguage.values())
                .map(mtl -> new MicrosoftLanguageTokenizer()
                    .setMaxTokenLength(200)
                    .setIsSearchTokenizer(false)
                    .setLanguage(mtl)
                    .setName(generateName()))
            .collect(Collectors.toList())
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
                .map(s -> new EdgeNGramTokenFilterV2()
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

        List<Index> indexes = splitIndex(index);
        indexes.forEach(expectedIndex -> {
            StepVerifier
                .create(searchServiceClient.createIndex(expectedIndex))
                .assertNext(res -> assertAnalysisComponentsEqual(expectedIndex, res))
                .verifyComplete();
            searchServiceClient.deleteIndex(expectedIndex.getName()).block();
        });
    }
}
