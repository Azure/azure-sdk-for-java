// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.CallAutomationEventProcessor;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Injects the event handling into the result
 *
 * @param <TEventResult> the type of the event result
 */
public abstract class ResultWithEventHandling<TEventResult> {
    /**
     * The event processor that handles events.
     */
    protected CallAutomationEventProcessor eventProcessor;
    /**
     * The call's connection id.
     */
    protected String callConnectionId;
    /**
     * Operation context from the api request.
     */
    protected String operationContextFromRequest;

    /**
     * Creates a new instance of {@link ResultWithEventHandling}.
     */
    public ResultWithEventHandling() {
    }

    /**
     * Sets the event processor
     *
     * @param eventProcessor the event processor
     * @param callConnectionId the call connection id
     * @param operationContext the operation context
     */
    public void setEventProcessor(CallAutomationEventProcessor eventProcessor, String callConnectionId,
        String operationContext) {
        this.eventProcessor = eventProcessor;
        this.callConnectionId = callConnectionId;
        this.operationContextFromRequest = operationContext;
    }

    /**
     * Waits for the event processor to process the event
     *
     * @return the result of the event processing
     */
    public TEventResult waitForEventProcessor() {
        return waitForEventProcessorAsync().block();
    }

    /**
     * Waits for the event processor to process the event
     *
     * @param timeout the timeout
     * @return the result of the event processing
     */
    public TEventResult waitForEventProcessor(Duration timeout) {
        return waitForEventProcessorAsync(timeout).block();
    }

    /**
     * Waits for the event processor to process the event
     *
     * @return the result of the event processing
     */
    public Mono<TEventResult> waitForEventProcessorAsync() {
        return waitForEventProcessorAsync(null);
    }

    /**
     * Waits for the event processor to process the event
     *
     * @param timeout the timeout
     * @return the result of the event processing
     */
    public abstract Mono<TEventResult> waitForEventProcessorAsync(Duration timeout);

    /**
     * Sets the returned event
     *
     * @param event the event to set
     * @return the result of the event processing
     */
    protected abstract TEventResult getReturnedEvent(CallAutomationEventBase event);
}
