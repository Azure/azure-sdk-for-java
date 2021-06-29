// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.TestMode;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation given to some tests to indicate that it only runs when {@link TestMode} is {@link TestMode#PLAYBACK}.
 * <p>
 * When a test is annotated with this and {@link TestMode} is either {@link TestMode#LIVE} or {@link TestMode#RECORD}
 * it will be skipped.
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface PlaybackOnly {
    /**
     * Reason why the test only runs in {@link TestMode#PLAYBACK}.
     *
     * @return Reason why the test only runs in {@link TestMode#PLAYBACK}.
     */
    String reason() default "Test only runs in TestMode.PLAYBACK";
}
