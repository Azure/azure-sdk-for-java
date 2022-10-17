// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.*;
import com.azure.communication.common.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentifierConverterUnitTests extends CallAutomationUnitTestBase {

    private final String TestUserId = "User Id";
    private final String TestRawId = "Raw Id";
    private final String TestPhoneNumber = "+12223334444";
    private final String TestPhoneNumberRawId = "4:+12223334444";
    private final String TestTeamsUserId = "Microsoft Teams User Id";

    @Test
    public void convertWithoutKindCommunicationUser() {
        CommunicationUserIdentifierModel communicationUserIdentifierModel = new CommunicationUserIdentifierModel()
            .setId(TestUserId);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setCommunicationUser(communicationUserIdentifierModel)
            .setRawId(TestRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        CommunicationUserIdentifier expected = new CommunicationUserIdentifier(TestUserId);

        assertNull(communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindCommunicationUser() {
        CommunicationUserIdentifierModel communicationUserIdentifierModel = new CommunicationUserIdentifierModel()
            .setId(TestUserId);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setCommunicationUser(communicationUserIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.COMMUNICATION_USER)
            .setRawId(TestRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        CommunicationUserIdentifier expected = new CommunicationUserIdentifier(TestUserId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.COMMUNICATION_USER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindButNoCommunicationUser() {
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierModelKind.COMMUNICATION_USER)
            .setRawId(TestRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        UnknownIdentifier expected = new UnknownIdentifier(TestRawId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.COMMUNICATION_USER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithoutKindPhoneNumber() {
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = new PhoneNumberIdentifierModel()
            .setValue(TestPhoneNumber);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setPhoneNumber(phoneNumberIdentifierModel)
            .setRawId(TestPhoneNumberRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        PhoneNumberIdentifier expected = new PhoneNumberIdentifier(TestPhoneNumber);

        assertNull(communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindPhoneNumber() {
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = new PhoneNumberIdentifierModel()
            .setValue(TestPhoneNumber);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setPhoneNumber(phoneNumberIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.PHONE_NUMBER)
            .setRawId(TestPhoneNumberRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        PhoneNumberIdentifier expected = new PhoneNumberIdentifier(TestPhoneNumber);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.PHONE_NUMBER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindButNoPhoneNumber() {
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierModelKind.PHONE_NUMBER)
            .setRawId(TestRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        UnknownIdentifier expected = new UnknownIdentifier(TestRawId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.PHONE_NUMBER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithoutKindMicrosoftTeamsUser() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(TestTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setRawId(TestRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        MicrosoftTeamsUserIdentifier expected = new MicrosoftTeamsUserIdentifier(TestTeamsUserId)
            .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
            .setRawId(TestRawId);

        assertNull(communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindMicrosoftTeamsUser() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(TestTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER)
            .setRawId(TestRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        MicrosoftTeamsUserIdentifier expected = new MicrosoftTeamsUserIdentifier(TestTeamsUserId)
            .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
            .setRawId(TestRawId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindButNoMicrosoftTeamsUser() {
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER)
            .setRawId(TestRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        UnknownIdentifier expected = new UnknownIdentifier(TestRawId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }
}
