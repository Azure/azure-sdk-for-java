// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.CallConnection;
import com.azure.communication.callautomation.CallConnectionAsync;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CreateCallFailed;
import com.azure.core.annotation.Immutable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * The result of answering a call
 */
@Immutable
public final class CreateCallResult extends CallResult {

    /**
     * Constructor
     *
     * @param callConnectionProperties The callConnectionProperties
     * @param callConnection The callConnection
     * @param callConnectionAsync The callConnectionAsync
     */
    public CreateCallResult(CallConnectionProperties callConnectionProperties, CallConnection callConnection,
        CallConnectionAsync callConnectionAsync) {
        super(callConnectionProperties, callConnection, callConnectionAsync);
    }

    /**
     * Waits for the event processor to process the event
     *
     * @return the result of the event processing
     */
    public CreateCallEventResult waitForEventProcessor() {
        return waitForEventProcessorAsync().block();
    }

    /**
     * Waits for the event processor to process the event
     *
     * @param timeout the timeout
     * @return the result of the event processing
     */
    public CreateCallEventResult waitForEventProcessor(Duration timeout) {
        return waitForEventProcessorAsync(timeout).block();
    }

    /**
     * Waits for the event processor to process the event
     *
     * @return the result of the event processing
     */
    public Mono<CreateCallEventResult> waitForEventProcessorAsync() {
        return waitForEventProcessorAsync(null);
    }

    /**
     * Waits for the event processor to process the event
     *
     * @param timeout the timeout
     * @return the result of the event processing
     */
    public Mono<CreateCallEventResult> waitForEventProcessorAsync(Duration timeout) {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return (timeout == null
            ? eventProcessor
                .waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == CallConnected.class || event.getClass() == CreateCallFailed.class))
            : eventProcessor.waitForEventProcessorAsync(
                event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
                    && (Objects.equals(event.getOperationContext(), operationContextFromRequest)
                        || operationContextFromRequest == null)
                    && (event.getClass() == CallConnected.class || event.getClass() == CreateCallFailed.class),
                timeout)).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    /**
     * Sets the returned event
     *
     * @param event the event to set
     * @return the result of the event processing
     */
    private CreateCallEventResult getReturnedEvent(CallAutomationEventBase event) {
        if (event.getClass() == CallConnected.class) {
            return new CreateCallEventResult(true, (CallConnected) event, null);
        }

        if (event.getClass() == CreateCallFailed.class) {
            return new CreateCallEventResult(false, null, (CreateCallFailed) event);
        }

        return null;
    }
}
