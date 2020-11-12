// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.FieldBuilderOptions;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.test.environment.models.Hotel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;

public class FieldBuilderServiceTests extends SearchTestBase {
    private SearchIndexClient client;
    private final List<String> indexesToDelete = new ArrayList<>();
    private SearchIndex index;
    String synonymMapName = "fieldbuilder";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchIndexClientBuilder().buildClient();
        index = new SearchIndex(testResourceNamer.randomName("fieldbuilder", 32));
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
    public void createIndexWithFieldBuilder() {
        SynonymMap synonymMap = new SynonymMap(synonymMapName).setSynonyms("hotel,motel");
        client.createSynonymMap(synonymMap);
        index.setFields(SearchIndexClient.buildSearchFields(Hotel.class, new FieldBuilderOptions()
            .setJsonSerializer(new JacksonJsonSerializerBuilder().serializer(new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY))
                .build())));
        client.createIndex(index);
        indexesToDelete.add(index.getName());
        assertObjectEquals(index, client.getIndex(index.getName()), true);
    }
}
