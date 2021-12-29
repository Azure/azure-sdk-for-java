// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.core;

/**
 * Merge properties with its parent.
 * @param <C> The type of child properties.
 * @param <P> The type of parent properties.
 */
@FunctionalInterface
public interface ParentMerger<C, P> {

    C mergeParent(C child, P parent);

}
