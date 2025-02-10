// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.CropRegion;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to find representatives sub-regions of the image file sample.jpg,
 *  for thumbnail generation, with priority given to include faces. It uses an asynchronous client.
 *
 *  Thumbnails often need to have a certain aspect ratio, where aspect ratio is defined as the 
 *  width in pixels divided by the height in pixels. For example, 1.0 for a square image, or 1.77
 *  for a 16:9 widescreen image.
 *
 *  You can optionally request one or more aspect ratios by setting the `smartCropsAspectRatios` 
 *  argument in the call to `analyze`. Supported values are from 0.75 to 1.8 (inclusive).
 *  If you do not set this value, the service will return one result with an aspect ratio it sees
 *  fit between 0.5 and 2.0 (inclusive).
 *
 *  The synchronous (blocking ) `analyze` method call returns an `ImageAnalysisResult` object.
 *  A call to `getSmartCrops()` on the result will return a `SmartCropsResult` object. It contains 
 *  a list of `CropRegion` objects. Each one contains:
 *  - The aspect ratio of the region
 *  - A `BoundingBox` coordinates in pixels, defining the region in the image.
 *
 *  Set these two environment variables before running the sample:
 *  1) VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *                       where `your-resource-name` is your unique Azure Computer Vision resource name.
 *  2) VISION_KEY - Your Computer Vision key (a 32-character Hexadecimal number)
 */
public class SampleSmartCropsImageFile {

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
            // Smart cropping for an input image buffer. This is a synchronous (blocking) call.
            ImageAnalysisResult result = client.analyze(
                BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
                Arrays.asList(VisualFeatures.SMART_CROPS), // visualFeatures
                new ImageAnalysisOptions().setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))); // options: Set one or more aspect ratios, or null for default.

            printAnalysisResults(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Print analysis results to the console
    public static void printAnalysisResults(ImageAnalysisResult result) {

        System.out.println("Image analysis results:");
        System.out.println(" Crop Suggestions:");
        for (CropRegion cropRegion : result.getSmartCrops().getValues()) {
            System.out.println("   Aspect ratio "
                + cropRegion.getAspectRatio() + ": Bounding box " + cropRegion.getBoundingBox());
        }
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
