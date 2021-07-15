// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Importance;
import com.azure.spring.data.cosmos.domain.PageableMemo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageableMemoRepository extends PagingAndSortingRepository<PageableMemo, String> {
    Slice<PageableMemo> findByImportance(Importance importance, Pageable pageable);
}
