// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.AcsCallParticipantInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModelKind;
import com.azure.communication.callingserver.implementation.models.CommunicationUserIdentifierModel;


public class ModelGenerator {
    static CommunicationIdentifierModel generateUserIdentifierModel(String userId) {
        return new CommunicationIdentifierModel()
            .setRawId("rawId")
            .setKind(CommunicationIdentifierModelKind.COMMUNICATION_USER)
            .setCommunicationUser(new CommunicationUserIdentifierModel()
                .setId(userId));
    }

    static AcsCallParticipantInternal generateAcsCallParticipantInternal(String callerId, boolean isMuted) {
        return new AcsCallParticipantInternal()
            .setIdentifier(ModelGenerator.generateUserIdentifierModel(callerId))
            .setIsMuted(isMuted);
    }
}
