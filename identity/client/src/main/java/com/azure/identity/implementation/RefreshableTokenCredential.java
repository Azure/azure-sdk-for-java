package com.azure.identity.implementation;

import com.azure.core.credentials.TokenCredential;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RefreshableTokenCredential<T> extends TokenCredential {
    private static final int REFRESH_TIMEOUT_SECONDS = 30;

    private final Map<String, T> cache;
    private final Map<String, AtomicBoolean> wips;
    private final EmitterProcessor<String> emitterProcessor = EmitterProcessor.create(false);
    private final FluxSink<String> sink = emitterProcessor.sink(OverflowStrategy.BUFFER);

    public RefreshableTokenCredential(){
        super();
        cache = new ConcurrentHashMap<>();
        wips = new ConcurrentHashMap<>();
    }

    protected abstract String getTokenFromAuthResult(T authResult);

    protected abstract boolean isExpired(T authResult);

    protected abstract Mono<T> authenticateAsync(String resource);

    protected Mono<T> refreshAsync(T expiredAuthResult, String resource) {
        return authenticateAsync(resource);
    }

    public Mono<String> getTokenAsync(String resource) {
        if (isCached(resource)) {
            return Mono.just(getCachedToken(resource));
        } else {
            return Mono.fromCallable(() -> {
                synchronized (wips) {
                    if (!wips.containsKey(resource)) {
                        wips.put(resource, new AtomicBoolean(false));
                    }
                    return wips.get(resource);
                }
            }).flatMap(wip -> {
                Mono<String> poller = emitterProcessor.filter(s -> s.equals(resource)).next()
                    .timeout(Duration.ofSeconds(REFRESH_TIMEOUT_SECONDS))
                    .map(this::getCachedToken);

                if (!wip.getAndSet(true)) {
                    Mono<T> ret = Mono.empty();
                    if (cache.containsKey(resource)) {
                        ret = ret.switchIfEmpty(refreshAsync(cache.get(resource), resource));
                    }
                    ret = ret.onErrorResume(t -> Mono.empty())
                        .switchIfEmpty(authenticateAsync(resource))
                        .doOnNext(val -> {
                            cache.put(resource, val);
                            sink.next(resource);
                            wip.set(false);
                        });
                    return Flux.merge(poller, ret.map(this::getTokenFromAuthResult)).last();
                } else {
                    return poller;
                }
            });
        }
    }

    private boolean isCached(String resource) {
        return cache.containsKey(resource) && !isExpired(cache.get(resource));
    }

    private String getCachedToken(String resource) {
        return getTokenFromAuthResult(cache.get(resource));
    }
}
