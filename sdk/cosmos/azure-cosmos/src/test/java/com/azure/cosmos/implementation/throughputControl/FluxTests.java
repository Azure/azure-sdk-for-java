package com.azure.cosmos.implementation.throughputControl;

import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

public class FluxTests {

    @Test
    public void fluxTest() {
        this.test1()
            .flatMap(dummy -> this.test2())
            .flatMap(avoid -> this.test3())
            .then(Mono.just(1))
            .onErrorResume(throwable -> {
                System.out.println("wowowowoowo");
                return Mono.empty();
            })
            .subscribe();
    }

    public Mono<Integer> test1() {
        System.out.println("inside test 1");
        return Mono.just(1);
    }

    public Mono<Void> test2() {
        System.out.println("inside test 2");
        //return Mono.empty();
        return Mono.error(new IllegalArgumentException("haha"));
        //throw new IllegalArgumentException("haha");
       // return Mono.error(new IllegalArgumentException("haha"));
        //return Mono.empty();
    }

    public Mono<Void> test3() {
        System.out.println("inside test 3");
        return Mono.empty();
    }
}
