// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.credentials;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CredentialValidatorTest {
    private static final ClientLogger LOGGER = new ClientLogger(CredentialValidatorTest.class);

    // Mock credentials for testing
    private static final StorageSharedKeyCredential SHARED_KEY = new StorageSharedKeyCredential("account", "key");
    private static final TokenCredential TOKEN = new MockTokenCredential();
    private static final AzureSasCredential AZURE_SAS = new AzureSasCredential("sig=test");
    private static final String SAS_TOKEN = "sv=2021-01-01&sig=test";

    @ParameterizedTest
    @MethodSource("singleCredentialValidCases")
    public void validateSingleCredentialIsPresentValid(StorageSharedKeyCredential sharedKey, TokenCredential token,
        AzureSasCredential azureSas, String sasToken) {
        assertDoesNotThrow(
            () -> CredentialValidator.validateSingleCredentialIsPresent(sharedKey, token, azureSas, sasToken, LOGGER));
    }

    @ParameterizedTest
    @MethodSource("singleCredentialInvalidCases")
    public void validateSingleCredentialIsPresentInvalid(StorageSharedKeyCredential sharedKey, TokenCredential token,
        AzureSasCredential azureSas, String sasToken) {
        assertThrows(IllegalStateException.class,
            () -> CredentialValidator.validateSingleCredentialIsPresent(sharedKey, token, azureSas, sasToken, LOGGER));
    }

    @ParameterizedTest
    @MethodSource("credentialsNotAmbiguousValidCases")
    public void validateCredentialsNotAmbiguousValid(StorageSharedKeyCredential sharedKey, TokenCredential token,
        AzureSasCredential azureSas, String sasToken) {
        assertDoesNotThrow(
            () -> CredentialValidator.validateCredentialsNotAmbiguous(sharedKey, token, azureSas, sasToken, LOGGER));
    }

    @ParameterizedTest
    @MethodSource("credentialsNotAmbiguousInvalidCases")
    public void validateCredentialsNotAmbiguousInvalid(StorageSharedKeyCredential sharedKey, TokenCredential token,
        AzureSasCredential azureSas, String sasToken) {
        assertThrows(IllegalStateException.class,
            () -> CredentialValidator.validateCredentialsNotAmbiguous(sharedKey, token, azureSas, sasToken, LOGGER));
    }

    private static Stream<Arguments> singleCredentialValidCases() {
        return Stream.of(
            // No credentials valid according to the method as written
            Arguments.of(null, null, null, null),
            // Single credential of each type
            Arguments.of(SHARED_KEY, null, null, null),
            Arguments.of(null, TOKEN, null, null),
            Arguments.of(null, null, AZURE_SAS, null),
            Arguments.of(null, null, null, SAS_TOKEN));
    }

    private static Stream<Arguments> singleCredentialInvalidCases() {
        return Stream.of(
            // Two credentials
            Arguments.of(SHARED_KEY, TOKEN, null, null), Arguments.of(SHARED_KEY, null, AZURE_SAS, null),
            Arguments.of(SHARED_KEY, null, null, SAS_TOKEN), Arguments.of(null, TOKEN, AZURE_SAS, null),
            Arguments.of(null, TOKEN, null, SAS_TOKEN), Arguments.of(null, null, AZURE_SAS, SAS_TOKEN),
            // Three credentials
            Arguments.of(SHARED_KEY, TOKEN, AZURE_SAS, null), Arguments.of(SHARED_KEY, TOKEN, null, SAS_TOKEN),
            Arguments.of(SHARED_KEY, null, AZURE_SAS, SAS_TOKEN), Arguments.of(null, TOKEN, AZURE_SAS, SAS_TOKEN),
            // Four credentials
            Arguments.of(SHARED_KEY, TOKEN, AZURE_SAS, SAS_TOKEN));
    }

    private static Stream<Arguments> credentialsNotAmbiguousValidCases() {
        return Stream.of(
            // No credentials valid according to the method as written
            Arguments.of(null, null, null, null),
            // Single credential of each type
            Arguments.of(SHARED_KEY, null, null, null),
            Arguments.of(null, TOKEN, null, null),
            Arguments.of(null, null, AZURE_SAS, null),
            Arguments.of(null, null, null, SAS_TOKEN),
            // Valid two-credential combinations (TokenCredential + XOR of SAS types)
            Arguments.of(null, TOKEN, AZURE_SAS, null),
            Arguments.of(null, TOKEN, null, SAS_TOKEN));
    }

    private static Stream<Arguments> credentialsNotAmbiguousInvalidCases() {
        return Stream.of(
            // Invalid two-credential combinations
            Arguments.of(SHARED_KEY, TOKEN, null, null),
            Arguments.of(SHARED_KEY, null, AZURE_SAS, null),
            Arguments.of(SHARED_KEY, null, null, SAS_TOKEN),
            Arguments.of(null, null, AZURE_SAS, SAS_TOKEN),
            // TokenCredential + both SAS types (violates XOR). Also 3 arguments
            Arguments.of(null, TOKEN, AZURE_SAS, SAS_TOKEN),
            // Three credentials
            Arguments.of(SHARED_KEY, TOKEN, AZURE_SAS, null),
            Arguments.of(SHARED_KEY, TOKEN, null, SAS_TOKEN),
            Arguments.of(SHARED_KEY, null, AZURE_SAS, SAS_TOKEN),
            // Four credentials
            Arguments.of(SHARED_KEY, TOKEN, AZURE_SAS, SAS_TOKEN));
    }
}
