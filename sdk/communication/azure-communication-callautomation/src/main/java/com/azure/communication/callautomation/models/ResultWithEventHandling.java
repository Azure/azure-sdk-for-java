// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.eventprocessor.CallAutomationEventProcessor;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import reactor.core.publisher.Mono;

/**
 * Injects the event handling into the result
 */
public abstract class ResultWithEventHandling<TEventResult> {
    protected CallAutomationEventProcessor eventProcessor;
    protected String callConnectionId;
    protected String operationContextFromRequest;

    /**
     * Sets the event processor
     *
     * @param eventProcessor the event processor
     * @param callConnectionId the call connection id
     * @param operationContext the operation context
     */
    public void setEventProcessor(CallAutomationEventProcessor eventProcessor, String callConnectionId, String operationContext) {
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
     * @return the result of the event processing
     */
    public abstract Mono<TEventResult> waitForEventProcessorAsync();

    /**
     * Sets the returned event
     *
     * @param event the event to set
     * @return the result of the event processing
     */
    protected abstract TEventResult getReturnedEvent(CallAutomationEventBase event);
}
