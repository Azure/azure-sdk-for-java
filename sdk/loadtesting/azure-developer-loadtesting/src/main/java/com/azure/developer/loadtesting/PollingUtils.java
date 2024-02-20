// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.developer.loadtesting;

import java.util.concurrent.Callable;

import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

final class PollingUtils {
    static <T> Mono<PollResponse<T>> getPollResponseMono(Callable<PollResponse<T>> pollOperation) {
        try {
            return Mono.just(pollOperation.call());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    static PollResponse<BinaryData> getValidationStatus(BinaryData fileBinary, ObjectMapper objectMapper) throws RuntimeException {
        String validationStatus, fileType;
        JsonNode file;
        try {
            file = objectMapper.readTree(fileBinary.toString());
            validationStatus = file.get("validationStatus").asText();
            fileType = file.get("fileType").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Encountered exception while retrieving validation status", e);
        }
        LongRunningOperationStatus lroStatus;
        switch (validationStatus) {
            case "VALIDATION_NOT_REQUIRED":
            case "VALIDATION_SUCCESS":
                lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;

            case "VALIDATION_FAILURE":
                lroStatus = LongRunningOperationStatus.FAILED;
                break;

            case "VALIDATION_INITIATED":
                lroStatus = LongRunningOperationStatus.IN_PROGRESS;
                break;

            case "NOT_VALIDATED":
                if ("JMX_FILE".equalsIgnoreCase(fileType)) {
                    lroStatus = LongRunningOperationStatus.NOT_STARTED;
                } else {
                    lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                }
                break;

            default:
                lroStatus = LongRunningOperationStatus.NOT_STARTED;
                break;
        }
        return new PollResponse<>(lroStatus, fileBinary);
    }

    static PollResponse<BinaryData> getTestRunStatus(BinaryData testRunBinary, ObjectMapper objectMapper) throws RuntimeException {
        String status;
        JsonNode testRun;
        try {
            testRun = objectMapper.readTree(testRunBinary.toString());
            status = testRun.get("status").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Encountered exception while retrieving test run status", e);
        }
        LongRunningOperationStatus lroStatus;
        switch (status) {
            case "NOTSTARTED":
                lroStatus = LongRunningOperationStatus.NOT_STARTED;
                break;

            case "DONE":
                lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;

            case "FAILED":
                lroStatus = LongRunningOperationStatus.FAILED;
                break;

            case "CANCELLED":
                lroStatus = LongRunningOperationStatus.USER_CANCELLED;
                break;

            default:
                lroStatus = LongRunningOperationStatus.IN_PROGRESS;
                break;
        }
        return new PollResponse<>(lroStatus, testRunBinary);
    }
}
