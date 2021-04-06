// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.codegen;

/**
 * Indicates violation of style rules.
 */
public class StyleException extends Exception {

    /**
     * Initializes a new instance of the {@link StyleException} class.
     *
     * @param message An explanation of the failure.
     */
    public StyleException(String message) {
        super(message);
    }
}
