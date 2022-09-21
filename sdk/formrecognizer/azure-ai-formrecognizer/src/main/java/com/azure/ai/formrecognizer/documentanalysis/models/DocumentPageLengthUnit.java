// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentPageLengthUnit. */
@Immutable
public final class DocumentPageLengthUnit extends ExpandableStringEnum<DocumentPageLengthUnit> {
    /** Static value pixel for DocumentPageLengthUnit. */
    public static final DocumentPageLengthUnit PIXEL = fromString("pixel");

    /** Static value inch for DocumentPageLengthUnit. */
    public static final DocumentPageLengthUnit INCH = fromString("inch");

    /**
     * Creates or finds a DocumentPageLengthUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentPageLengthUnit.
     */
    public static DocumentPageLengthUnit fromString(String name) {
        return fromString(name, DocumentPageLengthUnit.class);
    }

    /** @return known DocumentPageLengthUnit values. */
    public static Collection<DocumentPageLengthUnit> values() {
        return values(DocumentPageLengthUnit.class);
    }
}
