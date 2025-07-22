// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.MoveParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.models.MoveParticipantsResponse;
import com.azure.core.annotation.Immutable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** The MoveParticipantsResult model. */
@Immutable
public class MoveParticipantsResult {
    /*
     * The participants moved.
     */
    private final List<CallParticipant> participants;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    /*
     * The CallConnectionId for the call you want to move the participant from
     */
    private final String fromCall;

    static {
        MoveParticipantsResponseConstructorProxy
            .setAccessor(new MoveParticipantsResponseConstructorProxy.MoveParticipantsResponseConstructorAccessor() {
                @Override
                public MoveParticipantsResult create(MoveParticipantsResponse internalHeaders) {
                    return new MoveParticipantsResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public MoveParticipantsResult() {
        super();
        this.participants = null;
        this.operationContext = null;
        this.fromCall = null;
    }

    /**
     * Constructor of the class
     *
     * @param moveParticipantsResponse The response from the moveParticipants service
     */
    MoveParticipantsResult(MoveParticipantsResponse moveParticipantsResponse) {
        super();
        Objects.requireNonNull(moveParticipantsResponse, "moveParticipantsResponse must not be null");

        this.participants = moveParticipantsResponse.getParticipants() != null
            ? moveParticipantsResponse.getParticipants()
                .stream()
                .map(CallParticipantConverter::convert)
                .collect(Collectors.toList())
            : null;
        this.operationContext = moveParticipantsResponse.getOperationContext();
        this.fromCall = moveParticipantsResponse.getFromCall();
    }

    /**
     * Get the participants property: The participants moved.
     *
     * @return the participants value.
     */
    public List<CallParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the fromCall property: The CallConnectionId for the call you want to move the participant from.
     *
     * @return the fromCall value.
     */
    public String getFromCall() {
        return fromCall;
    }
}
