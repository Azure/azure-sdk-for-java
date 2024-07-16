// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SyncCreateJob {
    public static void main(String[] args) {
        DeidServicesClientBuilder deidentificationClientbuilder = new DeidServicesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        DeidServicesClient deidentificationClient = deidentificationClientbuilder.buildClient();

        String storageAccountSASUri = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_SAS_URI");
        List<String> extensions = new ArrayList<>();
        extensions.add("*");
        // BEGIN: com.azure.health.deidentification.sync.createjob.create
        String jobName = "MyJob-" + Instant.now().toEpochMilli();
        String outputFolder = "_output";
        String inputPrefix = "example_patient_1";
        DeidentificationJob job = new DeidentificationJob(
            new SourceStorageLocation(storageAccountSASUri, inputPrefix, extensions),
            new TargetStorageLocation(storageAccountSASUri, outputFolder),
            OperationType.SURROGATE,
            DocumentDataType.PLAINTEXT);
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
