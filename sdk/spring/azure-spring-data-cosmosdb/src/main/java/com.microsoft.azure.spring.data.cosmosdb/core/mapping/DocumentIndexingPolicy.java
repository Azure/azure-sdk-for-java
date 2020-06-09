// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import com.azure.data.cosmos.IndexingMode;
import com.microsoft.azure.spring.data.cosmosdb.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DocumentIndexingPolicy {
    boolean automatic() default Constants.DEFAULT_INDEXINGPOLICY_AUTOMATIC;

    IndexingMode mode() default IndexingMode.CONSISTENT; // Enum is not really compile time constant

    String[] includePaths() default {};

    String[] excludePaths() default {};
}
