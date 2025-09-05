// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.AnalyzeTextOutputType;
import com.azure.ai.contentsafety.models.AnalyzeTextResult;
import com.azure.ai.contentsafety.models.TextBlocklistMatch;
import com.azure.ai.contentsafety.models.TextCategoriesAnalysis;
import com.azure.ai.contentsafety.models.TextCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public final class AnalyzeTextAsyncTests extends ContentSafetyClientTestBase {
    @Test
    public void testAnalyzeTextAsyncTests() {
        // method invocation
        AnalyzeTextResult response
            = contentSafetyAsyncClient.analyzeText(new AnalyzeTextOptions("This is text example")).block();

        // response assertion
        Assertions.assertNotNull(response);

        List<TextBlocklistMatch> responseBlocklistsMatchResults = response.getBlocklistsMatch();
        Assertions.assertEquals(0, responseBlocklistsMatchResults.size());
        TextCategoriesAnalysis responseHateResult = response.getCategoriesAnalysis().get(0);
        Assertions.assertNotNull(responseHateResult);

        TextCategory responseHateResultCategory = responseHateResult.getCategory();
        Assertions.assertEquals(TextCategory.HATE, responseHateResultCategory);
        int responseHateResultSeverity = responseHateResult.getSeverity();
        Assertions.assertEquals(0, responseHateResultSeverity);
        TextCategoriesAnalysis responseSelfHarmResult = response.getCategoriesAnalysis().get(1);
        Assertions.assertNotNull(responseSelfHarmResult);

        TextCategory responseSelfHarmResultCategory = responseSelfHarmResult.getCategory();
        Assertions.assertEquals(TextCategory.SELF_HARM, responseSelfHarmResultCategory);
        int responseSelfHarmResultSeverity = responseSelfHarmResult.getSeverity();
        Assertions.assertEquals(0, responseSelfHarmResultSeverity);
        TextCategoriesAnalysis responseSexualResult = response.getCategoriesAnalysis().get(2);
        Assertions.assertNotNull(responseSexualResult);

        TextCategory responseSexualResultCategory = responseSexualResult.getCategory();
        Assertions.assertEquals(TextCategory.SEXUAL, responseSexualResultCategory);
        int responseSexualResultSeverity = responseSexualResult.getSeverity();
        Assertions.assertEquals(0, responseSexualResultSeverity);
        TextCategoriesAnalysis responseViolenceResult = response.getCategoriesAnalysis().get(3);
        Assertions.assertNotNull(responseViolenceResult);

        TextCategory responseViolenceResultCategory = responseViolenceResult.getCategory();
        Assertions.assertEquals(TextCategory.VIOLENCE, responseViolenceResultCategory);
        int responseViolenceResultSeverity = responseViolenceResult.getSeverity();
        Assertions.assertEquals(0, responseViolenceResultSeverity);
    }

    @Test
    public void testAnalyzeTextAsyncWithText() {
        // method invocation
        AnalyzeTextResult response = contentSafetyAsyncClient.analyzeText("This is text example").block();

        // response assertion
        Assertions.assertNotNull(response);

        List<TextBlocklistMatch> responseBlocklistsMatchResults = response.getBlocklistsMatch();
        Assertions.assertEquals(0, responseBlocklistsMatchResults.size());
        TextCategoriesAnalysis responseHateResult = response.getCategoriesAnalysis().get(0);
        Assertions.assertNotNull(responseHateResult);

        TextCategory responseHateResultCategory = responseHateResult.getCategory();
        Assertions.assertEquals(TextCategory.HATE, responseHateResultCategory);
        int responseHateResultSeverity = responseHateResult.getSeverity();
        Assertions.assertEquals(0, responseHateResultSeverity);
        TextCategoriesAnalysis responseSelfHarmResult = response.getCategoriesAnalysis().get(1);
        Assertions.assertNotNull(responseSelfHarmResult);

        TextCategory responseSelfHarmResultCategory = responseSelfHarmResult.getCategory();
        Assertions.assertEquals(TextCategory.SELF_HARM, responseSelfHarmResultCategory);
        int responseSelfHarmResultSeverity = responseSelfHarmResult.getSeverity();
        Assertions.assertEquals(0, responseSelfHarmResultSeverity);
        TextCategoriesAnalysis responseSexualResult = response.getCategoriesAnalysis().get(2);
        Assertions.assertNotNull(responseSexualResult);

        TextCategory responseSexualResultCategory = responseSexualResult.getCategory();
        Assertions.assertEquals(TextCategory.SEXUAL, responseSexualResultCategory);
        int responseSexualResultSeverity = responseSexualResult.getSeverity();
        Assertions.assertEquals(0, responseSexualResultSeverity);
        TextCategoriesAnalysis responseViolenceResult = response.getCategoriesAnalysis().get(3);
        Assertions.assertNotNull(responseViolenceResult);

        TextCategory responseViolenceResultCategory = responseViolenceResult.getCategory();
        Assertions.assertEquals(TextCategory.VIOLENCE, responseViolenceResultCategory);
        int responseViolenceResultSeverity = responseViolenceResult.getSeverity();
        Assertions.assertEquals(0, responseViolenceResultSeverity);
    }

    @Test
    public void testAnalyzeTextAsyncEightSeverity() {
        // method invocation
        AnalyzeTextResult response = contentSafetyAsyncClient.analyzeText(
            new AnalyzeTextOptions("This is text example").setOutputType(AnalyzeTextOutputType.EIGHT_SEVERITY_LEVELS))
            .block();

        // response assertion
        Assertions.assertNotNull(response);

        List<TextBlocklistMatch> responseBlocklistsMatchResults = response.getBlocklistsMatch();
        Assertions.assertEquals(0, responseBlocklistsMatchResults.size());
        TextCategoriesAnalysis responseHateResult = response.getCategoriesAnalysis().get(0);
        Assertions.assertNotNull(responseHateResult);

        TextCategory responseHateResultCategory = responseHateResult.getCategory();
        Assertions.assertEquals(TextCategory.HATE, responseHateResultCategory);
        int responseHateResultSeverity = responseHateResult.getSeverity();
        Assertions.assertEquals(0, responseHateResultSeverity);
        TextCategoriesAnalysis responseSelfHarmResult = response.getCategoriesAnalysis().get(1);
        Assertions.assertNotNull(responseSelfHarmResult);

        TextCategory responseSelfHarmResultCategory = responseSelfHarmResult.getCategory();
        Assertions.assertEquals(TextCategory.SELF_HARM, responseSelfHarmResultCategory);
        int responseSelfHarmResultSeverity = responseSelfHarmResult.getSeverity();
        Assertions.assertEquals(0, responseSelfHarmResultSeverity);
        TextCategoriesAnalysis responseSexualResult = response.getCategoriesAnalysis().get(2);
        Assertions.assertNotNull(responseSexualResult);

        TextCategory responseSexualResultCategory = responseSexualResult.getCategory();
        Assertions.assertEquals(TextCategory.SEXUAL, responseSexualResultCategory);
        int responseSexualResultSeverity = responseSexualResult.getSeverity();
        Assertions.assertEquals(0, responseSexualResultSeverity);
        TextCategoriesAnalysis responseViolenceResult = response.getCategoriesAnalysis().get(3);
        Assertions.assertNotNull(responseViolenceResult);

        TextCategory responseViolenceResultCategory = responseViolenceResult.getCategory();
        Assertions.assertEquals(TextCategory.VIOLENCE, responseViolenceResultCategory);
        int responseViolenceResultSeverity = responseViolenceResult.getSeverity();
        Assertions.assertEquals(0, responseViolenceResultSeverity);
    }
}
