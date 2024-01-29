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

    public static SearchIndexAsyncClient SEARCH_INDEX_ASYNC_CLIENT;

    /**
     * Code snippet for creating a {@link com.azure.search.documents.indexes.SearchIndexAsyncClient}
     */
    public void createSearchIndexAsyncClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.instantiation
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.instantiation
    }

    /**
     * Code snippet for creating an index.
     */
    public void createIndex() {
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

        SEARCH_INDEX_ASYNC_CLIENT.createIndex(searchIndex).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.createIndex#SearchIndex
    }

    /**
     * Code Snippet for listing all indexes
     */
    public void listIndexes() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listIndexes
        SEARCH_INDEX_ASYNC_CLIENT.listIndexes().subscribe(index -> System.out.println("The index name is " + index.getName()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listIndexes
    }

    /**
     * Code snippet for retrieving an index
     */
    public void getIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getIndex#String
        SearchIndex searchIndex = SEARCH_INDEX_ASYNC_CLIENT.getIndex("indexName").block();
        System.out.println("The ETag of the index is " + searchIndex.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getIndex#String
    }

    /**
     * Code snippet for updating an index
     */
    public void updateIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateIndex#SearchIndex
        SearchIndex searchIndex = SEARCH_INDEX_ASYNC_CLIENT.getIndex("indexName").block();
        searchIndex.setFields(new SearchField("newField", SearchFieldDataType.STRING));
        SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateIndex(searchIndex);
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateIndex#SearchIndex
    }

    /**
     * Code snippet for deleting an index
     */
    public void deleteIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteIndex#String
        String indexName = "indexName";
        SEARCH_INDEX_ASYNC_CLIENT.deleteIndex(indexName).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteIndex#String
    }

    /**
     * Code snippet for creating a synonym map
     */
    public void createSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.createSynonymMap#SynonymMap
        SynonymMap synonymMap = new SynonymMap("synonymMapName", "hotel, motel, \"motor inn\"");
        SEARCH_INDEX_ASYNC_CLIENT.createSynonymMap(synonymMap).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.createSynonymMap#SynonymMap
    }

    /**
     * Code snippet for listing all synonym maps
     */
    public void listSynonymMaps() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listSynonymMaps
        SEARCH_INDEX_ASYNC_CLIENT.listSynonymMaps().subscribe(synonymMap ->
            System.out.println("The synonymMap name is " + synonymMap.getName())
        );
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.listSynonymMaps
    }

    /**
     * Code snippet for retrieving a synonym map
     */
    public void getSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getSynonymMap#String
        SynonymMap synonymMap = SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("synonymMapName").block();
        System.out.println("The ETag of the synonymMap is " + synonymMap.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.getSynonymMap#String
    }

    /**
     * Code snippet for updating a synonym map
     */
    public void updateSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateSynonymMap#SynonymMap
        SynonymMap synonymMap = SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("synonymMapName").block();
        synonymMap.setSynonyms("inn");
        SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateSynonymMap(synonymMap).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.updateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for deleting a synonym map
     */
    public void deleteSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteSynonymMap#String
        String synonymMapName = "synonymMapName";
        SEARCH_INDEX_ASYNC_CLIENT.deleteSynonymMap(synonymMapName).block();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient-classLevelJavaDoc.deleteSynonymMap#String
    }

}
