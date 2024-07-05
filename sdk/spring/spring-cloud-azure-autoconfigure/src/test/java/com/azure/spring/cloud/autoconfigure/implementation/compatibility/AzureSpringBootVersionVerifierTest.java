// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureSpringBootVersionVerifier.SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith({OutputCaptureExtension.class})
class AzureSpringBootVersionVerifierTest {

    @ParameterizedTest
    @ValueSource(strings = { "3.0.0", "3.0.x", "3.0.0-M" })
    void shouldMatchWhenManifestNumberAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
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
    void shouldNotMatchWhenManifestNumberAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
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
    void shouldMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
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
    void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
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
    void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase2(String acceptedVersion) {
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
    void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase3(String acceptedVersion) {
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
    void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase4(String acceptedVersion) {
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

    @Test
    void testVersionVerifierLog(CapturedOutput capturedOutput) {
        List<String> acceptedVersions = Arrays.asList("2.5.x", "2.6.x", "2.7.x");
        ClassNameResolverPredicate mockResolver = mock(ClassNameResolverPredicate.class);

        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions,
            mockResolver) {
            String getVersionFromManifest() {
                return "2.6.2";
            }
        };
        versionVerifier.verify();
        String allOutput = capturedOutput.getAll();
        String log1 = "Currently running on Spring Boot version [2.6.2], trying to match it with Spring Cloud Azure accepted version [2.5.x].";
        String log2 = "Currently running on Spring Boot version [2.6.2], trying to match it with Spring Cloud Azure accepted version [2.6.x].";
        String log3 = "The current Spring Boot version matches Spring Cloud Azure accepted version [2.6.x].";
        assertTrue(allOutput.contains(log1) && allOutput.contains(log2) && allOutput.contains(log3));
    }
}
