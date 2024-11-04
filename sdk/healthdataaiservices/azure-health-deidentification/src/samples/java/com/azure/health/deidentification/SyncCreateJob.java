// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.DeidentificationJob;
import com.azure.health.deidentification.models.DocumentDataType;
import com.azure.health.deidentification.models.OperationType;
import com.azure.health.deidentification.models.SourceStorageLocation;
import com.azure.health.deidentification.models.TargetStorageLocation;

import java.time.Instant;

public class SyncCreateJob {
    public static void main(String[] args) {
        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        DeidentificationClient deidentificationClient = deidentificationClientbuilder.buildClient();

        // BEGIN: com.azure.health.deidentification.sync.createjob.create
        String storageLocation = "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
        String jobName = "MyJob-" + Instant.now().toEpochMilli();
        String outputFolder = "_output";
        String inputPrefix = "example_patient_1";
        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, outputFolder));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        // END: com.azure.health.deidentification.sync.createjob.create
        // BEGIN: com.azure.health.deidentification.sync.createjob.process
        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
            .waitForCompletion()
            .getValue();
        System.out.println(jobName + " - " + result.getStatus());
        // MyJob-1719953889301 - Succeeded
        // END: com.azure.health.deidentification.sync.createjob.process

    }
}
