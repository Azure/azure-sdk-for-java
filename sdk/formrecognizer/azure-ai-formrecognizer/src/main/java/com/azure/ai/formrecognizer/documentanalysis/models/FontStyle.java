// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Font style. */
public final class FontStyle extends ExpandableStringEnum<FontStyle> {
    /** Static value normal for FontStyle. */
    public static final FontStyle NORMAL = fromString("normal");

    /** Static value italic for FontStyle. */
    public static final FontStyle ITALIC = fromString("italic");

    /**
     * Creates a new instance of FontStyle value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public FontStyle() {}

    /**
     * Creates or finds a FontStyle from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding FontStyle.
     */
    public static FontStyle fromString(String name) {
        return fromString(name, FontStyle.class);
    }

    /**
     * Gets known FontStyle values.
     *
     * @return known FontStyle values.
     */
    public static Collection<FontStyle> values() {
        return values(FontStyle.class);
    }
}
