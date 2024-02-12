// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisAsyncClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.DetectedTextLine;
import com.azure.ai.vision.imageanalysis.models.DetectedTextWord;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 *  This sample demonstrates how to extract printed or hand-written text from a publicly accessible
 *  image URL, using an asynchronous client.
 *
 *  This sample is similar to the `SampleOcrImageUrlAsync.java` sample, but it uses the 
 *  `analyzeWithResponseAsync` method instead of `analyzeAsync` method. This allows customization of the 
 *  HTTP request, and access to HTTP request and response details. This is not commonly required, but can
 *  be useful for service customization or troubleshooting issues.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleOcrImageUrlWithResponseAsync {

    public static void main(String[] args) throws InterruptedException {

        String endpoint = System.getenv("VISION_ENDPOINT");
        String key = System.getenv("VISION_KEY");

        if (endpoint == null || key == null) {
            System.out.println("Missing environment variable 'VISION_ENDPOINT' or 'VISION_KEY'.");
            System.out.println("Set them before running this sample.");
            System.exit(1);
        }

        // Create an asynchronous Image Analysis client.
        ImageAnalysisAsyncClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();

        // Examples of modifying the HTTP request. Not commonly used. Set to null if not used.
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.fromString("YourHeaderName"), "YourHeaderValue")
            .addQueryParam("YourQueryParameterName", "YourQueryParameterValue");

        // Extract text from an input image URL. This is an asynchronous (non-blocking) call.
        client.analyzeFromUrlWithResponse(
            "https://aka.ms/azsdk/image-analysis/sample.jpg", // imageUrl: the URL of the image to analyze
            Arrays.asList(VisualFeatures.READ), // visualFeatures
            null, // imageAnalysisOptions: There are no options for READ visual feature
            requestOptions) // requestOptions: Additional HTTP request options
            .subscribe(
                response -> { 
                    printHttpRequestAndResponse(response);
                    printAnalysisResults(response.getValue());
                },
                error -> System.err.println("Image analysis terminated with error message: " + error));

        // The .subscribe() is not a blocking call. For the purpose of this sample, we sleep
        // the thread so the program does not end before the analyze operation is complete.
        // Using .block() instead of .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(5);
    }

    // Print HTTP request and response to the console
    public static void printHttpRequestAndResponse(Response<ImageAnalysisResult> response) {

        HttpRequest request = response.getRequest();
        System.out.println(" HTTP request method: " + request.getHttpMethod());
        System.out.println(" HTTP request URL: " + request.getUrl());
        System.out.println(" HTTP request headers: ");
        request.getHeaders().forEach(header -> {
            System.out.println("   " + header.getName() + ": " + header.getValue());
        });
        if (request.getHeaders().getValue("content-type").contains("application/json")) {
            System.out.println(" HTTP request body: " + request.getBodyAsBinaryData().toString());
        }
        System.out.println(" HTTP response status code: " + response.getStatusCode());
        System.out.println(" HTTP response headers: ");
        response.getHeaders().forEach(header -> {
            System.out.println("   " + header.getName() + ": " + header.getValue());
        });
    }

    // Print analysis results to the console
    public static void printAnalysisResults(ImageAnalysisResult result) {

        System.out.println("Image analysis results:");
        System.out.println(" Read:");
        for (DetectedTextLine line : result.getRead().getBlocks().get(0).getLines()) {
            System.out.println("   Line: '" + line.getText()
                + "', Bounding polygon " + line.getBoundingPolygon());
            for (DetectedTextWord word : line.getWords()) {
                System.out.println("     Word: '" + word.getText()
                    + "', Bounding polygon " + word.getBoundingPolygon()
                    + ", Confidence " + String.format("%.4f", word.getConfidence()));
            }
        }
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
