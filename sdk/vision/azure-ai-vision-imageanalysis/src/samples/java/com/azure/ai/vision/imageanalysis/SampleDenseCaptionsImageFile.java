// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.DenseCaption;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to generate up to 10 human-readable sentences (captions) that describe
 *  the content of different sub-regions of the image file sample.jpg. The first caption returned
 *  always represents the whole image. The sample uses the synchronous client.
 *
 *  By default the captions may contain gender terms such as "man", "woman", or "boy", "girl".
 *  You have the option to request gender-neutral terms such as "person" or "child" by setting
 *  `genderNeutralCaption` to `true` when calling `analyze`, as shown in this example.
 *
 *  The synchronous (blocking) `analyze` method call returns an `ImageAnalysisResult` object.
 *  A call to `getDenseCaptions()` on this result will return a `DenseCaptionsResult` object, which
 *  includes a list of up to 10 `DenseCaption` objects. Each one of them contains:
 *  - The text of the caption. Captions are only supported in English at the moment. 
 *  - A confidence score in the range [0, 1], with higher values indicating greater confidences in
 *    the caption.
 *  - A `BoundingBox` coordinates in pixels, for a rectangular marking the area in the image associated
 *    with this caption.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleDenseCaptionsImageFile {

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
            // Generate several captions for different regions of the input image buffer. This is a synchronous (blocking) call.
            ImageAnalysisResult result = client.analyze(
                BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
                Arrays.asList(VisualFeatures.DENSE_CAPTIONS), // visualFeatures
                new ImageAnalysisOptions().setGenderNeutralCaption(true)); // options:  Set to 'true' or 'false' (relevant for CAPTION or DENSE_CAPTIONS visual features)

            printAnalysisResults(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Print analysis results to the console
    public static void printAnalysisResults(ImageAnalysisResult result) {

        System.out.println("Image analysis results:");
        System.out.println(" Dense Captions:");
        for (DenseCaption denseCaption : result.getDenseCaptions().getValues()) {
            System.out.println("   \"" + denseCaption.getText() + "\", Bounding box "
                + denseCaption.getBoundingBox() + ", Confidence " + String.format("%.4f", denseCaption.getConfidence()));
        }
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
