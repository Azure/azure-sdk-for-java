// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SynonymMap;

import java.util.Arrays;
@SuppressWarnings("unused")
public class SearchIndexAsyncClientJavaDocSnippets {

    private static SearchIndexAsyncClient searchIndexAsyncClient;

    /**
     * Code snippet for creating a {@link com.azure.search.documents.indexes.SearchIndexAsyncClient}
     */
    private static SearchIndexAsyncClient createSearchIndexAsyncClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.instantiation
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.instantiation
        return searchIndexAsyncClient;
    }

    /**
     * Code snippet for creating an index.
     */
    public static void createIndex() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.createIndex#SearchIndex
        SearchIndex searchIndex = new SearchIndex("indexName", Arrays.asList(
            new SearchField("hotelId", SearchFieldDataType.STRING)
                .setKey(true)
                .setFilterable(true)
                .setSortable(true),
            new SearchField("hotelName", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setFilterable(true)
                .setSortable(true),
            new SearchField("description", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
            new SearchField("descriptionFr", SearchFieldDataType.STRING)
                .setSearchable(true)
                .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE),
            new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setSearchable(true)
                .setFilterable(true)
                .setFacetable(true),
            new SearchField("address", SearchFieldDataType.COMPLEX)
                .setFields(
                    new SearchField("streetAddress", SearchFieldDataType.STRING)
                        .setSearchable(true),
                    new SearchField("city", SearchFieldDataType.STRING)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true),
                    new SearchField("stateProvince", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true),
                    new SearchField("country", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setSynonymMapNames("synonymMapName")
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true),
                    new SearchField("postalCode", SearchFieldDataType.STRING)
                        .setSearchable(true)
                        .setFilterable(true)
                        .setSortable(true)
                        .setFacetable(true))
        ));

        searchIndexAsyncClient.createIndex(searchIndex).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.createIndex#SearchIndex
    }

    /**
     * Code Snippet for listing all indexes
     */
    public static void listIndexes() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listIndexes
        searchIndexAsyncClient.listIndexes().subscribe(index -> System.out.println("The index name is " + index.getName()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listIndexes
    }

    /**
     * Code snippet for retrieving an index
     */
    public static void getIndex() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getIndex#String
        SearchIndex searchIndex = searchIndexAsyncClient.getIndex("indexName").block();
        if (searchIndex != null) {
            System.out.println("The index name is " + searchIndex.getName());
        }
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getIndex#String
    }

    /**
     * Code snippet for updating an index
     */
    public static void updateIndex() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateIndex#SearchIndex
        SearchIndex searchIndex = searchIndexAsyncClient.getIndex("indexName").block();
        if (searchIndex != null) {
            searchIndex.setFields(new SearchField("newField", SearchFieldDataType.STRING));
            searchIndexAsyncClient.createOrUpdateIndex(searchIndex);
        }
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateIndex#SearchIndex
    }

    /**
     * Code snippet for deleting an index
     */
    public static void deleteIndex() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteIndex#String
        String indexName = "indexName";
        searchIndexAsyncClient.deleteIndex(indexName).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteIndex#String
    }

    /**
     * Code snippet for creating a synonym map
     */
    public static void createSynonymMap() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.createSynonymMap#SynonymMap
        SynonymMap synonymMap = new SynonymMap("synonymMapName", "hotel, motel, \"motor inn\"");
        searchIndexAsyncClient.createSynonymMap(synonymMap).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.createSynonymMap#SynonymMap
    }

    /**
     * Code snippet for listing all synonym maps
     */
    public static void listSynonymMaps() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listSynonymMaps
        searchIndexAsyncClient.listSynonymMaps().subscribe(synonymMap ->
            System.out.println("The synonymMap name is " + synonymMap.getName())
        );
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listSynonymMaps
    }

    /**
     * Code snippet for retrieving a synonym map
     */
    public static void getSynonymMap() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getSynonymMap#String
        SynonymMap synonymMap = searchIndexAsyncClient.getSynonymMap("synonymMapName").block();
        if (synonymMap != null) {
            System.out.println("The synonymMap name is " + synonymMap.getName());
        }
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getSynonymMap#String
    }

    /**
     * Code snippet for updating a synonym map
     */
    public static void updateSynonymMap() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateSynonymMap#SynonymMap
        SynonymMap synonymMap = searchIndexAsyncClient.getSynonymMap("synonymMapName").block();
        if (synonymMap != null) {
            synonymMap.setSynonyms("hotel, motel, inn");
            searchIndexAsyncClient.createOrUpdateSynonymMap(synonymMap).block();
        }
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for deleting a synonym map
     */
    public static void deleteSynonymMap() {
        searchIndexAsyncClient = createSearchIndexAsyncClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteSynonymMap#String
        String synonymMapName = "synonymMapName";
        searchIndexAsyncClient.deleteSynonymMap(synonymMapName).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteSynonymMap#String
    }

}
