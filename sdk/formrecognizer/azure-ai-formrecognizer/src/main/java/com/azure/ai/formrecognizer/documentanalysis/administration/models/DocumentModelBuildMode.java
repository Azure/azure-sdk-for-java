// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Class describing the custom document build mode values */
@Immutable
public final class DocumentModelBuildMode extends ExpandableStringEnum<DocumentModelBuildMode> {

    /**
     * Creates a DocumentModelBuildMode object.
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public DocumentModelBuildMode() {
    }

    /**
     * Used for documents with fixed visual templates.
     */
    public static final DocumentModelBuildMode TEMPLATE = fromString("template");

    /**
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

    /**
     * Returns known DocumentModelBuildMode values.
     * @return known DocumentModelBuildMode values.
     */
    public static Collection<DocumentModelBuildMode> values() {
        return values(DocumentModelBuildMode.class);
    }
}
