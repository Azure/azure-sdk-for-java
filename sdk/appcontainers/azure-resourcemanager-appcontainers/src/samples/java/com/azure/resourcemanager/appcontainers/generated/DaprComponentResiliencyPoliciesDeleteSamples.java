// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appcontainers.generated;

/**
 * Samples for DaprComponentResiliencyPolicies Delete.
 */
public final class DaprComponentResiliencyPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/preview/2024-08-02-preview/examples/
     * DaprComponentResiliencyPolicies_Delete.json
     */
    /**
     * Sample code: Delete dapr component resiliency policy.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        deleteDaprComponentResiliencyPolicy(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponentResiliencyPolicies()
            .deleteWithResponse("examplerg", "myenvironment", "mydaprcomponent", "myresiliencypolicy",
                com.azure.core.util.Context.NONE);
    }
}
