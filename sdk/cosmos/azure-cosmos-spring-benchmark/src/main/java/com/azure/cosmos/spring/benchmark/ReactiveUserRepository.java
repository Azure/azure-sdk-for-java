package com.azure.cosmos.spring.benchmark;

import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveUserRepository extends ReactiveCosmosRepository<User, String> {
    Flux<User> findByFirstName(String firstName);

    Mono<User> findById(String id);
}
