// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for DocumentPageKind. */
public final class DocumentPageKind extends ExpandableStringEnum<DocumentPageKind> {
    /** Static value document for DocumentPageKind. */
    public static final DocumentPageKind DOCUMENT = fromString("document");

    /** Static value sheet for DocumentPageKind. */
    public static final DocumentPageKind SHEET = fromString("sheet");

    /** Static value slide for DocumentPageKind. */
    public static final DocumentPageKind SLIDE = fromString("slide");

    /** Static value image for DocumentPageKind. */
    public static final DocumentPageKind IMAGE = fromString("image");

    /**
     * Creates or finds a DocumentPageKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentPageKind.
     */
    @JsonCreator
    public static DocumentPageKind fromString(String name) {
        return fromString(name, DocumentPageKind.class);
    }

    /**
     * Gets known DocumentPageKind values.
     *
     * @return known DocumentPageKind values.
     */
    public static Collection<DocumentPageKind> values() {
        return values(DocumentPageKind.class);
    }
}
