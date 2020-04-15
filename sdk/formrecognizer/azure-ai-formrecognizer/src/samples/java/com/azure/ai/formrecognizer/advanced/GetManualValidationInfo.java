// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.advanced;

import com.azure.ai.formrecognizer.FormRecognizerAsyncClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.TextContentType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.IOException;

/*
 * Sample for recognizing fields from custom forms.
 */
public class GetManualValidationInfo {

    /**
     * Main method to invoke this demo to analyze custom forms to extract information.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("48c9ec5b1c444c899770946defc486c4"))
            .endpoint("https://javaformrecognizertestresource.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String modelId = "{unlabeled_model_id}";
        PollerFlux<OperationResult, IterableStream<RecognizedForm>> trainingPoller =
            client.beginRecognizeCustomFormsFromUrl("{file_source_url}",
                modelId, false, null);

        IterableStream<RecognizedForm> recognizedForms = trainingPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            }).block();

        System.out.println("--------RECOGNIZING FORM --------");
        recognizedForms.forEach(extractedForm -> {
            System.out.printf("Form has type: %s%n", extractedForm.getFormType());
            // each field is of type FormField
            //     The value of the field can also be a FormField, or a list of FormFields
            //     In our sample, it is not.
            extractedForm.getFields().forEach((fieldText, fieldValue) -> {
                System.out.printf("Field %s has value %s based on %s within bounding box, with a confidence score "
                        + "of %s.%n",
                    fieldText, fieldValue.getFieldValue(), fieldValue.getValueText().getText(),
                    fieldValue.getConfidence());
            });

            // Page Information
            extractedForm.getPages().forEach(formPage -> {
                System.out.printf("-------Recognizing Page %s of Form -------%n", 1);
                System.out.printf("Has width %s , angle %s, height %s %n", formPage.getWidth(),
                    formPage.getTextAngle(), formPage.getHeight());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables().forEach(formTable -> {
                    formTable.getCells().forEach(formTableCell -> {
                        System.out.printf("%s has following words: %n", formTableCell.getText());
                        // text_content only exists if you set include_text_content to True in your
                        // function call to recognize_custom_forms
                        // It is also a list of FormWords and FormLines, but in this example, we only deal with
                        // FormWords
                        formTableCell.getElements().forEach(formContent -> {
                            if (formContent.getTextContentType().equals(TextContentType.WORD)) {
                                FormWord formWordElement = (FormWord) (formContent);
                                StringBuilder str = new StringBuilder();
                                if (formWordElement.getBoundingBox() != null) {
                                    formWordElement.getBoundingBox().getPoints().forEach(point -> {
                                        str.append(String.format("[%s, %s]", point.getX(), point.getY()));
                                    });
                                }
                                System.out.printf("Word '%s' within bounding box %s with a confidence of %s.%n",
                                    formWordElement.getText(), str, formWordElement.getConfidence());
                            }
                        });
                    });
                    System.out.println();
                });
            });
        });
    }
}
