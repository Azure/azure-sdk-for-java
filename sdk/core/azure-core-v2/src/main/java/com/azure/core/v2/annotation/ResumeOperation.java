// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for method representing continuation operation.
 *
 * @deprecated This interface is no longer used, or respected, in code.
 */
@Retention(RUNTIME)
@Target(METHOD)
@Deprecated
public @interface ResumeOperation {
}
