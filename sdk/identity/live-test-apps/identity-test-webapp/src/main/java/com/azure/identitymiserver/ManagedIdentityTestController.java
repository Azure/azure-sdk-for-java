// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identitymiserver;

import com.azure.core.util.Configuration;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ManagedIdentityTestController {
    @GetMapping("/mitest")
    public String webapp() {

        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        String resourceId = configuration.get("IDENTITY_USER_DEFINED_IDENTITY");
        String account1 = configuration.get("IDENTITY_STORAGE_NAME_1");
        String account2 = configuration.get("IDENTITY_STORAGE_NAME_2");

        ManagedIdentityCredential credential1 = new ManagedIdentityCredentialBuilder().build();
        ManagedIdentityCredential credential2 = new ManagedIdentityCredentialBuilder().resourceId(resourceId).build();

        BlobServiceClient systemAssignedBlobClient = new BlobServiceClientBuilder()
            .endpoint("https://" + account1 + ".blob.core.windows.net")
            .credential(credential1)
            .buildClient();
        BlobServiceClient userAssignedBlobClient = new BlobServiceClientBuilder()
            .endpoint("https://" + account2 + ".blob.core.windows.net")
            .credential(credential2)
            .buildClient();

        try {
            systemAssignedBlobClient.listBlobContainers().forEach(container -> container.getName());
            userAssignedBlobClient.listBlobContainers().forEach(container -> container.getName());
            return "Successfully acquired a token from ManagedIdentityCredential";
        } catch (Exception ex) {
            return "Failed to acquire a token from ManagedIdentityCredential";
        }
    }
}
