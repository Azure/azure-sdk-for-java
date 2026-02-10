// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.LexicalNormalizerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.indexes.models.VectorEncodingFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to create {@link SearchField SearchFields} using {@link SearchIndexClient#buildSearchFields(Class)}
 * or {@link SearchIndexAsyncClient#buildSearchFields(Class)}.
 * <p>
 * Only fields or methods annotated with this annotation or {@link ComplexField} will be used to create
 * {@link SearchField SearchFields}.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BasicField {
    /**
     * The {@link SearchField#getName()} used in the {@link SearchIndex}.
     *
     * @return The name of the field.
     */
    String name();

    /**
     * Indicates if the field or method should generate as a key {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a key {@link SearchField field}.
     */
    BooleanHelper isKey() default BooleanHelper.NULL;

    /**
     * Indicates if the field or method should generate as a hidden {@link SearchField field}.
     * <p>
     * When building fields, unless {@link BooleanHelper#NULL} is set, this must have the opposite value of
     * {@link #isRetrievable()}.
     *
     * @return A flag indicating if the field or method should generate as a hidden {@link SearchField field}.
     * @deprecated Use {@link #isRetrievable()} instead and flip the boolean value.
     */
    @Deprecated
    BooleanHelper isHidden() default BooleanHelper.NULL;

    /**
     * Indicates if the field or method should generate as a retrievable {@link SearchField field}.
     * <p>
     * When building fields, unless {@link BooleanHelper#NULL} is set, this must have the opposite value of
     * {@link #isHidden()}.
     *
     * @return A flag indicating if the field or method should generate as a retrievable {@link SearchField field}.
     */
    BooleanHelper isRetrievable() default BooleanHelper.NULL;

    /**
     * Indicates if whether the field will be persisted separately on disk to be returned in a search result.
     *
     * @return A flag indicating if the field or method should generate as a stored {@link SearchField field}.
     */
    BooleanHelper isStored() default BooleanHelper.NULL;

    /**
     * Indicates whether the field can be searched against.
     *
     * @return Indicates whether the field can be searched against.
     */
    BooleanHelper isSearchable() default BooleanHelper.NULL;

    /**
     * Indicates if the field or method should generate as a filterable {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a filterable {@link SearchField field}.
     */
    BooleanHelper isFilterable() default BooleanHelper.NULL;

    /**
     * Indicates if the field or method should generate as a sortable {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a sortable {@link SearchField field}.
     */
    BooleanHelper isSortable() default BooleanHelper.NULL;

    /**
     * Indicates if the field or method should generate as a facetable {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a facetable {@link SearchField field}.
     */
    BooleanHelper isFacetable() default BooleanHelper.NULL;

    /**
     * Indicates if the field or method should be used as a permission filter {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a filterable {@link SearchField field}.
     */
    String permissionFilter() default "";

    /**
     * Indicates if the field or method should be used for sensitivity label filtering. This enables document-level
     * filtering based on Microsoft Purview sensitivity labels.
     *
     * @return A flag indicating if the field or method should generate as a sensitivity label {@link SearchField field}.
     */
    BooleanHelper isSensitivityLabel() default BooleanHelper.NULL;

    /**
     * A {@link LexicalAnalyzerName} to associate as the search and index analyzer for the {@link SearchField field}.
     *
     * @return The {@link LexicalAnalyzerName} that will be associated as the search and index analyzer for the
     * {@link SearchField field}.
     */
    String analyzerName() default "";

    /**
     * A {@link LexicalAnalyzerName} to associate as the search analyzer for the {@link SearchField field}.
     *
     * @return The {@link LexicalAnalyzerName} that will be associated as the search analyzer for the
     * {@link SearchField field}.
     */
    String searchAnalyzerName() default "";

    /**
     * A {@link LexicalAnalyzerName} to associate as the index analyzer for the {@link SearchField field}.
     *
     * @return The {@link LexicalAnalyzerName} that will be associated as the index analyzer for the
     * {@link SearchField field}.
     */
    String indexAnalyzerName() default "";

    /**
     * A {@link LexicalNormalizerName} to associate as the normalizer for the {@link SearchField field}.
     *
     * @return The {@link LexicalNormalizerName} that will be associated as the normalizer for the
     * {@link SearchField field}.
     */
    String normalizerName() default "";

    /**
     * The dimensionality of the vector field.
     * <p>
     * If the value is negative or 0, the field won't have a {@link SearchField#getVectorSearchDimensions()} value.
     *
     * @return The dimensionality of the vector {@link SearchField field}.
     */
    int vectorSearchDimensions() default -1;

    /**
     * The name of the vector search profile that specifies the parameters for searching the vector field.
     * <p>
     * If the value is empty, the field won't have a {@link SearchField#getVectorSearchProfileName()} ()} value.
     *
     * @return The name of the vector search profile that specifies the parameters for searching the vector
     * {@link SearchField field}.
     */
    String vectorSearchProfileName() default "";

    /**
     * A {@link VectorEncodingFormat} to be associated with the {@link SearchField field}.
     * <p>
     * If the value is empty, the field won't have a {@link SearchField#getVectorEncodingFormat()} value.
     *
     * @return The {@link VectorEncodingFormat} that will be associated with the {@link SearchField field}.
     */
    String vectorEncodingFormat() default "";

    /**
     * A list of {@link SynonymMap} names to be associated with the {@link SearchField field}.
     * <p>
     * Assigning a synonym map to a field ensures that query terms targeting that field are expanded at query-time using
     * the rules in the synonym map. The synonym map attribute may be changed on existing fields.
     * <p>
     * Currently, only one synonym map per field is supported.
     *
     * @return The {@link SynonymMap} names that will be associated with the {@link SearchField field}.
     */
    String[] synonymMapNames() default { };

    /**
     * Enum helper for boolean values to allow for nullness.
     */
    enum BooleanHelper {
        /**
         * Equivalent to {@code Boolean b = null}, used when the Azure AI Search default for the field type should be
         * used.
         */
        NULL,

        /**
         * Equivalent to {@code Boolean b = false}.
         */
        FALSE,

        /**
         * Equivalent to {@code Boolean b = true}.
         */
        TRUE
    }
}
