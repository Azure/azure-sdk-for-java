// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.sas.FileServiceSasQueryParameters;
import com.azure.storage.file.sas.FileServiceSasSignatureValues;
import com.azure.storage.file.sas.ShareSasPermission;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Code snippets for {@link FileServiceSasSignatureValues}.
 */
public class FileServiceSasSignatureValuesJavaDocCodeSnippets {
    /**
     * Creates a file share SAS.
     */
    public void shareSas() {
        // BEGIN: com.azure.storage.file.fileServiceSasQueryParameters.generateSasQueryParameters.shareSas#StorageSharedKeyCredential
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("my-account", "key");
        ShareSasPermission permission = new ShareSasPermission()
            .setCreatePermission(true)
            .setReadPermission(true);

        // The expiry time and permissions are required to create a valid SAS
        // if a stored access policy identifier is not set.
        FileServiceSasQueryParameters sasQueryParameters = new FileServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plus(Duration.ofDays(3)))
            .setPermissions(permission)
            .setShareName("file-share-name")
            .generateSasQueryParameters(credential);
        // END: com.azure.storage.file.fileServiceSasQueryParameters.generateSasQueryParameters.shareSas#StorageSharedKeyCredential
    }

    /**
     * Creates a file share SAS.
     */
    public void fileSas() {
        // BEGIN: com.azure.storage.file.fileServiceSasQueryParameters.generateSasQueryParameters#StorageSharedKeyCredential
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("my-account", "key");

        // The shared access policy, "read-write-user" exists in the storage account. The file SAS generated from this
        // has the same duration and permissions as the policy.
        // The expiry and permissions should not be set explicitly.
        FileServiceSasQueryParameters sasQueryParameters = new FileServiceSasSignatureValues()
            .setIdentifier("read-write-user")
            .setShareName("file-share-name")
            .generateSasQueryParameters(credential);
        // END: com.azure.storage.file.fileServiceSasQueryParameters.generateSasQueryParameters#StorageSharedKeyCredential
    }
}
