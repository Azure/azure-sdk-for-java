package com.azure.core.util.polling;

import com.azure.core.util.polling.implementation.LongRunningOperation;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

public class AsyncPollResponse<PollResultT, FinalResultT> {
    private final LongRunningOperation.State<PollResultT> state;
    private final Function<PollResponse<PollResultT>, Mono<PollResultT>> cancellationOperation;
    private final FinalResultT result;
    private final PollResponse<PollResultT> inner;

    public static <PollResultT, FinalResultT>
        AsyncPollResponse<PollResultT, FinalResultT> from(LongRunningOperation.State<PollResultT> state,
                                              Function<PollResponse<PollResultT>, Mono<PollResultT>> cancellationOp) {
        return new AsyncPollResponse<>(state, cancellationOp, null);
    }

    public static <PollResultT, FinalResultT>
    AsyncPollResponse<PollResultT, FinalResultT> from(LongRunningOperation.State<PollResultT> state,
                                                      Function<PollResponse<PollResultT>, Mono<PollResultT>> cancellationOp,
                                                      FinalResultT result) {
        return new AsyncPollResponse<>(state, cancellationOp, result);
    }

    /**
     * @return a Mono, upon subscription it cancel the server operation and polling.
     */
    public Mono<PollResultT> cancel() {
        return Mono.defer(() -> {
            this.state.markCancelled();
            if (this.cancellationOperation != null) {
                return this.cancellationOperation.apply(this.state.getLastResponse());
            } else {
                return Mono.empty();
            }
        });
    }

    public FinalResultT getValue() {
        return this.result;
    }

    public PollResponse.OperationStatus getStatus() {
        return this.inner.getStatus();
    }

    public Map<Object, Object> getProperties() {
        return this.inner.getProperties();
    }

    private AsyncPollResponse(LongRunningOperation.State<PollResultT> state,
                             Function<PollResponse<PollResultT>, Mono<PollResultT>> cancellationOperation,
                             FinalResultT result) {
        this.state = state;
        this.cancellationOperation = cancellationOperation;
        this.result = result;
        this.inner = this.state.getLastResponse();
    }
}

