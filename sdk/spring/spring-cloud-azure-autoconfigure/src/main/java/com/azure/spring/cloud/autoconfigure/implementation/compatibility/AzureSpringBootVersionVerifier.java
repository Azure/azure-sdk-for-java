// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

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

public class AzureSpringBootVersionVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureSpringBootVersionVerifier.class);

    static final String SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0 = "org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer";
    static final String SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_1 = "org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer.ValidationConfigurationCustomizer,setIgnoreRegistrationFailure,";
    static final String SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_2 = "org.springframework.boot.autoconfigure.web.client.RestClientSsl";
    static final String SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_3 = "org.springframework.boot.autoconfigure.ldap.PropertiesLdapConnectionDetails";
    /**
     * Versions supported by Spring Cloud Azure, for present is [3.0, 3.1, 3.2, 3.3]. Update this value if needed.
     */
    private final Map<String, String> supportedVersions = new HashMap<>();

    /**
     * Versions specified in the configuration or environment.
     */
    private final List<String> acceptedVersions;

    private final ClassNameResolverPredicate classNameResolver;

    public AzureSpringBootVersionVerifier(List<String> acceptedVersions, ClassNameResolverPredicate classNameResolver) {
        this.acceptedVersions = acceptedVersions;
        this.classNameResolver = classNameResolver;
        initDefaultSupportedBootVersionCheckMeta();
    }


    /**
     * Init default supported Spring Boot Version compatibility check meta data.
     */
    private void initDefaultSupportedBootVersionCheckMeta() {
        supportedVersions.put("3.0", SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_0);
        supportedVersions.put("3.1", SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_1);
        supportedVersions.put("3.2", SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_2);
        supportedVersions.put("3.3", SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_3_3);
    }

    /**
     * Verify the current spring-boot version
     *
     * @return Verification result of spring-boot version
     * @throws AzureCompatibilityNotMetException thrown if using an unsupported spring-boot version
     */
    public VerificationResult verify() {
        if (this.springBootVersionMatches()) {
            return VerificationResult.compatible();
        } else {
            List<VerificationResult> errors =
                new ArrayList<>(Collections.singleton(VerificationResult.notCompatible(this.errorDescription(),
                    this.action())));
            throw new AzureCompatibilityNotMetException(errors);
        }
    }

    private String errorDescription() {
        String versionFromManifest = this.getVersionFromManifest();
        return StringUtils.hasText(versionFromManifest) ? String.format("Spring Boot [%s] is not compatible with this"
            + " Spring Cloud Azure version.", versionFromManifest) : "Spring Boot is not compatible with this "
            + "Spring Cloud Azure version.";
    }

    private String action() {
        return String.format("Change Spring Boot version to one of the following versions %s.%n"
                + "You can find the latest Spring Boot versions here [%s].%n"
                + "If you want to learn more about the Spring Cloud Azure compatibility, "
                + "you can visit this page [%s] and check the [Which Version of Spring Cloud Azure Should I Use] "
                + "section.%n If you want to disable this check, "
                + "just set the property [spring.cloud.azure.compatibility-verifier.enabled=false].",
            this.acceptedVersions,
            "https://spring.io/projects/spring-boot#learn",
            "https://aka.ms/spring/versions");
    }

    String getVersionFromManifest() {
        return SpringBootVersion.getVersion();
    }

    private boolean springBootVersionMatches() {
        for (String acceptedVersion : acceptedVersions) {
            try {
                if (this.matchSpringBootVersionFromManifest(acceptedVersion)) {
                    LOGGER.debug("The current Spring Boot version matches Spring Cloud Azure accepted version [{}].",
                        acceptedVersion);
                    return true;
                }
            } catch (FileNotFoundException e) {
                String versionString = stripWildCardFromVersion(acceptedVersion);
                String fullyQualifiedClassName = this.supportedVersions.get(versionString);

                if (classNameResolver.resolve(fullyQualifiedClassName)) {
                    LOGGER.debug("Predicate for Spring Boot Version of [{}] was matched.", versionString);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchSpringBootVersionFromManifest(String acceptedVersion) throws FileNotFoundException {
        String version = this.getVersionFromManifest();
        LOGGER.debug("Currently running on Spring Boot version [{}], trying to match it with Spring Cloud Azure "
            + "accepted version [{}].", version, acceptedVersion);
        if (!StringUtils.hasText(version)) {
            throw new FileNotFoundException("Spring Boot version not found");
        } else {
            return version.startsWith(stripWildCardFromVersion(acceptedVersion));
        }
    }

    private static String stripWildCardFromVersion(String version) {
        return version.endsWith(".x") ? version.substring(0, version.indexOf(".x")) : version;
    }

}
