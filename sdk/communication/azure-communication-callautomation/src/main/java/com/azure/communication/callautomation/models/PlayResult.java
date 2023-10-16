// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.PlayFailed;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The result of a play operation.
 */
public class PlayResult extends ResultWithEventHandling<PlayEventResult> {

    /**
     * Initializes a new instance of PlayResult.
     */
    public PlayResult() {}

    /**
     * Waits for the event processor to process the event
     *
     * @return the result of the event processing
     */
    @Override
    public Mono<PlayEventResult> waitForEventProcessorAsync() {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return eventProcessor.waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
            && (Objects.equals(event.getOperationContext(), operationContextFromRequest) || operationContextFromRequest == null)
            && (event.getClass() == PlayCompleted.class || event.getClass() == PlayFailed.class)
        ).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    /**
     * Sets the returned event
     *
     * @param event the event to set
     * @return the result of the event processing
     */
    @Override
    protected PlayEventResult getReturnedEvent(CallAutomationEventBase event) {
        PlayEventResult result = null;

        if (event.getClass() == PlayCompleted.class) {
            result = new PlayEventResult(true, (PlayCompleted) event, null);
        } else if (event.getClass() == PlayFailed.class) {
            result = new PlayEventResult(false, null, (PlayFailed) event);
        }

        return result;
    }
}
