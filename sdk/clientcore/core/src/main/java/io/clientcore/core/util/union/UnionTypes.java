// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.union;

/**
 * This annotation is used to specify the types that are allowed in a {@link Union} type. It allows for better tooling
 * support to ensure users are properly consuming the types in a {@link Union} type.
 *
 * @see Union
 */
public @interface UnionTypes {
    /**
     * The types that are allowed in a {@link Union} type.
     *
     * @return The types that are allowed in a {@link Union} type.
     */
    Class<?>[] value();
}
