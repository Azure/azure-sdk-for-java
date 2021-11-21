// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.implementation.AzureMethodSourceArgumentsProvider;
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
@ArgumentsSource(AzureMethodSourceArgumentsProvider.class)
public @interface AzureMethodSource {
    /**
     * Name of the method, either fully-qualified with package and class information or relative to the test method,
     * which provides parameterized testing values.
     * <p>
     * The source supplier method must be static and have a return type of {@code Stream<Arguments>}. If either of these
     * don't hold true an {@link IllegalArgumentException} will be thrown during runtime.
     * <p>
     * By default no additional parameterized testing values are expected.
     *
     * @return The name of the method, either fully-qualified or relative, which provides parameterized testing values.
     */
    String sourceSupplier() default "";

    /**
     * A flag indicating if the test method should use {@link HttpClient HttpClients} when creating test permutations.
     * <p>
     * When {@link TestMode} is {@link TestMode#PLAYBACK} this value is ignored as playback uses a specialized
     * HttpClient that doesn't make network calls.
     * <p>
     * By default {@link HttpClient HttpClients} are used in testing permutations.
     *
     * @return A flag indication if {@link HttpClient HttpClients} are used when creating test permutations.
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
     * If neither this or {@link #maximumServiceVersion()} are set the test will run against all service versions. If
     * only this is set then the test will run against all service versions equal to or later than the minimum. If both
     * this and {@link #maximumServiceVersion()} are set then the inclusive range of service versions will be used to
     * test.
     * <p>
     * If no service versions meet the requirements of this and {@link #maximumServiceVersion()} the test will fail.
     * <p>
     * {@link ServiceVersion ServiceVersions} are compared using their enum ordinal.
     *
     * @return The minimum service version that the test can be ran against.
     */
    String minimumServiceVersion() default "";

    /**
     * The maximum service version that the test can be ran against.
     * <p>
     * If neither this or {@link #minimumServiceVersion()} are set the test will run against all service versions. If
     * only this is set then the test will run against all service versions less than or equal to the maximum. If both
     * this and {@link #minimumServiceVersion()} are set then the inclusive range of service versions will be used to
     * test.
     * <p>
     * If no service versions meet the requirements of this and {@link #minimumServiceVersion()} the test will fail.
     * <p>
     * {@link ServiceVersion ServiceVersions} are compared using their enum ordinal.
     *
     * @return The maximum service version that the test can be ran against.
     */
    String maximumServiceVersion() default "";
}
