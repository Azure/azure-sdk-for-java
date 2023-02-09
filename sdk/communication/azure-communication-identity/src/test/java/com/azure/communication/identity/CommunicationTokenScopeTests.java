package com.azure.communication.identity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.identity.models.CommunicationTokenScope;

public class CommunicationTokenScopeTests {
    @Test
    public void communicationTokenScopeCanBeExtended() {
        // Arrange
        CommunicationTokenScope scope = CommunicationTokenScope.fromString("new");

        // Action
        var actual = scope.toString();

        // Assert
        assertEquals("new", actual);
    }

    @Test
    public void valuesContainWellKnownScopes() {
        // Arrange
        var expectedScope1 = CommunicationTokenScope.CHAT;
        var expectedScope2 = CommunicationTokenScope.VOIP;

        // Action
        var actual = CommunicationTokenScope.values();

        // Assert
        assertTrue(actual.contains(expectedScope1));
        assertTrue(actual.contains(expectedScope2));
    }
}
