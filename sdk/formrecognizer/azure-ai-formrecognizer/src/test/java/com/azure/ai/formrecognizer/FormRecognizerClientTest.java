// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerLanguage;
import com.azure.ai.formrecognizer.models.FormRecognizerLocale;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeBusinessCardsOptions;
import com.azure.ai.formrecognizer.models.RecognizeContentOptions;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizeIdentityDocumentOptions;
import com.azure.ai.formrecognizer.models.RecognizeInvoicesOptions;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.TextStyleName;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormSubmodel;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.azure.ai.formrecognizer.TestUtils.BLANK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_GERMAN_PDF;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.NON_EXIST_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.SELECTION_MARK_PDF;
import static com.azure.ai.formrecognizer.TestUtils.getContentDetectionFileData;
import static com.azure.ai.formrecognizer.TestUtils.validateExceptionSource;
import static com.azure.ai.formrecognizer.models.FormContentType.APPLICATION_PDF;
import static com.azure.ai.formrecognizer.models.FormContentType.IMAGE_JPEG;
import static com.azure.ai.formrecognizer.models.FormContentType.IMAGE_PNG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FormRecognizerClientTest extends FormRecognizerClientTestBase {

    private FormRecognizerClient client;

    private FormRecognizerClient getFormRecognizerClient(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion) {
        return getFormRecognizerClientBuilder(httpClient, serviceVersion).buildClient();
    }

    private FormTrainingClient getFormTrainingClient(HttpClient httpClient,
                                                     FormRecognizerServiceVersion serviceVersion) {
        return getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Receipt recognition

    // Receipt - non-URL

    /**
     * Verifies receipt data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceipts(data,
                    dataLength,
                    new RecognizeReceiptsOptions().setContentType(FormContentType.IMAGE_JPEG),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateReceiptData(syncPoller.getFinalResult(), false, FormContentType.IMAGE_JPEG);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies content type will be auto detected when using receipt API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceipts(getContentDetectionFileData(filePath),
                    dataLength,
                    new RecognizeReceiptsOptions(),
                    Context.NONE)
                    .setPollInterval(durationTestMode);

            syncPoller.waitForCompletion();
            validateReceiptData(syncPoller.getFinalResult(), false, FormContentType.IMAGE_JPEG);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data for a document using source as as input stream data and text content when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataIncludeFieldElements(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceipts(
                    data,
                    dataLength,
                    new RecognizeReceiptsOptions().setContentType(FormContentType.IMAGE_JPEG)
                        .setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateReceiptData(syncPoller.getFinalResult(), true, FormContentType.IMAGE_JPEG);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data from a document using PNG file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithPngFile(HttpClient httpClient,
                                                FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceipts(data,
                    dataLength,
                    new RecognizeReceiptsOptions().setContentType(FormContentType.IMAGE_PNG)
                        .setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateReceiptData(syncPoller.getFinalResult(), true, FormContentType.IMAGE_PNG);
        }, RECEIPT_CONTOSO_PNG);
    }

    /**
     * Verifies receipt data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptDataWithBlankPdf(HttpClient httpClient,
                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceipts(data,
                    dataLength,
                    new RecognizeReceiptsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceipts(
                    data,
                    dataLength,
                    new RecognizeReceiptsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        }, MULTIPAGE_RECEIPT_PDF);
    }

    /**
     * Verify that receipt recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromDamagedPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException
                = assertThrows(HttpResponseException.class,
                    () -> client.beginRecognizeReceipts(data,
                            dataLength,
                            new RecognizeReceiptsOptions().setContentType(APPLICATION_PDF),
                            Context.NONE)
                            .setPollInterval(durationTestMode)
                            .getFinalResult());
            FormRecognizerErrorInformation errorInformation
                = (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
        });
    }

    // Receipt - URL

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceiptsFromUrl(sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateReceiptData(syncPoller.getFinalResult(), false, FormContentType.IMAGE_JPEG);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize receipt from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException
                = assertThrows(HttpResponseException.class,
                    () -> client.beginRecognizeReceiptsFromUrl(sourceUrl).setPollInterval(durationTestMode));
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl) ->
            assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeReceiptsFromUrl(sourceUrl).setPollInterval(durationTestMode)));
    }

    /**
     * Verifies receipt data for a document using source as file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlIncludeFieldElements(HttpClient httpClient,
                                                            FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceiptsFromUrl(sourceUrl,
                    new RecognizeReceiptsOptions().setFieldElementsIncluded(true), Context.NONE)
                        .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateReceiptData(syncPoller.getFinalResult(), true, IMAGE_JPEG);
        }, RECEIPT_CONTOSO_JPG);
    }

    /**
     * Verifies receipt data for a document using source as PNG file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptSourceUrlWithPngFile(HttpClient httpClient,
                                                     FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceiptsFromUrl(sourceUrl,
                    new RecognizeReceiptsOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateReceiptData(syncPoller.getFinalResult(), true, IMAGE_PNG);
        }, RECEIPT_CONTOSO_PNG);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeReceiptFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(receiptUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeReceiptsFromUrl(receiptUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageReceiptData(syncPoller.getFinalResult());
        }, MULTIPAGE_RECEIPT_PDF);
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void receiptValidLocale(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);

        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> receiptPoller
                = client.beginRecognizeReceipts(
                    getContentDetectionFileData(filePath),
                    dataLength,
                    new RecognizeReceiptsOptions().setLocale(FormRecognizerLocale.EN_US),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            validateReceiptData(receiptPoller.getFinalResult(), false, FormContentType.IMAGE_JPEG);

        }, RECEIPT_CONTOSO_JPG);
    }

    // Content Recognition

    // Content - non-URL

    /**
     * Verifies layout/content data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContent(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(FormContentType.IMAGE_JPEG),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies content type will be auto detected when using content/layout API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(
                    getContentDetectionFileData(filePath),
                    dataLength,
                    new RecognizeContentOptions().setContentType(null),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, CONTENT_FORM_JPG);

    }

    /**
     * Verifies blank form file is still a valid file to process
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentResultWithBlankPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, BLANK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verify that content recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromDamagedPdf(HttpClient httpClient,
                                               FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeContent(data,
                        dataLength,
                        new RecognizeContentOptions().setContentType(APPLICATION_PDF),
                        Context.NONE)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithSelectionMarks(HttpClient httpClient,
                                                   FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(APPLICATION_PDF).setPages(Collections.singletonList("1")),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentData(formPages, true);
            assertEquals(1, formPages.size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithPages(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(APPLICATION_PDF).setPages(Arrays.asList("1", "2")),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentData(formPages, true);
            assertEquals(2, formPages.size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithPageRange(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(APPLICATION_PDF).setPages(Arrays.asList("1-2", "3")),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentData(formPages, true);
            assertEquals(3, formPages.size());
        }, MULTIPAGE_INVOICE_PDF);
    }

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentAppearance(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContent(data,
                    dataLength,
                    new RecognizeContentOptions().setContentType(FormContentType.IMAGE_JPEG),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            List<FormPage> formPages = syncPoller.getFinalResult();
            validateContentData(formPages, true);
            assertEquals(TextStyleName.OTHER,
                formPages.get(0).getLines().get(0).getAppearance().getStyleName());
        }, CONTENT_FORM_JPG);
    }

    // Content - URL

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContentFromUrl(sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, CONTENT_FORM_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize a content from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException
                = assertThrows(HttpResponseException.class,
                    () -> client.beginRecognizeContentFromUrl(sourceUrl).setPollInterval(durationTestMode));
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies layout data for a pdf url
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlWithPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContentFromUrl(sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, INVOICE_6_PDF);
    }

    /**
     * Verifies that an exception is thrown for invalid source url for recognizing content/layout information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl)
            -> assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeContentFromUrl(invalidSourceUrl).setPollInterval(durationTestMode)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner((formUrl) -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContentFromUrl(formUrl).setPollInterval(durationTestMode);

            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, MULTIPAGE_INVOICE_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentWithSelectionMarksFromUrl(HttpClient httpClient,
                                                          FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContentFromUrl(sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, SELECTION_MARK_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeGermanContentFromUrl(HttpClient httpClient,
                                              FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<FormPage>> syncPoller
                = client.beginRecognizeContentFromUrl(sourceUrl,
                    new RecognizeContentOptions().setLanguage(FormRecognizerLanguage.DE),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateContentData(syncPoller.getFinalResult(), true);
        }, CONTENT_GERMAN_PDF);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeContentIncorrectLanguageFromUrl(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        testingContainerUrlRunner(sourceUrl -> {
            HttpResponseException exception
                = assertThrows(HttpResponseException.class,
                    () -> client.beginRecognizeContentFromUrl(sourceUrl,
                            new RecognizeContentOptions().setLanguage(FormRecognizerLanguage.fromString("language")),
                            Context.NONE)
                            .setPollInterval(durationTestMode));

            assertEquals(((FormRecognizerErrorInformation) exception.getValue()).getErrorCode(),
                "NotSupportedLanguage");
        }, CONTENT_GERMAN_PDF);
    }

    // Custom form recognition
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeCustomFormLabeledDataWithSelectionMark(HttpClient httpClient,
                                                                FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) ->
            beginSelectionMarkTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
                SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller
                    = getFormTrainingClient(httpClient, serviceVersion)
                    .beginTraining(trainingFilesUrl, useTrainingLabels)
                        .setPollInterval(durationTestMode);
                trainingPoller.waitForCompletion();

                SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                    = client.beginRecognizeCustomForms(trainingPoller.getFinalResult().getModelId(),
                        data,
                        dataLength,
                        new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF).setFieldElementsIncluded(true),
                        Context.NONE)
                        .setPollInterval(durationTestMode);
                syncPoller.waitForCompletion();
                validateCustomFormWithSelectionMarks(syncPoller.getFinalResult(), true, 1);
            }), SELECTION_MARK_PDF);
    }
    // Business card recognition

    // Business card - non-URL

    /**
     * Verifies business card data for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCards(data,
                    dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(FormContentType.IMAGE_JPEG),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult(), false);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies content type will be auto detected when using business card API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCards(getContentDetectionFileData(filePath), dataLength)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult(), false);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data for a document using source as as input stream data and text content when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataIncludeFieldElements(HttpClient httpClient,
                                                              FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCards(data,
                    dataLength,
                    new RecognizeBusinessCardsOptions()
                        .setContentType(FormContentType.IMAGE_JPEG)
                        .setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult(), true);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data from a document using PNG file data as source and including text content details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataWithPngFile(HttpClient httpClient,
                                                     FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCards(data,
                    dataLength,
                    new RecognizeBusinessCardsOptions()
                        .setContentType(FormContentType.IMAGE_PNG)
                        .setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult(), true);
        }, BUSINESS_CARD_PNG);
    }

    /**
     * Verifies business card data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardDataWithBlankPdf(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCards(data,
                    dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that business card recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardFromDamagedPdf(HttpClient httpClient,
                                                    FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeBusinessCards(data,
                        dataLength,
                        new RecognizeBusinessCardsOptions().setContentType(APPLICATION_PDF),
                        Context.NONE)
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
            FormRecognizerErrorInformation errorInformation
                = (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
        });
    }

    /**
     * Verify business card recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeMultipageBusinessCard(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCards(data,
                    dataLength,
                    new RecognizeBusinessCardsOptions().setContentType(APPLICATION_PDF).setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageBusinessData(syncPoller.getFinalResult());
        }, MULTIPAGE_BUSINESS_CARD_PDF);
    }

    // business card - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCardsFromUrl(sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult(), false);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize business card from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                           FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeBusinessCardsFromUrl(sourceUrl).setPollInterval(durationTestMode));
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardInvalidSourceUrl(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl) -> assertThrows(HttpResponseException.class,
            () -> client.beginRecognizeBusinessCardsFromUrl(sourceUrl).setPollInterval(durationTestMode)));
    }

    /**
     * Verifies business card data for a document using source as file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardFromUrlIncludeFieldElements(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult(), true);
        }, BUSINESS_CARD_JPG);
    }

    /**
     * Verifies business card data for a document using source as PNG file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
                                                          FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBusinessCardData(syncPoller.getFinalResult(), true);
        }, BUSINESS_CARD_PNG);
    }

    /**
     * Verify business card recognition with multipage pdf url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeMultipageBusinessCardUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeBusinessCardsFromUrl(sourceUrl,
                    new RecognizeBusinessCardsOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageBusinessData(syncPoller.getFinalResult());
        }, MULTIPAGE_BUSINESS_CARD_PDF);
    }

    // Invoice recognition

    // Invoice - non-URL

    /**
     * Verifies invoice data recognition  for a document using source as input stream data.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoices(data,
                    dataLength,
                    new RecognizeInvoicesOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult(), false);
        }, INVOICE_PDF);
    }

    /**
     * Verifies content type will be auto detected when using invoice API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoices(getContentDetectionFileData(filePath),
                    dataLength,
                    new RecognizeInvoicesOptions(),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult(), false);
        }, INVOICE_PDF);
    }

    /**
     * Verifies invoice data for a document using source as as input stream data and text content when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceDataIncludeFieldElements(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoices(data,
                    dataLength,
                    new RecognizeInvoicesOptions().setContentType(APPLICATION_PDF).setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult(), true);
        }, INVOICE_PDF);
    }


    /**
     * Verifies invoice data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceDataWithBlankPdf(HttpClient httpClient,
                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoices(data,
                    dataLength,
                    new RecognizeInvoicesOptions().setContentType(APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateBlankPdfData(syncPoller.getFinalResult());
        }, BLANK_PDF);
    }

    /**
     * Verify that invoice recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceFromDamagedPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeInvoices(data,
                        dataLength,
                        new RecognizeInvoicesOptions().setContentType(APPLICATION_PDF),
                        Context.NONE)
                        .setPollInterval(durationTestMode).getFinalResult());
            FormRecognizerErrorInformation errorInformation
                = (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
        });
    }

    /**
     * Verify invoice data recognition with multipage pdf.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeMultipageInvoice(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        // confirm if pageResults should be returned for prebuilt model recognition
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoices(data,
                    dataLength,
                    new RecognizeInvoicesOptions().setContentType(APPLICATION_PDF).setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateMultipageInvoiceData(syncPoller.getFinalResult());
        }, MULTIPAGE_VENDOR_INVOICE_PDF);
    }

    // invoice - URL

    /**
     * Verifies invoice card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner((sourceUrl) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoicesFromUrl(sourceUrl)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult(), false);
        }, INVOICE_PDF);
    }

    /**
     * Verifies encoded blank url must stay same when sent to service for a document using invalid source url with
     * encoded blank space as input data to recognize invoice card from url API.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                      FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        encodedBlankSpaceSourceUrlRunner(sourceUrl -> {
            HttpResponseException errorResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeInvoicesFromUrl(sourceUrl).setPollInterval(durationTestMode));
            validateExceptionSource(errorResponseException);
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceInvalidSourceUrl(HttpClient httpClient,
                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((sourceUrl)
            -> assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeInvoicesFromUrl(sourceUrl).setPollInterval(durationTestMode)));
    }

    /**
     * Verifies invoice data for a document using source as file url and include form element references
     * when includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeInvoiceFromUrlIncludeFieldElements(HttpClient httpClient,
                                                            FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoicesFromUrl(sourceUrl,
                    new RecognizeInvoicesOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateInvoiceData(syncPoller.getFinalResult(), true);
        }, INVOICE_PDF);
    }

    /**
     * Verify locale parameter passed when specified by user.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void invoiceValidLocale(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeInvoices(
                    getContentDetectionFileData(filePath),
                    dataLength,
                    new RecognizeInvoicesOptions().setLocale(FormRecognizerLocale.EN_US),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            validateInvoiceData(syncPoller.getFinalResult(), false);
        }, INVOICE_PDF);
    }

    /**
     * Verify SDK returns empty object and array for null sub line items field.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void invoiceSubLineItemsNull(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            List<RecognizedForm> recognizedForms = client.beginRecognizeInvoices(
                    getContentDetectionFileData(filePath),
                    dataLength,
                    new RecognizeInvoicesOptions().setLocale(FormRecognizerLocale.EN_US),
                    Context.NONE)
                    .setPollInterval(durationTestMode)
                    .getFinalResult();

            RecognizedForm recognizedForm = recognizedForms.get(0);
            FormField itemFieldList = recognizedForm.getFields().get("Items").getValue().asList().get(0);
            Map<String, FormField> formFieldMap = itemFieldList.getValue().asMap();

            assertNull(formFieldMap);
            assertEquals(String.valueOf(1), itemFieldList.getValueData().getText());

        }, INVOICE_NO_SUB_LINE_PDF);
    }

    // Identity Document Recognition

    /**
     * Verifies license card data from a document using file data as source.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLicenseCardData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeIdentityDocuments(data,
                    dataLength,
                    new RecognizeIdentityDocumentOptions().setContentType(FormContentType.IMAGE_JPEG),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult(), false);
        }, LICENSE_CARD_JPG);
    }

    /**
     * Verifies content type will be auto detected when using custom form API with input stream data overload.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLicenseDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        localFilePathRunner((filePath, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeIdentityDocuments(
                    getContentDetectionFileData(filePath),
                    dataLength,
                    new RecognizeIdentityDocumentOptions(),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult(), false);
        }, LICENSE_CARD_JPG);
    }

    /**
     * Verifies identity document data from a document using file data as source and including element reference details.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLicenseDataIncludeFieldElements(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeIdentityDocuments(data,
                    dataLength,
                    new RecognizeIdentityDocumentOptions()
                        .setContentType(FormContentType.IMAGE_JPEG)
                        .setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult(), true);
        }, LICENSE_CARD_JPG);
    }

    /**
     * Verifies identity document data from a document using blank PDF.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeIDDocumentWithBlankPdf(HttpClient httpClient,
                                                FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        dataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeIdentityDocuments(
                    data,
                    dataLength,
                    new RecognizeIdentityDocumentOptions().setContentType(FormContentType.APPLICATION_PDF),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            assertEquals(0, syncPoller.getFinalResult().size());
        }, BLANK_PDF);
    }

    /**
     * Verify that identity document recognition with damaged PDF file.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeIDDocumentFromDamagedPdf(HttpClient httpClient,
                                                  FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        damagedPdfDataRunner((data, dataLength) -> {
            HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeIdentityDocuments(
                        data,
                        dataLength,
                        new RecognizeIdentityDocumentOptions().setContentType(FormContentType.APPLICATION_PDF),
                        Context.NONE)
                        .setPollInterval(durationTestMode)
                        .getFinalResult());
            FormRecognizerErrorInformation errorInformation
                = (FormRecognizerErrorInformation) httpResponseException.getValue();
            assertEquals(BAD_ARGUMENT_CODE, errorInformation.getErrorCode());
        });
    }

    // Identity Document - URL

    /**
     * Verifies business card data for a document using source as file url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeLicenseSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeIdentityDocumentsFromUrl(sourceUrl).setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult(), false);
        }, LICENSE_CARD_JPG);
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeIDDocumentInvalidSourceUrl(HttpClient httpClient,
                                                    FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        invalidSourceUrlRunner((invalidSourceUrl) -> {
            assertThrows(HttpResponseException.class,
                () -> client.beginRecognizeIdentityDocumentsFromUrl(invalidSourceUrl)
                    .setPollInterval(durationTestMode)
                    .getFinalResult());
        });
    }

    /**
     * Verifies license identity data for a document using source as file url and include content when
     * includeFieldElements is true.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void recognizeIDDocumentFromUrlIncludeFieldElements(HttpClient httpClient,
                                                               FormRecognizerServiceVersion serviceVersion) {
        client = getFormRecognizerClient(httpClient, serviceVersion);
        urlRunner(sourceUrl -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller
                = client.beginRecognizeIdentityDocumentsFromUrl(sourceUrl,
                    new RecognizeIdentityDocumentOptions().setFieldElementsIncluded(true),
                    Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            validateIdentityData(syncPoller.getFinalResult(), true);
        }, LICENSE_CARD_JPG);
    }
}
