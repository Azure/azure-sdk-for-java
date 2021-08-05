// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import com.azure.cosmos.models.CompositePathSortOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying a composite index path
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CompositeIndexPath {

    /**
     * Index path
     * @return String
     */
    String path() default "";

    /**
     * Index order
     * @return CompositePathSortOrder
     */
    CompositePathSortOrder order() default CompositePathSortOrder.ASCENDING;

}
