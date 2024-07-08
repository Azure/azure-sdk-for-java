// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used for flattening properties separated by '.'. E.g. a property with JsonProperty value
 * "properties.value" will have "value" property under the "properties" tree on the wire. This annotation when used on a
 * class, all JSON fields will be checked for '.' and be flattened appropriately.
 */
@Retention(RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.FIELD })
public @interface JsonFlatten {
}
