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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.azure.ai.formrecognizer.Transforms.toReceipt;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestJsonUtil {
    // protected static JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(df);

        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    static AnalyzeOperationResult getRawResponseContent() {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get("src/test/resources/sample-files/layoutContent.json")));
            return getObjectMapper().readValue(content, AnalyzeOperationResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static AnalyzeOperationResult getRawResponseReceipt() {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get("src/test/resources/sample-files/receiptContent.json")));
            return getObjectMapper().readValue(content, AnalyzeOperationResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static AnalyzeOperationResult getRawResponseForm() {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get("src/test/resources/sample-files/receiptContent.json")));
            return getObjectMapper().readValue(content, AnalyzeOperationResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<List<FormTable>> getPagedTables() {
        List<List<FormTable>> wholeListTables = new ArrayList<>();
        List<PageResult> pageResults = getRawResponseContent().getAnalyzeResult().getPageResults();
        for (int i = 0; i < pageResults.size(); i++) {
            wholeListTables.add(Transforms.getPageTables(pageResults.get(i), i + 1));
        }
        return wholeListTables;
    }

    static List<List<FormLine>> getPagedLines() {
        List<List<FormLine>> lines = new ArrayList<>();
        List<ReadResult> readResults = getRawResponseContent().getAnalyzeResult().getReadResults();
        for (int i = 0; i < readResults.size(); i++) {
            lines.add(Transforms.getReadResultFormLines(readResults.get(i)));
        }
        return lines;
    }

    static IterableStream<RecognizedReceipt> getRawExpectedReceipt(boolean includeTextDetails) {
        return toReceipt(getRawResponseReceipt().getAnalyzeResult(), includeTextDetails);
    }

    static List<RecognizedForm> getRawExpectedForms(boolean includeTextDetails) {
        return toRecognizedForm(getRawResponseForm().getAnalyzeResult(), includeTextDetails);
    }
}
