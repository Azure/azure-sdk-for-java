// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.SearchField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation is to indicate whether the field is a simple field. This annotation can only set boolean field of
 * {@link SearchField}. {@code isSearchable} will set to {@code false}.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleFieldProperty {
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
}
