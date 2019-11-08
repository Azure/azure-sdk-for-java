// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
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
import com.azure.search.models.TokenInfo;
import com.azure.search.models.Tokenizer;
import com.azure.search.models.TokenizerName;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomAnalyzerSyncTests extends CustomAnalyzerTestsBase {
    private SearchServiceClient searchServiceClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchServiceClient = getSearchServiceClientBuilder().buildClient();
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
        Index createdIndex = searchServiceClient.createIndex(index);
        assertIndexesEqual(index, createdIndex);

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
        searchServiceClient.createIndex(index);

        AnalyzeRequest request = new AnalyzeRequest()
            .setText("One two")
            .setAnalyzer(AnalyzerName.WHITESPACE);
        PagedIterable<TokenInfo> results = searchServiceClient.analyzeIndex(index.getName(), request);
        Iterator<TokenInfo> it = results.stream().iterator();
        assertTokenInfoEqual("One", 0, 3, 0, it.next());
        assertTokenInfoEqual("two", 4, 7, 1, it.next());
        Assert.assertFalse(it.hasNext());

        request = new AnalyzeRequest()
            .setText("One's <two/>")
            .setTokenizer(TokenizerName.WHITESPACE)
            .setTokenFilters(Arrays.asList(TokenFilterName.APOSTROPHE))
            .setCharFilters(Arrays.asList(CharFilterName.HTML_STRIP));
        results = searchServiceClient.analyzeIndex(index.getName(), request);
        // End offset is based on the original token, not the one emitted by the filters.
        it = results.stream().iterator();
        assertTokenInfoEqual("One", 0, 5, 0, it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Override
    public void canAnalyzeWithAllPossibleNames() {
        Index index = createTestIndex();
        searchServiceClient.createIndex(index);

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
        searchServiceClient.createIndex(index);

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));

        try {
            searchServiceClient.createOrUpdateIndex(index);
            Assert.fail("createOrUpdate did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(),
                ((HttpResponseException) ex).getResponse().getStatusCode());
            Assert.assertTrue(ex.getMessage().contains("Index update not allowed because it would cause downtime."));
        }
    }

    @Override
    public void canAddCustomAnalyzerWithIndexDowntime() {
        Index index = createTestIndex()
            .setAnalyzers(Arrays.asList(
                new StopAnalyzer().setName("a1")
            ));
        searchServiceClient.createIndex(index);

        addAnalyzerToIndex(index, new StopAnalyzer().setName("a2"));
        Index updatedIndex = searchServiceClient.createOrUpdateIndex(index, true);

        assertAnalysisComponentsEqual(index, updatedIndex);
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

        Index createdIndex = searchServiceClient.createIndex(index);

        assertAnalysisComponentsEqual(index, createdIndex);
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

        Index createdIndex = searchServiceClient.createIndex(index);

        assertAnalysisComponentsEqual(index, createdIndex);
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
            Index createdIndex = searchServiceClient.createIndex(expectedIndex);
            assertAnalysisComponentsEqual(expectedIndex, createdIndex);
            searchServiceClient.deleteIndex(createdIndex.getName());
        });
    }
}
