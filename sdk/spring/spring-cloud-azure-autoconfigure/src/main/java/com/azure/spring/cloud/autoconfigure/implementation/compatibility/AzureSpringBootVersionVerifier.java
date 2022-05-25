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

    static final String SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5 = "org.springframework.boot.context.properties.bind.Bindable.BindRestriction";

    static final String SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6 = "org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer";

    static final String SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_7 = "org.springframework.boot.autoconfigure.amqp.RabbitStreamTemplateConfigurer";
    /**
     * Versions supported by Spring Cloud Azure, for present is [2.5, 2.6]. Update this value if needed.
     */
    private final Map<String, String> supportedVersions = new HashMap<>();

    /**
     * Versionsspecified in the configuration or environment.
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
        supportedVersions.put("2.5", SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_5);
        supportedVersions.put("2.6", SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_6);
        supportedVersions.put("2.7", SPRINGBOOT_CONDITIONAL_CLASS_NAME_OF_2_7);
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
            + " Spring Cloud Azure release", versionFromManifest) : "Spring Boot is not compatible with this "
            + "Spring Cloud Azure release";
    }

    private String action() {
        return String.format("Change Spring Boot version to one of the following versions %s.%n"
                + "You can find the latest Spring Boot versions here [%s]. %n"
                + "If you want to learn more about the Spring Cloud Azure compatibility, "
                + "you can visit this page [%s] and check the [Which Version of Spring Cloud Azure Should I Use] section.%n"
                + "If you want to disable this check, "
                + "just set the property [spring.cloud.azure.compatibility-verifier.enabled=false]",
            this.acceptedVersions,
            "https://spring.io/projects/spring-boot#learn",
            "https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Versions-Mapping");
    }

    String getVersionFromManifest() {
        return SpringBootVersion.getVersion();
    }

    private boolean springBootVersionMatches() {
        for (String acceptedVersion : acceptedVersions) {
            try {
                if (this.matchSpringBootVersionFromManifest(acceptedVersion)) {
                    return true;
                }
            } catch (FileNotFoundException e) {
                String versionString = stripWildCardFromVersion(acceptedVersion);
                String fullyQualifiedClassName = this.supportedVersions.get(versionString);

                if (classNameResolver.resolve(fullyQualifiedClassName)) {
                    LOGGER.debug("Predicate for Spring Boot Version of [{}] was matched", versionString);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchSpringBootVersionFromManifest(String acceptedVersion) throws FileNotFoundException {
        String version = this.getVersionFromManifest();
        LOGGER.debug("Version found in Boot manifest [{}]", version);
        if (!StringUtils.hasText(version)) {
            LOGGER.info("Cannot check Boot version from manifest");
            throw new FileNotFoundException("Spring Boot version not found");
        } else {
            return version.startsWith(stripWildCardFromVersion(acceptedVersion));
        }
    }

    private static String stripWildCardFromVersion(String version) {
        return version.endsWith(".x") ? version.substring(0, version.indexOf(".x")) : version;
    }

}
