// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation given to all classes that are expected to provide a fluent API to end users. If a class has this
 * annotation,
 * checks can be made to ensure all API meets this expectation. Similarly, classes that are not annotated with this
 * annotation should not have fluent APIs.
 */
@Target({TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Fluent {

}
