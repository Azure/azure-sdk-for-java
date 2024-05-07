// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.TestMode;

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

    /**
     * Returns whether the test will be ignored during a {@link TestMode#PLAYBACK playback} test run.
     *
     * @return Flag indicating if the test will be ignored during a playback test run.
     * @deprecated If a test should not run in playback, use {@link LiveOnly} instead. This will be removed in a future
     * release.
     */
    @Deprecated
    boolean skipInPlayback() default false;
}
