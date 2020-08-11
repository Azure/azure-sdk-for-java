package com.azure.digitaltwins.core;

import com.azure.core.util.ServiceVersion;

public enum DigitalTwinsServiceVersion implements ServiceVersion {
    V2020_05_31_preview("2020-05-31-preview");

    private final String version;

    DigitalTwinsServiceVersion(String version) {
        this.version = version;
    }

    public static DigitalTwinsServiceVersion getLatest() {
        return V2020_05_31_preview;
    }

    @Override
    public String getVersion() {
        return this.version;
    }
}
