// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.implementation.models.CommunicationUserIdentifierModel;
import com.azure.communication.common.CommunicationUserIdentifier;

/**
 * A converter for {@link CommunicationUserIdentifier} and {@link CommunicationUserIdentifierModel}
 */
public final class CommunicationUserIdentifierConverter {

    /**
     * Converts to {@link CommunicationUserIdentifierModel}.
     */
    public static CommunicationUserIdentifierModel convert(CommunicationUserIdentifier communicationUserIdentifier) {

        CommunicationUserIdentifierModel communicationUserIdentifierModel
            = (communicationUserIdentifier == null || communicationUserIdentifier.getId().isEmpty())
                ? null
                : CommunicationIdentifierConverter
                    .convert(new CommunicationUserIdentifier(communicationUserIdentifier.getId()))
                    .getCommunicationUser();
        return communicationUserIdentifierModel;
    }

    /**
     * Converts to {@link CommunicationUserIdentifier}.
     */
    public static CommunicationUserIdentifier
        convert(CommunicationUserIdentifierModel communicationUserIdentifierModel) {

        CommunicationUserIdentifier communicationUserIdentifier
            = (communicationUserIdentifierModel == null || communicationUserIdentifierModel.getId().isEmpty())
                ? null
                : new CommunicationUserIdentifier(communicationUserIdentifierModel.getId());

        return communicationUserIdentifier;
    }

    private CommunicationUserIdentifierConverter() {
    }
}
