// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentModelBuildMode. */
public final class DocumentModelBuildMode extends ExpandableStringEnum<DocumentModelBuildMode> {

    /** Static value template for DocumentModelBuildMode.
     * Used for documents with fixed visual templates.
     */
    public static final DocumentModelBuildMode TEMPLATE = fromString("template");

    /** Static value neural for DocumentModelBuildMode.
     * Used for English documents with diverse visual templates.
     */
    public static final DocumentModelBuildMode NEURAL = fromString("neural");

    /**
     * Creates or finds a DocumentModelBuildMode from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentModelBuildMode.
     */
    public static DocumentModelBuildMode fromString(String name) {
        return fromString(name, DocumentModelBuildMode.class);
    }

    /** @return known DocumentModelBuildMode values. */
    public static Collection<DocumentModelBuildMode> values() {
        return values(DocumentModelBuildMode.class);
    }
}
