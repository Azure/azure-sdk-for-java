// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.MemberNameConverterProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.search.documents.indexes.FieldIgnore;
import com.azure.search.documents.indexes.SearchableFieldProperty;
import com.azure.search.documents.indexes.SimpleFieldProperty;
import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import reactor.util.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Helper to convert model class to Search {@link SearchField fields}.
 * <p>
 * {@link FieldBuilder} currently only read fields of Java model class. If passed a custom {@link ObjectSerializer} in
 * API, please remember the helper class is only able to read the rename annotation on the field instead of
 * getter/setter methods.
 */
public final class FieldBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(FieldBuilder.class);

    private static final int MAX_DEPTH = 10000;
    private static final Map<Type, SearchFieldDataType> SUPPORTED_NONE_PARAMETERIZED_TYPE = new HashMap<>();
    private static final Set<Type> UNSUPPORTED_TYPES = new HashSet<>();

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

        UNSUPPORTED_TYPES.add(byte.class);
        UNSUPPORTED_TYPES.add(Byte.class);
        UNSUPPORTED_TYPES.add(float.class);
        UNSUPPORTED_TYPES.add(Float.class);
        UNSUPPORTED_TYPES.add(short.class);
        UNSUPPORTED_TYPES.add(Short.class);
    }

    /**
     * Creates a collection of {@link SearchField} objects corresponding to the properties of the type supplied.
     *
     * @param modelClass The class for which fields will be created, based on its properties.
     * @param options Configuration used to determine generation of the {@link SearchField SearchFields}.
     * @param <T> The generic type of the model class.
     * @return A collection of fields.
     */
    public static <T> List<SearchField> build(Class<T> modelClass, FieldBuilderOptions options) {
        MemberNameConverter converter;
        if (options == null || options.getJsonSerializer() == null) {
            converter = MemberNameConverterProviders.createInstance();
        } else if (!(options.getJsonSerializer() instanceof MemberNameConverter)) {
            converter = MemberNameConverterProviders.createInstance();
        } else {
            converter = (MemberNameConverter) options.getJsonSerializer();
        }

        return build(modelClass, new Stack<>(), converter);
    }

    /**
     * Recursive class to build complex data type.
     *
     * @param currentClass Current class to be built.
     * @param classChain A class chain from {@code modelClass} to prior of {@code currentClass}.
     * @return A list of {@link SearchField} that currentClass is built to.
     */
    private static List<SearchField> build(Class<?> currentClass, Stack<Class<?>> classChain,
        MemberNameConverter serializer) {
        if (classChain.contains(currentClass)) {
            LOGGER.warning(String.format("There is circular dependencies %s, %s", classChain, currentClass));
            return null;
        }
        if (classChain.size() > MAX_DEPTH) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "The dependency graph is too deep. Please review your schema."));
        }
        classChain.push(currentClass);
        List<SearchField> searchFields = Arrays.stream(currentClass.getDeclaredFields())
            .filter(classField -> !classField.isAnnotationPresent(FieldIgnore.class))
            .map(classField -> buildField(classField, classChain, serializer))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        classChain.pop();
        return searchFields;
    }

    private static SearchField buildField(Field classField, Stack<Class<?>> classChain,
        MemberNameConverter serializer) {
        String fieldName = serializer.convertMemberName(classField);
        if (fieldName == null) {
            return null;
        }
        Type type = classField.getGenericType();

        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return buildNoneParameterizedType(fieldName, classField);
        }
        if (isArrayOrList(type)) {
            return buildCollectionField(fieldName, classField, classChain, serializer);
        }

        return getSearchField(classField, classChain, serializer, fieldName, (Class<?>) type);
    }

    @Nullable
    private static SearchField getSearchField(Field classField, Stack<Class<?>> classChain,
        MemberNameConverter serializer, String fieldName, Class<?> type) {
        List<SearchField> childFields = build(type, classChain, serializer);

        SearchField searchField = convertToBasicSearchField(fieldName, classField);
        if (searchField == null) {
            return null;
        }

        return searchField.setFields(childFields);
    }

    private static SearchField buildNoneParameterizedType(String fieldName, Field classField) {
        SearchField searchField = convertToBasicSearchField(fieldName, classField);
        if (searchField == null) {
            return null;
        }
        return enrichWithAnnotation(searchField, classField);
    }


    private static boolean isArrayOrList(Type type) {
        return isList(type) || ((Class<?>) type).isArray();
    }

    private static boolean isList(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        Type rawType = ((ParameterizedType) type).getRawType();

        return List.class.isAssignableFrom((Class<?>) rawType);
    }

    private static SearchField buildCollectionField(String fieldName, Field classField,
        Stack<Class<?>> classChain, MemberNameConverter serializer) {
        Type componentOrElementType = getComponentOrElementType(classField.getGenericType());

        validateType(componentOrElementType, true);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(componentOrElementType)) {
            SearchField searchField = convertToBasicSearchField(fieldName, classField);
            if (searchField == null) {
                return null;
            }
            return enrichWithAnnotation(searchField, classField);
        }
        return getSearchField(classField, classChain, serializer, fieldName, (Class<?>) componentOrElementType);
    }

    private static Type getComponentOrElementType(Type arrayOrListType) {
        if (isList(arrayOrListType)) {
            ParameterizedType pt = (ParameterizedType) arrayOrListType;
            return pt.getActualTypeArguments()[0];
        }

        if (((Class<?>) arrayOrListType).isArray()) {
            return ((Class<?>) arrayOrListType).getComponentType();
        }

        throw LOGGER.logExceptionAsError(new RuntimeException(String.format(
            "Collection type %s is not supported.", arrayOrListType.getTypeName())));
    }

    private static SearchField convertToBasicSearchField(String fieldName, Field classField) {

        SearchFieldDataType dataType = covertToSearchFieldDataType(classField.getGenericType(), false);
        if (dataType == null) {
            return null;
        }

        return new SearchField(fieldName, dataType);
    }

    private static SearchField enrichWithAnnotation(SearchField searchField, java.lang.reflect.Field classField) {
        if (classField.isAnnotationPresent(SimpleFieldProperty.class)
            && classField.isAnnotationPresent(SearchableFieldProperty.class)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
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
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format("SearchFieldProperty can only"
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
                throw LOGGER.logExceptionAsError(new RuntimeException(
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

    private static void validateType(Type type, boolean hasArrayOrCollectionWrapped) {
        if (!(type instanceof ParameterizedType)) {
            if (UNSUPPORTED_TYPES.contains(type)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    String.format("Type '%s' is not supported. "
                            + "Please use @FieldIgnore to exclude the field "
                            + "and manually build SearchField to the list if the field is needed. %n"
                            + "For more information, refer to link: aka.ms/azsdk/java/search/fieldbuilder",
                        type.getTypeName())));
            }
            return;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Map and its subclasses are not supported"));
        }

        if (hasArrayOrCollectionWrapped) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Only single-dimensional array is supported."));
        }

        if (!List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Collection type %s is not supported", type.getTypeName())));
        }
    }

    private static SearchFieldDataType covertToSearchFieldDataType(Type type, boolean hasArrayOrCollectionWrapped) {
        validateType(type, hasArrayOrCollectionWrapped);

        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return SUPPORTED_NONE_PARAMETERIZED_TYPE.get(type);
        }

        if (isArrayOrList(type)) {
            Type componentOrElementType = getComponentOrElementType(type);
            return SearchFieldDataType.collection(covertToSearchFieldDataType(componentOrElementType, true));
        }

        return SearchFieldDataType.COMPLEX;
    }
}
