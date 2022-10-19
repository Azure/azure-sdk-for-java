// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentSelectionMarkState. */
@Immutable
public final class DocumentSelectionMarkState extends ExpandableStringEnum<DocumentSelectionMarkState> {
    /** Static value selected for DocumentSelectionMarkState. */
    public static final DocumentSelectionMarkState SELECTED = fromString("selected");

    /** Static value unselected for DocumentSelectionMarkState. */
    public static final DocumentSelectionMarkState UNSELECTED = fromString("unselected");

    /**
     * Creates or finds a DocumentSelectionMarkState from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentSelectionMarkState.
     */
    public static DocumentSelectionMarkState fromString(String name) {
        return fromString(name, DocumentSelectionMarkState.class);
    }

    /** @return known DocumentSelectionMarkState values. */
    public static Collection<DocumentSelectionMarkState> values() {
        return values(DocumentSelectionMarkState.class);
    }
}
