// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for ModelOperationKind. */
public final class ModelOperationKind extends ExpandableStringEnum<ModelOperationKind> {
    /** Static value documentModelBuild for ModelOperationKind. */
    public static final ModelOperationKind DOCUMENT_MODEL_BUILD = fromString("documentModelBuild");

    /** Static value documentModelCompose for ModelOperationKind. */
    public static final ModelOperationKind DOCUMENT_MODEL_COMPOSE = fromString("documentModelCompose");

    /** Static value documentModelCopyTo for ModelOperationKind. */
    public static final ModelOperationKind DOCUMENT_MODEL_COPY_TO = fromString("documentModelCopyTo");

    /**
     * Creates or finds a ModelOperationKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ModelOperationKind.
     */
    public static ModelOperationKind fromString(String name) {
        return fromString(name, ModelOperationKind.class);
    }

    /** @return known ModelOperationKind values. */
    public static Collection<ModelOperationKind> values() {
        return values(ModelOperationKind.class);
    }
}
