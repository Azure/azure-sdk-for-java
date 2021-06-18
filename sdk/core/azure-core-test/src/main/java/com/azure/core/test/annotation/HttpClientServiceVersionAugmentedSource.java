// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides test methods the ability to create a parameterized test using the permutation of
 * <p>
 * HttpClient X ServiceVersions X Arguments supplied by the parameterized testing values
 * <p>
 * This annotation should act similar to {@link ParameterizedTest}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(HttpClientServiceVersionAugmentedArgumentsProvider.class)
public @interface HttpClientServiceVersionAugmentedSource {
    /**
     * Name of the method, either fully-qualified with package and class information or relative to the test method,
     * which provides parameterized testing values.
     * <p>
     * The source supplier method must be static and have a return type of {@code Stream<Arguments>}. If either of these
     * don't hold true an {@link IllegalArgumentException} will be thrown during runtime.
     * <p>
     * If the return value from the source supplier method isn't {@code Stream<Arguments>} an {@link
     * IllegalStateException} will be thrown during runtime.
     * <p>
     * By default no additional parameterized testing values are expected.
     *
     * @return The name of the method, either fully-qualified or relative, which provides parameterized testing values.
     */
    String sourceSupplier() default "";

    /**
     * A flag indicating if the test method ignores {@link HttpClient HttpClients} that the test run is expected to
     * use.
     * <p>
     * When {@link TestMode} is {@link TestMode#PLAYBACK} this value is ignored as playback uses a specialized
     * HttpClient that doesn't make network calls.
     *
     * @return Whether the expected HttpClients to be used in testing are ignored.
     */
    boolean useHttpClientPermutation() default false;

    /**
     * The class that represents the service version type.
     * <p>
     * This is used to convert the {@link #minimumServiceVersion()} into its {@link ServiceVersion} type.
     *
     * @return The class that represents the minimum service version the test can be ran against.
     */
    Class<? extends ServiceVersion> serviceVersionType();

    /**
     * The minimum service version that the test can be ran against.
     * <p>
     * If this isn't set the test will run against all service versions.
     *
     * @return The minimum service version that the test can be ran against.
     */
    String minimumServiceVersion() default "";
}
