// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.monitor;

import java.util.Comparator;

class OTelVersion {

    private static final Comparator<OTelVersion> VERSION_COMPARATOR = Comparator.comparingInt(OTelVersion::getMajorVersion)
        .thenComparing(OTelVersion::getMinorVersion)
        .thenComparing(OTelVersion::getPatchVersion);
    final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;


    private int getMajorVersion() {
        return majorVersion;
    }

    private int getMinorVersion() {
        return minorVersion;
    }

    private int getPatchVersion() {
        return patchVersion;
    }

    OTelVersion(String otelVersionAsString) {
        String[] versionComponents = otelVersionAsString.split("\\.");
        this.majorVersion = Integer.parseInt(versionComponents[0]);
        this.minorVersion = Integer.parseInt(versionComponents[1]);
        this.patchVersion = Integer.parseInt(versionComponents[2]);
    }

    boolean isLessThan(OTelVersion oTelVersion) {
        return VERSION_COMPARATOR.compare(this, oTelVersion) < 0;
    }

    boolean isGreaterThan(OTelVersion oTelVersion) {
        return VERSION_COMPARATOR.compare(this, oTelVersion) > 0;
    }

    boolean hasSameMajorVersionAs(OTelVersion oTelVersion) {
        return this.majorVersion == oTelVersion.majorVersion;
    }

}
