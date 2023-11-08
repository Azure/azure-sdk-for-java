// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;
import com.azure.core.annotation.Immutable;

/**
* The result from uploading the blob.
*/
@Immutable
public final class UploadRegistryBlobResult {
    static {
        ConstructorAccessors.setUploadRegistryBlobResultAccessor(UploadRegistryBlobResult::new);
    }

    private final String digest;
    private final long length;

    /**
     * Instantiate the upload blob result.
     * @param digest The digest of the blob that was uploaded.
     */
    private UploadRegistryBlobResult(String digest, long length) {
        this.digest = digest;
        this.length = length;
    }

    /**
     * The digest of the uploaded blob, calculated by the registry.
     * @return The digest value returned by the upload operation.
     */
    public String getDigest() {
        return digest;
    }

    /**
     * The size of uploaded blob.
     * @return Size of the uploaded blob in bytes.
     */
    public long getSizeInBytes() {
        return length;
    }
}
