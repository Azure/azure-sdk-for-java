// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CreateCallRequest;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.common.CommunicationIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter for {@link CreateCallRequest}
 */
public final class CallConnectionRequestConverter {

    /**
     * Converts to {@link CreateCallRequest}.
     */
    public static CreateCallRequest convert(
        CommunicationIdentifier source,
        CommunicationIdentifier[] targets,
        CreateCallOptions createCallOptions) {
        if (source == null || targets == null || targets.length == 0) {
            return null;
        }

        CreateCallRequest createCallRequest
            = new CreateCallRequest()
            .setSource(CommunicationIdentifierConverter.convert(source))
            .setTargets(new ArrayList<>(Arrays.asList(targets))
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));

        if (createCallOptions == null) {
            return createCallRequest;
        }

        List<CallModality> requestedMediaTypes = new LinkedList<>();
        for (CallModality modality : createCallOptions.getRequestedMediaTypes()) {
            requestedMediaTypes.add(CallModality.fromString(modality.toString()));
        }

        List<EventSubscriptionType> requestedCallEvents = new LinkedList<>();
        for (EventSubscriptionType requestedCallEvent : createCallOptions.getRequestedCallEvents()) {
            requestedCallEvents.add(EventSubscriptionType.fromString(requestedCallEvent.toString()));
        }

        PhoneNumberIdentifierModel sourceAlternateIdentity = null;
        if (createCallOptions.getAlternateCallerId() != null) {
            sourceAlternateIdentity = new PhoneNumberIdentifierModel()
                .setValue(createCallOptions.getAlternateCallerId().getPhoneNumber());
        }

        return createCallRequest
            .setRequestedMediaTypes(requestedMediaTypes)
            .setRequestedCallEvents(requestedCallEvents)
            .setAlternateCallerId(sourceAlternateIdentity)
            .setCallbackUri(createCallOptions.getCallbackUri());
    }
}
