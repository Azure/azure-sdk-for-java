// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.applicationinsights.spring;

class OTelVersion {

    private final String oTelVersionAsString;
    private final int majorVersion;
    private final int minorVersionVersion;
    private final int subMinorVersion;

    OTelVersion(String oTelVersionAsString) {
        this.oTelVersionAsString = oTelVersionAsString;
        String[] versionComponents = oTelVersionAsString.split("\\.");
        this.majorVersion = Integer.parseInt(versionComponents[0]);
        this.minorVersionVersion = Integer.parseInt(versionComponents[1]);
        this.subMinorVersion = Integer.parseInt(versionComponents[2]);
    }

    boolean isLessThan(OTelVersion oTelVersion) {
        if(this.oTelVersionAsString.equals(oTelVersion.oTelVersionAsString)) {
            return false;
        }
        return !isGreaterThan(oTelVersion);
    }

    boolean isGreaterThan(OTelVersion oTelVersion) {
        if(this.oTelVersionAsString.equals(oTelVersion.oTelVersionAsString)) {
            return false;
        }
        if(this.majorVersion > oTelVersion.majorVersion) {
            return true;
        }
        if(this.minorVersionVersion > oTelVersion.minorVersionVersion) {
            return true;
        }
        if(this.subMinorVersion > oTelVersion.subMinorVersion) {
            return true;
        }
        return false;
    }

}
