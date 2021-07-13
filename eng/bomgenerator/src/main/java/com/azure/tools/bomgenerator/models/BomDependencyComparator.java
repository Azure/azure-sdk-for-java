package com.azure.tools.bomgenerator.models;

import org.apache.maven.model.Dependency;

public class BomDependencyComparator extends DependencyComparator {

    private String getDependencyName(String groupId, String artifactId, String version) {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    String getDependencyName(BomDependency dependency) {
        return getDependencyName(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    @Override
    String getDependencyName(BomDependencyNoVersion dependencyNoVersion) {
        return null;
    }

    @Override
    String getDependencyName(Dependency dependency) {
        return getDependencyName(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }
}
