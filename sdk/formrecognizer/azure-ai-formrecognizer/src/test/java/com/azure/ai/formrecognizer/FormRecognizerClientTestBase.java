// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

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
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.FormRecognizerClientBuilder.OCP_APIM_SUBSCRIPTION_KEY;
import static com.azure.ai.formrecognizer.TestUtils.LABELED_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.LAYOUT_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.RECEIPT_LOCAL_URL;
import static com.azure.ai.formrecognizer.TestUtils.VALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.getFileData;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class FormRecognizerClientTestBase extends TestBase {
    private static final String AZURE_FORM_RECOGNIZER_API_KEY = "AZURE_FORM_RECOGNIZER_API_KEY";
    private static final String NAME = "name";
    private static final String FORM_RECOGNIZER_PROPERTIES = "azure-ai-formrecognizer.properties";
    private static final String VERSION = "version";

    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final Map<String, String> properties = CoreUtils.getProperties(FORM_RECOGNIZER_PROPERTIES);
    private final String clientName = properties.getOrDefault(NAME, "UnknownName");
    private final String clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");

    static void validateLayoutResult(IterableStream<FormPage> expectedFormPages,
        IterableStream<FormPage> actualFormPages) {
        List<FormPage> expectedPageList = expectedFormPages.stream().collect(Collectors.toList());
        List<FormPage> actualPageList = actualFormPages.stream().collect(Collectors.toList());
        assertEquals(expectedPageList.size(), actualPageList.size());
        for (int i = 0; i < actualPageList.size(); i++) {
            validateFormPage(expectedPageList.get(i), actualPageList.get(i));
        }
    }

    static void validateReceiptResult(boolean includeTextDetails, IterableStream<RecognizedReceipt> expectedReceipts,
        IterableStream<RecognizedReceipt> actualResult) {
        List<RecognizedReceipt> expectedReceiptList = expectedReceipts.stream().collect(Collectors.toList());
        List<RecognizedReceipt> actualReceiptList = actualResult.stream().collect(Collectors.toList());
        assertEquals(expectedReceiptList.size(), actualReceiptList.size());
        for (int i = 0; i < actualReceiptList.size(); i++) {
            validateReceipt(expectedReceiptList.get(i), actualReceiptList.get(i), includeTextDetails);
        }
    }

    static void validateRecognizedFormResult(IterableStream<RecognizedForm> expectedForms, IterableStream<RecognizedForm> actualForms) {
        List<RecognizedForm> expectedFormList = expectedForms.stream().collect(Collectors.toList());
        List<RecognizedForm> actualFormList = actualForms.stream().collect(Collectors.toList());

        assertEquals(expectedFormList.size(), actualFormList.size());
        for (int i = 0; i < actualFormList.size(); i++) {
            validateRecognizedForm(expectedFormList.get(i), actualFormList.get(i));
        }
    }

    static void validateUSReceipt(USReceipt expectedReceipt, USReceipt actualRecognizedReceipt,
        boolean includeTextDetails) {
        assertEquals(expectedReceipt.getReceiptType().getType(), actualRecognizedReceipt.getReceiptType().getType());
        assertEquals(expectedReceipt.getReceiptType().getConfidence(), actualRecognizedReceipt.getReceiptType().getConfidence());
        validateFieldValue(expectedReceipt.getMerchantName(), actualRecognizedReceipt.getMerchantName(), includeTextDetails);
        validateFieldValue(expectedReceipt.getMerchantPhoneNumber(), actualRecognizedReceipt.getMerchantPhoneNumber(), includeTextDetails);
        validateFieldValue(expectedReceipt.getMerchantAddress(), actualRecognizedReceipt.getMerchantAddress(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTotal(), actualRecognizedReceipt.getTotal(), includeTextDetails);
        validateFieldValue(expectedReceipt.getSubtotal(), actualRecognizedReceipt.getSubtotal(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTax(), actualRecognizedReceipt.getTax(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTip(), actualRecognizedReceipt.getTip(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTransactionDate(), actualRecognizedReceipt.getTransactionDate(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTransactionTime(), actualRecognizedReceipt.getTransactionTime(), includeTextDetails);
        validateReceiptItems(expectedReceipt.getReceiptItems(), actualRecognizedReceipt.getReceiptItems(), includeTextDetails);
    }

    @Test
    abstract void extractReceiptSourceUrl();

    @Test
    abstract void extractReceiptSourceUrlTextDetails();

    @Test
    abstract void extractReceiptData();

    @Test
    abstract void extractReceiptDataTextDetails();

    @Test
    abstract void extractLayoutValidSourceUrl();

    @Test
    abstract void extractLayoutInValidSourceUrl();

    @Test
    abstract void extractCustomFormValidSourceUrl();

    @Test
    abstract void extractCustomFormLabeledData();

    @Test
    abstract void extractCustomFormInValidSourceUrl();

    void receiptSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.RECEIPT_URL);
    }

    void receiptSourceUrlRunnerTextDetails(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(TestUtils.RECEIPT_URL, true);
    }

    void receiptDataRunner(Consumer<Mono<InputStream>> testRunner) {
        testRunner.accept(getFileData(RECEIPT_LOCAL_URL));
    }

    void receiptDataRunnerTextDetails(BiConsumer<Mono<InputStream>, Boolean> testRunner) {
        testRunner.accept(getFileData(RECEIPT_LOCAL_URL), true);
    }

    void invalidSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_RECEIPT_URL);
    }

    void layoutValidSourceUrlRunner(Consumer<Mono<InputStream>> testRunner) {
        testRunner.accept(getFileData(LAYOUT_LOCAL_URL));
    }

    void customFormValidSourceUrlRunner(BiConsumer<Mono<InputStream>, String> testRunner) {
        testRunner.accept(getFileData(TestUtils.FORM_LOCAL_URL), VALID_MODEL_ID);
    }

    void customFormLabeledDataRunner(BiConsumer<Mono<InputStream>, String> testRunner) {
        testRunner.accept(getFileData(TestUtils.FORM_LOCAL_URL), LABELED_MODEL_ID);
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
        policies.add(new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion, buildConfiguration));
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

    private static void validateFormPage(FormPage expectedFormPage, FormPage actualFormPage) {
        assertEquals(expectedFormPage.getHeight(), actualFormPage.getHeight());
        assertEquals(expectedFormPage.getWidth(), actualFormPage.getWidth());
        assertEquals(expectedFormPage.getUnit(), actualFormPage.getUnit());
        assertEquals(expectedFormPage.getTextAngle(), actualFormPage.getTextAngle());
        validateFormLine(expectedFormPage.getLines(), actualFormPage.getLines());
        validateFormTable(expectedFormPage.getTables(), actualFormPage.getTables());
    }

    private static void validateFormTable(IterableStream<FormTable> expectedFormTables, IterableStream<FormTable> actualFormTables) {
        List<FormTable> expectedFormTable = expectedFormTables.stream().collect(Collectors.toList());
        List<FormTable> actualFormTable = actualFormTables.stream().collect(Collectors.toList());
        assertEquals(expectedFormTable.size(), actualFormTable.size());
        for (int i = 0; i < actualFormTable.size(); i++) {
            FormTable expectedTable = expectedFormTable.get(i);
            FormTable actualTable = actualFormTable.get(i);
            assertEquals(expectedTable.getColumnCount(), actualTable.getColumnCount());
            validateCells(expectedTable.getCells(), actualTable.getCells());
            assertEquals(expectedTable.getRowCount(), actualTable.getRowCount());
        }
    }

    private static void validateReceipt(RecognizedReceipt expectedReceipt, RecognizedReceipt actualReceipt, boolean includeTextDetails) {
        assertEquals(expectedReceipt.getReceiptLocale(), actualReceipt.getReceiptLocale());
        validateRecognizedForm(expectedReceipt.getRecognizedForm(), actualReceipt.getRecognizedForm());
    }

    private static void validateCells(IterableStream<FormTableCell> expectedTableCells, IterableStream<FormTableCell> actualTableCells) {
        List<FormTableCell> expectedTableCellList = expectedTableCells.stream().collect(Collectors.toList());
        List<FormTableCell> actualTableCellList = actualTableCells.stream().collect(Collectors.toList());
        assertEquals(expectedTableCellList.size(), actualTableCellList.size());
        for (int i = 0; i < actualTableCellList.size(); i++) {
            FormTableCell expectedTableCell = expectedTableCellList.get(i);
            FormTableCell actualTableCell = actualTableCellList.get(i);
            assertEquals(expectedTableCell.getColumnIndex(), actualTableCell.getColumnIndex());
            assertEquals(expectedTableCell.getColumnSpan(), actualTableCell.getColumnSpan());
            assertEquals(expectedTableCell.getRowIndex(), actualTableCell.getRowIndex());
            assertEquals(expectedTableCell.getRowSpan(), actualTableCell.getRowSpan());
            validateBoundingBox(expectedTableCell.getBoundingBox(), actualTableCell.getBoundingBox());
        }
    }

    private static void validateFormLine(IterableStream<FormLine> expectedFormLines, IterableStream<FormLine> actualFormLines) {
        List<FormLine> expectedLineList = expectedFormLines.stream().collect(Collectors.toList());
        List<FormLine> actualLineList = actualFormLines.stream().collect(Collectors.toList());
        assertEquals(expectedLineList.size(), actualLineList.size());
        for (int i = 0; i < actualLineList.size(); i++) {
            FormLine expectedLine = expectedLineList.get(i);
            FormLine actualLine = actualLineList.get(i);
            assertEquals(expectedLine.getText(), actualLine.getText());
            validateBoundingBox(expectedLine.getBoundingBox(), actualLine.getBoundingBox());
            assertEquals(expectedLine.getPageNumber(), actualLine.getPageNumber());
            validateFormWord(expectedLine.getFormWords(), actualLine.getFormWords());
        }
    }

    private static void validateFormWord(IterableStream<FormWord> expectedFormWords, IterableStream<FormWord> actualFormWords) {
        List<FormWord> expectedFormWordList = expectedFormWords.stream().collect(Collectors.toList());
        List<FormWord> actualFormWordList = actualFormWords.stream().collect(Collectors.toList());
        assertEquals(expectedFormWordList.size(), actualFormWordList.size());
        for (int i = 0; i < actualFormWordList.size(); i++) {

            FormWord expectedWord = expectedFormWordList.get(i);
            FormWord actualWord = actualFormWordList.get(i);
            assertEquals(expectedWord.getText(), actualWord.getText());
            validateBoundingBox(expectedWord.getBoundingBox(), actualWord.getBoundingBox());
            assertEquals(expectedWord.getPageNumber(), actualWord.getPageNumber());
            assertEquals(expectedWord.getConfidence(), actualWord.getConfidence());
        }
    }

    private static void validateRecognizedForm(RecognizedForm expectedForm, RecognizedForm actualForm) {
        assertEquals(expectedForm.getFormType(), actualForm.getFormType());
        validatePageRange(expectedForm.getPageRange(), actualForm.getPageRange());
        validateLayoutResult(expectedForm.getPages(), actualForm.getPages());
        validateFieldMap(expectedForm.getFields(), actualForm.getFields());
    }

    private static void validateFieldMap(Map<String, FormField<?>> expectedFieldMap, Map<String, FormField<?>> actualFieldMap) {
        assertEquals(expectedFieldMap.size(), actualFieldMap.size());
        expectedFieldMap.entrySet().stream()
            .allMatch(e -> e.getValue().equals(actualFieldMap.get(e.getKey())));
    }

    private static void validateReceiptItems(List<USReceiptItem> expectedReceiptItems, List<USReceiptItem> actualReceiptItems, boolean includeTextDetails) {
        List<USReceiptItem> expectedReceiptItemList = expectedReceiptItems.stream().collect(Collectors.toList());
        List<USReceiptItem> actualReceiptItemList = actualReceiptItems.stream().collect(Collectors.toList());
        assertEquals(expectedReceiptItemList.size(), actualReceiptItemList.size());
        for (int i = 0; i < expectedReceiptItemList.size(); i++) {
            USReceiptItem expectedReceiptItem = expectedReceiptItemList.get(i);
            USReceiptItem actualReceiptItem = actualReceiptItemList.get(i);
            validateFieldValue(expectedReceiptItem.getName(), actualReceiptItem.getName(), includeTextDetails);
            validateFieldValue(expectedReceiptItem.getQuantity(), actualReceiptItem.getQuantity(), includeTextDetails);
            validateFieldValue(expectedReceiptItem.getTotalPrice(), actualReceiptItem.getTotalPrice(), includeTextDetails);
        }
    }

    private static void validateFieldValue(FormField<?> actualFieldValue, FormField<?> expectedFieldValue, boolean includeTextDetails) {
        assertEquals(expectedFieldValue.getFieldValue(), actualFieldValue.getFieldValue());
        assertEquals(expectedFieldValue.getName(), actualFieldValue.getName());
        if (includeTextDetails) {
            if (expectedFieldValue.getLabelText() != null && actualFieldValue.getLabelText() != null) {
                validateReferenceElements(expectedFieldValue.getLabelText().getTextContent(), actualFieldValue.getLabelText().getTextContent());
            }
            validateReferenceElements(expectedFieldValue.getValueText().getTextContent(), actualFieldValue.getValueText().getTextContent());

        }
    }

    private static void validateReferenceElements(IterableStream<FormContent> expectedElementStream, IterableStream<FormContent> actualElementStream) {
        if (expectedElementStream != null && actualElementStream != null) {
            List<FormContent> expectedFormContentList = expectedElementStream.stream().collect(Collectors.toList());
            List<FormContent> actualFormContentList = actualElementStream.stream().collect(Collectors.toList());
            assertEquals(expectedFormContentList.size(), actualFormContentList.size());
            for (int i = 0; i < actualFormContentList.size(); i++) {
                FormContent actualFormContent = actualFormContentList.get(i);
                FormContent expectedFormContent = expectedFormContentList.get(i);
                assertEquals(expectedFormContent.getTextContentType(), actualFormContent.getTextContentType());
                if (actualFormContentList.get(i).getTextContentType().equals(TextContentType.LINE)) {
                    FormLine actualFormLine = (FormLine) actualFormContent;
                    FormLine expectedFormLine = (FormLine) expectedFormContent;
                    validateFormWord(expectedFormLine.getFormWords(), actualFormLine.getFormWords());
                }
                assertEquals(expectedFormContent.getText(), actualFormContent.getText());
                validateBoundingBox(expectedFormContent.getBoundingBox(), actualFormContent.getBoundingBox());
                assertEquals(expectedFormContent.getPageNumber(), actualFormContent.getPageNumber());
            }
        }
    }

    private static void validateBoundingBox(BoundingBox expectedPoints, BoundingBox actualPoints) {
        if (expectedPoints.getPoints() != null && actualPoints.getPoints() != null) {
            assertEquals(expectedPoints.getPoints().size(), actualPoints.getPoints().size());
            for (int i = 0; i < actualPoints.getPoints().size(); i++) {
                Point expectedPoint = expectedPoints.getPoints().get(i);
                Point actualPoint = actualPoints.getPoints().get(i);
                assertEquals(expectedPoint.getX(), actualPoint.getX());
                assertEquals(expectedPoint.getY(), actualPoint.getY());
            }
        }
    }

    private static void validatePageRange(PageRange expectedPageInfo, PageRange actualPageInfo) {
        assertEquals(expectedPageInfo.getStartPageNumber(), actualPageInfo.getStartPageNumber());
        assertEquals(expectedPageInfo.getEndPageNumber(), actualPageInfo.getEndPageNumber());
    }
}
