// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.util.Objects;

/**
 * Implements lightweight semantic version based on https://semver.org/ for internal use.
 */
public final class SemanticVersion implements Comparable<SemanticVersion> {

    /**
     * Represents unknown version - either invalid or missing.
     */
    public static final String UNKNOWN_VERSION = "unknown";

    /**
     * Returns implementation version of the package for given class.
     *
     * @param className - class name to get package version of.
     * @return parsed {@link SemanticVersion} or invalid one.
     */
    public static SemanticVersion getPackageVersionForClass(String className) {
        Objects.requireNonNull(className, "'className' cannot be null.");
        try {
            return SemanticVersion.getPackageVersion(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return createInvalid();
        }
    }

    /**
     * Parses semver 2.0.0 string.
     *
     * @param version to parse.
     * @return parsed {@link SemanticVersion} or invalid one.
     */
    public static SemanticVersion parse(String version) {
        Objects.requireNonNull(version, "'version' cannot be null.");
        String[] parts = version.split("\\.");
        if (parts.length < 3) {
            return createInvalid(version);
        }

        int majorDotIdx = version.indexOf('.');
        int minorDotIdx = version.indexOf('.', majorDotIdx + 1);
        if (majorDotIdx < 0 || minorDotIdx < 0) {
            return createInvalid(version);
        }

        int patchEndIdx = version.indexOf('-', minorDotIdx + 1);
        int extEndIdx = version.indexOf('+', minorDotIdx + 1);

        if (patchEndIdx < 0) {
            patchEndIdx = version.length();
        }

        if (extEndIdx < 0) {
            extEndIdx = version.length();
        }

        patchEndIdx = Math.min(patchEndIdx, extEndIdx);


        try {
            Integer major = Integer.valueOf(version.substring(0, majorDotIdx));
            Integer minor = Integer.valueOf(version.substring(majorDotIdx + 1, minorDotIdx));
            Integer patch = Integer.valueOf(version.substring(minorDotIdx + 1, patchEndIdx));

            return new SemanticVersion(major, minor, patch, version.substring(patchEndIdx, extEndIdx), version);
        } catch (Throwable ex) {
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
        if (versionStr == null) {
            return createInvalid();
        }

        return parse(versionStr);
    }

    private final int major;
    private final int minor;
    private final int patch;
    private final String prerelease;
    private final String versionString;

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

        if (isStringNullOrEmpty(prerelease)) {
            return isStringNullOrEmpty(other.prerelease) ? 0 : 1;
        }

        if (isStringNullOrEmpty(other.prerelease)) {
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

    /**
     * Returns flag indicating if version is valid.
     *
     * @return true if version is valid, false otherwise.
     */
    public boolean isValid() {
        return this.major >= 0;
    }

    private static boolean isStringNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
