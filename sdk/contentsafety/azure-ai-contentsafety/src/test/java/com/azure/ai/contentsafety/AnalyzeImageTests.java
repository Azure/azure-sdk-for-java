// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.ai.contentsafety.models.AnalyzeImageOptions;
import com.azure.ai.contentsafety.models.AnalyzeImageResult;
import com.azure.ai.contentsafety.models.ContentSafetyImageData;
import com.azure.ai.contentsafety.models.ImageCategoriesAnalysis;
import com.azure.ai.contentsafety.models.ImageCategory;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class AnalyzeImageTests extends ContentSafetyClientTestBase {
    @Test
    public void testAnalyzeImageAsyncWithContent() throws IOException {
        // method invocation
        ContentSafetyImageData image = new ContentSafetyImageData();
        String cwd = System.getProperty("user.dir");
        String source = "/src/samples/resources/image.png";
        image.setContent(BinaryData.fromBytes(Files.readAllBytes(Paths.get(cwd, source))));

        AnalyzeImageResult response = contentSafetyAsyncClient.analyzeImage(new AnalyzeImageOptions(image)).block();

        // response assertion
        Assertions.assertNotNull(response);
        Assertions.assertEquals(4, response.getCategoriesAnalysis().size());

        ImageCategoriesAnalysis responseHateResult = response.getCategoriesAnalysis().get(0);
        Assertions.assertNotNull(responseHateResult);

        ImageCategory responseHateResultCategory = responseHateResult.getCategory();
        Assertions.assertEquals(ImageCategory.HATE, responseHateResultCategory);
        int responseHateResultSeverity = responseHateResult.getSeverity();
        Assertions.assertEquals(0, responseHateResultSeverity);
    }

    @Test
    public void testAnalyzeImageAsyncWithConvenientContent() throws IOException {
        // method invocation
        String cwd = System.getProperty("user.dir");
        String source = "/src/samples/resources/image.png";
        BinaryData content = BinaryData.fromBytes(Files.readAllBytes(Paths.get(cwd, source)));

        AnalyzeImageResult response = contentSafetyAsyncClient.analyzeImage(content).block();

        // response assertion
        Assertions.assertNotNull(response);
        Assertions.assertEquals(4, response.getCategoriesAnalysis().size());

        ImageCategoriesAnalysis responseHateResult = response.getCategoriesAnalysis().get(0);
        Assertions.assertNotNull(responseHateResult);

        ImageCategory responseHateResultCategory = responseHateResult.getCategory();
        Assertions.assertEquals(ImageCategory.HATE, responseHateResultCategory);
        int responseHateResultSeverity = responseHateResult.getSeverity();
        Assertions.assertEquals(0, responseHateResultSeverity);
    }

    @Test
    public void testAnalyzeImageAsyncWithBlobUri() throws IOException {
        // method invocation
        ContentSafetyImageData image = new ContentSafetyImageData();
        image.setBlobUrl("https://cmbugbashsampledata.blob.core.windows.net/image-sdk-test/image.png");

        AnalyzeImageResult response = contentSafetyAsyncClient.analyzeImage(new AnalyzeImageOptions(image)).block();

        // response assertion
        Assertions.assertNotNull(response);
        Assertions.assertEquals(4, response.getCategoriesAnalysis().size());

        ImageCategoriesAnalysis responseHateResult = response.getCategoriesAnalysis().get(0);
        Assertions.assertNotNull(responseHateResult);

        ImageCategory responseHateResultCategory = responseHateResult.getCategory();
        Assertions.assertEquals(ImageCategory.HATE, responseHateResultCategory);
        int responseHateResultSeverity = responseHateResult.getSeverity();
        Assertions.assertEquals(0, responseHateResultSeverity);
    }

    @Test
    public void testAnalyzeImageAsyncWithConvenientBlobUri() throws IOException {
        // method invocation
        String blobUrl = "https://cmbugbashsampledata.blob.core.windows.net/image-sdk-test/image.png";

        AnalyzeImageResult response = contentSafetyAsyncClient.analyzeImage(blobUrl).block();

        // response assertion
        Assertions.assertNotNull(response);
        Assertions.assertEquals(4, response.getCategoriesAnalysis().size());

        ImageCategoriesAnalysis responseHateResult = response.getCategoriesAnalysis().get(0);
        Assertions.assertNotNull(responseHateResult);

        ImageCategory responseHateResultCategory = responseHateResult.getCategory();
        Assertions.assertEquals(ImageCategory.HATE, responseHateResultCategory);
        int responseHateResultSeverity = responseHateResult.getSeverity();
        Assertions.assertEquals(0, responseHateResultSeverity);
    }
}
