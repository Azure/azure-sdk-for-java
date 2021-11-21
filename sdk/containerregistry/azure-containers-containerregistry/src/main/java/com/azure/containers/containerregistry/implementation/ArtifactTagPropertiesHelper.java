// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link ArtifactTagProperties} instance.
 */
public final class ArtifactTagPropertiesHelper {
    private static ArtifactTagPropertiesAccessor accessor;

    private ArtifactTagPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ArtifactTagProperties} instance.
     */
    public interface ArtifactTagPropertiesAccessor {
        void setRepositoryName(ArtifactTagProperties tagProperties, String repositoryName);
        void setName(ArtifactTagProperties tagProperties, String tagName);
        void setDigest(ArtifactTagProperties tagProperties, String digest);
        void setCreatedOn(ArtifactTagProperties tagProperties, OffsetDateTime  createdOn);
        void setlastUpdatedOn(ArtifactTagProperties tagProperties, OffsetDateTime lastUpdatedOn);
    }

    /**
     * The method called from {@link ArtifactTagProperties} to set it's accessor.
     *
     * @param tagPropertiesAccessor The accessor.
     */
    public static void setAccessor(final ArtifactTagPropertiesAccessor tagPropertiesAccessor) {
        accessor = tagPropertiesAccessor;
    }

    public static void setName(ArtifactTagProperties tagProperties, String name) {
        accessor.setName(tagProperties, name);
    }

    public static void setRepositoryName(ArtifactTagProperties tagProperties, String repositoryName) {
        accessor.setRepositoryName(tagProperties, repositoryName);
    }

    public static void setDigest(ArtifactTagProperties tagProperties, String digest) {
        accessor.setDigest(tagProperties, digest);
    }

    public static void setCreatedOn(ArtifactTagProperties tagProperties, OffsetDateTime createdOn) {
        accessor.setCreatedOn(tagProperties, createdOn);
    }

    public static void setlastUpdatedOn(ArtifactTagProperties tagProperties, OffsetDateTime lastUpdatedOn) {
        accessor.setlastUpdatedOn(tagProperties, lastUpdatedOn);
    }
}
