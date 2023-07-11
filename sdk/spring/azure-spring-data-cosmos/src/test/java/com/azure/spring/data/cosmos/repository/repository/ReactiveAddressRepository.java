// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface ReactiveAddressRepository extends ReactiveCosmosRepository<Address, String> {
    Mono<Void> deleteByCity(String city);

    Flux<Address> findByCityIn(List<String> cities);

    @Query("select * from a where a.city = @city")
    Flux<Address> annotatedFindListByCity(@Param("city") String city);
}
