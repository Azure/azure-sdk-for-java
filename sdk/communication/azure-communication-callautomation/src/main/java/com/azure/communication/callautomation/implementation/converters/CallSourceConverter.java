// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.implementation.models.CallSourceInternal;
import com.azure.communication.callautomation.models.CallSource;

/**
 * A converter for {@link CallSource}
 */
public final class CallSourceConverter {

    /**
     * Converts to {@link CallSource}.
     */
    public static CallSource convert(CallSourceInternal callSourceDto) {

        if (callSourceDto == null) {
            return null;
        }

        return new CallSource(CommunicationIdentifierConverter.convert(callSourceDto.getIdentifier()))
            .setCallerId(PhoneNumberIdentifierConverter.convert(callSourceDto.getCallerId()))
            .setDisplayName(callSourceDto.getDisplayName());
    }

    /**
     * Converts to {@link CallSourceInternal}.
     */
    public static CallSourceInternal convert(CallSource callSource) {

        if (callSource == null) {
            return null;
        }

        return new CallSourceInternal()
            .setIdentifier(CommunicationIdentifierConverter.convert(callSource.getIdentifier()))
            .setCallerId(PhoneNumberIdentifierConverter.convert(callSource.getCallerId()))
            .setDisplayName(callSource.getDisplayName());
    }

    private CallSourceConverter() {
    }
}
