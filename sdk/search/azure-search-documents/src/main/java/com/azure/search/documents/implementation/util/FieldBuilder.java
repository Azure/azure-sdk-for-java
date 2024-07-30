// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.models.GeoPoint;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.MemberNameConverterProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.search.documents.indexes.FieldBuilderIgnore;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.VectorEncodingFormat;
import reactor.util.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
import java.util.stream.Stream;

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
    private static final Set<SearchFieldDataType> UNSUPPORTED_SERVICE_TYPES = new HashSet<>();

    private static final SearchFieldDataType COLLECTION_STRING = SearchFieldDataType.collection(
        SearchFieldDataType.STRING);
    private static final SearchFieldDataType COLLECTION_SINGLE = SearchFieldDataType.collection(
        SearchFieldDataType.SINGLE);

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
        //noinspection UseOfObsoleteDateTimeApi
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Date.class, SearchFieldDataType.DATE_TIME_OFFSET);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(OffsetDateTime.class, SearchFieldDataType.DATE_TIME_OFFSET);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(GeoPoint.class, SearchFieldDataType.GEOGRAPHY_POINT);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Float.class, SearchFieldDataType.SINGLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(float.class, SearchFieldDataType.SINGLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(byte.class, SearchFieldDataType.SBYTE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Byte.class, SearchFieldDataType.SBYTE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(short.class, SearchFieldDataType.INT16);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Short.class, SearchFieldDataType.INT16);
        UNSUPPORTED_SERVICE_TYPES.add(SearchFieldDataType.BYTE);
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
        } else {
            JsonSerializer serializer = options.getJsonSerializer();
            if (serializer instanceof MemberNameConverter) {
                converter = (MemberNameConverter) serializer;
            } else {
                converter = MemberNameConverterProviders.createInstance();
            }
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
            LOGGER.warning("There is circular dependencies {}, {}", classChain, currentClass);
            return null;
        }

        if (classChain.size() > MAX_DEPTH) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("The dependency graph is too deep. Please review your schema."));
        }

        classChain.push(currentClass);
        List<SearchField> searchFields = getDeclaredFieldsAndMethods(currentClass).filter(
                FieldBuilder::fieldOrMethodIgnored)
            .map(classField -> buildSearchField(classField, classChain, serializer))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        classChain.pop();
        return searchFields;
    }

    /*
     * Retrieves all declared fields and methods from the passed Class.
     */
    private static Stream<Member> getDeclaredFieldsAndMethods(Class<?> model) {
        List<Member> fieldsAndMethods = new ArrayList<>(Arrays.asList(model.getDeclaredFields()));
        fieldsAndMethods.addAll(Arrays.asList(model.getDeclaredMethods()));

        return fieldsAndMethods.stream();
    }

    /*
     * Indicates if the Member, should be a Field or Method, is annotated with FieldBuilderIgnore indicating that it
     * shouldn't have a SearchField created for it.
     */
    private static boolean fieldOrMethodIgnored(Member member) {
        if (member instanceof Field) {
            return !((Field) member).isAnnotationPresent(FieldBuilderIgnore.class);
        } else if (member instanceof Method) {
            return !((Method) member).isAnnotationPresent(FieldBuilderIgnore.class);
        } else {
            return false;
        }
    }

    private static SearchField buildSearchField(Member member, Stack<Class<?>> classChain,
        MemberNameConverter serializer) {
        String fieldName = serializer.convertMemberName(member);
        if (fieldName == null) {
            return null;
        }

        Type type = getFieldOrMethodReturnType(member);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return buildNoneParameterizedType(fieldName, member, type);
        }

        if (isArrayOrList(type)) {
            return buildCollectionField(fieldName, member, type, classChain, serializer);
        }

        return getSearchField(type, classChain, serializer, fieldName, (Class<?>) type);
    }

    private static Type getFieldOrMethodReturnType(Member member) {
        if (member instanceof Field) {
            return ((Field) member).getGenericType();
        } else if (member instanceof Method) {
            return ((Method) member).getGenericReturnType();
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Member isn't instance of Field or Method."));
        }
    }

    @Nullable
    private static SearchField getSearchField(Type type, Stack<Class<?>> classChain, MemberNameConverter serializer,
        String fieldName, Class<?> clazz) {
        SearchField searchField = convertToBasicSearchField(fieldName, type);
        if (searchField == null) {
            return null;
        }

        return searchField.setFields(build(clazz, classChain, serializer));
    }

    private static SearchField buildNoneParameterizedType(String fieldName, Member member, Type type) {
        SearchField searchField = convertToBasicSearchField(fieldName, type);

        return (searchField == null) ? null : enrichWithAnnotation(searchField, member);
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

    private static SearchField buildCollectionField(String fieldName, Member member, Type type,
        Stack<Class<?>> classChain, MemberNameConverter serializer) {
        Type componentOrElementType = getComponentOrElementType(type);

        validateType(componentOrElementType, true);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(componentOrElementType)) {
            SearchField searchField = convertToBasicSearchField(fieldName, type);
            if (searchField == null) {
                return null;
            }
            return enrichWithAnnotation(searchField, member);
        }
        return getSearchField(type, classChain, serializer, fieldName, (Class<?>) componentOrElementType);
    }

    private static Type getComponentOrElementType(Type arrayOrListType) {
        if (isList(arrayOrListType)) {
            ParameterizedType pt = (ParameterizedType) arrayOrListType;
            return pt.getActualTypeArguments()[0];
        }

        if (((Class<?>) arrayOrListType).isArray()) {
            return ((Class<?>) arrayOrListType).getComponentType();
        }

        throw LOGGER.logExceptionAsError(
            new RuntimeException("Collection type '" + arrayOrListType + "' is not supported."));
    }

    private static SearchField convertToBasicSearchField(String fieldName, Type type) {
        SearchFieldDataType dataType = covertToSearchFieldDataType(type, false);

        return (dataType == null) ? null : new SearchField(fieldName, dataType);
    }

    private static SearchField enrichWithAnnotation(SearchField searchField, Member member) {
        SimpleField simpleField = getDeclaredAnnotation(member, SimpleField.class);
        SearchableField searchableField = getDeclaredAnnotation(member, SearchableField.class);

        if (simpleField != null && searchableField != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "@SimpleField and @SearchableField cannot be present simultaneously for " + member.getName()));
        }

        if (simpleField == null && searchableField == null) {
            return searchField;
        }

        boolean key;
        boolean hidden;
        boolean filterable;
        boolean sortable;
        boolean facetable;
        boolean stored;
        boolean searchable = searchableField != null;
        String analyzerName = null;
        String searchAnalyzerName = null;
        String indexAnalyzerName = null;
        String[] synonymMapNames = null;
        Integer vectorSearchDimensions = null;
        String vectorSearchProfileName = null;
        String vectorEncodingFormat = null;

        if (simpleField != null) {
            key = simpleField.isKey();
            hidden = simpleField.isHidden();
            stored = true;
            filterable = simpleField.isFilterable();
            sortable = simpleField.isSortable();
            facetable = simpleField.isFacetable();
        } else {
            key = searchableField.isKey();
            hidden = searchableField.isHidden();
            stored = searchableField.isStored();
            filterable = searchableField.isFilterable();
            sortable = searchableField.isSortable();
            facetable = searchableField.isFacetable();
            analyzerName = searchableField.analyzerName();
            searchAnalyzerName = searchableField.searchAnalyzerName();
            indexAnalyzerName = searchableField.indexAnalyzerName();
            synonymMapNames = searchableField.synonymMapNames();
            vectorSearchDimensions = searchableField.vectorSearchDimensions() > 0
                ? searchableField.vectorSearchDimensions()
                : null;
            vectorSearchProfileName = CoreUtils.isNullOrEmpty(searchableField.vectorSearchProfileName())
                ? null
                : searchableField.vectorSearchProfileName();
            vectorEncodingFormat = CoreUtils.isNullOrEmpty(searchableField.vectorEncodingFormat())
                ? null
                : searchableField.vectorEncodingFormat();
        }

        StringBuilder errorMessage = new StringBuilder();
        boolean isStringOrCollectionString = searchField.getType() == SearchFieldDataType.STRING
            || searchField.getType() == COLLECTION_STRING;
        boolean isSearchableType = isStringOrCollectionString || searchField.getType() == COLLECTION_SINGLE;
        boolean hasAnalyzerName = !CoreUtils.isNullOrEmpty(analyzerName);
        boolean hasSearchAnalyzerName = !CoreUtils.isNullOrEmpty(searchAnalyzerName);
        boolean hasIndexAnalyzerName = !CoreUtils.isNullOrEmpty(indexAnalyzerName);
        boolean hasVectorEncodingFormat = !CoreUtils.isNullOrEmpty(vectorEncodingFormat);
        if (searchable) {
            if (!isSearchableType) {
                errorMessage.append("SearchField can only be used on 'Edm.String', 'Collection(Edm.String)', or "
                        + "'Collection(Edm.Single)' types. Property '")
                    .append(member.getName())
                    .append("' returns a '")
                    .append(searchField.getType())
                    .append("' value. ");
            }

            // Searchable fields are allowed to have either no analyzer names configure or one of the following
            // analyzerName is set and searchAnalyzerName and indexAnalyzerName are not set
            // searchAnalyzerName and indexAnalyzerName are set and analyzerName is not set
            if ((!hasAnalyzerName && (hasSearchAnalyzerName != hasIndexAnalyzerName)) || (hasAnalyzerName && (
                hasSearchAnalyzerName || hasIndexAnalyzerName))) {
                errorMessage.append("Please specify either analyzer or both searchAnalyzer and indexAnalyzer. ");
            }
        }

        if (searchField.getType() == COLLECTION_SINGLE && (vectorSearchDimensions == null
            || vectorSearchProfileName == null)) {
            errorMessage.append(
                "Please specify both vectorSearchDimensions and vectorSearchProfileName for Collection(Edm.Single) type. ");
        }

        if (errorMessage.length() > 0) {
            throw LOGGER.logExceptionAsError(new RuntimeException(errorMessage.toString()));
        }

        searchField.setKey(key)
            .setHidden(hidden)
            .setSearchable(searchable)
            .setFilterable(filterable)
            .setSortable(sortable)
            .setFacetable(facetable)
            .setStored(stored)
            .setVectorSearchDimensions(vectorSearchDimensions)
            .setVectorSearchProfileName(vectorSearchProfileName);

        if (hasAnalyzerName) {
            searchField.setAnalyzerName(LexicalAnalyzerName.fromString(analyzerName));
        } else if (hasSearchAnalyzerName || hasIndexAnalyzerName) {
            searchField.setSearchAnalyzerName(LexicalAnalyzerName.fromString(searchAnalyzerName));
            searchField.setIndexAnalyzerName(LexicalAnalyzerName.fromString(indexAnalyzerName));
        }

        if (hasVectorEncodingFormat) {
            searchField.setVectorEncodingFormat(VectorEncodingFormat.fromString(vectorEncodingFormat));
        }

        if (!CoreUtils.isNullOrEmpty(synonymMapNames)) {
            List<String> synonymMaps = Arrays.stream(searchableField.synonymMapNames())
                .filter(synonym -> !synonym.trim().isEmpty())
                .collect(Collectors.toList());
            searchField.setSynonymMapNames(synonymMaps);
        }

        return searchField;
    }

    private static <T extends Annotation> T getDeclaredAnnotation(Member member, Class<T> annotationType) {
        if (member instanceof Field) {
            return ((Field) member).getAnnotation(annotationType);
        } else if (member instanceof Method) {
            return ((Method) member).getAnnotation(annotationType);
        } else {
            return null;
        }
    }

    private static void validateType(Type type, boolean hasArrayOrCollectionWrapped) {
        if (!(type instanceof ParameterizedType)) {
            if (UNSUPPORTED_TYPES.contains(type)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Type '" + type + "' is not supported. Please use @FieldIgnore to exclude the field and manually "
                        + "build SearchField to the list if the field is needed. For more information, refer to link: "
                        + "aka.ms/azsdk/java/search/fieldbuilder"));
            }
            return;
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Map and its subclasses are not supported"));
        }

        if (hasArrayOrCollectionWrapped) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Only single-dimensional array is supported."));
        }

        if (!List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Collection type '" + type + "' is not supported"));
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
