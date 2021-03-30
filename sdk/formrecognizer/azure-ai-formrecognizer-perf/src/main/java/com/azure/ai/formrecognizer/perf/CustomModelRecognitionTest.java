// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf;

import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Performs custom model recognition operations.
 */
public class CustomModelRecognitionTest extends ServiceTest<PerfStressOptions> {

    private static final String URL_TEST_FILE_FORMAT = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/"
        + "master/sdk/formrecognizer/azure-ai-formrecognizer/src/test/resources/sample_files/Test/";
    private static final String FORM_JPG = "Form_1.jpg";

    /**
     * The CustomModelRecognitionTest class.
     *
     * @param options the configurable options for perf testing this class
     */
    public CustomModelRecognitionTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        List<RecognizedForm> recognizedForms =
            formrecognizerClient.beginRecognizeCustomFormsFromUrl(modelId, URL_TEST_FILE_FORMAT + FORM_JPG)
                .getFinalResult();
        recognizedForms.stream()
            .forEach(recognizedForm -> {
                assert recognizedForm.getFields() != null;
            });

    }

    @Override
    public Mono<Void> runAsync() {
        return formrecognizerAsyncClient
            .beginRecognizeCustomFormsFromUrl(modelId, URL_TEST_FILE_FORMAT + FORM_JPG)
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return Mono.empty();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });
    }
}
