// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Legacy similarity algorithm which uses the Lucene TFIDFSimilarity
 * implementation of TF-IDF. This variation of TF-IDF introduces static
 * document length normalization as well as coordinating factors that penalize
 * documents that only partially match the searched queries.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.ClassicSimilarity")
@Fluent
public final class ClassicSimilarityAlgorithm extends SimilarityAlgorithm {
}
