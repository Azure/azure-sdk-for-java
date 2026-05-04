// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SynonymMap;

import java.util.Arrays;
@SuppressWarnings("unused")
public class SearchIndexClientJavaDocSnippets {

    private static SearchIndexClient searchIndexClient;

    /**
     * Code snippet for creating a {@link com.azure.search.documents.indexes.SearchIndexClient}
     */
    private static SearchIndexClient createSearchIndexClient() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.instantiation
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.instantiation
        return searchIndexClient;
    }

    /**
     * Code snippet for creating an index.
     */
    public static void createIndex() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createIndex#SearchIndex
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

        searchIndexClient.createIndex(searchIndex);
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createIndex#SearchIndex
    }

    /**
     * Code Snippet for listing all indexes
     */
    public static void listIndexes() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listIndexes
        searchIndexClient.listIndexes().forEach(index -> System.out.println(index.getName()));
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listIndexes
    }

    /**
     * Code snippet for retrieving an index
     */
    public static void getIndex() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getIndex#String
        SearchIndex searchIndex = searchIndexClient.getIndex("indexName");
        if (searchIndex != null) {
            System.out.println("The ETag of the index is " + searchIndex.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getIndex#String
    }

    /**
     * Code snippet for updating an index
     */
    public static void updateIndex() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateIndex#SearchIndex
        SearchIndex searchIndex = searchIndexClient.getIndex("indexName");
        if (searchIndex != null) {
            searchIndex.setFields(new SearchField("newField", SearchFieldDataType.STRING));
            searchIndexClient.createOrUpdateIndex(searchIndex);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateIndex#SearchIndex
    }

    /**
     * Code snippet for deleting an index
     */
    public static void deleteIndex() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteIndex#String
        String indexName = "indexName";
        searchIndexClient.deleteIndex(indexName);
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteIndex#String
    }

    /**
     * Code snippet for creating a synonym map
     */
    public static void createSynonymMap() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createSynonymMap#SynonymMap
        SynonymMap synonymMap = new SynonymMap("synonymMapName", "hotel, motel, \"motor inn\"");
        searchIndexClient.createSynonymMap(synonymMap);
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.createSynonymMap#SynonymMap
    }

    /**
     * Code snippet for listing all synonym maps
     */
    public static void listSynonymMaps() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listSynonymMaps
        searchIndexClient.listSynonymMaps().forEach(synonymMap -> System.out.println(synonymMap.getName()));
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.listSynonymMaps
    }

    /**
     * Code snippet for retrieving a synonym map
     */
    public static void getSynonymMap() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getSynonymMap#String
        SynonymMap synonymMap = searchIndexClient.getSynonymMap("synonymMapName");
        if (synonymMap != null) {
            System.out.println("The ETag of the synonymMap is " + synonymMap.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.getSynonymMap#String
    }

    /**
     * Code snippet for updating a synonym map
     */
    public static void updateSynonymMap() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateSynonymMap#SynonymMap
        SynonymMap synonymMap = searchIndexClient.getSynonymMap("synonymMapName");
        if (synonymMap != null) {
            synonymMap.setSynonyms("inn,hotel,motel");
            searchIndexClient.createOrUpdateSynonymMap(synonymMap);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.updateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for deleting a synonym map
     */
    public static void deleteSynonymMap() {
        searchIndexClient = createSearchIndexClient();
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteSynonymMap#String
        String synonymMapName = "synonymMapName";
        searchIndexClient.deleteSynonymMap(synonymMapName);
        // END: com.azure.search.documents.indexes.SearchIndexClient-classLevelJavaDoc.deleteSynonymMap#String
    }
}
