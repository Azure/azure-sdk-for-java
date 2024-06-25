// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotation given to all immutable classes. If a class has this annotation, checks can be made to ensure all fields in
 * this class are final.
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Immutable {

}
