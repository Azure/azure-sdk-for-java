// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Result of the start recognizing call media operation
 */
public class StartRecognizingCallMediaResult extends ResultWithEventHandling<StartRecognizingEventResult> {
    /**
     * Initializes a new instance of StartRecognizingCallMediaResult.
     */
    public StartRecognizingCallMediaResult() {}

    @Override
    public Mono<StartRecognizingEventResult> waitForEventProcessorAsync() {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return eventProcessor.waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
            && (Objects.equals(event.getOperationContext(), operationContextFromRequest) || operationContextFromRequest == null)
            && (event.getClass() == RecognizeCompleted.class || event.getClass() == RecognizeFailed.class)
        ).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    @Override
    protected StartRecognizingEventResult getReturnedEvent(CallAutomationEventBase event) {
        StartRecognizingEventResult result = null;

        if (event.getClass() == RecognizeCompleted.class) {
            result = new StartRecognizingEventResult(true, (RecognizeCompleted) event, null);
        } else if (event.getClass() == RecognizeFailed.class) {
            result = new StartRecognizingEventResult(false, null, (RecognizeFailed) event);
        }

        return result;
    }
}
