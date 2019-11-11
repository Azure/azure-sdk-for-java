package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import static org.junit.Assert.*;

import org.junit.Test;

public class TokenClaimTests {
    
    @Test
    public void defaultConstructorTest() {
        // Arrange and Act
        TokenClaim value = new TokenClaim();
        // Asset
        assertNotNull(value);
    }

    @Test
    public void constructorTest() {
        // Arrange
        String expectedType = "type";
        String expectedValue = "value";
        // Act
        TokenClaim value = new TokenClaim(expectedType, expectedValue);

        // Assert
        assertNotNull(value);
        assertEquals(value.getClaimType(), expectedType);
        assertEquals(value.getClaimValue(), expectedValue);
    }

    @Test
    public void nullInConstructorShouldThrownTest() {
     // Arrange
        String providedType = null;
        String expectedValue = "value";
        
        // Act
        try { 
            @SuppressWarnings("unused")
            TokenClaim value = new TokenClaim(providedType, expectedValue);
            fail("Should thrown");
        } catch (NullPointerException e) {
            // Assert
            assertTrue(e.getMessage().contains("claimType"));
        }
    }
    
    @Test
    public void staticValuesTest() {
        // Arrange
        String expectedTokenClaimType = "urn:microsoft:azure:mediaservices:contentkeyidentifier";

        // Act
        String results = TokenClaim.getContentKeyIdentifierClaimType();

        // Assert
        assertEquals(results, expectedTokenClaimType);
    }
    
    @Test
    public void staticValues2Test() {
        // Arrange
        String expectedTokenClaimType = "urn:microsoft:azure:mediaservices:contentkeyidentifier";
        TokenClaim claim = TokenClaim.getContentKeyIdentifierClaim();
        
        // Act
        String results = claim.getClaimType();

        // Assert
        assertEquals(results, expectedTokenClaimType);
    }
}
