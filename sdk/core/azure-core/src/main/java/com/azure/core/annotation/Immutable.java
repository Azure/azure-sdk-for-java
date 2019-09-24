// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation given to all immutable classes. If a class has this annotation, checks can be made to ensure all
 * fields in this class are final.
 */
@Target({TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Immutable {

}
