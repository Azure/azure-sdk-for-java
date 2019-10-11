package com.azure.storage.common;

public enum ServiceVersion {
    V2018_11_09("2011-08-18");

    ServiceVersion(String version) { }

    public static ServiceVersion getLatest() {
        return V2018_11_09;
    }
}
