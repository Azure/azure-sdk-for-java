// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

public interface AnnotatedQueriesUserReactiveRepositoryCodeSnippet extends ReactiveCosmosRepository<User, String> {
    @Query(value = "select * from c where c.firstName = @firstName and c.lastName = @lastName")
    Flux<User> getUsersByTitleAndValue(@Param("firstName") int firstName, @Param("lastName") String lastName);

    @Query(value = "select * from c offset @offset limit @limit")
    Flux<User> getUsersWithOffsetLimit(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "select count(c.id) as num_ids, c.lastName from c group by c.lastName")
    Flux<ObjectNode> getCoursesGroupByDepartment();
}
