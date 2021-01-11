// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

/**
 * Enumeration of return types used with {@link ServiceMethod} annotation to indicate if a
 * method is expected to return a single item or a collection
 */
public enum ReturnType {
    SINGLE,
    COLLECTION,
    LONG_RUNNING_OPERATION
}
