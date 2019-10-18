// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

import com.azure.storage.common.StorageSharedKeyCredential;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Code snippets for {@link AccountSasSignatureValues}.
 */
public class AccountSasSignatureValuesJavaDocCodeSnippets {
    public void generateSas() {
        // BEGIN: com.azure.storage.common.sas.accountSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("my-account", "my-key");

        AccountSasPermission permissions = new AccountSasPermission()
            .setListPermission(true)
            .setReadPermission(true);
        String resourceTypes = new AccountSasResourceType().setContainer(true).toString();
        String services = new AccountSasService().setBlob(true).setFile(true).toString();

        // Creates an account SAS that can read and list from blob containers and file shares.
        // The following are required: permissions, resourceTypes, services, and expiry date.
        AccountSasQueryParameters sasQueryParameters = new AccountSasSignatureValues()
            .setPermissions(permissions)
            .setResourceTypes(resourceTypes)
            .setServices(services)
            .setExpiryTime(OffsetDateTime.now().plus(Duration.ofDays(2)))
            .generateSasQueryParameters(credential);
        // END: com.azure.storage.common.sas.accountSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential
    }
}
