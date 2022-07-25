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
    private final SemanticVersion coreVersion;
    private final SemanticVersion databindVersion;
    private final SemanticVersion xmlVersion;
    private final SemanticVersion jsr310Version;

    private static final String CORE_PACKAGE_NAME = "jackson-core";
    private static final String DATABIND_PACKAGE_NAME = "jackson-databind";
    private static final String XML_PACKAGE_NAME = "jackson-dataformat-xml";
    private static final String JSR310_PACKAGE_NAME = "jackson-datatype-jsr310";
    private static final String TROUBLESHOOTING_DOCS_LINK = "https://aka.ms/azsdk/java/dependency/troubleshoot";

    private static final SemanticVersion MIN_SUPPORTED_VERSION = SemanticVersion.parse("2.10.0");
    private static final int MAX_SUPPORTED_MAJOR_VERSION = 2;

    private static final String AZURE_CORE_PROPERTIES_NAME = "azure-core.properties";
    private static final String AZURE_CORE_PROPERTIES_VERSION_KEY = "version";

    private static final String AZURE_CORE_VERSION = CoreUtils
        .getProperties(AZURE_CORE_PROPERTIES_NAME)
        .getOrDefault(AZURE_CORE_PROPERTIES_VERSION_KEY, SemanticVersion.UNKNOWN_VERSION);

    private static final ClientLogger LOGGER = new ClientLogger(JacksonVersion.class);

    private static JacksonVersion instance = null;

    private final String helpString;

    private JacksonVersion() {
        coreVersion = SemanticVersion.parse(
            new com.fasterxml.jackson.core.json.PackageVersion().version().toString());
        databindVersion = SemanticVersion.parse(
            new com.fasterxml.jackson.databind.cfg.PackageVersion().version().toString());
        xmlVersion = SemanticVersion.parse(
            new com.fasterxml.jackson.dataformat.xml.PackageVersion().version().toString());
        jsr310Version = SemanticVersion.parse(
            new com.fasterxml.jackson.datatype.jsr310.PackageVersion().version().toString());
        checkVersion(coreVersion, CORE_PACKAGE_NAME);
        checkVersion(databindVersion, DATABIND_PACKAGE_NAME);
        checkVersion(xmlVersion, XML_PACKAGE_NAME);
        checkVersion(jsr310Version, JSR310_PACKAGE_NAME);
        helpString = formatHelpString();
        LOGGER.info(helpString);
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
            LOGGER.verbose("Could not find version of '{}'.", packageName);
            return;
        }

        if (version.compareTo(MIN_SUPPORTED_VERSION) < 0) {
            LOGGER.error("Version '{}' of package '{}' is not supported (older than earliest supported version - `{}`)"
                + ", please upgrade.", version.getVersionString(), packageName, MIN_SUPPORTED_VERSION);
        }

        if (version.getMajorVersion() > MAX_SUPPORTED_MAJOR_VERSION) {
            LOGGER.error("Major version '{}' of package '{}' is newer than latest supported version - '{}'.",
                version.getVersionString(),
                packageName,
                MAX_SUPPORTED_MAJOR_VERSION);
        }
    }

    /**
     * Generates help information with versions detected in runtime.
     */
    private String formatHelpString() {
        return "Package versions: "
            + CORE_PACKAGE_NAME + "=" + coreVersion.getVersionString() + ", "
            + DATABIND_PACKAGE_NAME + "=" + databindVersion.getVersionString() + ", "
            + XML_PACKAGE_NAME + "=" + xmlVersion.getVersionString() + ", "
            + JSR310_PACKAGE_NAME + "=" + jsr310Version.getVersionString() + ", "
            + "azure-core=" + AZURE_CORE_VERSION + ", "
            + "Troubleshooting version conflicts: " + TROUBLESHOOTING_DOCS_LINK;
    }
}
