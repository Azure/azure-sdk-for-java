package com.azure.identity.implementation;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public class TokenRefresher<T> {
    private static final int REFRESH_TIMEOUT_SECONDS = 30;

    private final Map<String, T> cache;
    private final Function<T, String> getToken;
    private final Function<T, Boolean> isExpired;
    private final Supplier<Mono<T>> getNew;
    private final Function<T, Mono<T>> refresh;
    private final AtomicBoolean wip = new AtomicBoolean(false);
    private final EmitterProcessor<String> emitterProcessor = EmitterProcessor.create();
    private final FluxSink<String> sink = emitterProcessor.sink(OverflowStrategy.BUFFER);

    public TokenRefresher(Function<T, String> getToken,
                          Function<T, Boolean> isExpired,
                          Supplier<Mono<T>> getNew) {
        this(getToken, isExpired, getNew, t -> getNew.get());
    }

    public TokenRefresher(Function<T, String> getToken,
                          Function<T, Boolean> isExpired,
                          Supplier<Mono<T>> getNew,
                          Function<T, Mono<T>> refresh) {
        cache = new HashMap<>();
        this.getToken = getToken;
        this.isExpired = isExpired;
        this.getNew = getNew;
        this.refresh = refresh;
    }

    public boolean isCached(String resource) {
        return cache.containsKey(resource) && !isExpired.apply(cache.get(resource));
    }

    public String getCachedToken(String resource) {
        return getToken.apply(cache.get(resource));
    }

    public Mono<String> getTokenAsync(String resource) {
        if (isCached(resource)) {
            return Mono.just(getCachedToken(resource));
        } else {
            if (!wip.getAndSet(true)) {
                Mono<T> ret = Mono.empty();
                if (cache.containsKey(resource)) {
                    ret = ret.switchIfEmpty(refresh.apply(cache.get(resource)));
                }
                return ret.onErrorResume(t -> Mono.empty())
                    .switchIfEmpty(getNew.get())
                    .doOnNext(val -> {
                        cache.put(resource, val);
                        sink.next(resource);
                    })
                    .flatMap(val -> emitterProcessor.filter(s -> s.equals(resource)).next())
                    .map(this::getCachedToken)
                    .doFinally(sig -> wip.set(false));
            }
            return emitterProcessor.filter(s -> s.equals(resource)).next()
                .timeout(Duration.ofSeconds(REFRESH_TIMEOUT_SECONDS))
                .map(this::getCachedToken);
        }
    }
}
