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
 * Represents a unique key on that enforces uniqueness constraint on items in the container in the Azure Cosmos DB
 * service.
 * <p>
 * 1) For containers, the value of partition key is implicitly a part of each unique key.
 * <p>
 * 2) Uniqueness constraint is also enforced for missing values.
 * <p>
 * For instance, if unique key policy defines a unique key with single property path, there could be only one item that
 * has missing value for this property.
 *
 * @see CosmosUniqueKeyPolicy
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CosmosUniqueKey {

    /**
     * A set of paths which must be unique for each item in the Azure Cosmos DB service.
     * <p>
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the item, such as
     * "/name/first".
     *
     * @return unique paths
     */
    String[] paths() default {};
}
