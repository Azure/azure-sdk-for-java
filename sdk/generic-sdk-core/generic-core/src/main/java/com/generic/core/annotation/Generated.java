// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotation given to all classes that are auto-generated with a tool such as TypeSpec or AutoRest.
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Generated {

}
