package com.azure.tools.apiview.processor.model.maven;

public class Dependency extends Gav {
    private final String scope; // default scope is compile-time scope

    Dependency(String groupId, String artifactId, String version, String scope) {
        super(groupId, artifactId, version);
        this.scope = scope == null || scope.isEmpty() ? "compile" : scope;
    }

    public String getScope() {
        return scope;
    }
}
