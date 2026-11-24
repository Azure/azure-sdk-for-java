// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerWorkflow;
import com.azure.ai.contentunderstanding.models.ContentCategoryDefinition;
import com.azure.ai.contentunderstanding.models.ContentFieldDefinition;
import com.azure.ai.contentunderstanding.models.ContentFieldSchema;
import com.azure.ai.contentunderstanding.models.ContentFieldType;
import com.azure.ai.contentunderstanding.models.GenerationMethod;
import com.azure.ai.contentunderstanding.models.SemanticChunkingStrategy;
import com.azure.core.util.BinaryData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class PreviewSampleTestSupport {
    private PreviewSampleTestSupport() {
    }

    static ContentAnalyzer createDefaultWorkflowAnalyzer(String completionModel) {
        return createWorkflowAnalyzer(null, completionModel);
    }

    static ContentAnalyzer createAgenticWorkflowAnalyzer(String completionModel) {
        return createWorkflowAnalyzer(ContentAnalyzerWorkflow.AGENTIC, completionModel);
    }

    private static ContentAnalyzer createWorkflowAnalyzer(ContentAnalyzerWorkflow workflow, String completionModel) {
        Map<String, ContentFieldDefinition> fields = new HashMap<>();
        fields.put("InvoiceId",
            new ContentFieldDefinition().setType(ContentFieldType.STRING)
                .setDescription(
                    "Invoice identifier printed on the invoice. Return only the identifier value without its label."));
        fields.put("AverageItemPrice",
            new ContentFieldDefinition().setType(ContentFieldType.NUMBER)
                .setDescription(
                    "Calculate the arithmetic mean of all values in the UNIT PRICE column. Use only unit prices, "
                        + "not quantities, line amounts, subtotals, taxes, or totals."));
        ContentFieldSchema schema = new ContentFieldSchema().setName("invoice_workflow_comparison")
            .setDescription("Invoice fields used to compare workflows")
            .setFields(fields);
        ContentAnalyzerConfig config = new ContentAnalyzerConfig().setReturnDetails(true);
        if (workflow != null) {
            config.setWorkflow(workflow);
        }
        String workflowName = workflow == null ? "default" : workflow.toString();
        return new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription("Analyzer using the " + workflowName + " workflow")
            .setFieldSchema(schema)
            .setConfig(config)
            .setModels(Collections.singletonMap("completion", completionModel));
    }

    static ContentAnalyzer createGroundingAnalyzer(String completionModel) {
        Map<String, ContentFieldDefinition> fields = new HashMap<>();
        fields.put("company_name",
            new ContentFieldDefinition().setType(ContentFieldType.STRING)
                .setMethod(GenerationMethod.EXTRACT)
                .setDescription("Name of the company"));
        fields.put("document_summary",
            new ContentFieldDefinition().setType(ContentFieldType.STRING)
                .setMethod(GenerationMethod.GENERATE)
                .setDescription("A brief summary of the document content"));
        fields.put("document_type",
            new ContentFieldDefinition().setType(ContentFieldType.STRING)
                .setMethod(GenerationMethod.CLASSIFY)
                .setDescription("Type of document")
                .setEnumProperty(Arrays.asList("invoice", "receipt", "contract", "report", "other")));
        ContentFieldSchema schema = new ContentFieldSchema().setName("extract_generate_classify_grounding_schema")
            .setDescription("Schema for verifying extract, generate, and classify grounding")
            .setFields(fields);
        Map<String, String> models = new HashMap<>();
        models.put("completion", completionModel);
        models.put("embedding", "text-embedding-3-large");
        return new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription("Preview analyzer for extract, generate, and classify grounding")
            .setConfig(new ContentAnalyzerConfig().setLayoutEnabled(true)
                .setOcrEnabled(true)
                .setEstimateFieldSourceAndConfidence(true)
                .setReturnDetails(true))
            .setFieldSchema(schema)
            .setModels(models);
    }

    static ContentAnalyzer createChunkingAnalyzer(String completionModel) {
        return new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription("Analyzer with semantic chunking")
            .setConfig(new ContentAnalyzerConfig().setReturnDetails(true)
                .setLayoutEnabled(true)
                .setChunkingStrategy(new SemanticChunkingStrategy().setMaxTokens(300)))
            .setModels(Collections.singletonMap("completion", completionModel));
    }

    static ContentAnalyzer createInPageClassifier(String completionModel) {
        Map<String, ContentCategoryDefinition> categories = new HashMap<>();
        categories.put("Invoice", new ContentCategoryDefinition().setDescription(
            "An invoice requesting payment for goods or services, with line items, totals, and payment terms."));
        categories.put("BankStatement", new ContentCategoryDefinition().setDescription(
            "A bank account statement listing balances, deposits, withdrawals, fees, and transactions."));
        ContentAnalyzerConfig config = new ContentAnalyzerConfig().setReturnDetails(true)
            .setSegmentEnabled(true)
            .setAllowInPageSegments(true)
            .setEstimateFieldSourceAndConfidence(true)
            .setContentCategories(categories);
        return new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription("Classify financial documents that may share a page.")
            .setConfig(config)
            .setModels(Collections.singletonMap("completion", completionModel));
    }

    static BinaryData readSample(String fileName) throws IOException {
        return BinaryData.fromBytes(Files.readAllBytes(Paths.get("src/samples/resources", fileName)));
    }

}
