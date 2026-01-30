// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.azure.core.models.GeoPoint;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.BasicField;
import com.azure.search.documents.indexes.ComplexField;
import com.azure.search.documents.indexes.models.*;

import java.lang.reflect.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper to convert model class to {@link SearchField SearchFields}.
 * <p>
 * {@link FieldBuilder} only inspects {@link Field fields} and {@link Method methods} declared by the model, and uses
 * the following rules for creating {@link SearchField SearchFields}:
 * <ul>
 *     <li>If the field or method is annotated with {@link BasicField} the {@link SearchFieldDataType} inferred by the
 *     type of the field or return type of the method cannot be {@link SearchFieldDataType#COMPLEX}. It may be a
 *     {@link SearchFieldDataType#collection(SearchFieldDataType)} though.</li>
 *     <li>If the field or method is annotated with {@link ComplexField} the {@link SearchFieldDataType} inferred by the
 *     type of the field or return type of the method must be {@link SearchFieldDataType#COMPLEX}. It may be a
 *     {@link SearchFieldDataType#collection(SearchFieldDataType)} of {@link SearchFieldDataType#COMPLEX}.</li>
 *     <li>If the field or method isn't annotated with either {@link BasicField} or {@link ComplexField} it will be
 *     ignored.</li>
 * </ul>
 * <p>
 * If the type of the field or return type of the method is an array or {@link Iterable} it will be considered a
 * {@link SearchFieldDataType#collection(SearchFieldDataType)} type. Nested
 * {@link SearchFieldDataType#collection(SearchFieldDataType)} aren't allowed and will throw an exception, ex.
 * {@code String[][]} or {@code List<List<String>>}.
 *
 * <table border="1">
 *     <caption>Conversion of Java type to {@link SearchFieldDataType}</caption>
 *     <thead>
 *         <tr><th>Java type</th><th>{@link SearchFieldDataType}</th></tr>
 *     </thead>
 *     <tbody>
 *         <tr><td>{@code byte}</td><td>{@link SearchFieldDataType#SBYTE}</td></tr>
 *         <tr><td>{@link Byte}</td><td>{@link SearchFieldDataType#SBYTE}</td></tr>
 *         <tr><td>{@code boolean}</td><td>{@link SearchFieldDataType#BOOLEAN}</td></tr>
 *         <tr><td>{@link Boolean}</td><td>{@link SearchFieldDataType#BOOLEAN}</td></tr>
 *         <tr><td>{@code short}</td><td>{@link SearchFieldDataType#INT16}</td></tr>
 *         <tr><td>{@link Short}</td><td>{@link SearchFieldDataType#INT16}</td></tr>
 *         <tr><td>{@code int}</td><td>{@link SearchFieldDataType#INT32}</td></tr>
 *         <tr><td>{@link Integer}</td><td>{@link SearchFieldDataType#INT32}</td></tr>
 *         <tr><td>{@code long}</td><td>{@link SearchFieldDataType#INT64}</td></tr>
 *         <tr><td>{@link Long}</td><td>{@link SearchFieldDataType#INT64}</td></tr>
 *         <tr><td>{@code float}</td><td>{@link SearchFieldDataType#SINGLE}</td></tr>
 *         <tr><td>{@link Float}</td><td>{@link SearchFieldDataType#SINGLE}</td></tr>
 *         <tr><td>{@code double}</td><td>{@link SearchFieldDataType#DOUBLE}</td></tr>
 *         <tr><td>{@link Double}</td><td>{@link SearchFieldDataType#DOUBLE}</td></tr>
 *         <tr><td>{@code char}</td><td>{@link SearchFieldDataType#STRING}</td></tr>
 *         <tr><td>{@link Character}</td><td>{@link SearchFieldDataType#STRING}</td></tr>
 *         <tr><td>{@link CharSequence}</td><td>{@link SearchFieldDataType#STRING}</td></tr>
 *         <tr><td>{@link String}</td><td>{@link SearchFieldDataType#STRING}</td></tr>
 *         <tr><td>{@link Date}</td><td>{@link SearchFieldDataType#DATE_TIME_OFFSET}</td></tr>
 *         <tr><td>{@link OffsetDateTime}</td><td>{@link SearchFieldDataType#DATE_TIME_OFFSET}</td></tr>
 *         <tr><td>{@link GeoPoint}</td><td>{@link SearchFieldDataType#GEOGRAPHY_POINT}</td></tr>
 *         <tr><td>Any other type</td><td>Attempted to be consumed as {@link SearchFieldDataType#COMPLEX}</td></tr>
 *     </tbody>
 * </table>
 * <p>
 * {@link SearchFieldDataType#HALF} and {@link SearchFieldDataType#BYTE} aren't supported by {@link Field} given there
 * isn't a built-in Java type that represents them.
 * <p>
 * When generating {@link SearchField SearchFields} there is a maximum class depth limit of {@code 1000} before an
 * exception will be thrown.
 */
public final class FieldBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(FieldBuilder.class);

    private static final int MAX_DEPTH = 1000;
    private static final Map<Type, SearchFieldDataType> SUPPORTED_NONE_PARAMETERIZED_TYPE = new HashMap<>();

    private static final SearchFieldDataType COLLECTION_STRING
        = SearchFieldDataType.collection(SearchFieldDataType.STRING);
    private static final SearchFieldDataType COLLECTION_SINGLE
        = SearchFieldDataType.collection(SearchFieldDataType.SINGLE);

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
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Float.class, SearchFieldDataType.SINGLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(float.class, SearchFieldDataType.SINGLE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(byte.class, SearchFieldDataType.SBYTE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Byte.class, SearchFieldDataType.SBYTE);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(short.class, SearchFieldDataType.INT16);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Short.class, SearchFieldDataType.INT16);
        //noinspection UseOfObsoleteDateTimeApi
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(Date.class, SearchFieldDataType.DATE_TIME_OFFSET);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(OffsetDateTime.class, SearchFieldDataType.DATE_TIME_OFFSET);
        SUPPORTED_NONE_PARAMETERIZED_TYPE.put(GeoPoint.class, SearchFieldDataType.GEOGRAPHY_POINT);
    }

    /**
     * Creates a collection of {@link SearchField} objects corresponding to the properties of the type supplied.
     *
     * @param modelClass The class for which fields will be created, based on its properties.
     * @return A collection of fields.
     */
    public static List<SearchField> build(Class<?> modelClass) {
        return build(modelClass, new Stack<>());
    }

    /**
     * Recursive class to build complex data type.
     *
     * @param currentClass Current class to be built.
     * @param classChain   A class chain from {@code modelClass} to prior of {@code currentClass}.
     * @return A list of {@link SearchField} that currentClass is built to.
     */
    private static List<SearchField> build(Class<?> currentClass, Stack<Class<?>> classChain) {
        if (classChain.contains(currentClass)) {
            LOGGER.warning("There is circular dependencies {}, {}", classChain, currentClass);
            return null;
        }

        if (classChain.size() > MAX_DEPTH) {
            throw LOGGER.atError()
                .log(new IllegalStateException("The dependency graph is too deep. Please review your schema."));
        }

        classChain.push(currentClass);
        List<SearchField> searchFields = new ArrayList<>();
        for (Field field : currentClass.getDeclaredFields()) {
            SearchField searchField = createSearchField(field, Field::getGenericType, classChain);
            if (searchField != null) {
                searchFields.add(searchField);
            }
        }
        for (Method method : currentClass.getDeclaredMethods()) {
            SearchField searchField = createSearchField(method, Method::getGenericReturnType, classChain);
            if (searchField != null) {
                searchFields.add(searchField);
            }
        }
        classChain.pop();
        return searchFields;
    }

    private static <T extends AccessibleObject & Member> SearchField createSearchField(T fieldOrMethod,
        Function<T, Type> typeGetter, Stack<Class<?>> stack) {
        BasicField basicField = fieldOrMethod.getAnnotation(BasicField.class);
        ComplexField complexField = fieldOrMethod.getAnnotation(ComplexField.class);

        if (basicField != null && complexField != null) {
            throw LOGGER.atError()
                .addKeyValue("fieldOrMethodName", fieldOrMethod.getName())
                .addKeyValue("fieldOrMemberDeclaringClass", fieldOrMethod.getDeclaringClass())
                .log(new IllegalStateException("Field or method may only be annotated with one of 'BasicField', "
                    + "or 'ComplexField' at a time."));
        } else if (basicField != null) {
            return generateBasicField(typeGetter.apply(fieldOrMethod), basicField, fieldOrMethod);
        } else if (complexField != null) {
            return generateComplexField(typeGetter.apply(fieldOrMethod), complexField, fieldOrMethod, stack);
        }

        return null;
    }

    private static SearchField generateBasicField(Type type, BasicField basicField, Member member) {
        SearchFieldDataType basicDataType = SUPPORTED_NONE_PARAMETERIZED_TYPE.get(type);
        if (basicDataType != null) {
            SearchField searchField = new SearchField(basicField.name(), basicDataType);
            return enrichBasicSearchField(searchField, basicField, member);
        }

        Type collectionType = getCollectionType(type, member);
        if (collectionType != null) {
            SearchFieldDataType collectionDataType = SUPPORTED_NONE_PARAMETERIZED_TYPE.get(collectionType);
            if (collectionDataType != null) {
                SearchField searchField
                    = new SearchField(basicField.name(), SearchFieldDataType.collection(collectionDataType));
                return enrichBasicSearchField(searchField, basicField, member);
            }
        }

        throw LOGGER.atError()
            .addKeyValue("fieldOrMethodName", member.getName())
            .addKeyValue("fieldOrMemberDeclaringClass", member.getDeclaringClass())
            .log(new IllegalStateException(
                "'BasicField' cannot be used on fields or methods which result in 'SearchFieldDataType.COMPLEX'."));
    }

    private static SearchField generateComplexField(Type type, ComplexField complexField, Member member,
        Stack<Class<?>> stack) {
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            throw LOGGER.atError()
                .addKeyValue("fieldOrMethodName", member.getName())
                .addKeyValue("fieldOrMemberDeclaringClass", member.getDeclaringClass())
                .log(new IllegalStateException("'ComplexField' cannot be used on fields or methods which don't result "
                    + "in 'SearchFieldDataType.COMPLEX'."));
        }

        Type collectionType = getCollectionType(type, member);
        if (collectionType != null) {
            if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(collectionType)) {
                throw LOGGER.atError()
                    .addKeyValue("fieldOrMethodName", member.getName())
                    .addKeyValue("fieldOrMemberDeclaringClass", member.getDeclaringClass())
                    .log(new IllegalStateException("'ComplexField' cannot be used on fields or methods which don't "
                        + "result in 'SearchFieldDataType.COMPLEX'."));
            }

            return new SearchField(complexField.name(), SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                .setFields(build((Class<?>) collectionType, stack));
        }

        return new SearchField(complexField.name(), SearchFieldDataType.COMPLEX)
            .setFields(build((Class<?>) type, stack));
    }

    private static Type getCollectionType(Type type, Member member) {
        Type collectionType = null;
        if (isArrayOrIterable(type)) {
            collectionType = getComponentOrElementType(type);
        }

        if (collectionType != null && isArrayOrIterable(collectionType)) {
            throw LOGGER.atError()
                .addKeyValue("fieldOrMethodName", member.getName())
                .addKeyValue("fieldOrMemberDeclaringClass", member.getDeclaringClass())
                .log(new IllegalStateException("Field or method annotated with 'BasicField' or 'ComplexField' cannot "
                    + "be a nested array or Iterable."));
        }

        return collectionType;
    }

    private static boolean isArrayOrIterable(Type type) {
        return isList(type) || ((Class<?>) type).isArray();
    }

    private static boolean isList(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        Type rawType = ((ParameterizedType) type).getRawType();

        return Iterable.class.isAssignableFrom((Class<?>) rawType);
    }

    private static Type getComponentOrElementType(Type arrayOrListType) {
        if (isList(arrayOrListType)) {
            ParameterizedType pt = (ParameterizedType) arrayOrListType;
            return pt.getActualTypeArguments()[0];
        }

        if (((Class<?>) arrayOrListType).isArray()) {
            return ((Class<?>) arrayOrListType).getComponentType();
        }

        throw LOGGER
            .logExceptionAsError(new RuntimeException("Collection type '" + arrayOrListType + "' is not supported."));
    }

    private static SearchField enrichBasicSearchField(SearchField searchField, BasicField basicField, Member member) {
        searchField.setKey(toBoolean(basicField.isKey()))
            .setHidden(toBoolean(basicField.isHidden()))
            .setRetrievable(toBoolean(basicField.isRetrievable()))
            .setStored(toBoolean(basicField.isStored()))
            .setSearchable(toBoolean(basicField.isSearchable()))
            .setFilterable(toBoolean(basicField.isFilterable()))
            .setSortable(toBoolean(basicField.isSortable()))
            .setFacetable(toBoolean(basicField.isFacetable()))
            .setPermissionFilter(nullOrT(basicField.permissionFilter(), PermissionFilter::fromString))
            .setSensitivityLabel(toBoolean(basicField.isSensitivityLabel()))
            .setAnalyzerName(nullOrT(basicField.analyzerName(), LexicalAnalyzerName::fromString))
            .setSearchAnalyzerName(nullOrT(basicField.searchAnalyzerName(), LexicalAnalyzerName::fromString))
            .setIndexAnalyzerName(nullOrT(basicField.indexAnalyzerName(), LexicalAnalyzerName::fromString))
            .setNormalizerName(nullOrT(basicField.normalizerName(), LexicalNormalizerName::fromString))
            .setVectorSearchDimensions(
                basicField.vectorSearchDimensions() > 0 ? basicField.vectorSearchDimensions() : null)
            .setVectorSearchProfileName(nullOrT(basicField.vectorSearchProfileName(), Function.identity()))
            .setVectorEncodingFormat(nullOrT(basicField.vectorEncodingFormat(), VectorEncodingFormat::fromString))
            .setSynonymMapNames(synonymMapNames(basicField.synonymMapNames()));

        StringBuilder errorMessage = new StringBuilder();
        boolean isStringOrCollectionString
            = searchField.getType() == SearchFieldDataType.STRING || searchField.getType() == COLLECTION_STRING;
        boolean isSearchableType = isStringOrCollectionString || searchField.getType() == COLLECTION_SINGLE;
        boolean hasAnalyzerName = searchField.getAnalyzerName() != null;
        boolean hasSearchAnalyzerName = searchField.getSearchAnalyzerName() != null;
        boolean hasIndexAnalyzerName = searchField.getIndexAnalyzerName() != null;
        if (searchField.isHidden() != null
            && searchField.isRetrievable() != null
            && searchField.isHidden() == searchField.isRetrievable()) {
            errorMessage.append("'isHidden' and 'isRetrievable' were set to the same boolean value, this is "
                + "invalid as they have opposite meanings and therefore the configuration is ambiguous.");
        }
        if (Boolean.TRUE.equals(searchField.isSearchable())) {
            if (!isSearchableType) {
                errorMessage
                    .append("SearchField can only be used on 'Edm.String', 'Collection(Edm.String)', "
                        + "or 'Collection(Edm.Single)' types. Property '")
                    .append(member.getName())
                    .append("' returns a '")
                    .append(searchField.getType())
                    .append("' value. ");
            }

            // Searchable fields are allowed to have either no analyzer names configure or one of the following
            // analyzerName is set and searchAnalyzerName and indexAnalyzerName are not set
            // searchAnalyzerName and indexAnalyzerName are set and analyzerName is not set
            if ((!hasAnalyzerName && (hasSearchAnalyzerName != hasIndexAnalyzerName))
                || (hasAnalyzerName && (hasSearchAnalyzerName || hasIndexAnalyzerName))) {
                errorMessage.append("Please specify either analyzer or both searchAnalyzer and indexAnalyzer. ");
            }
        }

        // Any field is allowed to have a normalizer, but it must be either a STRING or Collection(STRING) and have one
        // of filterable, sortable, or facetable set to true.
        if (searchField.getNormalizerName() != null
            && (!isStringOrCollectionString
                || !(Boolean.TRUE.equals(searchField.isFilterable())
                    || Boolean.TRUE.equals(searchField.isSortable())
                    || Boolean.TRUE.equals(searchField.isFacetable())))) {
            errorMessage.append("A field with a normalizer name can only be used on string properties and must have ")
                .append("one of filterable, sortable, or facetable set to true. ");
        }

        if ((searchField.getVectorSearchDimensions() != null && searchField.getVectorSearchProfileName() == null)
            || (searchField.getVectorSearchDimensions() == null && searchField.getVectorSearchProfileName() != null)) {
            errorMessage
                .append("Please specify both vectorSearchDimensions and vectorSearchProfileName for vector field. ");
        }

        if (errorMessage.length() > 0) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(errorMessage.toString()));
        }

        return searchField;
    }

    private static Boolean toBoolean(BasicField.BooleanHelper booleanHelper) {
        if (booleanHelper == null || booleanHelper == BasicField.BooleanHelper.NULL) {
            return null;
        } else {
            return booleanHelper == BasicField.BooleanHelper.TRUE;
        }
    }

    private static <T> T nullOrT(String raw, Function<String, T> converter) {
        return CoreUtils.isNullOrEmpty(raw) ? null : converter.apply(raw);
    }

    private static List<String> synonymMapNames(String[] synonyms) {
        if (CoreUtils.isNullOrEmpty(synonyms)) {
            return null;
        }
        return Arrays.stream(synonyms).filter(synonym -> !synonym.trim().isEmpty()).collect(Collectors.toList());
    }
}
