package com.azure.messaging.eventgrid;

import com.azure.core.util.ServiceVersion;

public enum EventGridServiceVersion implements ServiceVersion {

    V2018_01_01("2018_01_01");

    private String version;

    EventGridServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public static EventGridServiceVersion getLatest() {
        return V2018_01_01;
    }
}
