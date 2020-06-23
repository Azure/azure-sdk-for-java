// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.FieldIgnore;
import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;
import com.azure.search.documents.models.AnalyzerName;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.GeoPoint;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Helper to convert model class to Search {@link Field fields}.
 */
public final class FieldBuilder {
    private static final int MAX_DEPTH = 10000;
    private static final Map<Class<?>, DataType> SUPPORTED_NONE_PARAMETERIZED_TYPE = new HashMap<>();

    static {
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Integer.class, DataType.EDM_INT32);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(int.class, DataType.EDM_INT32);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Long.class, DataType.EDM_INT64);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(long.class, DataType.EDM_INT64);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Double.class, DataType.EDM_DOUBLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(double.class, DataType.EDM_DOUBLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Boolean.class, DataType.EDM_BOOLEAN);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(boolean.class, DataType.EDM_BOOLEAN);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(String.class, DataType.EDM_STRING);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Date.class, DataType.EDM_DATE_TIME_OFFSET);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(OffsetDateTime.class, DataType.EDM_DATE_TIME_OFFSET);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(GeoPoint.class, DataType.EDM_GEOGRAPHY_POINT);
    }

    private static final List<Class<?>> UNSUPPORTED_TYPES = Arrays.asList(Byte.class,
        CharSequence.class,
        Character.class,
        char.class,
        Float.class,
        float.class,
        Short.class,
        short.class);

    /**
     * Creates a collection of {@link Field} objects corresponding to the properties of the type supplied.
     *
     * @param modelClass The class for which fields will be created, based on its properties.
     * @param <T> The generic type of the model class.
     * @return A collection of fields.
     */
    public static <T> List<Field> build(Class<T> modelClass) {
        ClientLogger logger = new ClientLogger(FieldBuilder.class);
        return build(modelClass, new Stack<>(), logger);
    }

    /**
     * Recursive class to build complex data type.
     *
     * @param currentClass Current class to be built.
     * @param classChain A class chain from {@code modelClass} to prior of {@code currentClass}.
     * @param logger {@link ClientLogger}.
     * @return A list of {@link Field} that currentClass is built to.
     */
    private static List<Field> build(Class<?> currentClass, Stack<Class<?>> classChain, ClientLogger logger) {
        if (classChain.contains(currentClass)) {
            logger.warning(String.format("There is circular dependencies %s, %s", classChain, currentClass));
            return null;
        }
        if (classChain.size() > MAX_DEPTH) {
            throw logger.logExceptionAsError(new RuntimeException(
                "The dependency graph is too deep. Please review your schema."));
        }
        classChain.push(currentClass);
        List<Field> searchFields = Arrays.stream(currentClass.getDeclaredFields())
            .filter(classField -> !classField.isAnnotationPresent(FieldIgnore.class))
            .map(classField -> buildField(classField, classChain, logger))
            .collect(Collectors.toList());
        classChain.pop();
        return searchFields;
    }

    private static Field buildField(java.lang.reflect.Field classField, Stack<Class<?>> classChain,
        ClientLogger logger) {
        Type type = classField.getGenericType();

        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return buildNoneParameterizedType(classField, logger);
        }
        if (isArrayOrList(type)) {
            return buildCollectionField(classField, classChain, logger);
        }
        List<Field> childFields = build((Class<?>) type, classChain, logger);
        Field searchField = convertToBasicSearchField(classField, logger);
        searchField.setFields(childFields);
        return searchField;
    }

    private static Field buildNoneParameterizedType(java.lang.reflect.Field classField,
        ClientLogger logger) {
        Field searchField = convertToBasicSearchField(classField, logger);
        return enrichWithAnnotation(searchField, classField, logger);
    }


    private static boolean isArrayOrList(Type type) {
        return type.getClass().isArray() || isList(type);
    }

    private static boolean isList(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        Type rawType = ((ParameterizedType) type).getRawType();
        return List.class.isAssignableFrom((Class<?>) rawType);
    }

    private static Field buildCollectionField(java.lang.reflect.Field classField,
        Stack<Class<?>> classChain, ClientLogger logger) {
        Type componentOrElementType = getComponentOrElementType(classField.getGenericType(), logger);
        validateType(componentOrElementType, true, logger);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(componentOrElementType)) {
            Field searchField = convertToBasicSearchField(classField, logger);
            return enrichWithAnnotation(searchField, classField, logger);
        }
        List<Field> childFields = build((Class<?>) componentOrElementType, classChain, logger);
        Field searchField = convertToBasicSearchField(classField, logger);
        searchField.setFields(childFields);
        return searchField;
    }

    private static Type getComponentOrElementType(Type arrayOrListType, ClientLogger logger) {
        if (arrayOrListType.getClass().isArray()) {
            return arrayOrListType.getClass().getComponentType();
        }
        if (isList(arrayOrListType)) {
            ParameterizedType pt = (ParameterizedType) arrayOrListType;
            return pt.getActualTypeArguments()[0];
        }
        throw logger.logExceptionAsError(new RuntimeException(String.format(
            "Collection type %s is not supported.", arrayOrListType.getTypeName())));
    }

    private static Field convertToBasicSearchField(java.lang.reflect.Field classField,
        ClientLogger logger) {
        Field searchField = new Field();
        searchField.setName(classField.getName());
        DataType dataType = covertToDataType(classField.getGenericType(), false, logger);
        searchField.setType(dataType)
            .setKey(false)
            .setSearchable(false)
            .setFacetable(false)
            .setHidden(false)
            .setFilterable(false)
            .setSortable(false);
        return searchField;
    }

    private static Field enrichWithAnnotation(Field searchField, java.lang.reflect.Field classField,
        ClientLogger logger) {
        if (classField.isAnnotationPresent(SimpleFieldProperty.class)
            && classField.isAnnotationPresent(SearchableFieldProperty.class)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                    String.format("@SimpleFieldProperty and @SearchableFieldProperty cannot be present simultaneously "
                        + "for %s", classField.getName())));
        }
        if (classField.isAnnotationPresent(SimpleFieldProperty.class)) {
            SimpleFieldProperty simpleFieldPropertyAnnotation =
                classField.getDeclaredAnnotation(SimpleFieldProperty.class);
            searchField.setSearchable(false)
                .setSortable(simpleFieldPropertyAnnotation.isSortable())
                .setFilterable(simpleFieldPropertyAnnotation.isFilterable())
                .setFacetable(simpleFieldPropertyAnnotation.isFacetable())
                .setKey(simpleFieldPropertyAnnotation.isKey())
                .setHidden(simpleFieldPropertyAnnotation.isHidden());
        } else if (classField.isAnnotationPresent(SearchableFieldProperty.class)) {
            if (!searchField.getType().equals(DataType.EDM_STRING)
                && !searchField.getType().equals(DataType.collection(DataType.EDM_STRING))) {
                throw logger.logExceptionAsError(new RuntimeException(String.format("SearchFieldProperty can only"
                    + " be used on string properties. Property %s returns a %s value.",
                    classField.getName(), searchField.getType())));
            }
            SearchableFieldProperty searchableFieldPropertyAnnotation =
                classField.getDeclaredAnnotation(SearchableFieldProperty.class);
            searchField.setSearchable(true)
                .setSortable(searchableFieldPropertyAnnotation.isSortable())
                .setFilterable(searchableFieldPropertyAnnotation.isFilterable())
                .setFacetable(searchableFieldPropertyAnnotation.isFacetable())
                .setKey(searchableFieldPropertyAnnotation.isKey())
                .setHidden(searchableFieldPropertyAnnotation.isHidden());
            String analyzer = searchableFieldPropertyAnnotation.analyzer();
            String searchAnalyzer = searchableFieldPropertyAnnotation.searchAnalyzer();
            String indexAnalyzer = searchableFieldPropertyAnnotation.indexAnalyzer();
            if (!analyzer.isEmpty() && (!searchAnalyzer.isEmpty() || !indexAnalyzer.isEmpty())) {
                throw logger.logExceptionAsError(new RuntimeException(
                    "Please specify either analyzer or both searchAnalyzer and indexAnalyzer."));
            }
            if (!searchableFieldPropertyAnnotation.analyzer().isEmpty()) {
                searchField.setAnalyzer(AnalyzerName.fromString((searchableFieldPropertyAnnotation.analyzer())));
            }
            if (!searchableFieldPropertyAnnotation.searchAnalyzer().isEmpty()) {
                searchField.setAnalyzer(AnalyzerName.fromString((searchableFieldPropertyAnnotation.searchAnalyzer())));
            }
            if (!searchableFieldPropertyAnnotation.indexAnalyzer().isEmpty()) {
                searchField.setAnalyzer(AnalyzerName.fromString((searchableFieldPropertyAnnotation.indexAnalyzer())));
            }
            if (searchableFieldPropertyAnnotation.synonymMaps().length != 0) {
                List<String> synonymMaps = Arrays.stream(searchableFieldPropertyAnnotation.synonymMaps())
                    .filter(synonym -> !synonym.trim().isEmpty()).collect(Collectors.toList());
                searchField.setSynonymMaps(synonymMaps);
            }
        }
        return searchField;
    }

    private static void validateType(Type type, boolean hasArrayOrCollectionWrapped, ClientLogger logger) {
        if (!(type instanceof ParameterizedType)) {
            if (UNSUPPORTED_TYPES.contains(type)) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format("%s is not supported",
                    type.getTypeName())));
            }
            return;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Map and its subclasses are not supported"));
        }
        if (hasArrayOrCollectionWrapped) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Only single-dimensional array is supported."));
        }
        if (!List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                String.format("Collection type %s is not supported", type.getTypeName())));
        }
    }

    private static DataType covertToDataType(Type type, boolean hasArrayOrCollectionWrapped, ClientLogger logger) {
        validateType(type, hasArrayOrCollectionWrapped, logger);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return SUPPORTED_NONE_PARAMETERIZED_TYPE.get(type);
        }
        if (isArrayOrList(type)) {
            Type componentOrElementType = getComponentOrElementType(type, logger);
            return DataType.collection(covertToDataType(componentOrElementType, true, logger));
        }
        return DataType.EDM_COMPLEX_TYPE;
    }
}
