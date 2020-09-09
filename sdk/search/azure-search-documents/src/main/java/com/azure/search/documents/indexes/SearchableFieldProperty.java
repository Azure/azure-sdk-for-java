// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.LexicalAnalyzerName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation is to indicate whether the field is a searchable field. The boolean field of isSearchable
 * defaults to true if use the annotation.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableFieldProperty {
    /**
     * Optional arguments defines whether the field is a key field or not.
     *
     * @return True if it is the key of SearchField, and false by default for non-key field.
     */
    boolean isKey() default false;

    /**
     * Optional arguments defines whether the field is hidden or not.
     *
     * @return True if it is not retrievable, and false by default for retrievable field.
     */
    boolean isHidden() default false;

    /**
     * Optional arguments defines whether the field is facetable or not.
     *
     * @return True if it is facetable, and false by default for non-facetable field.
     */
    boolean isFacetable() default false;

    /**
     * Optional arguments defines whether the field is sortable or not.
     *
     * @return True if it is sortable, and false by default for non-sortable field.
     */
    boolean isSortable() default false;

    /**
     * Optional arguments defines whether the field is filterable or not.
     *
     * @return True if it is filterable, and false by default for non-filterable field.
     */
    boolean isFilterable() default false;

    /**
     * Optional arguments defines the name of the analyzer used for the field.
     *
     * @return {@link LexicalAnalyzerName} String value. Or default to "null" String type.
     */
    String analyzerName() default "";

    /**
     * Optional arguments defines the name of the search analyzer used for the field.
     *
     * @return {@link LexicalAnalyzerName} String value. Or default to an empty String.
     */
    String searchAnalyzerName() default "";

    /**
     * Optional arguments defines the name of the analyzer used for the field.
     *
     * @return {@link LexicalAnalyzerName} String value. Or default to an empty String.
     */
    String indexAnalyzerName() default "";

    /**
     * Optional arguments defines the array of synonymMaps used for the field.
     * This option can be used only with searchable fields. Currently only one
     * synonym map per field is supported. Assigning a synonym map to a field
     * ensures that query terms targeting that field are expanded at query-time
     * using the rules in the synonym map. This attribute can be changed on
     * existing fields.
     *
     * @return An array of synonym map values. Or default to empty string array.
     */
    String[] synonymMapNames() default {};
}
