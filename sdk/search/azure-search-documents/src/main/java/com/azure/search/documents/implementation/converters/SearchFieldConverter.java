// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchField} and {@link SearchField}.
 */
public final class SearchFieldConverter {
    private static final ClientLogger LOGGER =
        new ClientLogger(com.azure.search.documents.implementation.converters.SearchFieldConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchField} to {@link SearchField}.
     */
    public static SearchField map(com.azure.search.documents.indexes.implementation.models.SearchField obj) {
        if (obj == null) {
            return null;
        }
        SearchField searchField = new SearchField();

        Boolean filterable = obj.isFilterable();
        searchField.setFilterable(filterable);

        Boolean hidden = obj.isRetrievable() == null ? null : !obj.isRetrievable();
        searchField.setHidden(hidden);

        Boolean sortable = obj.isSortable();
        searchField.setSortable(sortable);

        if (obj.getType() != null) {
            SearchFieldDataType type = SearchFieldDataTypeConverter.map(obj.getType());
            searchField.setType(type);
        }

        Boolean searchable = obj.isSearchable();
        searchField.setSearchable(searchable);

        if (obj.getAnalyzer() != null) {
            LexicalAnalyzerName analyzer = LexicalAnalyzerNameConverter.map(obj.getAnalyzer());
            searchField.setAnalyzerName(analyzer);
        }

        if (obj.getSearchAnalyzer() != null) {
            LexicalAnalyzerName searchAnalyzer = LexicalAnalyzerNameConverter.map(obj.getSearchAnalyzer());
            searchField.setSearchAnalyzerName(searchAnalyzer);
        }

        String name = obj.getName();
        searchField.setName(name);

        if (obj.getIndexAnalyzer() != null) {
            LexicalAnalyzerName indexAnalyzer = LexicalAnalyzerNameConverter.map(obj.getIndexAnalyzer());
            searchField.setIndexAnalyzerName(indexAnalyzer);
        }

        Boolean facetable = obj.isFacetable();
        searchField.setFacetable(facetable);

        if (obj.getSynonymMaps() != null) {
            List<String> synonymMaps = new ArrayList<>(obj.getSynonymMaps());
            searchField.setSynonymMapNames(synonymMaps);
        }

        if (obj.getFields() != null) {
            List<SearchField> fields =
                obj.getFields().stream().map(com.azure.search.documents.implementation.converters.SearchFieldConverter::map).collect(Collectors.toList());
            searchField.setFields(fields);
        }

        Boolean key = obj.isKey();
        searchField.setKey(key);
        return searchField;
    }

    /**
     * Maps from {@link SearchField} to {@link com.azure.search.documents.indexes.implementation.models.SearchField}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchField map(SearchField obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SearchField searchField =
            new com.azure.search.documents.indexes.implementation.models.SearchField();

        Boolean filterable = obj.isFilterable();
        searchField.setFilterable(filterable);

        Boolean hidden = obj.isHidden() == null ? null : !obj.isHidden();
        searchField.setRetrievable(hidden);

        Boolean sortable = obj.isSortable();
        searchField.setSortable(sortable);

        if (obj.getType() != null) {
            com.azure.search.documents.indexes.implementation.models.SearchFieldDataType type =
                SearchFieldDataTypeConverter.map(obj.getType());
            searchField.setType(type);
        }

        Boolean searchable = obj.isSearchable();
        searchField.setSearchable(searchable);

        if (obj.getAnalyzerName() != null) {
            com.azure.search.documents.indexes.implementation.models.LexicalAnalyzerName analyzer =
                LexicalAnalyzerNameConverter.map(obj.getAnalyzerName());
            searchField.setAnalyzer(analyzer);
        }

        if (obj.getSearchAnalyzerName() != null) {
            com.azure.search.documents.indexes.implementation.models.LexicalAnalyzerName searchAnalyzer =
                LexicalAnalyzerNameConverter.map(obj.getSearchAnalyzerName());
            searchField.setSearchAnalyzer(searchAnalyzer);
        }

        String name = obj.getName();
        searchField.setName(name);

        if (obj.getIndexAnalyzerName() != null) {
            com.azure.search.documents.indexes.implementation.models.LexicalAnalyzerName indexAnalyzer =
                LexicalAnalyzerNameConverter.map(obj.getIndexAnalyzerName());
            searchField.setIndexAnalyzer(indexAnalyzer);
        }

        Boolean facetable = obj.isFacetable();
        searchField.setFacetable(facetable);

        if (obj.getSynonymMapNames() != null) {
            List<String> synonymMaps = new ArrayList<>(obj.getSynonymMapNames());
            searchField.setSynonymMaps(synonymMaps);
        }

        if (obj.getFields() != null) {
            List<com.azure.search.documents.indexes.implementation.models.SearchField> fields =
                obj.getFields().stream().map(com.azure.search.documents.implementation.converters.SearchFieldConverter::map).collect(Collectors.toList());
            searchField.setFields(fields);
        }

        Boolean key = obj.isKey();
        searchField.setKey(key);
        return searchField;
    }

    private SearchFieldConverter() {
    }
}
