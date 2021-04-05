// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Interface for adding java statements to a scope.
 */
public interface StatementAdder {
    /**
     * Adds a {@link JavaStatement} to the scope.
     *
     * @param javaStatement The {@link JavaStatement} to add.
     */
    void addStatement(JavaStatement javaStatement);
}
