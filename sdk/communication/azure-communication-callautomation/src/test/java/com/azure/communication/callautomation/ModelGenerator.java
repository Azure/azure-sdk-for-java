// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.CallParticipantInternal;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModelKind;
import com.azure.communication.callautomation.implementation.models.CommunicationUserIdentifierModel;


public class ModelGenerator {
    static CommunicationIdentifierModel generateUserIdentifierModel(String userId) {
        return new CommunicationIdentifierModel()
            .setRawId("rawId")
            .setKind(CommunicationIdentifierModelKind.COMMUNICATION_USER)
            .setCommunicationUser(new CommunicationUserIdentifierModel()
                .setId(userId));
    }

    static CallParticipantInternal generateAcsCallParticipantInternal(String callerId, boolean isMuted) {
        return new CallParticipantInternal()
            .setIdentifier(ModelGenerator.generateUserIdentifierModel(callerId))
            .setIsMuted(isMuted);
    }
}
