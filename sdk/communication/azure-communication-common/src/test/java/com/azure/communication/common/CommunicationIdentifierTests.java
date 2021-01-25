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
    public void equalityOnlyTestIdIfPresentOnBothSide() {
        assertEquals(new MicrosoftTeamsUserIdentifier(userId, true).setId(fullId),
            new MicrosoftTeamsUserIdentifier(userId, true));
        assertEquals(new MicrosoftTeamsUserIdentifier(userId, true),
            new MicrosoftTeamsUserIdentifier(userId, true));
        assertEquals(new MicrosoftTeamsUserIdentifier(userId, true),
            new MicrosoftTeamsUserIdentifier(userId, true).setId(fullId));
        assertNotEquals(new MicrosoftTeamsUserIdentifier(userId, true).setId(fullId),
            new MicrosoftTeamsUserIdentifier(userId, true).setId("another id"));

        assertEquals(new PhoneNumberIdentifier("+12223334444").setId(fullId),
            new PhoneNumberIdentifier("+12223334444"));
        assertEquals(new PhoneNumberIdentifier("+12223334444"), new PhoneNumberIdentifier("+12223334444"));
        assertEquals(new PhoneNumberIdentifier("+12223334444"),
            new PhoneNumberIdentifier("+12223334444").setId(fullId));
        assertNotEquals(new PhoneNumberIdentifier("+12223334444").setId(fullId),
            new PhoneNumberIdentifier("+12223334444").setId("another id"));
    }

    @Test
    public void defaultCloudIsPublicForMicrosoftTeamsUserIdentifier() {
        assertEquals(CommunicationCloudEnvironment.PUBLIC,
            new MicrosoftTeamsUserIdentifier(userId, true).setId(fullId).getCloudEnvironment());
    }
}
