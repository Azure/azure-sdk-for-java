// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to generate a human-readable sentence that describes the content
 *  of the image file sample.jpg, using a synchronous client.
 *
 *  This sample is similar to the `SampleCaptionImageFile.java` sample, but it uses the 
 *  `analyzeWithResponse` method instead of `analyze` method. This allows customization of the 
 *  HTTP request, and access to HTTP request and response details. This is not commonly required, but can
 *  be useful for service customization or troubleshooting issues.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleCaptionImageFileWithResponse {

    public static void main(String[] args) {

        String endpoint = System.getenv("VISION_ENDPOINT");
        String key = System.getenv("VISION_KEY");

        if (endpoint == null || key == null) {
            System.out.println("Missing environment variable 'VISION_ENDPOINT' or 'VISION_KEY'.");
            System.out.println("Set them before running this sample.");
            System.exit(1);
        }

        // Create a synchronous Image Analysis client.
        ImageAnalysisClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();

        // Examples of modifying the HTTP request. Not commonly used. Set to null if not used.
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.fromString("YourHeaderName"), "YourHeaderValue")
            .addQueryParam("YourQueryParameterName", "YourQueryParameterValue");

        // Generate a caption for an input image buffer. This is a synchronous (blocking) call.
        Response<ImageAnalysisResult> response = client.analyzeWithResponse(
            BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
            Arrays.asList(VisualFeatures.CAPTION), // visualFeatures
            new ImageAnalysisOptions().setGenderNeutralCaption(true), // imageAnalysisOptions:  Set to 'true' or 'false' (relevant for CAPTION or DENSE_CAPTIONS visual features)
            requestOptions); // requestOptions: Additional HTTP request options

        // Print HTTP request details to console
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

        // Print HTTP response details to console
        System.out.println(" HTTP response status code: " + response.getStatusCode());
        System.out.println(" HTTP response headers: ");
        response.getHeaders().forEach(header -> {
            System.out.println("   " + header.getName() + ": " + header.getValue());
        });

        // Print analysis results to the console
        ImageAnalysisResult result = response.getValue();
        System.out.println("Image analysis results:");
        System.out.println(" Caption:");
        System.out.println("   \"" + result.getCaption().getText() + "\", Confidence " 
            + String.format("%.4f", result.getCaption().getConfidence()));
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
