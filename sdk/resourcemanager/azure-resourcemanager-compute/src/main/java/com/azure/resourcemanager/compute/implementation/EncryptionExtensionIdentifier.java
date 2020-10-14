// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.OperatingSystemTypes;

/** An internal type expose utility methods around encryption extension identifier. */
class EncryptionExtensionIdentifier {
    //
    private static final String ENCRYPTION_EXTENSION_PUBLISHER = "Microsoft.Azure.Security";
    //
    private static final String LINUX_LEGACY_ENCRYPTION_EXTENSION_VERSION = "0.1";
    private static final String LINUX_NOAAD_ENCRYPTION_EXTENSION_VERSION = "1.1";
    //
    private static final String WINDOWS_LEGACY_ENCRYPTION_EXTENSION_VERSION = "1.1";
    private static final String WINDOWS_NOAAD_ENCRYPTION_EXTENSION_VERSION = "2.2";
    //
    private static final String LINUX_ENCRYPTION_TYPE_NAME = "AzureDiskEncryptionForLinux";
    private static final String WINDOWS_ENCRYPTION_TYPE_NAME = "AzureDiskEncryption";

    /** @return encryption extension publisher name */
    static String publisherName() {
        return ENCRYPTION_EXTENSION_PUBLISHER;
    }

    /** @return OS specific encryption extension type */
    static String typeName(OperatingSystemTypes osType) {
        if (osType == OperatingSystemTypes.LINUX) {
            return LINUX_ENCRYPTION_TYPE_NAME;
        } else {
            return WINDOWS_ENCRYPTION_TYPE_NAME;
        }
    }

    /**
     * Given os type and no aad flag return the encryption extension version.
     *
     * @param osType os type
     * @param isNoAAD no aad flag
     * @return the encryption extension version
     */
    static String version(OperatingSystemTypes osType, boolean isNoAAD) {
        if (osType == OperatingSystemTypes.LINUX) {
            return isNoAAD ? LINUX_NOAAD_ENCRYPTION_EXTENSION_VERSION : LINUX_LEGACY_ENCRYPTION_EXTENSION_VERSION;
        } else {
            return isNoAAD ? WINDOWS_NOAAD_ENCRYPTION_EXTENSION_VERSION : WINDOWS_LEGACY_ENCRYPTION_EXTENSION_VERSION;
        }
    }

    /**
     * Checks whether the given version is a legacy extension version or no-aad extension version for the given OS type.
     *
     * @param osType os type
     * @param version extension version
     * @return true if legacy false otherwise.
     */
    static boolean isNoAADVersion(OperatingSystemTypes osType, String version) {
        final String majorVersion = version.split("\\.")[0];
        if (osType == OperatingSystemTypes.LINUX) {
            return majorVersion.equals(LINUX_NOAAD_ENCRYPTION_EXTENSION_VERSION.split("\\.")[0]);
        } else {
            return majorVersion.equals(WINDOWS_NOAAD_ENCRYPTION_EXTENSION_VERSION.split("\\.")[0]);
        }
    }

    /**
     * Checks whether the given publisher name is encryption publisher name.
     *
     * @param publisherName publisher name
     * @return true if the given publisher name is encryption publisher name, false otherwise
     */
    static boolean isEncryptionPublisherName(String publisherName) {
        return publisherName.equalsIgnoreCase(EncryptionExtensionIdentifier.publisherName());
    }

    /**
     * Checks whether the given type name is an encryption type name for given OS type.
     *
     * @param typeName type name
     * @param osType the os type
     * @return true if the given type name is encryption type name, false otherwise
     */
    static boolean isEncryptionTypeName(String typeName, OperatingSystemTypes osType) {
        return typeName.equalsIgnoreCase(EncryptionExtensionIdentifier.typeName(osType));
    }
}
