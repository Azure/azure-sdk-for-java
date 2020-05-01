// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DataTable;
import com.azure.ai.formrecognizer.implementation.models.DataTableCell;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
import com.azure.ai.formrecognizer.implementation.models.FieldValue;
import com.azure.ai.formrecognizer.implementation.models.KeyValuePair;
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
import com.azure.ai.formrecognizer.models.PageRange;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.ReceiptItemType;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.TextContentType;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.ai.formrecognizer.models.USReceiptItem;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.FormRecognizerClientBuilder.OCP_APIM_SUBSCRIPTION_KEY;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.getFileData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class FormRecognizerClientTestBase extends TestBase {
    private static final String AZURE_FORM_RECOGNIZER_API_KEY = "AZURE_FORM_RECOGNIZER_API_KEY";
    private static final String NAME = "name";
    private static final String FORM_RECOGNIZER_PROPERTIES = "azure-ai-formrecognizer.properties";
    private static final String VERSION = "version";
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]+");
    public static final String FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL =
        "FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL";

    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(FORM_RECOGNIZER_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

    static void validateLayoutDataResults(IterableStream<FormPage> actualFormPages, List<ReadResult> readResults,
        List<PageResult> pageResults, boolean includeTextDetails) {
        List<FormPage> actualFormPageList = actualFormPages.stream().collect(Collectors.toList());
        for (int i = 0; i < actualFormPageList.size(); i++) {
            FormPage actualFormPage = actualFormPageList.get(i);
            ReadResult readResult = readResults.get(i);

            assertEquals(readResult.getAngle(), actualFormPage.getTextAngle());
            assertEquals(readResult.getWidth(), actualFormPage.getWidth());
            assertEquals(readResult.getHeight(), actualFormPage.getHeight());
            assertEquals(readResult.getUnit().toString(), actualFormPage.getUnit().toString());
            if (includeTextDetails) {
                validateFormLineData(readResult.getLines(), actualFormPage.getLines());
            }
            if (pageResults != null) {
                validateFormTableData(pageResults.get(i).getTables(), actualFormPage.getTables(), readResults,
                    includeTextDetails);
            }
        }
    }

    private static void validateReferenceElementsData(List<String> expectedElements,
        IterableStream<FormContent> actualElementStream, List<ReadResult> readResults) {
        if (expectedElements != null && actualElementStream != null) {
            List<FormContent> actualFormContentList = actualElementStream.stream().collect(Collectors.toList());
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
        IterableStream<FormTable> actualFormTables, List<ReadResult> readResults, boolean includeTextDetails) {
        List<FormTable> actualFormTable = actualFormTables.stream().collect(Collectors.toList());
        assertEquals(expectedFormTables.size(), actualFormTable.size());
        for (int i = 0; i < actualFormTable.size(); i++) {
            DataTable expectedTable = expectedFormTables.get(i);
            FormTable actualTable = actualFormTable.get(i);
            assertEquals(expectedTable.getColumns(), actualTable.getColumnCount());
            validateCellData(expectedTable.getCells(), actualTable.getCells(), readResults, includeTextDetails);
            assertEquals(expectedTable.getRows(), actualTable.getRowCount());
        }
    }

    private static void validateCellData(List<DataTableCell> expectedTableCells,
        IterableStream<FormTableCell> actualTableCells, List<ReadResult> readResults, boolean includeTextDetails) {
        List<FormTableCell> actualTableCellList = actualTableCells.stream().collect(Collectors.toList());
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

    private static void validateFormLineData(List<TextLine> expectedLines, IterableStream<FormLine> actualLines) {
        List<FormLine> actualLineList = actualLines.stream().collect(Collectors.toList());
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
        IterableStream<FormWord> actualFormWords) {
        List<FormWord> actualFormWordList = actualFormWords.stream().collect(Collectors.toList());
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

    static void validateReceiptResultData(IterableStream<RecognizedReceipt> actualResult, AnalyzeResult rawResponse,
        boolean includeTextDetails) {
        List<RecognizedReceipt> actualReceiptList = actualResult.stream().collect(Collectors.toList());
        for (int i = 0; i < actualReceiptList.size(); i++) {
            final RecognizedReceipt actualReceipt = actualReceiptList.get(i);
            assertEquals("en-US", actualReceipt.getReceiptLocale());
            validateLabeledData(actualReceipt.getRecognizedForm(), includeTextDetails,
                rawResponse.getReadResults(), rawResponse.getDocumentResults().get(i));
        }
    }

    static void validateRecognizedResult(IterableStream<RecognizedForm> actualForms,
        AnalyzeResult rawResponse, boolean includeTextDetails, boolean isLabeled) {
        List<ReadResult> readResults = rawResponse.getReadResults();
        List<PageResult> pageResults = rawResponse.getPageResults();
        List<DocumentResult> documentResults = rawResponse.getDocumentResults();
        List<RecognizedForm> actualFormList = actualForms.stream().collect(Collectors.toList());

        for (int i = 0; i < actualFormList.size(); i++) {
            if (isLabeled) {
                validateLabeledData(actualFormList.get(i), includeTextDetails, readResults,
                    documentResults.get(i));
            } else {
                validateUnLabeledResult(actualFormList.get(i), includeTextDetails, readResults,
                    pageResults.get(i), pageResults);
            }
        }
    }

    private static void validateUnLabeledResult(RecognizedForm actualForm, boolean includeTextDetails,
        List<ReadResult> readResults, PageResult expectedPage, List<PageResult> pageResults) {
        validateLayoutDataResults(actualForm.getPages(), readResults, pageResults, includeTextDetails);
        validatePageRangeData(expectedPage.getPage(), actualForm.getPageRange());
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

    private static void validateLabeledData(RecognizedForm actualForm, boolean includeTextDetails,
        List<ReadResult> readResults, DocumentResult documentResult) {

        validateLayoutDataResults(actualForm.getPages(), readResults, null, includeTextDetails);
        assertEquals(documentResult.getPageRange().get(0), actualForm.getPageRange().getStartPageNumber());
        assertEquals(documentResult.getPageRange().get(1), actualForm.getPageRange().getEndPageNumber());
        documentResult.getFields().forEach((label, expectedFieldValue) -> {
            final FormField<?> actualFormField = actualForm.getFields().get(label);
            assertEquals(label, actualFormField.getName());
            assertEquals(expectedFieldValue.getPage(), actualFormField.getPageNumber());
            if (expectedFieldValue.getConfidence() != null) {
                assertEquals(expectedFieldValue.getConfidence(), actualFormField.getConfidence());
            } else {
                assertEquals(1.0f, actualFormField.getConfidence());
            }
            validateFieldValueTransforms(expectedFieldValue, actualFormField, readResults, includeTextDetails);
        });
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
                    assertEquals(expectedFieldValue.getValueTime(), actualFormField.getFieldValue());
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

    private static void validatePageRangeData(int expectedPageInfo, PageRange actualPageInfo) {
        assertEquals(expectedPageInfo, actualPageInfo.getStartPageNumber());
        assertEquals(expectedPageInfo, actualPageInfo.getEndPageNumber());
    }

    static void validateUSReceiptData(USReceipt actualRecognizedReceipt, AnalyzeResult analyzeResult,
        boolean includeTextDetails) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        DocumentResult documentResult = analyzeResult.getDocumentResults().get(0);
        final Map<String, FieldValue> expectedReceiptFields = documentResult.getFields();
        validatePageRangeData(documentResult.getPageRange().get(0),
            actualRecognizedReceipt.getRecognizedForm().getPageRange());
        validatePageRangeData(documentResult.getPageRange().get(1),
            actualRecognizedReceipt.getRecognizedForm().getPageRange());
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
        validateFieldValueTransforms(expectedReceiptFields.get("Total"),
            actualRecognizedReceipt.getTotal(), readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("Subtotal"),
            actualRecognizedReceipt.getSubtotal(), readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("Tax"),
            actualRecognizedReceipt.getTax(), readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("TransactionDate"),
            actualRecognizedReceipt.getTransactionDate(),
            readResults, includeTextDetails);
        validateFieldValueTransforms(expectedReceiptFields.get("TransactionTime"),
            actualRecognizedReceipt.getTransactionTime(),
            readResults, includeTextDetails);
        validateReceiptItemsData(expectedReceiptFields.get("Items").getValueArray(),
            actualRecognizedReceipt.getReceiptItems(), readResults, includeTextDetails);
    }

    private static void validateReceiptItemsData(List<FieldValue> expectedReceiptItemList,
        List<USReceiptItem> actualReceiptItems, List<ReadResult> readResults, boolean includeTextDetails) {
        List<USReceiptItem> actualReceiptItemList = new ArrayList<>(actualReceiptItems);
        assertEquals(expectedReceiptItemList.size(), actualReceiptItemList.size());
        for (int i = 0; i < expectedReceiptItemList.size(); i++) {
            FieldValue expectedReceiptItem = expectedReceiptItemList.get(i);
            USReceiptItem actualReceiptItem = actualReceiptItemList.get(i);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.NAME.toString()),
                actualReceiptItem.getName(), readResults, includeTextDetails);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.QUANTITY.toString()), actualReceiptItem.getQuantity(), readResults, includeTextDetails);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.TOTAL_PRICE.toString()), actualReceiptItem.getTotalPrice(), readResults, includeTextDetails);
            validateFieldValueTransforms(expectedReceiptItem.getValueObject().get(ReceiptItemType.PRICE.toString()), actualReceiptItem.getPrice(), readResults, includeTextDetails);
        }
    }

    @Test
    abstract void recognizeReceiptSourceUrl();

    @Test
    abstract void recognizeReceiptSourceUrlTextDetails();

    @Test
    abstract void recognizeReceiptData();

    @Test
    abstract void recognizeReceiptDataTextDetails();

    @Test
    abstract void recognizeReceiptDataTextDetailsWithNullData();

    @Test
    abstract void recognizeReceiptDataWithContentTypeAutoDetection();

    @Test
    abstract void recognizeLayoutData();

    @Test
    abstract void recognizeLayoutDataWithNullData();

    @Test
    abstract void recognizeLayoutDataWithContentTypeAutoDetection();

    @Test
    abstract void recognizeLayoutSourceUrl();

    @Test
    abstract void recognizeLayoutInvalidSourceUrl();

    @Test
    abstract void recognizeCustomFormLabeledData();

    @Test
    abstract void recognizeCustomFormUnlabeledData();

    @Test
    abstract void recognizeCustomFormLabeledDataWithNullValues();

    @Test
    abstract void recognizeCustomFormLabeledDataWithContentTypeAutoDetection();

    @Test
    abstract void recognizeCustomFormInvalidSourceUrl();

    void receiptSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.RECEIPT_URL);
    }

    void receiptSourceUrlRunnerTextDetails(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(TestUtils.RECEIPT_URL, true);
    }

    void receiptDataRunner(Consumer<InputStream> testRunner) {
        testRunner.accept(getFileData(RECEIPT_LOCAL_URL));
    }

    void receiptDataRunnerTextDetails(BiConsumer<InputStream, Boolean> testRunner) {
        testRunner.accept(getFileData(RECEIPT_LOCAL_URL), true);
    }

    void invalidSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_RECEIPT_URL);
    }

    void layoutDataRunner(Consumer<InputStream> testRunner) {
        testRunner.accept(getFileData(LAYOUT_LOCAL_URL));
    }

    void layoutSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.LAYOUT_URL);
    }

    void customFormDataRunner(Consumer<InputStream> testRunner) {
        testRunner.accept(getFileData(TestUtils.FORM_LOCAL_URL));
    }

    void beginTrainingUnlabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(createStorageAndGenerateSas("src/test/resources/sample_files/Train"), false);
    }

    void beginTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(createStorageAndGenerateSas("src/test/resources/sample_files/TrainLabeled"), true);
    }

    protected <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        // TODO: #9252 AAD not supported by service
        // TokenCredential credential = null;
        AzureKeyCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new AzureKeyCredential(getApiKey());
        }

        HttpClient httpClient;
        Configuration buildConfiguration = Configuration.getGlobalConfiguration().clone();

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion,
            buildConfiguration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        if (credential != null) {
            policies.add(new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY, credential));
        }

        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            httpClient = new NettyAsyncHttpClientBuilder().wiretap(true).build();
        }
        policies.add(interceptorManager.getRecordPolicy());

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    /**
     * Get the string of API key value based on what running mode is on.
     *
     * @return the API key string
     */
    String getApiKey() {
        return interceptorManager.isPlaybackMode() ? "apiKeyInPlayback"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_API_KEY);
    }

    protected String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_ENDPOINT");
    }

    private String createStorageAndGenerateSas(String folderPath) {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            return Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL);
        }
    }
}
