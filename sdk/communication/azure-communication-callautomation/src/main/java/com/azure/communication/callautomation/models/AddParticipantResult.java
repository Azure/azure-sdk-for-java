// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.AddParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.models.AddParticipantResponseInternal;
import com.azure.communication.callautomation.models.events.AddParticipantFailed;
import com.azure.communication.callautomation.models.events.AddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.core.annotation.Immutable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/** The AddParticipantResult model. */
@Immutable
public class AddParticipantResult extends ResultWithEventHandling<AddParticipantEventResult> {
    /*
     * The participant property.
     */
    private final CallParticipant participant;

    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    /*
     * The invitation ID used to send out add participant request.
     */
    private final String invitationId;

    static {
        AddParticipantResponseConstructorProxy
            .setAccessor(new AddParticipantResponseConstructorProxy.AddParticipantResponseConstructorAccessor() {
                @Override
                public AddParticipantResult create(AddParticipantResponseInternal internalHeaders) {
                    return new AddParticipantResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public AddParticipantResult() {
        super();
        this.participant = null;
        this.operationContext = null;
        this.invitationId = null;
    }

    /**
     * Constructor of the class
     *
     * @param addParticipantResponseInternal The response from the addParticipant service
     */
    AddParticipantResult(AddParticipantResponseInternal addParticipantResponseInternal) {
        super();
        Objects.requireNonNull(addParticipantResponseInternal, "addParticipantResponseInternal must not be null");

        this.participant = CallParticipantConverter.convert(addParticipantResponseInternal.getParticipant());
        this.operationContext = addParticipantResponseInternal.getOperationContext();
        this.invitationId = addParticipantResponseInternal.getInvitationId();
    }

    /**
     * Get the participant property: The participant property.
     *
     * @return the participant value.
     */
    public CallParticipant getParticipant() {
        return this.participant;
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
     * Get the invitationId property: The invitation ID used to send out add
     * participant request.
     *
     * @return the invitationId value.
     */
    public String getInvitationId() {
        return invitationId;
    }

    @Override
    public Mono<AddParticipantEventResult> waitForEventProcessorAsync(Duration timeout) {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return (timeout == null
            ? eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == AddParticipantSucceeded.class
                        || event.getClass() == AddParticipantFailed.class))
            : eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == AddParticipantSucceeded.class
                        || event.getClass() == AddParticipantFailed.class),
                    timeout)).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    @Override
    protected AddParticipantEventResult getReturnedEvent(CallAutomationEventBase event) {
        AddParticipantEventResult result = null;
        if (event.getClass() == AddParticipantSucceeded.class) {
            result = new AddParticipantEventResult(true, (AddParticipantSucceeded) event, null,
                ((AddParticipantSucceeded) event).getParticipant());
        } else if (event.getClass() == AddParticipantFailed.class) {
            result = new AddParticipantEventResult(false, null, (AddParticipantFailed) event,
                ((AddParticipantFailed) event).getParticipant());
        }

        return result;
    }
}
