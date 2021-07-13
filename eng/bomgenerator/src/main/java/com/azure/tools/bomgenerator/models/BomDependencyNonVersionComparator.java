package com.azure.tools.bomgenerator.models;

import org.apache.maven.model.Dependency;

public class BomDependencyNonVersionComparator extends DependencyComparator {

    private String getDependencyName(String groupId, String artifactId) {
        return groupId + ":" + artifactId;
    }

    @Override
    String getDependencyName(BomDependency dependency) {
        return getDependencyName(dependency.getGroupId(), dependency.getArtifactId());
    }

    @Override
    String getDependencyName(BomDependencyNoVersion dependencyNoVersion) {
        return getDependencyName(dependencyNoVersion.getGroupId(), dependencyNoVersion.getArtifactId());
    }

    @Override
    String getDependencyName(Dependency dependency) {
        return getDependencyName(dependency.getGroupId(), dependency.getArtifactId());
    }
}
