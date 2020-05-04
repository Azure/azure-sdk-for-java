// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import java.util.List;

/**
 * A helper Field model to build a searchable {@link Field}.
 */
public class SearchableField extends SimpleField {
    private AnalyzerName analyzer;
    private AnalyzerName searchAnalyzer;
    private AnalyzerName indexAnalyzer;
    private List<String> synonymMapNames;

    /**
     * Initializes a new instance of the {@link SearchableField} class.
     *
     * @param name The name of the field, which must be unique within the index or parent field.
     * @param collection Whether the field is a collection of strings.
     * @throws NullPointerException when {@code name} is null.
     */
    public SearchableField(String name, boolean collection) {
        super(name, DataType.EDM_STRING, collection);
    }

    /**
     * Gets the name of the language analyzer. This property cannot be set when either {@code searchAnalyzer} or
     * {@code indexAnalyzer} are set. Once the analyzer is chosen, it cannot be changed for the field in the index.
     *
     * @return The {@link AnalyzerName} used for analyzer.
     */
    public AnalyzerName getAnalyzer() {
        return analyzer;
    }

    /**
     * Sets the name of the language analyzer. This property cannot be set when either {@code searchAnalyzer} or
     * {@code indexAnalyzer} are set. Once the analyzer is chosen, it cannot be changed for the field in the index.
     *
     * @param analyzer The {@link AnalyzerName} used for analyzer.
     * @return The SearchableField object itself.
     */
    public SearchableField setAnalyzer(AnalyzerName analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    /**
     * Gets the name of the language analyzer for searching. This property must be set together with
     * {@code indexAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @return The {@link AnalyzerName} used for search analyzer.
     */
    public AnalyzerName getSearchAnalyzer() {
        return searchAnalyzer;
    }

    /**
     * Sets the name of the language analyzer for searching. This property must be set together with
     * {@code indexAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @param searchAnalyzer The {@link AnalyzerName} used for search analyzer.
     * @return The SearchableField object itself.
     */
    public SearchableField setSearchAnalyzer(AnalyzerName searchAnalyzer) {
        this.searchAnalyzer = searchAnalyzer;
        return this;
    }

    /**
     * Gets the name of the language analyzer for indexing. This property must be set together with
     * {@code searchAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @return The {@link AnalyzerName} used for index analyzer.
     */
    public AnalyzerName getIndexAnalyzer() {
        return indexAnalyzer;
    }

    /**
     * Gets the name of the language analyzer for indexing. This property must be set together with
     * {@code searchAnalyzer}, and cannot be set when {@code analyzer} is set. Once the analyzer is chosen, it cannot be
     * changed for the field in the index.
     *
     * @param indexAnalyzer The {@link AnalyzerName} used for index analyzer.
     * @return The SearchableField object itself.
     */
    public SearchableField setIndexAnalyzer(AnalyzerName indexAnalyzer) {
        this.indexAnalyzer = indexAnalyzer;
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
    public SearchableField setSynonymMapNames(List<String> synonymMapNames) {
        this.synonymMapNames = synonymMapNames;
        return this;
    }

    /**
     * Convert SearchableField to {@link Field}.
     *
     * @return The {@link Field} object.
     */
    public Field build() {
        return new Field()
            .setName(super.getName())
            .setType(super.getDataType())
            .setSearchable(true)
            .setKey(super.isKey())
            .setSortable(super.isSortable())
            .setFilterable(super.isFilterable())
            .setHidden(super.isHidden())
            .setFacetable(super.isFacetable())
            .setAnalyzer(this.analyzer)
            .setSearchAnalyzer(this.searchAnalyzer)
            .setIndexAnalyzer(this.indexAnalyzer)
            .setSynonymMaps(this.synonymMapNames);
    }
}
