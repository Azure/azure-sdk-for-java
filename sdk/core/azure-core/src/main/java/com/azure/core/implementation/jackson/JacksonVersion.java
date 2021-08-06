// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.SemanticVersion;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * Provides information about Jackson package versions used, detects and logs errors.
 */
final class JacksonVersion {
    private SemanticVersion annotationsVersion;
    private SemanticVersion coreVersion;
    private SemanticVersion databindVersion;
    private SemanticVersion xmlVersion;
    private SemanticVersion jsr310Version;

    private static final String ANNOTATIONS_PACKAGE_NAME = "jackson-annotations";
    private static final String CORE_PACKAGE_NAME = "jackson-core";
    private static final String DATABIND_PACKAGE_NAME = "jackson-databind";
    private static final String XML_PACKAGE_NAME = "jackson-dataformat-xml";
    private static final String JSR310_PACKAGE_NAME = "jackson-datatype-jsr310";

    private static final SemanticVersion MIN_SUPPORTED_VERSION = SemanticVersion.parse("2.10.0");
    private static final SemanticVersion MAX_SUPPORTED_VERSION = SemanticVersion.parse("2.12.4");

    private static final String AZURE_CORE_PROPERTIES_NAME = "azure-core.properties";
    private static final String AZURE_CORE_PROPERTIES_VERSION_KEY = "version";

    private static final String AZURE_CORE_VERSION = CoreUtils
        .getProperties(AZURE_CORE_PROPERTIES_NAME)
        .getOrDefault(AZURE_CORE_PROPERTIES_VERSION_KEY, SemanticVersion.UNKNOWN_VERSION);

    private final String helpString;
    private final ClientLogger logger = new ClientLogger(JacksonVersion.class);

    private JacksonVersion() {
        annotationsVersion = getVersion("com.fasterxml.jackson.annotation.JsonProperty");
        coreVersion = getVersion("com.fasterxml.jackson.core.JsonGenerator");
        databindVersion = getVersion("com.fasterxml.jackson.databind.ObjectMapper");
        xmlVersion = getVersion("com.fasterxml.jackson.dataformat.xml.XmlMapper");
        jsr310Version = getVersion("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule");
        checkVersion(annotationsVersion, ANNOTATIONS_PACKAGE_NAME);
        checkVersion(coreVersion, CORE_PACKAGE_NAME);
        checkVersion(databindVersion, DATABIND_PACKAGE_NAME);
        checkVersion(xmlVersion, XML_PACKAGE_NAME);
        checkVersion(jsr310Version, JSR310_PACKAGE_NAME);
        helpString = formatHelpString();
        logger.info(helpString);
    }

    /**
     * Returns help info containing actual detected package versions.
     *
     * @return diagnostics information with detected versions.
     */
    public String getHelpInfo() {
        return helpString;
    }

    /**
     * Gets {@code SemanticVersion} for given class name
     */
    private SemanticVersion getVersion(String className) {
        try {
            return SemanticVersion.getPackageVersionForClass(className);
        } catch (Throwable e) {
            logger.warning("Failed to retrieve package version for class {}", className, e);
            return SemanticVersion.createInvalid();
        }
    }

    private static JacksonVersion instance = null;

    /**
     * Gets {@code JacksonVersion} instance singleton.
     */
    public static synchronized JacksonVersion getInstance() {
        if (instance == null) {
            instance = new JacksonVersion();
        }

        return instance;
    }

    /**
     * Checks package version and logs if any issues detected.
     */
    private void checkVersion(SemanticVersion version, String packageName) {
        if (!version.isValid()) {
            logger.warning("Could not find version of '{}'.", packageName);
        }

        if (version.compareTo(MIN_SUPPORTED_VERSION) < 0) {
            logger.error("Version '{}' of package '{}' is not supported (too old), please upgrade.", version.getVersionString(), packageName);
        }

        if (version.getMajorVersion() > MAX_SUPPORTED_VERSION.getMajorVersion()) {
            logger.error("Major version '{}' of package '{}' is newer than latest supported version - '{}'.",
                version.getVersionString(),
                packageName,
                MAX_SUPPORTED_VERSION.getVersionString());
        }
    }

    /**
     * Generates help information with versions detected in runtime.
     */
    private String formatHelpString() {
        // TODO(limolkova): add  link to troubleshooting docs
        return new StringBuilder()
            .append("Package versions: ")
            .append(ANNOTATIONS_PACKAGE_NAME)
            .append("=")
            .append(annotationsVersion.getVersionString())
            .append(", ")
            .append(CORE_PACKAGE_NAME)
            .append("=")
            .append(coreVersion.getVersionString())
            .append(", ")
            .append(DATABIND_PACKAGE_NAME)
            .append("=")
            .append(databindVersion.getVersionString())
            .append(", ")
            .append(XML_PACKAGE_NAME)
            .append("=")
            .append(xmlVersion.getVersionString())
            .append(", ")
            .append(JSR310_PACKAGE_NAME)
            .append("=")
            .append(jsr310Version.getVersionString())
            .append(", ")
            .append("azure-core=")
            .append(AZURE_CORE_VERSION)
            .toString();
    }
}
