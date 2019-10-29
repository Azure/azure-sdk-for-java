// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasQueryParameters;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.PathSasPermission;

import java.time.OffsetDateTime;

/**
 * Code snippets for {@link DataLakeServiceSasSignatureValues}.
 */
public class DataLakeServiceSasSignatureValuesJavaDocCodeSnippets {
    /**
     * Generates a file or directory SAS with {@link StorageSharedKeyCredential}
     */
    public void fileSas() {
        // BEGIN: com.azure.storage.file.datalake.sas.DataLakeServiceSasQueryParameters.generateSasQueryParameters#StorageSharedKeyCredential
        PathSasPermission pathPermission = new PathSasPermission().setReadPermission(true);

        // We are creating a SAS to a path because we set both the file system name and path name.
        DataLakeServiceSasSignatureValues builder = new DataLakeServiceSasSignatureValues()
            .setProtocol(SasProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
            .setExpiryTime(OffsetDateTime.now().plusDays(2))
            .setFileSystemName("my-file-system")
            .setPathName("HelloWorld.txt")
            .setPermissions(pathPermission);

        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("account-name", "key");
        DataLakeServiceSasQueryParameters sasQueryParameters = builder.generateSasQueryParameters(credential);
        // END: com.azure.storage.file.datalake.sas.DataLakeServiceSasQueryParameters.generateSasQueryParameters#StorageSharedKeyCredential
    }

    /**
     * Generates a file system SAS using {@link UserDelegationKey}.
     */
    public void userDelegationKey() {
        // BEGIN: com.azure.storage.file.datalake.sas.DataLakeServiceSasQueryParameters.generateSasQueryParameters#UserDelegationKey-String
        PathSasPermission pathPermission = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true);

        // We are creating a SAS to a file system because only file system name is set.
        DataLakeServiceSasSignatureValues builder = new DataLakeServiceSasSignatureValues()
            .setProtocol(SasProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
            .setExpiryTime(OffsetDateTime.now().plusDays(2))
            .setFileSystemName("my-file-system")
            .setPermissions(pathPermission);

        // Get a user delegation key after signing in with Azure AD
        UserDelegationKey credential = new UserDelegationKey();
        String account = "my-path-storage-account";
        DataLakeServiceSasQueryParameters sasQueryParameters = builder.generateSasQueryParameters(credential, account);
        // END: com.azure.storage.file.datalake.sas.DataLakeServiceSasQueryParameters.generateSasQueryParameters#UserDelegationKey-String
    }
}
