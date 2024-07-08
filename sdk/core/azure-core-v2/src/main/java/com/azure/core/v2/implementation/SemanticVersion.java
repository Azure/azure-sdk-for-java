// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation;

import com.azure.core.v2.util.CoreUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Implements lightweight semantic version based on https://semver.org/ for internal use.
 */
public final class SemanticVersion implements Comparable<SemanticVersion> {

    /**
     * Represents unknown version - either invalid or missing.
     */
    public static final String UNKNOWN_VERSION = "unknown";

    private final int major;
    private final int minor;
    private final int patch;
    private final String prerelease;
    private final String versionString;

    /**
     * Returns implementation version of the package for given class. If version can't be retrieved or parsed, returns invalid version.
     *
     * @param className - class name to get package version of.
     * @return parsed {@link SemanticVersion} or invalid one.
     */
    public static SemanticVersion getPackageVersionForClass(String className) {
        try {
            return getPackageVersion(Class.forName(className));
        } catch (Exception ignored) {
            return SemanticVersion.createInvalid();
        }
    }

    /**
     * Parses semver 2.0.0 string. If version can't be retrieved or parsed, returns invalid version.
     *
     * @param version to parse.
     * @return parsed {@link SemanticVersion} or invalid one.
     */
    public static SemanticVersion parse(String version) {
        Objects.requireNonNull(version, "'version' cannot be null.");

        int majorDotIdx = version.indexOf('.');
        if (majorDotIdx < 0) {
            return createInvalid(version);
        }

        int minorDotIdx = version.indexOf('.', majorDotIdx + 1);
        if (minorDotIdx < 0) {
            return createInvalid(version);
        }

        int patchEndIdx = minorDotIdx + 1;
        while (patchEndIdx < version.length()) {
            char ch = version.charAt(patchEndIdx);

            // accommodate common broken semantic versions (e.g. 1.2.3.4)
            if (ch == '.' || ch == '-' || ch == '+') {
                break;
            }

            patchEndIdx++;
        }

        int extEndIdx = version.indexOf('+', patchEndIdx);
        if (extEndIdx < 0) {
            extEndIdx = version.length();
        }

        try {
            int major = Integer.parseInt(version.substring(0, majorDotIdx));
            int minor = Integer.parseInt(version.substring(majorDotIdx + 1, minorDotIdx));
            int patch = Integer.parseInt(version.substring(minorDotIdx + 1, patchEndIdx));

            String prerelease = (patchEndIdx == extEndIdx) ? "" : version.substring(patchEndIdx + 1, extEndIdx);
            return new SemanticVersion(major, minor, patch, prerelease, version);
        } catch (NumberFormatException ignored) {
            return createInvalid(version);
        }
    }

    /**
     * Returns implementation version of the package for given class.
     *
     * @param clazz - class to get package version of.
     * @return parsed {@link SemanticVersion} or invalid one.
     */
    private static SemanticVersion getPackageVersion(Class<?> clazz) {
        Objects.requireNonNull(clazz, "'clazz' cannot be null.");
        if (clazz.getPackage() == null) {
            return createInvalid();
        }

        String versionStr = clazz.getPackage().getImplementationVersion();

        if (versionStr != null) {
            return parse(versionStr);
        }

        // if versionStr is null, try loading the version from the manifest in the jar file
        try (JarFile jar = new JarFile(clazz.getProtectionDomain().getCodeSource().getLocation().getFile())) {
            Manifest manifest = jar.getManifest();
            versionStr = manifest.getMainAttributes().getValue("Implementation-Version");
            if (versionStr == null) {
                versionStr = manifest.getMainAttributes().getValue("Bundle-Version");
            }
            return parse(versionStr);
        } catch (IOException | SecurityException ignored) {
            return createInvalid();
        }
    }

    /**
     * Creates invalid semantic version.
     * @return instance of invalid semantic version.
     */
    public static SemanticVersion createInvalid() {
        return createInvalid(UNKNOWN_VERSION);
    }

    /**
     * Creates invalid semantic version.
     */
    private static SemanticVersion createInvalid(String version) {
        return new SemanticVersion(-1, -1, -1, null, version);
    }

    /**
     * Creates semantic version.
     *
     * @param major major version.
     * @param minor minor version.
     * @param patch patch version.
     * @param prerelease extensions (including '-' or '+' separator after patch).
     * @param versionString full version string.
     */
    SemanticVersion(int major, int minor, int patch, String prerelease, String versionString) {
        Objects.requireNonNull(versionString, "'versionString' cannot be null.");
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.prerelease = prerelease;
        this.versionString = versionString;
    }

    /**
     * Returns full version string that was used to create this {@code SemanticVersion}
     *
     * @return original version string.
     */
    public String getVersionString() {
        return versionString;
    }

    /**
     * Returns major version component or -1 for invalid version.
     *
     * @return major version.
     */
    public int getMajorVersion() {
        return major;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(SemanticVersion other) {
        if (this == other) {
            return 0;
        }

        if (other == null) {
            return -1;
        }

        if (major != other.major) {
            return major > other.major ? 1 : -1;
        }

        if (minor != other.minor) {
            return minor > other.minor ? 1 : -1;
        }

        if (patch != other.patch) {
            return patch > other.patch ? 1 : -1;
        }

        if (CoreUtils.isNullOrEmpty(prerelease)) {
            return CoreUtils.isNullOrEmpty(other.prerelease) ? 0 : 1;
        }

        if (CoreUtils.isNullOrEmpty(other.prerelease)) {
            return -1;
        }

        return prerelease.compareTo(other.prerelease);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SemanticVersion)) {
            return false;
        }

        SemanticVersion otherVer = (SemanticVersion) other;

        return versionString.equals(otherVer.versionString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return versionString.hashCode();
    }

    @Override
    public String toString() {
        return versionString;
    }

    /**
     * Returns flag indicating if version is valid.
     *
     * @return true if version is valid, false otherwise.
     */
    public boolean isValid() {
        return this.major >= 0;
    }
}
