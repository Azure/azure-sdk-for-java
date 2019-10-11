package com.azure.security.keyvault.keys;

public enum ServiceVersion {
    V7_0("7.0");

    ServiceVersion(String version) { }

    public static ServiceVersion getLatest() {
        return V7_0;
    }
}
