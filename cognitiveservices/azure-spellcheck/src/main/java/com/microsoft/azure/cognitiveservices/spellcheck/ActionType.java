/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.spellcheck;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ActionType.
 */
public final class ActionType extends ExpandableStringEnum<ActionType> {
    /** Static value Edit for ActionType. */
    public static final ActionType EDIT = fromString("Edit");

    /** Static value Load for ActionType. */
    public static final ActionType LOAD = fromString("Load");

    /**
     * Creates or finds a ActionType from its string representation.
     * @param name a name to look for
     * @return the corresponding ActionType
     */
    @JsonCreator
    public static ActionType fromString(String name) {
        return fromString(name, ActionType.class);
    }

    /**
     * @return known ActionType values
     */
    public static Collection<ActionType> values() {
        return values(ActionType.class);
    }
}
