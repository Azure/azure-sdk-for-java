// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Source code snippets from this file are embedded in Image Analysis SDK JavaDoc (API documentations).
import com.azure.ai.vision.imageanalysis.ImageAnalysisAsyncClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import reactor.core.publisher.Mono;

public class ImageAnalysisAsyncJavaDocCodeSnippets {

    private static String endpoint;
    private static String key;

    public static void main(String[] args) throws MalformedURLException {

        endpoint = System.getenv("VISION_ENDPOINT");
        key = System.getenv("VISION_KEY");

        if (endpoint == null || key == null) {
            System.out.println("Missing environment variable 'VISION_ENDPOINT' or 'VISION_KEY'.");
            System.out.println("Set them before running this sample.");
            System.exit(1);
        }

        analysisFromFile();

        analysisFromUrl();
    }

    private static void analysisFromFile() {
        // BEGIN: com.azure.ai.vision.imageanalysis.async-client
        //
        // Create an asynchronous Image Analysis client.
        //
        ImageAnalysisAsyncClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();
        // END: com.azure.ai.vision.imageanalysis.async-client

        // BEGIN: com.azure.ai.vision.imageanalysis.async-analysis-from-file
        //
        // Asynchronous analysis of an image file (sample.jpg), using all visual features,
        // with all options set. You will likely need fewer visual features and only
        // some (or no) options set.
        //
        ImageAnalysisOptions options = new ImageAnalysisOptions()
            .setLanguage("en")
            .setGenderNeutralCaption(true)
            .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))
            .setModelVersion("latest");

        Mono<ImageAnalysisResult> result = client.analyze(
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
        // END: com.azure.ai.vision.imageanalysis.async-analysis-from-file

        ImageAnalysisSyncJavaDocCodeSnippets.printAnalysisResults(result.block());
    }

    private static void analysisFromUrl() throws MalformedURLException {

        ImageAnalysisAsyncClient client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();

        // BEGIN: com.azure.ai.vision.imageanalysis.async-analysis-from-url
        //
        // Asynchronous analysis of an image file (https://aka.ms/azsdk/image-analysis/sample.jpg),
        // using all visual features, with all options set. You will likely need fewer visual features
        // and only some (or no) options set.
        //
        ImageAnalysisOptions options = new ImageAnalysisOptions()
            .setLanguage("en")
            .setGenderNeutralCaption(true)
            .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))
            .setModelVersion("latest");

        Mono<ImageAnalysisResult> result = client.analyze(
            new URL("https://aka.ms/azsdk/image-analysis/sample.jpg"),
            Arrays.asList(
                VisualFeatures.SMART_CROPS,
                VisualFeatures.CAPTION,
                VisualFeatures.DENSE_CAPTIONS,
                VisualFeatures.OBJECTS,
                VisualFeatures.PEOPLE,
                VisualFeatures.READ,
                VisualFeatures.TAGS),
            options);
        // END: com.azure.ai.vision.imageanalysis.async-analysis-from-url

        ImageAnalysisSyncJavaDocCodeSnippets.printAnalysisResults(result.block());
    }
}
