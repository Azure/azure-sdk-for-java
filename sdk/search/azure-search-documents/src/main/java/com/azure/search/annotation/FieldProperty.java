// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD})
public @interface FieldProperty {
    boolean isKey() default false;
    boolean isRetrievable() default false;
    boolean isFacetable() default false;
    boolean isSearchable() default false;
    boolean isSortable() default false;
    boolean isFilterable() default false;
    String analyzer() default "null";
    String searchAnalyzer() default "null";
    String indexAnalyzer() default "null";
    String[] synonymMaps() default {};
}
