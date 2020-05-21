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

        Boolean _filterable = obj.isFilterable();
        searchField.setFilterable(_filterable);

        Boolean _hidden = obj.isRetrievable() == null ? null : !obj.isRetrievable();
        searchField.setHidden(_hidden);

        Boolean _sortable = obj.isSortable();
        searchField.setSortable(_sortable);

        if (obj.getType() != null) {
            SearchFieldDataType _type = SearchFieldDataTypeConverter.map(obj.getType());
            searchField.setType(_type);
        }

        Boolean _searchable = obj.isSearchable();
        searchField.setSearchable(_searchable);

        if (obj.getAnalyzer() != null) {
            LexicalAnalyzerName _analyzer = LexicalAnalyzerNameConverter.map(obj.getAnalyzer());
            searchField.setAnalyzer(_analyzer);
        }

        if (obj.getSearchAnalyzer() != null) {
            LexicalAnalyzerName _searchAnalyzer = LexicalAnalyzerNameConverter.map(obj.getSearchAnalyzer());
            searchField.setSearchAnalyzer(_searchAnalyzer);
        }

        String _name = obj.getName();
        searchField.setName(_name);

        if (obj.getIndexAnalyzer() != null) {
            LexicalAnalyzerName _indexAnalyzer = LexicalAnalyzerNameConverter.map(obj.getIndexAnalyzer());
            searchField.setIndexAnalyzer(_indexAnalyzer);
        }

        Boolean _facetable = obj.isFacetable();
        searchField.setFacetable(_facetable);

        if (obj.getSynonymMaps() != null) {
            List<String> _synonymMaps = new ArrayList<>(obj.getSynonymMaps());
            searchField.setSynonymMaps(_synonymMaps);
        }

        if (obj.getFields() != null) {
            List<SearchField> _fields =
                obj.getFields().stream().map(com.azure.search.documents.implementation.converters.SearchFieldConverter::map).collect(Collectors.toList());
            searchField.setFields(_fields);
        }

        Boolean _key = obj.isKey();
        searchField.setKey(_key);
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

        Boolean _filterable = obj.isFilterable();
        searchField.setFilterable(_filterable);

        Boolean _hidden = obj.isHidden() == null ? null : !obj.isHidden();
        searchField.setRetrievable(_hidden);

        Boolean _sortable = obj.isSortable();
        searchField.setSortable(_sortable);

        if (obj.getType() != null) {
            com.azure.search.documents.implementation.models.SearchFieldDataType _type =
                SearchFieldDataTypeConverter.map(obj.getType());
            searchField.setType(_type);
        }

        Boolean _searchable = obj.isSearchable();
        searchField.setSearchable(_searchable);

        if (obj.getAnalyzer() != null) {
            com.azure.search.documents.implementation.models.LexicalAnalyzerName _analyzer =
                LexicalAnalyzerNameConverter.map(obj.getAnalyzer());
            searchField.setAnalyzer(_analyzer);
        }

        if (obj.getSearchAnalyzer() != null) {
            com.azure.search.documents.implementation.models.LexicalAnalyzerName _searchAnalyzer =
                LexicalAnalyzerNameConverter.map(obj.getSearchAnalyzer());
            searchField.setSearchAnalyzer(_searchAnalyzer);
        }

        String _name = obj.getName();
        searchField.setName(_name);

        if (obj.getIndexAnalyzer() != null) {
            com.azure.search.documents.implementation.models.LexicalAnalyzerName _indexAnalyzer =
                LexicalAnalyzerNameConverter.map(obj.getIndexAnalyzer());
            searchField.setIndexAnalyzer(_indexAnalyzer);
        }

        Boolean _facetable = obj.isFacetable();
        searchField.setFacetable(_facetable);

        if (obj.getSynonymMaps() != null) {
            List<String> _synonymMaps = new ArrayList<>(obj.getSynonymMaps());
            searchField.setSynonymMaps(_synonymMaps);
        }

        if (obj.getFields() != null) {
            List<com.azure.search.documents.implementation.models.SearchField> _fields =
                obj.getFields().stream().map(com.azure.search.documents.implementation.converters.SearchFieldConverter::map).collect(Collectors.toList());
            searchField.setFields(_fields);
        }

        Boolean _key = obj.isKey();
        searchField.setKey(_key);
        return searchField;
    }
}
