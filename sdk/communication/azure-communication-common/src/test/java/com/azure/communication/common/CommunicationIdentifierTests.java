// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentifierTests {

    final String userId = "user id";
    final String fullId = "some lengthy id string";

    @Test
    public void defaultCloudIsPublicForMicrosoftTeamsUserIdentifier() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftTeamsUserIdentifier(userId, true).setRawId(fullId).getCloudEnvironment());
    }

    @Test
    public void rawIdTakesPrecedenceInEqualityCheck() {
        // Teams users
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true),
            new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true));
        assertNotEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true)
                .setRawId("Raw Id"),
            new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true)
                .setRawId("Another Raw Id"));
        assertEquals(new MicrosoftTeamsUserIdentifier("override", true)
                .setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"),
            new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true),
            new MicrosoftTeamsUserIdentifier("override", true).setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH).setRawId("8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.GCCH).setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD).setRawId("8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setCloudEnvironment(CommunicationCloudEnvironment.DOD).setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setRawId("8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertEquals(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  true),
            new MicrosoftTeamsUserIdentifier("override", false)
                .setRawId("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130"));

        // Phone numbers
        assertEquals(new PhoneNumberIdentifier("+14255550123"), new PhoneNumberIdentifier("+14255550123"));
        assertNotEquals(new PhoneNumberIdentifier("+14255550123").setRawId("Raw Id"),
            new PhoneNumberIdentifier("+14255550123").setRawId("Another Raw Id"));

        assertEquals(new PhoneNumberIdentifier("+override").setRawId("4:14255550123"),
            new PhoneNumberIdentifier("+14255550123"));
        assertEquals(new PhoneNumberIdentifier("+14255550123"),
            new PhoneNumberIdentifier("+override").setRawId("4:14255550123"));
    }

    @Test
    public void getRawIdOfIdentifier()
    {
        assertRawId(new CommunicationUserIdentifier("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new CommunicationUserIdentifier("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new CommunicationUserIdentifier("someFutureFormat"), "someFutureFormat");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130"), "8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130").setCloudEnvironment(CommunicationCloudEnvironment.DOD), "8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130").setCloudEnvironment(CommunicationCloudEnvironment.GCCH), "8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130",  false), "8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true), "8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true).setRawId("8:orgid:legacyFormat"), "8:orgid:legacyFormat");
        assertRawId(new PhoneNumberIdentifier("+112345556789"), "4:112345556789");
        assertRawId(new PhoneNumberIdentifier("+112345556789").setRawId("4:otherFormat"), "4:otherFormat");
        assertRawId(new UnknownIdentifier("28:45ab2481-1c1c-4005-be24-0ffb879b1130"), "28:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRawId(new UnknownIdentifier("someFutureFormat"), "someFutureFormat");
    }

    @Test
    public void createIdentifierFromRawId()
    {
        assertIdentifier("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:spool:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:spool:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:dod-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:dod-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130", new CommunicationUserIdentifier("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130"));
        assertIdentifier("8:acs:something", new CommunicationUserIdentifier("8:acs:something"));
        assertIdentifier("8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", false).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));
        assertIdentifier("8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", false).setCloudEnvironment(CommunicationCloudEnvironment.DOD));
        assertIdentifier("8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", false).setCloudEnvironment(CommunicationCloudEnvironment.GCCH));
        assertIdentifier("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130", new MicrosoftTeamsUserIdentifier("45ab2481-1c1c-4005-be24-0ffb879b1130", true).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));
        assertIdentifier("8:orgid:legacyFormat", new MicrosoftTeamsUserIdentifier("legacyFormat", false).setCloudEnvironment(CommunicationCloudEnvironment.PUBLIC));
        assertIdentifier("4:112345556789", new PhoneNumberIdentifier("+112345556789"));
        assertIdentifier("4:otherFormat", new PhoneNumberIdentifier("+otherFormat"));
        assertIdentifier("28:45ab2481-1c1c-4005-be24-0ffb879b1130", new UnknownIdentifier("28:45ab2481-1c1c-4005-be24-0ffb879b1130"));

        final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> CommunicationIdentifier.fromRawId(null));
        assertEquals("The parameter [rawId] cannot be null to empty.", illegalArgumentException.getMessage());
    }

    @Test
    public void rawIdStaysTheSameAfterConversionToIdentifierAndBack()
    {
        assertRoundTrip("8:acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:spool:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:dod-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:gcch-acs:bbbcbc1e-9f06-482a-b5d8-20e3f26ef0cd_45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:acs:something");
        assertRoundTrip("8:orgid:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:dod:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:gcch:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:teamsvisitor:45ab2481-1c1c-4005-be24-0ffb879b1130");
        assertRoundTrip("8:orgid:legacyFormat");
        assertRoundTrip("4:112345556789");
        assertRoundTrip("4:otherFormat");
        assertRoundTrip("28:45ab2481-1c1c-4005-be24-0ffb879b1130");
    }

    private void assertRawId(CommunicationIdentifier identifier, String expectedRawId)  {
        assertEquals(identifier.rawId, expectedRawId);
    }

    private void assertIdentifier(String rawId, CommunicationIdentifier expectedIdentifier)
    {
        assertEquals(CommunicationIdentifier.fromRawId(rawId), expectedIdentifier);
        assertEquals(CommunicationIdentifier.fromRawId(rawId).hashCode(), expectedIdentifier.hashCode());
    }

    private void assertRoundTrip(String rawId)
    {
        assertEquals(CommunicationIdentifier.fromRawId(rawId).getRawId(), rawId);
    }
}
