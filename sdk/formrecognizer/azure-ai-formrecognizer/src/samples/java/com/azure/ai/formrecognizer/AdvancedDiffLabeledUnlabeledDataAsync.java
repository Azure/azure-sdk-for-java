// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;

/**
 * Async sample to show the differences in output that arise when RecognizeCustomForms
 * is called with custom models trained with labeled and unlabeled data.
 * For this sample, you can use the training forms found in https://aka.ms/azsdk/formrecognizer/docs/trainingdocs for
 * creating your custom models.
 * The models used in this sample can be created using TrainModelsWithLabels.java and TrainModelsWithoutLabels.java.
 * <p>
 * See
 * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview#train-without-labels">here</a>
 * for service documentation on training with and without labels.
 * </p>
 */
public class AdvancedDiffLabeledUnlabeledDataAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        File analyzeFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/java/"
            + "sample-forms/forms/Form_1.jpg");
        byte[] fileContent = Files.readAllBytes(analyzeFile.toPath());

        PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> labeledCustomFormPoller =
            client.beginRecognizeCustomForms("{labeled_model_Id}",
                toFluxByteBuffer(new ByteArrayInputStream(fileContent)),
                analyzeFile.length(),
                    new RecognizeCustomFormsOptions()
                    .setContentType(FormContentType.IMAGE_JPEG)
                    .setFieldElementsIncluded(true)
                    .setPollInterval(Duration.ofSeconds(5)));

        PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> unlabeledCustomFormPoller =
            client.beginRecognizeCustomForms("{unlabeled_model_Id}",
                toFluxByteBuffer(new ByteArrayInputStream(fileContent)),
                analyzeFile.length(),
                    new RecognizeCustomFormsOptions()
                    .setContentType(FormContentType.IMAGE_JPEG)
                    .setFieldElementsIncluded(true)
                    .setPollInterval(Duration.ofSeconds(5)));

        Mono<List<RecognizedForm>> labeledDataResult = labeledCustomFormPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        Mono<List<RecognizedForm>> unlabeledDataResult = unlabeledCustomFormPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        System.out.println("--------Recognizing forms with labeled custom model--------");
        // With a form recognized by a model trained with labels, the `formField.getName()` key will be its label
        // given during training
        // `value` will contain the typed field value and `valueText` will contain information about the field value
        // `labelText` is not populated for a model trained with labels as this was the given label used to extract
        // the key
        labeledDataResult.subscribe(formsWithLabeledModel -> formsWithLabeledModel.forEach(labeledForm ->
            labeledForm.getFields().forEach((label, formField) -> {
                System.out.printf("Field %s has value data text %s based on %s within bounding box %s with "
                        + "a confidence score of %.2f.%n",
                    label, formField.getValueData().getText(), formField.getValueData().getText(),
                    formField.getValueData().getBoundingBox().toString(), formField.getConfidence());

                // Find the value of a specific labeled field.
                System.out.println("Value for a specific labeled field using the training-time label:");
                labeledForm.getFields().entrySet()
                    .stream()
                    .filter(formFieldEntry -> "Merchant".equals(formFieldEntry.getKey())) // filter by form field key
                    .findAny()
                    .ifPresent(formFieldEntry ->
                        System.out.printf("The Merchant name is: %s%n", formFieldEntry.getValue()
                            .getValue().asString()));
                    // @since 9
                    // .ifPresentOrElse(
                    //     formFieldEntry -> System.out.printf("The Merchant name is: %s%n", formFieldEntry.getValue()),
                    //     () -> System.out.println("'Merchant' training-time label does not exist. Substitute it with "
                    //         + "your own training-time label."));
            })));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("-----------------------------------------------------------");

        System.out.println("-------Recognizing forms with unlabeled custom model-------");
        unlabeledDataResult.subscribe(recognizedForms -> recognizedForms.forEach(unLabeledForm ->
            unLabeledForm.getFields().forEach((label, formField) -> {
                final StringBuilder boundingBoxStr = new StringBuilder();
                if (formField.getValueData().getBoundingBox() != null) {
                    formField.getValueData().getBoundingBox().getPoints().stream().map(point ->
                        String.format("[%.2f, %.2f]", point.getX(), point.getY())).forEach(boundingBoxStr::append);
                }

                if (formField.getLabelData() != null && formField.getLabelData().getBoundingBox() != null) {
                    formField.getLabelData().getBoundingBox().getPoints().stream().map(point ->
                        String.format("[%.2f, %.2f]", point.getX(), point.getY())).forEach(boundingBoxStr::append);

                    System.out.printf("Field %s has label %s within bounding box %s with a confidence score "
                            + "of %.2f.%n",
                        label, formField.getLabelData().getText(), "", formField.getConfidence());
                }

                System.out.printf("Field %s has value data text %s based on %s within bounding box %s with "
                        + "a confidence score of %.2f.%n",
                    label, formField.getValueData().getText(), formField.getValueData().getText(), boundingBoxStr,
                    formField.getConfidence());

                // Find the value of a specific unlabeled field. The specific key "Vendor Name:" provided in the
                // example will only be found if sample training forms used
                unLabeledForm.getFields().entrySet()
                    .stream()
                    //filter by label text
                    .filter(formFieldEntry -> "Vendor Name:".equals(formFieldEntry.getValue().getLabelData().getText()))
                    .findAny()
                    .ifPresent(formFieldEntry ->
                        System.out.printf("The Vendor name is: %s%n", formFieldEntry.getValue()
                            .getValue().asString()));
                    // @since 9
                    // .ifPresentOrElse(
                    //     formFieldEntry -> System.out.printf("The Vendor name is: %s%n", formFieldEntry.getValue()),
                    //     () -> System.out.println("'Vendor Name:' label text does not exist"));
            })));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
