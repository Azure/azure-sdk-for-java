package com.azure.storage.queue;

import reactor.core.publisher.Mono;

public class Demo {
    public static void main(String[] args) {
        Mono.just("test")
            .subscribe(System.out::println);
        Mono<String> m=Mono.just("test");
        m.doOnSuccess(x -> System.out.println("sss"))

//        Mono<Void> v=m.then();
//        v
//m
            .doOnNext(x->System.out.println("OK"))
            .subscribe(x -> System.out.println("ok2"));

    }
}
