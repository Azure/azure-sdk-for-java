// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.TestMode;
import com.azure.core.util.ServiceVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides test classes the ability to configure which service versions are used when running tests in
 * {@link TestMode#RECORD}, {@link TestMode#LIVE}, and {@link TestMode#PLAYBACK} modes.
 * <p>
 * The service versions configured here mat interact with how {@link HttpClientServiceVersionAugmentedSource} generates
 * parameterized test permutations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TestingServiceVersions {
    /**
     * The class that represents the service version type.
     * <p>
     * This is used to convert {@link #liveServiceVersions()} and {@link #recordingServiceVersion()} into their
     * {@link ServiceVersion} type.
     * <p>
     * If no {@link #liveServiceVersions()} are provided or {@link #recordingServiceVersion()} isn't configured this
     * class will be used to determine the latest service version and that will be used for their values.
     *
     * @return The class that represents the service version type.
     */
    Class<? extends ServiceVersion> serviceVersionType();

    /**
     * List of service versions used during {@link TestMode#LIVE} testing.
     * <p>
     * If no service versions are configured the latest service version available will be the default.
     * <p>
     * When {@link TestMode} is {@link TestMode#PLAYBACK} this value is ignored as playback always runs against the
     * {@link #recordingServiceVersion()}.
     *
     * @return A list of supported service version strings.
     */
    String[] liveServiceVersions() default {};

    /**
     * The service version used when running in {@link TestMode#RECORD} and {@link TestMode#PLAYBACK} mode.
     * mode.
     * <p>
     * If this isn't set the latest service version of {@link #serviceVersionType()} is used.
     *
     * @return The service version used when running in {@link TestMode#RECORD} and {@link TestMode#PLAYBACK} mode.
     */
    String recordingServiceVersion() default "";
}
