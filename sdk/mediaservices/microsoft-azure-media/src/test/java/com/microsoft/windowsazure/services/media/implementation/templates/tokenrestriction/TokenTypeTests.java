package com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.templates.tokenrestriction.TokenType;

public class TokenTypeTests {

    @Test
    public void valueOfUndefinedTokenTypeTests() {
        // provides full code coverage
        assertEquals(TokenType.Undefined, TokenType.valueOf("Undefined"));
        assertEquals(TokenType.SWT, TokenType.valueOf("SWT"));
        assertEquals(TokenType.JWT, TokenType.valueOf("JWT"));
    }

    @Test
    public void fromCodeUndefinedTokenTypeTests() {
        // Arrange
        TokenType expectedTokenType = TokenType.Undefined;

        // Act
        TokenType tokenTypeResult = TokenType.fromCode(0);

        // Assert
        assertEquals(tokenTypeResult, expectedTokenType);
    }

    @Test
    public void fromCodeSWTTokenTypeTests() {
        // Arrange
        TokenType expectedTokenType = TokenType.SWT;

        // Act
        TokenType tokenTypeResult = TokenType.fromCode(1);

        // Assert
        assertEquals(tokenTypeResult, expectedTokenType);
    }

    @Test
    public void fromCodeJWTTokenTypeTests() {
        // Arrange
        TokenType expectedTokenType = TokenType.JWT;

        // Act
        TokenType tokenTypeResult = TokenType.fromCode(2);

        // Assert
        assertEquals(tokenTypeResult, expectedTokenType);
    }

    @Test
    public void fromCodeInvalidTokenTypeTests() {
        // Arrange
        int invalidCode = 666;
        String expectedMessage = "code";
        // Act
        try {
            @SuppressWarnings("unused")
            TokenType tokenTypeResult = TokenType.fromCode(invalidCode);
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
        TokenType tokenTypeResult = TokenType.JWT;
        int resultCode = tokenTypeResult.getCode();

        // Assert
        assertEquals(resultCode, expectedCode);
    }
}
