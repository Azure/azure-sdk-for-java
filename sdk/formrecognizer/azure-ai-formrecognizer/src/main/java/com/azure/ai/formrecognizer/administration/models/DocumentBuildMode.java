// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentBuildMode. */
public final class DocumentBuildMode extends ExpandableStringEnum<DocumentBuildMode> {

    /** Static value template for DocumentBuildMode.
     * Used for documents with fixed visual templates.
     */
    public static final DocumentBuildMode TEMPLATE = fromString("template");

    /** Static value neural for DocumentBuildMode.
     * Used for English documents with diverse visual templates.
     */
    public static final DocumentBuildMode NEURAL = fromString("neural");

    /**
     * Creates or finds a DocumentBuildMode from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentBuildMode.
     */
    public static DocumentBuildMode fromString(String name) {
        return fromString(name, DocumentBuildMode.class);
    }

    /** @return known DocumentBuildMode values. */
    public static Collection<DocumentBuildMode> values() {
        return values(DocumentBuildMode.class);
    }
}
