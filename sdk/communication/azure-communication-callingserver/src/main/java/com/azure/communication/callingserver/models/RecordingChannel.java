// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for RecordingChannel. */
public final class RecordingChannel extends ExpandableStringEnum<RecordingChannel> {
    /** Static value mixed for RecordingChannel. */
    public static final RecordingChannel MIXED = fromString("mixed");

    /** Static value unmixed for RecordingChannel. */
    public static final RecordingChannel UNMIXED = fromString("unmixed");

    /**
     * Creates a new instance of {@link RecordingChannel} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link RecordingChannel} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public RecordingChannel() {
    }

    /**
     * Creates or finds a RecordingChannel from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecordingChannel.
     */
    public static RecordingChannel fromString(String name) {
        return fromString(name, RecordingChannel.class);
    }

    /**
     * Gets known RecordingChannel values.
     *
     * @return known RecordingChannel values.
     */
    public static Collection<RecordingChannel> values() {
        return values(RecordingChannel.class);
    }
}
