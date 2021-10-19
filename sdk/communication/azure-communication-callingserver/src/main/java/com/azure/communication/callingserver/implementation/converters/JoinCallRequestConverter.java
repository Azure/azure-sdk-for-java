// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CallLocatorModel;
import com.azure.communication.callingserver.implementation.models.JoinCallRequest;
import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.common.CommunicationIdentifier;

import java.util.ArrayList;

/**
 * A converter for {@link JoinCallRequest}
 */
public final class JoinCallRequestConverter {

    /**
     * Converts to {@link JoinCallRequest}.
     */
    public static JoinCallRequest convert(CallLocator callLocator, CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        if (source == null) {
            return null;
        }

        JoinCallRequest joinCallRequest = new JoinCallRequest()
            .setSource(CommunicationIdentifierConverter.convert(source))
            .setCallLocator(CallLocatorConverter.convert(callLocator));

        if (joinCallOptions == null) {
            return joinCallRequest;
        }

        joinCallRequest.setSubject(joinCallOptions.getSubject());
        joinCallRequest.setCallbackUri(joinCallOptions.getCallbackUri().toString());
        joinCallRequest.setRequestedMediaTypes(new ArrayList<>(joinCallOptions.getRequestedMediaTypes()));
        joinCallRequest.setRequestedCallEvents(new ArrayList<>(joinCallOptions.getRequestedCallEvents()));
        return joinCallRequest;
    }

    private JoinCallRequestConverter() {
    }
}

