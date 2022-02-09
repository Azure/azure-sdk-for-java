// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CommunicationIdentifierTests {

    final String userId = "user id";
    final String fullId = "some lengthy id string";

    @Test
    public void equalityOnlyTestRawIdIfPresentOnBothSide() {
        assertEquals(new MicrosoftTeamsUserIdentifier(userId, true).setRawId(fullId),
            new MicrosoftTeamsUserIdentifier(userId, true));
        assertEquals(new MicrosoftTeamsUserIdentifier(userId, true),
            new MicrosoftTeamsUserIdentifier(userId, true));
        assertEquals(new MicrosoftTeamsUserIdentifier(userId, true),
            new MicrosoftTeamsUserIdentifier(userId, true).setRawId(fullId));
        assertNotEquals(new MicrosoftTeamsUserIdentifier(userId, true).setRawId(fullId),
            new MicrosoftTeamsUserIdentifier(userId, true).setRawId("another id"));

        assertEquals(new PhoneNumberIdentifier("+12223334444").setRawId(fullId),
            new PhoneNumberIdentifier("+12223334444"));
        assertEquals(new PhoneNumberIdentifier("+12223334444"), new PhoneNumberIdentifier("+12223334444"));
        assertEquals(new PhoneNumberIdentifier("+12223334444"),
            new PhoneNumberIdentifier("+12223334444").setRawId(fullId));
        assertNotEquals(new PhoneNumberIdentifier("+12223334444").setRawId(fullId),
            new PhoneNumberIdentifier("+12223334444").setRawId("another id"));
    }

    @Test
    public void defaultCloudIsPublicForMicrosoftTeamsUserIdentifier() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftTeamsUserIdentifier(userId, true).setRawId(fullId).getCloudEnvironment());
    }
}
