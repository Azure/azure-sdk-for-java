// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.core.credential.KeyCredential;

// BEGIN: imports-caption-url-snippet
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import java.util.Arrays;
// END: imports-caption-url-snippet

/**
 *  This sample demonstrates how to generate a human-readable sentence that describes the content
 *  of a publicly accessible image URL, using a synchronous client.
 *
 *  By default the caption may contain gender terms such as "man", "woman", or "boy", "girl".
 *  You have the option to request gender-neutral terms such as "person" or "child" by setting
 *  `genderNeutralCaption` to `true` when calling `analyzeFromUrl`, as shown in this example.
 *
 *  The synchronous (blocking) `analyzeFromUrl` method call returns an `ImageAnalysisResult` object.
 *  A call to `getCaption()` on this result will return a `CaptionResult` object. It contains:
 *  - The text of the caption. Captions are only supported in English at the moment. 
 *  - A confidence score in the range [0, 1], with higher values indicating greater confidences in
 *    the caption.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleCaptionImageUrl {

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

        // Generate a caption for an input image buffer. This is a synchronous (blocking) call.
        // BEGIN: caption-url-snippet
        ImageAnalysisResult result = client.analyzeFromUrl(
            "https://aka.ms/azsdk/image-analysis/sample.jpg", // imageUrl: the URL of the image to analyze
            Arrays.asList(VisualFeatures.CAPTION), // visualFeatures
            new ImageAnalysisOptions().setGenderNeutralCaption(true)); // options:  Set to 'true' or 'false' (relevant for CAPTION or DENSE_CAPTIONS visual features)

        // Print analysis results to the console
        System.out.println("Image analysis results:");
        System.out.println(" Caption:");
        System.out.println("   \"" + result.getCaption().getText() + "\", Confidence "
            + String.format("%.4f", result.getCaption().getConfidence()));
        // END: caption-url-snippet
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
