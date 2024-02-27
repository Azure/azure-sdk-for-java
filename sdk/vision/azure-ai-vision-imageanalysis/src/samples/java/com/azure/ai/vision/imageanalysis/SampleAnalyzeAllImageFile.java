// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.CropRegion;
import com.azure.ai.vision.imageanalysis.models.DenseCaption;
import com.azure.ai.vision.imageanalysis.models.DetectedObject;
import com.azure.ai.vision.imageanalysis.models.DetectedPerson;
import com.azure.ai.vision.imageanalysis.models.DetectedTag;
import com.azure.ai.vision.imageanalysis.models.DetectedTextLine;
import com.azure.ai.vision.imageanalysis.models.DetectedTextWord;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to analyze all supported visual features from the image file sample.jpg,
 *  using a synchronous client.
 *
 *  The synchronous (blocking) `analyze` method makes a single REST call to the Azure AI Vision
 *  service, where all visual features are analyzed in parallel. When the service responds, the method returns
 *  an `ImageAnalysisResult` object, which contains separate result properties for each one of the visual features.
 *  This sample prints all the results to the console.
 *
 *  The sample also shows how to turn on console SDK logs by calling httpLogOptions, which may be needed
 *  for troubleshooting purposes. You will also need to set environment variable `AZURE_LOG_LEVEL` to `debug`
 *  to see the logs.
 *
 *  For more information on a particular visual feature, and optional setting associated with it,
 *  have a look at the sample in this folder dedicated to that visual feature.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleAnalyzeAllImageFile {

    public static void main(String[] args) {

        String endpoint = System.getenv("VISION_ENDPOINT");
        String key = System.getenv("VISION_KEY");

        if (endpoint == null || key == null) {
            System.out.println("Missing environment variable 'VISION_ENDPOINT' or 'VISION_KEY'.");
            System.out.println("Set them before running this sample.");
            System.exit(1);
        }

        // Create a synchronous Image Analysis client, with logging enabled.
        // For log levels, see: https://learn.microsoft.com/java/api/com.azure.core.http.policy.httplogdetaillevel?view=azure-java-stable
        ImageAnalysisClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        // Specify analysis options (or set `options` to null for defaults)
        ImageAnalysisOptions options = new ImageAnalysisOptions()
            .setLanguage("en") // language (optional): Relevant only for TAGS. See https://aka.ms/cv-languages for supported languages.
            .setGenderNeutralCaption(true) // genderNeutralCaption (optional): Relevant only if CAPTION or DENSE_CAPTIONS were specified above.
            .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33)) // smartCropsAspectRatios (optional). Relevant only if SMART_CROPS was specified above.
            .setModelVersion("latest"); // modelVersion (optional): The model version to use. When not specified, the default value of "latest" is used.

        try {

            // Analyze all visual features from an image stream. This is a synchronous (blocking) call.
            ImageAnalysisResult result = client.analyze(
                BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: the image file loaded into memory as BinaryData
                Arrays.asList(
                    VisualFeatures.SMART_CROPS,
                    VisualFeatures.CAPTION,
                    VisualFeatures.DENSE_CAPTIONS,
                    VisualFeatures.OBJECTS,
                    VisualFeatures.PEOPLE,
                    VisualFeatures.READ,
                    VisualFeatures.TAGS), // visualFeatures: Select one or more visual features to analyze.
                options);

            printAnalysisResults(result);

        } catch (HttpResponseException e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());
            System.out.println("Status code: " + e.getResponse().getStatusCode());
            System.out.println("Message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Message: " + e.getMessage());
        }
    }

    // Print all analysis results to the console
    public static void printAnalysisResults(ImageAnalysisResult result) {

        System.out.println("Image analysis results:");

        if (result.getCaption() != null) {
            System.out.println(" Caption:");
            System.out.println("   \"" + result.getCaption().getText() + "\", Confidence "
                + String.format("%.4f", result.getCaption().getConfidence()));
        }

        if (result.getDenseCaptions() != null) {
            System.out.println(" Dense Captions:");
            for (DenseCaption denseCaption : result.getDenseCaptions().getValues()) {
                System.out.println("   \"" + denseCaption.getText() + "\", Bounding box "
                    + denseCaption.getBoundingBox() + ", Confidence " + String.format("%.4f", denseCaption.getConfidence()));
            }
        }

        if (result.getRead() != null) {
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
        }

        if (result.getTags() != null) {
            System.out.println(" Tags:");
            for (DetectedTag tag : result.getTags().getValues()) {
                System.out.println("   \"" + tag.getName() + "\", Confidence " + String.format("%.4f", tag.getConfidence()));
            }
        }

        if (result.getObjects() != null) {
            System.out.println(" Objects:");
            for (DetectedObject detectedObject : result.getObjects().getValues()) {
                System.out.println("   \"" + detectedObject.getTags().get(0).getName() + "\", Bounding box "
                    + detectedObject.getBoundingBox() + ", Confidence " + String.format("%.4f", detectedObject.getTags().get(0).getConfidence()));
            }
        }

        if (result.getPeople() != null) {
            System.out.println(" People:");
            for (DetectedPerson person : result.getPeople().getValues()) {
                System.out.println("   Bounding box "
                    + person.getBoundingBox() + ", Confidence " + String.format("%.4f", person.getConfidence()));
            }
        }

        if (result.getSmartCrops() != null) {
            System.out.println(" Crop Suggestions:");
            for (CropRegion cropRegion : result.getSmartCrops().getValues()) {
                System.out.println("   Aspect ratio "
                    + cropRegion.getAspectRatio() + ": Bounding box " + cropRegion.getBoundingBox());
            }
        }

        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
