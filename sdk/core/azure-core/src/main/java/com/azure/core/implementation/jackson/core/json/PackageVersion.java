package com.azure.core.implementation.jackson.core.json;

import com.azure.core.implementation.jackson.core.Version;
import com.azure.core.implementation.jackson.core.Versioned;
import com.azure.core.implementation.jackson.core.util.VersionUtil;

/**
 * Automatically generated from PackageVersion.java.in during
 * packageVersion-generate execution of maven-replacer-plugin in
 * pom.xml.
 */
public final class PackageVersion implements Versioned {
    public final static Version VERSION = VersionUtil.parseVersion(
        "2.13.2", "com.azure.core.implementation.jackson.core", "jackson-core");

    @Override
    public Version version() {
        return VERSION;
    }
}
