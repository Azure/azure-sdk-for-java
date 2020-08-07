// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.search.documents.indexes.FieldIgnore;
import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Helper to convert model class to Search {@link SearchField fields}.
 * <p>
 * {@link FieldBuilder} currently only read fields of Java model class.
 * If passed a custom {@link ObjectSerializer} in API, please remember the helper class is only able to read the rename
 * annotation on the field instead of getter/setter methods.
 */
public final class FieldBuilder {
    private static final int MAX_DEPTH = 10000;
    private static final Map<Class<?>, SearchFieldDataType> SUPPORTED_NONE_PARAMETERIZED_TYPE = new HashMap<>();

    static {
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Integer.class, SearchFieldDataType.INT32);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(int.class, SearchFieldDataType.INT32);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Long.class, SearchFieldDataType.INT64);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(long.class, SearchFieldDataType.INT64);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Double.class, SearchFieldDataType.DOUBLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(double.class, SearchFieldDataType.DOUBLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Boolean.class, SearchFieldDataType.BOOLEAN);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(boolean.class, SearchFieldDataType.BOOLEAN);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(String.class, SearchFieldDataType.STRING);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(CharSequence.class, SearchFieldDataType.STRING);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Character.class, SearchFieldDataType.STRING);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(char.class, SearchFieldDataType.STRING);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Date.class, SearchFieldDataType.DATE_TIME_OFFSET);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(OffsetDateTime.class, SearchFieldDataType.DATE_TIME_OFFSET);
        //SUPPORTED_NONE_PARAMETERIZED_TYPE.put(PointGeometry.class, SearchFieldDataType.GEOGRAPHY_POINT);
    }

    private static final List<Class<?>> UNSUPPORTED_TYPES = Arrays.asList(byte.class, Byte.class,
        Float.class, float.class,
        Short.class, short.class);

    /**
     * Creates a collection of {@link SearchField} objects corresponding to the properties of the type supplied.
     *
     * @param modelClass The class for which fields will be created, based on its properties.
     * @param serializer Optional serializer which allow to use customized serializer library. Default to take Jackson
     * serialization.
     * @param <T> The generic type of the model class.
     * @return A collection of fields.
     */
    public static <T> List<SearchField> build(Class<T> modelClass, MemberNameConverter serializer) {
        ClientLogger logger = new ClientLogger(FieldBuilder.class);
        return build(modelClass, new Stack<>(), serializer, logger);
    }

    /**
     * Recursive class to build complex data type.
     *
     * @param currentClass Current class to be built.
     * @param classChain A class chain from {@code modelClass} to prior of {@code currentClass}.
     * @param logger {@link ClientLogger}.
     * @return A list of {@link SearchField} that currentClass is built to.
     */
    private static List<SearchField> build(Class<?> currentClass, Stack<Class<?>> classChain,
        MemberNameConverter serializer, ClientLogger logger) {
        if (classChain.contains(currentClass)) {
            logger.warning(String.format("There is circular dependencies %s, %s", classChain, currentClass));
            return null;
        }
        if (classChain.size() > MAX_DEPTH) {
            throw logger.logExceptionAsError(new RuntimeException(
                "The dependency graph is too deep. Please review your schema."));
        }
        classChain.push(currentClass);
        List<SearchField> searchFields = Arrays.stream(currentClass.getDeclaredFields())
            .filter(classField -> !classField.isAnnotationPresent(FieldIgnore.class))
            .map(classField -> buildField(classField, classChain, serializer, logger))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        classChain.pop();
        return searchFields;
    }

    private static SearchField buildField(Field classField, Stack<Class<?>> classChain,
        MemberNameConverter serializer, ClientLogger logger) {
        String fieldName = serializer.convertMemberName(classField);
        if (fieldName == null) {
            return null;
        }
        Type type = classField.getGenericType();

        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return buildNoneParameterizedType(fieldName, classField, logger);
        }
        if (isArrayOrList(type)) {
            return buildCollectionField(fieldName, classField, classChain, serializer, logger);
        }
        List<SearchField> childFields = build((Class<?>) type, classChain, serializer, logger);
        SearchField searchField = convertToBasicSearchField(fieldName, classField, logger);
        if (searchField == null) {
            return null;
        }
        searchField.setFields(childFields);
        return searchField;
    }

    private static SearchField buildNoneParameterizedType(String fieldName, Field classField,
        ClientLogger logger) {
        SearchField searchField = convertToBasicSearchField(fieldName, classField, logger);
        if (searchField == null) {
            return null;
        }
        return enrichWithAnnotation(searchField, classField, logger);
    }


    private static boolean isArrayOrList(Type type) {
        return isList(type) || ((Class) type).isArray();
    }

    private static boolean isList(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        Type rawType = ((ParameterizedType) type).getRawType();

        return List.class.isAssignableFrom((Class<?>) rawType);
    }

    private static SearchField buildCollectionField(String fieldName, Field classField,
        Stack<Class<?>> classChain, MemberNameConverter serializer, ClientLogger logger) {
        Type componentOrElementType = getComponentOrElementType(classField.getGenericType(), logger);

        validateType(componentOrElementType, true, logger);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(componentOrElementType)) {
            SearchField searchField = convertToBasicSearchField(fieldName, classField, logger);
            if (searchField == null) {
                return null;
            }
            return enrichWithAnnotation(searchField, classField, logger);
        }
        List<SearchField> childFields = build((Class<?>) componentOrElementType, classChain, serializer, logger);
        SearchField searchField = convertToBasicSearchField(fieldName, classField, logger);
        if (searchField == null) {
            return null;
        }
        searchField.setFields(childFields);
        return searchField;
    }

    private static Type getComponentOrElementType(Type arrayOrListType, ClientLogger logger) {
        if (isList(arrayOrListType)) {
            ParameterizedType pt = (ParameterizedType) arrayOrListType;
            return pt.getActualTypeArguments()[0];
        }
        if (((Class) arrayOrListType).isArray()) {
            return ((Class) arrayOrListType).getComponentType();
        }
        throw logger.logExceptionAsError(new RuntimeException(String.format(
            "Collection type %s is not supported.", arrayOrListType.getTypeName())));
    }

    private static SearchField convertToBasicSearchField(String fieldName, Field classField, ClientLogger logger) {

        SearchFieldDataType dataType = covertToSearchFieldDataType(classField.getGenericType(), false, logger);
        if (dataType == null) {
            return null;
        }

        SearchField searchField = new SearchField(fieldName, dataType);
        return searchField;
    }

    private static SearchField enrichWithAnnotation(SearchField searchField, java.lang.reflect.Field classField,
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
            if (!searchField.getType().equals(SearchFieldDataType.STRING)
                && !searchField.getType().equals(SearchFieldDataType.collection(SearchFieldDataType.STRING))) {
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
            String analyzer = searchableFieldPropertyAnnotation.analyzerName();
            String searchAnalyzer = searchableFieldPropertyAnnotation.searchAnalyzerName();
            String indexAnalyzer = searchableFieldPropertyAnnotation.indexAnalyzerName();
            if (!analyzer.isEmpty() && (!searchAnalyzer.isEmpty() || !indexAnalyzer.isEmpty())) {
                throw logger.logExceptionAsError(new RuntimeException(
                    "Please specify either analyzer or both searchAnalyzer and indexAnalyzer."));
            }
            if (!searchableFieldPropertyAnnotation.analyzerName().isEmpty()) {
                searchField.setAnalyzerName(LexicalAnalyzerName.fromString(
                    searchableFieldPropertyAnnotation.analyzerName()));
            }
            if (!searchableFieldPropertyAnnotation.searchAnalyzerName().isEmpty()) {
                searchField.setAnalyzerName(LexicalAnalyzerName.fromString(
                    searchableFieldPropertyAnnotation.searchAnalyzerName()));
            }
            if (!searchableFieldPropertyAnnotation.indexAnalyzerName().isEmpty()) {
                searchField.setAnalyzerName(LexicalAnalyzerName.fromString(
                    searchableFieldPropertyAnnotation.indexAnalyzerName()));
            }
            if (searchableFieldPropertyAnnotation.synonymMapNames().length != 0) {
                List<String> synonymMaps = Arrays.stream(searchableFieldPropertyAnnotation.synonymMapNames())
                    .filter(synonym -> !synonym.trim().isEmpty()).collect(Collectors.toList());
                searchField.setSynonymMapNames(synonymMaps);
            }
        }
        return searchField;
    }

    private static void validateType(Type type, boolean hasArrayOrCollectionWrapped, ClientLogger logger) {
        if (!(type instanceof ParameterizedType)) {
            if (UNSUPPORTED_TYPES.contains(type)) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    String.format("Type '%s' is not supported. "
                        + "Please use @FieldIgnore to exclude the field "
                        + "and manually build SearchField to the list if the field is needed. %n"
                        + "For more info, refer to link: aka.ms/azsdk/java/search/fieldbuilder",
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

    private static SearchFieldDataType covertToSearchFieldDataType(Type type, boolean hasArrayOrCollectionWrapped,
        ClientLogger logger) {
        validateType(type, hasArrayOrCollectionWrapped, logger);

        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return SUPPORTED_NONE_PARAMETERIZED_TYPE.get(type);
        }
        if (isArrayOrList(type)) {
            Type componentOrElementType = getComponentOrElementType(type, logger);
            return SearchFieldDataType.collection(covertToSearchFieldDataType(componentOrElementType, true, logger));
        }
        return SearchFieldDataType.COMPLEX;
    }
}
