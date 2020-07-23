// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.SingleResponseEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SingleResponseRepository extends CosmosRepository<SingleResponseEntity, String> {
    List<SingleResponseEntity> findByEntityTitle(String title);

    Iterable<SingleResponseEntity> findByEntityId(String entityId);

    SingleResponseEntity findOneByEntityTitle(String title);

    Optional<SingleResponseEntity> findOptionallyByEntityTitle(String title);
}
