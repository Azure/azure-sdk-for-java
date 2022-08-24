// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentOperationKind. */
@Immutable
public final class DocumentOperationKind extends ExpandableStringEnum<DocumentOperationKind> {
    /** Static value documentModelBuild for DocumentOperationKind. */
    public static final DocumentOperationKind BUILD = fromString("documentModelBuild");

    /** Static value documentModelCompose for DocumentOperationKind. */
    public static final DocumentOperationKind COMPOSE = fromString("documentModelCompose");

    /** Static value documentModelCopyTo for DocumentOperationKind. */
    public static final DocumentOperationKind COPY_TO = fromString("documentModelCopyTo");

    /**
     * Creates or finds a DocumentOperationKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentOperationKind.
     */
    public static DocumentOperationKind fromString(String name) {
        return fromString(name, DocumentOperationKind.class);
    }

    /** @return known DocumentOperationKind values. */
    public static Collection<DocumentOperationKind> values() {
        return values(DocumentOperationKind.class);
    }
}
