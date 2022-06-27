// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.models.CallSource;
import com.azure.communication.callingserver.implementation.models.CallSourceDto;

/**
 * A converter for {@link CallSource}
 */
public final class CallSourceConverter {

    /**
     * Converts to {@link CallSource}.
     */
    public static CallSource convert(CallSourceDto callSourceDto) {

        if (callSourceDto == null) {
            return null;
        }

        return new CallSource(CommunicationIdentifierConverter.convert(callSourceDto.getIdentifier()))
            .setCallerId(PhoneNumberIdentifierConverter.convert(callSourceDto.getCallerId()));
    }

    /**
     * Converts to {@link CallSourceDto}.
     */
    public static CallSourceDto convert(CallSource callSource) {

        if (callSource == null) {
            return null;
        }

        return new CallSourceDto()
            .setIdentifier(CommunicationIdentifierConverter.convert(callSource.getIdentifier()))
            .setCallerId(PhoneNumberIdentifierConverter.convert(callSource.getCallerId()));
    }

    private CallSourceConverter() {
    }
}
