// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.implementation.OpenAIJsonHelper;
import com.azure.ai.projects.models.AzureAIEvaluationDataSource;
import com.azure.ai.projects.models.TestingCriterionAzureAIEvaluator;
import com.azure.core.util.BinaryData;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.runs.RunCreateParams;

/**
 * Helper methods for Azure AI evaluations.
 */
public final class EvaluationsHelper {
    private EvaluationsHelper() {
    }

    /**
     * Converts an Azure evaluation run data source to the native OpenAI parameter union.
     * @param source Azure data source.
     * @return a native run data source preserving Azure-specific fields.
     */
    public static RunCreateParams.DataSource toDataSource(AzureAIEvaluationDataSource source) {
        return OpenAIJsonHelper.toOpenAIType(source, RunCreateParams.DataSource.class);
    }

    /**
     * Creates an Azure evaluation schema configuration.
     * @param scenario scenario such as responses, red_team, traces_preview, or benchmark_preview.
     * @return native evaluation data-source configuration.
     */
    public static EvalCreateParams.DataSourceConfig createDataSourceConfig(String scenario) {
        java.util.Map<String, String> configuration = new java.util.LinkedHashMap<>();
        configuration.put("type", "azure_ai_source");
        configuration.put("scenario", java.util.Objects.requireNonNull(scenario, "scenario"));
        return OpenAIJsonHelper.fromBinaryData(BinaryData.fromObject(configuration),
            EvalCreateParams.DataSourceConfig.class);
    }

    /**
     * Converts an Azure AI evaluator model to an OpenAI evaluation testing criterion.
     *
     * <p>Use this helper when creating OpenAI evaluations with Azure-specific evaluator types such as
     * {@link TestingCriterionAzureAIEvaluator}. The helper preserves the Azure evaluator wire shape while hiding the
     * serialization details needed to pass it to the OpenAI SDK.</p>
     *
     * @param evaluator The Azure AI evaluator to use as an evaluation testing criterion.
     * @return The OpenAI evaluation testing criterion.
     * @throws NullPointerException if {@code evaluator} is null.
     */
    public static EvalCreateParams.TestingCriterion toTestingCriterion(TestingCriterionAzureAIEvaluator evaluator) {
        return OpenAIJsonHelper.toOpenAIType(evaluator, EvalCreateParams.TestingCriterion.class);
    }
}
