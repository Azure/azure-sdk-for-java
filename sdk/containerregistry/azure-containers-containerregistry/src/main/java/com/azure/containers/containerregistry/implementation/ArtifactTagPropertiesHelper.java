// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.containers.containerregistry.implementation.models.ArtifactTagPropertiesInternal;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;

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
        ArtifactTagProperties create(ArtifactTagPropertiesInternal internal);
    }

    /**
     * The method called from {@link ArtifactTagProperties} to set it's accessor.
     *
     * @param tagPropertiesAccessor The accessor.
     */
    public static void setAccessor(final ArtifactTagPropertiesAccessor tagPropertiesAccessor) {
        accessor = tagPropertiesAccessor;
    }

    public static ArtifactTagProperties create(ArtifactTagPropertiesInternal internal) {
        if (accessor == null) {
            new ArtifactTagProperties();
        }

        assert accessor != null;
        return accessor.create(internal);
    }
}
