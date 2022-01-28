// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.junit.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpringCloudAzureSpringBootVersionVerifierTest {

    private List<String> acceptedVersions = Collections.singletonList("2.6.x");
    private SpringCloudAzureSpringBootVersionVerifier versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions);

    @Test
    public void shouldReadConcreteVersionFromManifest() {
        acceptedVersions = Collections.singletonList("2.6.2");
        versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        acceptedVersions = Collections.singletonList("2.6");
        versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        acceptedVersions = Collections.singletonList("2.6");
        versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
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
        acceptedVersions = Collections.singletonList("2.5.8");
        versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "2.6.2";
            }
        };
        assertThrows(SpringCloudAzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void shouldFailToMatchAgainstPredicateWhenNoneIsMatching() {
        acceptedVersions = Collections.singletonList("2.6");
        versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.clear();
        assertThrows(SpringCloudAzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void shouldFailToMatchAgainstPredicateForNonCurrentVersions() {
        acceptedVersions = Collections.singletonList("2.6");
        versionVerifier = new SpringCloudAzureSpringBootVersionVerifier(acceptedVersions) {
            String getVersionFromManifest() {
                return "";
            }
        };
        versionVerifier.supportedVersions.remove("2.6");
        assertThrows(SpringCloudAzureCompatibilityNotMetException.class, versionVerifier::verify);
    }

    @Test
    public void testIsVersion() {
        assertTrue(versionVerifier.is2_6().isCompatible());
        assertFalse(versionVerifier.is2_5().isCompatible());
    }
}
