// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for ElementType.
 */
public final class ElementType extends ExpandableStringEnum<ElementType> {

    /**
     * Static value Line for ElementType.
     */
    public static final ElementType LINE = fromString("Line");

    /**
     * Static value Word for ElementType.
     */
    public static final ElementType WORD = fromString("Word");

    /**
     * Creates or finds a ElementType from its string representation.
     *
     * @param name a name to look for.
     *
     * @return the corresponding ElementType.
     */
    public static ElementType fromString(String name) {
        return fromString(name, ElementType.class);
    }
}
