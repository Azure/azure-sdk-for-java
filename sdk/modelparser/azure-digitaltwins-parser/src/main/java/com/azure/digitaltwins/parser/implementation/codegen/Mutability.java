// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Mutability of a java declaration.
 */
public enum Mutability {
    /**
     * Final, Readonly, Constant.
     */
    FINAL,

    /**
     * Mutable
     */
    MUTABLE,
}
