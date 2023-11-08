// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.containers.containerregistry.implementation.models.ArtifactManifestPropertiesInternal;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;

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
        ArtifactManifestProperties create(ArtifactManifestPropertiesInternal internal);
    }

    /**
     * The method called from {@link ArtifactManifestProperties} to set it's accessor.
     *
     * @param manifestPropertiesAccessor The accessor.
     */
    public static void setAccessor(final ArtifactManifestPropertiesAccessor manifestPropertiesAccessor) {
        accessor = manifestPropertiesAccessor;
    }

    public static ArtifactManifestProperties create(ArtifactManifestPropertiesInternal internal) {
        if (accessor == null) {
            new ArtifactManifestProperties();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
