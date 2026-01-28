// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.azure.core.models.GeoPoint;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.search.documents.indexes.ComplexField;
import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.LexicalNormalizerName;
import com.azure.search.documents.indexes.models.PermissionFilter;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.VectorEncodingFormat;

import java.lang.reflect.AnnotatedElement;
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
     * @return A collection of fields.
     */
    public static List<SearchField> build(Class<?> modelClass) {
        return build(modelClass, new Stack<>());
    }

    /**
     * Recursive class to build complex data type.
     *
     * @param currentClass Current class to be built.
     * @param classChain A class chain from {@code modelClass} to prior of {@code currentClass}.
     * @return A list of {@link SearchField} that currentClass is built to.
     */
    private static List<SearchField> build(Class<?> currentClass, Stack<Class<?>> classChain) {
        if (classChain.contains(currentClass)) {
            LOGGER.warning("There is circular dependencies {}, {}", classChain, currentClass);
            return null;
        }

        if (classChain.size() > MAX_DEPTH) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("The dependency graph is too deep. Please review your schema."));
        }

        classChain.push(currentClass);
        List<SearchField> searchFields
            = getDeclaredFieldsAndMethods(currentClass).map(classField -> buildSearchField(classField, classChain))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        classChain.pop();
        return searchFields;
    }

    /*
     * Retrieves all declared fields and methods from the passed Class.
     */
    private static Stream<Member> getDeclaredFieldsAndMethods(Class<?> model) {
        return Stream.concat(Arrays.stream(model.getDeclaredFields()), Arrays.stream(model.getDeclaredMethods()));
    }

    private static SearchField buildSearchField(Member member, Stack<Class<?>> classChain) {
        AnnotationData annotationData = AnnotationData.create(member);
        if (annotationData == null) {
            return null;
        }

        Type type = getFieldOrMethodReturnType(member);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(type)) {
            return buildNoneParameterizedType(annotationData.getFieldName(), annotationData, type);
        }

        if (isArrayOrList(type)) {
            return buildCollectionField(annotationData.getFieldName(), annotationData, type, classChain);
        }

        return getSearchField(type, classChain, annotationData.getFieldName(), (Class<?>) type);
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

    private static SearchField getSearchField(Type type, Stack<Class<?>> classChain, String fieldName, Class<?> clazz) {
        SearchField searchField = convertToBasicSearchField(fieldName, type);
        if (searchField == null) {
            return null;
        }

        return searchField.setFields(build(clazz, classChain));
    }

    private static SearchField buildNoneParameterizedType(String fieldName, AnnotationData annotationData, Type type) {
        SearchField searchField = convertToBasicSearchField(fieldName, type);

        return (searchField == null) ? null : annotationData.enrichWithAnnotation(searchField);
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

    private static SearchField buildCollectionField(String fieldName, AnnotationData annotationData, Type type,
        Stack<Class<?>> classChain) {
        Type componentOrElementType = getComponentOrElementType(type);

        validateType(componentOrElementType, true);
        if (SUPPORTED_NONE_PARAMETERIZED_TYPE.containsKey(componentOrElementType)) {
            SearchField searchField = convertToBasicSearchField(fieldName, type);
            if (searchField == null) {
                return null;
            }
            return annotationData.enrichWithAnnotation(searchField);
        }
        return getSearchField(type, classChain, fieldName, (Class<?>) componentOrElementType);
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

    private static SearchField convertToBasicSearchField(String fieldName, Type type) {
        SearchFieldDataType dataType = covertToSearchFieldDataType(type, false);

        return (dataType == null) ? null : new SearchField(fieldName, dataType);
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
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Only single-dimensional array is supported."));
        }

        if (!List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Collection type '" + type + "' is not supported"));
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

    private static final class AnnotationData {
        private final Member member;
        private final SimpleField simpleField;
        private final SearchableField searchableField;
        private final ComplexField complexField;

        private AnnotationData(Member member, SimpleField simpleField, SearchableField searchableField,
            ComplexField complexField) {
            this.member = member;
            this.simpleField = simpleField;
            this.searchableField = searchableField;
            this.complexField = complexField;
        }

        private static AnnotationData create(Member member) {
            AnnotatedElement annotatedElement;
            if (member instanceof Field) {
                annotatedElement = (Field) member;
            } else if (member instanceof Method) {
                annotatedElement = (Method) member;
            } else {
                return null;
            }

            SimpleField simpleField = annotatedElement.getAnnotation(SimpleField.class);
            SearchableField searchableField = annotatedElement.getAnnotation(SearchableField.class);
            ComplexField complexField = annotatedElement.getAnnotation(ComplexField.class);

            List<String> contains = new ArrayList<>(3);
            if (simpleField != null) {
                contains.add("SimpleField");
            }
            if (searchableField != null) {
                contains.add("SearchableField");
            }
            if (complexField != null) {
                contains.add("ComplexField");
            }

            if (contains.size() > 1) {
                throw LOGGER.atError()
                    .addKeyValue("annotations", CoreUtils.stringJoin(",", contains))
                    .addKeyValue("fieldOrMethodName", member.getName())
                    .addKeyValue("fieldOrMemberDeclaringClass", member.getDeclaringClass())
                    .log(new IllegalStateException("Field or method may only be annotated with one of 'SimpleField', "
                        + "'SearchableField', or 'ComplexField' at a time."));
            } else if (contains.isEmpty()) {
                return null;
            } else {
                return new AnnotationData(member, simpleField, searchableField, complexField);
            }
        }

        private String getFieldName() {
            if (simpleField != null) {
                return simpleField.name();
            } else if (searchableField != null) {
                return searchableField.name();
            } else {
                return complexField.name();
            }
        }

        private SearchField enrichWithAnnotation(SearchField searchField) {
            if (complexField != null) {
                // ComplexFields have no enrichments.
                return searchField;
            }
            boolean key;
            boolean hidden;
            boolean retrievable;
            boolean filterable;
            boolean sortable;
            boolean facetable;
            String permissionFilter;
            boolean sensitivityLabel;
            boolean stored;
            boolean searchable = searchableField != null;
            String analyzerName = null;
            String searchAnalyzerName = null;
            String indexAnalyzerName = null;
            String[] synonymMapNames = null;
            String normalizerName = null;
            Integer vectorSearchDimensions = null;
            String vectorSearchProfileName = null;
            String vectorEncodingFormat = null;

            if (simpleField != null) {
                key = simpleField.isKey();
                hidden = simpleField.isHidden();
                retrievable = simpleField.isRetrievable();
                stored = true;
                filterable = simpleField.isFilterable();
                sortable = simpleField.isSortable();
                facetable = simpleField.isFacetable();
                normalizerName = simpleField.normalizerName();
                permissionFilter = simpleField.permissionFilter();
                sensitivityLabel = simpleField.isSensitivityLabel();
            } else {
                key = searchableField.isKey();
                hidden = searchableField.isHidden();
                retrievable = searchableField.isRetrievable();
                stored = searchableField.isStored();
                filterable = searchableField.isFilterable();
                sortable = searchableField.isSortable();
                facetable = searchableField.isFacetable();
                permissionFilter = searchableField.permissionFilter();
                sensitivityLabel = searchableField.isSensitivityLabel();
                analyzerName = searchableField.analyzerName();
                searchAnalyzerName = searchableField.searchAnalyzerName();
                indexAnalyzerName = searchableField.indexAnalyzerName();
                synonymMapNames = searchableField.synonymMapNames();
                normalizerName = searchableField.normalizerName();
                vectorSearchDimensions
                    = searchableField.vectorSearchDimensions() > 0 ? searchableField.vectorSearchDimensions() : null;
                vectorSearchProfileName = CoreUtils.isNullOrEmpty(searchableField.vectorSearchProfileName())
                    ? null
                    : searchableField.vectorSearchProfileName();
                vectorEncodingFormat = CoreUtils.isNullOrEmpty(searchableField.vectorEncodingFormat())
                    ? null
                    : searchableField.vectorEncodingFormat();
            }

            StringBuilder errorMessage = new StringBuilder();
            boolean isStringOrCollectionString
                = searchField.getType() == SearchFieldDataType.STRING || searchField.getType() == COLLECTION_STRING;
            boolean isSearchableType = isStringOrCollectionString || searchField.getType() == COLLECTION_SINGLE;
            boolean hasAnalyzerName = !CoreUtils.isNullOrEmpty(analyzerName);
            boolean hasSearchAnalyzerName = !CoreUtils.isNullOrEmpty(searchAnalyzerName);
            boolean hasIndexAnalyzerName = !CoreUtils.isNullOrEmpty(indexAnalyzerName);
            boolean hasNormalizerName = !CoreUtils.isNullOrEmpty(normalizerName);
            boolean hasVectorEncodingFormat = !CoreUtils.isNullOrEmpty(vectorEncodingFormat);
            if (retrievable == hidden) {
                errorMessage.append("'isHidden' and 'isRetrievable' were set to the same boolean value, this is "
                    + "invalid as they have opposite meanings and therefore the configuration is ambiguous.");
            }
            if (searchable) {
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

            if (searchField.getType() == COLLECTION_SINGLE
                && (vectorSearchDimensions == null || vectorSearchProfileName == null)) {
                errorMessage.append(
                    "Please specify both vectorSearchDimensions and vectorSearchProfileName for Collection(Edm.Single) type. ");
            }

            // Any field is allowed to have a normalizer, but it must be either a STRING or Collection(STRING) and have one
            // of filterable, sortable, or facetable set to true.
            if (hasNormalizerName && (!isStringOrCollectionString || !(filterable || sortable || facetable))) {
                errorMessage
                    .append("A field with a normalizer name can only be used on string properties and must have ")
                    .append("one of filterable, sortable, or facetable set to true. ");
            }

            if (errorMessage.length() > 0) {
                throw LOGGER.logExceptionAsError(new RuntimeException(errorMessage.toString()));
            }

            searchField.setKey(key)
                .setHidden(hidden)
                .setRetrievable(retrievable)
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

            if (hasNormalizerName) {
                searchField.setNormalizerName(LexicalNormalizerName.fromString(normalizerName));
            }

            if (hasVectorEncodingFormat) {
                searchField.setVectorEncodingFormat(VectorEncodingFormat.fromString(vectorEncodingFormat));
            }

            if (!CoreUtils.isNullOrEmpty(permissionFilter)) {
                searchField.setPermissionFilter(PermissionFilter.fromString(permissionFilter));
            }

            searchField.setSensitivityLabel(sensitivityLabel);

            if (!CoreUtils.isNullOrEmpty(synonymMapNames)) {
                List<String> synonymMaps = Arrays.stream(searchableField.synonymMapNames())
                    .filter(synonym -> !synonym.trim().isEmpty())
                    .collect(Collectors.toList());
                searchField.setSynonymMapNames(synonymMaps);
            }

            return searchField;
        }
    }
}
