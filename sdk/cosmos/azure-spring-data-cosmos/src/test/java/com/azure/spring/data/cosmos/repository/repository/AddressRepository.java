// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.domain.Address;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends CosmosRepository<Address, String> {
    void deleteByPostalCodeAndCity(String postalCode, String city);

    void deleteByCity(String city);

    List<Address> findByPostalCodeAndCity(String postalCode, String city);

    List<Address> findByCity(String city);

    List<Address> findByPostalCode(String postalCode);

    List<Address> findByStreetOrCity(String street, String city);

}
