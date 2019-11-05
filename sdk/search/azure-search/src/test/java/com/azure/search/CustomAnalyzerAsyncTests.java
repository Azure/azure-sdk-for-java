// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.models.AnalyzeRequest;
import com.azure.search.models.Analyzer;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CharFilterName;
import com.azure.search.models.CjkBigramTokenFilter;
import com.azure.search.models.CjkBigramTokenFilterScripts;
import com.azure.search.models.CustomAnalyzer;
import com.azure.search.models.DataType;
import com.azure.search.models.EdgeNGramTokenFilterSide;
import com.azure.search.models.EdgeNGramTokenFilterV2;
import com.azure.search.models.EdgeNGramTokenizer;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.MicrosoftLanguageStemmingTokenizer;
import com.azure.search.models.MicrosoftLanguageTokenizer;
import com.azure.search.models.MicrosoftStemmingTokenizerLanguage;
import com.azure.search.models.MicrosoftTokenizerLanguage;
import com.azure.search.models.PatternAnalyzer;
import com.azure.search.models.PhoneticEncoder;
import com.azure.search.models.PhoneticTokenFilter;
import com.azure.search.models.RegexFlags;
import com.azure.search.models.SnowballTokenFilter;
import com.azure.search.models.SnowballTokenFilterLanguage;
import com.azure.search.models.StemmerTokenFilter;
import com.azure.search.models.StemmerTokenFilterLanguage;
import com.azure.search.models.StopAnalyzer;
import com.azure.search.models.StopwordsList;
import com.azure.search.models.StopwordsTokenFilter;
import com.azure.search.models.TokenCharacterKind;
import com.azure.search.models.TokenFilter;
import com.azure.search.models.TokenFilterName;
import com.azure.search.models.Tokenizer;
import com.azure.search.models.TokenizerName;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CustomAnalyzerAsyncTests extends CustomAnalyzerTestsBase {
    private SearchServiceAsyncClient searchServiceClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchServiceClient = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Override
    public void canSearchWithCustomAnalyzer() {
        // TODO: This test is blocked on not being able to create custom enum from string values.
        // https://dev.azure.com/csee2e/Azure%20Search%20SDK/_workitems/edit/1396
        // One possible solution is to add a an overload of the enum's constructor to take a string value.
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
                .setAnalyzer(allAnalyzerNames.get(i)));
        }

        List<AnalyzerName> searchAnalyzersAndIndexAnalyzers = getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer();

        for (int i = 0; i < searchAnalyzersAndIndexAnalyzers.size(); i++) {
            DataType fieldType = (i % 2 == 0) ? DataType.EDM_STRING : DataType.COLLECTION_EDM_STRING;
            fields.add(new Field()
                .setName("field" + (fieldNumber++))
                .setType(fieldType)
                .setSearchable(true)
                .setSearchAnalyzer(searchAnalyzersAndIndexAnalyzers.get(i))
                .setIndexAnalyzer(searchAnalyzersAndIndexAnalyzers.get(i)));
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
        Field fieldWithLanguageAnalyzer = new Field()
            .setName("field" + (fieldNumber++))
            .setType(DataType.EDM_STRING)
            .setSearchable(true)
            .setSearchAnalyzer(AnalyzerName.EN_LUCENE)
            .setIndexAnalyzer(AnalyzerName.AR_MICROSOFT);
        addFieldToIndex(index, fieldWithLanguageAnalyzer);
        StepVerifier
            .create(searchServiceClient.createOrUpdateIndex(index))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage()
                    .contains("Language analyzers can be only specified in the Analyzer property."));
            });
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
            .create(searchServiceClient.createOrUpdateIndex(index, true))
            .assertNext(res -> assertAnalysisComponentsEqual(index, res))
            .verifyComplete();
    }

    @Override
    public void canCreateAllAnalysisComponents() {
        // TODO: This test is blocked on not being able to create custom enum from string values.
        // https://dev.azure.com/csee2e/Azure%20Search%20SDK/_workitems/edit/1397
        // One possible solution is to add a an overload of the enum's constructor to take a string value.
    }

    @Override
    public void canUseAllAnalysisComponentNames() {
        Analyzer analyzerWithAllTokenFilterAndCharFilters =
            new CustomAnalyzer()
                .setTokenizer(TokenizerName.LOWERCASE)
                .setTokenFilters(Arrays.asList(TokenFilterName.values()))
                .setCharFilters(Arrays.asList(CharFilterName.values()))
                .setName("abc");

        Index index = createTestIndex();
        List<Analyzer> analyzers = new ArrayList<>();
        analyzers.add(analyzerWithAllTokenFilterAndCharFilters);
        analyzers.addAll(Arrays.asList(TokenizerName.values())
            .stream()
            .map(tn -> new CustomAnalyzer()
                .setTokenizer(tn)
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
