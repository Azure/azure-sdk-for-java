// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.repository;

import com.microsoft.azure.spring.data.cosmosdb.domain.Question;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;

public interface QuestionRepository extends CosmosRepository<Question, String> {
}
