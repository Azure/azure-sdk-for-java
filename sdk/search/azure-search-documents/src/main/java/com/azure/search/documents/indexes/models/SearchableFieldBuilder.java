// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import java.util.List;

/**
 * A helper Field model to build a searchable {@link SearchField}.
 */
public class SearchableFieldBuilder extends SimpleFieldBuilder {
    private LexicalAnalyzerName analyzerName;
    private LexicalAnalyzerName searchAnalyzerName;
    private LexicalAnalyzerName indexAnalyzerName;
    private List<String> synonymMapNames;

    /**
     * Initializes a new instance of the {@link SearchableFieldBuilder} class.
     *
     * @param name The name of the field, which must be unique within the index or parent field.
     * @param collection Whether the field is a collection of strings.
     * @throws NullPointerException when {@code name} is null.
     */
    public SearchableFieldBuilder(String name, boolean collection) {
        super(name, SearchFieldDataType.STRING, collection);
    }

    /**
     * Gets the name of the language analyzer. This property cannot be set when either {@code searchAnalyzer} or
     * {@code indexAnalyzer} are set. Once the analyzer is chosen, it cannot be changed for the field in the index.
     *
     * @return The {@link LexicalAnalyzerName} used for analyzer.
     */
    public LexicalAnalyzerName getAnalyzerName() {
        return analyzerName;
    }

    /**
     * Sets the name of the language analyzer. This property cannot be set when either {@code searchAnalyzer} or
     * {@code indexAnalyzer} are set. Once the analyzer is chosen, it cannot be changed for the field in the index.
     *
     * @param analyzerName The {@link LexicalAnalyzerName} used for analyzer.
     * @return The SearchableFieldBuilder object itself.
     */
    public SearchableFieldBuilder setAnalyzerName(LexicalAnalyzerName analyzerName) {
        this.analyzerName = analyzerName;
        return this;
    }

    /**
     * Gets the name of the language analyzer for searching. This property must be set together with
     * {@code indexAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @return The {@link LexicalAnalyzerName} used for search analyzer.
     */
    public LexicalAnalyzerName getSearchAnalyzerName() {
        return searchAnalyzerName;
    }

    /**
     * Sets the name of the language analyzer for searching. This property must be set together with
     * {@code indexAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @param searchAnalyzerName The {@link LexicalAnalyzerName} used for search analyzer.
     * @return The SearchableField object itself.
     */
    public SearchableFieldBuilder setSearchAnalyzerName(LexicalAnalyzerName searchAnalyzerName) {
        this.searchAnalyzerName = searchAnalyzerName;
        return this;
    }

    /**
     * Gets the name of the language analyzer for indexing. This property must be set together with
     * {@code searchAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @return The {@link LexicalAnalyzerName} used for index analyzer.
     */
    public LexicalAnalyzerName getIndexAnalyzerName() {
        return indexAnalyzerName;
    }

    /**
     * Gets the name of the language analyzer for indexing. This property must be set together with
     * {@code searchAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @param indexAnalyzerName The {@link LexicalAnalyzerName} used for index analyzer.
     * @return The SearchableField object itself.
     */
    public SearchableFieldBuilder setIndexAnalyzerName(LexicalAnalyzerName indexAnalyzerName) {
        this.indexAnalyzerName = indexAnalyzerName;
        return this;
    }

    /**
     * Gets a list of names of synonym maps to associate with this field.
     * Currently, only one synonym map per field is supported.
     *
     * Assigning a synonym map to a field ensures that query terms targeting that field are expanded at query-time using
     * the rules in the synonym map. This attribute can be changed on existing fields.
     *
     * @return List of names of synonym maps to associate with this field.
     */
    public List<String> getSynonymMapNames() {
        return synonymMapNames;
    }

    /**
     * Sets a list of names of synonym maps to associate with this field.
     * Currently, only one synonym map per field is supported.
     *
     * Assigning a synonym map to a field ensures that query terms targeting that field are expanded at query-time using
     * the rules in the synonym map. This attribute can be changed on existing fields.
     *
     * @param synonymMapNames list of names of synonym maps to associate with this field.
     * @return The SearchableField object itself.
     */
    public SearchableFieldBuilder setSynonymMapNames(List<String> synonymMapNames) {
        this.synonymMapNames = synonymMapNames;
        return this;
    }

    /**
     * Convert SearchableField to {@link SearchField}.
     *
     * @return The {@link SearchField} object.
     */
    public SearchField build() {
        return new SearchField()
            .setName(super.getName())
            .setType(super.getDataType())
            .setSearchable(true)
            .setKey(super.isKey())
            .setSortable(super.isSortable())
            .setFilterable(super.isFilterable())
            .setHidden(super.isHidden())
            .setFacetable(super.isFacetable())
            .setAnalyzerName(this.analyzerName)
            .setSearchAnalyzerName(this.searchAnalyzerName)
            .setIndexAnalyzerName(this.indexAnalyzerName)
            .setSynonymMapNames(this.synonymMapNames);
    }
}
