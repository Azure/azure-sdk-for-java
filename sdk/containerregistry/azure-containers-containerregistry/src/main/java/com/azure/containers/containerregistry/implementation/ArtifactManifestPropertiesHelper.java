// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.containers.containerregistry.models.ArtifactArchitecture;
import com.azure.containers.containerregistry.models.ArtifactManifestPlatform;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactOperatingSystem;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link ArtifactManifestProperties} instance.
 */
public final class ArtifactManifestPropertiesHelper {
    private static ArtifactManifestPropertiesAccessor accessor;

    private ArtifactManifestPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ArtifactManifestProperties} instance.
     */
    public interface ArtifactManifestPropertiesAccessor {
        void setRepositoryName(ArtifactManifestProperties manifestProperties, String repositoryName);
        void setRegistryLoginServer(ArtifactManifestProperties manifestProperties, String registryLoginServer);
        void setDigest(ArtifactManifestProperties manifestProperties, String digest);
        void setRelatedArtifacts(ArtifactManifestProperties manifestProperties, List<ArtifactManifestPlatform> relatedArtifacts);
        void setCpuArchitecture(ArtifactManifestProperties manifestProperties, ArtifactArchitecture architecture);
        void setOperatingSystem(ArtifactManifestProperties manifestProperties, ArtifactOperatingSystem operatingSystem);
        void setTags(ArtifactManifestProperties manifestProperties, List<String> tags);
        void setSizeInBytes(ArtifactManifestProperties manifestProperties, Long sizeInBytes);
        void setCreatedOn(ArtifactManifestProperties manifestProperties, OffsetDateTime  createdOn);
        void setlastUpdatedOn(ArtifactManifestProperties manifestProperties, OffsetDateTime lastUpdatedOn);
    }

    /**
     * The method called from {@link ArtifactManifestProperties} to set it's accessor.
     *
     * @param manifestPropertiesAccessor The accessor.
     */
    public static void setAccessor(final ArtifactManifestPropertiesAccessor manifestPropertiesAccessor) {
        accessor = manifestPropertiesAccessor;
    }

    public static void setRegistryLoginServer(ArtifactManifestProperties manifestProperties, String registryLoginServer) {
        accessor.setRegistryLoginServer(manifestProperties, registryLoginServer);
    }

    public static void setRepositoryName(ArtifactManifestProperties manifestProperties, String repositoryName) {
        accessor.setRepositoryName(manifestProperties, repositoryName);
    }

    public static void setDigest(ArtifactManifestProperties manifestProperties, String digest) {
        accessor.setDigest(manifestProperties, digest);
    }

    public static void setCreatedOn(ArtifactManifestProperties manifestProperties, OffsetDateTime createdOn) {
        accessor.setCreatedOn(manifestProperties, createdOn);
    }

    public static void setlastUpdatedOn(ArtifactManifestProperties manifestProperties, OffsetDateTime lastUpdatedOn) {
        accessor.setlastUpdatedOn(manifestProperties, lastUpdatedOn);
    }

    public static void setRelatedArtifacts(ArtifactManifestProperties manifestProperties, List<ArtifactManifestPlatform> relatedArtifacts) {
        accessor.setRelatedArtifacts(manifestProperties, relatedArtifacts);
    }

    public static void setCpuArchitecture(ArtifactManifestProperties manifestProperties, ArtifactArchitecture architecture) {
        accessor.setCpuArchitecture(manifestProperties, architecture);
    }

    public static void setOperatingSystem(ArtifactManifestProperties manifestProperties, ArtifactOperatingSystem operatingSystem) {
        accessor.setOperatingSystem(manifestProperties, operatingSystem);
    }

    public static void setTags(ArtifactManifestProperties manifestProperties, List<String> tags) {
        accessor.setTags(manifestProperties, tags);
    }

    public static void setSizeInBytes(ArtifactManifestProperties manifestProperties, Long sizeInBytes) {
        accessor.setSizeInBytes(manifestProperties, sizeInBytes);
    }
}
