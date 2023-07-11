// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public class PageableRepositoryCodeSnippet<T> {

    @Autowired
    private PagingAndSortingRepository<T, String> repository;

    // BEGIN: readme-sample-findAllWithPageSize
    private List<T> findAllWithPageSize(int pageSize) {

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
        Page<T> page = repository.findAll(pageRequest);
        List<T> pageContent = page.getContent();
        while (page.hasNext()) {
            Pageable nextPageable = page.nextPageable();
            page = repository.findAll(nextPageable);
            pageContent = page.getContent();
        }
        return pageContent;
    }
    // END: readme-sample-findAllWithPageSize
}
