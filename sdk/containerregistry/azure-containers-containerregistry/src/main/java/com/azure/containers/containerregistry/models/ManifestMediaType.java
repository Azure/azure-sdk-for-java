// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Manifest media type.
 */
public final class ManifestMediaType extends ExpandableStringEnum<ManifestMediaType> {

    /**
     * Docker v2 manifest content type.
     */
    public static final ManifestMediaType DOCKER_MANIFEST = fromString("application/vnd.docker.distribution.manifest.v2+json");

    /**
     * OCI manifest content type.
     */
    public static final ManifestMediaType OCI_MANIFEST = fromString("application/vnd.oci.image.manifest.v1+json");

    @Deprecated
    ManifestMediaType() {
    }

    /**
     * Creates an instance of {@link ManifestMediaType} from content type string.
     * @param mediaType the string with manifest content type(s).
     * @return instance of new {@link ManifestMediaType}.
     */
    public static ManifestMediaType fromString(String mediaType) {
        return fromString(mediaType, ManifestMediaType.class);
    }
}
