// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model.maven;

/**
 * Maven group, artifact, and version coordinates.
 */
public class Gav {
    private final String groupId;
    private final String artifactId;
    private final String version;

    /**
     * Creates Maven coordinates.
     *
     * @param groupId the group id
     * @param artifactId the artifact id
     * @param version the version
     */
    public Gav(final String groupId, final String artifactId, final String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * Returns the group id.
     *
     * @return the group id
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Returns the artifact id.
     *
     * @return the artifact id
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Checks whether all Maven coordinates are nonempty.
     *
     * @return true if the group ID, artifact ID, and version are all nonempty
     */
    public boolean isValid() {
        return groupId != null
            && artifactId != null
            && version != null
            && !groupId.isEmpty()
            && !artifactId.isEmpty()
            && !version.isEmpty();
    }
}
