// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.INVALID_ENDPOINT;
import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.INVALID_UUID_EXCEPTION_MESSAGE;
import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE;
import static com.azure.ai.formrecognizer.TestUtils.CONTENT_FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.FAKE_ENCODED_EMPTY_SPACE_URL;
import static com.azure.ai.formrecognizer.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.TestUtils.URL_TEST_FILE_FORMAT;
import static com.azure.ai.formrecognizer.TestUtils.VALID_HTTPS_LOCALHOST;
import static com.azure.ai.formrecognizer.models.FormContentType.APPLICATION_PDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Class to test client side validations for Form Recognizer clients.
 */
public class FormRecognizerClientUnitTest {

    private static FormRecognizerClient client;
    private static FormRecognizerAsyncClient asyncClient;
    private static final String INPUT_STRING = "Hello World!";

    @BeforeAll
    protected static void beforeTest() {
        FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder()
            .endpoint(VALID_HTTPS_LOCALHOST)
            .credential(new AzureKeyCredential("fakeKey"));

        client = builder.buildClient();
        asyncClient = builder.buildAsyncClient();
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeReceiptDataNullData() {
        assertThrows(NullPointerException.class,
            () -> client.beginRecognizeReceipts(null, 0).setPollInterval(ONE_NANO_DURATION));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeContentResultWithNullData() {
        assertThrows(NullPointerException.class,
            () -> asyncClient.beginRecognizeContent(null, 0).setPollInterval(ONE_NANO_DURATION).getSyncPoller());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeBusinessCardDataNullData() {
        assertThrows(NullPointerException.class,
            () -> asyncClient.beginRecognizeBusinessCards(null, 0).getSyncPoller());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeIDDocumentDataNullData() {
        assertThrows(NullPointerException.class,
            () -> asyncClient.beginRecognizeIdentityDocuments(null, 0).getSyncPoller());
    }

    /**
     * Verifies an exception thrown for an empty model id when recognizing custom form from URL.
     */
    @Test
    public void recognizeCustomFormLabeledDataWithEmptyModelId() {
        Exception ex = assertThrows(RuntimeException.class,
            () -> asyncClient.beginRecognizeCustomForms(
                    "",
                    BinaryData.fromBytes(INPUT_STRING.getBytes()).toFluxByteBuffer(),
                    INPUT_STRING.length(),
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setFieldElementsIncluded(true))
                .setPollInterval(ONE_NANO_DURATION)
                .getSyncPoller());
        assertEquals(INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Verifies an exception thrown for a document using null model id.
     */
    @Test
    public void recognizeCustomFormLabeledDataWithNullModelId() {
        String inputString = "Hello World!";
        Exception ex = assertThrows(RuntimeException.class,
            () -> asyncClient.beginRecognizeCustomForms(
                    null,
                    BinaryData.fromBytes(inputString.getBytes()).toFluxByteBuffer(),
                    inputString.length(),
                    new RecognizeCustomFormsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setFieldElementsIncluded(true))
                .setPollInterval(ONE_NANO_DURATION)
                .getSyncPoller());
        assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Verifies an exception thrown for a null model id when recognizing custom form from URL.
     */
    @Test
    public void recognizeCustomFormFromUrlLabeledDataWithNullModelId() {
        Exception ex = assertThrows(RuntimeException.class,
            () -> asyncClient.beginRecognizeCustomFormsFromUrl(null, FAKE_ENCODED_EMPTY_SPACE_URL)
                .setPollInterval(ONE_NANO_DURATION).getSyncPoller());
        assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Verifies an exception thrown for an empty model id for recognizing custom forms from URL.
     */
    @Test
    public void recognizeCustomFormFromUrlLabeledDataWithEmptyModelId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> asyncClient.beginRecognizeCustomFormsFromUrl("", FAKE_ENCODED_EMPTY_SPACE_URL)
                .setPollInterval(ONE_NANO_DURATION).getSyncPoller());
        assertEquals(INVALID_UUID_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeReceiptDataNullDataSync() {
        assertThrows(NullPointerException.class, () -> client.beginRecognizeReceipts(null, 0));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeContentResultWithNullDataSync() {
        assertThrows(NullPointerException.class, () -> client.beginRecognizeContent(null, 0));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeBusinessCardDataNullDataSync() {
        assertThrows(NullPointerException.class,
            () -> client.beginRecognizeBusinessCards(null, 0));
    }

    /**
     * Verifies an exception thrown for a document using null data value.
     */
    @Test
    public void recognizeIDDocumentDataNullDataSync() {

        assertThrows(NullPointerException.class, () -> client.beginRecognizeIdentityDocuments(null, 0));
    }

    /**
     * Verifies an exception thrown for an empty model id.
     */
    @Test
    public void recognizeCustomFormLabeledDataWithEmptyModelIdSync() {
        Exception ex = assertThrows(RuntimeException.class,
            () -> client.beginRecognizeCustomForms("",
                    new ByteArrayInputStream(INPUT_STRING.getBytes()),
                    INPUT_STRING.length(),
                    new RecognizeCustomFormsOptions().setContentType(APPLICATION_PDF).setFieldElementsIncluded(true),
                    Context.NONE)
                .setPollInterval(ONE_NANO_DURATION));
        assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Verifies an exception thrown for an empty model id for recognizing custom forms from URL.
     */
    @Test
    public void recognizeCustomFormFromUrlLabeledDataWithEmptyModelIdSync() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> client.beginRecognizeCustomFormsFromUrl("", FAKE_ENCODED_EMPTY_SPACE_URL,
                new RecognizeCustomFormsOptions().setPollInterval(ONE_NANO_DURATION), Context.NONE));
        assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Verifies an exception thrown for a null model id when recognizing custom form from URL.
     */
    @Test
    public void recognizeCustomFormFromUrlLabeledDataWithNullModelIdSync() {
        Exception ex = assertThrows(RuntimeException.class, () -> client.beginRecognizeCustomFormsFromUrl(
            null, FAKE_ENCODED_EMPTY_SPACE_URL, new RecognizeCustomFormsOptions()
                .setPollInterval(ONE_NANO_DURATION), Context.NONE));
        assertEquals(MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE, ex.getMessage());
    }

    /**
     * Test for invalid endpoint, which throws connection refused exception message.
     */
    @Test
    public void clientBuilderWithInvalidEndpoint() {
        assertThrows(RuntimeException.class,
            () -> new FormRecognizerClientBuilder()
                .credential(new AzureKeyCredential("fakeKey"))
                .endpoint(INVALID_ENDPOINT)
                .buildClient()
                .beginRecognizeContentFromUrl(URL_TEST_FILE_FORMAT + CONTENT_FORM_JPG).getFinalResult());
    }
}
