// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.storage.blob.BlobClient

class NioBlobInputStreamTest extends APISpec {

    File sourceFile
    BlobClient bc

    def setup() {
        sourceFile = getRandomFile(8 * 1024 * 1024)
        bc = cc.getBlobClient(generateBlobName())
        bc.uploadFromFile(sourceFile.getPath())
    }

    def cleanup() {
        sourceFile.delete()
    }

    // Upload a file that's 8mb
    // Test marking and resetting
    // Test skipping
    // Test thread safety?
    // Mark supported
    // Available
    // Close
    // Reading
    // Test option validation on provider
    // Test path validation on provider
}
