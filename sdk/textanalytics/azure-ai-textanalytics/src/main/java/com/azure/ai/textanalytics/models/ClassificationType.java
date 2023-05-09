// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for ClassificationType. */
public final class ClassificationType extends ExpandableStringEnum<ClassificationType> {
    /** Static value Single for ClassificationType. */
    public static final ClassificationType SINGLE = fromString("Single");

    /** Static value Multi for ClassificationType. */
    public static final ClassificationType MULTI = fromString("Multi");

    /**
     * Creates or finds a ClassificationType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ClassificationType.
     */
    public static ClassificationType fromString(String name) {
        return fromString(name, ClassificationType.class);
    }
}
