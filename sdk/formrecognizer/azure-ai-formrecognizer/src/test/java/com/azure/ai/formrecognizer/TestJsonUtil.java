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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.ai.formrecognizer.Transforms.toReceipt;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * Contains helper methods for generating test data for test methods.
 */
final class TestJsonUtil {

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(df);

        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    static AnalyzeOperationResult getRawResponse(String filePath) {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
            return getObjectMapper().readValue(content, AnalyzeOperationResult.class);
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

    static List<RecognizedForm> getRawExpectedForms(boolean includeTextDetails) {
        String filePath = "src/test/resources/sample-files/receiptContent.json";
        return toRecognizedForm(getRawResponse(filePath).getAnalyzeResult(), includeTextDetails);
    }
}
