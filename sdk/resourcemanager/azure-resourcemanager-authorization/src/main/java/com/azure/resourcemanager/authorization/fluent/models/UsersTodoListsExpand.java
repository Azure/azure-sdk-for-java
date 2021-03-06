// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for UsersTodoListsExpand. */
public final class UsersTodoListsExpand extends ExpandableStringEnum<UsersTodoListsExpand> {
    /** Static value * for UsersTodoListsExpand. */
    public static final UsersTodoListsExpand ASTERISK = fromString("*");

    /** Static value extensions for UsersTodoListsExpand. */
    public static final UsersTodoListsExpand EXTENSIONS = fromString("extensions");

    /** Static value linkedResources for UsersTodoListsExpand. */
    public static final UsersTodoListsExpand LINKED_RESOURCES = fromString("linkedResources");

    /**
     * Creates or finds a UsersTodoListsExpand from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding UsersTodoListsExpand.
     */
    @JsonCreator
    public static UsersTodoListsExpand fromString(String name) {
        return fromString(name, UsersTodoListsExpand.class);
    }

    /** @return known UsersTodoListsExpand values. */
    public static Collection<UsersTodoListsExpand> values() {
        return values(UsersTodoListsExpand.class);
    }
}
