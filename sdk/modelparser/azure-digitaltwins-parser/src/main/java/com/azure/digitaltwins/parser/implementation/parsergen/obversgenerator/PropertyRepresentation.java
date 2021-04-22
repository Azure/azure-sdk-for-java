// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen.obversgenerator;

/**
 * The kind of java property for a material class.
 */
public enum PropertyRepresentation {
    /**
     * Property is represented as a singular item.
     */
    ITEM,

    /**
     * Property is represented as a singular item.
     */
    NULLABLE_ITEM,

    /**
     * Property is represented as a list.
     */
    LIST,

    /**
     * Property is represented as a map.
     */
    MAP,
}
