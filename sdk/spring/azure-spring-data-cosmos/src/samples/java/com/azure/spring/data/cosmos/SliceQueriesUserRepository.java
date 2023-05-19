// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;

// BEGIN: readme-sample-SliceQueriesUserRepository
public interface SliceQueriesUserRepository extends CosmosRepository<User, String> {
    @Query("select * from c where c.lastName = @lastName")
    Slice<User> getUsersByLastName(@Param("lastName") String lastName, Pageable pageable);
}
// END: readme-sample-SliceQueriesUserRepository
