// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

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
        SyncPoller<OperationResult, List<RecognizedForm>> recognizeFormPoller =
            client.beginRecognizeCustomFormsFromUrl(filePath, modelId, true, null);

        List<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

        System.out.println("--------RECOGNIZING FORM --------");
        for (int i = 0; i < recognizedForms.size(); i++) {
            final RecognizedForm recognizedForm = recognizedForms.get(i);
            System.out.printf("Form %s has type: %s%n", i, recognizedForm.getFormType());
            // each field is of type FormField
            // The value of the field can also be a FormField, or a list of FormFields
            // In our sample, it is not.
            recognizedForm.getFields().forEach((fieldText, fieldValue) -> System.out.printf("Field %s has value %s "
                    + "based on %s with a confidence score "
                    + "of %.2f.%n",
                fieldText, fieldValue.getFieldValue(), fieldValue.getValueText().getText(),
                fieldValue.getConfidence()));

            // Page Information
            final List<FormPage> pages = recognizedForm.getPages();
            for (int i1 = 0; i1 < pages.size(); i1++) {
                final FormPage formPage = pages.get(i1);
                System.out.printf("-------Recognizing Page %s of Form -------%n", i1);
                System.out.printf("Has width %f , angle %.2f, height %f %n", formPage.getWidth(),
                    formPage.getTextAngle(), formPage.getHeight());
                // Table information
                System.out.println("Recognized Tables: ");
                final List<FormTable> tables = formPage.getTables();
                for (int i2 = 0; i2 < tables.size(); i2++) {
                    final FormTable formTable = tables.get(i2);
                    System.out.printf("Table %d%n", i2);
                    formTable.getCells().forEach(formTableCell -> {
                        System.out.printf("Cell text %s has following words: %n", formTableCell.getText());
                        // textContent only exists if you set includeTextContent to True in your
                        // call to beginRecognizeCustomFormsFromUrl
                        // It is also a list of FormWords and FormLines, but in this example, we only deal with
                        // FormWords
                        formTableCell.getTextContent().forEach(formContent -> {
                            if (formContent instanceof FormWord) {
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
                }
            }
        }
    }
}
