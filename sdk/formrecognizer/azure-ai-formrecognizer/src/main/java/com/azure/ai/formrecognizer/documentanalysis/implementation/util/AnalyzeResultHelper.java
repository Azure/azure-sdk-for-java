// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentKeyValuePair;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLanguage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentParagraph;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentStyle;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeResult} instance.
 */
public final class AnalyzeResultHelper {
    private static AnalyzeResultAccessor accessor;

    private AnalyzeResultHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeResult} instance.
     */
    public interface AnalyzeResultAccessor {
        void setModelId(AnalyzeResult analyzeResult, String id);
        void setContent(AnalyzeResult analyzeResult, String content);
        void setPages(AnalyzeResult analyzeResult, List<DocumentPage> pages);
        void setTables(AnalyzeResult analyzeResult, List<DocumentTable> tables);
        void setKeyValuePairs(AnalyzeResult analyzeResult, List<DocumentKeyValuePair> keyValuePairs);
        void setStyles(AnalyzeResult analyzeResult, List<DocumentStyle> documentStyles);
        void setDocuments(AnalyzeResult analyzeResult, List<AnalyzedDocument> documents);
        void setLanguages(AnalyzeResult analyzeResult, List<DocumentLanguage> languages);
        void setParagraphs(AnalyzeResult analyzeResult, List<DocumentParagraph> paragraphs);
    }

    /**
     * The method called from {@link AnalyzeResult} to set it's accessor.
     *
     * @param analyzeResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeResultAccessor analyzeResultAccessor) {
        accessor = analyzeResultAccessor;
    }

    static void setModelId(AnalyzeResult analyzeResult, String modelId) {
        accessor.setModelId(analyzeResult, modelId);
    }

    static void setContent(AnalyzeResult analyzeResult, String content) {
        accessor.setContent(analyzeResult, content);
    }

    static void setPages(AnalyzeResult analyzeResult, List<DocumentPage> pages) {
        accessor.setPages(analyzeResult, pages);
    }

    static void setTables(AnalyzeResult analyzeResult, List<DocumentTable> tables) {
        accessor.setTables(analyzeResult, tables);
    }

    static void setKeyValuePairs(AnalyzeResult analyzeResult, List<DocumentKeyValuePair> keyValuePairs) {
        accessor.setKeyValuePairs(analyzeResult, keyValuePairs);
    }
    static void setDocuments(AnalyzeResult analyzeResult, List<AnalyzedDocument> documents) {
        accessor.setDocuments(analyzeResult, documents);
    }

    static void setStyles(AnalyzeResult analyzeResult, List<DocumentStyle> styles) {
        accessor.setStyles(analyzeResult, styles);
    }

    static void setLanguages(AnalyzeResult analyzeResult, List<DocumentLanguage> languages) {
        accessor.setLanguages(analyzeResult, languages);
    }

    static void setParagraphs(AnalyzeResult analyzeResult, List<DocumentParagraph> paragraphs) {
        accessor.setParagraphs(analyzeResult, paragraphs);
    }
}
