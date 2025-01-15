// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.CancelAddParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.CancelAddParticipantResponseConstructorProxy.CancelAddParticipantResponseConstructorAccessor;
import com.azure.communication.callautomation.implementation.models.CancelAddParticipantResponse;
import com.azure.communication.callautomation.models.events.CancelAddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CancelAddParticipantFailed;
import com.azure.core.annotation.Immutable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/** The CancelAddParticipantResult model. */
@Immutable
public final class CancelAddParticipantOperationResult
    extends ResultWithEventHandling<CancelAddParticipantEventResult> {

    /**
     * The invitation ID used to cancel the add participant request.
     */
    private final String invitationId;

    /**
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        CancelAddParticipantResponseConstructorProxy.setAccessor(new CancelAddParticipantResponseConstructorAccessor() {
            @Override
            public CancelAddParticipantOperationResult create(CancelAddParticipantResponse internalHeaders) {
                return new CancelAddParticipantOperationResult(internalHeaders);
            }
        });
    }

    /**
     * Public constructor.
     */
    public CancelAddParticipantOperationResult() {
        invitationId = null;
        operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param cancelAddParticipantResponseInternal The response from the service.
     */
    CancelAddParticipantOperationResult(CancelAddParticipantResponse cancelAddParticipantResponseInternal) {
        Objects.requireNonNull(cancelAddParticipantResponseInternal,
            "cancelAddParticipantResponseInternal must not be null");

        invitationId = cancelAddParticipantResponseInternal.getInvitationId();
        operationContext = cancelAddParticipantResponseInternal.getOperationContext();
    }

    /**
     * Get the invitationId property: The invitation ID used to cancel the add
     * participant request.
     *
     * @return the invitationId value.
     */
    public String getInvitationId() {
        return invitationId;
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    @Override
    public Mono<CancelAddParticipantEventResult> waitForEventProcessorAsync(Duration timeout) {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return (timeout == null
            ? eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == CancelAddParticipantSucceeded.class
                        || event.getClass() == CancelAddParticipantFailed.class))
            : eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == CancelAddParticipantSucceeded.class
                        || event.getClass() == CancelAddParticipantFailed.class),
                    timeout)).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    @Override
    protected CancelAddParticipantEventResult getReturnedEvent(CallAutomationEventBase event) {
        CancelAddParticipantEventResult result = null;
        if (event.getClass() == CancelAddParticipantSucceeded.class) {
            result = new CancelAddParticipantEventResult(true, (CancelAddParticipantSucceeded) event, null,
                ((CancelAddParticipantSucceeded) event).getInvitationId());
        } else if (event.getClass() == CancelAddParticipantFailed.class) {
            result = new CancelAddParticipantEventResult(false, null, (CancelAddParticipantFailed) event,
                ((CancelAddParticipantFailed) event).getInvitationId());
        }
        return result;
    }
}
