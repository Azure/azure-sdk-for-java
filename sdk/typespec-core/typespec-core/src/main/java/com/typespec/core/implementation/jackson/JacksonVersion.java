// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.implementation.SemanticVersion;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;

/**
 * Provides information about Jackson package versions used, detects and logs errors.
 */
final class JacksonVersion {
    private static final String CORE_PACKAGE_NAME = "jackson-core";
    private static final String DATABIND_PACKAGE_NAME = "jackson-databind";
    private static final String XML_PACKAGE_NAME = "jackson-dataformat-xml";
    private static final String JSR310_PACKAGE_NAME = "jackson-datatype-jsr310";
    private static final String TROUBLESHOOTING_DOCS_LINK = "https://aka.ms/azsdk/java/dependency/troubleshoot";

    private static final SemanticVersion MIN_SUPPORTED_VERSION = SemanticVersion.parse("2.10.0");
    private static final int MAX_SUPPORTED_MAJOR_VERSION = 2;

    private static final String AZURE_CORE_PROPERTIES_NAME = "azure-core.properties";
    private static final String AZURE_CORE_PROPERTIES_VERSION_KEY = "version";

    private static final String AZURE_CORE_VERSION = CoreUtils.getProperties(AZURE_CORE_PROPERTIES_NAME)
        .getOrDefault(AZURE_CORE_PROPERTIES_VERSION_KEY, SemanticVersion.UNKNOWN_VERSION);

    private static final ClientLogger LOGGER = new ClientLogger(JacksonVersion.class);

    private static final String HELP_STRING;

    static {
        SemanticVersion coreVersion = SemanticVersion.parse(com.fasterxml.jackson.core.json.PackageVersion.VERSION
            .toString());
        SemanticVersion databindVersion = SemanticVersion.parse(com.fasterxml.jackson.databind.cfg.PackageVersion
            .VERSION.toString());
        SemanticVersion jsr310Version = SemanticVersion.parse(com.fasterxml.jackson.datatype.jsr310.PackageVersion
            .VERSION.toString());

        SemanticVersion xmlVersion1;
        try {
            Class<?> xmlPackageVersion = Class.forName("com.fasterxml.jackson.dataformat.xml.PackageVersion");
            xmlVersion1 = SemanticVersion.parse(xmlPackageVersion.getDeclaredField("VERSION").get(null).toString());
        } catch (ReflectiveOperationException e) {
            xmlVersion1 = SemanticVersion.createInvalid();
        }
        SemanticVersion xmlVersion = xmlVersion1;

        checkVersion(coreVersion, CORE_PACKAGE_NAME);
        checkVersion(databindVersion, DATABIND_PACKAGE_NAME);
        checkVersion(xmlVersion, XML_PACKAGE_NAME);
        checkVersion(jsr310Version, JSR310_PACKAGE_NAME);

        HELP_STRING = "Package versions: "
            + CORE_PACKAGE_NAME + "=" + coreVersion.getVersionString() + ", "
            + DATABIND_PACKAGE_NAME + "=" + databindVersion.getVersionString() + ", "
            + XML_PACKAGE_NAME + "=" + xmlVersion.getVersionString() + ", "
            + JSR310_PACKAGE_NAME + "=" + jsr310Version.getVersionString() + ", "
            + "azure-core=" + AZURE_CORE_VERSION + ", "
            + "Troubleshooting version conflicts: " + TROUBLESHOOTING_DOCS_LINK;

        LOGGER.info(HELP_STRING);
    }

    private JacksonVersion() {
    }

    /**
     * Returns help info containing actual detected package versions.
     *
     * @return diagnostics information with detected versions.
     */
    public static String getHelpInfo() {
        return HELP_STRING;
    }

    /**
     * Checks package version and logs if any issues detected.
     */
    private static void checkVersion(SemanticVersion version, String packageName) {
        if (!version.isValid()) {
            LOGGER.verbose("Could not find version of '{}'.", packageName);
            return;
        }

        if (version.compareTo(MIN_SUPPORTED_VERSION) < 0) {
            LOGGER.warning("Version '{}' of package '{}' is not supported (older than earliest supported version - `{}`"
                    + "). It may result in runtime exceptions during serialization. Please consider updating Jackson "
                    + "to one of the supported versions {}",
                version.getVersionString(), packageName, MIN_SUPPORTED_VERSION, TROUBLESHOOTING_DOCS_LINK);
        }

        if (version.getMajorVersion() > MAX_SUPPORTED_MAJOR_VERSION) {
            LOGGER.warning("Major version '{}' of package '{}' is newer than latest supported version - '{}'."
                + " It may result in runtime exceptions during serialization.",
                version.getVersionString(), packageName, MAX_SUPPORTED_MAJOR_VERSION);
        }
    }
}
