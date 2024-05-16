package com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.AgcAndColorStripeRestriction;
import com.microsoft.windowsazure.services.media.implementation.templates.playreadylicense.ErrorMessages;

public class AgcAndColorStripeRestrictionTests {

    @Test
    public void NewAgcAndColorStripeRestrictionTests() {
        // Arrange
        byte expectedConfigurationData = 2;

        // Act
        AgcAndColorStripeRestriction agcAndColorStripeRestriction = new AgcAndColorStripeRestriction(
                expectedConfigurationData);
        byte resultConfigurationData = agcAndColorStripeRestriction.getConfigurationData();

        // Assert
        assertEquals(expectedConfigurationData, resultConfigurationData);
    }

    @Test
    public void BadConfigurationDataAgcAndColorStripeRestrictionShouldThrown() {
        // Arrange
        byte expectedConfigurationData = 4;

        // Act
        try {
            @SuppressWarnings("unused")
            AgcAndColorStripeRestriction agcAndColorStripeRestriction = new AgcAndColorStripeRestriction(
                    expectedConfigurationData);
            fail("Should Thrown");

        } catch (IllegalArgumentException e) {
            // Assert
            assertEquals(e.getMessage(), ErrorMessages.INVALID_TWO_BIT_CONFIGURATION_DATA);
        }
    }
}
