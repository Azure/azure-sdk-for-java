// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that directs {@link SearchIndexAsyncClient#buildSearchFields(Class)} to turn the field or method into a
 * {@link SearchFieldDataType#COMPLEX complex} {@link SearchField field}.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ComplexField {
    /**
     * The {@link SearchField#getName()} used in the {@link SearchIndex}.
     *
     * @return The name of the field.
     */
    String name();
}
