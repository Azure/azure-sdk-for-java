// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** The AddParticipantsResponse model. */
@Immutable
public final class AddParticipantsResponse {
    /*
     * The participants property.
     */
    private List<CallParticipant> participants;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    /**
     * Public constructor.
     *
     */
    public AddParticipantsResponse() {
        this.participants = null;
        this.operationContext = null;
    }

    /**
     * Constructor of the class
     *
     * @param addParticipantsResponseInternal The response from the addParticipant service
     */
    AddParticipantsResponse(AddParticipantsResponseInternal addParticipantsResponseInternal) {
        Objects.requireNonNull(addParticipantsResponseInternal, "addParticipantsResponseInternal must not be null");

        this.participants = addParticipantsResponseInternal.getParticipants()
            .stream()
            .map(CallParticipantConverter::convert)
            .collect(Collectors.toList());
        this.operationContext = addParticipantsResponseInternal.getOperationContext();
    }

    /**
     * Get the participants property: The participants property.
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
}
