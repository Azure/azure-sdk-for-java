// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.projects.models.DataGenerationJob;
import com.azure.ai.projects.models.DataGenerationJobInputs;
import com.azure.ai.projects.models.DataGenerationJobScenario;
import com.azure.ai.projects.models.DataGenerationModelOptions;
import com.azure.ai.projects.models.FoundryFeaturesOptInKeys;
import com.azure.ai.projects.models.PromptDataGenerationJobSource;
import com.azure.ai.projects.models.SimpleQnADataGenerationJobOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;
import java.util.UUID;

/**
 * Sample demonstrating data generation job operations using the synchronous DataGenerationJobsClient.
 *
 * <p>Data generation jobs are a preview feature. Before running, set the following environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - optional, a model deployment name for creating a generation job.</li>
 * </ul>
 */
public class DataGenerationJobsSample {
    private static final FoundryFeaturesOptInKeys DATA_GENERATION_PREVIEW
        = FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW;

    private static final DataGenerationJobsClient DATA_GENERATION_JOBS_CLIENT = new AIProjectClientBuilder()
        .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildDataGenerationJobsClient();

    public static void main(String[] args) {
        listGenerationJobs();

        // Uncomment to create, retrieve, cancel, and delete a sample job.
//        createGetCancelAndDeleteGenerationJob();
    }

    public static void listGenerationJobs() {
        // BEGIN:com.azure.ai.projects.DataGenerationJobsSample.listGenerationJobs

        Iterable<DataGenerationJob> jobs = DATA_GENERATION_JOBS_CLIENT.listGenerationJobs(
            DATA_GENERATION_PREVIEW, 5, PageOrder.DESC, null, null);

        int count = 0;
        for (DataGenerationJob job : jobs) {
            count++;
            System.out.printf("Data generation job ID: %s%n", job.getId());
            System.out.printf("Status: %s%n", job.getStatus());
            if (job.getInputs() != null) {
                System.out.printf("Input name: %s%n", job.getInputs().getName());
            }
            System.out.println("-------------------------------------------------");
        }
        if (count == 0) {
            System.out.println("No data generation jobs found.");
        }

        // END:com.azure.ai.projects.DataGenerationJobsSample.listGenerationJobs
    }

    public static void createGetCancelAndDeleteGenerationJob() {
        // BEGIN:com.azure.ai.projects.DataGenerationJobsSample.createGenerationJob

        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        DataGenerationJob job = DATA_GENERATION_JOBS_CLIENT.createGenerationJob(
            createSampleDataGenerationJob(model),
            DATA_GENERATION_PREVIEW,
            UUID.randomUUID().toString()
        );

        System.out.printf("Created data generation job: %s%n", job.getId());
        System.out.printf("Status: %s%n", job.getStatus());

        // END:com.azure.ai.projects.DataGenerationJobsSample.createGenerationJob

        // BEGIN:com.azure.ai.projects.DataGenerationJobsSample.getCancelDeleteGenerationJob

        DataGenerationJob fetched = DATA_GENERATION_JOBS_CLIENT.getGenerationJob(job.getId(), DATA_GENERATION_PREVIEW);
        System.out.printf("Fetched data generation job: %s%n", fetched.getId());
        System.out.printf("Status: %s%n", fetched.getStatus());

        DataGenerationJob cancelled = DATA_GENERATION_JOBS_CLIENT.cancelGenerationJob(job.getId(), DATA_GENERATION_PREVIEW);
        System.out.printf("Cancelled data generation job: %s%n", cancelled.getId());
        System.out.printf("Status: %s%n", cancelled.getStatus());

        DATA_GENERATION_JOBS_CLIENT.deleteGenerationJob(job.getId(), DATA_GENERATION_PREVIEW);
        System.out.printf("Deleted data generation job: %s%n", job.getId());

        // END:com.azure.ai.projects.DataGenerationJobsSample.getCancelDeleteGenerationJob
    }

    private static DataGenerationJob createSampleDataGenerationJob(String model) {
        PromptDataGenerationJobSource source = new PromptDataGenerationJobSource(
            "Contoso TrailGear sells hiking backpacks and tents. Customer support should answer questions about "
                + "warranty coverage, product care, returns, and trail safety in a concise, friendly tone.")
            .setDescription("Contoso TrailGear support policy and product guidance.");

        SimpleQnADataGenerationJobOptions options = new SimpleQnADataGenerationJobOptions(1)
            .setModelOptions(new DataGenerationModelOptions(model));

        DataGenerationJobInputs inputs = new DataGenerationJobInputs(
            "java-sample-data-generation-job",
            Collections.singletonList(source),
            options,
            DataGenerationJobScenario.EVALUATION);

        return new DataGenerationJob().setInputs(inputs);
    }
}
