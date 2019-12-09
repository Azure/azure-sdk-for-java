// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.Analyzer;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CharFilter;
import com.azure.search.models.Index;
import com.azure.search.models.TokenFilter;
import com.azure.search.models.TokenInfo;
import com.azure.search.models.Tokenizer;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;


public abstract class CustomAnalyzerTestsBase extends SearchServiceTestBase {
    private static final String NAME_PREFIX = "azsmnet";

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    public abstract void canSearchWithCustomAnalyzer();

    @Test
    public abstract void canUseAllAnalyzerNamesInIndexDefinition();

    @Test
    public abstract void canAnalyze();

    @Test
    public abstract void canAnalyzeWithAllPossibleNames();

    @Test
    public abstract void addingCustomAnalyzerThrowsHttpExceptionByDefault();

    @Test
    public abstract void canAddCustomAnalyzerWithIndexDowntime();

    @Test
    public abstract void canCreateAllAnalysisComponents();

    @Test
    public abstract void canUseAllAnalysisComponentNames();

    @Test
    public abstract void canUseAllRegexFlags();

    @Test
    public abstract void canUseAllAnalysisComponentOptions();

    void addAnalyzerToIndex(Index index, Analyzer analyzer) {
        List<Analyzer> analyzers = new ArrayList<>(index.getAnalyzers());
        analyzers.add(analyzer);

        index.setAnalyzers(analyzers);
    }

    void assertAnalysisComponentsEqual(Index expected, Index actual) {
        // Compare analysis components directly so that test failures show better comparisons.
        // Analyzers
        List<Analyzer> expectedAnalyzers = expected.getAnalyzers();
        List<Analyzer> actualAnalyzers = actual.getAnalyzers();
        if (expectedAnalyzers != null && actualAnalyzers != null) {
            Assert.assertEquals(expectedAnalyzers.size(), actualAnalyzers.size());
            for (int i = 0; i < expectedAnalyzers.size(); i++) {
                assertAnalyzersEqual(expectedAnalyzers.get(i), actualAnalyzers.get(i));
            }
        }

        // Tokenizer
        List<Tokenizer> expectedTokenizers = expected.getTokenizers();
        List<Tokenizer> actualTokenizers = actual.getTokenizers();
        if (expectedTokenizers != null && actualTokenizers != null) {
            Assert.assertEquals(expectedTokenizers.size(), actualTokenizers.size());
            for (int i = 0; i < expectedTokenizers.size(); i++) {
                assertTokenizersEqual(expectedTokenizers.get(i), actualTokenizers.get(i));
            }
        }

        // Char filter
        List<CharFilter> expectedCharfilters = expected.getCharFilters();
        List<CharFilter> actualCharfilters = actual.getCharFilters();
        if (expectedCharfilters != null && actualCharfilters != null) {
            Assert.assertEquals(expectedCharfilters.size(), actualCharfilters.size());
        }
    }

    private void assertAnalyzersEqual(Analyzer expected, Analyzer actual) {
        expected.setName("none");
        actual.setName("none");
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    private void assertTokenizersEqual(Tokenizer expected, Tokenizer actual) {
        expected.setName("none");
        actual.setName("none");
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    String generateName() {
        return SdkContext.randomResourceName(NAME_PREFIX, 24);
    }

    /**
     * Custom analysis components (analyzer/tokenzier/tokenFilter/charFilter) count in index must be between 0 and 50.
     * Split an Index into indexes, each of which has a total analysis components count within the limit.
     */
    List<Index> splitIndex(Index index) {

        Collection<List<Analyzer>> analyzersLists = splitAnalysisComponents(index.getAnalyzers());
        List<Index> indexes = analyzersLists
            .stream()
            .map(a -> createTestIndex().setAnalyzers(a)).collect(Collectors.toList());

        Collection<List<Tokenizer>> tokenizersLists = splitAnalysisComponents(index.getTokenizers());
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

    void assertTokenInfoEqual(String expectedToken,
                              Integer expectedStartOffset,
                              Integer expectedEndOffset,
                              Integer expectedPosition,
                              TokenInfo actual) {
        Assert.assertEquals(expectedToken, actual.getToken());
        Assert.assertEquals(expectedStartOffset, actual.getStartOffset());
        Assert.assertEquals(expectedEndOffset, actual.getEndOffset());
        Assert.assertEquals(expectedPosition, actual.getPosition());
    }

    String generateSimpleName(int n) {
        return String.format("a%d", n);
    }

    List<AnalyzerName> getAnalyzersAllowedForSearchAnalyzerAndIndexAnalyzer() {
        // Only non-language analyzer names can be set on the searchAnalyzer and indexAnalyzer properties.
        // ASSUMPTION: Only language analyzers end in .lucene or .microsoft.
        return AnalyzerName.values()
            .stream()
            .filter(an -> !an.toString().endsWith(".lucene") && !an.toString().endsWith(".microsoft"))
            .collect(Collectors.toList());
    }
}
