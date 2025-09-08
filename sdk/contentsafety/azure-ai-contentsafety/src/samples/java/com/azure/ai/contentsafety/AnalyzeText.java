// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.AnalyzeTextResult;
import com.azure.ai.contentsafety.models.TextCategoriesAnalysis;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;


public class AnalyzeText {
    public static void main(String[] args) {
        // BEGIN:com.azure.ai.contentsafety.analyzetext
        String endpoint = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY");
        ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
            .credential(new KeyCredential(key))
            .endpoint(endpoint).buildClient();

        AnalyzeTextResult response = contentSafetyClient.analyzeText(new AnalyzeTextOptions("This is text example"));

        for (TextCategoriesAnalysis result : response.getCategoriesAnalysis()) {
            System.out.println(result.getCategory() + " severity: " + result.getSeverity());
        }
        // END:com.azure.ai.contentsafety.analyzetext
    }
}
