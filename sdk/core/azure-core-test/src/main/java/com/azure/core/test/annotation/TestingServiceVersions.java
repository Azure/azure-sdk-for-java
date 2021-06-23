// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.TestMode;
import com.azure.core.util.ServiceVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides test classes the ability to configure which service versions are used when running tests in
 * {@link TestMode#RECORD}, {@link TestMode#LIVE}, and {@link TestMode#PLAYBACK} modes.
 * <p>
 * The service versions configured here mat interact with how {@link AzureMethodSource} generates parameterized test
 * permutations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TestingServiceVersions {
    /**
     * The class that represents the service version type.
     * <p>
     * This is used to convert {@link #minimumLiveServiceVersion()}, {@link #maximumLiveServiceVersion()}, and {@link
     * #recordingServiceVersion()} into their {@link ServiceVersion} type.
     * <p>
     * If neither of {@link #minimumLiveServiceVersion()} and {@link #maximumLiveServiceVersion()} are provided all
     * {@link ServiceVersion ServiceVersions} represented by this type will be used. If {@link
     * #recordingServiceVersion()} isn't configured the latest {@link ServiceVersion} represented by this type will be
     * used.
     * <p>
     * If this type isn't an {@link Enum} an {@link IllegalArgumentException} will be thrown during runtime.
     *
     * @return The class that represents the service version type.
     */
    Class<? extends ServiceVersion> serviceVersionType();

    /**
     * The minimum service version used during {@link TestMode#LIVE} testing.
     * <p>
     * If neither this or {@link #maximumLiveServiceVersion()} are set all service versions will be used. If only this
     * is set then all service versions equal to or later than the set version will be used. If both this and {@link
     * #maximumLiveServiceVersion()} are set then the inclusive range of service versions will be used.
     * <p>
     * If no service versions meet the requirements of this and {@link #maximumLiveServiceVersion()} an {@link
     * IllegalStateException} will be thrown during runtime.
     * <p>
     * {@link ServiceVersion ServiceVersions} are compared using their enum ordinal.
     *
     * @return The minimum service version that will be used.
     */
    String minimumLiveServiceVersion() default "";

    /**
     * The maximum service version used during {@link TestMode#LIVE} testing.
     * <p>
     * If neither this or {@link #minimumLiveServiceVersion()} are set all service versions will be used. If only this
     * is set then all service versions less than or equal to the set version will be used. If both this and {@link
     * #minimumLiveServiceVersion()} are set then the inclusive range of service versions will be used.
     * <p>
     * If no service versions meet the requirements of this and {@link #minimumLiveServiceVersion()} an {@link
     * IllegalStateException} will be thrown during runtime.
     * <p>
     * {@link ServiceVersion ServiceVersions} are compared using their enum ordinal.
     *
     * @return The minimum service version that will be used.
     */
    String maximumLiveServiceVersion() default "";

    /**
     * The service version used when running in {@link TestMode#RECORD} and {@link TestMode#PLAYBACK} mode.
     * <p>
     * If this isn't set the latest service version of {@link #serviceVersionType()} is used.
     *
     * @return The service version used when running in {@link TestMode#RECORD} and {@link TestMode#PLAYBACK} mode.
     */
    String recordingServiceVersion() default "";
}
