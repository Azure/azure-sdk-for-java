// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.ai.contentsafety.models.AnalyzeImageOptions;
import com.azure.ai.contentsafety.models.AnalyzeImageResult;
import com.azure.ai.contentsafety.models.ContentSafetyImageData;
import com.azure.ai.contentsafety.models.ImageCategoriesAnalysis;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AnalyzeImage {
    public static void main(String[] args) throws IOException {
        // BEGIN:com.azure.ai.contentsafety.analyzeimage
        String endpoint = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY");

        ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
            .credential(new KeyCredential(key))
            .endpoint(endpoint).buildClient();

        ContentSafetyImageData image = new ContentSafetyImageData();
        String cwd = System.getProperty("user.dir");
        String source = "/src/samples/resources/image.png";
        image.setContent(BinaryData.fromBytes(Files.readAllBytes(Paths.get(cwd, source))));

        AnalyzeImageResult response =
            contentSafetyClient.analyzeImage(new AnalyzeImageOptions(image));

        for (ImageCategoriesAnalysis result : response.getCategoriesAnalysis()) {
            System.out.println(result.getCategory() + " severity: " + result.getSeverity());
        }
        // END:com.azure.ai.contentsafety.analyzeimage
    }
}
