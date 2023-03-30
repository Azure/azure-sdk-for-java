// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.digitaltwins.core.helpers.ConsoleLogger;
import com.azure.digitaltwins.core.helpers.SamplesArguments;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;
import com.azure.digitaltwins.core.models.DigitalTwinsImportJob;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.util.UUID;

/**
 * This sample will list all the import jobs in your Azure Digital Twins instance,
 * then it will retrieve a particular import job. If the parameter for an existing import
 * job URL is provided then it will also create a new import job, and it will
 * delete that newly created import job.
 */
public class ImportJobsSyncSamples {
    private static DigitalTwinsClient client;

    public static void main(String[] args) {
        SamplesArguments parsedArguments = new SamplesArguments(args);

        client = new DigitalTwinsClientBuilder()
            .credential(
                new ClientSecretCredentialBuilder()
                    .tenantId(parsedArguments.getTenantId())
                    .clientId(parsedArguments.getClientId())
                    .clientSecret(parsedArguments.getClientSecret())
                    .build()
            )
            .endpoint(parsedArguments.getDigitalTwinEndpoint())
            .httpLogOptions(
                new HttpLogOptions()
                    .setLogLevel(parsedArguments.getHttpLogDetailLevel()))
            .buildClient();

        ConsoleLogger.printHeader("List Import Jobs");
        ConsoleLogger.print("Listing all import jobs in your Azure Digital Twins instance");
        PagedIterable<DigitalTwinsImportJob> importJobs = null;
        try {
            importJobs = client.listImportJobs();
        } catch (ErrorResponseException ex) {
            ConsoleLogger.printFatal("Failed to list import jobs");
            ex.printStackTrace();
            System.exit(0);
        }

        String existingImportJobId = null;
        for (DigitalTwinsImportJob importJob : importJobs) {
            existingImportJobId = importJob.getId();
            ConsoleLogger.print(String.format("\tImport Job Status: %s", importJob.getStatus()));
            ConsoleLogger.print(String.format("\tImport Job Created Time: %s", importJob.getCreatedDateTime()));
            if (importJob.getFinishedDateTime() != null) {
                ConsoleLogger.print(String.format("\tImport Job Finished Time: %s", importJob.getFinishedDateTime()));
            }
            ConsoleLogger.print("");
        }

        if (existingImportJobId != null) {
            ConsoleLogger.printHeader("Get import job");
            ConsoleLogger.print(String.format("Getting a single import job with Id %s", existingImportJobId));
            try {
                DigitalTwinsImportJob importJob = client.getImportJob(existingImportJobId);
                ConsoleLogger.print(String.format("Successfully retrieved import job response with Id %s", existingImportJobId));
                ConsoleLogger.print(String.format("\tImport Job Status: %s", importJob.getStatus()));
                ConsoleLogger.print(String.format("\tImport Job Created Time: %s", importJob.getCreatedDateTime()));
                if (importJob.getFinishedDateTime() != null) {
                    ConsoleLogger.print(String.format("\tImport Job Finished Time: %s", importJob.getFinishedDateTime()));
                }
            } catch (ErrorResponseException ex) {
                ConsoleLogger.printFatal(String.format("Failed to get import job with Id %s", existingImportJobId));
                ex.printStackTrace();
                System.exit(0);
            }
        } else {
            ConsoleLogger.print("No import job exist on your Azure Digital Twins instance yet.");
        }

        if (parsedArguments.getStorageAccountEndpoint() != null) {
            ConsoleLogger.printHeader("Create an import job");
            ConsoleLogger.print("A storage account was provided as an input parameter, so this sample will create a new import job");
            DigitalTwinsImportJob sampleImportJob = new DigitalTwinsImportJob(parsedArguments.getStorageAccountEndpoint() + "/sampleInputBlob.ndjson",
                parsedArguments.getStorageAccountEndpoint() + "/sampleOutputBlob.ndjson");
            String importJobId = "job-" + UUID.randomUUID();

            try {
                ConsoleLogger.print(String.format("Creating new importJob with Id %s", importJobId));
                client.createImportJob(importJobId, sampleImportJob);
                ConsoleLogger.print(String.format("Successfully created import job with Id %s", importJobId));
            } catch (ErrorResponseException ex) {
                ConsoleLogger.printFatal(String.format("Failed to create new import job with Id %s", importJobId));
                ex.printStackTrace();
                System.exit(0);
            }

            try {
                ConsoleLogger.printHeader("Cancel an import job");
                ConsoleLogger.print(String.format("Cancelling the newly created import job with Id %s", importJobId));
                client.cancelImportJob(importJobId);
                ConsoleLogger.print(String.format("Successfully cancelled import job with Id %s", importJobId));
            } catch (ErrorResponseException ex) {
                ConsoleLogger.printFatal(String.format("Failed to cancel import job with Id %s", importJobId));
                ex.printStackTrace();
                System.exit(0);
            }

            try {
                ConsoleLogger.printHeader("Delete an import job");
                ConsoleLogger.print(String.format("Deleting the newly created import job with Id %s", importJobId));
                client.deleteImportJob(importJobId);
                ConsoleLogger.print(String.format("Successfully deleted import job with Id %s", importJobId));
            } catch (ErrorResponseException ex) {
                ConsoleLogger.printFatal(String.format("Failed to delete import job with Id %s", importJobId));
                ex.printStackTrace();
                System.exit(0);
            }
        } else {
            ConsoleLogger.printWarning("No storage account name was provided as an input parameter, so this sample will not create a new import job");
            ConsoleLogger.printWarning("In order to create a new import job for this sample to use, use the Azure portal or the control plane client library.");
        }
    }
}
