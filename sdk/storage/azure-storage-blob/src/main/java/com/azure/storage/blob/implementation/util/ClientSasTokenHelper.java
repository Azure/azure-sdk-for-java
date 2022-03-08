// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.credential.AzureSasCredential;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;

/**
 * The helper class to get the non-public sas token of a {@link com.azure.storage.blob.specialized.BlobAsyncClientBase}
 * instance.
 */
public class ClientSasTokenHelper {

    private static ClientSasTokenAccessor accessor;

    private ClientSasTokenHelper () {
    }

    interface ClientSasTokenAccessor {
        AzureSasCredential getSasCredential(BlobAsyncClientBase blobClient);
    }

    static void setAccessor(final ClientSasTokenHelper.ClientSasTokenAccessor clientSasTokenAccessor) {
        accessor = clientSasTokenAccessor;
    }

    static void get

}
