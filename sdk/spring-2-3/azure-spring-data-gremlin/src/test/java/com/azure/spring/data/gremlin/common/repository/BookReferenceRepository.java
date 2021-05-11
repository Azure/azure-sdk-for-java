// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.repository;

import com.azure.spring.data.gremlin.common.domain.BookReference;
import com.azure.spring.data.gremlin.repository.GremlinRepository;

public interface BookReferenceRepository extends GremlinRepository<BookReference, Integer> {
}
