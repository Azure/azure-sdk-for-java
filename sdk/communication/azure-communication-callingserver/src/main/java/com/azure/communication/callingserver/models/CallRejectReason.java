// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for CallRejectReason. */
public final class CallRejectReason extends ExpandableStringEnum<CallRejectReason> {
    /** Static value none for CallRejectReason. */
    public static final CallRejectReason NONE = fromString("none");

    /** Static value busy for CallRejectReason. */
    public static final CallRejectReason BUSY = fromString("busy");

    /** Static value forbidden for CallRejectReason. */
    public static final CallRejectReason FORBIDDEN = fromString("forbidden");

    /**
     * Creates an instance of {@link CallRejectReason} with no string value.
     *
     * @deprecated Please use {@link #fromString(String)} to create an instance of CallRejectReason.
     */
    @Deprecated
    public CallRejectReason() {
    }

    /**
     * Creates or finds a CallRejectReason from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CallRejectReason.
     */
    public static CallRejectReason fromString(String name) {
        return fromString(name, CallRejectReason.class);
    }

    /**
     * Get the collection of CallRejectReason values.
     * @return known CallRejectReason values.
     */
    public static Collection<CallRejectReason> values() {
        return values(CallRejectReason.class);
    }
}
