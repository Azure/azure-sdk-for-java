// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.identity.credential.EnvironmentCredential;
import com.azure.storage.blob.models.ContainerItem;
import org.junit.BeforeClass;

import java.util.Random;

public class AadLoginTest {
    private static final Random RANDOM = new Random();
    private static BlobServiceClient storageClient;

    @BeforeClass
    public static void setup() {
        String endpoint = String.format("https://%s.blob.core.windows.net", ConfigurationManager.getConfiguration().get("PRIMARY_STORAGE_ACCOUNT_KEY"));
        storageClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new EnvironmentCredential())
//            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildClient();
    }

    //@Test
    public void listContainers() {
        for (ContainerItem item : storageClient.listContainers()) {
            System.out.println(item.name());
        }
    }
}
