package com.azure.storage.file.datalake;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.file.datalake.models.DataLakeAudience;

import java.util.Locale;

/**
 * This example shows how to use audience-based authentication with Azure Storage fpr DataLake. Audience-based
 * authentication requires AAD authentication. The audience is to be used when requesting a token from
 * Azure Active Directory (AAD). Note: This audience only has an effect when authenticating a TokenCredential.
 */
public class DataLakeAudienceExample {
    public static void main(String[] args) {
        /*
         * From the Azure portal, get your Storage account's name.
         */
        String accountName = SampleHelper.getAccountName();

        /*
         * audience will look like: "https://<your storage account>.blob.core.windows.net"
         */
        DataLakeAudience audience = DataLakeAudience.createDataLakeServiceAccountAudience(accountName);

        /* The credential used is DefaultAzureCredential because it combines commonly used credentials
         * in deployment and development and chooses the credential to used based on its running environment.
         * More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
         * AAD authentication is required for audience-based authentication.
         */
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        /*
         * From the Azure portal, get your Storage account datalake service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.dfs.core.windows.net", accountName);

        /*
         * Create a DataLakeServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(tokenCredential)
            .audience(null) // The default audience is "https://storage.azure.com"
            .buildClient();

        // This call will succeed because the default audience is "https://storage.azure.com"
        serviceClient.getProperties();

        /*
        Now create a DataLakeFileSystemClient that takes a specific audience. When storage account is specified, the
        appropriate service endpoint will be appended to the audience. For example, if storage account name is
        "myAccount", the audience will look like: "https://myaccount.blob.core.windows.net"
         */
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .credential(tokenCredential)
            .audience(audience)
            .fileSystemName("myFileSystem")
            .buildClient();

        /*
        Any calls to the service should successfully work with the specified audience.
         */
        fileSystemClient.create();
        fileSystemClient.createDirectory("myDirectory");


        /*
        The storage account name must be a valid name. If an incorrect storage account name is specified, authentication
        will fail.
         */
        DataLakeAudience badAudience = DataLakeAudience.createDataLakeServiceAccountAudience("invalidAccount");
        DataLakeFileSystemClient badFileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .credential(tokenCredential)
            .audience(badAudience) // audience will look like: "https://invalidaccount.blob.core.windows.net"
            .fileSystemName("myBadFileSystem")
            .buildClient();

        try {
            badFileSystemClient.create();
        } catch (Exception e) {
            System.out.println("Authentication failed with invalid storage account name.");
        }
    }
}
