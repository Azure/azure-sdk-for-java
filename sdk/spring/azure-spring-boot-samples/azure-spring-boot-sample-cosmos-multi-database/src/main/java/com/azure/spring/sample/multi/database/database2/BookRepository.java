// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.multi.database.database2;

import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

public interface BookRepository extends ReactiveCosmosRepository<Book, String> {
}
