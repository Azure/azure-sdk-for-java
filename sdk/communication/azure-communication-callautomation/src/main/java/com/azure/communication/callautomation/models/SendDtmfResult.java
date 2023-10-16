// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.SendDtmfCompleted;
import com.azure.communication.callautomation.models.events.SendDtmfFailed;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The result of a send dtmf event.
 */
public class SendDtmfResult extends ResultWithEventHandling<SendDtmfEventResult> {
    /**
     * Initializes a new instance of SendDtmfResult.
     */
    public SendDtmfResult() {}
    @Override
    public Mono<SendDtmfEventResult> waitForEventProcessorAsync() {
        if (eventProcessor == null) {
            return Mono.empty();
        }

        return eventProcessor.waitForEventProcessorAsync(event -> Objects.equals(event.getCallConnectionId(), callConnectionId)
            && (Objects.equals(event.getOperationContext(), operationContextFromRequest) || operationContextFromRequest == null)
            && (event.getClass() == SendDtmfCompleted.class || event.getClass() == SendDtmfFailed.class)
        ).flatMap(event -> Mono.just(getReturnedEvent(event)));
    }

    @Override
    protected SendDtmfEventResult getReturnedEvent(CallAutomationEventBase event) {
        SendDtmfEventResult result = null;

        if (event.getClass() == SendDtmfCompleted.class) {
            result = new SendDtmfEventResult(true, (SendDtmfCompleted) event, null);
        } else if (event.getClass() == SendDtmfFailed.class) {
            result = new SendDtmfEventResult(false, null, (SendDtmfFailed) event);
        }

        return result;
    }
}
