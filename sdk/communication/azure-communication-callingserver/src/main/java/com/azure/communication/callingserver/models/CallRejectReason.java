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
     * Creates a new instance of {@link CallRejectReason} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CallRejectReason} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
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
     * Gets known CallRejectReason values.
     *
     * @return known CallRejectReason values.
     */
    public static Collection<CallRejectReason> values() {
        return values(CallRejectReason.class);
    }
}
