// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.test.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Test timeout for test in playback mode.
 * No effect on tests in live or record mode.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PlaybackTimeout {
    /**
     * The duration of this timeout.
     *
     * @return timeout duration; must be a positive number
     */
    long value();
    /**
     * The time unit of this timeout.
     *
     * @return chrono unit
     * @see ChronoUnit
     */
    ChronoUnit unit() default ChronoUnit.SECONDS;
}
