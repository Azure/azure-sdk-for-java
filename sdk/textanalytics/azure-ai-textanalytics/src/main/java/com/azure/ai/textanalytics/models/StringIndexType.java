// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * The {@link StringIndexType} model. The default value will be UTF16CODE_UNIT for Java.
 */
public final class StringIndexType extends ExpandableStringEnum<StringIndexType> {
    /** Static value TextElements_v8 for StringIndexType. */
    public static final StringIndexType TEXT_ELEMENTS_V8 = fromString("TextElements_v8");

    /** Static value UnicodeCodePoint for StringIndexType. */
    public static final StringIndexType UNICODE_CODE_POINT = fromString("UnicodeCodePoint");

    /** Static value Utf16CodeUnit for StringIndexType. */
    public static final StringIndexType UTF16CODE_UNIT = fromString("Utf16CodeUnit");

    /**
     * Creates or finds a StringIndexType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding StringIndexType.
     */
    public static StringIndexType fromString(String name) {
        return fromString(name, StringIndexType.class);
    }
}
