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

public class SyncListJobs {
    public static void main(String[] args) {
        String jobName = "MyJob-" + Instant.now().toEpochMilli();

        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        DeidentificationClient deidentificationClient = deidentificationClientbuilder.buildClient();

        // BEGIN: com.azure.health.deidentification.sync.listjobs
        PagedIterable<DeidentificationJob> jobs = deidentificationClient.listJobs();
        for (DeidentificationJob currentJob : jobs) {
            System.out.println(currentJob.getName() + " - " + currentJob.getStatus());
            // MyJob-1719953889301 - Succeeded
        }
        // END: com.azure.health.deidentification.sync.listjobs
    }
}
