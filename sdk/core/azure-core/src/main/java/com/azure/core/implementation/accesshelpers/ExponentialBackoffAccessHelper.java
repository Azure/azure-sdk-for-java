// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.accesshelpers;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RequestRetryCondition;

import java.util.function.Predicate;

/**
 * Class containing helper methods for accessing private members of {@link ExponentialBackoff}.
 */
public final class ExponentialBackoffAccessHelper {
    private static ExponentialBackoffAccessor accessor;

    /**
     * Type defining the methods to set the non-public properties of an {@link ExponentialBackoff} instance.
     */
    public interface ExponentialBackoffAccessor {
        /**
         * Creates an {@link ExponentialBackoff} instance with the passed {@code exponentialBackoffOptions} and
         * {@code shouldRetryCondition}.
         *
         * @param exponentialBackoffOptions The {@link ExponentialBackoffOptions}.
         * @param shouldRetryCondition The {@link Predicate} to determine if a request should be retried.
         * @return The created {@link ExponentialBackoff} instance.
         */
        ExponentialBackoff create(ExponentialBackoffOptions exponentialBackoffOptions,
            Predicate<RequestRetryCondition> shouldRetryCondition);
    }

    /**
     * The method called from {@link ExponentialBackoff} to set it's accessor.
     *
     * @param exponentialBackoffAccessor The accessor.
     */
    public static void setAccessor(final ExponentialBackoffAccessor exponentialBackoffAccessor) {
        accessor = exponentialBackoffAccessor;
    }

    /**
     * Creates an {@link ExponentialBackoff} instance with the passed {@code exponentialBackoffOptions} and
     * {@code shouldRetryCondition}.
     *
     * @param exponentialBackoffOptions The {@link ExponentialBackoffOptions}.
     * @param shouldRetryCondition The {@link Predicate} to determine if a request should be retried.
     * @return The created {@link ExponentialBackoff} instance.
     */
    public static ExponentialBackoff create(ExponentialBackoffOptions exponentialBackoffOptions,
        Predicate<RequestRetryCondition> shouldRetryCondition) {
        if (accessor == null) {
            new ExponentialBackoff();
        }

        assert accessor != null;
        return accessor.create(exponentialBackoffOptions, shouldRetryCondition);
    }

    private ExponentialBackoffAccessHelper() {
    }
}
