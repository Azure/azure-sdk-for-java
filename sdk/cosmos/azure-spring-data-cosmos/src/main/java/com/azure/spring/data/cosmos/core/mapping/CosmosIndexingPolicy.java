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
 * Annotation for cosmos indexing policy.
 * Using this annotation will overwrite the cosmos indexing policy currently in Azure Portal.
 * To prevent overwriting the Indexing Policy defined on Azure Portal, Indexing Policy defined on the SDK through this annotation should be identical or set overwritePolicy flag to false.
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CosmosIndexingPolicy {
    /**
     * Whether or not to overwrite the indexing policy specified in the Portal.
     * @return default as false
     */
    boolean overwritePolicy() default Constants.DEFAULT_INDEXING_POLICY_OVERWRITE_POLICY;

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
