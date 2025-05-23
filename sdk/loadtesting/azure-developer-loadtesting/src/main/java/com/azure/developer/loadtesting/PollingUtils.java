// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.developer.loadtesting;

import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.developer.loadtesting.models.FileValidationStatus;
import com.azure.developer.loadtesting.models.LoadTestRun;
import com.azure.developer.loadtesting.models.TestFileInfo;
import com.azure.developer.loadtesting.models.TestProfileRun;
import com.azure.developer.loadtesting.models.TestProfileRunStatus;
import com.azure.developer.loadtesting.models.TestRunStatus;
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
        String validationStatus;

        try (JsonReader jsonReader = JsonProviders.createReader(fileBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            validationStatus = jsonTree.get("validationStatus").toString();
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
                lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                break;

            default:
                lroStatus = LongRunningOperationStatus.NOT_STARTED;

                break;
        }

        return new PollResponse<>(lroStatus, fileBinary);
    }

    static PollResponse<TestFileInfo> getValidationStatus(TestFileInfo testFile) throws RuntimeException {
        LongRunningOperationStatus lroStatus;

        if (FileValidationStatus.VALIDATION_NOT_REQUIRED.equals(testFile.getValidationStatus())
            || FileValidationStatus.VALIDATION_SUCCESS.equals(testFile.getValidationStatus())) {
            lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (FileValidationStatus.VALIDATION_FAILURE.equals(testFile.getValidationStatus())) {
            lroStatus = LongRunningOperationStatus.FAILED;
        } else if (FileValidationStatus.VALIDATION_INITIATED.equals(testFile.getValidationStatus())) {
            lroStatus = LongRunningOperationStatus.IN_PROGRESS;
        } else if (FileValidationStatus.NOT_VALIDATED.equals(testFile.getValidationStatus())) {
            lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else {
            lroStatus = LongRunningOperationStatus.NOT_STARTED;
        }

        return new PollResponse<>(lroStatus, testFile);
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

    static PollResponse<LoadTestRun> getTestRunStatus(LoadTestRun loadTestRun) throws RuntimeException {

        LongRunningOperationStatus lroStatus;

        if (TestRunStatus.NOT_STARTED.equals(loadTestRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.NOT_STARTED;
        } else if (TestRunStatus.DONE.equals(loadTestRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (TestRunStatus.FAILED.equals(loadTestRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.FAILED;
        } else if (TestRunStatus.CANCELLED.equals(loadTestRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.USER_CANCELLED;
        } else {
            lroStatus = LongRunningOperationStatus.IN_PROGRESS;
        }

        return new PollResponse<>(lroStatus, loadTestRun);
    }

    static PollResponse<BinaryData> getTestProfileRunStatus(BinaryData testProfileRunBinary) throws RuntimeException {
        String status;

        try (JsonReader jsonReader = JsonProviders.createReader(testProfileRunBinary.toBytes())) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);

            status = jsonTree.get("status").toString();
        } catch (IOException e) {
            throw new RuntimeException("Encountered exception while retrieving test profile run status", e);
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

        return new PollResponse<>(lroStatus, testProfileRunBinary);
    }

    static PollResponse<TestProfileRun> getTestProfileRunStatus(TestProfileRun testProfileRun) throws RuntimeException {
        LongRunningOperationStatus lroStatus;

        if (TestProfileRunStatus.NOT_STARTED.equals(testProfileRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.NOT_STARTED;
        } else if (TestProfileRunStatus.DONE.equals(testProfileRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (TestProfileRunStatus.FAILED.equals(testProfileRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.FAILED;
        } else if (TestProfileRunStatus.CANCELLED.equals(testProfileRun.getStatus())) {
            lroStatus = LongRunningOperationStatus.USER_CANCELLED;
        } else {
            lroStatus = LongRunningOperationStatus.IN_PROGRESS;
        }

        return new PollResponse<>(lroStatus, testProfileRun);
    }
}
