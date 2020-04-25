package com.azure.search.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleFieldProperty {
    boolean isKey();
    boolean isRetrievable();
    boolean isFacetable();
    boolean isSearchable();
    boolean isSortable();
    boolean isFilterable();
    String analyzer();
    String searchAnalyzer();
    String indexAnalyzer();
    String[] synonymMaps();
}
