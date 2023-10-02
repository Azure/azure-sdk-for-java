// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.json;

import com.typespec.json.implementation.jackson.core.Version;
import com.typespec.json.implementation.jackson.core.Versioned;
import com.typespec.json.implementation.jackson.core.util.VersionUtil;

/**
 * Automatically generated from PackageVersion.java.in during
 * packageVersion-generate execution of maven-replacer-plugin in
 * pom.xml.
 */
public final class PackageVersion implements Versioned {
    public final static Version VERSION = VersionUtil.parseVersion(
        "2.13.2", "com.azure.json.implementation.jackson.core", "jackson-core");

    @Override
    public Version version() {
        return VERSION;
    }
}
