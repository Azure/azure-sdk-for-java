// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IdentityUtil class
 */
public class IdentityUtilTests {

    public static void main(String[] args) {
        // Should prefer messages containing 'Suggestion' (case-insensitive)
        String output = "{\"type\":\"consoleMessage\",\"timestamp\":\"2025-08-18T15:08:14.4849845-07:00\",\"data\":{\"message\":\"\\nERROR: fetching token: AADSTS50076: Due to a configuration change made by your administrator, or because you moved to a new location, you must use multi-factor authentication to access 'tenant-id'. Trace ID: trace-id Correlation ID: correlation-id Timestamp: 2025-08-18 22:08:14Z\\n\"}}\n" +
                "{\"type\":\"consoleMessage\",\"timestamp\":\"2025-08-18T15:08:14.4849845-07:00\",\"data\":{\"message\":\"Suggestion: re-authentication required, run `azd auth login` to acquire a new token.\\n\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Suggestion: re-authentication required, run `azd auth login` to acquire a new token.", result);
    }

    @Test
    public void testExtractSuggestionMessagePreferred() {
        // Should prefer messages containing 'Suggestion' (case-insensitive)
        String output = "{\"type\":\"consoleMessage\",\"timestamp\":\"2025-08-18T15:08:14.4849845-07:00\",\"data\":{\"message\":\"\\nERROR: fetching token: AADSTS50076: Due to a configuration change made by your administrator, or because you moved to a new location, you must use multi-factor authentication to access 'tenant-id'. Trace ID: trace-id Correlation ID: correlation-id Timestamp: 2025-08-18 22:08:14Z\\n\"}}\n" +
                "{\"type\":\"consoleMessage\",\"timestamp\":\"2025-08-18T15:08:14.4849845-07:00\",\"data\":{\"message\":\"Suggestion: re-authentication required, run `azd auth login` to acquire a new token.\\n\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Suggestion: re-authentication required, run `azd auth login` to acquire a new token.", result);
    }

    @Test
    public void testExtractSuggestionCaseInsensitive() {
        // Should find 'suggestion' in any case
        String output = "{\"type\":\"consoleMessage\",\"data\":{\"message\":\"First message\"}}\n" +
                "{\"type\":\"consoleMessage\",\"data\":{\"message\":\"SUGGESTION: Try running azd auth login\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("SUGGESTION: Try running azd auth login", result);
    }

    @Test
    public void testExtractLastMessageWhenNoSuggestion() {
        // Should return last message when multiple messages but no suggestion
        String output = "{\"type\":\"consoleMessage\",\"data\":{\"message\":\"First error message\"}}\n" +
                "{\"type\":\"consoleMessage\",\"data\":{\"message\":\"Second error message\"}}\n" +
                "{\"type\":\"consoleMessage\",\"data\":{\"message\":\"Third error message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Third error message", result);
    }

    @Test
    public void testExtractFirstMessageWhenOnlyOne() {
        // Should return first message when only one exists
        String output = "{\"type\":\"consoleMessage\",\"data\":{\"message\":\"Only error message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Only error message", result);
    }

    @Test
    public void testExtractMessageFromNestedData() {
        // Should extract message from nested data structure
        String output = "{\"type\":\"consoleMessage\",\"data\":{\"message\":\"Error in nested data\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Error in nested data", result);
    }

    @Test
    public void testExtractMessageFromRootLevel() {
        // Should extract message from root level of JSON
        String output = "{\"message\":\"Root level error message\"}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Root level error message", result);
    }

    @Test
    public void testExtractMixedMessageLocations() {
        // Should handle messages at different JSON levels
        String output = "{\"message\":\"Root level message\"}\n" +
                "{\"data\":{\"message\":\"Nested message\"}}\n" +
                "{\"data\":{\"message\":\"suggestion: Use this suggestion\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("suggestion: Use this suggestion", result);
    }

    @Test
    public void testIgnoreEmptyMessages() {
        // Should ignore empty or whitespace-only messages
        String output = "{\"data\":{\"message\":\"   \"}}\n" +
                "{\"data\":{\"message\":\"\"}}\n" +
                "{\"data\":{\"message\":\"Valid message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Valid message", result);
    }

    @Test
    public void testIgnoreNonJsonLines() {
        // Should ignore lines that are not valid JSON
        String output = "This is not JSON\n" +
                "{\"data\":{\"message\":\"Valid JSON message\"}}\n" +
                "Another non-JSON line\n" +
                "{\"data\":{\"message\":\"Suggestion: Another valid message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Suggestion: Another valid message", result);
    }

    @Test
    public void testIgnoreNonStringMessages() {
        // Should ignore messages that are not strings
        String output = "{\"data\":{\"message\":123}}\n" +
                "{\"data\":{\"message\":{\"nested\":\"object\"}}}\n" +
                "{\"data\":{\"message\":\"Valid string message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Valid string message", result);
    }

    @Test
    public void testIgnoreEmptyLines() {
        // Should ignore empty lines and whitespace-only lines
        String output = "{\"data\":{\"message\":\"First message\"}}\n" +
                "\n" +
                "{\"data\":{\"message\":\"Second message\"}}\n";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Second message", result);
    }

    @Test
    public void testSanitizeTokenInOutput() {
        // Should sanitize tokens in the extracted message
        String output = "{\"data\":{\"message\":\"Error with token: abc123token in message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertNotNull(result);
        // Note: The actual redaction behavior depends on IdentityUtil.redactInfo implementation
        // This test just verifies the method doesn't return null and processes the input
        assertTrue(result.length() > 0);
    }

    @Test
    public void testReturnNullForNoValidMessages() {
        // Should return null when no valid messages found
        String output = "{\"data\":{\"notamessage\":\"Not a message\"}}\n" +
                "{\"nomessage\":\"Also not a message\"}\n" +
                "This is not JSON";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertNull(result);
    }

    @Test
    public void testReturnNullForEmptyOutput() {
        // Should return null for empty output
        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput("");
        assertNull(result);
    }

    @Test
    public void testReturnNullForNullOutput() {
        // Should return null for null output
        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(null);
        assertNull(result);
    }

    @Test
    public void testReturnNullForWhitespaceOnlyOutput() {
        // Should return null for whitespace-only output
        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput("   \n\n   \t  ");
        assertNull(result);
    }

    @Test
    public void testComplexRealWorldExample() {
        // Should handle complex real-world azd output
        String output = "{\"type\":\"consoleMessage\",\"timestamp\":\"2025-08-18T15:08:14.4849845-07:00\",\"data\":{\"message\":\"\\nERROR: fetching token: AADSTS50076: Due to a configuration change made by your administrator, or because you moved to a new location, you must use multi-factor authentication to access 'tenant-id'. Trace ID: trace-id Correlation ID: correlation-id Timestamp: 2025-08-18 22:08:14Z\\n\"}}\n" +
                "{\"type\":\"consoleMessage\",\"timestamp\":\"2025-08-18T15:08:14.4849845-07:00\",\"data\":{\"message\":\"Suggestion: re-authentication required, run `azd auth login` to acquire a new token.\\n\"}}\n" +
                "{\"type\":\"progress\",\"data\":{\"activity\":\"Cleaning up\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Suggestion: re-authentication required, run `azd auth login` to acquire a new token.", result);
    }

    @Test
    public void testStripWhitespaceFromMessages() {
        // Should strip leading and trailing whitespace from messages
        String output = "{\"data\":{\"message\":\"  \\n  Error message with whitespace  \\n  \"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Error message with whitespace", result);
    }

    @Test
    public void testHandleMalformedJsonGracefully() {
        // Should handle malformed JSON lines gracefully
        String output = "{\"data\":{\"message\":\"First valid message\"}}\n" +
                "{\"malformed\":\"json\"without\"closing\"brace\"\n" +
                "{\"data\":{\"message\":\"suggestion: This should be found\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("suggestion: This should be found", result);
    }

    @Test
    public void testMultipleSuggestionMessages() {
        // Should return the first suggestion message found
        String output = "{\"data\":{\"message\":\"First message\"}}\n" +
                "{\"data\":{\"message\":\"Suggestion: First suggestion\"}}\n" +
                "{\"data\":{\"message\":\"Another suggestion: Second suggestion\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Suggestion: First suggestion", result);
    }

    @Test
    public void testSuggestionWithDifferentCasing() {
        // Should find suggestion with various casing
        String output = "{\"data\":{\"message\":\"Regular message\"}}\n" +
                "{\"data\":{\"message\":\"sUgGeStIoN: Mixed case suggestion\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("sUgGeStIoN: Mixed case suggestion", result);
    }

    @Test
    public void testNestedJsonObjects() {
        // Should handle nested JSON structures properly
        String output = "{\"outer\":{\"data\":{\"message\":\"This should not be found\"}}}\n" +
                "{\"data\":{\"message\":\"This should be found\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("This should be found", result);
    }

    @Test
    public void testMessageWithSpecialCharacters() {
        // Should handle messages with special characters
        String output = "{\"data\":{\"message\":\"Error: Special chars !@#$%^&*()+ message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Error: Special chars !@#$%^&*()+ message", result);
    }

    @Test
    public void testMessageWithUnicodeCharacters() {
        // Should handle messages with Unicode characters
        String output = "{\"data\":{\"message\":\"Erreur: Caractères unicode éñ message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Erreur: Caractères unicode éñ message", result);
    }

    @Test
    public void testEmptyDataObject() {
        // Should handle empty data objects
        String output = "{\"data\":{}}\n" +
                "{\"data\":{\"message\":\"Valid message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Valid message", result);
    }

    @Test
    public void testMixedValidAndInvalidJson() {
        // Should handle mix of valid and invalid JSON gracefully
        String output = "{\"data\":{\"message\":\"First valid message\"}}\n" +
                "not json at all\n" +
                "{\"incomplete\": \"json\n" +
                "{\"data\":{\"message\":\"Suggestion: Final message\"}}";

        String result = IdentityUtil.extractUserFriendlyErrorFromAzdOutput(output);
        assertEquals("Suggestion: Final message", result);
    }
}