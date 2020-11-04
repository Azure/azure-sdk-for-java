// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnnotatedQueriesUserRepositoryCodeSnippet extends CosmosRepository<User, String> {
    @Query(value = "select * from c where c.firstName = @firstName and c.lastName = @lastName")
    List<User> getUsersByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query(value = "select * from c offset @offset limit @limit")
    List<User> getUsersWithOffsetLimit(@Param("offset") int offset, @Param("limit") int limit);
}
