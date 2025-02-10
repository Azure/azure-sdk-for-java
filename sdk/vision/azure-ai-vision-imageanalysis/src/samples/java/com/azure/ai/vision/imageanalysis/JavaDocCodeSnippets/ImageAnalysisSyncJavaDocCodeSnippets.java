// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Source code snippets from this file are embedded in Image Analysis SDK JavaDoc (API documentations).
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
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.File;
import java.util.Arrays;

public class ImageAnalysisSyncJavaDocCodeSnippets {

    private static String endpoint;
    private static String key;

    public static void main(String[] args) {

        endpoint = System.getenv("VISION_ENDPOINT");
        key = System.getenv("VISION_KEY");

        if (endpoint == null || key == null) {
            System.out.println("Missing environment variable 'VISION_ENDPOINT' or 'VISION_KEY'.");
            System.out.println("Set them before running this sample.");
            System.exit(1);
        }

        analysisFromFileUsingApiKeyAuth();

        analysisFromUrlUsingEntraIdAuth();
    }

    private static void analysisFromFileUsingApiKeyAuth() {
        // BEGIN: com.azure.ai.vision.imageanalysis.sync-client-api-key-auth
        //
        // Create a synchronous Image Analysis client with API key authentication.
        //
        ImageAnalysisClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();
        // END: com.azure.ai.vision.imageanalysis.sync-client-api-key-auth

        // BEGIN: com.azure.ai.vision.imageanalysis.sync-analysis-from-file
        //
        // Synchronous analysis of an image file (sample.jpg), using all visual features,
        // with all options set. You will likely need fewer visual features and only
        // some (or no) options set.
        //
        ImageAnalysisOptions options = new ImageAnalysisOptions()
            .setLanguage("en")
            .setGenderNeutralCaption(true)
            .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))
            .setModelVersion("latest");

        ImageAnalysisResult result = client.analyze(
            BinaryData.fromFile(new File("sample.jpg").toPath()),
            Arrays.asList(
                VisualFeatures.SMART_CROPS,
                VisualFeatures.CAPTION,
                VisualFeatures.DENSE_CAPTIONS,
                VisualFeatures.OBJECTS,
                VisualFeatures.PEOPLE,
                VisualFeatures.READ,
                VisualFeatures.TAGS),
            options);
        // END: com.azure.ai.vision.imageanalysis.sync-analysis-from-file

        printAnalysisResults(result);
    }

    private static void analysisFromUrlUsingEntraIdAuth() {
        // BEGIN: com.azure.ai.vision.imageanalysis.sync-client-entra-id-auth
        //
        // Create a synchronous Image Analysis client with Entra ID authentication.
        //
        ImageAnalysisClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.ai.vision.imageanalysis.sync-client-entra-id-auth

        // BEGIN: com.azure.ai.vision.imageanalysis.sync-analysis-from-url
        //
        // Synchronous analysis of an image file (https://aka.ms/azsdk/image-analysis/sample.jpg),
        // using all visual features, with all options set. You will likely need fewer visual features
        // and only some (or no) options set.
        //
        ImageAnalysisOptions options = new ImageAnalysisOptions()
            .setLanguage("en")
            .setGenderNeutralCaption(true)
            .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))
            .setModelVersion("latest");

        ImageAnalysisResult result = client.analyzeFromUrl(
            "https://aka.ms/azsdk/image-analysis/sample.jpg",
            Arrays.asList(
                VisualFeatures.SMART_CROPS,
                VisualFeatures.CAPTION,
                VisualFeatures.DENSE_CAPTIONS,
                VisualFeatures.OBJECTS,
                VisualFeatures.PEOPLE,
                VisualFeatures.READ,
                VisualFeatures.TAGS),
            options);
        // END: com.azure.ai.vision.imageanalysis.sync-analysis-from-url

        printAnalysisResults(result);
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
