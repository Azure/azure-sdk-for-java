// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import java.util.ArrayList;
import java.util.Objects;

class CommunicationIdentifierSerializer {
    /**
     * Deserialize CommunicationIdentifierModel into CommunicationIdentifier
     * @param identifier CommunicationIdentifierModel to be deserialized
     * @return deserialized CommunicationIdentifier
     */
    public static CommunicationIdentifier deserialize(CommunicationIdentifierModel identifier) {
        assertSingleType(identifier);
        String rawId = identifier.getRawId();

        if (identifier.getCommunicationUser() != null) {
            Objects.requireNonNull(identifier.getCommunicationUser().getId());
            return new CommunicationUserIdentifier(identifier.getCommunicationUser().getId());
        }

        if (identifier.getPhoneNumber() != null) {
            PhoneNumberIdentifierModel phoneNumberModel = identifier.getPhoneNumber();
            Objects.requireNonNull(phoneNumberModel.getValue());
            return new PhoneNumberIdentifier(phoneNumberModel.getValue()).setRawId(rawId);
        }

        if (identifier.getMicrosoftTeamsUser() != null) {
            MicrosoftTeamsUserIdentifierModel teamsUserIdentifierModel = identifier.getMicrosoftTeamsUser();
            Objects.requireNonNull(teamsUserIdentifierModel.getUserId());
            Objects.requireNonNull(teamsUserIdentifierModel.getCloud());
            Objects.requireNonNull(rawId);
            return new MicrosoftTeamsUserIdentifier(teamsUserIdentifierModel.getUserId(),
                teamsUserIdentifierModel.isAnonymous())
                .setRawId(rawId)
                .setCloudEnvironment(CommunicationCloudEnvironment.fromModel(teamsUserIdentifierModel.getCloud()));
        }

        Objects.requireNonNull(rawId);
        return new UnknownIdentifier(rawId);
    }

    private static void assertSingleType(CommunicationIdentifierModel identifier) {
        CommunicationUserIdentifierModel communicationUser = identifier.getCommunicationUser();
        PhoneNumberIdentifierModel phoneNumber = identifier.getPhoneNumber();
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUser = identifier.getMicrosoftTeamsUser();

        ArrayList<String> presentProperties = new ArrayList<String>();
        if (communicationUser != null) {
            presentProperties.add(communicationUser.getClass().getName());
        }
        if (phoneNumber != null) {
            presentProperties.add(phoneNumber.getClass().getName());
        }
        if (microsoftTeamsUser != null) {
            presentProperties.add(microsoftTeamsUser.getClass().getName());
        }

        if (presentProperties.size() > 1) {
            throw new IllegalArgumentException(String.format("Only one of the identifier models in %s should be present.",
                String.join(", ", presentProperties)));
        }
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
                .setCommunicationUser(
                    new CommunicationUserIdentifierModel().setId(((CommunicationUserIdentifier) identifier).getId()));
        }

        if (identifier instanceof PhoneNumberIdentifier) {
            PhoneNumberIdentifier phoneNumberIdentifier = (PhoneNumberIdentifier) identifier;
            return new CommunicationIdentifierModel()
                .setRawId(phoneNumberIdentifier.getRawId())
                .setPhoneNumber(new PhoneNumberIdentifierModel().setValue(phoneNumberIdentifier.getPhoneNumber()));
        }

        if (identifier instanceof MicrosoftTeamsUserIdentifier) {
            MicrosoftTeamsUserIdentifier teamsUserIdentifier = (MicrosoftTeamsUserIdentifier) identifier;
            return new CommunicationIdentifierModel()
                .setRawId(teamsUserIdentifier.getRawId())
                .setMicrosoftTeamsUser(new MicrosoftTeamsUserIdentifierModel()
                    .setIsAnonymous(teamsUserIdentifier.isAnonymous())
                    .setUserId(teamsUserIdentifier.getUserId())
                    .setCloud(CommunicationCloudEnvironmentModel.fromString(
                        teamsUserIdentifier.getCloudEnvironment().toString())));
        }

        if (identifier instanceof UnknownIdentifier) {
            return new CommunicationIdentifierModel()
                .setRawId(((UnknownIdentifier) identifier).getId());
        }

        throw new IllegalArgumentException(String.format("Unknown identifier class '%s'", identifier.getClass().getName()));
    }
}
