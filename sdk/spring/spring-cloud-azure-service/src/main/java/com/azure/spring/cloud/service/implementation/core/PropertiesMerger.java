// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.core;

/**
 * Merge properties with another type.
 * @param <C> The type of first properties.
 * @param <P> The type of second properties.
 */
@FunctionalInterface
public interface PropertiesMerger<C, P> {

    C merge(C first, P second);

}
