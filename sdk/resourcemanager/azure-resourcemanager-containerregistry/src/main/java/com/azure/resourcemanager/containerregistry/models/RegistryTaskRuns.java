// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of collection of Azure registry task runs. */
@Fluent()
public interface RegistryTaskRuns {
    /**
     * The function that begins the steps to schedule a run.
     *
     * @return the next step in the execution of a run.
     */
    RegistryTaskRun.DefinitionStages.BlankFromRuns scheduleRun();

    /**
     * The function that lists the RegistryTaskRun instances in a registry asynchronously.
     *
     * @param rgName the resource group of the parent registry.
     * @param acrName the name of the parent registry.
     * @return the list of RegistryTaskRun instances.
     */
    PagedFlux<RegistryTaskRun> listByRegistryAsync(String rgName, String acrName);

    /**
     * The function that lists the RegistryTaskRun instances in a registry asynch.
     *
     * @param rgName the resource group of the parent registry.
     * @param acrName the name of the parent registry.
     * @return the list of RegistryTaskRun instances.
     */
    PagedIterable<RegistryTaskRun> listByRegistry(String rgName, String acrName);

    /**
     * The function that returns the URI to the task run logs asynchronously.
     *
     * @param rgName the resource group of the parent registry.
     * @param acrName the name of the parent registry.
     * @param runId the id of the task run.
     * @return the URI to the task run logs.
     */
    Mono<String> getLogSasUrlAsync(String rgName, String acrName, String runId);

    /**
     * The function that returns the URI to the task run logs.
     *
     * @param rgName the resource group of the parent registry.
     * @param acrName the name of the parent registry.
     * @param runId the id of the task run.
     * @return the URI to the task run logs.
     */
    String getLogSasUrl(String rgName, String acrName, String runId);

    /**
     * The function that cancels a task run asynchronously.
     *
     * @param rgName the resource group of the parent registry.
     * @param acrName the name of the parent registry.
     * @param runId the id of the task run.
     * @return handle to the request.
     */
    Mono<Void> cancelAsync(String rgName, String acrName, String runId);

    /**
     * The function that cancels a task run.
     *
     * @param rgName the resource group of the parent registry.
     * @param acrName the name of the parent registry.
     * @param runId the id of the task run.
     */
    void cancel(String rgName, String acrName, String runId);
}
