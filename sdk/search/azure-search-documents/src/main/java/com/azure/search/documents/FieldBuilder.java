// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.annotation.FieldIgnore;
import com.azure.search.annotation.FieldProperty;
import com.azure.search.annotation.SearchableFieldProperty;
import com.azure.search.annotation.SimpleFieldProperty;
import com.azure.search.documents.models.AnalyzerName;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.GeoPoint;

import java.lang.annotation.Annotation;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper to build search field
 */
public class FieldBuilder {
    /**
     * Creates a collection of <see cref="Field"/> objects corresponding to
     * the properties of the type supplied.
     *
     * @param modelClass The class type for which fields will be created, based on its properties.
     * @return A collection of fields.
     */
    public static <T> List<Field> build(Class<T> modelClass) {
        ClientLogger logger = new ClientLogger(FieldBuilder.class);
        java.lang.reflect.Field[] fields = modelClass.getFields();
        List<Field> searchFieldList = new ArrayList<>();
        for (java.lang.reflect.Field field: fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof FieldIgnore) {
                    continue;
                }
                Field searchField = new Field();
                searchField.setName(field.getName());
                searchField.setType(covertToDataType(field, logger));
                if (annotation instanceof SimpleFieldProperty) {
                    buildSimpleField(searchField, (SimpleFieldProperty) annotation);
                    searchFieldList.add(searchField);
                } else if (annotation instanceof SearchableFieldProperty) {
                    buildSearchableField(searchField, (SearchableFieldProperty) annotation);
                    searchFieldList.add(searchField);
                } else if (annotation instanceof FieldProperty) {
                    buildField(searchField, (FieldProperty) annotation);
                    searchFieldList.add(searchField);
                }
                logger.info("The property {} is not a search field.", field.getName());
            }
        }
        return searchFieldList;
    }

    private static void buildSimpleField(Field searchField, SimpleFieldProperty annotation) {
        searchField.setSearchable(true);
        searchField.setSortable(true);
        searchField.setFilterable(true);
        searchField.setFacetable(true);
        searchField.setKey(annotation.isKey());
        if (!"null".equals(annotation.analyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.analyzer())));
        }
        if (!"null".equals(annotation.searchAnalyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.searchAnalyzer())));
        }
        if (!"null".equals(annotation.indexAnalyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.indexAnalyzer())));
        }
        if (annotation.synonymMaps().length != 0) {
            searchField.setSynonymMaps(Arrays.asList(annotation.synonymMaps()));
        }
    }

    private static void buildSearchableField(Field searchField, SearchableFieldProperty annotation) {
        searchField.setSearchable(true);
        searchField.setSortable(annotation.isSortable());
        searchField.setFilterable(annotation.isFilterable());
        searchField.setFacetable(annotation.isFacetable());
        searchField.setKey(annotation.isKey());
        if (!"null".equals(annotation.analyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.analyzer())));
        }
        if (!"null".equals(annotation.searchAnalyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.searchAnalyzer())));
        }
        if (!"null".equals(annotation.indexAnalyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.indexAnalyzer())));
        }
        if (annotation.synonymMaps().length != 0) {
            searchField.setSynonymMaps(Arrays.asList(annotation.synonymMaps()));
        }
    }

    private static void buildField(Field searchField, FieldProperty annotation) {
        searchField.setSearchable(annotation.isSearchable());
        searchField.setSortable(annotation.isSortable());
        searchField.setFilterable(annotation.isFilterable());
        searchField.setFacetable(annotation.isFacetable());
        searchField.setKey(annotation.isKey());
        if (!"null".equals(annotation.analyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.analyzer())));
        }
        if (!"null".equals(annotation.searchAnalyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.searchAnalyzer())));
        }
        if (!"null".equals(annotation.indexAnalyzer())) {
            searchField.setAnalyzer(AnalyzerName.fromString((annotation.indexAnalyzer())));
        }
        if (annotation.synonymMaps().length != 0) {
            searchField.setSynonymMaps(Arrays.asList(annotation.synonymMaps()));
        }
    }


    private static DataType covertToDataType(java.lang.reflect.Field field, ClientLogger logger) {
        Class<?> type = field.getType();
        if (type.isAssignableFrom(String.class)) {
            return DataType.EDM_STRING;
        }
        if (type.isAssignableFrom(String[].class)) {
            return DataType.collection(DataType.EDM_STRING);
        }
        if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            return DataType.EDM_INT32;
        }
        if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
            return DataType.EDM_INT64;
        }
        if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
            return DataType.EDM_BOOLEAN;
        }
        if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
            return DataType.EDM_DOUBLE;
        }
        if (type.isAssignableFrom(OffsetDateTime.class) || type.isAssignableFrom(Integer.class)) {
            return DataType.EDM_DATE_TIME_OFFSET;
        }
        if (type.isAssignableFrom(GeoPoint.class)) {
            return DataType.EDM_GEOGRAPHY_POINT;
        }
        throw logger.logExceptionAsError(new IllegalArgumentException(String.format("The type %s of property is "
            + "invalid field type for Search service. Please check DataType class for reference.",
            type.getSimpleName())));
    }
}

