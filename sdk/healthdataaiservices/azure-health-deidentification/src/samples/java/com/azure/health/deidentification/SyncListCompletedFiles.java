// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
        List<String> extensions = new ArrayList<>();
        extensions.add("*");

        SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);
        sourceStorageLocation.setExtensions(extensions);

        DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, outputFolder));
        job.setOperation(OperationType.SURROGATE);
        job.setDataType(DocumentDataType.PLAINTEXT);

        DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
            .waitForCompletion()
            .getValue();
        // BEGIN: com.azure.health.deidentification.sync.listcompletedfiles
        PagedIterable<DocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);

        for (DocumentDetails currentFile : reports) {
            System.out.println(currentFile.getId() + " - " + currentFile.getOutput().getPath());
            // c45dcd5e-e3ce-4ff2-80b6-a8bbeb47f878 - _output/MyJob-1719954393623/example_patient_1/visit_summary.txt
            // e55a1aa2-8eba-4515-b070-1fd3d005008b - _output/MyJob-1719954393623/example_patient_1/doctor_dictation.txt
        }
        // END: com.azure.health.deidentification.sync.listcompletedfiles
    }
}
