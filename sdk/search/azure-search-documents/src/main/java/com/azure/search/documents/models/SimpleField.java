// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

/**
 * A helper Field model to build a simple search field.
 */
public class SimpleField {
    private String name;
    private PrimitiveType dataType;
    private Boolean facetable;
    private AnalyzerName analyzer;
    private AnalyzerName searchAnalyzer;
    private AnalyzerName indexAnalyzer;

    public String getName() {
        return name;
    }

    public SimpleField setName(String name) {
        this.name = name;
        return this;
    }

    public PrimitiveType getDataType() {
        return dataType;
    }

    public SimpleField setDataType(PrimitiveType dataType) {
        this.dataType = dataType;
        return this;
    }


    public Boolean getFacetable() {
        return facetable;
    }

    public SimpleField setFacetable(Boolean facetable) {
        this.facetable = facetable;
        return this;
    }

    public AnalyzerName getAnalyzer() {
        return analyzer;
    }

    public SimpleField setAnalyzer(AnalyzerName analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public AnalyzerName getSearchAnalyzer() {
        return searchAnalyzer;
    }

    public SimpleField setSearchAnalyzer(AnalyzerName searchAnalyzer) {
        this.searchAnalyzer = searchAnalyzer;
        return this;
    }

    public AnalyzerName getIndexAnalyzer() {
        return indexAnalyzer;
    }

    public SimpleField setIndexAnalyzer(AnalyzerName indexAnalyzer) {
        this.indexAnalyzer = indexAnalyzer;
        return this;
    }

    public Field build() {
        return new Field().setName(name).setType(dataType.toDataType()).setSearchable(true)
            .setSortable(true).setFilterable(true).setHidden(false);
    }
}
