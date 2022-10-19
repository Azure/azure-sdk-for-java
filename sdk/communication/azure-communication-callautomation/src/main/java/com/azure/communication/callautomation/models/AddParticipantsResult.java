// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.AddParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.models.AddParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** The AddParticipantsResult model. */
@Immutable
public final class AddParticipantsResult {
    /*
     * The participants property.
     */
    private final List<CallParticipant> participants;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        AddParticipantsResponseConstructorProxy.setAccessor(
            new AddParticipantsResponseConstructorProxy.AddParticipantsResponseConstructorAccessor() {
                @Override
                public AddParticipantsResult create(AddParticipantsResponseInternal internalHeaders) {
                    return new AddParticipantsResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public AddParticipantsResult() {
        this.participants = null;
        this.operationContext = null;
    }

    /**
     * Constructor of the class
     *
     * @param addParticipantsResponseInternal The response from the addParticipant service
     */
    AddParticipantsResult(AddParticipantsResponseInternal addParticipantsResponseInternal) {
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
