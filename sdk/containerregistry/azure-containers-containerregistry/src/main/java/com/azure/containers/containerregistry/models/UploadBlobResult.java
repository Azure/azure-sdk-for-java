// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.containers.containerregistry.implementation.ConstructorAccessors;

/**
* The result from uploading the blob.
*/
public final class UploadBlobResult {
    static {
        ConstructorAccessors.setUploadBlobResultAccessor(UploadBlobResult::new);
    }

    private final String digest;

    /**
     * Instantiate the upload blob result.
     * @param digest The digest of the blob that was uploaded.
     */
    private UploadBlobResult(String digest) {
        this.digest = digest;
    }

    /**
     * The digest of the uploaded blob, calculated by the registry.
     * @return The digest value returned by the upload operation.
     */
    public String getDigest() {
        return digest;
    }
}
