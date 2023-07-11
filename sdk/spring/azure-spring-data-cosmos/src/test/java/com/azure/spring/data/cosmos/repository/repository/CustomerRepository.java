// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Customer;
import com.azure.spring.data.cosmos.repository.CosmosRepository;

public interface CustomerRepository extends CosmosRepository<Customer, String> {
    Iterable<Customer> findByUser_Name(String name);
}
