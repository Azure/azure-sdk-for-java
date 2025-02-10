// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.annotation;

/**
 * Enumeration of return types used with {@link ServiceMethod} annotation to indicate if a method is expected to return
 * a single item or a collection.
 */
public enum ReturnType {
    /**
     * Single value return type.
     */
    SINGLE,

    /**
     * Simple collection, enumeration, return type.
     */
    COLLECTION,

    /**
     * Server Sent Event operation return type.
     */
    SERVER_SENT_EVENT
}
