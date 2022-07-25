// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for AcsEventType. */
public final class AcsEventType extends ExpandableStringEnum<AcsEventType> {
    /** Static value unknown for AcsEventType. */
    public static final AcsEventType UNKNOWN = fromString("unknown");

    /** Static value callConnected for AcsEventType. */
    public static final AcsEventType CALL_CONNECTED = fromString("callConnected");

    /** Static value callDisconnected for AcsEventType. */
    public static final AcsEventType CALL_DISCONNECTED = fromString("callDisconnected");

    /** Static value callTransferAccepted for AcsEventType. */
    public static final AcsEventType CALL_TRANSFER_ACCEPTED = fromString("callTransferAccepted");

    /** Static value callTransferFailed for AcsEventType. */
    public static final AcsEventType CALL_TRANSFER_FAILED = fromString("callTransferFailed");

    /** Static value addParticipantSucceeded for AcsEventType. */
    public static final AcsEventType ADD_PARTICIPANT_SUCCEEDED = fromString("addParticipantSucceeded");

    /** Static value addParticipantFailed for AcsEventType. */
    public static final AcsEventType ADD_PARTICIPANT_FAILED = fromString("addParticipantFailed");

    /** Static value participantsUpdated for AcsEventType. */
    public static final AcsEventType PARTICIPANTS_UPDATED = fromString("participantsUpdated");

    /**
     * Creates or finds a AcsEventType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AcsEventType.
     */
    @JsonCreator
    public static AcsEventType fromString(String name) {
        return fromString(name, AcsEventType.class);
    }

    /** @return known AcsEventType values. */
    public static Collection<AcsEventType> values() {
        return values(AcsEventType.class);
    }
}
