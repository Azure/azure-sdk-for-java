// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.AnalyzeOptions;
import com.azure.ai.contentunderstanding.models.ContentUnderstandingDefaults;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvenienceMethodValidationTest {
    private static final List<AnalysisInput> INPUTS
        = Collections.singletonList(new AnalysisInput().setUrl("https://example.com/document.pdf"));
    private static final BinaryData BINARY_INPUT = BinaryData.fromString("content");

    @Test
    public void syncClientValidatesAnalyzerIdBeforeSendingRequest() {
        ContentUnderstandingClient client = createBuilder().buildClient();

        assertNull("analyzerId", () -> client.beginAnalyze(null, INPUTS));
        assertEmptyAnalyzerId(() -> client.beginAnalyze("", INPUTS));
        assertNull("analyzerId", () -> client.beginAnalyze(null, INPUTS, new AnalyzeOptions()));
        assertEmptyAnalyzerId(() -> client.beginAnalyze("", INPUTS, new AnalyzeOptions()));
        assertNull("analyzerId", () -> client.beginAnalyzeBinary(null, BINARY_INPUT, new AnalyzeBinaryOptions()));
        assertEmptyAnalyzerId(() -> client.beginAnalyzeBinary("", BINARY_INPUT, new AnalyzeBinaryOptions()));
        assertNull("analyzerId", () -> client.analyzeInline(null, INPUTS));
        assertEmptyAnalyzerId(() -> client.analyzeInline("", INPUTS));
        assertNull("analyzerId", () -> client.analyzeInline(null, INPUTS, new AnalyzeOptions()));
        assertEmptyAnalyzerId(() -> client.analyzeInline("", INPUTS, new AnalyzeOptions()));
        assertNull("analyzerId", () -> client.analyzeBinaryInline(null, BINARY_INPUT));
        assertEmptyAnalyzerId(() -> client.analyzeBinaryInline("", BINARY_INPUT));
        assertNull("analyzerId", () -> client.analyzeBinaryInline(null, BINARY_INPUT, new AnalyzeBinaryOptions()));
        assertEmptyAnalyzerId(() -> client.analyzeBinaryInline("", BINARY_INPUT, new AnalyzeBinaryOptions()));
    }

    @Test
    public void asyncClientValidatesAnalyzerIdBeforeSendingRequest() {
        ContentUnderstandingAsyncClient client = createBuilder().buildAsyncClient();

        assertNull("analyzerId", () -> client.beginAnalyze(null, INPUTS));
        assertEmptyAnalyzerId(() -> client.beginAnalyze("", INPUTS));
        assertNull("analyzerId", () -> client.beginAnalyze(null, INPUTS, new AnalyzeOptions()));
        assertEmptyAnalyzerId(() -> client.beginAnalyze("", INPUTS, new AnalyzeOptions()));
        assertNull("analyzerId", () -> client.beginAnalyzeBinary(null, BINARY_INPUT, new AnalyzeBinaryOptions()));
        assertEmptyAnalyzerId(() -> client.beginAnalyzeBinary("", BINARY_INPUT, new AnalyzeBinaryOptions()));
        assertNull("analyzerId", () -> client.analyzeInline(null, INPUTS));
        assertEmptyAnalyzerId(() -> client.analyzeInline("", INPUTS));
        assertNull("analyzerId", () -> client.analyzeInline(null, INPUTS, new AnalyzeOptions()));
        assertEmptyAnalyzerId(() -> client.analyzeInline("", INPUTS, new AnalyzeOptions()));
        assertNull("analyzerId", () -> client.analyzeBinaryInline(null, BINARY_INPUT));
        assertEmptyAnalyzerId(() -> client.analyzeBinaryInline("", BINARY_INPUT));
        assertNull("analyzerId", () -> client.analyzeBinaryInline(null, BINARY_INPUT, new AnalyzeBinaryOptions()));
        assertEmptyAnalyzerId(() -> client.analyzeBinaryInline("", BINARY_INPUT, new AnalyzeBinaryOptions()));
    }

    @Test
    public void syncClientValidatesRequiredPayloadsBeforeSendingRequest() {
        ContentUnderstandingClient client = createBuilder().buildClient();

        assertNull("inputs", () -> client.beginAnalyze("analyzer", null));
        assertNull("inputs", () -> client.beginAnalyze("analyzer", null, new AnalyzeOptions()));
        assertNull("binaryInput", () -> client.beginAnalyzeBinary("analyzer", (BinaryData) null));
        assertNull("binaryInput", () -> client.beginAnalyzeBinary("analyzer", null, new AnalyzeBinaryOptions()));
        assertNull("inputs", () -> client.analyzeInline("analyzer", null));
        assertNull("inputs", () -> client.analyzeInline("analyzer", null, new AnalyzeOptions()));
        assertNull("binaryInput", () -> client.analyzeBinaryInline("analyzer", (BinaryData) null));
        assertNull("binaryInput", () -> client.analyzeBinaryInline("analyzer", null, new AnalyzeBinaryOptions()));
        assertNull("modelDeployments", () -> client.updateDefaults((Map<String, String>) null));
        assertNull("defaults", () -> client.updateDefaults((ContentUnderstandingDefaults) null));
    }

    @Test
    public void asyncClientValidatesRequiredPayloadsBeforeSendingRequest() {
        ContentUnderstandingAsyncClient client = createBuilder().buildAsyncClient();

        assertNull("inputs", () -> client.beginAnalyze("analyzer", null));
        assertNull("inputs", () -> client.beginAnalyze("analyzer", null, new AnalyzeOptions()));
        assertNull("binaryInput", () -> client.beginAnalyzeBinary("analyzer", (BinaryData) null));
        assertNull("binaryInput", () -> client.beginAnalyzeBinary("analyzer", null, new AnalyzeBinaryOptions()));
        assertNull("inputs", () -> client.analyzeInline("analyzer", null));
        assertNull("inputs", () -> client.analyzeInline("analyzer", null, new AnalyzeOptions()));
        assertNull("binaryInput", () -> client.analyzeBinaryInline("analyzer", (BinaryData) null));
        assertNull("binaryInput", () -> client.analyzeBinaryInline("analyzer", null, new AnalyzeBinaryOptions()));
        assertNull("modelDeployments", () -> client.updateDefaults((Map<String, String>) null));
        assertNull("defaults", () -> client.updateDefaults((ContentUnderstandingDefaults) null));
    }

    private static ContentUnderstandingClientBuilder createBuilder() {
        return new ContentUnderstandingClientBuilder().endpoint("https://example.com")
            .credential(new KeyCredential("fake-key"))
            .httpClient(request -> Mono.error(new AssertionError("Validation must occur before an HTTP request.")));
    }

    private static void assertNull(String parameterName, Executable executable) {
        NullPointerException exception = assertThrows(NullPointerException.class, executable);
        assertEquals("'" + parameterName + "' cannot be null.", exception.getMessage());
    }

    private static void assertEmptyAnalyzerId(Executable executable) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("'analyzerId' cannot be empty.", exception.getMessage());
    }
}
