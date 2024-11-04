// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// BEGIN: imports-for-create-client-snippet
import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.core.credential.KeyCredential;
// END: imports-for-create-client-snippet

// BEGIN: imports-caption-file-snippet
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;
// END: imports-caption-file-snippet

/**
 *  This sample demonstrates how to generate a human-readable sentence that describes the content
 *  of the image file sample.jpg, using a synchronous client.
 *
 *  By default the caption may contain gender terms such as "man", "woman", or "boy", "girl".
 *  You have the option to request gender-neutral terms such as "person" or "child" by setting
 *  `genderNeutralCaption` to `true` when calling `analyze`, as shown in this example.
 *
 *  The synchronous (blocking) `analyze` method call returns an `ImageAnalysisResult` object.
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
public class SampleCaptionImageFile {

    public static void main(String[] args) {

        // BEGIN: create-client-snippet
        String endpoint = System.getenv("VISION_ENDPOINT");
        String key = System.getenv("VISION_KEY");

        if (endpoint == null || key == null) {
            System.out.println("Missing environment variable 'VISION_ENDPOINT' or 'VISION_KEY'.");
            System.out.println("Set them before running this sample.");
            System.exit(1);
        }

        // Create a synchronous client using API key authentication
        ImageAnalysisClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();
        // END: create-client-snippet

        // Generate a caption for an input image buffer. This is a synchronous (blocking) call.
        // BEGIN: caption-file-snippet
        ImageAnalysisResult result = client.analyze(
            BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
            Arrays.asList(VisualFeatures.CAPTION), // visualFeatures
            new ImageAnalysisOptions().setGenderNeutralCaption(true)); // options:  Set to 'true' or 'false' (relevant for CAPTION or DENSE_CAPTIONS visual features)

        // Print analysis results to the console
        System.out.println("Image analysis results:");
        System.out.println(" Caption:");
        System.out.println("   \"" + result.getCaption().getText() + "\", Confidence " 
            + String.format("%.4f", result.getCaption().getConfidence()));
        // END: caption-file-snippet
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
