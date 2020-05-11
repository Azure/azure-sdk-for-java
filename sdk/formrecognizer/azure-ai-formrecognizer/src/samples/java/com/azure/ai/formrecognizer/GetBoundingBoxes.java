// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.TextContentType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

/*
 * Sample to get detailed information to visualize the outlines of form content and fields,
 * which can be used for manual validation and drawing UI as part of an application.
 */
public class GetBoundingBoxes {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String modelId = "{model_Id}";
        String filePath = "{analyze_file_path}";
        SyncPoller<OperationResult, IterableStream<RecognizedForm>> trainingPoller =
            client.beginRecognizeCustomFormsFromUrl(filePath, modelId, true, null);

        IterableStream<RecognizedForm> recognizedForms = trainingPoller.getFinalResult();

        System.out.println("--------RECOGNIZING FORM --------");
        recognizedForms.forEach(recognizedForm -> {
            System.out.printf("Form has type: %s%n", recognizedForm.getFormType());
            // each field is of type FormField
            //     The value of the field can also be a FormField, or a list of FormFields
            //     In our sample, it is not.
            recognizedForm.getFields().forEach((fieldText, fieldValue) -> System.out.printf("Field %s has value %s "
                    + "based on %s with a confidence score "
                    + "of %.2f.%n",
                fieldText, fieldValue.getFieldValue(), fieldValue.getValueText().getText(),
                fieldValue.getConfidence()));

            // Page Information
            recognizedForm.getPages().forEach(formPage -> {
                System.out.printf("-------Recognizing Page %s of Form -------%n", 1);
                System.out.printf("Has width %d , angle %.2f, height %d %n", formPage.getWidth(),
                    formPage.getTextAngle(), formPage.getHeight());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables().forEach(formTable -> {
                    formTable.getCells().forEach(formTableCell -> {
                        System.out.printf("Cell text %s has following words: %n", formTableCell.getText());
                        // text_content only exists if you set include_text_content to True in your
                        // function call to recognize_custom_forms
                        // It is also a list of FormWords and FormLines, but in this example, we only deal with
                        // FormWords
                        formTableCell.getElements().forEach(formContent -> {
                            if (formContent.getTextContentType().equals(TextContentType.WORD)) {
                                FormWord formWordElement = (FormWord) (formContent);
                                StringBuilder boundingBoxStr = new StringBuilder();
                                if (formWordElement.getBoundingBox() != null) {
                                    formWordElement.getBoundingBox().getPoints().forEach(point -> {
                                        boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(),
                                            point.getY()));
                                    });
                                }
                                System.out.printf("Word '%s' within bounding box %s with a confidence of %.2f.%n",
                                    formWordElement.getText(), boundingBoxStr, formWordElement.getConfidence());
                            }
                        });
                    });
                    System.out.println();
                });
            });
        });
    }
}
