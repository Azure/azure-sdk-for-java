// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.PlayCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCanceled;
import com.azure.core.annotation.Immutable;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The result of answering a call
 */
@Immutable
public final class CancelAllMediaOperationsResult extends ResultWithEventHandling<CancelAllMediaOperationsEventResult>  {

    /**
     * Initializes a new instance of CancelAllMediaOperationsResult.
     */
    public CancelAllMediaOperationsResult() {
    }

    /**
     * Waits for the event processor to process the event
     *
     * @return the result of the event processing
     */
    @Override
    public Mono<CancelAllMediaOperationsEventResult> waitForEventProcessorAsync() {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return eventProcessor.waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
            && (Objects.equals(event.getOperationContext(), operationContextFromRequest) || operationContextFromRequest == null)
            && (event.getClass() == PlayCanceled.class || event.getClass() == RecognizeCanceled.class)
        ).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    /**
     * Sets the returned event
     *
     * @param event the event to set
     * @return the result of the event processing
     */
    @Override
    protected CancelAllMediaOperationsEventResult getReturnedEvent(CallAutomationEventBase event) {
        CancelAllMediaOperationsEventResult result = null;
        if (event.getClass() == PlayCanceled.class) {
            result = new CancelAllMediaOperationsEventResult(true, (PlayCanceled) event, null);
        } else if (event.getClass() == RecognizeCanceled.class) {
            result = new CancelAllMediaOperationsEventResult(true, null, (RecognizeCanceled) event);
        }

        return result;
    }
}
