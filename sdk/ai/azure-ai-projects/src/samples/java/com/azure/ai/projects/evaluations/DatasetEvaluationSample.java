// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.DatasetsClient;
import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.PendingUploadRequest;
import com.azure.ai.projects.models.PendingUploadResponse;
import com.azure.ai.projects.utils.SampleUtils;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource;
import com.openai.models.evals.runs.RunRetrieveResponse;
import com.openai.services.blocking.EvalService;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Uploads a JSONL dataset and evaluates its existing responses using coherence and F1.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME}. The project must have a default
 * Azure Storage connection. The sample deletes the evaluation and dataset version it creates.
 * The uploaded blob is retained because the upload SAS does not grant delete permission.
 */
public class DatasetEvaluationSample {
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        String model = EvaluationSampleUtils.requiredSetting("FOUNDRY_MODEL_NAME");
        Path file = SampleUtils.getPath("evaluations/evaluation-data.jsonl");
        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build());
        OpenAIClient client = builder.buildOpenAIClient();
        try {
            run(builder.buildDatasetsClient(), client.evals(), model, file);
        } finally {
            client.close();
        }
    }

    static RunRetrieveResponse run(DatasetsClient datasets, EvalService evaluations, String model, Path file) {
        return run(datasets, evaluations, model, file, "java-evaluation-" + UUID.randomUUID(), new BlobClientBuilder());
    }

    static RunRetrieveResponse run(DatasetsClient datasets, EvalService evaluations, String model, Path file,
        String name, BlobClientBuilder blobBuilder) {
        String version = "1";
        PendingUploadResponse pending = datasets.pendingUpload(name, version, new PendingUploadRequest());
        BlobClient blob = blobBuilder.endpoint(pending.getBlobReference().getCredential().getSasUrl())
            .blobName(name + "/" + file.getFileName()).buildClient();
        blob.upload(BinaryData.fromFile(file), false);
        System.out.printf("Uploaded source blob (retained; delete separately with Storage permissions): %s%n",
            blob.getBlobUrl());
        DatasetVersion dataset = datasets.createOrUpdateDatasetVersion(name, version,
            new FileDatasetVersion().setDataUrl(blob.getBlobUrl()));
        try {
            // BEGIN:com.azure.ai.projects.evaluations.datasetSource
            CreateEvalJsonlRunDataSource dataSource = CreateEvalJsonlRunDataSource.builder()
                .fileIdSource(dataset.getId())
                .build();
            // END:com.azure.ai.projects.evaluations.datasetSource
            return EvaluationSampleUtils.evaluate(evaluations, BuiltInEvaluatorsSample.createEvaluation(model),
                dataSource);
        } finally {
            datasets.deleteDatasetVersion(name, version);
            System.out.printf("Deleted dataset version: %s/%s%n", name, version);
        }
    }
}
