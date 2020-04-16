// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

/**
 * A helper Field model to build a simple search field.
 */
public class SimpleSearchField {
    private String name;
    private PrimitiveType dataType;
//    private Boolean searchable;
//    private Boolean filterable;
//    private Boolean sortable;
//    private Boolean facetable;
//    private Boolean hidden;
//    private AnalyzerName analyzer;
//    private AnalyzerName searchAnalyzer;
//    private AnalyzerName indexAnalyzer;

    public String getName() {
        return name;
    }

    public SimpleSearchField setName(String name) {
        this.name = name;
        return this;
    }

    public PrimitiveType getDataType() {
        return dataType;
    }

    public SimpleSearchField setDataType(PrimitiveType dataType) {
        this.dataType = dataType;
        return this;
    }
//
//    public Boolean getSearchable() {
//        return searchable;
//    }
//
//    public SimpleSearchField setSearchable(Boolean searchable) {
//        this.searchable = searchable;
//        return this;
//    }
//
//    public Boolean getFilterable() {
//        return filterable;
//    }
//
//    public SimpleSearchField setFilterable(Boolean filterable) {
//        this.filterable = filterable;
//        return this;
//    }
//
//    public Boolean getSortable() {
//        return sortable;
//    }
//
//    public SimpleSearchField setSortable(Boolean sortable) {
//        this.sortable = sortable;
//        return this;
//    }
//
//    public Boolean getFacetable() {
//        return facetable;
//    }
//
//    public SimpleSearchField setFacetable(Boolean facetable) {
//        this.facetable = facetable;
//        return this;
//    }
//
//    public Boolean getHidden() {
//        return hidden;
//    }
//
//    public SimpleSearchField setHidden(Boolean hidden) {
//        this.hidden = hidden;
//        return this;
//    }
//
//
//    public AnalyzerName getAnalyzer() {
//        return analyzer;
//    }
//
//    public SimpleSearchField setAnalyzer(AnalyzerName analyzer) {
//        this.analyzer = analyzer;
//        return this;
//    }
//
//    public AnalyzerName getSearchAnalyzer() {
//        return searchAnalyzer;
//    }
//
//    public SimpleSearchField setSearchAnalyzer(AnalyzerName searchAnalyzer) {
//        this.searchAnalyzer = searchAnalyzer;
//        return this;
//    }
//
//    public AnalyzerName getIndexAnalyzer() {
//        return indexAnalyzer;
//    }
//
//    public SimpleSearchField setIndexAnalyzer(AnalyzerName indexAnalyzer) {
//        this.indexAnalyzer = indexAnalyzer;
//        return this;
//    }

    public Field build() {
        return new Field().setName(name).setType(dataType.toDataType());
    }
}
