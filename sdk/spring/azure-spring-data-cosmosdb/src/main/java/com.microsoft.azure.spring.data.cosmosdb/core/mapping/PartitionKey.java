// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface PartitionKey {

    /**
     * The name of the partition key if the serialized attribute name differs from the field name
     *
     * @return partition key name
     */
    String value() default "";
}
