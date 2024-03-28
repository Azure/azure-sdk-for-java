// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for RecordingKind. */
public class RecordingKind extends ExpandableStringEnum<RecordingKind> {

    /** Static value azureCommunicationServices for RecordingKind. */
    public static final RecordingKind AZURE_COMMUNICATION_SERVICES = fromString("azureCommunicationServices");

    /** Static value teams for RecordingKind. */
    public static final RecordingKind TEAMS = fromString("teams");

    /** Static value teamsCompliance for RecordingKind. */
    public static final RecordingKind TEAMS_COMPLIANCE = fromString("teamsCompliance");

    /** Static value others for RecordingKind. */
    public static final RecordingKind OTHERS = fromString("others");

    /**
     * Creates or finds a RecordingKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecordingKind.
     */
    public static RecordingKind fromString(String name) {
        return fromString(name, RecordingKind.class);
    }

    /** @return known RecordingKind values. */
    public static Collection<RecordingKind> values() {
        return values(RecordingKind.class);
    }
    
}
