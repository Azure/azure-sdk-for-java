// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AzureSpringBootVersionVerifierTest {

    @ParameterizedTest
    @ValueSource(strings = { "3.0.0" })
    public void shouldMatchWhenManifestNumberAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {

            String getVersionFromManifest() {
                return "3.0.0M3";
            }
        };
        VerificationResult verificationResult = versionVerifier.verify();
        assertEquals(verificationResult.getDescription(), "");
        assertEquals(verificationResult.getAction(), "");
    }

    @ParameterizedTest
    @ValueSource(strings = { "3.0.0" })
    public void shouldNotMatchWhenManifestNumberAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {
            String getVersionFromManifest() {
                return "2.7.2";
            }
        };

        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }
}
