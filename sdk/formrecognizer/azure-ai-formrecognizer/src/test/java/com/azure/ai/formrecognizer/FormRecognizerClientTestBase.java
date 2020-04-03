// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.Element;
import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.PageMetadata;
import com.azure.ai.formrecognizer.models.ReceiptItem;
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

import java.io.IOException;
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
import static com.azure.ai.formrecognizer.TestUtils.getReceiptFileData;
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

    static void validateReceiptResult(boolean includeTextDetails, IterableStream<ExtractedReceipt> expectedReceipts,
                                      IterableStream<ExtractedReceipt> actualResult) {
        List<ExtractedReceipt> expectedReceiptList = expectedReceipts.stream().collect(Collectors.toList());
        List<ExtractedReceipt> actualReceiptList = actualResult.stream().collect(Collectors.toList());

        assertEquals(expectedReceiptList.size(), actualReceiptList.size());
        for (int i = 0; i < actualReceiptList.size(); i++) {
            validateReceipt(expectedReceiptList.get(i), actualReceiptList.get(i), includeTextDetails);
        }
    }
    // Extract receipt
    @Test
    abstract void extractReceiptSourceUrl();

    @Test
    abstract void extractReceiptSourceUrlTextDetails();

    @Test
    abstract void extractReceiptData() throws IOException;

    @Test
    abstract void extractReceiptDataTextDetails();

    void receiptSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.RECEIPT_URL);
    }

    void receiptSourceUrlRunnerTextDetails(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(TestUtils.RECEIPT_URL, true);
    }

    void receiptDataRunner(Consumer<InputStream> testRunner) {
        testRunner.accept(getReceiptFileData());
    }

    void receiptDataRunnerTextDetails(BiConsumer<InputStream, Boolean> testRunner) {
        testRunner.accept(getReceiptFileData(), true);
    }

    void receiptInvalidSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_RECEIPT_URL);
    }

    private static void validateReceipt(ExtractedReceipt expectedReceipt, ExtractedReceipt actualExtractedReceipt,
                                        boolean includeTextDetails) {
        assertEquals(expectedReceipt.getPageRange().getStartPageNumber(), actualExtractedReceipt.getPageRange().getStartPageNumber());
        assertEquals(expectedReceipt.getPageRange().getEndPageNumber(), actualExtractedReceipt.getPageRange().getEndPageNumber());
        validatePageMetadata(expectedReceipt.getPageMetadata(), actualExtractedReceipt.getPageMetadata());
        assertEquals(expectedReceipt.getReceiptType().getType(), actualExtractedReceipt.getReceiptType().getType());
        assertEquals(expectedReceipt.getReceiptType().getConfidence(), actualExtractedReceipt.getReceiptType().getConfidence());
        validateFieldValue(expectedReceipt.getMerchantName(), actualExtractedReceipt.getMerchantName(), includeTextDetails);
        validateFieldValue(expectedReceipt.getMerchantPhoneNumber(), actualExtractedReceipt.getMerchantPhoneNumber(), includeTextDetails);
        validateFieldValue(expectedReceipt.getMerchantAddress(), actualExtractedReceipt.getMerchantAddress(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTotal(), actualExtractedReceipt.getTotal(), includeTextDetails);
        validateFieldValue(expectedReceipt.getSubtotal(), actualExtractedReceipt.getSubtotal(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTax(), actualExtractedReceipt.getTax(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTip(), actualExtractedReceipt.getTip(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTransactionDate(), actualExtractedReceipt.getTransactionDate(), includeTextDetails);
        validateFieldValue(expectedReceipt.getTransactionTime(), actualExtractedReceipt.getTransactionTime(), includeTextDetails);
        assertEquals(expectedReceipt.getReceiptItems().size(), expectedReceipt.getReceiptItems().size());
        validateReceiptItems(expectedReceipt.getReceiptItems(), actualExtractedReceipt.getReceiptItems(), includeTextDetails);
    }

    private static void validateReceiptItems(List<ReceiptItem> actualReceiptItems, List<ReceiptItem> expectedReceiptItems, boolean includeTextDetails) {
        for (int i = 0; i < actualReceiptItems.size(); i++) {
            ReceiptItem expectedReceiptItem = expectedReceiptItems.get(i);
            ReceiptItem actualReceiptItem = actualReceiptItems.get(i);
            validateFieldValue(expectedReceiptItem.getName(), actualReceiptItem.getName(), includeTextDetails);
            validateFieldValue(expectedReceiptItem.getQuantity(), actualReceiptItem.getQuantity(), includeTextDetails);
            validateFieldValue(expectedReceiptItem.getTotalPrice(), actualReceiptItem.getTotalPrice(), includeTextDetails);
        }
    }

    private static void validateFieldValue(FieldValue<?> actualFieldValue, FieldValue<?> expectedFieldValue, boolean includeTextDetails) {
        if (actualFieldValue != null) {
            assertEquals(expectedFieldValue.getValue(), actualFieldValue.getValue());
            assertEquals(expectedFieldValue.getType(), actualFieldValue.getType());
            if (includeTextDetails) {
                assertEquals(expectedFieldValue.getElements().size(), actualFieldValue.getElements().size());
                validateReferenceElements(expectedFieldValue.getElements(), actualFieldValue.getElements());
            }
        }
    }

    private static void validateReferenceElements(List<Element> expectedElements, List<Element> actualElements) {
        for (int i = 0; i < actualElements.size(); i++) {
            Element expectedElement = expectedElements.get(i);
            Element actualElement = actualElements.get(i);
            assertEquals(expectedElement.getText(), actualElement.getText());
            validateBoundingBox(expectedElement.getBoundingBox(), actualElement.getBoundingBox());
        }
    }

    private static void validateBoundingBox(BoundingBox expectedPoints, BoundingBox actualPoints) {
        assertEquals(expectedPoints.getTopLeft().getX(), actualPoints.getTopLeft().getX());
        assertEquals(expectedPoints.getTopLeft().getY(), actualPoints.getTopLeft().getY());
        assertEquals(expectedPoints.getTopRight().getX(), actualPoints.getTopRight().getX());
        assertEquals(expectedPoints.getTopRight().getY(), actualPoints.getTopRight().getY());
        assertEquals(expectedPoints.getBottomRight().getX(), actualPoints.getBottomRight().getX());
        assertEquals(expectedPoints.getBottomRight().getY(), actualPoints.getBottomRight().getY());
        assertEquals(expectedPoints.getBottomLeft().getX(), actualPoints.getBottomLeft().getX());
        assertEquals(expectedPoints.getBottomLeft().getY(), actualPoints.getBottomLeft().getY());
    }

    private static void validatePageMetadata(PageMetadata expectedPageInfo, PageMetadata actualPageInfo) {
        assertEquals(expectedPageInfo.getPageNumber(), actualPageInfo.getPageNumber());
        assertEquals(expectedPageInfo.getPageHeight(), actualPageInfo.getPageHeight());
        assertEquals(expectedPageInfo.getPageWidth(), actualPageInfo.getPageWidth());
        assertEquals(expectedPageInfo.getUnit(), actualPageInfo.getUnit());
        assertEquals(expectedPageInfo.getTextAngle(), actualPageInfo.getTextAngle());
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

    /**
     * Create a client builder with endpoint and API key credential.
     *
     * @param endpoint the given endpoint
     * @param credential the given {@link FormRecognizerApiKeyCredential} credential
     *
     * @return {@link FormRecognizerClientBuilder}
     */
    FormRecognizerClientBuilder createClientBuilder(String endpoint, AzureKeyCredential credential) {
        final FormRecognizerClientBuilder clientBuilder = new FormRecognizerClientBuilder()
            .apiKey(credential)
            .endpoint(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            clientBuilder.httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        return clientBuilder;
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

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_ENDPOINT");
    }
}
