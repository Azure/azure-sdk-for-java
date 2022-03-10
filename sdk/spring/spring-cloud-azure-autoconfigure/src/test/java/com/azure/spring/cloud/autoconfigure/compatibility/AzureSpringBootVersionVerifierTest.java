// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureCompatibilityNotMetException;
import com.azure.spring.cloud.autoconfigure.implementation.compatibility.VerificationResult;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AzureSpringBootVersionVerifierTest {

    @Test
    public void shouldReadConcreteVersionFromManifest() {
        List<String> acceptedVersions = Collections.singletonList("2.6.2");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "2.6.2";
            }
        };
        VerificationResult verificationResult = versionVerifier.verify();
        assertEquals(verificationResult.description, "");
        assertEquals(verificationResult.action, "");
    }

    @Test
    public void shouldReadConcreteVersionFromManifestAndMatchItAgainstMinorVersion() {
        List<String> acceptedVersions = Collections.singletonList("2.6");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "2.6.2";
            }
        };
        VerificationResult verificationResult = versionVerifier.verify();
        assertEquals(verificationResult.description, "");
        assertEquals(verificationResult.action, "");
    }

    @Test
    public void shouldMatchAgainstPredicate() {
        List<String> acceptedVersions = Collections.singletonList("2.6");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.clear();
        versionVerifier.supportedVersions.put("2.6", () -> true);
        VerificationResult verificationResult = versionVerifier.verify();
        assertEquals(verificationResult.description, "");
        assertEquals(verificationResult.action, "");
    }

    @Test
    public void shouldMatchAgainstCurrentPredicateWithVersionEndingWithX() {
        List<String> acceptedVersions = Collections.singletonList("2.6.x");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.clear();
        versionVerifier.supportedVersions.put("2.6", () -> true);
        VerificationResult verificationResult = versionVerifier.verify();
        assertEquals(verificationResult.description, "");
        assertEquals(verificationResult.action, "");
    }

    @Test
    public void shouldReadConcreteVersionFromManifestWhenVersionIsNotMatched() {
        List<String> acceptedVersions = Collections.singletonList("2.5.8");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "2.6.2";
            }
        };
        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void shouldFailToMatchAgainstPredicateWhenNoneIsMatching() {
        List<String> acceptedVersions = Collections.singletonList("2.6");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.clear();
        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void shouldFailToMatchAgainstPredicateForNonCurrentVersions() {
        List<String> acceptedVersions = Collections.singletonList("2.6");
        AzureSpringBootVersionVerifier versionVerifier = new AzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.remove("2.6");
        assertThrows(AzureCompatibilityNotMetException.class, versionVerifier::verify);
    }
}
