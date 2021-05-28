// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public class SliceRepositoryCodeSnippet {
    @Autowired
    private SliceQueriesUserRepository repository;

    private List<User> getUsersByLastName(String lastName, int pageSize) {

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
        Slice<User> slice = repository.getUsersByLastName(lastName, pageRequest);
        List<User> content = slice.getContent();
        while (slice.hasNext()) {
            Pageable nextPageable = slice.nextPageable();
            slice = repository.getUsersByLastName(lastName, nextPageable);
            content.addAll(slice.getContent());
        }
        return content;
    }
}
