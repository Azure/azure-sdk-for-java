// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.DetectedTag;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to extract content tags in an image file sample.jpg, using a synchronous client.
 *  Tags are supported for thousands of recognizable objects, living beings, scenery, and actions that appear in images.
 *
 *  Tags names are supported in multiple languages, the default being English. You can set the `language` argument when
 *  calling `analyze` to a 2-letter language code. See [Image Analysis supported languages](https://aka.ms/cv-languages).
 *
 *  The synchronous (blocking) `analyze` method call returns an `ImageAnalysisResult` object.
 *  A call to `getTags()` on this result will return a `TagsResult` object. It contains a list of `DetectedTag` 
 *  objects. Each has:
 *  - The tag name, for example: "indoor", "table".
 *  - A confidence score in the range [0, 1], with higher values indicating greater confidences in the tag.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleTagsImageFile {

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

        try {
            // Extract tags for an input image buffer. This is a synchronous (blocking) call.
            ImageAnalysisResult result = client.analyze(
                BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
                Arrays.asList(VisualFeatures.TAGS), // visualFeatures
                new ImageAnalysisOptions().setLanguage("en")); // options: See https://aka.ms/cv-languages for supported languages. Or null to use the default of "en" (English).

            printAnalysisResults(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Print analysis results to the console
    public static void printAnalysisResults(ImageAnalysisResult result) {

        System.out.println("Image analysis results:");
        System.out.println(" Tags:");
        for (DetectedTag tag : result.getTags().getValues()) {
            System.out.println("   \"" + tag.getName() + "\", Confidence "
                + String.format("%.4f", tag.getConfidence()));
        }
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
