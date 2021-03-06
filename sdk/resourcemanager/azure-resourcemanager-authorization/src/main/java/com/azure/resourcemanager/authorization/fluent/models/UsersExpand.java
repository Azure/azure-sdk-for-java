// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for UsersExpand. */
public final class UsersExpand extends ExpandableStringEnum<UsersExpand> {
    /** Static value * for UsersExpand. */
    public static final UsersExpand ASTERISK = fromString("*");

    /** Static value masterCategories for UsersExpand. */
    public static final UsersExpand MASTER_CATEGORIES = fromString("masterCategories");

    /**
     * Creates or finds a UsersExpand from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding UsersExpand.
     */
    @JsonCreator
    public static UsersExpand fromString(String name) {
        return fromString(name, UsersExpand.class);
    }

    /** @return known UsersExpand values. */
    public static Collection<UsersExpand> values() {
        return values(UsersExpand.class);
    }
}
