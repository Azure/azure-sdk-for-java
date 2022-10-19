// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the unique key policy configuration for specifying uniqueness constraints on items in the container in the
 * Azure Cosmos DB service.
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CosmosUniqueKeyPolicy {

    /**
     * Set of unique keys which enforce uniqueness constraint on items in the container
     * in the Azure Cosmos DB service.
     *
     * @return unique keys
     */
    CosmosUniqueKey[] uniqueKeys() default {};
}
