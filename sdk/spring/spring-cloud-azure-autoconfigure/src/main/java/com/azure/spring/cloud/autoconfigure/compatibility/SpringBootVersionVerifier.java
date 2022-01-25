// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SpringBootVersionVerifier implements CompatibilityVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootVersionVerifier.class);
    final Map<String, CompatibilityPredicate> aCCEPTED_VERSIONS = new HashMap<String, CompatibilityPredicate>() {
        {
            this.put("2.5", SpringBootVersionVerifier.this.is2with5());
            this.put("2.6", SpringBootVersionVerifier.this.is2with6());
        }
    };
    private final List<String> acceptedVersions;

    SpringBootVersionVerifier(List<String> acceptedVersions) {
        this.acceptedVersions = acceptedVersions;
    }

    public VerificationResult verify() {
        if (this.springBootVersionMatches()) {
            return VerificationResult.compatible();
        } else {
            return VerificationResult.notCompatible(this.errorDescription(), this.action());
        }
    }

    private String errorDescription() {
        String versionFromManifest = this.getVersionFromManifest();
        return StringUtils.hasText(versionFromManifest) ? String.format("Spring Boot [%s] is not compatible with this Spring Cloud Azure release train", versionFromManifest) : "Spring Boot is not compatible with this Spring Cloud Azure release train";
    }

    private String action() {
        return String.format("Change Spring Boot version to one of the following versions %s .\nYou can find the latest Spring Boot versions here [%s]. \nIf you want to learn more about the Spring Cloud Azure Release train compatibility, you can visit this page [%s] and check the [Release Trains] section.\nIf you want to disable this check, "
            + "just set the property [spring.cloud.azure.compatibility-verifier.enabled=false]", this.acceptedVersions, "https://spring.io/projects/spring-boot#learn", "https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Versions-Mapping");
    }

    String getVersionFromManifest() {
        return SpringBootVersion.getVersion();
    }

    private boolean springBootVersionMatches() {
        for (String acceptedVersion : acceptedVersions) {
            Boolean versionFromManifest = this.bootVersionFromManifest(acceptedVersion);
            if (versionFromManifest != null && versionFromManifest) {
                return true;
            }
            if (versionFromManifest == null) {
                CompatibilityPredicate predicate = this.aCCEPTED_VERSIONS.get(stripWildCardFromVersion(acceptedVersion));
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

    private Boolean bootVersionFromManifest(String s) {
        String version = this.getVersionFromManifest();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Version found in Boot manifest [" + version + "]");
        }
        if (!StringUtils.hasText(version)) {
            LOGGER.info("Cannot check Boot version from manifest");
            return null;
        } else {
            return version.startsWith(stripWildCardFromVersion(s));
        }
    }

    static String stripWildCardFromVersion(String version) {
        return version.endsWith(".x") ? version.substring(0, version.indexOf(".x")) : version;
    }

    CompatibilityPredicate is2with6() {
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

    CompatibilityPredicate is2with5() {
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
}
