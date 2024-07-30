// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.junitextensions.PlaybackOnlyExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark tests that should only be run in PLAYBACK test mode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(PlaybackOnlyExtension.class)
public @interface PlaybackOnly {
    /**
     * The expiry time of the PLAYBACK annotation in yyyy-MM-dd format.
     * <p>
     * If the expiry time is not set, the test will only run in PLAYBACK mode.
     * <p>
     * If the expiry time is set, the test will begin to fail instead of being skipped once the expiry time has passed.
     * For example, if the expiry time is set to 2020-01-01, the test will be skipped in PLAYBACK mode until 2020-01-01.
     * After 2020-01-01, the test will fail in PLAYBACK mode until the test is re-enabled.
     *
     * @return The expiry time of the PLAYBACK annotation.
     */
    String expiryTime() default "";
}
