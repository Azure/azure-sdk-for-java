// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.implementation.models.MicrosoftTeamsAppIdentifierModel;
import com.azure.communication.common.MicrosoftTeamsAppIdentifier;

/**
 * A converter for {@link MicrosoftTeamsAppIdentifier} and {@link MicrosoftTeamsAppIdentifierModel}
 */
public final class MicrosoftTeamsAppIdentifierConverter {

    /**
     * Converts to {@link MicrosoftTeamsAppIdentifierModel}.
     */
    public static MicrosoftTeamsAppIdentifierModel convert(MicrosoftTeamsAppIdentifier microsoftTeamsAppIdentifier) {

        MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel
            = (microsoftTeamsAppIdentifier == null || microsoftTeamsAppIdentifier.getAppId().isEmpty())
                ? null
                : CommunicationIdentifierConverter
                    .convert(new MicrosoftTeamsAppIdentifier(microsoftTeamsAppIdentifier.getAppId()))
                    .getMicrosoftTeamsApp();
        return microsoftTeamsAppIdentifierModel;
    }

    /**
     * Converts to {@link MicrosoftTeamsAppIdentifier}.
     */
    public static MicrosoftTeamsAppIdentifier
        convert(MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel) {

        MicrosoftTeamsAppIdentifier microsoftTeamsAppIdentifier
            = (microsoftTeamsAppIdentifierModel == null || microsoftTeamsAppIdentifierModel.getAppId().isEmpty())
                ? null
                : new MicrosoftTeamsAppIdentifier(microsoftTeamsAppIdentifierModel.getAppId());

        return microsoftTeamsAppIdentifier;
    }

    private MicrosoftTeamsAppIdentifierConverter() {
    }
}
