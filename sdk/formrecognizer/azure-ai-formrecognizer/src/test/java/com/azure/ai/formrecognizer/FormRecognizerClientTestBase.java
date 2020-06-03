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
import com.azure.ai.formrecognizer.implementation.models.TextLine;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.FormContent;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.FormPageRange;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.ReceiptItemType;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.TextContentType;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.ai.formrecognizer.models.USReceiptItem;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_API_KEY;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_ENDPOINT;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.deserializeRawResponse;
import static com.azure.ai.formrecognizer.TestUtils.FORM_JPG;
import static com.azure.ai.formrecognizer.TestUtils.getFileData;
import static com.azure.ai.formrecognizer.TestUtils.getSerializerAdapter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class FormRecognizerClientTestBase extends TestBase {
    private static final String RECEIPT_CONTOSO_JPG = "contoso-allinone.jpg";
    private static final String INVOICE_PDF = "Invoice_6.pdf";
    private static final String MULTIPAGE_INVOICE_PDF = "multipage_invoice1.pdf";
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]+");
    private static final String EXPECTED_MULTIPAGE_ADDRESS_VALUE = "123 Hobbit Lane 567 Main St. Redmond, WA Redmond, WA";
    private static final String EXPECTED_MULTIPAGE_PHONE_NUMBER_VALUE = "+15555555555";
    private static final String ITEMIZED_RECEIPT_VALUE = "Itemized";

    private static void validateReferenceElementsData(List<String> expectedElements,
        List<FormContent> actualFormContentList, List<ReadResult> readResults) {
        if (expectedElements != null && actualFormContentList != null) {
            assertEquals(expectedElements.size(), actualFormContentList.size());
            for (int i = 0; i < actualFormContentList.size(); i++) {
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

                    if (actualFormContentList.get(i).getTextContentType().equals(TextContentType.LINE)) {
                        FormLine actualFormLine = (FormLine) actualFormContentList.get(i);
                        validateFormWordData(expectedTextLine.getWords(), actualFormLine.getFormWords());
                    }
                    FormWord actualFormContent = (FormWord) actualFormContentList.get(i);
                    assertEquals(expectedTextWord.getText(), actualFormContent.getText());
                    if (expectedTextWord.getConfidence() != null) {
                        assertEquals(expectedTextWord.getConfidence(), actualFormContent.getConfidence());
                    } else {
                        assertEquals(1.0f, actualFormContent.getConfidence());
                    }
                    validateBoundingBoxData(expectedTextWord.getBoundingBox(), actualFormContent.getBoundingBox());
                }
            }
        }
    }

    private static void validateFormTableData(List<DataTable> expectedFormTables,
        List<FormTable> actualFormTable, List<ReadResult> readResults, boolean includeTextDetails, int pageNumber) {
        assertEquals(expectedFormTables.size(), actualFormTable.size());
        for (int i = 0; i < actualFormTable.size(); i++) {
            DataTable expectedTable = expectedFormTables.get(i);
            FormTable actualTable = actualFormTable.get(i);
            assertEquals(pageNumber, actualTable.getPageNumber());
            assertEquals(expectedTable.getColumns(), actualTable.getColumnCount());
            validateCellData(expectedTable.getCells(), actualTable.getCells(), readResults, includeTextDetails);
            assertEquals(expectedTable.getRows(), actualTable.getRowCount());
        }
    }

    private static void validateCellData(List<DataTableCell> expectedTableCells,
        List<FormTableCell> actualTableCellList, List<ReadResult> readResults, boolean includeTextDetails) {
        assertEquals(expectedTableCells.size(), actualTableCellList.size());
        for (int i = 0; i < actualTableCellList.size(); i++) {
            DataTableCell expectedTableCell = expectedTableCells.get(i);
            FormTableCell actualTableCell = actualTableCellList.get(i);
            assertEquals(expectedTableCell.getColumnIndex(), actualTableCell.getColumnIndex());
            assertEquals(expectedTableCell.getColumnSpan(), actualTableCell.getColumnSpan());
            assertEquals(expectedTableCell.getRowIndex(), actualTableCell.getRowIndex());
            assertEquals(expectedTableCell.getRowSpan(), actualTableCell.getRowSpan());
            validateBoundingBoxData(expectedTableCell.getBoundingBox(), actualTableCell.getBoundingBox());
            if (includeTextDetails) {
                validateReferenceElementsData(expectedTableCell.getElements(), actualTableCell.getElements(),
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
            validateFormWordData(expectedLine.getWords(), actualLine.getFormWords());
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

    private static void validateBoundingBoxData(List<Float> expectedBoundingBox, BoundingBox actualBoundingBox) {
        if (actualBoundingBox != null && actualBoundingBox.getPoints() != null) {
            int i = 0;
            for (Point point : actualBoundingBox.getPoints()) {
                assertEquals(expectedBoundingBox.get(i), point.getX());
                assertEquals(expectedBoundingBox.get(++i), point.getY());
                i++;
            }
        }
    }

    private static void validateFieldValueTransforms(FieldValue expectedFieldValue, FormField<?> actualFormField,
        List<ReadResult> readResults, boolean includeTextDetails) {
        if (expectedFieldValue != null) {
            if (expectedFieldValue.getBoundingBox() != null) {
                validateBoundingBoxData(expectedFieldValue.getBoundingBox(),
                    actualFormField.getValueText().getBoundingBox());
            }
            if (includeTextDetails && expectedFieldValue.getElements() != null) {
                validateReferenceElementsData(expectedFieldValue.getElements(),
                    actualFormField.getValueText().getTextContent(), readResults);
            }
            switch (expectedFieldValue.getType()) {
                case NUMBER:
                    assertEquals(expectedFieldValue.getValueNumber(), actualFormField.getFieldValue());
                    break;
                case DATE:
                    assertEquals(expectedFieldValue.getValueDate(), actualFormField.getFieldValue());
                    break;
                case TIME:
                    assertEquals(LocalTime.parse(expectedFieldValue.getValueTime(),
                        DateTimeFormatter.ofPattern("HH:mm:ss")), actualFormField.getFieldValue());
                    break;
                case STRING:
                    assertEquals(expectedFieldValue.getValueString(), actualFormField.getFieldValue());
                    break;
                case INTEGER:
                    assertEquals(expectedFieldValue.getValueInteger(), actualFormField.getFieldValue());
                    break;
                case PHONE_NUMBER:
                    assertEquals(expectedFieldValue.getValuePhoneNumber(), actualFormField.getFieldValue());
                    break;
                case OBJECT:
                case ARRAY:
                    return;
                default:
                    assertFalse(false, "Field type not supported.");
            }
        }
    }

    private static void validatePageRangeData(int expectedPageInfo, FormPageRange actualPageInfo) {
        assertEquals(expectedPageInfo, actualPageInfo.getFirstPageNumber());
        assertEquals(expectedPageInfo, actualPageInfo.getLastPageNumber());
    }

    private static void validateReceiptItemsData(List<FieldValue> expectedReceiptItemList,
        List<USReceiptItem> actualReceiptItemList, List<ReadResult> readResults, boolean includeTextDetails) {
        assertEquals(expectedReceiptItemList.size(), actualReceiptItemList.size());
        for (int i = 0; i < expectedReceiptItemList.size(); i++) {
            FieldValue expectedReceiptItem = expectedReceiptItemList.get(i);
            USReceiptItem actualReceiptItem = actualReceiptItemList.get(i);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.NAME.toString()),
                actualReceiptItem.getName(), readResults, includeTextDetails);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.QUANTITY.toString()),
                actualReceiptItem.getQuantity(), readResults, includeTextDetails);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.TOTAL_PRICE.toString()),
                actualReceiptItem.getTotalPrice(), readResults, includeTextDetails);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.PRICE.toString()),
                actualReceiptItem.getPrice(), readResults, includeTextDetails);
        }
    }

    @Test
    abstract void recognizeReceiptSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptSourceUrlTextDetails(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    // Turn off the tests as there is service regression on the media type.
    // Issue link: https://github.com/Azure/azure-sdk-for-java/issues/11036
    // @Test
    // abstract void recognizeReceiptDataTextDetails(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataTextDetailsWithNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContent(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithNullData(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithNullValues(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithContentTypeAutoDetection(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormInvalidSourceUrl(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlMultiPageLabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormMultiPageUnlabeled(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    void validateUSReceiptData(USReceipt actualRecognizedReceipt, boolean includeTextDetails) {
        final AnalyzeResult analyzeResult = getAnalyzeRawResponse().getAnalyzeResult();
        List<ReadResult> readResults = analyzeResult.getReadResults();
        DocumentResult documentResult = analyzeResult.getDocumentResults().get(0);
        final Map<String, FieldValue> expectedReceiptFields = documentResult.getFields();
        validatePageRangeData(documentResult.getPageRange().get(0),
            actualRecognizedReceipt.getRecognizedForm().getFormPageRange());
        validatePageRangeData(documentResult.getPageRange().get(1),
            actualRecognizedReceipt.getRecognizedForm().getFormPageRange());
        assertEquals(expectedReceiptFields.get("ReceiptType").getValueString(),
            actualRecognizedReceipt.getReceiptType().getType());
        assertEquals(expectedReceiptFields.get("ReceiptType").getConfidence(),
            actualRecognizedReceipt.getReceiptType().getConfidence());
        validateFieldValueTransforms(expectedReceiptFields.get("MerchantName"),
            actualRecognizedReceipt.getMerchantName(), readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("MerchantPhoneNumber"),
            actualRecognizedReceipt.getMerchantPhoneNumber(), readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("MerchantAddress"),
            actualRecognizedReceipt.getMerchantAddress(), readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("Total"), actualRecognizedReceipt.getTotal(),
            readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("Subtotal"), actualRecognizedReceipt.getSubtotal(),
            readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("Tax"), actualRecognizedReceipt.getTax(), readResults,
            includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("TransactionDate"),
            actualRecognizedReceipt.getTransactionDate(), readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("TransactionTime"),
            actualRecognizedReceipt.getTransactionTime(), readResults, includeTextDetails);
        validateReceiptItemsData(expectedReceiptFields.get("Items").getValueArray(),
            actualRecognizedReceipt.getReceiptItems(), readResults, includeTextDetails);
    }

    void validateContentResultData(List<FormPage> actualFormPageList, boolean includeTextDetails) {
        AnalyzeResult analyzeResult = getAnalyzeRawResponse().getAnalyzeResult();
        final List<PageResult> pageResults = analyzeResult.getPageResults();
        final List<ReadResult> readResults = analyzeResult.getReadResults();
        for (int i = 0; i < actualFormPageList.size(); i++) {
            FormPage actualFormPage = actualFormPageList.get(i);
            ReadResult readResult = readResults.get(i);
            assertEquals(readResult.getAngle(), actualFormPage.getTextAngle());
            assertEquals(readResult.getWidth(), actualFormPage.getWidth());
            assertEquals(readResult.getHeight(), actualFormPage.getHeight());
            assertEquals(readResult.getUnit().toString(), actualFormPage.getUnit().toString());
            assertEquals(readResult.getPage(), actualFormPage.getPageNumber());
            if (includeTextDetails) {
                validateFormLineData(readResult.getLines(), actualFormPage.getLines());
            }
            if (pageResults != null) {
                validateFormTableData(pageResults.get(i).getTables(), actualFormPage.getTables(), readResults,
                    includeTextDetails, pageResults.get(i).getPage());
            }
        }
    }

    void validateReceiptResultData(List<RecognizedReceipt> actualReceiptList, boolean includeTextDetails) {
        final AnalyzeResult rawResponse = getAnalyzeRawResponse().getAnalyzeResult();
        for (int i = 0; i < actualReceiptList.size(); i++) {
            final RecognizedReceipt actualReceipt = actualReceiptList.get(i);
            assertEquals("en-US", actualReceipt.getReceiptLocale());
            validateLabeledData(actualReceipt.getRecognizedForm(), includeTextDetails, rawResponse.getReadResults(),
                rawResponse.getDocumentResults().get(i));
        }
    }

    void validateRecognizedResult(List<RecognizedForm> actualFormList, boolean includeTextDetails,
        boolean isLabeled) {
        final AnalyzeResult rawResponse = getAnalyzeRawResponse().getAnalyzeResult();
        List<ReadResult> readResults = rawResponse.getReadResults();
        List<PageResult> pageResults = rawResponse.getPageResults();
        List<DocumentResult> documentResults = rawResponse.getDocumentResults();

        for (int i = 0; i < actualFormList.size(); i++) {
            validateContentResultData(actualFormList.get(i).getPages(), includeTextDetails);
            if (isLabeled) {
                validateLabeledData(actualFormList.get(i), includeTextDetails, readResults, documentResults.get(i));
            } else {
                validateUnLabeledResult(actualFormList.get(i), includeTextDetails, readResults, pageResults.get(i));
            }
        }
    }

    void receiptSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(getStorageTestingFileUrl(RECEIPT_CONTOSO_JPG));
    }

    void receiptSourceUrlRunnerTextDetails(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getStorageTestingFileUrl(RECEIPT_CONTOSO_JPG), true);
    }

    void receiptDataRunner(Consumer<InputStream> testRunner) {
        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream("isPlaybackMode".getBytes()));
        } else {
            testRunner.accept(getFileData(getStorageTestingFileUrl(RECEIPT_CONTOSO_JPG)));
        }
    }

    void receiptDataRunnerTextDetails(BiConsumer<InputStream, Boolean> testRunner) {
        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream("isPlaybackMode".getBytes()), true);
        } else {
            testRunner.accept(getFileData(getStorageTestingFileUrl(RECEIPT_CONTOSO_JPG)), true);
        }
    }

    void invalidSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_RECEIPT_URL);
    }

    void contentFromDataRunner(Consumer<InputStream> testRunner) {
        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream("isPlaybackMode".getBytes()));
        } else {
            testRunner.accept(getFileData(getStorageTestingFileUrl(FORM_JPG)));
        }
    }

    void multipageFromDataRunner(Consumer<InputStream> testRunner) {
        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream("isPlaybackMode".getBytes()));
        } else {
            testRunner.accept(getFileData(getStorageTestingFileUrl(MULTIPAGE_INVOICE_PDF)));
        }
    }

    void multipageFromUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(getStorageTestingFileUrl(MULTIPAGE_INVOICE_PDF));
    }

    void contentFromUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(getStorageTestingFileUrl(FORM_JPG));
    }

    void customFormDataRunner(Consumer<InputStream> testRunner) {
        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream("testData.png".getBytes()));
        } else {
            testRunner.accept(getFileData(getStorageTestingFileUrl(INVOICE_PDF)));
        }
    }

    void beginTrainingUnlabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), false);
    }

    void beginTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), true);
    }

    void beginTrainingMultipageRunner(Consumer<String> testRunner) {
        testRunner.accept(getMultipageTrainingSasUri());
    }

    private void validateUnLabeledResult(RecognizedForm actualForm, boolean includeTextDetails,
        List<ReadResult> readResults, PageResult expectedPage) {
        validatePageRangeData(expectedPage.getPage(), actualForm.getFormPageRange());
        for (int i = 0; i < expectedPage.getKeyValuePairs().size(); i++) {
            final KeyValuePair expectedFormField = expectedPage.getKeyValuePairs().get(i);
            final FormField<?> actualFormField = actualForm.getFields().get("field-" + i);
            assertEquals(expectedFormField.getConfidence(), actualFormField.getConfidence());
            assertEquals(expectedFormField.getKey().getText(), actualFormField.getLabelText().getText());
            validateBoundingBoxData(expectedFormField.getKey().getBoundingBox(),
                actualFormField.getLabelText().getBoundingBox());
            if (includeTextDetails) {
                validateReferenceElementsData(expectedFormField.getKey().getElements(),
                    actualFormField.getLabelText().getTextContent(), readResults);
                validateReferenceElementsData(expectedFormField.getValue().getElements(),
                    actualFormField.getValueText().getTextContent(), readResults);
            }
            assertEquals(expectedFormField.getValue().getText(), actualFormField.getValueText().getText());
            validateBoundingBoxData(expectedFormField.getValue().getBoundingBox(),
                actualFormField.getValueText().getBoundingBox());
        }
    }

    private void validateLabeledData(RecognizedForm actualForm, boolean includeTextDetails,
        List<ReadResult> readResults, DocumentResult documentResult) {

        assertEquals(documentResult.getPageRange().get(0), actualForm.getFormPageRange().getFirstPageNumber());
        assertEquals(documentResult.getPageRange().get(1), actualForm.getFormPageRange().getLastPageNumber());
        documentResult.getFields().forEach((label, expectedFieldValue) -> {
            final FormField<?> actualFormField = actualForm.getFields().get(label);
            assertEquals(label, actualFormField.getName());
            if (expectedFieldValue != null) {
                if (expectedFieldValue.getConfidence() != null) {
                    assertEquals(expectedFieldValue.getConfidence(), actualFormField.getConfidence());
                } else {
                    assertEquals(1.0f, actualFormField.getConfidence());
                }
                validateFieldValueTransforms(expectedFieldValue, actualFormField, readResults, includeTextDetails);
            }
        });
    }

    static void validateMultiPageDataLabeled(List<RecognizedForm> actualRecognizedFormsList) {
        actualRecognizedFormsList.forEach(recognizedForm -> {
            assertEquals("custom:form", recognizedForm.getFormType());
            assertEquals(1, recognizedForm.getFormPageRange().getFirstPageNumber());
            assertEquals(3, recognizedForm.getFormPageRange().getLastPageNumber());
            assertEquals(3, recognizedForm.getPages().size());
            recognizedForm.getFields().forEach((label, formField) -> {
                assertNotNull(formField.getName());
                assertNotNull(formField.getFieldValue());
                assertNotNull(formField.getValueText().getText());
                assertNotNull(formField.getLabelText().getText());
            });
        });
    }

    static void validateMultiPageDataUnlabeled(List<RecognizedForm> actualRecognizedFormsList) {
        actualRecognizedFormsList.forEach(recognizedForm -> {
            assertNotNull(recognizedForm.getFormType());
            assertEquals(1, recognizedForm.getPages().stream().count());
            recognizedForm.getFields().forEach((label, formField) -> {
                assertNotNull(formField.getName());
                assertNotNull(formField.getFieldValue());
                assertNotNull(formField.getValueText().getText());
                assertNotNull(formField.getLabelText().getText());
            });
        });
    }

    static void validateMultipageReceiptData(List<RecognizedReceipt> recognizedReceipts) {
        assertEquals(3, recognizedReceipts.size());
        USReceipt receiptPage1 = ReceiptExtensions.asUSReceipt(recognizedReceipts.get(0));
        USReceipt receiptPage2 = ReceiptExtensions.asUSReceipt(recognizedReceipts.get(1));
        USReceipt receiptPage3 = ReceiptExtensions.asUSReceipt(recognizedReceipts.get(2));

        assertEquals(1, receiptPage1.getRecognizedForm().getFormPageRange().getFirstPageNumber());
        assertEquals(1, receiptPage1.getRecognizedForm().getFormPageRange().getLastPageNumber());
        assertEquals(EXPECTED_MULTIPAGE_ADDRESS_VALUE, receiptPage1.getMerchantAddress().getFieldValue());
        assertEquals("Bilbo Baggins", receiptPage1.getMerchantName().getFieldValue());
        assertEquals(EXPECTED_MULTIPAGE_PHONE_NUMBER_VALUE, receiptPage1.getMerchantPhoneNumber().getFieldValue());
        assertNotNull(receiptPage1.getTotal().getFieldValue());
        assertNotNull(receiptPage1.getRecognizedForm().getPages());
        assertEquals(ITEMIZED_RECEIPT_VALUE, receiptPage1.getReceiptType().getType());

        // Assert no fields, tables and lines on second page
        assertEquals(0, receiptPage2.getRecognizedForm().getFields().size());
        List<FormPage> receipt2Pages = receiptPage2.getRecognizedForm().getPages();
        assertEquals(1, receipt2Pages.size());
        assertEquals(0, receipt2Pages.stream().findFirst().get().getTables().size());
        assertEquals(0, receipt2Pages.stream().findFirst().get().getLines().size());
        assertEquals(2, receiptPage2.getRecognizedForm().getFormPageRange().getFirstPageNumber());
        assertEquals(2, receiptPage2.getRecognizedForm().getFormPageRange().getLastPageNumber());

        assertEquals(3, receiptPage3.getRecognizedForm().getFormPageRange().getFirstPageNumber());
        assertEquals(3, receiptPage3.getRecognizedForm().getFormPageRange().getLastPageNumber());
        assertEquals(EXPECTED_MULTIPAGE_ADDRESS_VALUE, receiptPage3.getMerchantAddress().getFieldValue());
        assertEquals("Frodo Baggins", receiptPage3.getMerchantName().getFieldValue());
        assertEquals(EXPECTED_MULTIPAGE_PHONE_NUMBER_VALUE, receiptPage3.getMerchantPhoneNumber().getFieldValue());
        assertNotNull(receiptPage3.getTotal().getFieldValue());
        // why isn't tip returned by service?
        // total value 1000 returned by service but should be 4300, service bug
        assertEquals(3000, receiptPage3.getSubtotal().getFieldValue());
        assertEquals(1000, receiptPage3.getTotal().getFieldValue());
        assertNotNull(receiptPage1.getRecognizedForm().getPages());
        assertEquals(ITEMIZED_RECEIPT_VALUE, receiptPage3.getReceiptType().getType());
    }

    /**
     * Get the string of API key value based on the test running mode.
     *
     * @return the API key string
     */
    String getApiKey() {
        return interceptorManager.isPlaybackMode() ? "apiKeyInPlayback"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_API_KEY);
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
                .get("FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL");
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
            return Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL);
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
}
