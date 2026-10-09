// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerWorkflow;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentFieldDefinition;
import com.azure.ai.contentunderstanding.models.ContentFieldSchema;
import com.azure.ai.contentunderstanding.models.ContentFieldType;
import com.azure.ai.contentunderstanding.models.ContentNumberField;
import com.azure.ai.contentunderstanding.models.ContentStringField;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Demonstrates default and agentic analyzer workflows by analyzing the same invoice with two custom analyzers.
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
 * <p>Configure model deployment defaults before running this sample; see {@link Sample00_UpdateDefaults}.</p>
 */
public class Sample17_CreateAnalyzerWorkflow {
    public static void main(String[] args) throws Exception {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW);
        ContentUnderstandingClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }
        String completionModel = getCompletionModel();
        String defaultAnalyzerId = "workflow-default-" + UUID.randomUUID().toString().replace("-", "");
        String agenticAnalyzerId = "workflow-agentic-" + UUID.randomUUID().toString().replace("-", "");

        try {
            ContentAnalyzer defaultAnalyzer = createAnalyzer(client, defaultAnalyzerId, null, completionModel);
            ContentAnalyzer agenticAnalyzer
                = createAnalyzer(client, agenticAnalyzerId, ContentAnalyzerWorkflow.AGENTIC, completionModel);
            verifyResolvedWorkflow(defaultAnalyzer, false, "Default analyzer");
            verifyResolvedWorkflow(agenticAnalyzer, true, "Agentic analyzer");

            BinaryData invoice = BinaryData.fromBytes(Files.readAllBytes(resolveSamplePath()));
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>
                defaultPoller = client.beginAnalyzeBinary(defaultAnalyzerId, invoice);
            AnalysisResult defaultResult
                = requireSuccessfulResult(defaultPoller.waitForCompletion().getStatus(),
                    defaultPoller::getFinalResult, "Default workflow analysis");
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>
                agenticPoller = client.beginAnalyzeBinary(agenticAnalyzerId, invoice);
            AnalysisResult agenticResult
                = requireSuccessfulResult(agenticPoller.waitForCompletion().getStatus(),
                    agenticPoller::getFinalResult, "Agentic workflow analysis");

            verifyAndPrintResults(defaultResult, agenticResult);
        } finally {
            deleteAnalyzer(client, defaultAnalyzerId);
            deleteAnalyzer(client, agenticAnalyzerId);
        }
    }

    private static ContentAnalyzer createAnalyzer(ContentUnderstandingClient client, String analyzerId,
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

        SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus, ContentAnalyzer> poller
            = client.beginCreateAnalyzer(analyzerId, analyzer, true);
        requireSuccessfulResult(poller.waitForCompletion().getStatus(), poller::getFinalResult,
            workflowName + " workflow analyzer creation");
        ContentAnalyzer retrieved = client.getAnalyzer(analyzerId);
        if (retrieved == null) {
            throw new IllegalStateException("Analyzer retrieval returned no result for " + analyzerId + ".");
        }
        return retrieved;
    }

    static void verifyResolvedWorkflow(ContentAnalyzer analyzer, boolean expectAgentic, String name) {
        if (analyzer == null || analyzer.getConfig() == null || analyzer.getConfig().getWorkflow() == null) {
            throw new IllegalStateException(name + " did not return a resolved workflow.");
        }
        boolean isAgentic
            = analyzer.getConfig().getWorkflow().toString().toLowerCase(Locale.ROOT).startsWith("agentic");
        if (isAgentic != expectAgentic) {
            throw new IllegalStateException(name + " resolved to unexpected workflow "
                + analyzer.getConfig().getWorkflow() + ".");
        }
    }

    static <T> T requireSuccessfulResult(LongRunningOperationStatus status, Supplier<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw new IllegalStateException(operationName + " completed unsuccessfully with status: " + status);
        }
        T result = finalResult.get();
        if (result == null) {
            throw new IllegalStateException(operationName + " completed without a final result.");
        }
        return result;
    }

    static void verifyAndPrintResults(AnalysisResult defaultResult, AnalysisResult agenticResult) {
        DocumentContent defaultContent = getDocumentContent(defaultResult, "Default workflow");
        DocumentContent agenticContent = getDocumentContent(agenticResult, "Agentic workflow");
        String defaultInvoiceId = getStringField(defaultContent, "InvoiceId", "Default workflow");
        String agenticInvoiceId = getStringField(agenticContent, "InvoiceId", "Agentic workflow");
        Double defaultAverage = getNumberField(defaultContent, "AverageItemPrice", false, "Default workflow");
        Double agenticAverage = getNumberField(agenticContent, "AverageItemPrice", true, "Agentic workflow");

        if (!"INV-2048".equals(defaultInvoiceId) || !"INV-2048".equals(agenticInvoiceId)) {
            throw new IllegalStateException("Both workflows should return InvoiceId INV-2048.");
        }
        if (Math.abs(agenticAverage - 20.5) > 0.01) {
            throw new IllegalStateException("Agentic AverageItemPrice should be 20.5 but was " + agenticAverage + ".");
        }

        System.out.println("Default workflow: InvoiceId=" + defaultInvoiceId + ", AverageItemPrice=" + defaultAverage);
        System.out.println("Agentic workflow: InvoiceId=" + agenticInvoiceId + ", AverageItemPrice=" + agenticAverage);
    }

    private static DocumentContent getDocumentContent(AnalysisResult result, String workflow) {
        if (result.getContents() == null || result.getContents().isEmpty()
            || !(result.getContents().get(0) instanceof DocumentContent)) {
            throw new IllegalStateException(workflow + " did not return DocumentContent.");
        }
        return (DocumentContent) result.getContents().get(0);
    }

    private static String getStringField(DocumentContent content, String fieldName, String workflow) {
        ContentField field = getFields(content, workflow).get(fieldName);
        if (!(field instanceof ContentStringField)) {
            throw new IllegalStateException(workflow + " did not return string field " + fieldName + ".");
        }
        String value = ((ContentStringField) field).getValue();
        if (value == null) {
            throw new IllegalStateException(workflow + " returned null for " + fieldName + ".");
        }
        return value;
    }

    private static Double getNumberField(DocumentContent content, String fieldName, boolean required,
        String workflow) {
        ContentField field = getFields(content, workflow).get(fieldName);
        if (!(field instanceof ContentNumberField)) {
            if (required) {
                throw new IllegalStateException(workflow + " did not return number field " + fieldName + ".");
            }
            return null;
        }
        Double value = ((ContentNumberField) field).getValue();
        if (required && value == null) {
            throw new IllegalStateException(workflow + " returned null for " + fieldName + ".");
        }
        return value;
    }

    private static Map<String, ContentField> getFields(DocumentContent content, String workflow) {
        if (content.getFields() == null) {
            throw new IllegalStateException(workflow + " did not return fields.");
        }
        return content.getFields();
    }

    private static String getCompletionModel() {
        return SampleModelConfiguration.getCompletionModel();
    }

    static Path resolveSamplePath() {
        Path packagePath = Paths.get("src/samples/resources/workflow_invoice_20_items.pdf");
        if (Files.exists(packagePath)) {
            return packagePath;
        }
        Path repositoryPath = Paths.get("sdk/contentunderstanding/azure-ai-contentunderstanding")
            .resolve(packagePath);
        if (Files.exists(repositoryPath)) {
            return repositoryPath;
        }
        throw new IllegalStateException("Could not locate workflow_invoice_20_items.pdf.");
    }

    private static void deleteAnalyzer(ContentUnderstandingClient client, String analyzerId) {
        try {
            client.deleteAnalyzer(analyzerId);
        } catch (RuntimeException error) {
            System.out.println("Note: Failed to delete analyzer '" + analyzerId + "': " + error.getMessage());
        }
    }
}
