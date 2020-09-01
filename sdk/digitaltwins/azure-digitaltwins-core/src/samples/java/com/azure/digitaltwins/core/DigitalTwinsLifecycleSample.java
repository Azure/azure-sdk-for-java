// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DigitalTwinsLifecycleSample {
    private static final ClientLogger logger = new ClientLogger(DigitalTwinsLifecycleSample.class);

    private static final String tenantId = System.getenv("TENANT_ID");
    private static final String clientId = System.getenv("CLIENT_ID");
    private static final String clientSecret = System.getenv("CLIENT_SECRET");
    private static final String endpoint = System.getenv("DIGITAL_TWINS_ENDPOINT");

    private static final int MaxTimeForTwinOperationsInSeconds = 10;

    private static final URL DtdlDirectoryUrl = DigitalTwinsLifecycleSample.class.getClassLoader().getResource("DTDL");
    private static final Path DtDlDirectoryPath;
    private static final Path TwinsPath;
    private static final Path ModelsPath;
    private static final Path RelationshipsPath;

    private static final DigitalTwinsAsyncClient client;

    static {
        try {
            assert DtdlDirectoryUrl != null;
            DtDlDirectoryPath = Paths.get(DtdlDirectoryUrl.toURI());
        } catch (URISyntaxException e) {
            logger.error("Unable to convert the DTDL directory URL to URI: ", e);
            throw new RuntimeException(e);
        }
        TwinsPath = Paths.get(DtDlDirectoryPath.toString(), "DigitalTwins");
        ModelsPath = Paths.get(DtDlDirectoryPath.toString(), "Models");
        RelationshipsPath = Paths.get(DtDlDirectoryPath.toString(), "Relationships");

        client = new DigitalTwinsClientBuilder()
            .tokenCredential(
                new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build()
            )
            .endpoint(endpoint)
            .httpLogOptions(
                new HttpLogOptions()
                    .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Ensure existing twins with the same name are deleted first
        deleteTwins();

        // Create twin counterparts for all the models
        createTwins();
    }

    public static void deleteTwins() throws IOException, InterruptedException {
        System.out.println("DELETE DIGITAL TWINS");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TwinsPath);
        final Semaphore deleteTwinsSemaphore = new Semaphore(0);

        twins
            .forEach((twinId, twinContent) -> client.deleteDigitalTwin(twinId)
                .subscribe(
                    aVoid -> { },
                    throwable -> System.err.println("Could not create digital twin " + twinId + " due to " + throwable),
                    () -> {
                        System.out.println("Deleted digital twin: " + twinId);
                        deleteTwinsSemaphore.release();
                    }));

        boolean created = deleteTwinsSemaphore.tryAcquire(twins.size(), MaxTimeForTwinOperationsInSeconds, TimeUnit.SECONDS);
        System.out.println("Twins deleted: " + created);
    }

    public static void createTwins() throws IOException, InterruptedException {
        System.out.println("CREATE DIGITAL TWINS");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TwinsPath);
        final Semaphore createTwinsSemaphore = new Semaphore(0);

        // Call APIs to create the twins.
        twins
            .forEach(
                (twinId, twinContent) -> client.createDigitalTwinWithResponse(twinId, twinContent)
                    .subscribe(
                        stringDigitalTwinsResponse -> {
                            System.out.println("Created digital twin: " + twinId);
                            System.out.println("\t Body: " + stringDigitalTwinsResponse.getValue());
                        },
                        throwable -> System.err.println("Could not create digital twin " + twinId + " due to " + throwable),
                        createTwinsSemaphore::release));

        boolean created = createTwinsSemaphore.tryAcquire(twins.size(), MaxTimeForTwinOperationsInSeconds, TimeUnit.SECONDS);
        System.out.println("Twins created: " + created);
    }
}
