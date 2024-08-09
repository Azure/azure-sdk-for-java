// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.DocumentAnalysisFeature;
import com.azure.ai.documentintelligence.models.DocumentStyle;
import com.azure.ai.documentintelligence.models.FontStyle;
import com.azure.ai.documentintelligence.models.FontWeight;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.azure.ai.documentintelligence.AnalyzeAddOnFonts.printSpan;

/**
 * This async sample demonstrates how to extract all identified barcodes using the add-on 'STYLE_FONT' capability.
 * Add-on capabilities are available within all models except for the Business card model.
 * This sample uses Layout model to demonstrate.
 */
public class AnalyzeAddOnFontsAsync {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceAsyncClient client = new DocumentIntelligenceClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .buildAsyncClient();

        File document = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/"
            + "sample-forms/addOns/fonts_and_languages.png");

        PollerFlux<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutPoller =
            client.beginAnalyzeDocument("prebuilt-layout",
                null,
                null,
                null,
                Arrays.asList(DocumentAnalysisFeature.STYLE_FONT),
                null,
                null,
                null,
                new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(document.toPath())));

        Mono<AnalyzeResult> analyzeLayoutResultMono =
            analyzeLayoutPoller
                .last()
                .flatMap(pollResponse -> {
                    if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(pollResponse.getStatus())) {
                        System.out.println("Polling completed successfully");
                        return pollResponse.getFinalResult();
                    } else {
                        return Mono.error(
                            new RuntimeException(
                                "Polling completed unsuccessfully with status:" + pollResponse.getStatus()));
                    }
                });

        analyzeLayoutResultMono.subscribe(analyzeLayoutResult -> {
            // DocumentStyle has the following font related attributes:
            Map<String, List<DocumentStyle>> similarFontFamilies = new HashMap<>(); // e.g., 'Arial, sans-serif
            Map<FontStyle, List<DocumentStyle>> fontStyles = new HashMap<>(); // e.g, 'italic'
            Map<FontWeight, List<DocumentStyle>> fontWeights = new HashMap<>(); // e.g., 'bold'
            Map<String, List<DocumentStyle>> fontColors = new HashMap<>(); // in '#rrggbb' hexadecimal format
            Map<String, List<DocumentStyle>> fontBackgroundColors = new HashMap<>(); // in '#rrggbb' hexadecimal format

            // styles
            List<DocumentStyle> documentStyles = analyzeLayoutResult.getStyles();
            // Content
            String content = analyzeLayoutResult.getContent();


            boolean isDocumentContainsHandwritten = documentStyles.stream().anyMatch(documentStyle -> {
                Boolean handwritten = documentStyle.isHandwritten();
                return handwritten != null && handwritten;
            });

            if (isDocumentContainsHandwritten) {
                System.out.println("Document contains handwritten content");
            } else {
                System.out.println("Document does not contains handwritten content");
            }

            System.out.println("----Fonts styles detected in the document----");

            // Iterate over the styles and group them by their font attributes.
            documentStyles.forEach(documentStyle -> {
                String similarFontFamily = documentStyle.getSimilarFontFamily();
                if (!CoreUtils.isNullOrEmpty(similarFontFamily)) {
                    if (!similarFontFamilies.containsKey(similarFontFamily)) {
                        similarFontFamilies.put(similarFontFamily, new ArrayList<>());
                    }
                    similarFontFamilies.get(similarFontFamily).add(documentStyle);
                }

                FontStyle fontStyle = documentStyle.getFontStyle();
                if (fontStyle != null) {
                    if (!fontStyles.containsKey(fontStyle)) {
                        fontStyles.put(fontStyle, new ArrayList<>());
                    }
                    fontStyles.get(fontStyle).add(documentStyle);
                }

                FontWeight fontWeight = documentStyle.getFontWeight();
                if (fontWeight != null) {
                    if (!fontWeights.containsKey(fontWeight)) {
                        fontWeights.put(fontWeight, new ArrayList<>());
                    }
                    fontWeights.get(fontWeight).add(documentStyle);
                }

                String fontColor = documentStyle.getColor();
                if (!CoreUtils.isNullOrEmpty(fontColor)) {
                    if (!fontColors.containsKey(fontColor)) {
                        fontColors.put(fontColor, new ArrayList<>());
                    }
                    fontColors.get(fontColor).add(documentStyle);
                }

                String backgroundColor = documentStyle.getBackgroundColor();
                if (!CoreUtils.isNullOrEmpty(backgroundColor)) {
                    if (!fontBackgroundColors.containsKey(backgroundColor)) {
                        fontBackgroundColors.put(backgroundColor, new ArrayList<>());
                    }
                    fontBackgroundColors.get(backgroundColor).add(documentStyle);
                }
            });

            System.out.printf("Detected %d font families: %n", similarFontFamilies.size());
            similarFontFamilies.forEach((fontFamily, styles) -> {
                System.out.println("- Font family: " + fontFamily);
                printSpan(styles, content);
            });

            System.out.printf("Detected %d font styles: %n", fontStyles.size());
            fontStyles.forEach((fontStyle, styles) -> {
                System.out.println("- Font style: " + fontStyle);
                printSpan(styles, content);
            });

            System.out.printf("Detected %d font weights: %n", fontWeights.size());
            fontWeights.forEach((fontWeight, styles) -> {
                System.out.println("- Font weight: " + fontWeight);
                printSpan(styles, content);
            });

            System.out.printf("Detected %d font colors: %n", fontColors.size());
            fontColors.forEach((fontColor, styles) -> {
                System.out.println("- Font color: " + fontColor);
                printSpan(styles, content);
            });

            System.out.printf("Detected %d font background colors: %n", fontBackgroundColors.size());
            fontBackgroundColors.forEach((fontBackgroundColor, styles) -> {
                System.out.println("- Font background color: " + fontBackgroundColor);
                printSpan(styles, content);
            });
        });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
