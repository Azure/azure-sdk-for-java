// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.FoundryFeaturesOptInKeys;
import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingStrategyOptions;

/**
 * Shared polling helpers for the Agents SDK.
 *
 * <p>The generated {@code OperationLocationPollingStrategy} / {@code SyncOperationLocationPollingStrategy}
 * delegate here so that the two strategies stay in sync and only minimal edits are needed in the
 * generated files.</p>
 *
 * <p>This class is package-private; it is <b>not</b> part of the public API.</p>
 */
final class AgentsServicePollUtils {

    /** Required preview-feature header for Memory Stores operations. */
    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");
    private static final String FOUNDRY_FEATURES_VALUE = FoundryFeaturesOptInKeys.MEMORY_STORES_V1_PREVIEW.toString();

    private AgentsServicePollUtils() {
    }

    /**
     * Adds the {@code Foundry-Features} header to the given {@link PollingStrategyOptions}'s
     * {@link Context}.  If the context already carries {@link HttpHeaders} under the
     * {@link AddHeadersFromContextPolicy} key they are preserved; the {@code Foundry-Features}
     * entry is merged in.  Because the pipeline already contains
     * {@link AddHeadersFromContextPolicy}, the header is automatically added to every HTTP
     * request the parent strategy makes (initial, poll, and final-result GETs).
     *
     * <p><strong>Note:</strong> this method mutates and returns the same
     * {@code PollingStrategyOptions} instance.</p>
     */
    static PollingStrategyOptions withFoundryFeatures(PollingStrategyOptions options) {
        Context context = options.getContext() != null ? options.getContext() : Context.NONE;
        Object existing = context.getData(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY).orElse(null);
        HttpHeaders headers
            = (existing instanceof HttpHeaders) ? new HttpHeaders((HttpHeaders) existing) : new HttpHeaders();
        headers.set(FOUNDRY_FEATURES, FOUNDRY_FEATURES_VALUE);
        return options.setContext(context.addData(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
    }

    /**
     * Remaps a {@link PollResponse} whose status may contain a custom service terminal state
     * ({@code "completed"}, {@code "superseded"}) that the base {@code OperationResourcePollingStrategy}
     * cannot recognize.  If no remapping is needed the original response is returned as-is.
     *
     * <p>The Memory Stores Azure core defines:</p>
     * <ul>
     *   <li>{@code "completed"} {@link LongRunningOperationStatus#SUCCESSFULLY_COMPLETED}</li>
     *   <li>{@code "superseded"}  {@link LongRunningOperationStatus#USER_CANCELLED}</li>
     * </ul>
     */
    static <T> PollResponse<T> remapStatus(PollResponse<T> response) {
        LongRunningOperationStatus status = response.getStatus();
        LongRunningOperationStatus mapped = mapCustomStatus(status);
        if (mapped == status) {
            return response;
        }
        return new PollResponse<>(mapped, response.getValue(), response.getRetryAfter());
    }

    private static LongRunningOperationStatus mapCustomStatus(LongRunningOperationStatus status) {
        // Standard statuses (Succeeded, Failed, Canceled, InProgress, NotStarted) are already
        // mapped correctly by the parent's PollResult; only remap the custom ones.
        String name = status.toString();
        if (MemoryStoreUpdateStatus.COMPLETED.toString().equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (MemoryStoreUpdateStatus.SUPERSEDED.toString().equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        }
        return status;
    }
}
