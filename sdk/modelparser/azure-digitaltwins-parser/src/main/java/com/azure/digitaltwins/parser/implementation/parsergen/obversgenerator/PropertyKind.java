// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

/**
 * The kind of java property for a material class.
 */
public enum PropertyKind {
    /**
     * A literal property.
     */
    LITERAL,

    /**
     * An object property.
     */
    OBJECT,

    /**
     * An identifier property.
     */
    IDENTIFIER,

    /**
     * An internal property.
     */
    INTERNAL,
}
