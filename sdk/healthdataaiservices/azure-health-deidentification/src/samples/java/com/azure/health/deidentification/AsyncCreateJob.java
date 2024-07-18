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

public class AsyncCreateJob {
    public static void main(String[] args) {
        // BEGIN: com.azure.health.deidentification.async.createjob
        String jobName = "MyJob-" + Instant.now().toEpochMilli();
        String outputFolder = "_output";
        String inputPrefix = "example_patient_1";

        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        DeidentificationAsyncClient deidentificationClient = deidentificationClientbuilder.buildAsyncClient();

        String storageAccountSASUri = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_SAS_URI");
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageAccountSASUri, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageAccountSASUri, outputFolder));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
            .getSyncPoller()
            .waitForCompletion()
            .getValue();
        System.out.println(jobName + " - " + result.getStatus());
        // MyJob-1719954700191 - Succeeded

        // END: com.azure.health.deidentification.async.createjob
    }
}
