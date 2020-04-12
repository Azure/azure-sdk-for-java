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
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.FormRecognizerClientBuilder.OCP_APIM_SUBSCRIPTION_KEY;
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
            validateFormPage(expectedPageList.get(i), actualPageList.get(i), i);
        }
    }

    private static void validateFormPage(FormPage expectedFormPage, FormPage actualFormPage, int i) {
        assertEquals(expectedFormPage.getHeight(), actualFormPage.getHeight());
        assertEquals(expectedFormPage.getWidth(), actualFormPage.getWidth());
        assertEquals(expectedFormPage.getUnit(), actualFormPage.getUnit());
        assertEquals(expectedFormPage.getTextAngle(), actualFormPage.getTextAngle());
        validateFormLine(expectedFormPage.getLines(), actualFormPage.getLines());
        validateFormTable(expectedFormPage.getTables(), actualFormPage.getTables());
    }

    private static void validateFormTable(List<FormTable> expectedFormTables, List<FormTable> actualFormTables) {
        assertEquals(expectedFormTables.size(), actualFormTables.size());
        for (int i = 0; i < actualFormTables.size(); i++) {

            FormTable expectedTable = expectedFormTables.get(i);
            FormTable actualTable = actualFormTables.get(i);
            assertEquals(expectedTable.getColumnCount(), actualTable.getColumnCount());
            validateCells(expectedTable.getCells(), actualTable.getCells());
            assertEquals(expectedTable.getRowCount(), actualTable.getRowCount());
        }
    }

    private static void validateCells(List<FormTableCell> expectedTableCells, List<FormTableCell> actualTableCells) {
        assertEquals(expectedTableCells.size(), actualTableCells.size());
        for (int i = 0; i < actualTableCells.size(); i++) {
            FormTableCell expectedTableCell = expectedTableCells.get(i);
            FormTableCell actualTableCell = actualTableCells.get(i);
            assertEquals(expectedTableCell.getColumnIndex(), actualTableCell.getColumnIndex());
            assertEquals(expectedTableCell.getColumnSpan(), actualTableCell.getColumnSpan());
            assertEquals(expectedTableCell.getRowIndex(), actualTableCell.getRowIndex());
            assertEquals(expectedTableCell.getRowSpan(), actualTableCell.getRowSpan());
            validateBoundingBox(expectedTableCell.getBoundingBox(), actualTableCell.getBoundingBox());
        }
    }

    private static void validateFormLine(List<FormLine> expectedFormLines, List<FormLine> actualFormLines) {
        assertEquals(expectedFormLines.size(), actualFormLines.size());
        for (int i = 0; i < actualFormLines.size(); i++) {
            FormLine expectedLine = expectedFormLines.get(i);
            FormLine actualLine = actualFormLines.get(i);
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

    static void validateReceiptResult(boolean includeTextDetails, IterableStream<RecognizedReceipt> expectedReceipts,
        IterableStream<RecognizedReceipt> actualResult) {
        List<RecognizedReceipt> expectedReceiptList = expectedReceipts.stream().collect(Collectors.toList());
        List<RecognizedReceipt> actualReceiptList = actualResult.stream().collect(Collectors.toList());

        assertEquals(expectedReceiptList.size(), actualReceiptList.size());
        for (int i = 0; i < actualReceiptList.size(); i++) {
            validateReceipt(expectedReceiptList.get(i), actualReceiptList.get(i), includeTextDetails);
        }
    }

    private static void validateReceipt(RecognizedReceipt expectedReceipt, RecognizedReceipt actualReceipt, boolean includeTextDetails) {
        assertEquals(expectedReceipt.getReceiptLocale(), actualReceipt.getReceiptLocale());
        validateRecognizedForm(expectedReceipt.getRecognizedForm(), actualReceipt.getRecognizedForm());
    }

    static void validateRecognizedFormResult(IterableStream<RecognizedForm> expectedForms, IterableStream<RecognizedForm> actualForms) {
        List<RecognizedForm> expectedFormList = expectedForms.stream().collect(Collectors.toList());
        List<RecognizedForm> actualFormList = actualForms.stream().collect(Collectors.toList());

        assertEquals(expectedFormList.size(), actualFormList.size());
        for (int i = 0; i < actualFormList.size(); i++) {
            validateRecognizedForm(expectedFormList.get(i), actualFormList.get(i));
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

    // private static void validateReceipt(RecognizedReceipt expectedReceipt, RecognizedReceipt actualRecognizedReceipt,
    //     boolean includeTextDetails) {
    //     validatePageRange(expectedReceipt.getPageRange(), actualRecognizedReceipt.getPageRange());
    //     assertEquals(expectedReceipt.getPageRange().getEndPageNumber(), actualRecognizedReceipt.getPageRange().getEndPageNumber());
    //     validatePages(expectedReceipt.getRecognizedForm().getPages(), actualRecognizedReceipt.getRecognizedForm().getPages());
    //     assertEquals(expectedPageInfo.getPageNumber(), actualPageInfo.getPageNumber());
    //     assertEquals(expectedPageInfo.getPageHeight(), actualPageInfo.getPageHeight());
    //     assertEquals(expectedPageInfo.getPageWidth(), actualPageInfo.getPageWidth());
    //     assertEquals(expectedPageInfo.getUnit(), actualPageInfo.getUnit());
    //     assertEquals(expectedPageInfo.getTextAngle(), actualPageInfo.getTextAngle());
    //     assertEquals(expectedReceipt.getReceiptType().getType(), actualRecognizedReceipt.getReceiptType().getType());
    //     assertEquals(expectedReceipt.getReceiptType().getConfidence(), actualRecognizedReceipt.getReceiptType().getConfidence());
    //     validateFieldValue(expectedReceipt.getMerchantName(), actualRecognizedReceipt.getMerchantName(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getMerchantPhoneNumber(), actualRecognizedReceipt.getMerchantPhoneNumber(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getMerchantAddress(), actualRecognizedReceipt.getMerchantAddress(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getTotal(), actualRecognizedReceipt.getTotal(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getSubtotal(), actualRecognizedReceipt.getSubtotal(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getTax(), actualRecognizedReceipt.getTax(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getTip(), actualRecognizedReceipt.getTip(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getTransactionDate(), actualRecognizedReceipt.getTransactionDate(), includeTextDetails);
    //     validateFieldValue(expectedReceipt.getTransactionTime(), actualRecognizedReceipt.getTransactionTime(), includeTextDetails);
    //     assertEquals(expectedReceipt.getReceiptItems().size(), expectedReceipt.getReceiptItems().size());
    //     validateReceiptItems(expectedReceipt.getReceiptItems(), actualRecognizedReceipt.getReceiptItems(), includeTextDetails);
    // }
    //
    // private static void validatePages(Iterable<FormPage> expectedPageStream, Iterable<FormPage> actualPageStream) {
    //     expectedPageList = expectedPageStream.
    // }

    private static void validateReceiptItems(List<USReceiptItem> actualReceiptItems, List<USReceiptItem> expectedReceiptItems, boolean includeTextDetails) {
        for (int i = 0; i < actualReceiptItems.size(); i++) {
            USReceiptItem expectedReceiptItem = expectedReceiptItems.get(i);
            USReceiptItem actualReceiptItem = actualReceiptItems.get(i);
            validateFieldValue(expectedReceiptItem.getName(), actualReceiptItem.getName(), includeTextDetails);
            validateFieldValue(expectedReceiptItem.getQuantity(), actualReceiptItem.getQuantity(), includeTextDetails);
            validateFieldValue(expectedReceiptItem.getTotalPrice(), actualReceiptItem.getTotalPrice(), includeTextDetails);
        }
    }

    private static void validateFieldValue(FormField<?> actualFieldValue, FormField<?> expectedFieldValue, boolean includeTextDetails) {
        assertEquals(expectedFieldValue.getFieldValue(), actualFieldValue.getFieldValue());
        assertEquals(expectedFieldValue.getName(), actualFieldValue.getName());
        if (includeTextDetails) {
            validateReferenceElements(expectedFieldValue.getLabelText().getTextContent(), actualFieldValue.getLabelText().getTextContent());
            validateReferenceElements(expectedFieldValue.getValueText().getTextContent(), actualFieldValue.getValueText().getTextContent());

        }
    }

    private static void validateReferenceElements(IterableStream<FormContent> expectedElementStream, IterableStream<FormContent> actualElementStream) {
        List<FormContent> expectedElements = expectedElementStream.stream().collect(Collectors.toList());
        List<FormContent> actualElements = actualElementStream.stream().collect(Collectors.toList());
        assertEquals(expectedElements.size(), actualElements.size());
        for (int i = 0; i < actualElements.size(); i++) {
            FormContent expectedElement = expectedElements.get(i);
            FormContent actualElement = actualElements.get(i);
            assertEquals(expectedElement.getText(), actualElement.getText());
            validateBoundingBox(expectedElement.getBoundingBox(), actualElement.getBoundingBox());
        }
    }

    private static void validateBoundingBox(BoundingBox expectedPoints, BoundingBox actualPoints) {
        assertEquals(expectedPoints.getPoints().size(), actualPoints.getPoints().size());
        for (int i = 0; i < expectedPoints.getPoints().size(); i++) {
            Point expectedPoint = expectedPoints.getPoints().get(i);
            Point actualPoint = actualPoints.getPoints().get(i);
            assertEquals(expectedPoint.getX(), actualPoint.getX());
            assertEquals(expectedPoint.getY(), actualPoint.getY());
        }
    }

    // private static void validatePageMetadata(PageMetadata expectedPageInfo, PageMetadata actualPageInfo) {
    //     assertEquals(expectedPageInfo.getPageNumber(), actualPageInfo.getPageNumber());
    //     assertEquals(expectedPageInfo.getPageHeight(), actualPageInfo.getPageHeight());
    //     assertEquals(expectedPageInfo.getPageWidth(), actualPageInfo.getPageWidth());
    //     assertEquals(expectedPageInfo.getUnit(), actualPageInfo.getUnit());
    //     assertEquals(expectedPageInfo.getTextAngle(), actualPageInfo.getTextAngle());
    // }


    private static void validatePageRange(PageRange expectedPageInfo, PageRange actualPageInfo) {
        assertEquals(expectedPageInfo.getStartPageNumber(), actualPageInfo.getStartPageNumber());
        assertEquals(expectedPageInfo.getEndPageNumber(), actualPageInfo.getEndPageNumber());
    }

    // Extract receipt
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
    abstract void extractCustomFormInValidSourceUrl();

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

    void layoutValidSourceUrlRunner(Consumer<InputStream> testRunner) {
        testRunner.accept(getFileData(TestUtils.LAYOUT_LOCAL_URL));
    }

    void customFormValidSourceUrlRunner(BiConsumer<InputStream, String> testRunner) {
        testRunner.accept(getFileData(TestUtils.FORM_LOCAL_URL), VALID_MODEL_ID);
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
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

    // /**
    //  * Create a client builder with endpoint and API key credential.
    //  *
    //  * @param endpoint the given endpoint
    //  * @param credential the given {@link FormRecognizerApiKeyCredential} credential
    //  *
    //  * @return {@link FormRecognizerClientBuilder}
    //  */
    // FormRecognizerClientBuilder createClientBuilder(String endpoint, AzureKeyCredential credential) {
    //     final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
    //         .apiKey(credential)
    //         .endpoint(endpoint);
    //
    //     if (interceptorManager.isPlaybackMode()) {
    //         clientBuilder.httpClient(interceptorManager.getPlaybackClient());
    //     } else {
    //         clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
    //             .addPolicy(interceptorManager.getRecordPolicy());
    //     }
    //
    //     return clientBuilder;
    // }

    /**
     * Get the string of API key value based on what running mode is on.
     *
     * @return the API key string
     */
    String getApiKey() {
        return interceptorManager.isPlaybackMode() ? "apiKeyInPlayback"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_API_KEY);
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_ENDPOINT");
    }
}
