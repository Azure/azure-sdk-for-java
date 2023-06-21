// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for OperationKind. */
@Immutable
public final class OperationKind extends ExpandableStringEnum<OperationKind> {
    /** Static value documentModelBuild for OperationKind. */
    public static final OperationKind DOCUMENT_MODEL_BUILD = fromString("documentModelBuild");

    /** Static value documentModelCompose for OperationKind. */
    public static final OperationKind DOCUMENT_MODEL_COMPOSE = fromString("documentModelCompose");

    /** Static value documentModelCopyTo for OperationKind. */
    public static final OperationKind DOCUMENT_MODEL_COPY_TO = fromString("documentModelCopyTo");

    /**
     * Creates or finds a OperationKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding OperationKind.
     */
    public static OperationKind fromString(String name) {
        return fromString(name, OperationKind.class);
    }

    /** @return known OperationKind values. */
    public static Collection<OperationKind> values() {
        return values(OperationKind.class);
    }
}
