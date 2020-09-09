// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Contact;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends CosmosRepository<Contact, String> {
    Iterable<Contact> findByTitle(String title);

    Iterable<Contact> findByLogicId(String title);

    Contact findOneByTitle(String title);

    Optional<Contact> findOptionallyByTitle(String title);

    @Query(value = "select * from c where c.title = @title and c.intValue = @value")
    List<Contact> getContactsByTitleAndValue(@Param("value") int value, @Param("title") String name);

    @Query(value = "select * from c offset @offset limit @limit")
    List<Contact> getContactsWithOffsetLimit(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "select * from c where c.status= true")
    List<Contact> findActiveContacts();

    @Query(value = "SELECT count(c.id) as id_count, c.intValue FROM c group by c.intValue")
    List<ObjectNode> selectGroupBy();

}
