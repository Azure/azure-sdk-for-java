// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.rest.Response;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class VectorSearchTests extends SearchTestBase {

    private void assertKeysEqual(SearchPagedResponse response,
                                 Function<SearchResult, String> keyAccessor,
                                 String[] expectedKeys) {

        List<SearchResult> docs = response.getValue().stream().toList();


    }

}
