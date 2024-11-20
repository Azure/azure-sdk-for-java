// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.DeidentificationJob;
import com.azure.health.deidentification.models.DeidentificationDocumentDetails;
import com.azure.health.deidentification.models.DeidentificationOperationType;
import com.azure.health.deidentification.models.SourceStorageLocation;
import com.azure.health.deidentification.models.TargetStorageLocation;

import java.time.Instant;

public class SyncListCompletedFiles {
    public static void main(String[] args) {
        String jobName = "MyJob-" + Instant.now().toEpochMilli();
        String outputFolder = "_output";
        String inputPrefix = "example_patient_1";

        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        DeidentificationClient deidentificationClient = deidentificationClientbuilder.buildClient();

        String storageLocation = "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, outputFolder));
        job.setOperation(DeidentificationOperationType.SURROGATE);

        DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
            .waitForCompletion()
            .getValue();
        // BEGIN: com.azure.health.deidentification.sync.listcompletedfiles
        PagedIterable<DeidentificationDocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);

        for (DeidentificationDocumentDetails currentFile : reports) {
            System.out.println(currentFile.getId() + " - " + currentFile.getOutput().getLocation());
        }
        // END: com.azure.health.deidentification.sync.listcompletedfiles
    }
}
