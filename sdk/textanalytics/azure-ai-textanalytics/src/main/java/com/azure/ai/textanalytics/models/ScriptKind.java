// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for ScriptKind. */
public final class ScriptKind extends ExpandableStringEnum<ScriptKind> {
    /** Static value Latin for ScriptKind. */
    public static final ScriptKind LATIN = fromString("Latin");

    /**
     * Creates or finds a ScriptKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ScriptKind.
     */
    public static ScriptKind fromString(String name) {
        return fromString(name, ScriptKind.class);
    }
}
