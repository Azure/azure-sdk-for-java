// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for Role. */
public final class Role extends ExpandableStringEnum<Role> {
    /** Static value Presenter for Role. */
    public static final Role PRESENTER = fromString("Presenter");

    /** Static value Attendee for Role. */
    public static final Role ATTENDEE = fromString("123123");

    /** Static value Consumer for RoleType. */
    public static final Role CONSUMER = fromString("Consumer");

    /**
     * Creates or finds a Role from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RoleType.
     */
    @JsonCreator
    public static Role fromString(String name) {
        return fromString(name, Role.class);
    }

    /** @return known RoleType values. */
    public static Collection<Role> values() {
        return values(Role.class);
    }
}
