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
     * Creates a new instance of {@link CallLocatorKind} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CallLocatorKind} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public CallLocatorKind() {
    }

    /**
     * Creates or finds a CallLocatorKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CallLocatorKind.
     */
    public static CallLocatorKind fromString(String name) {
        return fromString(name, CallLocatorKind.class);
    }

    /**
     * Gets known CallLocatorKind values.
     *
     * @return known CallLocatorKind values.
     */
    public static Collection<CallLocatorKind> values() {
        return values(CallLocatorKind.class);
    }
}
