// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormSubmodel;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.NON_EXIST_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.SELECTION_MARK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.getContentDetectionFileData;
import static com.azure.ai.formrecognizer.TestUtils.validateExceptionSource;
import static com.azure.ai.formrecognizer.models.FormContentType.APPLICATION_PDF;
import static com.azure.ai.formrecognizer.models.FormContentType.IMAGE_JPEG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Custom Model Functional Tests for Form Recognizer client.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomModelFormTest extends FormRecognizerClientTestBase {
    private FormRecognizerClient client;
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
    public void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    getPrecomputedTrainedUnlabeledModelId(httpClient, serviceVersion),
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateUnlabeledCustomForm(syncPoller.getFinalResult(), false, 1);
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid include element references
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                     FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;

        dataRunner((data, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    unLabeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF).setFieldElementsIncluded(true),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateUnlabeledCustomForm(syncPoller.getFinalResult(), true, 1);
        }, INVOICE_6_PDF);
    }

    @Order(2)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageUnlabeled(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    getPreComputedMultipageUnlabeledModelId(httpClient, serviceVersion),
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultiPageDataUnlabeled(syncPoller.getFinalResult());
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies custom form data for a JPG content type with unlabeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUnlabeledDataWithJpgContentType(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    unLabeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG),
                    Context.NONE)
                .setPollInterval(durationTestMode);
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
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    unLabeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankCustomForm(syncPoller.getFinalResult(), 1, false);
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
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        urlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(
                    unLabeledModelId[0],
                    fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, false);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data without labeled data and include element references
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlUnlabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                        FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        urlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(
                    unLabeledModelId[0],
                    fileUrl,
                    new RecognizeCustomFormsOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, false);
        }, CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlMultiPageUnlabeled(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        testingContainerUrlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(
                    multipageUnlabeledModelId[0], fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultiPageDataUnlabeled(syncPoller.getFinalResult());
        }, MULTIPAGE_INVOICE_PDF);
    }

    // Custom form - URL - labeled data

    /**
     * Verifies that an exception is thrown for invalid training data source.
     */
    @Order(3)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidSourceUrl(HttpClient httpClient,
                                                    FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        assertThrows(
            HttpResponseException.class,
            () -> client.beginRecognizeCustomFormsFromUrl(getPrecomputedTrainedLabeledModelId(httpClient, serviceVersion), INVALID_URL)
                .getFinalResult()
        );
    }

    /**
     * Verifies custom form data for an URL document data with labeled data and include element references
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();

        urlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(
                    labeledModelId[0], fileUrl, new RecognizeCustomFormsOptions()
                        .setFieldElementsIncluded(true).setPollInterval(durationTestMode), Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for an URL document data with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        urlRunner(fileUrl -> {

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(
                    labeledModelId[0],
                    fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for a JPG content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithJpgContentType(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    labeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, true);
            }, CONTENT_FORM_JPG);
    }

    /**
     * Verify custom form for an URL of multi-page labeled data
     */
    @Order(4)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlMultiPageLabeled(HttpClient httpClient,
                                                       FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        urlRunner(fileUrl -> {
            String modelId = getPreComputedMultipageLabeledModelId(httpClient, serviceVersion);
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(modelId, fileUrl)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultiPageDataLabeled(syncPoller.getFinalResult(), modelId);
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    labeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(IMAGE_JPEG).setFieldElementsIncluded(true),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, true);
            }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies custom form data for a blank PDF content type with labeled data
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithBlankPdfContentType(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    labeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankCustomForm(syncPoller.getFinalResult(), 1, true);
        }, BLANK_PDF);
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id,
     * excluding field elements.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataExcludeFieldElements(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
    dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    labeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateJpegCustomForm(syncPoller.getFinalResult(), false, 1, true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies an exception thrown for a document using null form data value.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithNullFormData(HttpClient httpClient,
                                                               FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        dataRunner((data, dataLength) -> {
            assertThrows(RuntimeException.class,
                () -> client.beginRecognizeCustomForms(labeledModelId[0],
                        (InputStream) null,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF)
                            .setFieldElementsIncluded(true),
                        Context.NONE)
                    .setPollInterval(durationTestMode));
        }, INVOICE_6_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormInvalidStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        invalidSourceUrlRunner((invalidSourceUrl) -> {
                HttpResponseException httpResponseException
                    = assertThrows(HttpResponseException.class,
                    () -> client.beginRecognizeCustomFormsFromUrl(
                            labeledModelId[0],
                            invalidSourceUrl)
                        .setPollInterval(durationTestMode)
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
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        localFilePathRunner((filePath, dataLength) -> beginTrainingLabeledRunner(
            (trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                    = client.beginRecognizeCustomForms(labeledModelId[0],
                        getContentDetectionFileData(filePath),
                        dataLength,
                        new RecognizeCustomFormsOptions().setFieldElementsIncluded(true),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                validateJpegCustomForm(syncPoller.getFinalResult(), true, 1, true);
            }), CONTENT_FORM_JPG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormMultiPageLabeled(HttpClient httpClient,
                                                    FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        dataRunner((data, dataLength) ->  {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(
                    multipageLabeledModelId[0],
                    data,
                    dataLength,
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultiPageDataLabeled(syncPoller.getFinalResult(), multipageLabeledModelId[0]);
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with \
     * encoded blank space as input data to recognize a custom form from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                         FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeCustomFormsFromUrl(NON_EXIST_MODEL_ID, sourceUrl));
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verify that custom form with invalid model id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlNonExistModelId(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        urlRunner(fileUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeCustomFormsFromUrl(NON_EXIST_MODEL_ID, fileUrl));
            FormRecognizerErrorInformation errorInformation
                = (FormRecognizerErrorInformation) errorResponseException.getValue();
            assertEquals(INVALID_MODEL_ID_ERROR_CODE, errorInformation.getErrorCode());
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verify that custom form with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormDamagedPdf(HttpClient httpClient,
                                              FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();;
        damagedPdfDataRunner((data, dataLength) -> {

            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeCustomForms(unLabeledModelId[0],
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF),
                        Context.NONE)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());

            FormRecognizerErrorInformation errorInformation
                = (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals("Invalid input file.", errorInformation.getMessage());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormUrlLabeledDataWithSelectionMark(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
        urlRunner(fileUrl -> beginSelectionMarkTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {

            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller
                = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient()
                .beginTraining(trainingFilesUrl, useTrainingLabels)
                .setPollInterval(durationTestMode);
            trainingPoller.waitForCompletion();

            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomFormsFromUrl(trainingPoller.getFinalResult().getModelId(),
                    fileUrl,
                    new RecognizeCustomFormsOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateCustomFormWithSelectionMarks(syncPoller.getFinalResult(), true, 1);
        }), SELECTION_MARK_PDF);
    }

    /**
     * Verifies recognized form type when labeled model used for recognition and model name is provided by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void checkRecognizeFormTypeLabeledWithModelName(
        HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        final FormTrainingClient formTrainingClient = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();
        dataRunner((data, dataLength) -> {
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller
                    = formTrainingClient.beginTraining(trainingFilesUrl, useTrainingLabels,
                        new TrainingOptions().setModelName("model1"),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                CustomFormModel createdModel = syncPoller.getFinalResult();

                FormRecognizerClient formRecognizerClient = formTrainingClient
                    .getFormRecognizerClient();
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller1
                    = formRecognizerClient.beginRecognizeCustomForms(
                        createdModel.getModelId(),
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions()
                            .setContentType(FormContentType.IMAGE_JPEG),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller1.waitForCompletion();
                final RecognizedForm recognizedForm = syncPoller1.getFinalResult().stream().findFirst().get();
                assertEquals("custom:model1", recognizedForm.getFormType());
                assertNotNull(recognizedForm.getFormTypeConfidence());

                // check formtype set on submodel
                final CustomFormSubmodel submodel = createdModel.getSubmodels().get(0);
                assertEquals("custom:model1", submodel.getFormType());
                formTrainingClient.deleteModel(createdModel.getModelId());
            });
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies recognized form type when labeled model used for recognition and model name is not provided by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void checkRecognizedFormTypeLabeledModel(
        HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        final FormTrainingClient formTrainingClient = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller
                    = formTrainingClient.beginTraining(trainingFilesUrl, useTrainingLabels)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                CustomFormModel createdModel = syncPoller.getFinalResult();

                FormRecognizerClient formRecognizerClient = formTrainingClient
                    .getFormRecognizerClient();
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller1
                    = formRecognizerClient.beginRecognizeCustomForms(
                        createdModel.getModelId(),
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller1.waitForCompletion();
                final RecognizedForm recognizedForm = syncPoller1.getFinalResult().stream().findFirst().get();
                assertEquals("custom:" + createdModel.getModelId(), recognizedForm.getFormType());
                assertNotNull(recognizedForm.getFormTypeConfidence());

                // check formtype set on submodel
                final CustomFormSubmodel submodel = createdModel.getSubmodels().get(0);
                assertEquals("custom:" + createdModel.getModelId(), submodel.getFormType());
                formTrainingClient.deleteModel(createdModel.getModelId());
            });
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies recognized form type when unlabeled model used for recognition and model name is not provided by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void checkRecognizedFormTypeUnlabeledModel(
        HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        final FormTrainingClient formTrainingClient = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller
                    = formTrainingClient.beginTraining(trainingFilesUrl, useTrainingLabels)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                CustomFormModel createdModel = syncPoller.getFinalResult();

                FormRecognizerClient formRecognizerClient = formTrainingClient
                    .getFormRecognizerClient();
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller1
                    = formRecognizerClient.beginRecognizeCustomForms(
                        createdModel.getModelId(),
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller1.waitForCompletion();
                final RecognizedForm recognizedForm = syncPoller1.getFinalResult().stream().findFirst().get();
                assertEquals("form-0", recognizedForm.getFormType());

                // check formtype set on submodel
                final CustomFormSubmodel submodel = createdModel.getSubmodels().get(0);
                assertEquals("form-0", submodel.getFormType());
                formTrainingClient.deleteModel(createdModel.getModelId());
            });
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies recognized form type when unlabeled model used for recognition and model name is provided by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void checkRecognizedFormTypeUnlabeledModelWithModelName(
        HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        final FormTrainingClient formTrainingClient = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {
            beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller
                    = formTrainingClient.beginTraining(trainingFilesUrl,
                        useTrainingLabels,
                        new TrainingOptions().setModelName("model1"),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                CustomFormModel createdModel = syncPoller.getFinalResult();

                FormRecognizerClient formRecognizerClient = formTrainingClient
                    .getFormRecognizerClient();
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller1
                    = formRecognizerClient.beginRecognizeCustomForms(
                        createdModel.getModelId(),
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller1.waitForCompletion();
                final RecognizedForm recognizedForm = syncPoller1.getFinalResult().stream().findFirst().get();
                assertEquals("form-0", recognizedForm.getFormType());

                // check formtype set on submodel
                final CustomFormSubmodel submodel = createdModel.getSubmodels().get(0);
                assertEquals("form-0", submodel.getFormType());

                formTrainingClient.deleteModel(createdModel.getModelId());
            });
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies recognized form type when using composed model for recognition when display name is not provided by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void checkRecognizeFormTypeComposedModel(
        HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        final FormTrainingClient formTrainingClient = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller
                    = formTrainingClient.beginTraining(trainingFilesUrl,
                    useTrainingLabels).setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                CustomFormModel createdModel = syncPoller.getFinalResult();

                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller1
                    = formTrainingClient.beginTraining(trainingFilesUrl, useTrainingLabels)
                    .setPollInterval(durationTestMode);
                syncPoller1.waitForCompletion();
                CustomFormModel createdModel1 = syncPoller1.getFinalResult();

                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller2
                    = formTrainingClient.beginCreateComposedModel(Arrays.asList(createdModel.getModelId(),
                    createdModel1.getModelId())).setPollInterval(durationTestMode);
                syncPoller2.waitForCompletion();
                CustomFormModel composedModel = syncPoller2.getFinalResult();

                FormRecognizerClient formRecognizerClient = formTrainingClient
                    .getFormRecognizerClient();
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller3
                    = formRecognizerClient.beginRecognizeCustomForms(
                        composedModel.getModelId(),
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller3.waitForCompletion();

                final RecognizedForm recognizedForm = syncPoller3.getFinalResult().stream().findFirst().get();
                if (recognizedForm.getFormType().equals("custom:" + createdModel1.getModelId())
                    || recognizedForm.getFormType().equals("custom:" + createdModel.getModelId())) {
                    assertTrue(true);
                } else {
                    fail();
                }
                assertNotNull(recognizedForm.getFormTypeConfidence());

                // check formtype set on submodel
                composedModel.getSubmodels()
                    .forEach(customFormSubmodel -> {
                        if (createdModel.getModelId().equals(customFormSubmodel.getModelId())) {
                            assertEquals("custom:" + createdModel.getModelId(), customFormSubmodel.getFormType());
                        } else {
                            assertEquals("custom:" + createdModel1.getModelId(), customFormSubmodel.getFormType());
                        }
                    });

                formTrainingClient.deleteModel(createdModel.getModelId());
                formTrainingClient.deleteModel(createdModel1.getModelId());
                formTrainingClient.deleteModel(composedModel.getModelId());
            });
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies recognized form type when using composed model for recognition when model name is provided by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void checkRecognizeFormTypeComposedModelWithModelName(
        HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        final FormTrainingClient formTrainingClient = getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();;
        dataRunner((data, dataLength) -> {
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller
                    = formTrainingClient.beginTraining(trainingFilesUrl, useTrainingLabels,
                        new TrainingOptions().setModelName("model1"),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                CustomFormModel createdModel1 = syncPoller.getFinalResult();

                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller1
                    = formTrainingClient.beginTraining(trainingFilesUrl,
                        useTrainingLabels,
                        new TrainingOptions().setModelName("model2"),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller1.waitForCompletion();
                CustomFormModel createdModel2 = syncPoller1.getFinalResult();

                SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller2
                    = formTrainingClient.beginCreateComposedModel(
                        Arrays.asList(createdModel1.getModelId(), createdModel2.getModelId()),
                        new CreateComposedModelOptions().setModelName("composedModelName"),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller2.waitForCompletion();
                CustomFormModel composedModel = syncPoller2.getFinalResult();

                FormRecognizerClient formRecognizerClient = formTrainingClient.getFormRecognizerClient();
                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller3
                    = formRecognizerClient.beginRecognizeCustomForms(
                        composedModel.getModelId(),
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(FormContentType.IMAGE_JPEG),
                        Context.NONE)
                    .setPollInterval(durationTestMode);
                syncPoller3.waitForCompletion();

                final RecognizedForm recognizedForm = syncPoller3.getFinalResult().stream().findFirst().get();
                String expectedFormType1 = "composedModelName:model1";
                String expectedFormType2 = "composedModelName:model2";
                assertTrue(expectedFormType1.equals(recognizedForm.getFormType())
                    || expectedFormType2.equals(recognizedForm.getFormType()));

                assertNotNull(recognizedForm.getFormTypeConfidence());

                formTrainingClient.deleteModel(createdModel1.getModelId());
                formTrainingClient.deleteModel(createdModel2.getModelId());
                formTrainingClient.deleteModel(composedModel.getModelId());
            });
        }, CONTENT_FORM_JPG);
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
        if (labeledModelId != null) {
            beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller
                    = getFormTrainingClientBuilder(httpClient, serviceVersion).buildAsyncClient()
                    .beginTraining(trainingFilesUrl, useTrainingLabels)
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
                trainingPoller.waitForCompletion();
                labeledModelId[0] = trainingPoller.getFinalResult().getModelId();
            });
        }
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
