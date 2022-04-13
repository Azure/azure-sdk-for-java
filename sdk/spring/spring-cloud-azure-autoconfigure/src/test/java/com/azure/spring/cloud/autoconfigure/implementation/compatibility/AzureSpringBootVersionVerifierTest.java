// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureSpringBootVersionVerifier.SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5;
import static com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureSpringBootVersionVerifier.SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class AzureSpringBootVersionVerifierTest {

    @ParameterizedTest
    @ValueSource(strings = { "2.6", "2.6.2", "2.6.x" })
    public void shouldMatchWhenManifestNumberAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {

            String getVersionFromManifest() {
                return "2.6.2";
            }
        };
        VerificationResult verificationResult = versionVerifier.verify();
        assertEquals(verificationResult.getDescription(), "");
        assertEquals(verificationResult.getAction(), "");
    }

    @ParameterizedTest
    @ValueSource(strings = { "2.6", "2.6.2", "2.6.x" })
    public void shouldNotMatchWhenManifestNumberAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "2.5.2";
            }
        };

        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }


    @ParameterizedTest
    @ValueSource(strings = { "2.5", "2.5.x", "2.6", "2.6.x" })
    public void shouldMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };

        AzureSpringBootVersionVerifier spyVerifier = spy(versionVerifier);

        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5)).thenReturn(true);
        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6)).thenReturn(true);

        VerificationResult verificationResult = spyVerifier.verify();

        assertEquals(verificationResult.getDescription(), "");
        assertEquals(verificationResult.getAction(), "");
    }


    @ParameterizedTest
    @ValueSource(strings = { "2.6", "2.6.x" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase1(String acceptedVersion) {
        List<String> acceptedVersions = Collections.singletonList(acceptedVersion);
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };

        AzureSpringBootVersionVerifier spyVerifier = spy(versionVerifier);

        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5)).thenReturn(true);
        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6)).thenReturn(false);

        assertThrows(AzureCompatibilityNotMetException.class, spyVerifier::verify);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2.6.2" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase2() {
        List<String> acceptedVersions = Collections.singletonList("2.6.2");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };

        AzureSpringBootVersionVerifier spyVerifier = spy(versionVerifier);

        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5)).thenReturn(true);
        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6)).thenReturn(true);

        assertThrows(AzureCompatibilityNotMetException.class, spyVerifier::verify);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2.5.2" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase3() {
        List<String> acceptedVersions = Collections.singletonList("2.6.2");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };

        AzureSpringBootVersionVerifier spyVerifier = spy(versionVerifier);

        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5)).thenReturn(true);
        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6)).thenReturn(true);

        assertThrows(AzureCompatibilityNotMetException.class, spyVerifier::verify);
    }


    @ParameterizedTest
    @ValueSource(strings = { "2.5", "2.5.x" })
    public void shouldNotMatchWhenManifestNumberNotPresentAndAcceptedNumberSpecifiedCase4() {
        List<String> acceptedVersions = Collections.singletonList("2.6.2");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };

        AzureSpringBootVersionVerifier spyVerifier = spy(versionVerifier);

        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5)).thenReturn(false);
        when(spyVerifier.test(SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6)).thenReturn(true);

        assertThrows(AzureCompatibilityNotMetException.class, spyVerifier::verify);
    }
}
