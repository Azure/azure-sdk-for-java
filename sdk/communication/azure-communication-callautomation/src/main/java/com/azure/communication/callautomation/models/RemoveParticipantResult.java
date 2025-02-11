// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.RemoveParticipantResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.RemoveParticipantResponseInternal;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.RemoveParticipantFailed;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceeded;
import com.azure.core.annotation.Immutable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/** The RemoveParticipantResult model. */
@Immutable
public final class RemoveParticipantResult extends ResultWithEventHandling<RemoveParticipantEventResult> {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        RemoveParticipantResponseConstructorProxy
            .setAccessor(new RemoveParticipantResponseConstructorProxy.RemoveParticipantResponseConstructorAccessor() {
                @Override
                public RemoveParticipantResult create(RemoveParticipantResponseInternal internalHeaders) {
                    return new RemoveParticipantResult(internalHeaders);
                }
            });
    }

    /**
     * Initializes a new instance of RemoveParticipantResult.
     */
    public RemoveParticipantResult() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  removeParticipantResponseInternal The response from the service
     */
    RemoveParticipantResult(RemoveParticipantResponseInternal removeParticipantResponseInternal) {
        Objects.requireNonNull(removeParticipantResponseInternal, "removeParticipantResponseInternal must not be null");

        this.operationContext = removeParticipantResponseInternal.getOperationContext();
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    @Override
    public Mono<RemoveParticipantEventResult> waitForEventProcessorAsync(Duration timeout) {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return (timeout == null
            ? eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == RemoveParticipantSucceeded.class
                        || event.getClass() == RemoveParticipantFailed.class))
            : eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == RemoveParticipantSucceeded.class
                        || event.getClass() == RemoveParticipantFailed.class),
                    timeout)).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    @Override
    protected RemoveParticipantEventResult getReturnedEvent(CallAutomationEventBase event) {
        RemoveParticipantEventResult result = null;

        if (event.getClass() == RemoveParticipantSucceeded.class) {
            result = new RemoveParticipantEventResult(true, (RemoveParticipantSucceeded) event, null,
                ((RemoveParticipantSucceeded) event).getParticipant());
        } else if (event.getClass() == RemoveParticipantFailed.class) {
            result = new RemoveParticipantEventResult(false, null, (RemoveParticipantFailed) event,
                ((RemoveParticipantFailed) event).getParticipant());
        }

        return result;
    }
}
