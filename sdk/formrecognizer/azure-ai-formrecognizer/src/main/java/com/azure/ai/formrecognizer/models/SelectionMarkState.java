// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for SelectionMarkState. i.e., Selected or Unselected.
 */
public final class SelectionMarkState extends ExpandableStringEnum<SelectionMarkState> {
    /**
     * Static value SELECTED for SelectionMarkState.
     */
    public static final SelectionMarkState SELECTED = fromString("selected");

    /**
     * Static value UNSELECTED for SelectionMarkState.
     */
    public static final SelectionMarkState UNSELECTED = fromString("unselected");

    /**
     * Creates or finds a SelectionMarkState from its string representation.
     *
     * @param value a value to look for.
     *
     * @return the corresponding SelectionMarkState.
     */
    public static SelectionMarkState fromString(String value) {
        return fromString(value, SelectionMarkState.class);
    }
}
