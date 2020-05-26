// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.LexicalAnalyzerName;
import com.azure.search.documents.models.SearchField;
import com.azure.search.documents.models.SearchFieldDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchField} and {@link SearchField}.
 */
public final class SearchFieldConverter {
    private static final ClientLogger LOGGER =
        new ClientLogger(com.azure.search.documents.implementation.converters.SearchFieldConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchField} to {@link SearchField}.
     */
    public static SearchField map(com.azure.search.documents.implementation.models.SearchField obj) {
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
            searchField.setAnalyzer(analyzer);
        }

        if (obj.getSearchAnalyzer() != null) {
            LexicalAnalyzerName searchAnalyzer = LexicalAnalyzerNameConverter.map(obj.getSearchAnalyzer());
            searchField.setSearchAnalyzer(searchAnalyzer);
        }

        String name = obj.getName();
        searchField.setName(name);

        if (obj.getIndexAnalyzer() != null) {
            LexicalAnalyzerName indexAnalyzer = LexicalAnalyzerNameConverter.map(obj.getIndexAnalyzer());
            searchField.setIndexAnalyzer(indexAnalyzer);
        }

        Boolean facetable = obj.isFacetable();
        searchField.setFacetable(facetable);

        if (obj.getSynonymMaps() != null) {
            List<String> synonymMaps = new ArrayList<>(obj.getSynonymMaps());
            searchField.setSynonymMaps(synonymMaps);
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
     * Maps from {@link SearchField} to {@link com.azure.search.documents.implementation.models.SearchField}.
     */
    public static com.azure.search.documents.implementation.models.SearchField map(SearchField obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchField searchField =
            new com.azure.search.documents.implementation.models.SearchField();

        Boolean filterable = obj.isFilterable();
        searchField.setFilterable(filterable);

        Boolean hidden = obj.isHidden() == null ? null : !obj.isHidden();
        searchField.setRetrievable(hidden);

        Boolean sortable = obj.isSortable();
        searchField.setSortable(sortable);

        if (obj.getType() != null) {
            com.azure.search.documents.implementation.models.SearchFieldDataType type =
                SearchFieldDataTypeConverter.map(obj.getType());
            searchField.setType(type);
        }

        Boolean searchable = obj.isSearchable();
        searchField.setSearchable(searchable);

        if (obj.getAnalyzer() != null) {
            com.azure.search.documents.implementation.models.LexicalAnalyzerName analyzer =
                LexicalAnalyzerNameConverter.map(obj.getAnalyzer());
            searchField.setAnalyzer(analyzer);
        }

        if (obj.getSearchAnalyzer() != null) {
            com.azure.search.documents.implementation.models.LexicalAnalyzerName searchAnalyzer =
                LexicalAnalyzerNameConverter.map(obj.getSearchAnalyzer());
            searchField.setSearchAnalyzer(searchAnalyzer);
        }

        String name = obj.getName();
        searchField.setName(name);

        if (obj.getIndexAnalyzer() != null) {
            com.azure.search.documents.implementation.models.LexicalAnalyzerName indexAnalyzer =
                LexicalAnalyzerNameConverter.map(obj.getIndexAnalyzer());
            searchField.setIndexAnalyzer(indexAnalyzer);
        }

        Boolean facetable = obj.isFacetable();
        searchField.setFacetable(facetable);

        if (obj.getSynonymMaps() != null) {
            List<String> synonymMaps = new ArrayList<>(obj.getSynonymMaps());
            searchField.setSynonymMaps(synonymMaps);
        }

        if (obj.getFields() != null) {
            List<com.azure.search.documents.implementation.models.SearchField> fields =
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
