// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import java.util.Objects;

class CommunicationIdentifierSerializer {
    /**
     * Deserialize CommunicationIdentifierModel into CommunicationIdentifier
     * @param identifier CommunicationIdentifierModel to be deserialized
     * @return deserialized CommunicationIdentifier
     */
    public static CommunicationIdentifier deserialize(CommunicationIdentifierModel identifier) {

        String id = identifier.getId();
        CommunicationIdentifierKind kind = identifier.getKind();

        if (kind == CommunicationIdentifierKind.COMMUNICATION_USER) {
            Objects.requireNonNull(id);
            return new CommunicationUserIdentifier(id);
        }

        if (kind == CommunicationIdentifierKind.CALLING_APPLICATION) {
            Objects.requireNonNull(id);
            return new CallingApplicationIdentifier(id);
        }

        if (kind == CommunicationIdentifierKind.PHONE_NUMBER) {
            Objects.requireNonNull(identifier.getPhoneNumber());
            Objects.requireNonNull(identifier.getId());
            return new PhoneNumberIdentifier(identifier.getPhoneNumber()).setId(identifier.getId());
        }

        if (kind == CommunicationIdentifierKind.MICROSOFT_TEAMS_USER) {
            Objects.requireNonNull(identifier.getMicrosoftTeamsUserId());
            Objects.requireNonNull(identifier.getCloudEnvironmentModel());
            Objects.requireNonNull(identifier.getId());
            return new MicrosoftTeamsUserIdentifier(identifier.getMicrosoftTeamsUserId(), identifier.isAnonymous())
                .setId(identifier.getId())
                .setCloudEnvironment(CommunicationCloudEnvironment.fromModel(identifier.getCloudEnvironmentModel()));
        }

        Objects.requireNonNull(id);
        return new UnknownIdentifier(id);
    }

    /**
     * Serialize CommunicationIdentifier into CommunicationIdentifierModel
     * @param identifier CommunicationIdentifier object to be serialized
     * @return CommunicationIdentifierModel
     * @throws IllegalArgumentException when identifier is an unknown class derived from
     *          CommunicationIdentifier
     */
    public static CommunicationIdentifierModel serialize(CommunicationIdentifier identifier)
        throws IllegalArgumentException {
        if (identifier instanceof CommunicationUserIdentifier) {
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.COMMUNICATION_USER)
                .setId(((CommunicationUserIdentifier) identifier).getId());
        }

        if (identifier instanceof CallingApplicationIdentifier) {
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.CALLING_APPLICATION)
                .setId(((CallingApplicationIdentifier) identifier).getId());
        }

        if (identifier instanceof PhoneNumberIdentifier) {
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setPhoneNumber(((PhoneNumberIdentifier) identifier).getPhoneNumber())
                .setId(identifier.getId());
        }

        if (identifier instanceof MicrosoftTeamsUserIdentifier) {
            MicrosoftTeamsUserIdentifier teamsUserIdentifier = (MicrosoftTeamsUserIdentifier) identifier;
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setMicrosoftTeamsUserId(teamsUserIdentifier.getUserId())
                .setIsAnonymous(teamsUserIdentifier.isAnonymous())
                .setId(teamsUserIdentifier.getId())
                .setCloudEnvironmentModel(new CommunicationCloudEnvironmentModel(teamsUserIdentifier.getCloudEnvironment().toString()));
        }

        if (identifier instanceof UnknownIdentifier) {
            return new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.UNKNOWN)
                .setId(((UnknownIdentifier) identifier).getId());
        }

        throw new IllegalArgumentException(String.format("Unknown identifier class '%s'", identifier.getClass().getName()));
    }
}
