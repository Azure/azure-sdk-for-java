package com.azure;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IllegalArgumentException, IOException {
        System.out.println("================================");
        System.out.println("     AZURE SDK SMOKE TEST");
        System.out.println("================================");

        StorageBlob.main(null);
        KeyVaultSecrets.main(null);
        EventHubs.main(null);
        CosmosDB.main(null);

    }
}
