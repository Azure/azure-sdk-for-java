package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.PlayReadyLicenseType;

public class PlayReadyLicenseTypeTests {

    @Test
    public void valueOfPlayReadyLicenseTypeTestsTests() {
        // provides full code coverage
        assertEquals(PlayReadyLicenseType.Nonpersistent, PlayReadyLicenseType.valueOf("Nonpersistent"));
        assertEquals(PlayReadyLicenseType.Persistent, PlayReadyLicenseType.valueOf("Persistent"));
    }

    @Test
    public void fromCodeNonpersistentPlayReadyLicenseTypeTest() {
        // Arrange
        PlayReadyLicenseType expectedPlayReadyLicenseType = PlayReadyLicenseType.Nonpersistent;

        // Act
        PlayReadyLicenseType playReadyLicenseType = PlayReadyLicenseType.fromCode(0);

        // Assert
        assertEquals(playReadyLicenseType, expectedPlayReadyLicenseType);
    }

    @Test
    public void fromCodePersistentPlayReadyLicenseTypeTest() {
        // Arrange
        PlayReadyLicenseType expectedPlayReadyLicenseType = PlayReadyLicenseType.Persistent;

        // Act
        PlayReadyLicenseType playReadyLicenseType = PlayReadyLicenseType.fromCode(1);

        // Assert
        assertEquals(playReadyLicenseType, expectedPlayReadyLicenseType);
    }

    @Test
    public void fromCodeInvalidTokenTypeTests() {
        // Arrange
        int invalidCode = 666;
        String expectedMessage = "code";
        // Act
        try {
            @SuppressWarnings("unused")
            PlayReadyLicenseType tokenPlayReadyLicenseType = PlayReadyLicenseType.fromCode(invalidCode);
            fail("Should throw");
        } catch (InvalidParameterException e) {
            // Assert
            assertEquals(e.getMessage(), expectedMessage);
        }
    }

    @Test
    public void getCodePersistentPlayReadyLicenseTypeTests() {
        // Arrange
        int expectedCode = 1;

        // Act
        PlayReadyLicenseType tokenTypeResult = PlayReadyLicenseType.Persistent;
        int resultCode = tokenTypeResult.getCode();

        // Assert
        assertEquals(resultCode, expectedCode);
    }
}
