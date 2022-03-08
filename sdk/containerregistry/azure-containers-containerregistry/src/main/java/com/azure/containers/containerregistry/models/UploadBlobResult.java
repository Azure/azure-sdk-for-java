// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

/**
* The result from uploading the blob.
*/
public final class UploadBlobResult {

    private final String digest;

    /**
     * Instantiate the upload blob result.
     * @param digest The digest of the blob that was uploaded.
     */
    public UploadBlobResult(String digest) {
        this.digest = digest;
    }

    /**
     * The digest of the uploaded blob, calculated by the registry.
     * @return The digest value returned by the upload operation.
     */
    public String getDigest() {
        return this.digest;
    }
}
