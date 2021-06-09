// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.common.CommunicationIdentifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter for {@link CreateCallRequestInternal}
 */
public final class CallConnectionRequestConverter {

    /**
     * Convert create call parameters into CreateCallRequestInternal
     * @param source Source of the call
     * @param targets Targets for the call
     * @param createCallOptions CreateCallOptions
     * @return CreateCallRequestInternal
     */
    public static CreateCallRequestInternal convert(CommunicationIdentifier source,
                                                              CommunicationIdentifier[] targets,
                                                              CreateCallOptions createCallOptions) {

        List<CommunicationIdentifier> targetsList = new ArrayList<>();
        for (CommunicationIdentifier communicationIdentifier : targets) {
            targetsList.add(communicationIdentifier);
        }

        List<CallModality> requestedModalities = new LinkedList<>();
        for (CallModality modality : createCallOptions.getRequestedMediaTypes()) {
            requestedModalities.add(CallModality.fromString(modality.toString()));
        }

        List<EventSubscriptionType> requestedCallEvents = new LinkedList<>();
        for (EventSubscriptionType requestedCallEvent : createCallOptions.getRequestedCallEvents()) {
            requestedCallEvents.add(EventSubscriptionType.fromString(requestedCallEvent.toString()));
        }

        PhoneNumberIdentifierModel sourceAlternateIdentity = null;
        if (createCallOptions.getAlternateCallerId() != null) {
            sourceAlternateIdentity = new PhoneNumberIdentifierModel();
            sourceAlternateIdentity.setValue(createCallOptions.getAlternateCallerId().getPhoneNumber());
        }

        CreateCallRequestInternal request = new CreateCallRequestInternal();
        request.setSource(CommunicationIdentifierConverter.convert(source));
        request.setTargets(targetsList.stream()
            .map(target -> CommunicationIdentifierConverter.convert(target))
            .collect(Collectors.toList()));
        request.setCallbackUri(createCallOptions.getCallbackUri());
        request.setRequestedMediaTypes(requestedModalities);
        request.setRequestedCallEvents(requestedCallEvents).setAlternateCallerId(sourceAlternateIdentity);

        return request;
    }
}
