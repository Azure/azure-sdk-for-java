// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends CosmosRepository<Address, String> {
    void deleteByPostalCodeAndCity(String postalCode, String city);

    void deleteByCity(String city);

    Iterable<Address> findByPostalCodeAndCity(String postalCode, String city);

    Iterable<Address> findByCity(String city);

    Iterable<Address> findByPostalCode(String postalCode);

    Iterable<Address> findByStreetOrCity(String street, String city);

}
