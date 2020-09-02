// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
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
    private static final String tenantId = System.getenv("TENANT_ID");
    private static final String clientId = System.getenv("CLIENT_ID");
    private static final String clientSecret = System.getenv("CLIENT_SECRET");
    private static final String endpoint = System.getenv("DIGITAL_TWINS_ENDPOINT");

    private static final int MaxWaitTimeAsyncOperationsInSeconds = 10;

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
            System.err.println("Unable to convert the DTDL directory URL to URI: " + e);
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
                    .setLogLevel(HttpLogDetailLevel.NONE))
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

        // Call APIs to delete the twins. For each async operation, once the operation is completed successfully, a semaphore is released.
        twins
            .forEach((twinId, twinContent) -> client.deleteDigitalTwin(twinId)
                .doOnSuccess(aVoid -> System.out.println("Deleted digital twin: " + twinId))
                .doOnError(throwable -> {
                    // If digital twin does not exist, ignore.
                    if (!(throwable instanceof ErrorResponseException) || !((ErrorResponseException) throwable).getValue().getError().getCode().equals("DigitalTwinNotFound")) {
                        System.err.println("Could not delete digital twin " + twinId + " due to " + throwable);
                    }
                })
                .doOnTerminate(deleteTwinsSemaphore::release)
                .subscribe());

        // Verify that a semaphore has been released for each async operation, signifying that the async call has completed.
        boolean created = deleteTwinsSemaphore.tryAcquire(twins.size(), MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
        System.out.println("Twins deleted: " + created);
    }

    public static void createTwins() throws IOException, InterruptedException {
        System.out.println("CREATE DIGITAL TWINS");
        Map<String, String> twins = FileHelper.loadAllFilesInPath(TwinsPath);
        final Semaphore createTwinsSemaphore = new Semaphore(0);

        // Call APIs to create the twins. For each async operation, once the operation is completed successfully, a semaphore is released.
        twins
            .forEach((twinId, twinContent) -> client.createDigitalTwinWithResponse(twinId, twinContent)
                .doOnSuccess(response -> System.out.println("Created digital twin: " + twinId + "\n\t Body: " + response.getValue()))
                .doOnError(throwable -> System.err.println("Could not create digital twin " + twinId + " due to " + throwable))
                .doOnTerminate(createTwinsSemaphore::release)
                .subscribe());

        // Verify that a semaphore has been released for each async operation, signifying that the async call has completed.
        boolean created = createTwinsSemaphore.tryAcquire(twins.size(), MaxWaitTimeAsyncOperationsInSeconds, TimeUnit.SECONDS);
        System.out.println("Twins created: " + created);
    }
}
