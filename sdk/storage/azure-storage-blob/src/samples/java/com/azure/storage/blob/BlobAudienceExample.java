package com.azure.storage.blob;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.BlobAudience;

import java.util.Locale;

/**
 * This example shows how to use audience-based authentication with Azure Storage fpr blobs. Audience-based
 * authentication requires AAD authentication. The audience is to be used when requesting a token from
 * Azure Active Directory (AAD). Note: This audience only has an effect when authenticating a TokenCredential.
 */
public class BlobAudienceExample {

    public static void main(String[] args) {
        /*
         * From the Azure portal, get your Storage account's name.
         */
        String accountName = SampleHelper.getAccountName();

        /*
         * audience will look like: "https://<your storage account>.blob.core.windows.net"
         */
        BlobAudience audience = BlobAudience.createBlobServiceAccountAudience(accountName);

        /* The credential used is DefaultAzureCredential because it combines commonly used credentials
         * in deployment and development and chooses the credential to used based on its running environment.
         * More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
         * AAD authentication is required for audience-based authentication.
         */
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(tokenCredential)
            .audience(null) // The default audience is "https://storage.azure.com"
            .buildClient();

        // This call will succeed because the default audience is "https://storage.azure.com"
        serviceClient.getProperties();


        /*
        Now create a BlobContainerClient that takes a specific audience.
         */
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .credential(tokenCredential)
            .audience(audience)
            .containerName("myContainer")
            .buildClient();

        /*
        Any calls to the service should successfully work with the specified audience.
         */
        containerClient.create();
        containerClient.getBlobClient("myBlob").uploadFromFile("path/to/file");

        /*
        The storage account name must be a valid name. If an incorrect storage account name is specified, authentication
        will fail.
         */
        BlobAudience badAudience = BlobAudience.createBlobServiceAccountAudience("invalidAccount");
        BlobContainerClient badContainerClient = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .credential(tokenCredential)
            .audience(badAudience) // audience will look like: "https://invalidaccount.blob.core.windows.net"
            .containerName("myBadContainer")
            .buildClient();

        try {
            badContainerClient.create();
        } catch (Exception e) {
            System.out.println("Authentication failed with invalid storage account name.");
        }
    }
}
