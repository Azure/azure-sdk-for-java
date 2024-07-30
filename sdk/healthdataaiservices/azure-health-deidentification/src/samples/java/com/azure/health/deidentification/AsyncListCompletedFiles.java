// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.*;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class AsyncListCompletedFiles {
    public static void main(String[] args) {
        // BEGIN: com.azure.health.deidentification.async.listcompoletedfiles
        String jobName = "MyJob-" + Instant.now().toEpochMilli();
        String outputFolder = "_output";
        String inputPrefix = "example_patient_1";

        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        DeidentificationAsyncClient deidentificationClient = deidentificationClientbuilder.buildAsyncClient();

        String storageLocation = "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, outputFolder));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
            .getSyncPoller()
            .waitForCompletion()
            .getValue();
        PagedFlux<DocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);

        reports.byPage() // Retrieves Flux<PagedResponse<T>>, where each PagedResponse<T> represents a page
            .flatMap(page -> Flux.fromIterable(page.getElements())) // Converts each page into a Flux<T> of its items
            .subscribe(item -> {
                System.out.println(item.getId() + " - " + item.getOutput().getPath());
            });

        // END: com.azure.health.deidentification.async.listcompoletedfiles
    }
}
