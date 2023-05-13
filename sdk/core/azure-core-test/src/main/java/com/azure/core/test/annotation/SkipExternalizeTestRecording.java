// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation given to some tests to skip externalizing recording to enable running test validations locally.
 * <p>This requires the test recordings to be present src/test/resources/session-records folder</p>
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface SkipExternalizeTestRecording {
}
