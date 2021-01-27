// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommunicationIdentifierSerializerTests {

    final String someId = "some id";
    final String teamsUserId = "Teams user id";
    final String fullId = "some lengthy id string";

    @Test
    public void missingPropertyDeserializerThrows() {
        CommunicationIdentifierModel[] modelsWithMissingMandatoryProperty = new CommunicationIdentifierModel[] {
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.UNKNOWN), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.COMMUNICATION_USER), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.CALLING_APPLICATION), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setId(someId), // Missing PhoneNumber
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setPhoneNumber("+12223334444"), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setId(someId)
                .setCloudEnvironmentModel(CommunicationCloudEnvironmentModel.PUBLIC), // Missing MicrosoftTeamsUserId
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setId(someId)
                .setMicrosoftTeamsUserId(teamsUserId), // Missing Cloud
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setCloudEnvironmentModel(CommunicationCloudEnvironmentModel.PUBLIC)
                .setMicrosoftTeamsUserId(teamsUserId) // Missing id
        };

        Arrays.stream(modelsWithMissingMandatoryProperty).forEach(identifierModel -> {
            assertThrows(NullPointerException.class,
                () -> {
                    CommunicationIdentifierSerializer.deserialize(identifierModel);
                });
        });
    }

    @Test
    public void serializeCommunicationUser() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new CommunicationUserIdentifier(someId));

        assertEquals(CommunicationIdentifierKind.COMMUNICATION_USER, model.getKind());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializeCommunicationUser() {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.COMMUNICATION_USER)
            .setId(someId));

        CommunicationUserIdentifier expectedIdentifier = new CommunicationUserIdentifier(someId);

        assertEquals(identifier.getClass(), CommunicationUserIdentifier.class);
        assertEquals(expectedIdentifier.getId(), ((CommunicationUserIdentifier) identifier).getId());
    }

    @Test
    public void serializeUnknown() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(new UnknownIdentifier(someId));

        assertEquals(CommunicationIdentifierKind.UNKNOWN, model.getKind());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializeUnknown() {
        CommunicationIdentifier unknownIdentifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.UNKNOWN)
                .setId(someId));
        assertEquals(UnknownIdentifier.class, unknownIdentifier.getClass());
        assertEquals(someId, ((UnknownIdentifier) unknownIdentifier).getId());
    }

    @Test
    public void serializeFutureTypeShouldThrow() {
        assertThrows(IllegalArgumentException.class,
            () -> {
                CommunicationIdentifierSerializer.serialize(
                    new CommunicationIdentifier() {
                        @Override
                        public String getId() {
                            return someId;
                        }
                    });
            });
    }

    @Test
    public void serializeCallingApplication() {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new CallingApplicationIdentifier(someId));

        assertEquals(CommunicationIdentifierKind.CALLING_APPLICATION, model.getKind());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializeCallingApplication() {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.CALLING_APPLICATION)
                .setId(someId));

        assertEquals(CallingApplicationIdentifier.class, identifier.getClass());
        assertEquals(someId, ((CallingApplicationIdentifier) identifier).getId());
    }

    @Test
    public void serializePhoneNumber() {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new PhoneNumberIdentifier(phoneNumber).setId(someId));

        assertEquals(CommunicationIdentifierKind.PHONE_NUMBER, model.getKind());
        assertEquals(phoneNumber, model.getPhoneNumber());
        assertEquals(someId, model.getId());
    }

    @Test
    public void deserializePhoneNumber() {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setPhoneNumber(phoneNumber)
                .setId(someId));

        assertEquals(PhoneNumberIdentifier.class, identifier.getClass());
        assertEquals(phoneNumber, ((PhoneNumberIdentifier) identifier).getPhoneNumber());
        assertEquals(someId, identifier.getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void serializeMicrosoftTeamsUser(boolean isAnonymous) {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.serialize(
            new MicrosoftTeamsUserIdentifier(teamsUserId, isAnonymous)
                .setId(someId)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD));

        assertEquals(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER, model.getKind());
        assertEquals(teamsUserId, model.getMicrosoftTeamsUserId());
        assertEquals(someId, model.getId());
        assertEquals(CommunicationCloudEnvironmentModel.DOD, model.getCloudEnvironmentModel());
        assertEquals(isAnonymous, model.isAnonymous());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void deserializerMicrosoftTeamsUser(boolean isAnonymous) {
        MicrosoftTeamsUserIdentifier identifier = (MicrosoftTeamsUserIdentifier) CommunicationIdentifierSerializer.deserialize(
            new CommunicationIdentifierModel()
                .setMicrosoftTeamsUserId(teamsUserId)
                .setId(someId)
                .setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setCloudEnvironmentModel(CommunicationCloudEnvironmentModel.GCCH)
                .setIsAnonymous(isAnonymous));

        assertEquals(MicrosoftTeamsUserIdentifier.class, identifier.getClass());
        assertEquals(teamsUserId, identifier.getUserId());
        assertEquals(someId, identifier.getId());
        assertEquals(CommunicationCloudEnvironment.GCCH, identifier.getCloudEnvironment());
        assertEquals(isAnonymous, identifier.isAnonymous());
    }

}
