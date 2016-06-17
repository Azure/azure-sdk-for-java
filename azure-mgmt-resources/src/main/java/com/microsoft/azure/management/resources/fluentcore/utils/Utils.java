/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * Defines a few utilities.
 */
public final class Utils {
    /**
     * Generate a random ID from a prefix.
     *
     * @param prefix the prefix for the random value
     * @return a random value with the given prefix
     */
    public static String randomId(String prefix) {
        return prefix + String.valueOf(System.currentTimeMillis() % 100000L);
    }

    /**
     * Converts an object Boolean to a primitive boolean.
     *
     * @param value the <tt>Boolean</tt> value
     * @return <tt>false</tt> if the given Boolean value is null or false else <tt>true</tt>
     */
    public static boolean toPrimitiveBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    /**
     * Creates a void callback from a callback that returns another type of
     * instance. This is useful for internal async handoffs where returned
     * resource is stored elsewhere.
     *
     * @param model the fluent model
     * @param callback the callback to return the fluent model
     * @param <T> the fluent model type
     * @return the void callback
     */
    public static <T> ServiceCallback<Void> toVoidCallback(final T model, final ServiceCallback<T> callback) {
        return new ServiceCallback<Void>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<Void> result) {
                callback.success(new ServiceResponse<>(model, result.getResponse()));
            }
        };
    }

    /**
     * Creates a callback returning the inner resource from a fluent model
     * and a void callback, and set the inner on the fluent model.
     *
     * @param modelImpl the implementation instance of the fluent resource
     * @param callback the void callback
     * @param <T> the inner resource type
     * @param <FluentT> the fluent resource type
     * @param <FluentImplT> the implementation for the fuent resource type
     * @return the inner callback
     */
    public static <T, FluentT, FluentImplT extends CreatableImpl<FluentT, T, FluentImplT>> ServiceCallback<T>
            fromVoidCallback(final FluentImplT modelImpl, final ServiceCallback<Void> callback) {
        return new ServiceCallback<T>() {
            @Override
            public void failure(Throwable t) {
                callback.failure(t);
            }

            @Override
            public void success(ServiceResponse<T> result) {
                modelImpl.setInner(result.getBody());
                callback.success(new ServiceResponse<Void>(result.getHeadResponse()));
            }
        };
    }

    private Utils() {
    }
}
