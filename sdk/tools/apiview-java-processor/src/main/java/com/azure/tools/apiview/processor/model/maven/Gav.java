package com.azure.tools.apiview.processor.model.maven;

public class Gav {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public Gav(final String groupId, final String artifactId, final String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public boolean isValid() {
        return groupId != null && artifactId != null && version != null
                   && !groupId.isEmpty() && !artifactId.isEmpty() && !version.isEmpty();
    }
}
