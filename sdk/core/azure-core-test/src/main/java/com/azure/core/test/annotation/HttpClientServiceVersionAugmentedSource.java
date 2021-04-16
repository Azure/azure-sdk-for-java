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
     * List of service versions either the test method supports.
     * <p>
     * If no service versions are configured the latest service version available will be the default.
     * <p>
     * When {@link TestMode} is {@link TestMode#PLAYBACK} this value is ignored as playback always runs against the
     * latest service version.
     *
     * @return A list of supported service version strings.
     */
    String[] serviceVersions() default {};

    /**
     * The class that represents the service version type used by the test method.
     * <p>
     * This is used to convert the {@link #serviceVersions()} into their {@link ServiceVersion} type.
     * <p>
     * If no {@link #serviceVersions()} are provided this class will be used to determine the latest service version and
     * use that.
     *
     * @return The class that represents the service version type used by the test method.
     */
    Class<? extends ServiceVersion> serviceVersionType();

    /**
     * A flag indicating if the test method ignores {@link HttpClient HttpClients} that the test run is expected to
     * use.
     * <p>
     * When {@link TestMode} is {@link TestMode#PLAYBACK} this value is ignored as playback uses a specialized
     * HttpClient that doesn't make network calls.
     *
     * @return Whether the expected HttpClients to be used in testing are ignored.
     */
    boolean ignoreHttpClients() default false;
}
