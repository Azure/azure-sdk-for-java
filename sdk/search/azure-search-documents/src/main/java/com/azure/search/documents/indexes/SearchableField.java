// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.indexes.models.VectorEncodingFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that directs {@link SearchIndexAsyncClient#buildSearchFields(Class, FieldBuilderOptions)} to turn the
 * field or method into a searchable {@link SearchField field}.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableField {
    /**
     * Indicates if the field or method should generate as a key {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a key {@link SearchField field}.
     */
    boolean isKey() default false;

    /**
     * Indicates if the field or method should generate as a hidden {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a hidden {@link SearchField field}.
     */
    boolean isHidden() default false;

    /**
     * Indicates if the field or method should generate as a facetable {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a facetable {@link SearchField field}.
     */
    boolean isFacetable() default false;

    /**
     * Indicates if the field or method should generate as a sortable {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a sortable {@link SearchField field}.
     */
    boolean isSortable() default false;

    /**
     * Indicates if whether the field will be persisted separately on disk to be returned in a search result.
     *
     * @return A flag indicating if the field or method should generate as a stored {@link SearchField field}.
     */
    boolean isStored() default true;

    /**
     * Indicates if the field or method should generate as a filterable {@link SearchField field}.
     *
     * @return A flag indicating if the field or method should generate as a filterable {@link SearchField field}.
     */
    boolean isFilterable() default false;

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
     * A list of {@link SynonymMap} names to be associated with the {@link SearchField field}.
     * <p>
     * Assigning a synonym map to a field ensures that query terms targeting that field are expanded at query-time using
     * the rules in the synonym map. The synonym map attribute may be changed on existing fields.
     * <p>
     * Currently, only one synonym map per field is supported.
     *
     * @return The {@link SynonymMap} names that will be associated with the {@link SearchField field}.
     */
    String[] synonymMapNames() default {};

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
}
