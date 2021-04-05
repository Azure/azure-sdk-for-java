// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Access level for java declarations.
 */
public enum Access {

    /**
     * No explicit access level.
     */
    IMPLICIT,

    /**
     * Public access.
     */
    PUBLIC,

    /**
     * Package private.
     */
    PACKAGE_PRIVATE,

    /**
     * Protected.
     */
    PROTECTED,

    /**
     * Private.
     */
    PRIVATE,
}
