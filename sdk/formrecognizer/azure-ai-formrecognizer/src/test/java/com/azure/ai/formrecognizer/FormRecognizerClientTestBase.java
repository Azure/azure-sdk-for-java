// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DataTable;
import com.azure.ai.formrecognizer.implementation.models.DataTableCell;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
import com.azure.ai.formrecognizer.implementation.models.FieldValue;
import com.azure.ai.formrecognizer.implementation.models.KeyValuePair;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.PageResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.implementation.models.SelectionMark;
import com.azure.ai.formrecognizer.implementation.models.TextLine;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.FieldBoundingBox;
import com.azure.ai.formrecognizer.models.FormElement;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormPageRange;
import com.azure.ai.formrecognizer.models.FormSelectionMark;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.PrebuiltType.BUSINESS_CARD;
import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.PrebuiltType.INVOICE;
import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.PrebuiltType.RECEIPT;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_ENDPOINT;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_SELECTION_MARK_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.deserializeRawResponse;
import static com.azure.ai.formrecognizer.TestUtils.FAKE_ENCODED_EMPTY_SPACE_URL;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.TestUtils.TEST_DATA_PNG;
import static com.azure.ai.formrecognizer.TestUtils.URL_TEST_FILE_FORMAT;
import static com.azure.ai.formrecognizer.TestUtils.getSerializerAdapter;
import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class FormRecognizerClientTestBase extends TestBase {

    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]+");
    private static final String EXPECTED_MULTIPAGE_ADDRESS_VALUE = "123 Hobbit Lane 567 Main St. Redmond, WA Redmond,"
        + " WA";
    private static final String EXPECTED_MULTIPAGE_PHONE_NUMBER_VALUE = "+15555555555";
    private static final String ITEMIZED_RECEIPT_VALUE = "Itemized";
    static final String RECEIPT_CONTOSO_JPG = "contoso-allinone.jpg";
    static final String RECEIPT_CONTOSO_PNG = "contoso-receipt.png";
    static final String INVOICE_6_PDF = "Invoice_6.pdf";
    static final String MULTIPAGE_INVOICE_PDF = "multipage_invoice1.pdf";
    static final String BUSINESS_CARD_JPG = "businessCard.jpg";
    static final String BUSINESS_CARD_PNG = "businessCard.png";
    static final String MULTIPAGE_BUSINESS_CARD_PDF = "business-card-multipage.pdf";
    static final String INVOICE_PDF = "Invoice_1.pdf";
    static final String MULTIPAGE_VENDOR_INVOICE_PDF = "multipage_vendor_invoice.pdf";

    // Error code
    static final String BAD_ARGUMENT_CODE = "BadArgument";
    static final String INVALID_IMAGE_ERROR_CODE = "InvalidImage";
    static final String INVALID_MODEL_ID_ERROR_CODE = "1001";
    static final String MODEL_ID_NOT_FOUND_ERROR_CODE = "1022";
    static final String URL_BADLY_FORMATTED_ERROR_CODE = "2001";
    static final String UNABLE_TO_READ_FILE_ERROR_CODE = "2005";

    // Error Message
    static final String HTTPS_EXCEPTION_MESSAGE =
        "Max retries 3 times exceeded. Error Details: Key credentials require HTTPS to prevent leaking the key.";
    static final String INVALID_UUID_EXCEPTION_MESSAGE = "Invalid UUID string: ";
    static final String MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE = "'modelId' is required and cannot be null.";

    static final String INVALID_ENDPOINT = "https://notreal.azure.com";
    static final String LOCAL_FILE_PATH = "src/test/resources/sample_files/Test/";
    static final String ENCODED_EMPTY_SPACE = "{\"source\":\"https://fakeuri.com/blank%20space\"}";

    // Business Card fields
    static final List<String> BUSINESS_CARD_FIELDS = Arrays.asList("ContactNames", "JobTitles", "Departments",
        "Emails", "Websites", "MobilePhones", "OtherPhones", "Faxes", "Addresses", "CompanyNames");

    // Receipt fields
    static final List<String> RECEIPT_FIELDS = Arrays.asList("MerchantName", "MerchantPhoneNumber", "MerchantAddress",
        "Total", "Subtotal", "Tax", "TransactionDate", "TransactionDate", "TransactionTime", "Items");

    // Invoice fields
    static final List<String> INVOICE_FIELDS = Arrays.asList("CustomerAddressRecipient", "InvoiceId", "VendorName",
        "VendorAddress", "CustomerAddress", "CustomerName", "InvoiceTotal", "DueDate", "InvoiceDate");

    enum PrebuiltType {
        RECEIPT, BUSINESS_CARD, INVOICE
    }

    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = ONE_NANO_DURATION;
        } else {
            durationTestMode = DEFAULT_POLL_INTERVAL;
        }
    }

    FormRecognizerClientBuilder getFormRecognizerClientBuilder(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }

    FormTrainingClientBuilder getFormTrainingClientBuilder(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        FormTrainingClientBuilder builder = new FormTrainingClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }

    private static void validateReferenceElementsData(List<String> expectedElements,
        List<FormElement> actualFormElementList, List<ReadResult> readResults) {
        if (expectedElements != null && actualFormElementList != null) {
            assertEquals(expectedElements.size(), actualFormElementList.size());
            for (int i = 0; i < actualFormElementList.size(); i++) {
                String[] indices = NON_DIGIT_PATTERN.matcher(expectedElements.get(i)).replaceAll(" ").trim().split(" ");

                if (indices.length < 2) {
                    return;
                }

                int readResultIndex = Integer.parseInt(indices[0]);
                int lineIndex = Integer.parseInt(indices[1]);
                if (indices.length == 3) {
                    int wordIndex = Integer.parseInt(indices[2]);
                    TextWord expectedTextWord =
                        readResults.get(readResultIndex).getLines().get(lineIndex).getWords().get(wordIndex);
                    TextLine expectedTextLine = readResults.get(readResultIndex).getLines().get(lineIndex);

                    if (actualFormElementList.get(i) instanceof FormLine) {
                        FormLine actualFormLine = (FormLine) actualFormElementList.get(i);
                        validateFormWordData(expectedTextLine.getWords(), actualFormLine.getWords());
                    }
                    FormWord actualFormWord = (FormWord) actualFormElementList.get(i);
                    assertEquals(expectedTextWord.getText(), actualFormWord.getText());
                    if (expectedTextWord.getConfidence() != null) {
                        assertEquals(expectedTextWord.getConfidence(), actualFormWord.getConfidence());
                    } else {
                        assertEquals(1.0f, actualFormWord.getConfidence());
                    }
                    validateBoundingBoxData(expectedTextWord.getBoundingBox(), actualFormWord.getBoundingBox());
                }
            }
        }
    }

    private static void validateFormTableData(List<DataTable> expectedFormTables,
        List<FormTable> actualFormTable, List<ReadResult> readResults, boolean includeFieldElements, int pageNumber) {
        assertEquals(expectedFormTables.size(), actualFormTable.size());
        for (int i = 0; i < actualFormTable.size(); i++) {
            DataTable expectedTable = expectedFormTables.get(i);
            FormTable actualTable = actualFormTable.get(i);
            assertEquals(pageNumber, actualTable.getPageNumber());
            assertEquals(expectedTable.getColumns(), actualTable.getColumnCount());
            validateCellData(expectedTable.getCells(), actualTable.getCells(), readResults, includeFieldElements);
            assertEquals(expectedTable.getRows(), actualTable.getRowCount());
            validateBoundingBoxData(expectedTable.getBoundingBox(), actualTable.getFieldBoundingBox());
        }
    }

    private static void validateCellData(List<DataTableCell> expectedTableCells,
        List<FormTableCell> actualTableCellList, List<ReadResult> readResults, boolean includeFieldElements) {
        assertEquals(expectedTableCells.size(), actualTableCellList.size());
        for (int i = 0; i < actualTableCellList.size(); i++) {
            DataTableCell expectedTableCell = expectedTableCells.get(i);
            FormTableCell actualTableCell = actualTableCellList.get(i);
            assertEquals(expectedTableCell.getColumnIndex(), actualTableCell.getColumnIndex());
            if (expectedTableCell.getColumnSpan() != null) {
                assertEquals(expectedTableCell.getColumnSpan(), actualTableCell.getColumnSpan());
            }

            assertEquals(expectedTableCell.getRowIndex(), actualTableCell.getRowIndex());
            if (expectedTableCell.getRowSpan() != null) {
                assertEquals(expectedTableCell.getRowSpan(), actualTableCell.getRowSpan());
            }
            validateBoundingBoxData(expectedTableCell.getBoundingBox(), actualTableCell.getBoundingBox());
            if (includeFieldElements) {
                validateReferenceElementsData(expectedTableCell.getElements(), actualTableCell.getFieldElements(),
                    readResults);
            }
        }
    }

    private static void validateFormLineData(List<TextLine> expectedLines, List<FormLine> actualLineList) {
        assertEquals(expectedLines.size(), actualLineList.size());
        for (int i = 0; i < actualLineList.size(); i++) {
            TextLine expectedLine = expectedLines.get(i);
            FormLine actualLine = actualLineList.get(i);
            assertEquals(expectedLine.getText(), actualLine.getText());
            validateBoundingBoxData(expectedLine.getBoundingBox(), actualLine.getBoundingBox());
            validateFormWordData(expectedLine.getWords(), actualLine.getWords());
        }
    }

    private static void validateFormSelectionMarkData(List<SelectionMark> expectedMarks,
        List<FormSelectionMark> actualMarks, int pageNumber) {
        for (int i = 0; i < actualMarks.size(); i++) {
            final SelectionMark expectedMark = expectedMarks.get(i);
            final FormSelectionMark actualMark = actualMarks.get(i);
            assertEquals(expectedMark.getState().toString(), actualMark.getState().toString());
            validateBoundingBoxData(expectedMark.getBoundingBox(), actualMark.getBoundingBox());
            // Currently, service has the null as the text value for layout.
            assertNull(actualMark.getText());
            assertEquals(pageNumber, actualMark.getPageNumber());
        }
    }

    private static void validateFormWordData(List<TextWord> expectedFormWords,
        List<FormWord> actualFormWordList) {
        assertEquals(expectedFormWords.size(), actualFormWordList.size());
        for (int i = 0; i < actualFormWordList.size(); i++) {

            TextWord expectedWord = expectedFormWords.get(i);
            FormWord actualWord = actualFormWordList.get(i);
            assertEquals(expectedWord.getText(), actualWord.getText());
            validateBoundingBoxData(expectedWord.getBoundingBox(), actualWord.getBoundingBox());
            if (expectedWord.getConfidence() != null) {
                assertEquals(expectedWord.getConfidence(), actualWord.getConfidence());
            } else {
                assertEquals(1.0f, actualWord.getConfidence());
            }
        }
    }

    private static void validateBoundingBoxData(List<Float> expectedBoundingBox, FieldBoundingBox actualFieldBoundingBox) {
        // TODO (Service Bug) To be fixed in preview 3
        // assertNotNull(actualFieldBoundingBox);
        // assertNotNull(actualFieldBoundingBox.getPoints());
        if (actualFieldBoundingBox != null && actualFieldBoundingBox.getPoints() != null) {
            int i = 0;
            for (Point point : actualFieldBoundingBox.getPoints()) {
                assertEquals(expectedBoundingBox.get(i), point.getX());
                assertEquals(expectedBoundingBox.get(++i), point.getY());
                i++;
            }
        }
    }

    private static void validateFieldValueTransforms(FieldValue expectedFieldValue, FormField actualFormField,
        List<ReadResult> readResults, boolean includeFieldElements) {
        if (expectedFieldValue != null) {
            if (expectedFieldValue.getBoundingBox() != null) {
                validateBoundingBoxData(expectedFieldValue.getBoundingBox(),
                    actualFormField.getValueData().getBoundingBox());
            }
            if (includeFieldElements && expectedFieldValue.getElements() != null) {
                validateReferenceElementsData(expectedFieldValue.getElements(),
                    actualFormField.getValueData().getFieldElements(), readResults);
            }
            switch (expectedFieldValue.getType()) {
                case NUMBER:
                    if (expectedFieldValue.getValueNumber() != null) {
                        assertEquals(expectedFieldValue.getValueNumber(), actualFormField.getValue().asFloat());
                    }
                    break;
                case DATE:
                    assertEquals(expectedFieldValue.getValueDate(), actualFormField.getValue().asDate());
                    break;
                case TIME:
                    assertEquals(LocalTime.parse(expectedFieldValue.getValueTime(),
                        DateTimeFormatter.ofPattern("HH:mm:ss")), actualFormField.getValue().asTime());
                    break;
                case STRING:
                    if (actualFormField.getName() != "ReceiptType") {
                        assertEquals(expectedFieldValue.getValueString(), actualFormField.getValue().asString());
                    }
                    break;
                case INTEGER:
                    assertEquals(expectedFieldValue.getValueInteger(), actualFormField.getValue().asLong());
                    break;
                case PHONE_NUMBER:
                    assertEquals(expectedFieldValue.getValuePhoneNumber(), actualFormField.getValue().asPhoneNumber());
                    break;
                case OBJECT:
                    expectedFieldValue.getValueObject().forEach((key, formField) -> {
                        FormField actualFormFieldValue = actualFormField.getValue().asMap().get(key);
                        validateFieldValueTransforms(formField, actualFormFieldValue, readResults,
                            includeFieldElements);
                    });
                    break;
                case ARRAY:
                    assertEquals(expectedFieldValue.getValueArray().size(), actualFormField.getValue().asList().size());
                    for (int i = 0; i < expectedFieldValue.getValueArray().size(); i++) {
                        FieldValue expectedReceiptItem = expectedFieldValue.getValueArray().get(i);
                        FormField actualReceiptItem = actualFormField.getValue().asList().get(i);
                        validateFieldValueTransforms(expectedReceiptItem, actualReceiptItem, readResults,
                            includeFieldElements);
                    }
                    break;
                default:
                    assertFalse(false, "Field type not supported.");
            }
        }
    }

    private static void validatePageRangeData(int expectedPageInfo, FormPageRange actualPageInfo) {
        assertEquals(expectedPageInfo, actualPageInfo.getFirstPageNumber());
        assertEquals(expectedPageInfo, actualPageInfo.getLastPageNumber());
    }

    // Receipt recognition

    @Test
    abstract void recognizeReceiptData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataWithBlankPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    // Receipt - URL

    @Test
    abstract void recognizeReceiptSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptFromUrlIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptSourceUrlWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    // Content recognition

    // Content - non-URL

    @Test
    abstract void recognizeContent(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithBlankPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentWithSelectionMarks(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    // Content - URL

    @Test
    abstract void recognizeContentFromUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromUrlWithPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentWithSelectionMarksFromUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    // Custom form recognition

    // Custom form - non-URL - labeled data

    @Test
    abstract void recognizeCustomFormLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithJpgContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithBlankPdfContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataExcludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithNullFormData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithNullModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithEmptyModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormInvalidStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormMultiPageLabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithSelectionMark(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    // Custom form - non-URL - unlabeled data
    @Test
    abstract void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUnlabeledDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormMultiPageUnlabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUnlabeledDataWithJpgContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUnlabeledDataWithBlankPdfContentType(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    // Custom form - URL - unlabeled data

    @Test
    abstract void recognizeCustomFormUrlUnlabeledData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlUnlabeledDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlMultiPageUnlabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    // Custom form - URL - labeled data

    @Test
    abstract void recognizeCustomFormInvalidSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormFromUrlLabeledDataWithNullModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormFromUrlLabeledDataWithEmptyModelId(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlLabeledDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlMultiPageLabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlLabeledDataWithSelectionMark(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    // Business Card - data
    @Test
    abstract void recognizeBusinessCardData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataNullData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataWithBlankPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardFromDamagedPdf(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    // Business card - URL

    @Test
    abstract void recognizeBusinessCardSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardInvalidSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardFromUrlIncludeFieldElements(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    void validateContentResultData(List<FormPage> actualFormPageList, boolean includeFieldElements) {
        AnalyzeResult analyzeResult = getAnalyzeRawResponse().getAnalyzeResult();
        final List<PageResult> pageResults = analyzeResult.getPageResults();
        final List<ReadResult> readResults = analyzeResult.getReadResults();
        for (int i = 0; i < actualFormPageList.size(); i++) {
            FormPage actualFormPage = actualFormPageList.get(i);
            ReadResult readResult = readResults.get(i);
            if (readResult.getAngle() > 180) {
                assertEquals(readResult.getAngle() - 360, actualFormPage.getTextAngle());
            } else {
                assertEquals(readResult.getAngle(), actualFormPage.getTextAngle());
            }
            assertEquals(readResult.getWidth(), actualFormPage.getWidth());
            assertEquals(readResult.getHeight(), actualFormPage.getHeight());
            assertEquals(readResult.getUnit().toString(), actualFormPage.getUnit().toString());
            assertEquals(readResult.getPage(), actualFormPage.getPageNumber());
            if (includeFieldElements) {
                validateFormLineData(readResult.getLines(), actualFormPage.getLines());
            }

            validateFormSelectionMarkData(readResult.getSelectionMarks(), actualFormPage.getSelectionMarks(),
                readResult.getPage());
            if (pageResults != null) {
                validateFormTableData(pageResults.get(i).getTables(), actualFormPage.getTables(), readResults,
                    includeFieldElements, pageResults.get(i).getPage());
            }
        }
    }

    void validateBlankPdfResultData(List<RecognizedForm> actualReceiptList) {
        assertEquals(1, actualReceiptList.size());
        final RecognizedForm actualReceipt = actualReceiptList.get(0);
        assertTrue(actualReceipt.getFields().isEmpty());
    }

    void validateRecognizedResult(List<RecognizedForm> actualFormList, boolean includeFieldElements,
        boolean isLabeled) {
        final AnalyzeResult rawResponse = getAnalyzeRawResponse().getAnalyzeResult();
        List<ReadResult> readResults = rawResponse.getReadResults();
        List<PageResult> pageResults = rawResponse.getPageResults();
        List<DocumentResult> documentResults = rawResponse.getDocumentResults();

        for (int i = 0; i < actualFormList.size(); i++) {
            validateContentResultData(actualFormList.get(i).getPages(), includeFieldElements);
            if (isLabeled) {
                validateLabeledData(actualFormList.get(i), includeFieldElements, readResults, documentResults.get(i));
            } else {
                validateUnLabeledResult(actualFormList.get(i), includeFieldElements, readResults, pageResults.get(i));
            }
        }
    }

    void validatePrebuiltResultData(List<RecognizedForm> actualPrebuiltRecognizedForms, boolean includeFieldElements,
        PrebuiltType prebuiltType) {
        final AnalyzeResult rawResponse = getAnalyzeRawResponse().getAnalyzeResult();
        final List<ReadResult> rawReadResults = rawResponse.getReadResults();
        for (int i = 0; i < actualPrebuiltRecognizedForms.size(); i++) {
            final RecognizedForm actualForm = actualPrebuiltRecognizedForms.get(i);
            final DocumentResult rawDocumentResult = rawResponse.getDocumentResults().get(i);

            validateLabeledData(actualForm, includeFieldElements, rawReadResults, rawDocumentResult);
            if (BUSINESS_CARD.equals(prebuiltType)) {
                assertEquals("prebuilt:businesscard", actualForm.getFormType());
                BUSINESS_CARD_FIELDS.forEach(businessCardField ->
                    validateFieldValueTransforms(rawDocumentResult.getFields().get(businessCardField),
                        actualForm.getFields().get(businessCardField), rawReadResults, includeFieldElements));
            } else if (RECEIPT.equals(prebuiltType)) {
                assertEquals("prebuilt:receipt", actualForm.getFormType());
                RECEIPT_FIELDS.forEach(receiptField -> {
                    final Map<String, FormField> actualRecognizedReceiptFields = actualForm.getFields();
                    Map<String, FieldValue> expectedReceiptFields = rawDocumentResult.getFields();
                    assertEquals(expectedReceiptFields.get("ReceiptType").getValueString(),
                        actualRecognizedReceiptFields.get("ReceiptType").getValue().asString());
                    assertEquals(expectedReceiptFields.get("ReceiptType").getConfidence(),
                        actualRecognizedReceiptFields.get("ReceiptType").getConfidence());
                    validateFieldValueTransforms(rawDocumentResult.getFields().get(receiptField),
                        actualRecognizedReceiptFields.get(receiptField), rawReadResults, includeFieldElements);
                });
            } else if (INVOICE.equals(prebuiltType)) {
                assertEquals("prebuilt:invoice", actualForm.getFormType());
                INVOICE_FIELDS.forEach(invoiceField -> {
                    final Map<String, FormField> actualRecognizedInvoiceFields = actualForm.getFields();
                    Map<String, FieldValue> expectedInvoiceFields = rawDocumentResult.getFields();

                    validateFieldValueTransforms(expectedInvoiceFields.get(invoiceField),
                        actualRecognizedInvoiceFields.get(invoiceField),
                        rawReadResults,
                        includeFieldElements);
                });
            } else {
                throw new RuntimeException("prebuilt type not supported");
            }
        }
    }


    // Others

    void invalidSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_RECEIPT_URL);
    }

    void encodedBlankSpaceSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(FAKE_ENCODED_EMPTY_SPACE_URL);
    }

    void urlRunner(Consumer<String> testRunner, String fileName) {
        testRunner.accept(URL_TEST_FILE_FORMAT + fileName);
    }

    void testingContainerUrlRunner(Consumer<String> testRunner, String fileName) {
        testRunner.accept(getStorageTestingFileUrl(fileName));
    }

    void dataRunner(BiConsumer<InputStream, Long> testRunner, String fileName) {
        final long fileLength = new File(LOCAL_FILE_PATH + fileName).length();

        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream(TEST_DATA_PNG.getBytes(StandardCharsets.UTF_8)), fileLength);
        } else {
            try {
                testRunner.accept(new FileInputStream(LOCAL_FILE_PATH + fileName), fileLength);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Local file not found.", e);
            }
        }
    }

    void localFilePathRunner(BiConsumer<String, Long> testRunner, String fileName) {
        final long fileLength = new File(LOCAL_FILE_PATH + fileName).length();
        testRunner.accept(LOCAL_FILE_PATH + fileName, fileLength);
    }

    void damagedPdfDataRunner(BiConsumer<InputStream, Integer> testRunner) {
        testRunner.accept(new ByteArrayInputStream(new byte[]{0x25, 0x50, 0x44, 0x46, 0x55, 0x55, 0x55}), 7);
    }

    void beginTrainingUnlabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), false);
    }

    void beginTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), true);
    }

    void beginSelectionMarkTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getSelectionMarkTrainingSasUri(), true);
    }

    void beginTrainingMultipageRunner(Consumer<String> testRunner) {
        testRunner.accept(getMultipageTrainingSasUri());
    }

    private void validateUnLabeledResult(RecognizedForm actualForm, boolean includeFieldElements,
        List<ReadResult> readResults, PageResult expectedPage) {
        validatePageRangeData(expectedPage.getPage(), actualForm.getPageRange());
        int i = 0;
        for (Map.Entry<String, FormField> entry : actualForm.getFields().entrySet()) {
            FormField actualFormField = entry.getValue();
            final KeyValuePair expectedFormField = expectedPage.getKeyValuePairs().get(i++);
            assertEquals(expectedFormField.getConfidence(), actualFormField.getConfidence());
            assertEquals(expectedFormField.getKey().getText(), actualFormField.getLabelData().getText());
            validateBoundingBoxData(expectedFormField.getKey().getBoundingBox(),
                actualFormField.getLabelData().getBoundingBox());
            if (includeFieldElements) {
                validateReferenceElementsData(expectedFormField.getKey().getElements(),
                    actualFormField.getLabelData().getFieldElements(), readResults);
                validateReferenceElementsData(expectedFormField.getValue().getElements(),
                    actualFormField.getValueData().getFieldElements(), readResults);
            }
            assertEquals(expectedFormField.getValue().getText(), actualFormField.getValueData().getText());
            validateBoundingBoxData(expectedFormField.getValue().getBoundingBox(),
                actualFormField.getValueData().getBoundingBox());
        }
    }

    private void validateLabeledData(RecognizedForm actualForm, boolean includeFieldElements,
        List<ReadResult> readResults, DocumentResult documentResult) {

        assertEquals(documentResult.getPageRange().get(0), actualForm.getPageRange().getFirstPageNumber());
        assertEquals(documentResult.getPageRange().get(1), actualForm.getPageRange().getLastPageNumber());
        assertEquals(documentResult.getFields().keySet(), actualForm.getFields().keySet());
        documentResult.getFields().forEach((label, expectedFieldValue) -> {
            final FormField actualFormField = actualForm.getFields().get(label);
            assertEquals(label, actualFormField.getName());
            if (expectedFieldValue != null) {
                if (expectedFieldValue.getConfidence() != null) {
                    assertEquals(expectedFieldValue.getConfidence(), actualFormField.getConfidence());
                } else {
                    assertEquals(1.0f, actualFormField.getConfidence());
                }
                validateFieldValueTransforms(expectedFieldValue, actualFormField, readResults, includeFieldElements);
            }
        });
    }

    static void validateMultiPageDataLabeled(List<RecognizedForm> actualRecognizedFormsList) {
        actualRecognizedFormsList.forEach(recognizedForm -> {
            // TODO (#14889): assertEquals("custom:modelId", recognizedForm.getFormType());
            // assertEquals("custom:form", recognizedForm.getFormType());
            assertEquals(1, recognizedForm.getPageRange().getFirstPageNumber());
            assertEquals(3, recognizedForm.getPageRange().getLastPageNumber());
            assertEquals(3, recognizedForm.getPages().size());
            recognizedForm.getFields().forEach((label, formField) -> {
                assertNotNull(formField.getName());
                assertNotNull(formField.getValue());
                assertNotNull(formField.getValueData().getText());
                assertNull(formField.getLabelData());
            });
        });
    }

    static void validateMultiPageDataUnlabeled(List<RecognizedForm> actualRecognizedFormsList) {
        actualRecognizedFormsList.forEach(recognizedForm -> {
            assertNotNull(recognizedForm.getFormType());
            assertEquals(1, (long) recognizedForm.getPages().size());
            recognizedForm.getFields().forEach((label, formField) -> {
                assertNotNull(formField.getName());
                assertNotNull(formField.getValue());
                assertNotNull(formField.getValueData().getText());
                assertNotNull(formField.getLabelData().getText());
            });
        });
    }

    static void validateMultipageBusinessData(List<RecognizedForm> recognizedBusinessCards) {
        assertEquals(2, recognizedBusinessCards.size());
        RecognizedForm businessCard1 = recognizedBusinessCards.get(0);
        RecognizedForm businessCard2 = recognizedBusinessCards.get(1);

        assertEquals(1, businessCard1.getPageRange().getFirstPageNumber());
        assertEquals(1, businessCard1.getPageRange().getLastPageNumber());
        Map<String, FormField> businessCard1Fields = businessCard1.getFields();
        List<FormField> emailList = businessCard1Fields.get("Emails").getValue().asList();
        assertEquals("johnsinger@contoso.com", emailList.get(0).getValue().asString());
        List<FormField> phoneNumberList = businessCard1Fields.get("OtherPhones").getValue().asList();
        assertEquals("+14257793479", phoneNumberList.get(0).getValue().asPhoneNumber());
        assertNotNull(businessCard1.getPages());

        // assert contact name page number
        FormField contactNameField = businessCard1Fields.get("ContactNames").getValue().asList().get(0);
        assertEquals(1, contactNameField.getValueData().getPageNumber());
        assertEquals("JOHN SINGER", contactNameField.getValueData().getText());

        assertEquals(2, businessCard2.getPageRange().getFirstPageNumber());
        assertEquals(2, businessCard2.getPageRange().getLastPageNumber());
        Map<String, FormField> businessCard2Fields = businessCard2.getFields();
        List<FormField> email2List = businessCard2Fields.get("Emails").getValue().asList();
        assertEquals("avery.smith@contoso.com", email2List.get(0).getValue().asString());
        List<FormField> phoneNumber2List = businessCard2Fields.get("OtherPhones").getValue().asList();
        assertEquals("+44 (0) 20 9876 5432", phoneNumber2List.get(0).getValueData().getText());
        assertNotNull(businessCard2.getPages());

        // assert contact name page number
        FormField contactName2Field = businessCard2Fields.get("ContactNames").getValue().asList().get(0);
        assertEquals(2, contactName2Field.getValueData().getPageNumber());
        assertEquals("Dr. Avery Smith", contactName2Field.getValueData().getText());
    }

    static void validateMultipageReceiptData(List<RecognizedForm> recognizedReceipts) {
        assertEquals(3, recognizedReceipts.size());
        RecognizedForm receiptPage1 = recognizedReceipts.get(0);
        RecognizedForm receiptPage2 = recognizedReceipts.get(1);
        RecognizedForm receiptPage3 = recognizedReceipts.get(2);

        assertEquals(1, receiptPage1.getPageRange().getFirstPageNumber());
        assertEquals(1, receiptPage1.getPageRange().getLastPageNumber());
        Map<String, FormField> receiptPage1Fields = receiptPage1.getFields();
        assertEquals(EXPECTED_MULTIPAGE_ADDRESS_VALUE, receiptPage1Fields.get("MerchantAddress")
            .getValue().asString());
        assertEquals("Bilbo Baggins", receiptPage1Fields.get("MerchantName")
            .getValue().asString());
        assertEquals(EXPECTED_MULTIPAGE_PHONE_NUMBER_VALUE, receiptPage1Fields.get("MerchantPhoneNumber")
            .getValue().asPhoneNumber());
        // assertNotNull(receiptPage1Fields.get("Total").getValue().asFloat());
        assertNotNull(receiptPage1.getPages());
        assertEquals(ITEMIZED_RECEIPT_VALUE, receiptPage1Fields.get("ReceiptType").getValue().asString());

        // Assert no fields and lines on second page
        assertEquals(0, receiptPage2.getFields().size());
        List<FormPage> receipt2Pages = receiptPage2.getPages();
        assertEquals(1, receipt2Pages.size());
        assertEquals(0, receipt2Pages.stream().findFirst().get().getLines().size());
        assertEquals(2, receiptPage2.getPageRange().getFirstPageNumber());
        assertEquals(2, receiptPage2.getPageRange().getLastPageNumber());

        assertEquals(3, receiptPage3.getPageRange().getFirstPageNumber());
        assertEquals(3, receiptPage3.getPageRange().getLastPageNumber());
        Map<String, FormField> receiptPage3Fields = receiptPage3.getFields();
        assertEquals(EXPECTED_MULTIPAGE_ADDRESS_VALUE, receiptPage3Fields.get("MerchantAddress").getValue().asString());
        assertEquals("Frodo Baggins", receiptPage3Fields.get("MerchantName").getValue().asString());
        assertEquals(EXPECTED_MULTIPAGE_PHONE_NUMBER_VALUE, receiptPage3Fields.get("MerchantPhoneNumber").getValue().asPhoneNumber());
        // assertNotNull(receiptPage3Fields.get("Total").getValue().asFloat());
        assertEquals(ITEMIZED_RECEIPT_VALUE, receiptPage3Fields.get("ReceiptType").getValue().asString());
    }

    static void validateMultipageInvoiceData(List<RecognizedForm> recognizedInvoices) {
        assertEquals(1, recognizedInvoices.size());
        RecognizedForm recognizedForm = recognizedInvoices.get(0);

        assertEquals(1, recognizedForm.getPageRange().getFirstPageNumber());
        assertEquals(2, recognizedForm.getPageRange().getLastPageNumber());
        Map<String, FormField> recognizedInvoiceFields = recognizedForm.getFields();
        final FormField remittanceAddressRecipient = recognizedInvoiceFields.get("RemittanceAddressRecipient");

        assertEquals("Contoso Ltd.", remittanceAddressRecipient.getValue().asString());
        assertEquals(1, remittanceAddressRecipient.getValueData().getPageNumber());
        final FormField remittanceAddress = recognizedInvoiceFields.get("RemittanceAddress");

        assertEquals("2345 Dogwood Lane Birch, Kansas 98123", remittanceAddress.getValue().asString());
        assertEquals(1, remittanceAddress.getValueData().getPageNumber());

        final FormField vendorName = recognizedInvoiceFields.get("VendorName");
        assertEquals("Southridge Video", vendorName.getValue().asString());
        assertEquals(2, vendorName.getValueData().getPageNumber());

        assertEquals(2, recognizedForm.getPages().size());
    }

    protected String getEndpoint() {
        return interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    private String getTrainingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            return Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL);
        }
    }

    private String getSelectionMarkTrainingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            return Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_SELECTION_MARK_BLOB_CONTAINER_SAS_URL);
        }
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    private String getMultipageTrainingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            return Configuration.getGlobalConfiguration()
                .get(FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL);
        }
    }

    /**
     * Get the testing data set SAS Url value based on the test running mode.
     *
     * @return the testing data set Url
     */
    private String getTestingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode?SASToken";
        } else {
            return Configuration.getGlobalConfiguration().get("FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL");
        }
    }

    /**
     * Prepare the file url from the testing data set SAS Url value.
     *
     * @return the testing data specific file Url
     */
    private String getStorageTestingFileUrl(String fileName) {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            final String[] urlParts = getTestingSasUri().split("\\?");
            return urlParts[0] + "/" + fileName + "?" + urlParts[1];
        }
    }

    /**
     * Prepare the expected test data from service raw response.
     *
     * @return the {@code AnalyzeOperationResult} test data
     */
    private AnalyzeOperationResult getAnalyzeRawResponse() {
        final SerializerAdapter serializerAdapter = getSerializerAdapter();
        final NetworkCallRecord networkCallRecord =
            interceptorManager.getRecordedData().findFirstAndRemoveNetworkCall(record -> {
                AnalyzeOperationResult rawModelResponse = deserializeRawResponse(serializerAdapter, record,
                    AnalyzeOperationResult.class);
                return rawModelResponse != null && rawModelResponse.getStatus() == OperationStatus.SUCCEEDED;
            });
        interceptorManager.getRecordedData().addNetworkCall(networkCallRecord);
        return deserializeRawResponse(serializerAdapter, networkCallRecord, AnalyzeOperationResult.class);
    }

    void validateNetworkCallRecord(String requestParam, String value) {
        final NetworkCallRecord networkCallRecord1 =
            interceptorManager.getRecordedData().findFirstAndRemoveNetworkCall(networkCallRecord -> {
                URL url = null;
                try {
                    url = new URL(networkCallRecord.getUri());
                } catch (MalformedURLException e) {
                    assertFalse(false, e.getMessage());
                }

                if (url != null && url.getQuery() != null) {
                    String[] params = url.getQuery().split("&");
                    for (String param : params) {
                        String name = param.split("=")[0];
                        String queryValue = param.split("=")[1];
                        if (name.equals(requestParam) && value.equals(queryValue)) {
                            return true;
                        }
                    }
                    return false;
                }
                return false;
            });

        assertNotNull(networkCallRecord1);
        interceptorManager.getRecordedData().addNetworkCall(networkCallRecord1);
    }
}
