// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.TransferCallResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.TransferCallResponseInternal;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallTransferAccepted;
import com.azure.communication.callautomation.models.events.CallTransferFailed;
import com.azure.core.annotation.Immutable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/** The TransferCallResult model. */
@Immutable
public final class TransferCallResult extends ResultWithEventHandling<TransferCallToParticipantEventResult> {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        TransferCallResponseConstructorProxy
            .setAccessor(new TransferCallResponseConstructorProxy.TransferCallResponseConstructorAccessor() {
                @Override
                public TransferCallResult create(TransferCallResponseInternal internalHeaders) {
                    return new TransferCallResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public TransferCallResult() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param transferCallResponseInternal The response from the service.
     */
    TransferCallResult(TransferCallResponseInternal transferCallResponseInternal) {
        Objects.requireNonNull(transferCallResponseInternal, "transferCallResponseInternal must not be null");

        this.operationContext = transferCallResponseInternal.getOperationContext();
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
    public Mono<TransferCallToParticipantEventResult> waitForEventProcessorAsync(Duration timeout) {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return (timeout == null
            ? eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == CallTransferAccepted.class || event.getClass() == CallTransferFailed.class))
            : eventProcessor.waitForEventProcessorAsync(
                event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == CallTransferAccepted.class || event.getClass() == CallTransferFailed.class),
                timeout)).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    @Override
    protected TransferCallToParticipantEventResult getReturnedEvent(CallAutomationEventBase event) {
        TransferCallToParticipantEventResult result = null;

        if (event.getClass() == CallTransferAccepted.class) {
            result = new TransferCallToParticipantEventResult(true, (CallTransferAccepted) event, null);
        } else if (event.getClass() == CallTransferFailed.class) {
            result = new TransferCallToParticipantEventResult(false, null, (CallTransferFailed) event);
        }

        return result;
    }
}
