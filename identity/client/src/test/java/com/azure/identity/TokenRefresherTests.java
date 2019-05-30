package com.azure.identity;

import com.azure.identity.implementation.TokenRefresher;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Random;

public class TokenRefresherTests {
    private static final Random RANDOM = new Random();

    @Test
    public void testCanGetToken() {
        TokenRefresher<Token> refresher = new TokenRefresher<Token>(
            Token::token,
            Token::isExpired,
            this::simluateRemoteGetTokenAsync);

        Flux.range(1, 10)
            .flatMap(i -> {
                OffsetDateTime start = OffsetDateTime.now();
                return refresher.getTokenAsync("resource")
                    .map(t -> start);
            }).subscribeOn(Schedulers.parallel())
            .doOnNext(start -> System.out.format("Thread: %s\tStart: %s\tEnd: %s%n",
                Thread.currentThread().getName(), start.toString(), OffsetDateTime.now().toString()))
            .blockLast();
    }

    private Mono<Token> simluateRemoteGetTokenAsync() {
        return Mono.delay(Duration.ofSeconds(1))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100))));
    }

    private static class Token {
        private String token;
        private OffsetDateTime dateTime;

        String token() {
            return token;
        }

        Token(String token) {
            this.token = token;
            this.dateTime = OffsetDateTime.now();
        }

        boolean isExpired() {
            Duration duration = Duration.between(dateTime, OffsetDateTime.now());
            return duration.getSeconds() > 5;
        }
    }
}
