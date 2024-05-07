// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.DetectedPerson;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to detect people in the image file sample.jpg using a synchronous client.
 *
 *  The synchronous (blocking) `analyze` method returns an `ImageAnalysisResult` object.
 *  A call to `getPeople()` on the result will return a `PeopleResult` object. It contains a list 
 *  of `DetectedPerson` objects. Each one contains:
 *  - A confidence score in the range [0, 1], with higher values indicating greater confidences in
 *    the detection of a person. 
 *  - A `BoundingBox` coordinates in pixels, for a rectangle surrounding the person in the image.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SamplePeopleImageFile {

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
            // Detect people in an input image buffer. This is a synchronous (blocking) call.
            ImageAnalysisResult result = client.analyze(
                BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
                Arrays.asList(VisualFeatures.PEOPLE), // visualFeatures
                null); // options: There are no options for PEOPLE visual feature

            printAnalysisResults(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Print analysis results to the console
    public static void printAnalysisResults(ImageAnalysisResult result) {

        System.out.println("Image analysis results:");
        System.out.println(" People:");
        for (DetectedPerson person : result.getPeople().getValues()) {
            System.out.println("   Bounding box "
                + person.getBoundingBox() + ", Confidence " + String.format("%.4f", person.getConfidence()));
        }
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
