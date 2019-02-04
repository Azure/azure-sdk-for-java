package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ErrorMessages;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ScmsRestriction;

public class ScmsRestrictionTests {

    @Test
    public void NewScmsRestrictionTests() {
        // Arrange
        byte expectedConfigurationData = 2;

        // Act
        ScmsRestriction scmsRestriction = new ScmsRestriction(
                expectedConfigurationData);
        byte resultConfigurationData = scmsRestriction.getConfigurationData();

        // Assert
        assertEquals(expectedConfigurationData, resultConfigurationData);
    }

    @Test
    public void BadConfigurationDataScmsRestrictionShouldThrown() {
        // Arrange
        byte expectedConfigurationData = 4;

        // Act
        try {
            @SuppressWarnings("unused")
            ScmsRestriction scmsRestriction = new ScmsRestriction(
                    expectedConfigurationData);
            fail("Should Thrown");

        } catch (IllegalArgumentException e) {
            // Assert
            assertEquals(e.getMessage(), ErrorMessages.INVALID_TWO_BIT_CONFIGURATION_DATA);
        }
    }
}
