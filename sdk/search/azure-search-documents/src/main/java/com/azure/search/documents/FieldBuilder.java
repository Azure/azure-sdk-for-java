// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.FieldIgnore;
import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;
import com.azure.search.documents.models.AnalyzerName;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.GeoPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
     * @param curClass Current class to be built.
     * @param classChain A class chain from {@code modelClass} to prior of {@code curClass}.
     * @param logger {@link ClientLogger}.
     * @return A list of {@link Field} that curClass is built to.
     */
    private static List<Field> build(Class<?> curClass, Stack<Class<?>> classChain, ClientLogger logger) {
        if (classChain.contains(curClass)) {
            logger.warning(String.format("There is circular dependencies %s, %s", classChain, curClass));
            return null;
        }
        if (classChain.size() > MAX_DEPTH) {
            throw logger.logExceptionAsError(new RuntimeException(
                "The dependency graph is too deep. Please review your schema."));
        }
        classChain.push(curClass);
        List<java.lang.reflect.Field> classFields = Arrays.asList(curClass.getDeclaredFields());
        List<Field> searchFields = classFields.stream()
            .filter(classField -> !classField.isAnnotationPresent(FieldIgnore.class))
            .map(classField -> buildField(classField, classChain, logger))
            .collect(Collectors.toList());
        classChain.pop();
        return searchFields;
    }

    private static Field buildField(java.lang.reflect.Field classField, Stack<Class<?>> classChain,
        ClientLogger logger) {
        Type type = classField.getGenericType();

        if (isSupportedNoneParameterizedType(type)) {
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

    private static boolean isSupportedNoneParameterizedType(Type type) {
        return SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type);
    }

    private static Field buildNoneParameterizedType(java.lang.reflect.Field classField,
        ClientLogger logger) {
        Field searchField = convertToBasicSearchField(classField, logger);
        return enrichWithAnnotation(searchField, classField, logger);
    }


    private static boolean isArrayOrList(Type type) {
        if (type.getClass().isArray()) {
            return true;
        }
        return isList(type);
    }

    private static boolean isList(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            return List.class.isAssignableFrom((Class<?>) rawType);
        }
        return false;
    }

    private static Field buildCollectionField(java.lang.reflect.Field classField,
        Stack<Class<?>> classChain, ClientLogger logger) {
        Type componentOrElementType = getComponentOrElementType(classField.getGenericType(), logger);
        if (isSupportedNoneParameterizedType(componentOrElementType)) {
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
        throw logger.logExceptionAsError(new RuntimeException("Should not be there"));
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
                searchField.setSynonymMaps(Arrays.asList(searchableFieldPropertyAnnotation.synonymMaps()));
            }
        }
        return searchField;
    }

    private static void validateType(Class<?> type, boolean hasArrayOrCollectionWrapped, ClientLogger logger) {
        if (Map.class.isAssignableFrom(type)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Map and its subclasses are not supported"));
        }
        if (UNSUPPORTED_TYPES.contains(type)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format("%s is not supported",
                    type.getName())));
        }
        if (Collection.class.isAssignableFrom(type)) {
            if (!List.class.isAssignableFrom(type)) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    String.format("%s is not supported", type.getName())));
            }
            if (hasArrayOrCollectionWrapped) {
                throw logger.logExceptionAsError(new IllegalArgumentException(""));
            }
        }
    }

    private static DataType covertToDataType(Type type, boolean hasArrayOrCollectionWrapped, ClientLogger logger) {
        validateType(type.getClass(), hasArrayOrCollectionWrapped, logger);
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
