// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class SpringBootVersionVerifier implements CompatibilityVerifier {
    private static final Log log = LogFactory.getLog(SpringBootVersionVerifier.class);
    final Map<String, CompatibilityPredicate> ACCEPTED_VERSIONS = new HashMap<String, CompatibilityPredicate>(){
        {
            this.put("2.5", SpringBootVersionVerifier.this.is2_5());
            this.put("2.6", SpringBootVersionVerifier.this.is2_6());
        }
    };
    private final List<String> acceptedVersions;

    SpringBootVersionVerifier(List<String> acceptedVersions) {
        this.acceptedVersions = acceptedVersions;
    }

    public VerificationResult verify() {
        boolean matches = this.springBootVersionMatches();
        return matches ? VerificationResult.compatible() : VerificationResult.notCompatible(this.errorDescription(), this.action());
    }

    private String errorDescription() {
        String versionFromManifest = this.getVersionFromManifest();
        return StringUtils.hasText(versionFromManifest) ? String.format("Spring Boot [%s] is not compatible with this Spring Cloud Azure release train", versionFromManifest) : "Spring Boot is not compatible with this Spring Cloud Azure release train";
    }

    private String action() {
        return String.format("Change Spring Boot version to one of the following versions %s .\nYou can find the latest Spring Boot versions here [%s]. \nIf you want to learn more about the Spring Cloud Azure Release train compatibility, you can visit this page [%s] and check the [Release Trains] section.\nIf you want to disable this check, just set the property "
            + "[spring.cloud.azure.compatibility-verifier.enabled=false]", this.acceptedVersions, "https://spring.io/projects/spring-boot#learn", "https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Versions-Mapping");
    }

    String getVersionFromManifest() {
        return SpringBootVersion.getVersion();
    }

    private boolean springBootVersionMatches() {
        Iterator<String> var1 = this.acceptedVersions.iterator();

        while(var1.hasNext()) {
            String acceptedVersion = var1.next();
            Boolean versionFromManifest = this.bootVersionFromManifest(acceptedVersion);
            if (versionFromManifest != null && versionFromManifest) {
                return true;
            }

            if (versionFromManifest == null) {
                CompatibilityPredicate predicate = this.ACCEPTED_VERSIONS.get(stripWildCardFromVersion(acceptedVersion));
                if (predicate != null && predicate.isCompatible()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Predicate [" + predicate + "] was matched");
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private Boolean bootVersionFromManifest(String s) {
        String version = this.getVersionFromManifest();
        if (log.isDebugEnabled()) {
            log.debug("Version found in Boot manifest [" + version + "]");
        }

        if (!StringUtils.hasText(version)) {
            log.info("Cannot check Boot version from manifest");
            return null;
        } else {
            return version.startsWith(stripWildCardFromVersion(s));
        }
    }

    static String stripWildCardFromVersion(String version) {
        return version.endsWith(".x") ? version.substring(0, version.indexOf(".x")) : version;
    }

    CompatibilityPredicate is2_6() {
        return new CompatibilityPredicate() {
            public String toString() {
                return "Predicate for Boot 2.6";
            }

            public boolean isCompatible() {
                try {
                    Class.forName("org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer");
                    return true;
                } catch (ClassNotFoundException var2) {
                    return false;
                }
            }
        };
    }

    CompatibilityPredicate is2_5() {
        return new CompatibilityPredicate() {
            public String toString() {
                return "Predicate for Boot 2.5";
            }

            public boolean isCompatible() {
                try {
                    Class.forName("org.springframework.boot.context.properties.bind.Bindable.BindRestriction");
                    return true;
                } catch (ClassNotFoundException var2) {
                    return false;
                }
            }
        };
    }
}
