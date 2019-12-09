// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.TestMode;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation given to some tests to indicate that network calls made during the test shouldn't be recorded.
 *
 * <p>
 * Pass {@code true} for {@link #skipInPlayback() skipInPlayback} to indicate that the test shouldn't run when tests are
 * ran in {@link TestMode#PLAYBACK}. A common case for setting this to {@code true} is when the test has either
 * sensitive content that cannot be redacted or calls into code that cannot be mocked.
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface DoNotRecord {

    /**
     * Returns whether the test will be ignored during a {@link TestMode#PLAYBACK playback} test run.
     *
     * @return Flag indicating if the test will be ignored during a playback test run.
     */
    boolean skipInPlayback() default false;
}
