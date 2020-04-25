package com.azure.search.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableFieldProperty {
    boolean isKey();
    boolean isRetrievable();
    boolean isFacetable();
    boolean isSortable();
    boolean isFilterable();
    String analyzer();
    String searchAnalyzer();
    String indexAnalyzer();
    String[] synonymMaps();
}
