package com.azure.search.documents.models.utils;

import com.azure.search.documents.models.Field;

public class FieldUnion {
    public static Field union(Field f1, Field f2) {
        if (f1 == null && f2 == null) {
            return null;
        }
        if (f1 == null || f2 == null) {
            return f1 == null ? f2 : f1;
        }
        return new Field().setName(union(f1.getName(), f2.getName()))
            .setType(union(f1.getType(), f2.getType()))
            .setKey(union(f1.isKey(), f2.isKey()))
            .setRetrievable(union(f1.isRetrievable(), f2.isRetrievable()))
            .setSearchable(union(f1.isSearchable(), f2.isSearchable()))
            .setFilterable(union(f1.isFilterable(), f2.isFilterable()))
            .setSortable(union(f1.isSortable(), f2.isSortable()))
            .setFacetable(union(f1.isFacetable(), f2.isFacetable()))
            .setAnalyzer(union(f1.getAnalyzer(), f2.getAnalyzer()))
            .setSearchAnalyzer(union(f1.getSearchAnalyzer(), f2.getSearchAnalyzer()))
            .setIndexAnalyzer(union(f1.getIndexAnalyzer(), f2.getIndexAnalyzer()))
            .setSynonymMaps(union(f1.getSynonymMaps(), f2.getSynonymMaps()))
            .setFields(union(f1.getFields(), f2.getFields()));
    }

    private static <T> T union(T t1, T t2) {
        if (t1 == null) {
            return t2;
        }
        return t1;
    }
}
