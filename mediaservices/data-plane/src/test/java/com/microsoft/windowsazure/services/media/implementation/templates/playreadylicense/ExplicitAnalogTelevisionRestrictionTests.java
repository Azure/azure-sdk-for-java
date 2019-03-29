package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ErrorMessages;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ExplicitAnalogTelevisionRestriction;

public class ExplicitAnalogTelevisionRestrictionTests {

    @Test
    public void NewExplicitAnalogTelevisionRestrictionTest() {
        // Arrange
        boolean expectedBestEffort = true;
        byte expectedConfigurationData = 2;

        // Act
        ExplicitAnalogTelevisionRestriction explicitAnalogTelevisionRestriction = new ExplicitAnalogTelevisionRestriction(
                expectedBestEffort, expectedConfigurationData);
        boolean resultBestEffort = explicitAnalogTelevisionRestriction.isBestEffort();
        byte resultConfigurationData = explicitAnalogTelevisionRestriction.getConfigurationData();

        // Assert
        assertEquals(expectedBestEffort, resultBestEffort);
        assertEquals(expectedConfigurationData, resultConfigurationData);
    }

    @Test
    public void GetterSetterExplicitAnalogTelevisionRestrictionTest() {
        // Arrange
        boolean expectedBestEffort = true;
        byte expectedConfigurationData = 2;

        // Act
        ExplicitAnalogTelevisionRestriction explicitAnalogTelevisionRestriction = new ExplicitAnalogTelevisionRestriction(
                false, expectedConfigurationData);
        explicitAnalogTelevisionRestriction.setBestEffort(expectedBestEffort);
        boolean resultBestEffort = explicitAnalogTelevisionRestriction.isBestEffort();
        byte resultConfigurationData = explicitAnalogTelevisionRestriction.getConfigurationData();

        // Assert
        assertEquals(expectedBestEffort, resultBestEffort);
        assertEquals(expectedConfigurationData, resultConfigurationData);
    }

    @Test
    public void BadConfigurationDataExplicitAnalogTelevisionRestrictionShouldThrown() {
        // Arrange
        byte expectedConfigurationData = 4;

        // Act
        try {
            @SuppressWarnings("unused")
            ExplicitAnalogTelevisionRestriction explicitAnalogTelevisionRestriction = new ExplicitAnalogTelevisionRestriction(
                    false, expectedConfigurationData);
            fail("Should Thrown");

            // Assert
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), ErrorMessages.INVALID_TWO_BIT_CONFIGURATION_DATA);
        }
    }
}
