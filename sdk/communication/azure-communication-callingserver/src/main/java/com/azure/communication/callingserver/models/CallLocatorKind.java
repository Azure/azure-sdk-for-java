// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for CallLocatorKind. */
public final class CallLocatorKind extends ExpandableStringEnum<CallLocatorKind> {
    /** Static value groupCallLocator for CallLocatorKind. */
    public static final CallLocatorKind GROUP_CALL_LOCATOR = fromString("groupCallLocator");

    /** Static value serverCallLocator for CallLocatorKind. */
    public static final CallLocatorKind SERVER_CALL_LOCATOR = fromString("serverCallLocator");

    /**
     * Creates or finds a CallLocatorKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CallLocatorKind.
     */
    public static CallLocatorKind fromString(String name) {
        return fromString(name, CallLocatorKind.class);
    }

    /** @return known CallLocatorKind values. */
    public static Collection<CallLocatorKind> values() {
        return values(CallLocatorKind.class);
    }
}
