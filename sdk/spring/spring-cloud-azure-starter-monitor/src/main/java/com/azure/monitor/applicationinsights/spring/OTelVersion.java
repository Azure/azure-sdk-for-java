// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.applicationinsights.spring;

class OTelVersion {

    private final String otelVersionAsString;
    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;

    OTelVersion(String otelVersionAsString) {
        this.otelVersionAsString = otelVersionAsString;
        String[] versionComponents = otelVersionAsString.split("\\.");
        this.majorVersion = Integer.parseInt(versionComponents[0]);
        this.minorVersion = Integer.parseInt(versionComponents[1]);
        this.patchVersion = Integer.parseInt(versionComponents[2]);
    }

    boolean isLessThan(OTelVersion oTelVersion) {
        if (this.otelVersionAsString.equals(oTelVersion.otelVersionAsString)) {
            return false;
        }
        return !isGreaterThan(oTelVersion);
    }

    boolean isGreaterThan(OTelVersion oTelVersion) {
        if (this.otelVersionAsString.equals(oTelVersion.otelVersionAsString)) {
            return false;
        }
        if (this.majorVersion > oTelVersion.majorVersion) {
            return true;
        }
        if (this.minorVersion > oTelVersion.minorVersion) {
            return true;
        }
        if (this.patchVersion > oTelVersion.patchVersion) {
            return true;
        }
        return false;
    }

    boolean hasSameMajorVersionAs(OTelVersion oTelVersion) {
        return this.majorVersion == oTelVersion.majorVersion;
    }

}
