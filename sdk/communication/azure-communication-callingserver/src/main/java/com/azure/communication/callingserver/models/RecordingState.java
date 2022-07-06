// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for RecordingStateInternal. */
public final class RecordingState extends ExpandableStringEnum<RecordingState> {
    /** Static value active for RecordingStateInternal. */
    public static final RecordingState ACTIVE = fromString("active");

    /** Static value inactive for RecordingStateInternal. */
    public static final RecordingState INACTIVE = fromString("inactive");

    /**
     * Creates or finds a RecordingStateInternal from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecordingStateInternal.
     */
    public static RecordingState fromString(String name) {
        return fromString(name, RecordingState.class);
    }

    /** @return known RecordingStateInternal values. */
    public static Collection<RecordingState> values() {
        return values(RecordingState.class);
    }
}
