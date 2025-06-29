// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Evaluation;
import com.azure.ai.projects.models.EvaluatorConfiguration;
import com.azure.ai.projects.models.EvaluatorId;
import com.azure.ai.projects.models.InputDataset;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

public class EvaluationsAsyncSample {
    private static AIProjectClientBuilder clientBuilder
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build());

    private static EvaluationsAsyncClient evaluationsAsyncClient = clientBuilder.buildEvaluationsAsyncClient();
    private static DatasetsAsyncClient datasetsAsyncClient = clientBuilder.buildDatasetsAsyncClient();

    public static void main(String[] args) {
        //createEvaluation().block();
        //getEvaluation().block();
        listEvaluations().blockLast();
    }

    public static Flux<Evaluation> listEvaluations() {
        // BEGIN:com.azure.ai.projects.EvaluationsAsyncSample.listEvaluations

        System.out.println("Listing all evaluations:");
        return evaluationsAsyncClient.listEvaluations()
            .doOnNext(evaluation -> {
                System.out.println("Display Name: " + evaluation.getDisplayName());
                System.out.println("Status: " + evaluation.getStatus());
                
                if (evaluation.getDescription() != null) {
                    System.out.println("Description: " + evaluation.getDescription());
                }
                
                System.out.println("Evaluators:");
                evaluation.getEvaluators().forEach((name, evaluator) -> {
                    System.out.println("  - " + name + ": " + evaluator.getId());
                });
            });

        // END:com.azure.ai.projects.EvaluationsAsyncSample.listEvaluations
    }

    public static Mono<Evaluation> getEvaluation() {
        // BEGIN:com.azure.ai.projects.EvaluationsAsyncSample.getEvaluation

        String evaluationId = Configuration.getGlobalConfiguration().get("EVALUATION_ID", "my-evaluation-id");
        
        return evaluationsAsyncClient.getEvaluation(evaluationId)
            .doOnNext(evaluation -> {
                System.out.println("Retrieved evaluation:");
                System.out.println("Display Name: " + evaluation.getDisplayName());
                System.out.println("Status: " + evaluation.getStatus());
                System.out.println("Data Type: " + evaluation.getData().getType());
                
                if (evaluation.getDescription() != null) {
                    System.out.println("Description: " + evaluation.getDescription());
                }
                
                if (evaluation.getTags() != null) {
                    System.out.println("Tags:");
                    evaluation.getTags().forEach((key, value) -> {
                        System.out.println("  " + key + ": " + value);
                    });
                }
                
                System.out.println("Evaluators:");
                evaluation.getEvaluators().forEach((name, evaluator) -> {
                    System.out.println("  - " + name + ": " + evaluator.getId());
                    
                    if (evaluator.getDataMapping() != null) {
                        System.out.println("    Data Mapping:");
                        evaluator.getDataMapping().forEach((k, v) -> {
                            System.out.println("      " + k + " -> " + v);
                        });
                    }
                });
            });

        // END:com.azure.ai.projects.EvaluationsAsyncSample.getEvaluation
    }

    public static Mono<Evaluation> createEvaluation() {
        // BEGIN:com.azure.ai.projects.EvaluationsAsyncSample.createEvaluation

        // Create an evaluation definition
        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
        String version = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");
        String modelEndpoint = Configuration.getGlobalConfiguration().get("MODEL_ENDPOINT", "https://your-model-endpoint.com");
        String modelApiKey = Configuration.getGlobalConfiguration().get("MODEL_API_KEY", "your-model-api-key");
        String modelName = Configuration.getGlobalConfiguration().get("MODEL_NAME", "gpt-4o-mini");


        return datasetsAsyncClient.getDatasetVersion(datasetName, version)
            .flatMap(datasetVersion -> {
                InputDataset dataset = new InputDataset(datasetVersion.getId());
                Evaluation evaluation = new Evaluation(
                    dataset,
                    mapOf("relevance",
                        new EvaluatorConfiguration(EvaluatorId.RELEVANCE.getValue())
                            .setInitParams(mapOf("deployment_name", BinaryData.fromObject(modelName)))))
                    .setDisplayName("Sample Evaluation")
                    .setDescription("This is a sample evaluation created using the SDK");

                // Create the evaluation
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setHeader("model-endpoint", modelEndpoint);
                requestOptions.setHeader("api-key", modelApiKey);

                return evaluationsAsyncClient.createEvaluationWithResponse(BinaryData.fromObject(evaluation), requestOptions)
                    .map(response -> response.getValue().toObject(Evaluation.class));
            })
            .doOnNext(createdEvaluation -> {
                System.out.println("Created evaluation:");
                System.out.println("Display Name: " + createdEvaluation.getDisplayName());
                System.out.println("Status: " + createdEvaluation.getStatus());
            });

        // END:com.azure.ai.projects.EvaluationsAsyncSample.createEvaluation
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
