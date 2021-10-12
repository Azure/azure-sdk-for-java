// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Supplier;

public interface CosmosPageFactory {

    <T> Page<T> createPage(List<T> content, Pageable pageable, Supplier<Long> totalFunction);

}
