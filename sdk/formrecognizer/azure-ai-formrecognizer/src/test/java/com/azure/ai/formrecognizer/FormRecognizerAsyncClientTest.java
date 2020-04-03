// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.formrecognizer.TestUtils.CUSTOM_FORM_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_FILE_LENGTH;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_URL;
import static com.azure.ai.formrecognizer.TestUtils.VALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedFormPages;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedReceipts;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedRecognizedForms;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedRecognizedLabeledForms;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedUSReceipt;
import static com.azure.ai.formrecognizer.TestUtils.getFileBufferData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FormRecognizerAsyncClientTest extends FormRecognizerClientTestBase {

    private FormRecognizerAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildAsyncClient());
    }

    /**
     * Verifies receipt data for a document using source as file url.
     */
    @Test
    void extractReceiptSourceUrl() {
        receiptSourceUrlRunner((sourceUrl) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data for a document using source as file url and include content when includeTextDetails is true.
     */
    @Test
    void extractReceiptSourceUrlTextDetails() {
        receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(true, getExpectedReceipts(includeTextDetails), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data from a document using file data as source.
     */
    @Test
    void extractReceiptData() {
        receiptDataRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
                client.beginRecognizeReceipts(getFileBufferData(data), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG, false,
                    null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(false), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies receipt data from a document using file data as source and including text content details.
     */
    @Test
    void extractReceiptDataTextDetails() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller
                = client.beginRecognizeReceipts(getFileBufferData(data), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG, includeTextDetails,
                null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateReceiptResult(false, getExpectedReceipts(true),
                syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for invalid source url.
     */
    @Test
    void extractReceiptInvalidSourceUrl() {
        invalidSourceUrlRunner((sourceUrl) -> assertThrows(ErrorResponseException.class, () ->
            client.beginRecognizeReceiptsFromUrl(sourceUrl).getSyncPoller()));
    }

    /**
     * Verifies receipt data is correctly transformed to USReceipt type.
     */
    @Test
    void extractReceiptAsUSReceipt() {
        receiptDataRunnerTextDetails((data, includeTextDetails) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller
                = client.beginRecognizeReceipts(getFileBufferData(data), RECEIPT_FILE_LENGTH, FormContentType.IMAGE_JPEG, includeTextDetails,
                null).getSyncPoller();
            syncPoller.waitForCompletion();
            syncPoller.getFinalResult().forEach(recognizedReceipt -> validateUSReceipt(getExpectedUSReceipt(), ReceiptExtensions.asUSReceipt(recognizedReceipt), includeTextDetails));
        });
    }

    /**
     * Verifies layout data for a document using source as input stream data.
     */
    @Test
    void extractLayoutValidSourceUrl() {
        layoutValidSourceUrlRunner((data) -> {
            SyncPoller<OperationResult, IterableStream<FormPage>> syncPoller
                = client.beginRecognizeContent(getFileBufferData(data),
                FormContentType.IMAGE_JPEG, LAYOUT_FILE_LENGTH, null).getSyncPoller();
            syncPoller.waitForCompletion();
            validateLayoutResult(getExpectedFormPages(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @Test
    void extractLayoutInValidSourceUrl() {
        invalidSourceUrlRunner((invalidSourceUrl) -> assertThrows(ErrorResponseException.class, () ->
            client.beginRecognizeContentFromUrl(invalidSourceUrl).getSyncPoller()));
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid unlabeled model Id.
     */
    @Test
    void extractCustomFormValidSourceUrl() {
        customFormValidSourceUrlRunner((data, validModelId) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(getFileBufferData(data), validModelId,
                CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF).getSyncPoller();
            syncPoller.waitForCompletion();
            validateRecognizedFormResult(getExpectedRecognizedForms(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies custom form data for a document using source as input stream data and valid labeled model Id.
     */
    @Test
    void extractCustomFormLabeledData() {
        customFormLabeledDataRunner((data, validModelId) -> {
            SyncPoller<OperationResult, IterableStream<RecognizedForm>> syncPoller
                = client.beginRecognizeCustomForms(getFileBufferData(data), validModelId,
                CUSTOM_FORM_FILE_LENGTH, FormContentType.APPLICATION_PDF, true, null)
                .getSyncPoller();
            syncPoller.waitForCompletion();
            validateRecognizedFormResult(getExpectedRecognizedLabeledForms(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @Test
    void extractCustomFormInValidSourceUrl() {
        ErrorResponseException httpResponseException = assertThrows(
            ErrorResponseException.class,
            () -> client.beginRecognizeCustomFormsFromUrl(INVALID_URL, VALID_MODEL_ID).getSyncPoller().getFinalResult());
        assertEquals(httpResponseException.getMessage(), (INVALID_SOURCE_URL_ERROR));
    }
}
//
// package com.azure.ai.formrecognizer;
//
// import com.azure.ai.formrecognizer.models.ExtractedReceipt;
// import com.azure.ai.formrecognizer.models.FormContentType;
// import com.azure.ai.formrecognizer.models.OperationResult;
// import com.azure.core.exception.HttpResponseException;
// import com.azure.core.util.IterableStream;
// import com.azure.core.util.polling.SyncPoller;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import reactor.test.StepVerifier;
//
// import java.time.Duration;
//
// import static com.azure.ai.formrecognizer.TestUtils.FILE_LENGTH;
// import static com.azure.ai.formrecognizer.TestUtils.getExtractedReceipts;
// import static com.azure.ai.formrecognizer.TestUtils.getReceiptFileBufferData;
// import static org.junit.jupiter.api.Assertions.assertThrows;
//
// public class FormRecognizerAsyncClientTest extends FormRecognizerClientTestBase {
//
//     private FormRecognizerAsyncClient client;
//
//     @BeforeAll
//     static void beforeAll() {
//         StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
//     }
//
//     @AfterAll
//     static void afterAll() {
//         StepVerifier.resetDefaultTimeout();
//     }
//
//     @Override
//     protected void beforeTest() {
//         client = clientSetup(httpPipeline -> new FormRecognizerClientBuilder()
//             .endpoint(getEndpoint())
//             .pipeline(httpPipeline)
//             .buildAsyncClient());
//     }
//
//     // Extract receipts
//     @Test
//     void extractReceiptSourceUrl() {
//         receiptSourceUrlRunner((sourceUrl) -> {
//             SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
//                 client.beginExtractReceiptsFromUrl(sourceUrl, ).getSyncPoller();
//             syncPoller.waitForCompletion();
//             validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
//         });
//     }
//
//     @Test
//     void extractReceiptSourceUrlTextDetails() {
//         receiptSourceUrlRunnerTextDetails((sourceUrl, includeTextDetails) -> {
//             SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
//                 client.beginExtractReceiptsFromUrl(sourceUrl, , null, includeTextDetails).getSyncPoller();
//             syncPoller.waitForCompletion();
//             validateReceiptResult(true, getExtractedReceipts(), syncPoller.getFinalResult());
//         });
//     }
//
//     @Test
//     void extractReceiptData() {
//         receiptDataRunner((data) -> {
//             SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
//                 client.beginExtractReceipts(getReceiptFileBufferData(), , false,
//                     FormContentType.IMAGE_JPEG, null, FILE_LENGTH).getSyncPoller();
//             syncPoller.waitForCompletion();
//             validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
//         });
//     }
//
//     @Test
//     void extractReceiptDataTextDetails() {
//         receiptDataRunnerTextDetails((data, includeTextDetails) -> {
//             SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller
//                 = client.beginExtractReceipts(getReceiptFileBufferData(), , includeTextDetails,
//                 FormContentType.IMAGE_JPEG, null, FILE_LENGTH).getSyncPoller();
//             syncPoller.waitForCompletion();
//             validateReceiptResult(false, getExtractedReceipts(), syncPoller.getFinalResult());
//         });
//     }
//
//     @Test
//     void extractReceiptInvalidSourceUrl() {
//         receiptInvalidSourceUrlRunner((sourceUrl) -> {
//             assertThrows(HttpResponseException.class, () -> client.beginExtractReceiptsFromUrl(sourceUrl, ).getSyncPoller());
//         });
//     }
// }
