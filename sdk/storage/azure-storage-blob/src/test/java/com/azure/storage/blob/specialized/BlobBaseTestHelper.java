// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

public class BlobBaseTestHelper {
    public static String getEncryptionScope(BlobAsyncClientBase baseClient) {
        return baseClient.getEncryptionScope();
    }
}
