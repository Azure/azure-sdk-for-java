// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Contact;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends CosmosRepository<Contact, String> {
    Iterable<Contact> findByTitle(String title);

    Iterable<Contact> findByLogicId(String title);

    Contact findOneByTitle(String title);

    Optional<Contact> findOptionallyByTitle(String title);
}
