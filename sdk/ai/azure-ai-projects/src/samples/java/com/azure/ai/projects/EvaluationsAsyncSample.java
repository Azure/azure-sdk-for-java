// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Evaluation;
import com.azure.ai.projects.models.EvaluatorConfiguration;
import com.azure.ai.projects.models.EvaluatorId;
import com.azure.ai.projects.models.InputDataset;
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
        // Using block() to wait for the async operations to complete in the sample
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
                System.out.println("Data Type: " + evaluation.getData().getType());
                
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
        String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");
        String version = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1");
        
        return datasetsAsyncClient.getDatasetVersion(datasetName, version)
            .flatMap(datasetVersion -> {
                InputDataset dataset = new InputDataset(datasetVersion.getId());
                Evaluation evaluation = new Evaluation(
                    dataset,
                    mapOf("relevance",
                        new EvaluatorConfiguration(EvaluatorId.RELEVANCE.getValue())
                            .setInitParams(mapOf("deployment_name", BinaryData.fromObject("gpt-4o")))))
                    .setDisplayName("Sample Evaluation")
                    .setDescription("This is a sample evaluation created using the SDK");

                // Create the evaluation
                return evaluationsAsyncClient.createEvaluation(evaluation);
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
