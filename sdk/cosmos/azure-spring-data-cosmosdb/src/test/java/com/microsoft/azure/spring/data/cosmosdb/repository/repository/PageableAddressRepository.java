// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.repository;

import com.microsoft.azure.spring.data.cosmosdb.domain.PageableAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageableAddressRepository extends PagingAndSortingRepository<PageableAddress, String> {
    Page<PageableAddress> findByStreet(String street, Pageable pageable);

    Page<PageableAddress> findByCity(String city, Pageable pageable);
}
