package com.azure.communication.common;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommunicationIdentifierSerializerTests {

    public static final String SOME_ID = "some id";

    @Test
    public void MissingProperty_DeserializerThrows()
    {
        CommunicationIdentifierModel[] modelsWithMissingMandatoryProperty = new CommunicationIdentifierModel[]
        {
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.UNKNOWN), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.COMMUNICATION_USER), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.CALLING_APPLICATION), // Missing Id
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER).setId(SOME_ID), // Missing PhoneNumber
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER).setId(SOME_ID), // Missing IsAnonymous
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER) .setId(SOME_ID).setIsAnonymous(true) // Missing MicrosoftTeamsUserId
        };

        Arrays.stream(modelsWithMissingMandatoryProperty).forEach(identifierModel -> {
            assertThrows(NullPointerException.class,
                () -> { CommunicationIdentifierSerializer.Deserialize(identifierModel);});
        });
    }

    @Test
    public void SerializeCommunicationUser()
    {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.Serialize(new CommunicationUserIdentifier(SOME_ID));

        assertEquals(CommunicationIdentifierKind.COMMUNICATION_USER, model.getKind());
        assertEquals(SOME_ID, model.getId());
    }

    @Test
    public void DeserializeCommunicationUser()
    {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.Deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.COMMUNICATION_USER)
            .setId(SOME_ID));

        CommunicationUserIdentifier expectedIdentifier = new CommunicationUserIdentifier(SOME_ID);

        assertEquals(identifier.getClass(), CommunicationUserIdentifier.class);
        assertEquals(expectedIdentifier.getId(), ((CommunicationUserIdentifier)identifier).getId());
    }

    @Test
    public void SerializeUnknown()
    {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.Serialize(new UnknownIdentifier(SOME_ID));

        assertEquals(CommunicationIdentifierKind.UNKNOWN, model.getKind());
        assertEquals(SOME_ID, model.getId());
    }

    @Test
    public void DeserializeUnknown() {
        CommunicationIdentifier unknownIdentifier = CommunicationIdentifierSerializer.Deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.UNKNOWN)
                .setId(SOME_ID));
        assertEquals(UnknownIdentifier.class, unknownIdentifier.getClass());
        assertEquals(SOME_ID, ((UnknownIdentifier)unknownIdentifier).getId());
    }

    @Test
    public void DesericalizeFutureType(){
        final String futureType = "NewKind";
        CommunicationIdentifier unknownIdentifier = CommunicationIdentifierSerializer.Deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.fromString(futureType))
                .setId(SOME_ID));
        assertEquals(UnknownIdentifier.class, unknownIdentifier.getClass());
        assertEquals(SOME_ID, ((UnknownIdentifier)unknownIdentifier).getId());
    }

    @Test
    public void SerializeCallingApplication()
    {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.Serialize(
            new CallingApplicationIdentifier(SOME_ID));

        assertEquals(CommunicationIdentifierKind.CALLING_APPLICATION, model.getKind());
        assertEquals(SOME_ID, model.getId());
    }

    @Test
    public void DeserializeCallingApplication()
    {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.Deserialize(
            new CommunicationIdentifierModel()
                .setKind(CommunicationIdentifierKind.CALLING_APPLICATION)
                .setId(SOME_ID));

        assertEquals(CallingApplicationIdentifier.class, identifier.getClass());
        assertEquals(SOME_ID, ((CallingApplicationIdentifier)identifier).getId());
    }

    @Test
    public void SerializePhoneNumber()
    {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.Serialize(
            new PhoneNumberIdentifier(phoneNumber));

        assertEquals(CommunicationIdentifierKind.PHONE_NUMBER, model.getKind());
        assertEquals(phoneNumber, model.getPhoneNumber());
    }

    @Test
    public void DeserializePhoneNumber()
    {
        final String phoneNumber = "+12223334444";
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.Deserialize(
            new CommunicationIdentifierModel().setKind(CommunicationIdentifierKind.PHONE_NUMBER)
                .setPhoneNumber(phoneNumber));

        assertEquals(PhoneNumberIdentifier.class, identifier.getClass());
        assertEquals(phoneNumber, ((PhoneNumberIdentifier)identifier).getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void SerializeMicrosoftTeamsUser(boolean isAnonymous)
    {
        CommunicationIdentifierModel model = CommunicationIdentifierSerializer.Serialize(
            new MicrosoftTeamsUserIdentifier(SOME_ID, isAnonymous));

        assertEquals(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER, model.getKind());
        assertEquals(SOME_ID, model.getMicrosoftTeamsUserId());
        assertEquals(isAnonymous, model.isAnonymous());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void DeserializerMicrosoftTeamsUser(boolean isAnonymous)
    {
        CommunicationIdentifier identifier = CommunicationIdentifierSerializer.Deserialize(
            new CommunicationIdentifierModel()
                .setMicrosoftTeamsUserId(SOME_ID)
                .setKind(CommunicationIdentifierKind.MICROSOFT_TEAMS_USER)
                .setIsAnonymous(isAnonymous));

        assertEquals(MicrosoftTeamsUserIdentifier.class, identifier.getClass());
        assertEquals(SOME_ID, ((MicrosoftTeamsUserIdentifier)identifier).getUserId());
        assertEquals(isAnonymous, ((MicrosoftTeamsUserIdentifier)identifier).isAnonymous());
    }
    
}
