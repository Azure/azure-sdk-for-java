// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    @FunctionName("MITest")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");


        String resourceId = System.getenv().get("IDENTITY_USER_DEFINED_IDENTITY");
        String account1 = System.getenv().get("IDENTITY_STORAGE_NAME_1");
        String account2 = System.getenv().get("IDENTITY_STORAGE_NAME_2");
        ManagedIdentityCredential systemAssigned = new ManagedIdentityCredentialBuilder().build();
        ManagedIdentityCredential userAssigned = new ManagedIdentityCredentialBuilder()
                .resourceId(resourceId)
                .build();

        BlobServiceClient systemAssignedBlobClient = new BlobServiceClientBuilder()
                .endpoint("https://" + account1 + ".blob.core.windows.net")
                .credential(systemAssigned)
                .buildClient();
        BlobServiceClient userAssignedBlobClient = new BlobServiceClientBuilder()
                .endpoint("https://" + account2 + ".blob.core.windows.net")
                .credential(userAssigned)
                .buildClient();

        try {
            systemAssignedBlobClient.listBlobContainers().forEach(container -> container.getName());
            userAssignedBlobClient.listBlobContainers().forEach(container -> container.getName());
            return request.createResponseBuilder(HttpStatus.OK).body("Successfully retrieved managed identity tokens").build();

        } catch (Exception e) {
        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage() + "\r\n" + e.getStackTrace().toString()).build();
        }
    }
}
