// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.implementation.models.CommunicationCloudEnvironmentModel;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModelKind;
import com.azure.communication.callautomation.implementation.models.CommunicationUserIdentifierModel;
import com.azure.communication.callautomation.implementation.models.MicrosoftTeamsUserIdentifierModel;
import com.azure.communication.callautomation.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.CommunicationCloudEnvironment;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.MicrosoftTeamsUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.common.UnknownIdentifier;

import java.util.ArrayList;
import java.util.Objects;
/**
 * A converter between {@link CommunicationIdentifierModel} and {@link CommunicationIdentifier}.
 */
public class CommunicationIdentifierConverter {

    /**
     * Maps from {@link CommunicationIdentifierModel} to {@link CommunicationIdentifier}.
     */
    public static CommunicationIdentifier convert(CommunicationIdentifierModel identifier) {
        if (identifier == null) {
            return null;
        }

        assertSingleType(identifier);
        String rawId = identifier.getRawId();

        CommunicationIdentifierModelKind kind = identifier.getKind();

        if (kind != null) {
            if (kind == CommunicationIdentifierModelKind.COMMUNICATION_USER
                && identifier.getCommunicationUser() != null) {
                Objects.requireNonNull(identifier.getCommunicationUser().getId());
                return new CommunicationUserIdentifier(identifier.getCommunicationUser().getId());
            }

            if (kind == CommunicationIdentifierModelKind.PHONE_NUMBER
                && identifier.getPhoneNumber() != null) {
                PhoneNumberIdentifierModel phoneNumberModel = identifier.getPhoneNumber();
                Objects.requireNonNull(phoneNumberModel.getValue());
                return new PhoneNumberIdentifier(phoneNumberModel.getValue()).setRawId(rawId);
            }

            if (kind == CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER
                && identifier.getMicrosoftTeamsUser() != null) {
                MicrosoftTeamsUserIdentifierModel teamsUserIdentifierModel = identifier.getMicrosoftTeamsUser();
                Objects.requireNonNull(teamsUserIdentifierModel.getUserId());
                Objects.requireNonNull(teamsUserIdentifierModel.getCloud());
                Objects.requireNonNull(rawId);
                return new MicrosoftTeamsUserIdentifier(teamsUserIdentifierModel.getUserId(),
                    teamsUserIdentifierModel.isAnonymous())
                    .setRawId(rawId)
                    .setCloudEnvironment(CommunicationCloudEnvironment
                        .fromString(teamsUserIdentifierModel.getCloud().toString()));
            }

            Objects.requireNonNull(rawId);
            return new UnknownIdentifier(rawId);
        }

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
                .setCloudEnvironment(CommunicationCloudEnvironment
                    .fromString(teamsUserIdentifierModel.getCloud().toString()));
        }

        Objects.requireNonNull(rawId);
        return new UnknownIdentifier(rawId);
    }

    /**
     * Maps from {@link CommunicationIdentifier} to {@link CommunicationIdentifierModel}.
     */
    public static CommunicationIdentifierModel convert(CommunicationIdentifier identifier)
        throws IllegalArgumentException {

        if (identifier == null) {
            return null;
        }

        if (identifier instanceof CommunicationUserIdentifier) {
            CommunicationUserIdentifier communicationUserIdentifier = (CommunicationUserIdentifier) identifier;
            return new CommunicationIdentifierModel()
                .setRawId(communicationUserIdentifier.getRawId())
                .setCommunicationUser(
                    new CommunicationUserIdentifierModel().setId(communicationUserIdentifier.getId()));
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
            UnknownIdentifier unknownIdentifier = (UnknownIdentifier) identifier;
            return new CommunicationIdentifierModel().setRawId(unknownIdentifier.getId());
        }

        throw new IllegalArgumentException(String.format("Unknown identifier class '%s'", identifier.getClass().getName()));
    }

    private static void assertSingleType(CommunicationIdentifierModel identifier) {
        CommunicationUserIdentifierModel communicationUser = identifier.getCommunicationUser();
        PhoneNumberIdentifierModel phoneNumber = identifier.getPhoneNumber();
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUser = identifier.getMicrosoftTeamsUser();

        ArrayList<String> presentProperties = new ArrayList<>();
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
            throw new IllegalArgumentException(
                String.format(
                    "Only one of the identifier models in %s should be present.",
                    String.join(", ", presentProperties)));
        }
    }
}

