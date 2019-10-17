// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.BlobSasPermission;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.SasProtocol;
import com.azure.storage.common.credentials.SharedKeyCredential;

import java.time.OffsetDateTime;

public class BlobServiceSasSignatureValuesJavaDocCodeSnippets {
    public void blobSas() {
        // BEGIN: com.azure.storage.blob.specialized.BlobServiceSasSignatureValues.generateSasQueryParameters#SharedKeyCredential
        BlobSasPermission blobPermission = new BlobSasPermission().setReadPermission(true);

        // We are creating a SAS to a blob because we set both the container name and blob name.
        BlobServiceSasSignatureValues builder = new BlobServiceSasSignatureValues()
            .setProtocol(SasProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
            .setExpiryTime(OffsetDateTime.now().plusDays(2))
            .setContainerName("my-container")
            .setBlobName("HelloWorld.txt")
            .setPermissions(blobPermission);

        SharedKeyCredential credential = new SharedKeyCredential("account-name", "key");
        BlobServiceSasQueryParameters sasQueryParameters = builder.generateSasQueryParameters(credential);
        // END: com.azure.storage.blob.specialized.BlobServiceSasSignatureValues.generateSasQueryParameters#SharedKeyCredential
    }

    public void userDelegationKey() {
        // BEGIN: com.azure.storage.blob.specialized.BlobServiceSasSignatureValues.generateSasQueryParameters#UserDelegationKey-String
        BlobSasPermission blobPermission = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true);

        // We are creating a SAS to a container because only container name is set.
        BlobServiceSasSignatureValues builder = new BlobServiceSasSignatureValues()
            .setProtocol(SasProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
            .setExpiryTime(OffsetDateTime.now().plusDays(2))
            .setContainerName("my-container")
            .setPermissions(blobPermission);

        // Get a user delegation key after signing in with Azure AD
        UserDelegationKey credential = new UserDelegationKey();
        String account = "my-blob-storage-account";
        BlobServiceSasQueryParameters sasQueryParameters = builder.generateSasQueryParameters(credential, account);
        // END: com.azure.storage.blob.specialized.BlobServiceSasSignatureValues.generateSasQueryParameters#UserDelegationKey-String
    }
}
