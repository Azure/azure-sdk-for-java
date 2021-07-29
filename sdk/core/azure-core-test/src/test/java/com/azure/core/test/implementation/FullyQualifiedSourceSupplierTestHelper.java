// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

/**
 * Helper class for testing the source supplier methods on {@link AzureMethodSourceArgumentsProvider}.
 */
public final class FullyQualifiedSourceSupplierTestHelper {
    /**
     * Dummy method for testing that a non-static, non-{@code Stream<Arguments>} supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer.
     */
    public int nonStaticAndInvalidReturnTypeMethod() {
        return 1;
    }

    /**
     * Dummy method for testing that a non-static, non-Arguments Stream supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer Stream.
     */
    public Stream<Integer> anotherNonStaticAndInvalidReturnTypeMethod() {
        return Stream.of(1);
    }

    /**
     * Dummy method for testing that a non-static, {@code Stream<Arguments>} supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy Arguments Stream.
     */
    public Stream<Arguments> nonStaticMethod() {
        return Stream.of(Arguments.of(1));
    }

    /**
     * Dummy method for testing that a static, non-{@code Stream<Arguments>} supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer.
     */
    public static int staticButInvalidReturnTypeMethod() {
        return 1;
    }

    /**
     * Dummy method for testing that a static, non-Arguments stream supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer Stream.
     */
    public static Stream<Integer> anotherStaticButInvalidReturnTypeMethod() {
        return Stream.of(1);
    }

    /**
     * Dummy method for testing that a static, {@code Stream<Arguments>} supplier method that has any parameters results
     * in a {@link RuntimeException}.
     *
     * @param dummyParam Dummy parameter.
     * @return Dummy Arguments Stream.
     */
    public static Stream<Arguments> staticAndValidReturnTypeButHasParameters(Object dummyParam) {
        return Stream.of(Arguments.of(dummyParam));
    }

    /**
     * Dummy method for testing that a static, {@code Stream<Arguments>} supplier method is valid and returns the
     * expected Stream.
     *
     * @return A single Arguments Stream whose only value is the integer {@code 1}.
     */
    public static Stream<Arguments> staticAndValidReturnType() {
        return Stream.of(Arguments.of(1));
    }
}
