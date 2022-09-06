// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for RecordingChannel. */
public final class RecordingChannel extends ExpandableStringEnum<RecordingChannel> {
    /** Static value mixed for RecordingChannel. */
    public static final RecordingChannel MIXED = fromString("mixed");

    /** Static value unmixed for RecordingChannel. */
    public static final RecordingChannel UNMIXED = fromString("unmixed");

    /**
     * Creates or finds a RecordingChannel from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecordingChannel.
     */
    public static RecordingChannel fromString(String name) {
        return fromString(name, RecordingChannel.class);
    }

    /** @return known RecordingChannel values. */
    public static Collection<RecordingChannel> values() {
        return values(RecordingChannel.class);
    }
}
