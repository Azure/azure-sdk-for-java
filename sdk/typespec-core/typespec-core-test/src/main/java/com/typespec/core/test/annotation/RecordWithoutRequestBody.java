// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation given to some tests to skip recording request bodies when test run in Record mode and using Test Proxy.
 * <p>A common case for setting this is when the test has either
 * sensitive content that cannot be redacted or large request body content that does not need to be recorded.
 * </p>
 */
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RecordWithoutRequestBody {
}
