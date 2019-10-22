package com.azure.core.util.polling.implementation;

import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * INTERNAL UTILITY CLASS.
 * @param <PollResultT> the type of the polling response model
 * @param <FinalResultT> the type of the final response
 *
 *
 */
// This is an internal utility method to begin a Long running operation followed by polling.
//
//    Using LongRunningOperation::begin we can have following signature for storage beginBlobCopy
//
//    Flux<AsyncPollResponse<BlobCopyStatus, BlobCopyStatus>> beginBlobCopy(param);
//
//    Flux<AsyncPollResponse<BlobCopyStatus, BlobCopyStatus>> pollFlux = client.beginBlobCopy(params);
//
//    pollFlux
//       .takeUntil(pr -> pr.getStatus() == PollResponse.OperationStatus.IN_PROGRESS)
//       .block();
//
//    pollFlux
//       .timeout(Duration.ofMinutes(15))
//       .flatmap(pr ->
//           if(pr.getStatus() == PollResponse.OperationStatus.FAILED)) {
//              return pr.cancel();
//           } else {
//              return Mono.just(pr);
//           }
//       })
//       .block();
//
//
//    pollFlux
//       .flatmap(pr -> {
//            if(pr.getStatus() == PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED)) {
//               return Mono.just(pr.getValue());
//            } else {
//               return Mono.empty();
//       })
//       .block();
//
/**
 * INTERNAL UTILITY CLASS.
 **/
public class LongRunningOperation<PollResultT, FinalResultT> {
    private final Function<PollResponse<PollResultT>, Mono<PollResponse<PollResultT>>> pollOperation;
    private final Function<PollResponse<PollResultT>, Mono<PollResultT>> cancelOperation;
    private final Supplier<Mono<FinalResultT>> fetchResultOperation;
    //
    private volatile PollResponse<PollResultT> lroStartResponse;
    //
    private volatile int lroStartFlag = 0;
    private final AtomicIntegerFieldUpdater<LongRunningOperation> guardLroStartCall =
        AtomicIntegerFieldUpdater.newUpdater(LongRunningOperation.class, "lroStartFlag");
    //
    private final Mono<PollResponse<PollResultT>> lroStartMonoWithResponseCache;

    public LongRunningOperation(Mono<PollResultT> lroStartMono,
                                Function<PollResponse<PollResultT>, Mono<PollResponse<PollResultT>>> pollOperation,
                                Function<PollResponse<PollResultT>, Mono<PollResultT>> cancelOperation,
                                Supplier<Mono<FinalResultT>> fetchResultOperation) {
        this.lroStartMonoWithResponseCache = lroStartMonoWithResponseCache(lroStartMono);
        this.pollOperation = pollOperation;
        this.cancelOperation = cancelOperation;
        this.fetchResultOperation = fetchResultOperation;
    }

    /**
     * @return a Flux, upon subscription starts the long running operation followed by polling.
     */
    public Flux<AsyncPollResponse<PollResultT, FinalResultT>> begin() {
        // Get a Flux on which ONLY ONE subscription can start LRO successfully and ALL subscriptions
        // can poll. Polling is not shared, each subscription starts it's own polling, if user need
        // multi-casting for any reason then they should call share() on returned Flux.
        return
        this.lroStartMonoWithResponseCache
            .flatMapMany(lroStartResponse -> pollingLoop(lroStartResponse));
    }

    /**
     * Returns a decorated Mono, upon subscription it internally subscribes to the Mono that starts LRO.
     * The decorated Mono caches the response of LRO initiate call, this cached response will be replayed
     * for any future subsequent subscriptions.
     *
     * Note: we can't use the reactor standard cache() operator, because it caches error terminal signal
     * and forward it to any future subscriptions. If there is an error from lroStartMono
     * then we don't want to cache it but just forward it to subscription that initiated the failed LRO.
     * For any future subscriptions we don't want to forward the past error instead lroStartMono
     * should again invoked. Once a subscription manage to get a successful event from lroStartMono call 
     * then we want to cache it and replay it to any future subscriptions.
     *
     * The decorated Mono also handles concurrent subscription to it, on such event only one of them
     * will be able to call lroStartMono and other subscriptions will keep resubscribing until
     * it sees a cached response or get a chance to call lroStartMono as the one previously
     * entered the critical section got an error from lroStartMono.
     *
     * @param lroStartMono the Mono upon subscription initiate/start the long running operation.
     * @return
     */
    private Mono<PollResponse<PollResultT>> lroStartMonoWithResponseCache(final Mono<PollResultT> lroStartMono) {
        return Mono.defer(() -> {
            if (this.lroStartResponse != null) {
                return Mono.just(this.lroStartResponse);
            }
            if (this.guardLroStartCall.compareAndSet(this, 0, 1)) {
                return lroStartMono.map((PollResultT response) -> {
                    this.lroStartResponse = new PollResponse<>(PollResponse.OperationStatus.NOT_STARTED, response);
                    return this.lroStartResponse;
                })
                .switchIfEmpty(Mono.error(new RuntimeException("lroStartMono should produce a Response.")))
                .doOnError(throwable -> guardLroStartCall.compareAndSet(this, 1, 0));
            } else {
                return Mono.empty();
            }
        }).repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    /**
     * Do the polling until a terminal state (including cancel) reaches.
     *
     * @param lroStartResponse the response from LRO start operation
     * @return a Flux that emits async polling status.
     */
    private Flux<AsyncPollResponse<PollResultT, FinalResultT>> pollingLoop(PollResponse<PollResultT> lroStartResponse) {
        return Flux.using(
            // Create a state per subscription
            () -> new State<>(lroStartResponse),
            // Do polling
            // set|read in state as needed, reactor guarantee thread-safety of state object.
            state -> Mono.defer(() ->  {
                    if (state.isCancelled()) {
                        return Mono.just(new PollResponse<PollResultT>(PollResponse.OperationStatus.USER_CANCELLED, null));
                    } else {
                        return pollOperation.apply(state.getLastResponse());
                    }
                })
                .delaySubscription(state.getDelayFromLastResponse())
                .repeat()
                .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                .onErrorResume(throwable -> {
                    // We want to continue on polling as per guideline.
                    return Mono.empty();
                })
                .flatMap(currentPollResponse -> {
                    state.setLastResponse(currentPollResponse);
                    // If there is a final result fetcher then invoke it.
                    if (this.fetchResultOperation != null) {
                        return this.fetchResultOperation.get().flatMap(
                            (Function<FinalResultT, Mono<AsyncPollResponse<PollResultT, FinalResultT>>>) result
                                -> Mono.just(AsyncPollResponse.from(state, cancelOperation, result)));
                    } else {
                        return Mono.just(AsyncPollResponse.from(state, this.cancelOperation));
                    }
                }),
            //
            // No cleaning needed, state will be GC-ed
            state -> {});
    }

    public static final class State<PollResultT> {
        private PollResponse<PollResultT> lastResponse;
        private boolean isCancelled = false;

        public State(PollResponse<PollResultT> lastResponse) {
            this.lastResponse = Objects.requireNonNull(lastResponse);
        }

        public PollResponse<PollResultT> getLastResponse() {
            return this.lastResponse;
        }

        void setLastResponse(PollResponse<PollResultT> value) {
            this.lastResponse = Objects.requireNonNull(value);
        }

        public void markCancelled() {
            this.isCancelled = true;
        }

        boolean isCancelled() {
            return this.isCancelled;
        }

        Duration getDelayFromLastResponse() {
            Duration retryAfter = lastResponse.getRetryAfter();
            Duration defaultRetryAfter = Duration.ofSeconds(10);
            if (retryAfter == null) {
                return defaultRetryAfter;
            } else {
                return retryAfter.compareTo(Duration.ZERO) > 0
                    ? retryAfter
                    : defaultRetryAfter;
            }
        }
    }
}
