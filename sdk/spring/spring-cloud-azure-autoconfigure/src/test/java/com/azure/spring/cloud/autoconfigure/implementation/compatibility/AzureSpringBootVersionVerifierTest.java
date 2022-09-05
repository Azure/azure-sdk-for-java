// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureSpringBootVersionVerifier.SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureSpringBootVersionVerifierTest {

    @ParameterizedTest
    @ValueSource(strings = { "3.0.0", "3.0.x", "3.0.0-M" })
    public void shouldMatchWhenManifestNumberAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {

            String getVersionFromManifest() {
                return "3.0.0-M4";
            }
        };
        VerificationResult verificationResult = versionVerifier.verify();
        assertEquals(verificationResult.getDescription(), "");
        assertEquals(verificationResult.getAction(), "");
    }

    @ParameterizedTest
    @ValueSource(strings = { "3.0.0", "3.0.x" })
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


    @ParameterizedTest
    @ValueSource(strings = { "3.0", "3.0.x" })
    public void shouldMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        when(mockResolver.resolve(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0)).thenReturn(true);

        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {
            String getVersionFromManifest() {
                return "";
            }
        };

        VerificationResult verificationResult = versionVerifier.verify();

        assertEquals(verificationResult.getDescription(), "");
        assertEquals(verificationResult.getAction(), "");
    }


    @ParameterizedTest
    @ValueSource(strings = { "3.1", "3.1.x" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        when(mockResolver.resolve(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0)).thenReturn(true);

        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {
            String getVersionFromManifest() {
                return "";
            }
        };

        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @ParameterizedTest
    @ValueSource(strings = { "3.0.0-M3" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase2(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        when(mockResolver.resolve(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0)).thenReturn(true);

        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {
            String getVersionFromManifest() {
                return "";
            }
        };

        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @ParameterizedTest
    @ValueSource(strings = { "3.0.0-M4" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase3(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        when(mockResolver.resolve(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0)).thenReturn(true);

        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {
            String getVersionFromManifest() {
                return "";
            }
        };

        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }


    @ParameterizedTest
    @ValueSource(strings = { "3.0", "3.0.x" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase4(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);
        when(mockResolver.resolve(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0)).thenReturn(false);

        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {
            String getVersionFromManifest() {
                return "";
            }
        };

        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }
}
