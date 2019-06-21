package com.azure.storage.blob;

import com.azure.identity.credential.EnvironmentCredential;
import com.azure.storage.blob.models.ContainerItem;
import org.junit.BeforeClass;

import java.util.Random;

public class AadLoginTest {
    private static final Random RANDOM = new Random();
    private static StorageClient storageClient;

    @BeforeClass
    public static void setup() {
        storageClient = StorageClient.storageClientBuilder()
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
            .credentials(new EnvironmentCredential())
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
