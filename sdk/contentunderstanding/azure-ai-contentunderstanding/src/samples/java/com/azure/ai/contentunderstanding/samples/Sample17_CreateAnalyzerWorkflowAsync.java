// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerWorkflow;
import com.azure.ai.contentunderstanding.models.ContentFieldDefinition;
import com.azure.ai.contentunderstanding.models.ContentFieldSchema;
import com.azure.ai.contentunderstanding.models.ContentFieldType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Demonstrates default and agentic analyzer workflows with the asynchronous client by analyzing the same invoice
 * with two custom analyzers.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>The workflow is the only difference between the analyzers:</p>
 * <ul>
 *   <li>Omit {@link ContentAnalyzerConfig#setWorkflow(ContentAnalyzerWorkflow)} or set
 *       {@link ContentAnalyzerWorkflow#DEFAULT} explicitly for straightforward field extraction.</li>
 *   <li>Use {@link ContentAnalyzerWorkflow#AGENTIC} when an answer must be built from evidence through multistep
 *       reasoning, calculations, validation, or analysis of complex tables and figures.</li>
 * </ul>
 *
 * <p>The sample compares a directly extracted invoice ID with a derived average item price. Analysis supports one
 * input file per request regardless of service API version or workflow. Agentic analysis typically takes longer,
 * consumes more model tokens, and uses the advanced contextualization rate.</p>
 *
 * <p>For the included invoice, both workflows should return {@code InvoiceId=INV-2048}. The default workflow can
 * approximate {@code AverageItemPrice}, so its value can vary between runs. The agentic workflow uses reasoning and
 * calculation and is expected to return the accurate value {@code 20.5}.</p>
 *
 * <p>Configure model deployment defaults before running this sample; see {@link Sample00_UpdateDefaultsAsync}.</p>
 */
public class Sample17_CreateAnalyzerWorkflowAsync {
    public static void main(String[] args) throws Exception {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW);
        ContentUnderstandingAsyncClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildAsyncClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildAsyncClient();
        }
        String model = getCompletionModel();
        String defaultId = "workflow-default-" + UUID.randomUUID().toString().replace("-", "");
        String agenticId = "workflow-agentic-" + UUID.randomUUID().toString().replace("-", "");
        BinaryData invoice
            = BinaryData.fromBytes(Files.readAllBytes(Sample17_CreateAnalyzerWorkflow.resolveSamplePath()));

        Mono<Boolean> workflow = Mono.when(createAnalyzer(client, defaultId, null, model),
            createAnalyzer(client, agenticId, ContentAnalyzerWorkflow.AGENTIC, model))
            .then(Mono.zip(analyze(client, defaultId, invoice), analyze(client, agenticId, invoice)))
            .map(results -> {
                Sample17_CreateAnalyzerWorkflow.verifyAndPrintResults(results.getT1(), results.getT2());
                return Boolean.TRUE;
            })
            .switchIfEmpty(Mono.error(new IllegalStateException("Workflow comparison returned no result.")));

        Boolean completed
            = runWithCleanup(workflow, () -> deleteAnalyzers(client, defaultId, agenticId)).block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("Workflow comparison returned no result.");
        }
    }

    private static Mono<ContentAnalyzer> createAnalyzer(ContentUnderstandingAsyncClient client, String analyzerId,
        ContentAnalyzerWorkflow workflow, String completionModel) {
        Map<String, ContentFieldDefinition> fields = new HashMap<>();
        fields.put("InvoiceId", new ContentFieldDefinition().setType(ContentFieldType.STRING)
            .setDescription("Invoice identifier printed on the invoice. Return only the identifier value without its label."));
        fields.put("AverageItemPrice", new ContentFieldDefinition().setType(ContentFieldType.NUMBER)
            .setDescription("Calculate the arithmetic mean of all values in the UNIT PRICE column. Use only unit prices, "
                + "not quantities, line amounts, subtotals, taxes, or totals."));
        ContentFieldSchema schema = new ContentFieldSchema().setName("invoice_workflow_comparison")
            .setDescription("Invoice fields used to compare workflows").setFields(fields);
        Map<String, String> models = new HashMap<>();
        models.put("completion", completionModel);
        ContentAnalyzerConfig config = new ContentAnalyzerConfig().setReturnDetails(true);
        if (workflow != null) {
            config.setWorkflow(workflow);
        }
        String workflowName = workflow == null ? "default" : workflow.toString();
        ContentAnalyzer analyzer = new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription("Analyzer using the " + workflowName + " workflow")
            .setFieldSchema(schema)
            .setConfig(config)
            .setModels(models);
        boolean expectAgentic = workflow != null;
        return client.beginCreateAnalyzer(analyzerId, analyzer, true)
            .last()
            .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                workflowName + " workflow analyzer creation"))
            .flatMap(created -> client.getAnalyzer(analyzerId)
                .switchIfEmpty(Mono.error(new IllegalStateException(
                    "Analyzer retrieval returned no result for " + analyzerId + "."))))
            .doOnNext(retrieved -> Sample17_CreateAnalyzerWorkflow.verifyResolvedWorkflow(
                retrieved, expectAgentic, workflowName + " workflow analyzer"));
    }

    private static Mono<AnalysisResult> analyze(ContentUnderstandingAsyncClient client, String analyzerId,
        BinaryData invoice) {
        return client.beginAnalyzeBinary(analyzerId, invoice)
            .last()
            .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                analyzerId + " analysis"));
    }

    private static Mono<Void> deleteAnalyzers(ContentUnderstandingAsyncClient client, String... analyzerIds) {
        return Mono.defer(() -> Mono.when(java.util.Arrays.stream(analyzerIds)
            .map(analyzerId -> deleteAnalyzer(client, analyzerId))
            .toArray(Mono[]::new)));
    }

    private static Mono<Void> deleteAnalyzer(ContentUnderstandingAsyncClient client, String analyzerId) {
        return client.deleteAnalyzer(analyzerId).onErrorResume(error -> {
            System.out.println("Note: Failed to delete analyzer '" + analyzerId + "': " + error.getMessage());
            return Mono.empty();
        });
    }

    static <T> Mono<T> requireSuccessfulResult(LongRunningOperationStatus status, Mono<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            return Mono
                .error(new IllegalStateException(operationName + " completed unsuccessfully with status: " + status));
        }
        return finalResult
            .switchIfEmpty(Mono.error(new IllegalStateException(operationName + " completed without a final result.")));
    }

    static <T> Mono<T> runWithCleanup(Mono<T> workflow, Supplier<Mono<Void>> cleanup) {
        return Mono.usingWhen(Mono.just(Boolean.TRUE),
            ignored -> workflow
                .switchIfEmpty(Mono.error(new IllegalStateException("Workflow completed without a result."))),
            ignored -> cleanup.get(), (ignored, error) -> cleanup.get(), ignored -> cleanup.get());
    }

    private static String getCompletionModel() {
        return SampleModelConfiguration.getCompletionModel();
    }
}
