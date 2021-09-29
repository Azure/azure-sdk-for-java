// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.AnalyzeResultHelper;

import java.util.List;

/**
 * Document analysis result.
 */
public final class AnalyzeResult {

    /*
     * Model ID used to produce this result.
     */
    private String modelId;

    /*
     * Method used to compute string offset and length.
     */
    private StringIndexType stringIndexType;

    /*
     * Concatenate string representation of all textual and visual elements in
     * reading order.
     */
    private String content;

    /*
     * Analyzed pages.
     */
    private List<DocumentPage> pages;

    /*
     * Extracted tables.
     */
    private List<DocumentTable> tables;

    /*
     * Extracted key-value pairs.
     */
    private List<DocumentKeyValuePair> keyValuePairs;

    /*
     * Extracted entities.
     */
    private List<DocumentEntity> entities;

    /*
     * Extracted font styles.
     */
    private List<DocumentStyle> styles;

    /*
     * Extracted documents.
     */
    private List<AnalyzedDocument> documents;

    /**
     * Get the modelId property: Model ID used to produce this result.
     *
     * @return the modelId value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Set the modelId property: Model ID used to produce this result.
     *
     * @param modelId the modelId value to set.
     * @return the AnalyzeResult object itself.
     */
    void setModelId(String modelId) {
        this.modelId = modelId;
    }

    /**
     * Get the stringIndexType property: Method used to compute string offset and length.
     *
     * @return the stringIndexType value.
     */
    public StringIndexType getStringIndexType() {
        return this.stringIndexType;
    }

    /**
     * Set the stringIndexType property: Method used to compute string offset and length.
     *
     * @param stringIndexType the stringIndexType value to set.
     * @return the AnalyzeResult object itself.
     */
    void setStringIndexType(StringIndexType stringIndexType) {
        this.stringIndexType = stringIndexType;
    }

    /**
     * Get the content property: Concatenate string representation of all textual and visual elements in reading order.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Concatenate string representation of all textual and visual elements in reading order.
     *
     * @param content the content value to set.
     * @return the AnalyzeResult object itself.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the pages property: Analyzed pages.
     *
     * @return the pages value.
     */
    public List<DocumentPage> getPages() {
        return this.pages;
    }

    /**
     * Set the pages property: Analyzed pages.
     *
     * @param pages the pages value to set.
     * @return the AnalyzeResult object itself.
     */
    void setPages(List<DocumentPage> pages) {
        this.pages = pages;
    }

    /**
     * Get the tables property: Extracted tables.
     *
     * @return the tables value.
     */
    public List<DocumentTable> getTables() {
        return this.tables;
    }

    /**
     * Set the tables property: Extracted tables.
     *
     * @param tables the tables value to set.
     * @return the AnalyzeResult object itself.
     */
    void setTables(List<DocumentTable> tables) {
        this.tables = tables;
    }

    /**
     * Get the keyValuePairs property: Extracted key-value pairs.
     *
     * @return the keyValuePairs value.
     */
    public List<DocumentKeyValuePair> getKeyValuePairs() {
        return this.keyValuePairs;
    }

    /**
     * Set the keyValuePairs property: Extracted key-value pairs.
     *
     * @param keyValuePairs the keyValuePairs value to set.
     * @return the AnalyzeResult object itself.
     */
    void setKeyValuePairs(List<DocumentKeyValuePair> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    /**
     * Get the entities property: Extracted entities.
     *
     * @return the entities value.
     */
    public List<DocumentEntity> getEntities() {
        return this.entities;
    }

    /**
     * Set the entities property: Extracted entities.
     *
     * @param entities the entities value to set.
     * @return the AnalyzeResult object itself.
     */
    void setEntities(List<DocumentEntity> entities) {
        this.entities = entities;
    }

    /**
     * Get the styles property: Extracted font styles.
     *
     * @return the styles value.
     */
    public List<DocumentStyle> getStyles() {
        return this.styles;
    }

    /**
     * Set the styles property: Extracted font styles.
     *
     * @param styles the styles value to set.
     * @return the AnalyzeResult object itself.
     */
    void setStyles(List<DocumentStyle> styles) {
        this.styles = styles;
    }

    /**
     * Get the documents property: Extracted documents.
     *
     * @return the documents value.
     */
    public List<AnalyzedDocument> getDocuments() {
        return this.documents;
    }

    /**
     * Set the documents property: Extracted documents.
     *
     * @param documents the documents value to set.
     * @return the AnalyzeResult object itself.
     */
    void setDocuments(List<AnalyzedDocument> documents) {
        this.documents = documents;
    }

    static {
        AnalyzeResultHelper.setAccessor(new AnalyzeResultHelper.AnalyzeResultAccessor() {
            @Override
            public void setModelId(AnalyzeResult analyzeResult, String id) {
                analyzeResult.setModelId(id);
            }

            @Override
            public void setStringIndexType(AnalyzeResult analyzeResult, StringIndexType stringIndexType) {
                analyzeResult.setStringIndexType(stringIndexType);
            }

            @Override
            public void setContent(AnalyzeResult analyzeResult, String content) {
                analyzeResult.setContent(content);
            }

            @Override
            public void setPages(AnalyzeResult analyzeResult, List<DocumentPage> pages) {
                analyzeResult.setPages(pages);
            }

            @Override
            public void setTables(AnalyzeResult analyzeResult, List<DocumentTable> tables) {
                analyzeResult.setTables(tables);
            }

            @Override
            public void setKeyValuePairs(AnalyzeResult analyzeResult, List<DocumentKeyValuePair> keyValuePairs) {
                analyzeResult.setKeyValuePairs(keyValuePairs);
            }

            @Override
            public void setEntities(AnalyzeResult analyzeResult, List<DocumentEntity> entities) {
                analyzeResult.setEntities(entities);
            }

            @Override
            public void setStyles(AnalyzeResult analyzeResult, List<DocumentStyle> styles) {
                analyzeResult.setStyles(styles);
            }

            @Override
            public void setDocuments(AnalyzeResult analyzeResult, List<AnalyzedDocument> documents) {
                analyzeResult.setDocuments(documents);
            }
        });
    }
}
