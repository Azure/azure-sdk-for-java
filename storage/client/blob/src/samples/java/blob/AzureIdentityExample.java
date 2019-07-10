// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package blob;

import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.storage.blob.StorageClient;
import com.azure.storage.blob.StorageClientBuilder;

import java.util.Locale;

import static blob.SampleHelper.getAccountName;

/**
 * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
 * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
 * ClientSecretCredential.
 */
public class AzureIdentityExample {
    public static void main(String[] args) {
        String accountName = getAccountName();

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a storage client using the Azure Identity credentials.
         */
        StorageClient storageClient = new StorageClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredential())
                .buildClient();
        System.out.println("Successfully setup client using the Azure Identity, please check the service version: "
            + storageClient.getProperties().value().defaultServiceVersion());

    }
}
