// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.ApiError;
import com.azure.ai.projects.models.DataGenerationJob;
import com.azure.ai.projects.models.DataGenerationJobInputs;
import com.azure.ai.projects.models.DataGenerationJobOutput;
import com.azure.ai.projects.models.DataGenerationJobOutputOptions;
import com.azure.ai.projects.models.DataGenerationJobScenario;
import com.azure.ai.projects.models.DataGenerationModelOptions;
import com.azure.ai.projects.models.DatasetDataGenerationJobOutput;
import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FoundryFeaturesOptInKeys;
import com.azure.ai.projects.models.JobStatus;
import com.azure.ai.projects.models.PromptDataGenerationJobSource;
import com.azure.ai.projects.models.SimpleQnADataGenerationJobOptions;
import com.azure.ai.projects.models.TestingCriterionAzureAIEvaluator;
import com.azure.core.util.Configuration;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.EvalCreateParams.DataSourceConfig.Custom;
import com.openai.models.evals.EvalCreateParams.DataSourceConfig.Custom.ItemSchema;
import com.openai.models.evals.EvalCreateResponse;
import com.openai.models.evals.EvalDeleteParams;
import com.openai.models.evals.runs.CreateEvalCompletionsRunDataSource;
import com.openai.models.evals.runs.RunCreateParams;
import com.openai.models.evals.runs.RunCreateResponse;
import com.openai.models.evals.runs.RunRetrieveParams;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.models.evals.runs.outputitems.OutputItemListParams;
import com.openai.models.evals.runs.outputitems.OutputItemListResponse;
import com.openai.models.responses.EasyInputMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * End-to-end sample combining data generation with an OpenAI evaluation run.
 *
 * <p>The sample creates a Simple QnA data generation job from an inline prompt, waits for the generated dataset,
 * creates an OpenAI evaluation using Azure AI built-in evaluators, runs the evaluation against the dataset, and cleans
 * up the evaluation and data generation job.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - the model deployment name used for generation and judging.</li>
 *   <li>{@code DATASET_NAME} - optional, the generated dataset name.</li>
 *   <li>{@code POLL_INTERVAL_SECONDS} - optional, seconds to wait between polling attempts.</li>
 * </ul>
 */
public class DataGenerationJobWithEvaluationSample {
    private static final FoundryFeaturesOptInKeys DATA_GENERATION_PREVIEW
        = FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW;
    private static final String DEFAULT_DATASET_NAME = "dataset-generation-eval-sample";
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 10;

    public static void main(String[] args) throws InterruptedException {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String modelName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", DEFAULT_DATASET_NAME);
        int pollIntervalSeconds = Integer.parseInt(Configuration.getGlobalConfiguration()
            .get("POLL_INTERVAL_SECONDS", String.valueOf(DEFAULT_POLL_INTERVAL_SECONDS)));

        AIProjectClientBuilder projectClientBuilder = new AIProjectClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build());

        DataGenerationJobsClient dataGenerationJobsClient = projectClientBuilder.buildDataGenerationJobsClient();
        DatasetsClient datasetsClient = projectClientBuilder.buildDatasetsClient();
        OpenAIClient openAIClient = projectClientBuilder.buildOpenAIClient();

        DataGenerationJob job = null;
        EvalCreateResponse eval = null;

        try {
            System.out.println("Create a data generation job.");
            job = dataGenerationJobsClient.createGenerationJob(createDataGenerationJob(modelName, datasetName),
                DATA_GENERATION_PREVIEW, UUID.randomUUID().toString());
            System.out.printf("Created data generation job `%s` (status: `%s`).%n", job.getId(), job.getStatus());

            job = waitForDataGenerationJob(dataGenerationJobsClient, job.getId(), pollIntervalSeconds);
            System.out.printf("Final job status: `%s`.%n", job.getStatus());

            if (!JobStatus.SUCCEEDED.equals(job.getStatus())) {
                ApiError error = job.getError();
                String message = error == null ? "<no error message>" : error.getMessage();
                throw new IllegalStateException(String.format("Job `%s` ended with status `%s`: %s",
                    job.getId(), job.getStatus(), message));
            }

            DatasetDataGenerationJobOutput output = findDatasetOutput(job);
            DatasetVersion dataset = datasetsClient.getDatasetVersion(output.getName(), output.getVersion());
            System.out.printf("Generated dataset: name=`%s` version=`%s` id=`%s`%n",
                dataset.getName(), dataset.getVersion(), dataset.getId());

            System.out.println("Create the evaluation.");
            eval = openAIClient.evals().create(createEvaluationParams(modelName));
            System.out.printf("Evaluation created (id: %s).%n", eval.id());

            System.out.printf("Create an evaluation run that consumes dataset `%s`.%n", dataset.getId());
            RunCreateResponse evalRun = openAIClient.evals().runs().create(createEvaluationRunParams(eval.id(),
                dataset.getId(), modelName));
            System.out.printf("Evaluation run created (id: %s).%n", evalRun.id());

            RunRetrieveResponse completedRun = waitForEvaluationRun(openAIClient, eval.id(), evalRun.id(),
                pollIntervalSeconds);
            System.out.printf("Final eval run status: `%s`.%n", completedRun.status());

            if ("completed".equals(completedRun.status())) {
                System.out.printf("Result counts: %s%n", completedRun.resultCounts());
                System.out.printf("Eval run report URL: %s%n", completedRun.reportUrl());
                printOutputItems(openAIClient, eval.id(), evalRun.id());
            } else {
                System.out.println("Evaluation run did not complete successfully.");
            }
        } finally {
            if (eval != null) {
                System.out.printf("Delete evaluation `%s`.%n", eval.id());
                openAIClient.evals().delete(EvalDeleteParams.builder().evalId(eval.id()).build());
            }
            if (job != null) {
                System.out.printf("Delete the data generation job `%s`.%n", job.getId());
                dataGenerationJobsClient.deleteGenerationJob(job.getId(), DATA_GENERATION_PREVIEW);
            }
        }
    }

    static DataGenerationJob createDataGenerationJob(String modelName, String datasetName) {
        PromptDataGenerationJobSource source = new PromptDataGenerationJobSource(
            "Contoso offers a full refund within 30 days of purchase for any product returned in its original "
                + "condition. After 30 days, store credit may be issued at the discretion of customer support. "
                + "Digital goods are non-refundable once downloaded.")
            .setDescription("Contoso refund policy");

        SimpleQnADataGenerationJobOptions options = new SimpleQnADataGenerationJobOptions(15)
            .setModelOptions(new DataGenerationModelOptions(modelName));

        DataGenerationJobOutputOptions outputOptions = new DataGenerationJobOutputOptions()
            .setName(datasetName)
            .setDescription("QnA pairs generated from the Contoso refund policy prompt.")
            .setTags(Collections.singletonMap("sample", "dataset-generation-with-evaluation"));

        DataGenerationJobInputs inputs = new DataGenerationJobInputs("qna-from-policy-prompt",
            Collections.singletonList(source), options, DataGenerationJobScenario.EVALUATION)
            .setOutputOptions(outputOptions);

        return new DataGenerationJob().setInputs(inputs);
    }

    private static DataGenerationJob waitForDataGenerationJob(DataGenerationJobsClient dataGenerationJobsClient,
        String jobId, int pollIntervalSeconds) throws InterruptedException {
        System.out.printf("Poll job `%s` until it reaches a terminal state.", jobId);
        DataGenerationJob job;
        do {
            Thread.sleep(pollIntervalSeconds * 1000L);
            System.out.print(".");
            job = dataGenerationJobsClient.getGenerationJob(jobId, DATA_GENERATION_PREVIEW);
        } while (!isTerminalStatus(job.getStatus()));
        System.out.println();
        return job;
    }

    static boolean isTerminalStatus(JobStatus status) {
        return JobStatus.SUCCEEDED.equals(status)
            || JobStatus.FAILED.equals(status)
            || JobStatus.CANCELLED.equals(status);
    }

    static DatasetDataGenerationJobOutput findDatasetOutput(DataGenerationJob job) {
        if (job.getResult() != null && job.getResult().getOutputs() != null) {
            for (DataGenerationJobOutput output : job.getResult().getOutputs()) {
                if (output instanceof DatasetDataGenerationJobOutput) {
                    DatasetDataGenerationJobOutput datasetOutput = (DatasetDataGenerationJobOutput) output;
                    if (datasetOutput.getName() != null && datasetOutput.getVersion() != null) {
                        return datasetOutput;
                    }
                }
            }
        }

        throw new IllegalStateException(String.format("Job `%s` did not produce a dataset output.", job.getId()));
    }

    static EvalCreateParams createEvaluationParams(String modelName) {
        Map<String, Object> queryProperty = new LinkedHashMap<>();
        queryProperty.put("type", "string");

        Map<String, Object> groundTruthProperty = new LinkedHashMap<>();
        groundTruthProperty.put("type", "string");

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("query", queryProperty);
        properties.put("ground_truth", groundTruthProperty);

        ItemSchema itemSchema = ItemSchema.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty("properties", JsonValue.from(properties))
            .putAdditionalProperty("required", JsonValue.from(Collections.singletonList("query")))
            .build();

        Custom dataSourceConfig = Custom.builder()
            .itemSchema(itemSchema)
            .includeSampleSchema(true)
            .build();

        return EvalCreateParams.builder()
            .name("generated-qna-evaluation")
            .dataSourceConfig(dataSourceConfig)
            .testingCriteria(createAzureAIEvaluatorCriteria(modelName))
            .build();
    }

    private static List<EvalCreateParams.TestingCriterion> createAzureAIEvaluatorCriteria(String modelName) {
        Map<String, String> dataMapping = new LinkedHashMap<>();
        dataMapping.put("query", "{{item.query}}");
        dataMapping.put("response", "{{sample.output_text}}");
        return Arrays.asList(
            createAzureAIEvaluator("coherence", "builtin.coherence", modelName,
                dataMapping),
            createAzureAIEvaluator("fluency", "builtin.fluency", modelName,
                Collections.singletonMap("response", "{{sample.output_text}}")));
    }

    private static EvalCreateParams.TestingCriterion createAzureAIEvaluator(String name, String evaluatorName,
        String modelName, Map<String, String> dataMapping) {
        TestingCriterionAzureAIEvaluator evaluator = new TestingCriterionAzureAIEvaluator(name, evaluatorName)
            .setInitializationParameters(Collections.singletonMap("deployment_name", BinaryData.fromObject(modelName)))
            .setDataMapping(dataMapping);

        return EvaluationsHelper.toTestingCriterion(evaluator);
    }

    static RunCreateParams createEvaluationRunParams(String evalId, String datasetId, String modelName) {
        CreateEvalCompletionsRunDataSource.InputMessages.Template inputMessages
            = CreateEvalCompletionsRunDataSource.InputMessages.Template.builder()
                .addTemplate(EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.DEVELOPER)
                    .content("You are a Contoso customer-support assistant. Answer the user's question about the "
                        + "Contoso refund policy clearly and concisely.")
                    .build())
                .addTemplate(EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("{{item.query}}")
                    .build())
                .build();

        CreateEvalCompletionsRunDataSource dataSource = CreateEvalCompletionsRunDataSource.builder()
            .fileIdSource(datasetId)
            .type(CreateEvalCompletionsRunDataSource.Type.COMPLETIONS)
            .inputMessages(inputMessages)
            .model(modelName)
            .build();

        return RunCreateParams.builder()
            .evalId(evalId)
            .name("generated-qna-evaluation-run")
            .dataSource(dataSource)
            .build();
    }

    private static RunRetrieveResponse waitForEvaluationRun(OpenAIClient openAIClient, String evalId, String runId,
        int pollIntervalSeconds) throws InterruptedException {
        RunRetrieveResponse evalRun = openAIClient.evals().runs().retrieve(RunRetrieveParams.builder()
            .evalId(evalId)
            .runId(runId)
            .build());
        while (!"completed".equals(evalRun.status()) && !"failed".equals(evalRun.status())) {
            Thread.sleep(pollIntervalSeconds * 1000L);
            evalRun = openAIClient.evals().runs().retrieve(RunRetrieveParams.builder()
                .evalId(evalId)
                .runId(runId)
                .build());
        }
        return evalRun;
    }

    private static void printOutputItems(OpenAIClient openAIClient, String evalId, String runId) {
        int count = 0;
        for (OutputItemListResponse item : openAIClient.evals().runs().outputItems().list(OutputItemListParams.builder()
            .evalId(evalId)
            .runId(runId)
            .build()).autoPager()) {
            count++;
            System.out.printf("  item %d: status=%s | %s%n", count, item.status(), item.results());
        }
        System.out.printf("Output items (total: %d).%n", count);
    }
}
