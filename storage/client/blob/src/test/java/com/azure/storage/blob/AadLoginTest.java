// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.identity.credential.EnvironmentCredential;
import com.azure.storage.blob.models.ContainerItem;
import org.junit.BeforeClass;

import java.util.Random;

public class AadLoginTest {
    private static final Random RANDOM = new Random();
    private static BlobServiceClient storageClient;

    @BeforeClass
    public static void setup() {
        storageClient = new BlobServiceClientBuilder()
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
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
