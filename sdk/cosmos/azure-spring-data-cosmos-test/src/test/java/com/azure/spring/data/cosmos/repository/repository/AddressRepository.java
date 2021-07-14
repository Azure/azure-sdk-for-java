// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends CosmosRepository<Address, String> {
    void deleteByPostalCodeAndCity(String postalCode, String city);

    void deleteByCity(String city);

    Iterable<Address> findByPostalCodeAndCity(String postalCode, String city);

    Iterable<Address> findByCity(String city);

    Iterable<Address> findByCityIn(List<String> cities);

    Iterable<Address> findByPostalCode(String postalCode);

    Iterable<Address> findByPostalCodeInAndCity(List<String> postalCodes, String city);

    Iterable<Address> findByStreetOrCity(String street, String city);

    @Query("select * from a where a.city = @city")
    List<Address> annotatedFindListByCity(@Param("city") String city);

    @Query("select * from a where a.city = @city")
    Page<Address> annotatedFindByCity(@Param("city") String city, Pageable pageable);

    @Query("select * from a where a.city = @city")
    List<Address> annotatedFindByCity(@Param("city") String city, Sort pageable);

    @Query("select DISTINCT value a.postalCode from a where a.city = @city")
    Page<String> annotatedFindPostalCodeValuesByCity(@Param("city") String city, Pageable pageable);

    @Query("select DISTINCT a.postalCode from a where a.city = @city")
    Page<JsonNode> annotatedFindPostalCodesByCity(@Param("city") String city, Pageable pageable);

    @Query("select DISTINCT value a.postalCode from a where a.city = @city")
    List<String> annotatedFindPostalCodeValuesByCity(@Param("city") String city);
}
