// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.implementation.task.TaskSubmitter;
import com.azure.compute.batch.models.BatchCreateTaskCollectionResult;
import com.azure.compute.batch.models.BatchError;
import com.azure.compute.batch.models.BatchErrorException;
import com.azure.compute.batch.models.BatchTaskAddStatus;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.BatchTaskCreateResult;
import com.azure.compute.batch.models.BatchTaskGroup;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A fake implementation of {@link TaskSubmitter} used exclusively for unit testing.
 * <p>
 * This class simulates Batch service responses when adding tasks to a job, including:
 * <ul>
 *   <li>Client-side errors for specific task IDs.</li>
 *   <li>Server-side transient failures (retries before eventual success).</li>
 *   <li>HTTP 413 "RequestBodyTooLarge" errors to exercise chunk-splitting logic.</li>
 * </ul>
 * <p>
 * It also records metadata about submissions (per-task counts, maximum observed batch size, etc.)
 * so that tests can assert retry and splitting behavior.
 */
public final class FakeTaskSubmitter implements TaskSubmitter {

    private static final ClientLogger LOGGER = new ClientLogger(FakeTaskSubmitter.class); // <-- add this

    private final Set<String> clientErrorIds;
    private final Map<String, Integer> serverFailuresBeforeSuccess;
    private final int groupSizeLimitFor413;

    /**
     * Tracks the number of submission attempts per task ID.
     */
    private final ConcurrentHashMap<String, Integer> submissionCounts = new ConcurrentHashMap<>();

    /**
     * Tracks the maximum group size seen across <em>all</em> submissions, including failed ones.
     */
    private volatile int maxObservedGroupSize = 0;

    /**
     * Tracks the maximum group size that successfully returned a result (did not throw 413).
     */
    private volatile int maxObservedSuccessfulGroupSize = 0;

    /**
     * Creates a new {@link FakeTaskSubmitter}.
     *
     * @param clientErrorIds IDs of tasks that should consistently fail with {@code CLIENT_ERROR}.
     * @param serverFailuresBeforeSuccess Mapping of task ID → number of {@code SERVER_ERROR} responses before eventually succeeding. Values are decremented on each attempt.
     * @param groupSizeLimitFor413 If greater than zero, any batch larger than this will trigger an artificial {@code HttpResponseException} with status 413 to simulate payload-too-large errors.
     */
    public FakeTaskSubmitter(Set<String> clientErrorIds, Map<String, Integer> serverFailuresBeforeSuccess,
        int groupSizeLimitFor413) {
        this.clientErrorIds = clientErrorIds == null ? Collections.emptySet() : new HashSet<>(clientErrorIds);
        this.serverFailuresBeforeSuccess
            = serverFailuresBeforeSuccess == null ? new HashMap<>() : new HashMap<>(serverFailuresBeforeSuccess);
        this.groupSizeLimitFor413 = groupSizeLimitFor413;
    }

    // Helper methods used by tests

    /**
     * Gets the number of times a specific task ID has been submitted.
     *
     * @param taskId The ID of the task.
     * @return The number of submission attempts for that task.
     */
    public int getSubmissionCount(String taskId) {
        return submissionCounts.getOrDefault(taskId, 0);
    }

    /**
     * Gets the maximum group size observed across <em>all</em> submissions, including oversized ones
     * that failed with 413.
     *
     * @return The maximum attempted group size.
     */
    public int getMaxObservedGroupSize() {
        return maxObservedGroupSize;
    }

    /**
     * Gets the maximum group size that was successfully processed (i.e., did not trigger 413).
     *
     * @return The maximum successful group size.
     */
    public int getMaxObservedSuccessfulGroupSize() {
        return maxObservedSuccessfulGroupSize;
    }

    //  TaskSubmitter

    /**
     * Simulates submission of tasks to the Batch service.
     * <p>
     * Behavior:
     * <ul>
     *   <li>If the group size exceeds {@code groupSizeLimitFor413}, throws an {@link HttpResponseException} with status 413.</li>
     *   <li>If a task ID is in {@code clientErrorIds}, returns a result with {@code CLIENT_ERROR}.</li>
     *   <li>If a task ID has a positive entry in {@code serverFailuresBeforeSuccess}, decrements the counter
     *       and returns a result with {@code SERVER_ERROR}. Once the counter reaches zero, the task succeeds.</li>
     *   <li>Otherwise, tasks succeed with {@code SUCCESS}.</li>
     * </ul>
     * <p>
     * Updates per-task submission counts and max group size statistics for later assertions.
     *
     * @param jobId The job ID.
     * @param taskCollection The batch of tasks to submit.
     * @return A simulated {@link BatchCreateTaskCollectionResult}.
     * @throws HttpResponseException If the batch size exceeds {@code groupSizeLimitFor413}.
     */
    @Override
    public BatchCreateTaskCollectionResult submitTasks(String jobId, BatchTaskGroup taskCollection) {
        final List<BatchTaskCreateParameters> tasks = taskCollection.getValues();
        if (tasks == null || tasks.isEmpty()) {
            return newResult(Collections.emptyList());
        }

        // Record attempted group size even if we later throw 413.
        maxObservedGroupSize = Math.max(maxObservedGroupSize, tasks.size());

        // Simulate 413 (RequestBodyTooLarge).
        if (groupSizeLimitFor413 > 0 && tasks.size() > groupSizeLimitFor413) {
            HttpRequest req = new HttpRequest(HttpMethod.POST, "https://fake/batch/tasks:addCollection");
            HttpResponse resp = new HttpResponse(req) {
                @Override
                public int getStatusCode() {
                    return 413;
                }

                @Override
                public String getHeaderValue(String name) {
                    return null;
                }

                @Override
                public HttpHeaders getHeaders() {
                    return new HttpHeaders();
                }

                @Override
                public Flux<ByteBuffer> getBody() {
                    return Flux.empty();
                }

                @Override
                public Mono<byte[]> getBodyAsByteArray() {
                    return Mono.just(new byte[0]);
                }

                @Override
                public Mono<String> getBodyAsString() {
                    return Mono.just("");
                }

                @Override
                public Mono<String> getBodyAsString(Charset charset) {
                    return Mono.just("");
                }
            };
            throw LOGGER.logExceptionAsError(new BatchErrorException("RequestBodyTooLarge", resp, null));
        }

        final List<BatchTaskCreateResult> results = new ArrayList<>(tasks.size());
        for (BatchTaskCreateParameters t : tasks) {
            final String id = t.getId();
            submissionCounts.merge(id, 1, Integer::sum);

            if (clientErrorIds.contains(id)) {
                results.add(makeResult(BatchTaskAddStatus.CLIENT_ERROR, id, "InvalidTask"));
                continue;
            }

            Integer remaining = serverFailuresBeforeSuccess.get(id);
            if (remaining != null && remaining > 0) {
                serverFailuresBeforeSuccess.put(id, remaining - 1);
                results.add(makeResult(BatchTaskAddStatus.SERVER_ERROR, id, "InternalServerError"));
            } else {
                results.add(makeResult(BatchTaskAddStatus.SUCCESS, id, null));
            }
        }

        maxObservedSuccessfulGroupSize = Math.max(maxObservedSuccessfulGroupSize, tasks.size());
        return newResult(results);
    }

    // Since the constructors of BatchCreateTaskCollectionResult, BatchTaskCreateResult, BatchError are private/not visible, we must mock them here

    /**
     * Creates a {@link BatchCreateTaskCollectionResult} instance reflectively with the given results.
     * This is only for unit testing purposes.
     *
     * @param values The task results to include.
     * @return A {@link BatchCreateTaskCollectionResult} with those values.
     */
    private static BatchCreateTaskCollectionResult newResult(List<BatchTaskCreateResult> values) {
        try {
            Constructor<BatchCreateTaskCollectionResult> constructor
                = BatchCreateTaskCollectionResult.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            BatchCreateTaskCollectionResult result = constructor.newInstance();

            Field valuesField = BatchCreateTaskCollectionResult.class.getDeclaredField("values");
            valuesField.setAccessible(true);
            valuesField.set(result, values);
            return result;
        } catch (ReflectiveOperationException e) {
            throw LOGGER
                .logExceptionAsError(new RuntimeException("Failed to build BatchCreateTaskCollectionResult", e));
        }
    }

    /**
     * Creates a {@link BatchTaskCreateResult} reflectively with the specified status, task ID,
     * and optional error code. This is only for unit testing purposes.
     *
     * @param status The task add status.
     * @param taskId The task ID.
     * @param errorCode Optional error code for failures, or {@code null}.
     * @return A {@link BatchTaskCreateResult}.
     */
    private static BatchTaskCreateResult makeResult(BatchTaskAddStatus status, String taskId, String errorCode) {
        try {
            Constructor<BatchTaskCreateResult> constructor
                = BatchTaskCreateResult.class.getDeclaredConstructor(BatchTaskAddStatus.class, String.class);
            constructor.setAccessible(true);
            BatchTaskCreateResult result = constructor.newInstance(status, taskId);

            if (errorCode != null) {
                BatchError error = newBatchError(errorCode);
                Field errorField = BatchTaskCreateResult.class.getDeclaredField("error");
                errorField.setAccessible(true);
                errorField.set(result, error);
            }
            return result;
        } catch (ReflectiveOperationException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to build BatchTaskCreateResult", e));
        }
    }

    /**
     * Creates a {@link BatchError} reflectively for unit tests with only the {@code code} field set.
     * This is only for unit testing purposes.
     *
     * @param code The error code string.
     * @return A {@link BatchError} with the code set.
     */
    private static BatchError newBatchError(String code) {
        try {
            Constructor<BatchError> constructor = BatchError.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            BatchError batchError = constructor.newInstance();

            Field codeField = BatchError.class.getDeclaredField("code");
            codeField.setAccessible(true);
            codeField.set(batchError, code);
            return batchError;
        } catch (ReflectiveOperationException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to build BatchError", e));
        }
    }
}
