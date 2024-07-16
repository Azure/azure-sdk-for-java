// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.*;

import java.time.Instant;

public class AsyncListJobs {
    public static void main(String[] args) {
        // BEGIN: com.azure.health.deidentification.async.listjobs
        DeidServicesClientBuilder deidentificationClientbuilder = new DeidServicesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        DeidServicesAsyncClient deidentificationClient = deidentificationClientbuilder.buildAsyncClient();

        PagedFlux<DeidentificationJob> jobs = deidentificationClient.listJobs();

        jobs.byPage() // Retrieves Flux<PagedResponse<T>>, where each PagedResponse<T> represents a page
            .subscribe(page -> {
                page.getElements().forEach(item -> System.out.println(item.getName() + " - " + item.getStatus()));
            }, ex -> System.out.println("Error listing pages" + ex.getMessage()));

        // END: com.azure.health.deidentification.async.listjobs
    }
}
