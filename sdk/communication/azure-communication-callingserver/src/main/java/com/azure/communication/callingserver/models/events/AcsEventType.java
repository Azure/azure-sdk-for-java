// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.models.AcsEventTypeInternal;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for AcsEventType. */
public final class AcsEventType extends ExpandableStringEnum<AcsEventType> {
    /** Static value unknown for AcsEventType. */
    public static final AcsEventType UNKNOWN = fromString(AcsEventTypeInternal.UNKNOWN.toString());

    /** Static value callConnected for AcsEventType. */
    public static final AcsEventType CALL_CONNECTED = fromString(AcsEventTypeInternal.CALL_CONNECTED.toString());

    /** Static value callDisconnected for AcsEventType. */
    public static final AcsEventType CALL_DISCONNECTED = fromString(AcsEventTypeInternal.CALL_DISCONNECTED.toString());

    /** Static value callTransferAccepted for AcsEventType. */
    public static final AcsEventType CALL_TRANSFER_ACCEPTED = fromString(AcsEventTypeInternal.CALL_TRANSFER_ACCEPTED.toString());

    /** Static value callTransferFailed for AcsEventType. */
    public static final AcsEventType CALL_TRANSFER_FAILED = fromString(AcsEventTypeInternal.CALL_TRANSFER_FAILED.toString());

    /** Static value addParticipantSucceeded for AcsEventType. */
    public static final AcsEventType ADD_PARTICIPANTS_SUCCEEDED = fromString(AcsEventTypeInternal.ADD_PARTICIPANTS_SUCCEEDED.toString());

    /** Static value addParticipantFailed for AcsEventType. */
    public static final AcsEventType ADD_PARTICIPANTS_FAILED = fromString(AcsEventTypeInternal.ADD_PARTICIPANTS_FAILED.toString());

    /** Static value participantsUpdated for AcsEventType. */
    public static final AcsEventType PARTICIPANTS_UPDATED = fromString(AcsEventTypeInternal.PARTICIPANTS_UPDATED.toString());

    /** Static value IncomingCallEvent for AcsEventType. */
    public static final AcsEventType INCOMING_CALL_EVENT = fromString("Microsoft.Communication.IncomingCall");

    /** Static value SubscriptionValidationEvent for AcsEventType. */
    public static final AcsEventType SUBSCRIPTION_VALIDATION_EVENT = fromString("Microsoft.EventGrid.SubscriptionValidationEvent");

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
