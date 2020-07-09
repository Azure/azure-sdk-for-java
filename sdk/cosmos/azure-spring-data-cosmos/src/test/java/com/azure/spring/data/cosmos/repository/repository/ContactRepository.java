// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.domain.Contact;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends CosmosRepository<Contact, String> {
    List<Contact> findByTitle(String title);
}
