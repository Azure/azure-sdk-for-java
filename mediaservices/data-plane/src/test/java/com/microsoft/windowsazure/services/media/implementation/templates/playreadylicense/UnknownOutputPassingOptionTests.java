package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.UnknownOutputPassingOption;

public class UnknownOutputPassingOptionTests {

    @Test
    public void valueOfPlayReadyLicenseTypeTestsTests() {
        // provides full code coverage
        assertEquals(UnknownOutputPassingOption.NotAllowed, UnknownOutputPassingOption.valueOf("NotAllowed"));
        assertEquals(UnknownOutputPassingOption.Allowed, UnknownOutputPassingOption.valueOf("Allowed"));
        assertEquals(UnknownOutputPassingOption.AllowedWithVideoConstriction,
                UnknownOutputPassingOption.valueOf("AllowedWithVideoConstriction"));
    }

    @Test
    public void fromCodeNotAllowedUnknownOutputPassingOption() {
        // Arrange
        UnknownOutputPassingOption expectedUnknownOutputPassingOption = UnknownOutputPassingOption.NotAllowed;

        // Act
        UnknownOutputPassingOption unknownOutputPassingOptionResult = UnknownOutputPassingOption.fromCode(0);

        // Assert
        assertEquals(expectedUnknownOutputPassingOption, unknownOutputPassingOptionResult);
    }

    @Test
    public void fromCodeAllowedUnknownOutputPassingOptionTests() {
        // Arrange
        UnknownOutputPassingOption expectedUnknownOutputPassingOption = UnknownOutputPassingOption.Allowed;

        // Act
        UnknownOutputPassingOption unknownOutputPassingOptionResult = UnknownOutputPassingOption.fromCode(1);

        // Assert
        assertEquals(expectedUnknownOutputPassingOption, unknownOutputPassingOptionResult);
    }

    @Test
    public void fromCodeAllowedWithVideoConstrictionUnknownOutputPassingOptionTests() {
        // Arrange
        UnknownOutputPassingOption expectedUnknownOutputPassingOption = UnknownOutputPassingOption.AllowedWithVideoConstriction;

        // Act
        UnknownOutputPassingOption unknownOutputPassingOptionResult = UnknownOutputPassingOption.fromCode(2);

        // Assert
        assertEquals(expectedUnknownOutputPassingOption, unknownOutputPassingOptionResult);
    }

    @Test
    public void fromCodeInvalidUnknownOutputPassingOptionTests() {
        // Arrange
        int invalidCode = 666;
        String expectedMessage = "code";
        // Act
        try {
            @SuppressWarnings("unused")
            UnknownOutputPassingOption unknownOutputPassingOptionResult = UnknownOutputPassingOption
                    .fromCode(invalidCode);
            fail("Should throw");
        } catch (InvalidParameterException e) {
            // Assert
            assertEquals(e.getMessage(), expectedMessage);
        }
    }

    @Test
    public void getCodeJWTTokenTypeTests() {
        // Arrange
        int expectedCode = 2;

        // Act
        UnknownOutputPassingOption unknownOutputPassingOptionResult = UnknownOutputPassingOption.AllowedWithVideoConstriction;
        int resultCode = unknownOutputPassingOptionResult.getCode();

        // Assert
        assertEquals(resultCode, expectedCode);
    }
}
