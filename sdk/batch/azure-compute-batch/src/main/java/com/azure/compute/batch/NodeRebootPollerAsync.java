// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchNode;
import com.azure.compute.batch.models.BatchNodeState;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Async poller class used by {@code beginRebootNode} to implement polling logic for rebooting a {@link BatchNode}.
 * Returns {@link BatchNode} values during polling and the final {@link BatchNode} upon successful completion.
 */
public final class NodeRebootPollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String poolId;
    private final String nodeId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link NodeRebootPollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param poolId The ID of the pool that contains the node.
     * @param nodeId The ID of the node to reboot.
     * @param options Optional request options for service calls.
     */
    public NodeRebootPollerAsync(BatchAsyncClient batchAsyncClient, String poolId, String nodeId,
        RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.poolId = poolId;
        this.nodeId = nodeId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the reboot.
     *
     * @return A function that initiates the reboot call and returns a {@link PollResponse}
     *         with {@link LongRunningOperationStatus#IN_PROGRESS}.
     */
    public Function<PollingContext<BatchNode>, Mono<PollResponse<BatchNode>>> getActivationOperation() {
        return ctx -> batchAsyncClient.rebootNodeWithResponse(poolId, nodeId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation – watches the node until it leaves {@code rebooting} and reaches {@code idle} or {@code running}.
     *
     * @return A function that polls the node and returns a {@link PollResponse}
     *         reflecting the current long-running operation status.
     */
    public Function<PollingContext<BatchNode>, Mono<PollResponse<BatchNode>>> getPollOperation() {
        return ctx -> {
            RequestOptions pollOpts = new RequestOptions().setContext(requestContext);
            return batchAsyncClient.getNodeWithResponse(poolId, nodeId, pollOpts).map(resp -> {
                BatchNode node = resp.getValue().toObject(BatchNode.class);
                LongRunningOperationStatus status = node.getState() == BatchNodeState.REBOOTING
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                return new PollResponse<>(status, node);
            });
        };
    }

    /**
     * Cancel operation – not supported for reboot.
     *
     * @return A function that always returns an empty {@link Mono}, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchNode>, PollResponse<BatchNode>, Mono<BatchNode>> getCancelOperation() {
        return (ctx, resp) -> Mono.empty();
    }

    /**
     * Final-result fetch operation.
     *
     * @return A function that returns the final {@link BatchNode} from the latest poll response.
     */
    public Function<PollingContext<BatchNode>, Mono<BatchNode>> getFetchResultOperation() {
        return ctx -> Mono.justOrEmpty(ctx.getLatestResponse().getValue());
    }
}
