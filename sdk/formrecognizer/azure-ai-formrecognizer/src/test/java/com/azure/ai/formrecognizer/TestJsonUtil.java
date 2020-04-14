// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.PageResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.util.IterableStream;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.ai.formrecognizer.Transforms.toReceipt;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;

/**
 * Contains helper methods for generating test data for test methods.
 */
final class TestJsonUtil {

    private TestJsonUtil() {
    }

    public static SerializerAdapter getSerializerAdapter() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }

    static AnalyzeOperationResult getRawResponse(String filePath) {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
            return getSerializerAdapter().deserialize(content, AnalyzeOperationResult.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<List<FormTable>> getPagedTables() {
        String filePath = "src/test/resources/sample-files/layoutContent.json";
        List<PageResult> pageResults = getRawResponse(filePath).getAnalyzeResult().getPageResults();
        return IntStream.range(0, pageResults.size())
            .mapToObj(i -> Transforms.getPageTables(pageResults.get(i), i + 1))
            .collect(Collectors.toList());
    }

    static List<List<FormLine>> getPagedLines() {
        String filePath = "src/test/resources/sample-files/layoutContent.json";
        List<ReadResult> readResults = getRawResponse(filePath).getAnalyzeResult().getReadResults();
        return readResults.stream().map(Transforms::getReadResultFormLines).collect(Collectors.toList());
    }

    static IterableStream<RecognizedReceipt> getRawExpectedReceipt(boolean includeTextDetails) {
        String filePath = "src/test/resources/sample-files/receiptContent.json";
        return toReceipt(getRawResponse(filePath).getAnalyzeResult(), includeTextDetails);
    }

    static List<RecognizedForm> getRawExpectedForms() {
        String filePath = "src/test/resources/sample-files/customFormContent.json";
        return toRecognizedForm(getRawResponse(filePath).getAnalyzeResult(), false);
    }

    static List<RecognizedForm> getRawExpectedLabeledForms() {
        String filePath = "src/test/resources/sample-files/customFormLabeledContent.json";
        return toRecognizedForm(getRawResponse(filePath).getAnalyzeResult(), true);
    }
}
