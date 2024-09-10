// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// BEGIN: imports-for-create-client-entra-id-snippet
import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
// END: imports-for-create-client-entra-id-snippet

import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.util.Arrays;

/**
 *  This sample demonstrates how to generate a human-readable sentence that describes the content
 *  of the image file sample.jpg, using a synchronous client authenticated with Entra ID.
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
 *  Set this environment variable before running the sample:
 *  VISION_ENDPOINT - Your endpoint URL, in the form https://your-resource-name.cognitiveservices.azure.com
 *  where `your-resource-name` is your unique Azure Computer Vision resource name.
 *
 *  You also need to set up your environment for Entra ID authentication. See the Prerequisites
 *  section of the package README.md file:
 *  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/vision/azure-ai-vision-imageanalysis#prerequisites
 */
public class SampleCaptionImageFileEntraIdAuth {

    public static void main(String[] args) {

        // BEGIN: create-client-entra-id-snippet
        String endpoint = System.getenv("VISION_ENDPOINT");

        if (endpoint == null) {
            System.out.println("Missing environment variable 'VISION_ENDPOINT'.");
            System.out.println("Set it before running this sample.");
            System.exit(1);
        }

        // Create a synchronous client using Entra ID authentication.
        ImageAnalysisClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: create-client-entra-id-snippet

        // Generate a caption for an input image buffer. This is a synchronous (blocking) call.
        ImageAnalysisResult result = client.analyze(
            BinaryData.fromFile(new File("sample.jpg").toPath()), // imageData: Image file loaded into memory as BinaryData
            Arrays.asList(VisualFeatures.CAPTION), // visualFeatures
            new ImageAnalysisOptions().setGenderNeutralCaption(true)); // options:  Set to 'true' or 'false' (relevant for CAPTION or DENSE_CAPTIONS visual features)

        // Print analysis results to the console
        System.out.println("Image analysis results:");
        System.out.println(" Caption:");
        System.out.println("   \"" + result.getCaption().getText() + "\", Confidence " 
            + String.format("%.4f", result.getCaption().getConfidence()));
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
