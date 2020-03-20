package com.azure.search.documents.models.fieldInfo;

import com.azure.search.documents.models.analyzerName.NonLanguageAnalyzerName;

public class DualAnalyzerInfo {
    private NonLanguageAnalyzerName searchAnalyzer;
    private NonLanguageAnalyzerName indexAnalyzer;

    public DualAnalyzerInfo(NonLanguageAnalyzerName searchAnalyzer, NonLanguageAnalyzerName indexAnalyer) {
        this.searchAnalyzer = searchAnalyzer;
        this.indexAnalyzer = indexAnalyer;
    }

    public NonLanguageAnalyzerName getSearchAnalyzer() {
        return this.searchAnalyzer;
    }

    public NonLanguageAnalyzerName getIndexAnalyzer() {
        return this.indexAnalyzer;
    }
}
