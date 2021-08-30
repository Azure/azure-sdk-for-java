// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The category label enum values.
 */
@Immutable
public final class DocumentCategory extends ExpandableStringEnum<DocumentCategory> {
    /**
     * Enum value for politics.
     */
    public static final DocumentCategory POLITICS = fromString("Politics");

    /**
     * Creates or finds a {@link DocumentCategory} from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding {@link DocumentCategory}.
     */
    @JsonCreator
    public static DocumentCategory fromString(String name) {
        return fromString(name, DocumentCategory.class);
    }
}
