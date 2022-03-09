// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping;

import com.azure.cosmos.models.IndexingMode;
import com.azure.spring.data.cosmos.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for cosmos indexing policy
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CosmosIndexingPolicy {
    /**
     * To set automatic indexing
     * @return default as true
     */
    boolean automatic() default Constants.DEFAULT_INDEXING_POLICY_AUTOMATIC;

    /**
     * To set indexing mode
     *
     * @return IndexingMode
     */
    IndexingMode mode() default IndexingMode.CONSISTENT; // Enum is not really compile time constant

    /**
     * To include paths
     * @return String[]
     */
    String[] includePaths() default {};

    /**
     * To exclude paths
     * @return String[]
     */
    String[] excludePaths() default {};


    /**
     * Composite Indexes
     * @return CompositeIndexDefinition[]
     */
    CompositeIndex[] compositeIndexes() default {};
}
