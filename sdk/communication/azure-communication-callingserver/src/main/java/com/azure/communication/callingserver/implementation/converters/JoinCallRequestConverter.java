// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.JoinCallRequest;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.MediaType;
import com.azure.communication.common.CommunicationIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A converter for {@link JoinCallRequest}
 */
public final class JoinCallRequestConverter {

    /**
     * Converts to {@link JoinCallRequest}.
     */
    public static JoinCallRequest convert(CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        if (source == null) {
            return null;
        }

        JoinCallRequest joinCallRequest = new JoinCallRequest()
            .setSource(CommunicationIdentifierConverter.convert(source));

        if (joinCallOptions == null) {
            return joinCallRequest;
        }

        joinCallRequest.setSubject(joinCallOptions.getSubject());
        joinCallRequest.setCallbackUri(joinCallOptions.getCallbackUri());

        List<MediaType> requestedModalities = new ArrayList<>();
        Collections.addAll(requestedModalities, joinCallOptions.getRequestedMediaTypes());
        joinCallRequest.setRequestedMediaTypes(requestedModalities);

        List<EventSubscriptionType> requestedCallEvents = new ArrayList<>();
        Collections.addAll(requestedCallEvents, joinCallOptions.getRequestedCallEvents());
        joinCallRequest.setRequestedCallEvents(requestedCallEvents);

        return joinCallRequest;
    }

    private JoinCallRequestConverter() {
    }
}

