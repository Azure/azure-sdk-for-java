// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Gets or sets the type of PII redaction to be used.
 */
public final class RedactionType extends ExpandableStringEnum<RedactionType> {
    /**
     * Static value maskWithCharacter for RedactionType.
     */
    public static final RedactionType MASK_WITH_CHARACTER = fromString("maskWithCharacter");

    /**
     * Creates a new instance of RedactionType value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public RedactionType() {
    }

    /**
     * Creates or finds a RedactionType from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding RedactionType.
     */
    public static RedactionType fromString(String name) {
        return fromString(name, RedactionType.class);
    }

    /**
     * Gets known RedactionType values.
     * 
     * @return known RedactionType values.
     */
    public static Collection<RedactionType> values() {
        return values(RedactionType.class);
    }
}
