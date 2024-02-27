// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.core.credential.KeyCredential;

// BEGIN: imports-ocr-file-snippet
import com.azure.ai.vision.imageanalysis.models.DetectedTextLine;
import com.azure.ai.vision.imageanalysis.models.DetectedTextWord;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;
// END: imports-ocr-file-snippet

/**
 *  This sample demonstrates how to extract printed or hand-written text for the image file sample.jpg
 *  using a synchronous client.
 *
 *  The synchronous (blocking) `analyze` method call returns an `ImageAnalysisResult` object.
 *  A call to `getRead()` on the result will return a `ReadResult` object. It includes a list of 
 *  `DetectedTextBlock` objects. Currently, the list will always contain one block, as the service does 
 *  not yet support grouping text lines into separate blocks. The `DetectedTextBlock` object contains a 
 *  list of `DetectedTextLine` object. Each one includes: 
 *  - The text content of the line.
 *  - A polygon coordinates in pixels, for a polygon surrounding the line of text in the image.
 *  - A list of `DetectedTextWord` objects.
 *  Each `DetectedTextWord` object contains:
 *  - The text content of the word.
 *  - A polygon coordinates in pixels, for a polygon surrounding the word in the image.
 *  - A confidence score in the range [0, 1], with higher values indicating greater confidences in
 *    the recognition of the word. 
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleOcrImageFile {

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

        // Extract text from an input image buffer. This is a synchronous (blocking) call.
        // BEGIN: ocr-file-snippet
        ImageAnalysisResult result = client.analyze(
            BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
            Arrays.asList(VisualFeatures.READ), // visualFeatures
            null); // options: There are no options for READ visual feature

        // Print analysis results to the console
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
        // END: ocr-file-snippet
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
