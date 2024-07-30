// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationCloudEnvironmentModel;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModelKind;
import com.azure.communication.callautomation.implementation.models.CommunicationUserIdentifierModel;
import com.azure.communication.callautomation.implementation.models.MicrosoftTeamsAppIdentifierModel;
import com.azure.communication.callautomation.implementation.models.MicrosoftTeamsUserIdentifierModel;
import com.azure.communication.callautomation.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.common.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentifierConverterUnitTests extends CallAutomationUnitTestBase {

    private final String testUserId = "User Id";
    private final String testRawId = "Raw Id";
    private final String testPhoneNumber = "+12223334444";
    private final String testPhoneNumberRawId = "4:+12223334444";
    private final String testTeamsUserId = "Microsoft Teams User Id";

    @Test
    public void convertWithoutKindCommunicationUser() {
        CommunicationUserIdentifierModel communicationUserIdentifierModel = new CommunicationUserIdentifierModel()
            .setId(testUserId);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setCommunicationUser(communicationUserIdentifierModel)
            .setRawId(testRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        CommunicationUserIdentifier expected = new CommunicationUserIdentifier(testUserId);

        assertNull(communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindCommunicationUser() {
        CommunicationUserIdentifierModel communicationUserIdentifierModel = new CommunicationUserIdentifierModel()
            .setId(testUserId);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setCommunicationUser(communicationUserIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.COMMUNICATION_USER)
            .setRawId(testRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        CommunicationUserIdentifier expected = new CommunicationUserIdentifier(testUserId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.COMMUNICATION_USER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindCommunicationUserNoId() {
        CommunicationUserIdentifierModel communicationUserIdentifierModel = new CommunicationUserIdentifierModel();
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setCommunicationUser(communicationUserIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.COMMUNICATION_USER)
            .setRawId(testRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindCommunicationUserNoId() {
        CommunicationUserIdentifierModel communicationUserIdentifierModel = new CommunicationUserIdentifierModel();
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setCommunicationUser(communicationUserIdentifierModel)
            .setRawId(testRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithKindButNoCommunicationUser() {
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierModelKind.COMMUNICATION_USER)
            .setRawId(testRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        UnknownIdentifier expected = new UnknownIdentifier(testRawId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.COMMUNICATION_USER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithoutKindPhoneNumber() {
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = new PhoneNumberIdentifierModel()
            .setValue(testPhoneNumber);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setPhoneNumber(phoneNumberIdentifierModel)
            .setRawId(testPhoneNumberRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        PhoneNumberIdentifier expected = new PhoneNumberIdentifier(testPhoneNumber);

        assertNull(communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindPhoneNumber() {
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = new PhoneNumberIdentifierModel()
            .setValue(testPhoneNumber);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setPhoneNumber(phoneNumberIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.PHONE_NUMBER)
            .setRawId(testPhoneNumberRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        PhoneNumberIdentifier expected = new PhoneNumberIdentifier(testPhoneNumber);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.PHONE_NUMBER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindButNoPhoneNumber() {
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierModelKind.PHONE_NUMBER)
            .setRawId(testRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        UnknownIdentifier expected = new UnknownIdentifier(testRawId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.PHONE_NUMBER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindPhoneNumberNoPhoneValue() {
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = new PhoneNumberIdentifierModel();
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setPhoneNumber(phoneNumberIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.PHONE_NUMBER)
            .setRawId(testPhoneNumberRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindPhoneNumberNoPhoneValue() {
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = new PhoneNumberIdentifierModel();
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setPhoneNumber(phoneNumberIdentifierModel)
            .setRawId(testPhoneNumberRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindMicrosoftTeamsUser() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(testTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setRawId(testRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        MicrosoftTeamsUserIdentifier expected = new MicrosoftTeamsUserIdentifier(testTeamsUserId)
            .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
            .setRawId(testRawId);

        assertNull(communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindMicrosoftTeamsUser() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(testTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER)
            .setRawId(testRawId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        MicrosoftTeamsUserIdentifier expected = new MicrosoftTeamsUserIdentifier(testTeamsUserId)
            .setCloudEnvironment(CommunicationCloudEnvironment.GCCH)
            .setRawId(testRawId);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindMicrosoftTeamsUserNoUserId() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER)
            .setRawId(testRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindMicrosoftTeamsUserNoUserId() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setRawId(testRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithKindMicrosoftTeamsUserNoCloudEnv() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(testTeamsUserId)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER)
            .setRawId(testRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindMicrosoftTeamsUserNoCloudEnv() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(testTeamsUserId)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel)
            .setRawId(testRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithKindMicrosoftTeamsUserNoRawId() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(testTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_USER)
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindMicrosoftTeamsUserNoRawId() {
        MicrosoftTeamsUserIdentifierModel microsoftTeamsUserIdentifierModel = new MicrosoftTeamsUserIdentifierModel()
            .setUserId(testTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH)
            .setIsAnonymous(true);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsUser(microsoftTeamsUserIdentifierModel);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }


    @Test
    public void convertWithoutKindMicrosoftTeamsApp() {
        MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel = new MicrosoftTeamsAppIdentifierModel()
            .setAppId(testTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsApp(microsoftTeamsAppIdentifierModel)
            .setRawId(testTeamsUserId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        MicrosoftTeamsAppIdentifier expected = new MicrosoftTeamsAppIdentifier(testTeamsUserId, CommunicationCloudEnvironment.GCCH);

        assertNull(communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindMicrosoftTeamsApp() {
        MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel = new MicrosoftTeamsAppIdentifierModel()
            .setAppId(testTeamsUserId)
            .setCloud(CommunicationCloudEnvironmentModel.GCCH);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsApp(microsoftTeamsAppIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_APP)
            .setRawId(testTeamsUserId);

        CommunicationIdentifier got = CommunicationIdentifierConverter.convert(communicationIdentifierModel);

        MicrosoftTeamsAppIdentifier expected = new MicrosoftTeamsAppIdentifier(testTeamsUserId, CommunicationCloudEnvironment.GCCH);

        assertNotNull(communicationIdentifierModel.getKind());
        assertEquals(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_APP, communicationIdentifierModel.getKind());
        assertEquals(expected, got);
    }

    @Test
    public void convertWithKindMicrosoftTeamsAppNoAppId() {
        MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel = new MicrosoftTeamsAppIdentifierModel()
            .setCloud(CommunicationCloudEnvironmentModel.GCCH);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsApp(microsoftTeamsAppIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_APP)
            .setRawId(testTeamsUserId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindMicrosoftTeamsAppNoAppId() {
        MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel = new MicrosoftTeamsAppIdentifierModel()
            .setCloud(CommunicationCloudEnvironmentModel.GCCH);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsApp(microsoftTeamsAppIdentifierModel)
            .setRawId(testTeamsUserId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithKindMicrosoftTeamsAppNoCloudEnv() {
        MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel = new MicrosoftTeamsAppIdentifierModel()
            .setAppId(testTeamsUserId);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsApp(microsoftTeamsAppIdentifierModel)
            .setKind(CommunicationIdentifierModelKind.MICROSOFT_TEAMS_APP)
            .setRawId(testTeamsUserId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindMicrosoftTeamsAppNoCloudEnv() {
        MicrosoftTeamsAppIdentifierModel microsoftTeamsAppIdentifierModel = new MicrosoftTeamsAppIdentifierModel()
            .setAppId(testTeamsUserId);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setMicrosoftTeamsApp(microsoftTeamsAppIdentifierModel);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }
    
    @Test
    public void convertWithKindUnknownAndRawId() {
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setKind(CommunicationIdentifierModelKind.UNKNOWN);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithoutKindUnknownAndRawId() {
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel();

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof NullPointerException);
        }
    }

    @Test
    public void convertWithMultipleTypes() {
        CommunicationUserIdentifierModel communicationUserIdentifierModel = new CommunicationUserIdentifierModel()
            .setId(testUserId);
        PhoneNumberIdentifierModel phoneNumberIdentifierModel = new PhoneNumberIdentifierModel()
            .setValue(testPhoneNumber);
        CommunicationIdentifierModel communicationIdentifierModel = new CommunicationIdentifierModel()
            .setCommunicationUser(communicationUserIdentifierModel)
            .setPhoneNumber(phoneNumberIdentifierModel)
            .setRawId(testRawId);

        try {
            CommunicationIdentifierConverter.convert(communicationIdentifierModel);
        } catch (Exception e) {
            assert (e instanceof IllegalArgumentException);
        }
    }
}
