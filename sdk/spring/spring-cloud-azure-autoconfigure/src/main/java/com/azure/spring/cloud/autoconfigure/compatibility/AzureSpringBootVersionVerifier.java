// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureCompatibilityNotMetException;
import com.azure.spring.cloud.autoconfigure.implementation.compatibility.VerificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.util.StringUtils;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AzureSpringBootVersionVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureSpringBootVersionVerifier.class);
    final Map<String, CompatibilityPredicate> supportedVersions = new HashMap<String, CompatibilityPredicate>() {
        {
            this.put("2.5", AzureSpringBootVersionVerifier.this.is2_5());
            this.put("2.6", AzureSpringBootVersionVerifier.this.is2_6());
        }
    };
    private final List<String> acceptedVersions;

    AzureSpringBootVersionVerifier(List<String> acceptedVersions) {
        this.acceptedVersions = acceptedVersions;
    }

    /**
     * Verify the current spring-boot version
     * @return Verification result of spring-boot version
     * @throws AzureCompatibilityNotMetException thrown if using an unsupported spring-boot version
     */
    public VerificationResult verify() {
        if (this.springBootVersionMatches()) {
            return VerificationResult.compatible();
        } else {
            List<VerificationResult> errors = new ArrayList<>(Collections.singleton(VerificationResult.notCompatible(this.errorDescription(), this.action())));
            throw new AzureCompatibilityNotMetException(errors);
        }
    }

    private String errorDescription() {
        String versionFromManifest = this.getVersionFromManifest();
        return StringUtils.hasText(versionFromManifest) ? String.format("Spring Boot [%s] is not compatible with this Spring Cloud Azure release train", versionFromManifest) : "Spring Boot is not compatible with this Spring Cloud Azure release train";
    }

    private String action() {
        return String.format("Change Spring Boot version to one of the following versions %s .%n"
            + "You can find the latest Spring Boot versions here [%s]. %n"
            + "If you want to learn more about the Spring Cloud Azure Release train compatibility, "
            + "you can visit this page [%s] and check the [Release Trains] section.%nIf you want to disable this check, "
            + "just set the property [spring.cloud.azure.compatibility-verifier.enabled=false]", this.acceptedVersions,
            "https://spring.io/projects/spring-boot#learn", "https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Versions-Mapping");
    }

    String getVersionFromManifest() {
        return SpringBootVersion.getVersion();
    }

    private boolean springBootVersionMatches() {
        for (String acceptedVersion : acceptedVersions) {
            try {
                boolean matched = this.matchSpringBootVersionFromManifest(acceptedVersion);
                if (matched) {
                    return true;
                }
            } catch (FileNotFoundException e) {
                CompatibilityPredicate predicate = this.supportedVersions.get(stripWildCardFromVersion(acceptedVersion));
                if (predicate != null && predicate.isCompatible()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Predicate [" + predicate + "] was matched");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchSpringBootVersionFromManifest(String s) throws FileNotFoundException {
        String version = this.getVersionFromManifest();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Version found in Boot manifest [" + version + "]");
        }
        if (!StringUtils.hasText(version)) {
            LOGGER.info("Cannot check Boot version from manifest");
            throw new FileNotFoundException("Spring Boot version not found");
        } else {
            return version.startsWith(stripWildCardFromVersion(s));
        }
    }

    private static String stripWildCardFromVersion(String version) {
        return version.endsWith(".x") ? version.substring(0, version.indexOf(".x")) : version;
    }

    private CompatibilityPredicate is2_6() {
        return new CompatibilityPredicate() {
            public String toString() {
                return "Predicate for Boot 2.6";
            }
            public boolean isCompatible() {
                try {
                    Class.forName("org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer");
                    return true;
                } catch (ClassNotFoundException ex) {
                    return false;
                }
            }
        };
    }

    private CompatibilityPredicate is2_5() {
        return new CompatibilityPredicate() {
            public String toString() {
                return "Predicate for Boot 2.5";
            }
            public boolean isCompatible() {
                try {
                    Class.forName("org.springframework.boot.context.properties.bind.Bindable.BindRestriction");
                    return true;
                } catch (ClassNotFoundException ex) {
                    return false;
                }
            }
        };
    }

    @FunctionalInterface
    interface CompatibilityPredicate {

        /**
         * Compatible of the current spring-boot version
         * @return the version supported or not
         */
        boolean isCompatible();
    }
}
