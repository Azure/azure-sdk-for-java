// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.DetectedObject;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to detect physical objects in an image file sample.jpg, using a synchronous client.
 *
 *  The synchronous (blocking) `analyze` method call returns an `ImageAnalysisResult` object.
 *  A call to `getObjects()` on the result will return a `ObjectsResult` object. It contains a list 
 *  of `DetectedObject` objects. Each has:
 *  - The object name, for example: "chair", "laptop". 
 *  - A confidence score in the range [0, 1], with higher values indicating greater confidences in the detection.
 *  - A `BoundingBox` coordinates in pixels, for a rectangular surrounding the object in the image.
 *
 *  Object names are only supported in English at the moment.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleObjectsImageFile {

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
            // Detect objects in an input image buffer. This is a synchronous (blocking) call.
            ImageAnalysisResult result = client.analyze(
                BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
                Arrays.asList(VisualFeatures.OBJECTS), // visualFeatures
                null); // options: There are no options for OBJECTS visual feature

            printAnalysisResults(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Print analysis results to the console
    public static void printAnalysisResults(ImageAnalysisResult result) {

        System.out.println("Image analysis results:");
        System.out.println(" Objects:");
        for (DetectedObject detectedObject : result.getObjects().getValues()) {
            System.out.println("   \"" + detectedObject.getTags().get(0).getName() + "\", Bounding box "
                + detectedObject.getBoundingBox() + ", Confidence " + String.format("%.4f", detectedObject.getTags().get(0).getConfidence()));
        }
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
