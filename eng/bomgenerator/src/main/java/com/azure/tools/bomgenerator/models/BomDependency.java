package com.azure.tools.bomgenerator.models;

import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

public class BomDependency extends BomDependencyNoVersion {
    private String version;
    private ScopeType scope;

    public BomDependency(String groupId, String artifactId, String version) {
        super(groupId, artifactId);
        this.version = version;
        this.scope = null;
    }

    public BomDependency(String groupId, String artifactId, String version, ScopeType scope) {
        this(groupId, artifactId, version);
        this.scope = scope;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    public ScopeType getScope() {
        return this.scope;
    }

    @Override
    public String toString() {
        return this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getVersion();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof BomDependency) {
            BomDependency bomDependency = (BomDependency) o;
            bomDependency.toString().equals(this.toString());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
