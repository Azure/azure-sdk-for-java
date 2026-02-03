// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation given to some tests to indicate that network calls made during the test shouldn't be recorded or there
 * won't be any network calls made during the test to prevent creating an empty recording.
 */
@Retention(RUNTIME)
@Target({ METHOD })
public @interface DoNotRecord {
}
