// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.DatasetsAsyncClient;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.ai.projects.utils.SampleUtils;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.async.EvalServiceAsync;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Asynchronously uploads a JSONL dataset and evaluates its existing responses.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME}. The project must have a default
 * Azure Storage connection. The sample deletes the evaluation and dataset version it creates.
 * The uploaded blob is retained because the upload SAS does not grant delete permission.
 */
public class DatasetEvaluationAsyncSample {
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        Path file = SampleUtils.getPath("evaluations/evaluation-data.jsonl");
        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build());
        OpenAIClientAsync client = builder.buildOpenAIAsyncClient();
        try {
            run(builder.buildDatasetsAsyncClient(), client.evals(), model, file).block();
        } finally {
            client.close();
        }
    }

    static Mono<RunRetrieveResponse> run(DatasetsAsyncClient datasets, EvalServiceAsync evaluations, String model,
        Path file) {
        return Mono.defer(() -> run(datasets, evaluations, model, file,
            "java-async-evaluation-" + UUID.randomUUID(), new BlobClientBuilder()));
    }

    static Mono<RunRetrieveResponse> run(DatasetsAsyncClient datasets, EvalServiceAsync evaluations, String model,
        Path file, String name, BlobClientBuilder blobBuilder) {
        String version = "1";
        return datasets.pendingUpload(name, version, new PendingUploadRequest()).flatMap(pending -> {
            BlobAsyncClient blob = blobBuilder.endpoint(pending.getBlobReference().getCredential().getSasUrl())
                .blobName(name + "/" + file.getFileName()).buildAsyncClient();
            return blob.upload(BinaryData.fromFile(file), false)
                .doOnSuccess(ignored -> System.out.printf(
                    "Uploaded source blob (retained; delete separately with Storage permissions): %s%n",
                    blob.getBlobUrl()))
                .then(Mono.usingWhen(datasets.createOrUpdateDatasetVersion(name, version,
                    new FileDatasetVersion().setDataUrl(blob.getBlobUrl())),
                    dataset -> EvaluationSampleUtils.evaluateAsync(evaluations,
                        BuiltInEvaluatorsSample.createEvaluation(model),
                        CreateEvalJsonlRunDataSource.builder().fileIdSource(dataset.getId()).build()),
                    dataset -> datasets.deleteDatasetVersion(name, version)
                        .doOnSuccess(ignored -> System.out.printf("Deleted dataset version: %s/%s%n", name, version))));
        });
    }
}
