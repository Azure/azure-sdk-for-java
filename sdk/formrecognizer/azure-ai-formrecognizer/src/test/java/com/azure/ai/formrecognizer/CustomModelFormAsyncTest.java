// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_SOURCE_URL_ERROR_CODE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.getContentDetectionFileData;
import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Custom Model Async tests for testing with both client and service.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomModelFormAsyncTest extends FormRecognizerClientTestBase {
    private FormRecognizerAsyncClient client;
    static String[] labeledModelId = {null};
    static String[] multipageLabeledModelId = {null};
    static String[] multipageUnlabeledModelId = {null};
    static String[] unLabeledModelId = {null};

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @Order(1)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(getPrecomputedTrainedLabeledModelId(httpClient, serviceVersion),
                        toFluxByteBuffer(data),
                        dataLength,
                        new RecognizeCustomFormsOptions()
                            .setContentType(FormContentType.IMAGE_JPEG)
                            .setFieldElementsIncluded(true))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for a JPG content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithJpgContentType(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(
                        labeledModelId[0],
                        toFluxByteBuffer(data),
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for a blank PDF content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithBlankPdfContentType(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(labeledModelId[0],
                    toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(FormContentType.APPLICATION_PDF))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankCustomForm(syncPoller.getFinalResult(), 1, true);
        }, BLANK_PDF);
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id,
     * excluding element references.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataExcludeFieldElements(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(labeledModelId[0],
                    toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(FormContentType.APPLICATION_PDF))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithNullFormData(HttpClient httpClient,
                                                               FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            assertThrows(RuntimeException.class,
                () -> client.beginRecognizeCustomForms(labeledModelId[0],
                        null,
                        dataLength,
                        new RecognizeCustomFormsOptions()
                            .setContentType(FormContentType.APPLICATION_PDF)
                            .setFieldElementsIncluded(true))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller());
        }, INVOICE_6_PDF);
    }

    // Custom form - non-URL - labeled data

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            HttpResponseException httpResponseException
                = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeCustomFormsFromUrl(
                        labeledModelId[0],
                        invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller()
                    .getFinalResult());

            FormRecognizerErrorInformation errorInformation
                = (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(INVALID_SOURCE_URL_EXCEPTION_MESSAGE, errorInformation.getMessage());
        });
    }

    /**
     * Verifies content type will be auto detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                           FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        localFilePathRunner((filePath, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(labeledModelId[0],
                    toFluxByteBuffer(getContentDetectionFileData(filePath)),
                    dataLength,
                    new RecognizeCustomFormsOptions().setFieldElementsIncluded(true))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidSourceUrl(HttpClient httpClient,
                                                    FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();

        StepVerifier.create(client.beginRecognizeCustomFormsFromUrl(labeledModelId[0], INVALID_URL)
                .setPollInterval(durationTestMode))
            .expectErrorSatisfies(throwable -> {
                final HttpResponseException httpResponseException = (HttpResponseException) throwable;
                final FormRecognizerErrorInformation errorInformation =
                    (FormRecognizerErrorInformation) httpResponseException.getValue();
                assertEquals(INVALID_SOURCE_URL_ERROR_CODE, errorInformation.getErrorCode());
            })
            .verify(Duration.ofSeconds(30));
    }

    /**
     * Verifies custom form data for an URL document data with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        urlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomFormsFromUrl(
                        labeledModelId[0], fileUrl)
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data with labeled data and include element references.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        urlRunner(fileUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomFormsFromUrl(
                        labeledModelId[0],
                        fileUrl,
                        new RecognizeCustomFormsOptions().setFieldElementsIncluded(true))
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, true);
        }, CONTENT_FORM_JPG);
    }

    // Custom form recognition multipage

    /**
     * Verify custom form for a data stream of multi-page labeled data
     */
    @Order(2)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageLabeled(HttpClient httpClient,
                                                    FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {

            String modelId = getPreComputedMultipageLabeledModelId(httpClient, serviceVersion);

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(
                        modelId,
                        toFluxByteBuffer(data),
                        dataLength,
                        new RecognizeCustomFormsOptions().setFieldElementsIncluded(true)
                            .setContentType(FormContentType.APPLICATION_PDF))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultiPageDataLabeled(syncPoller.getFinalResult(), modelId);
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verify custom form for a URL of multi-page labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlMultiPageLabeled(HttpClient httpClient,
                                                       FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        urlRunner(fileUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomFormsFromUrl(
                        multipageLabeledModelId[0], fileUrl, new RecognizeCustomFormsOptions().setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultiPageDataLabeled(syncPoller.getFinalResult(), multipageLabeledModelId[0]);
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verify custom form for a data stream of multi-page unlabeled data
     */
    @Order(3)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageUnlabeled(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(
                        getPreComputedMultipageUnlabeledModelId(httpClient, serviceVersion),
                        toFluxByteBuffer(data),
                        dataLength,
                        new RecognizeCustomFormsOptions().setFieldElementsIncluded(true)
                            .setContentType(FormContentType.APPLICATION_PDF))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultiPageDataUnlabeled(syncPoller.getFinalResult());
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verify custom form for an URL of multi-page unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlMultiPageUnlabeled(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        testingContainerUrlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(multipageUnlabeledModelId[0], fileUrl)
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateMultiPageDataUnlabeled(syncPoller.getFinalResult());
        }, MULTIPAGE_INVOICE_PDF);
    }

    // Custom form - non-URL - unlabeled data

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @Order(4)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    getPrecomputedTrainedUnlabeledModelId(httpClient, serviceVersion),
                    toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeCustomFormsOptions().setFieldElementsIncluded(true)
                        .setContentType(FormContentType.APPLICATION_PDF))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateUnlabeledCustomForm(syncPoller.getFinalResult(), true, 1);
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid include field elements
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                     FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(unLabeledModelId[0],
                    toFluxByteBuffer(data),
                    dataLength,
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setFieldElementsIncluded(true))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateUnlabeledCustomForm(syncPoller.getFinalResult(), true, 1);
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies custom form data for a JPG content type with unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataWithJpgContentType(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(
                        unLabeledModelId[0], toFluxByteBuffer(data), dataLength,
                        new RecognizeCustomFormsOptions()
                            .setContentType(FormContentType.IMAGE_JPEG))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, false);
        }, CONTENT_FORM_JPG);

    }

    /**
     * Verifies custom form data for a blank PDF content type with unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataWithBlankPdfContentType(HttpClient httpClient,
                                                                        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomForms(
                        unLabeledModelId[0],
                        toFluxByteBuffer(data),
                        dataLength,
                        new RecognizeCustomFormsOptions().setFieldElementsIncluded(true)
                            .setContentType(FormContentType.APPLICATION_PDF))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    // Custom form - URL - unlabeled data

    /**
     * Verifies custom form data for an URL document data without labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlUnlabeledData(HttpClient httpClient,
                                                    FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();

        urlRunner(fileUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomFormsFromUrl(
                        unLabeledModelId[0], fileUrl)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, false);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data without labeled data and include element references.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlUnlabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        urlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                client.beginRecognizeCustomFormsFromUrl(
                        unLabeledModelId[0], fileUrl, new RecognizeCustomFormsOptions()
                            .setFieldElementsIncluded(true))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, false);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verify that custom form with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormDamagedPdf(HttpClient httpClient,
                                              FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildAsyncClient();
        damagedPdfDataRunner((data, dataLength) -> {

            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeCustomForms(unLabeledModelId[0],
                        toFluxByteBuffer(data),
                        dataLength,
                        new RecognizeCustomFormsOptions().setFieldElementsIncluded(true))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller().getFinalResult());

            FormRecognizerErrorInformation errorInformation =
                (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals("Invalid input file.", errorInformation.getMessage());
        });
    }

    private String getPreComputedMultipageUnlabeledModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        if (multipageUnlabeledModelId != null) {
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller
                    = getFormTrainingClientBuilder(httpClient, serviceVersion).buildAsyncClient()
                    .beginTraining(trainingFilesUrl, false)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
                trainingPoller.waitForCompletion();
                multipageUnlabeledModelId[0] = trainingPoller.getFinalResult().getModelId();
            });
        }
        assert multipageUnlabeledModelId != null;
        return multipageUnlabeledModelId[0];
    }

    private String getPreComputedMultipageLabeledModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        if (multipageLabeledModelId != null) {
            beginTrainingMultipageRunner((trainingFilesUrl) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller
                    = getFormTrainingClientBuilder(httpClient, serviceVersion).buildAsyncClient()
                    .beginTraining(trainingFilesUrl, true)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
                trainingPoller.waitForCompletion();
                multipageLabeledModelId[0] = trainingPoller.getFinalResult().getModelId();
            });
        }
        assert multipageLabeledModelId != null;
        return multipageLabeledModelId[0];
    }

    private String getPrecomputedTrainedLabeledModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller
                = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient()
                .beginTraining(trainingFilesUrl, useTrainingLabels)
                .setPollInterval(durationTestMode);
            labeledModelId[0] = trainingPoller.getFinalResult().getModelId();
        });
        assert labeledModelId != null;
        return labeledModelId[0];
    }

    private String getPrecomputedTrainedUnlabeledModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        if (unLabeledModelId != null) {
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                    getFormTrainingClientBuilder(httpClient, serviceVersion).buildAsyncClient()
                        .beginTraining(trainingFilesUrl, useTrainingLabels)
                        .setPollInterval(durationTestMode)
                        .getSyncPoller();
                trainingPoller.waitForCompletion();
                unLabeledModelId[0] = trainingPoller.getFinalResult().getModelId();
            });
        }
        assert unLabeledModelId != null;
        return unLabeledModelId[0];
    }
}
