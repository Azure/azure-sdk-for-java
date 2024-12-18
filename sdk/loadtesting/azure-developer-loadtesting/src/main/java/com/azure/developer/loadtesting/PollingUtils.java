// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.developer.loadtesting;

import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

final class PollingUtils {
    static <T> Mono<PollResponse<T>> getPollResponseMono(Callable<PollResponse<T>> pollOperation) {
        try {
            return Mono.just(pollOperation.call());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    static PollResponse<BinaryData> getValidationStatus(BinaryData fileBinary) throws RuntimeException {
        String validationStatus, fileType;

        try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            validationStatus = jsonTree.get("validationStatus").toString();
            fileType = jsonTree.get("fileType").toString();
        } catch (IOException e) {
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

    static PollResponse<BinaryData> getTestRunStatus(BinaryData testRunBinary) throws RuntimeException {
        String status;

        try (JsonReader jsonReader = JsonProviders.createReader(testRunBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            status = jsonTree.get("status").toString();
        } catch (IOException e) {
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
