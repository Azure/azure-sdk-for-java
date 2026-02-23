// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.testingmodels.Hotel;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;

public class FieldBuilderServiceTests extends SearchTestBase {
    private SearchIndexClient client;
    private SearchIndexAsyncClient asyncClient;
    private final List<String> indexesToDelete = new ArrayList<>();
    String synonymMapName = "fieldbuilder";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchIndexClientBuilder(true).buildClient();
        asyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        for (String index : indexesToDelete) {
            client.deleteIndex(index);
        }

        client.deleteSynonymMap(synonymMapName);
    }

    @Test
    public void createIndexWithFieldBuilderSync() {
        SynonymMap synonymMap = new SynonymMap(synonymMapName, "hotel,motel");
        client.createSynonymMap(synonymMap);

        SearchIndex index = new SearchIndex(testResourceNamer.randomName("fieldbuilder", 32),
            SearchIndexClient.buildSearchFields(Hotel.class));

        client.createIndex(index);
        indexesToDelete.add(index.getName());
        assertObjectEquals(index, client.getIndex(index.getName()), true);
    }

    @Test
    public void createIndexWithFieldBuilderAsync() {
        SynonymMap synonymMap = new SynonymMap(synonymMapName, "hotel,motel");
        asyncClient.createSynonymMap(synonymMap).block();

        SearchIndex index = new SearchIndex(testResourceNamer.randomName("fieldbuilder", 32),
            SearchIndexClient.buildSearchFields(Hotel.class));

        Mono<SearchIndex> createThenGetIndex = asyncClient.createIndex(index).flatMap(actual -> {
            indexesToDelete.add(actual.getName());
            return asyncClient.getIndex(actual.getName());
        });

        StepVerifier.create(createThenGetIndex)
            .assertNext(actual -> assertObjectEquals(index, actual, true))
            .verifyComplete();
    }
}
