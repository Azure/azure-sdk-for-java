// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringCloudAzureSpringBootVersionVerifierTest {

    @Test
    public void shouldReadConcreteVersionFromManifest() {
        List<String> acceptedVersions = Collections.singletonList("2.6.2");
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "2.6.2";
            }
        };
        assertThrows(SpringCloudAzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void shouldFailToMatchAgainstPredicateWhenNoneIsMatching() {
        List<String> acceptedVersions = Collections.singletonList("2.6");
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.clear();
        assertThrows(SpringCloudAzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void shouldFailToMatchAgainstPredicateForNonCurrentVersions() {
        List<String> acceptedVersions = Collections.singletonList("2.6");
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.remove("2.6");
        assertThrows(SpringCloudAzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void testIsVersion() {
        List<String> acceptedVersions = Collections.singletonList("2.6.x");
        SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions);
        assertTrue(versionVerifier.is2_6().isCompatible());
        assertFalse(versionVerifier.is2_5().isCompatible());
    }
}
