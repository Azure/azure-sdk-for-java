// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.LexicalTokenizerName;
import com.azure.search.documents.indexes.models.OcrSkill;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexStatistics;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SearchableFieldBuilder;
import com.azure.search.documents.indexes.models.SimpleFieldBuilder;
import com.azure.search.documents.indexes.models.SynonymMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchJavaDocCodeSnippets {
    private SearchClient searchClient = new SearchClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link SearchClient}.
     */
    public void createSearchClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchClient.instantiation
        SearchClient searchClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildClient();
        // END: com.azure.search.documents.SearchClient.instantiation
    }

    private SearchAsyncClient searchAsyncClient = new SearchClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link SearchAsyncClient}.
     */
    public void createSearchAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.instantiation
        SearchAsyncClient searchAsyncClient = new SearchClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .indexName("{indexName}")
            .buildAsyncClient();
        // END: com.azure.search.documents.SearchAsyncClient.instantiation
    }

    private SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().buildClient();
    private String key1 = "key1";
    private String value1 = "val1";

    /**
     * Code snippet for creating a {@link SearchIndexClient}.
     */
    public void createSearchIndexClientFromBuilder() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.instantiation
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexClient.instantiation
    }

    /**
     * Code snippet for creating {@link SearchIndexClient#createIndex(SearchIndex)}.
     */
    public void createSearchIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createIndex#SearchIndex
        List<SearchField> searchFields = Arrays.asList(
            new SimpleFieldBuilder("hotelId", SearchFieldDataType.STRING, false).setKey(true).build(),
            new SearchableFieldBuilder("hotelName", false).build()
        );
        SearchIndex searchIndex = new SearchIndex("searchIndex", searchFields);
        SearchIndex indexFromService = searchIndexClient.createIndex(searchIndex);
        System.out.printf("The index name is %s. The etag of index is %s.%n", indexFromService.getName(),
            indexFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexClient#createIndexWithResponse(SearchIndex, Context)}.
     */
    public void createSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createIndexWithResponse#SearchIndex-Context
        List<SearchField> searchFields = Arrays.asList(
            new SimpleFieldBuilder("hotelId", SearchFieldDataType.STRING, false).setKey(true).build(),
            new SearchableFieldBuilder("hotelName", false).build()
        );
        SearchIndex searchIndex = new SearchIndex("searchIndex", searchFields);

        Response<SearchIndex> indexFromServiceResponse =
            searchIndexClient.createIndexWithResponse(searchIndex, new Context(key1, value1));
        System.out.printf("The status code of the response is %s. The index name is %s.%n",
            indexFromServiceResponse.getStatusCode(), indexFromServiceResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createIndexWithResponse#SearchIndex-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndex(String)}
     */
    public void getSearchIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndex#String
        SearchIndex indexFromService =
            searchIndexClient.getIndex("searchIndex");
        System.out.printf("The index name is %s. The etag of index is %s.%n", indexFromService.getName(),
            indexFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndex#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexWithResponse(String, Context)}}
     */
    public void getSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-Context
        Response<SearchIndex> indexFromServiceResponse =
            searchIndexClient.getIndexWithResponse("searchIndex", new Context(key1, value1));

        System.out.printf("The status code of the response is %s. The index name is %s.%n",
            indexFromServiceResponse.getStatusCode(), indexFromServiceResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexStatistics(String)}
     */
    public void getSearchIndexStatistics() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String
        SearchIndexStatistics statistics = searchIndexClient.getIndexStatistics("searchIndex");
        System.out.printf("There are %d documents and storage size of %d available in 'searchIndex'.%n",
            statistics.getDocumentCount(), statistics.getStorageSize());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexStatisticsWithResponse(String, Context)}
     */
    public void getSearchIndexStatisticsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatisticsWithResponse#String-Context
        Response<SearchIndexStatistics> statistics = searchIndexClient.getIndexStatisticsWithResponse("searchIndex",
            new Context(key1, value1));
        System.out.printf("The status code of the response is %s.%n"
                + "There are %d documents and storage size of %d available in 'searchIndex'.%n",
            statistics.getStatusCode(), statistics.getValue().getDocumentCount(),
            statistics.getValue().getStorageSize());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatisticsWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#listIndexes()}
     */
    public void listIndexes() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexes
        PagedIterable<SearchIndex> indexes = searchIndexClient.listIndexes();
        for (SearchIndex index: indexes) {
            System.out.printf("The index name is %s. The etag of index is %s.%n", index.getName(),
                index.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexes
    }

    /**
     * Code snippet for {@link SearchIndexClient#listIndexes(Context)}
     */
    public void listIndexesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context
        PagedIterable<SearchIndex> indexes = searchIndexClient.listIndexes(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + indexes.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndex index: indexes) {
            System.out.printf("The index name is %s. The etag of index is %s.%n", index.getName(), index.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#listIndexNames()}
     */
    public void listIndexNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames
        PagedIterable<String> indexes = searchIndexClient.listIndexNames();
        for (String indexName: indexes) {
            System.out.printf("The index name is %s.%n", indexName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames
    }

    /**
     * Code snippet for {@link SearchIndexClient#listIndexNames(Context)}
     */
    public void listIndexNamesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames#Context
        PagedIterable<String> indexes = searchIndexClient.listIndexNames(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + indexes.iterableByPage().iterator().next().getStatusCode());
        for (String indexName: indexes) {
            System.out.printf("The index name is %s.%n", indexName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames#Context
    }


    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateIndex(SearchIndex)}
     */
    public void createOrUpdateIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndex#SearchIndex
        SearchIndex indexFromService = searchIndexClient.getIndex("searchIndex");
        indexFromService.setSuggesters(Collections.singletonList(new SearchSuggester("sg",
            Collections.singletonList("hotelName"))));
        SearchIndex updatedIndex = searchIndexClient.createOrUpdateIndex(indexFromService);
        System.out.printf("The index name is %s. The suggester name of index is %s.%n", updatedIndex.getName(),
            updatedIndex.getSuggesters().get(0).getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexClient#createIndexWithResponse(SearchIndex, Context)}
     */
    public void createOrUpdateIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context
        SearchIndex indexFromService = searchIndexClient.getIndex("searchIndex");
        indexFromService.setSuggesters(Collections.singletonList(new SearchSuggester("sg",
            Collections.singletonList("hotelName"))));
        Response<SearchIndex> updatedIndexResponse = searchIndexClient.createOrUpdateIndexWithResponse(indexFromService, true,
            false, new Context(key1, value1));
        System.out.printf("The status code of the normal response is %s.%n"
                + "The index name is %s. The etag of index is %s.%n", updatedIndexResponse.getStatusCode(),
            updatedIndexResponse.getValue().getName(), updatedIndexResponse.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteIndex(String)}
     */
    public void deleteSearchIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteIndex#String
        searchIndexClient.deleteIndex("searchIndex");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteIndex#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteIndexWithResponse(SearchIndex, boolean, Context)}
     */
    public void deleteSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#SearchIndex-boolean-Context
        SearchIndex indexFromService = searchIndexClient.getIndex("searchIndex");
        Response<Void> deleteResponse = searchIndexClient.deleteIndexWithResponse(indexFromService, true,
            new Context(key1, value1));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#SearchIndex-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#analyzeText(String, AnalyzeTextOptions)}
     */
    public void analyzeText() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions
        PagedIterable<AnalyzedTokenInfo> tokenInfos = searchIndexClient.analyzeText("searchIndex",
            new AnalyzeTextOptions("The quick brown fox", LexicalTokenizerName.CLASSIC));
        for (AnalyzedTokenInfo tokenInfo : tokenInfos) {
            System.out.printf("The token emitted by the analyzer is %s.%n", tokenInfo.getToken());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#analyzeText(String, AnalyzeTextOptions, Context)}
     */
    public void analyzeTextResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions-Context
        PagedIterable<AnalyzedTokenInfo> tokenInfos = searchIndexClient.analyzeText("searchIndex",
            new AnalyzeTextOptions("The quick brown fox", LexicalTokenizerName.CLASSIC), new Context(key1, value1));
        System.out.println("The status code of the response is "
            + tokenInfos.iterableByPage().iterator().next().getStatusCode());
        for (AnalyzedTokenInfo tokenInfo : tokenInfos) {
            System.out.printf("The token emitted by the analyzer is %s.%n", tokenInfo.getToken());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions-Context
    }

    /**
     * Code snippet for creating {@link SearchIndexClient#createSynonymMap(SynonymMap)}.
     */
    public void createSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMap#SynonymMap
        SynonymMap synonymMap = new SynonymMap("synonymMap",
            "United States, United States of America, USA\nWashington, Wash. => WA");
        SynonymMap synonymMapFromService = searchIndexClient.createSynonymMap(synonymMap);
        System.out.printf("The synonym map name is %s. The etag of synonym map is %s.%n",
            synonymMapFromService.getName(), synonymMapFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexClient#createIndexWithResponse(SearchIndex, Context)}.
     */
    public void createSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMapWithResponse#SynonymMap-Context
        SynonymMap synonymMap = new SynonymMap("synonymMap",
            "United States, United States of America, USA\nWashington, Wash. => WA");
        Response<SynonymMap> synonymMapFromService = searchIndexClient.createSynonymMapWithResponse(synonymMap,
            new Context(key1, value1));
        System.out.printf("The status code of the response is %d.%n"
                + "The synonym map name is %s. The etag of synonym map is %s.%n", synonymMapFromService.getStatusCode(),
            synonymMapFromService.getValue().getName(), synonymMapFromService.getValue().getETag());
        // END:com.azure.search.documents.indexes.SearchIndexClient.createSynonymMapWithResponse#SynonymMap-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#getSynonymMap(String)}
     */
    public void getSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMap#String
        SynonymMap synonymMapFromService =
            searchIndexClient.getSynonymMap("synonymMap");
        System.out.printf("The synonym map is %s. The etag of synonym map is %s.%n", synonymMapFromService.getName(),
            synonymMapFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMap#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getSynonymMapWithResponse(String, Context)}}
     */
    public void getSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-Context
        Response<SynonymMap> synonymMapFromService =
            searchIndexClient.getSynonymMapWithResponse("synonymMap", new Context(key1, value1));
        System.out.printf("The status code of the response is %d.%n"
                + "The synonym map name is %s. The etag of synonym map is %s.%n", synonymMapFromService.getStatusCode(),
            synonymMapFromService.getValue().getName(), synonymMapFromService.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMaps()}
     */
    public void listSynonymMaps() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps
        PagedIterable<SynonymMap> synonymMaps = searchIndexClient.listSynonymMaps();
        for (SynonymMap synonymMap: synonymMaps) {
            System.out.printf("The synonymMap name is %s. The etag of synonymMap is %s.%n", synonymMap.getName(),
                synonymMap.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMaps(Context)}
     */
    public void listSynonymMapsWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context
        PagedIterable<SynonymMap> synonymMaps = searchIndexClient.listSynonymMaps(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + synonymMaps.iterableByPage().iterator().next().getStatusCode());
        for (SynonymMap index: synonymMaps) {
            System.out.printf("The index name is %s. The etag of index is %s.%n", index.getName(), index.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMapNames()}
     */
    public void listSynonymMapNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNames
        PagedIterable<String> synonymMaps = searchIndexClient.listSynonymMapNames();
        for (String synonymMap: synonymMaps) {
            System.out.printf("The synonymMap name is %s.%n", synonymMap);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNames
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMapNames(Context)}
     */
    public void listSynonymMapNamesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNamesWithResponse#Context
        PagedIterable<String> synonymMaps = searchIndexClient.listIndexNames(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + synonymMaps.iterableByPage().iterator().next().getStatusCode());
        for (String synonymMapNames: synonymMaps) {
            System.out.printf("The synonymMap name is %s.%n", synonymMapNames);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNamesWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateSynonymMap(SynonymMap)}
     */
    public void createOrUpdateSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMap#SynonymMap
        SynonymMap synonymMap = searchIndexClient.getSynonymMap("searchIndex");
        synonymMap.setSynonyms("United States, United States of America, USA, America\nWashington, Wash. => WA");
        SynonymMap updatedSynonymMap = searchIndexClient.createOrUpdateSynonymMap(synonymMap);
        System.out.printf("The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getName(),
            updatedSynonymMap.getSynonyms());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateSynonymMapWithResponse(SynonymMap, boolean, Context)}
     */
    public void createOrUpdateSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context
        SynonymMap synonymMap = searchIndexClient.getSynonymMap("searchIndex");
        synonymMap.setSynonyms("United States, United States of America, USA, America\nWashington, Wash. => WA");
        Response<SynonymMap> updatedSynonymMap =
            searchIndexClient.createOrUpdateSynonymMapWithResponse(synonymMap, true,
                new Context(key1, value1));
        System.out.printf("The status code of the normal response is %s.%n"
                + "The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getStatusCode(),
            updatedSynonymMap.getValue().getName(), updatedSynonymMap.getValue().getSynonyms());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteSynonymMap(String)}
     */
    public void deleteSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMap#String
        searchIndexClient.deleteSynonymMap("synonymMap");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMap#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteSynonymMapWithResponse(SynonymMap, boolean, Context)}
     */
    public void deleteSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#SynonymMap-boolean-Context
        SynonymMap synonymMap = searchIndexClient.getSynonymMap("synonymMap");
        Response<Void> response = searchIndexClient.deleteSynonymMapWithResponse(synonymMap, true,
            new Context(key1, value1));
        System.out.println("The status code of the response is" + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#SynonymMap-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#getServiceStatistics()}
     */
    public void getServiceStatistics() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatistics
        SearchServiceStatistics serviceStatistics = searchIndexClient.getServiceStatistics();
        System.out.printf("There are %s search indexes in your service.%n",
            serviceStatistics.getCounters().getIndexCounter());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatistics
    }

    /**
     * Code snippet for {@link SearchIndexClient#getServiceStatisticsWithResponse(Context)}
     */
    public void getServiceStatisticsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#Context
        Response<SearchServiceStatistics> serviceStatistics =
            searchIndexClient.getServiceStatisticsWithResponse(new Context(key1, value1));
        System.out.printf("The status code of the response is %s.%nThere are %s search indexes in your service.%n",
            serviceStatistics.getStatusCode(),
            serviceStatistics.getValue().getCounters().getIndexCounter());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#Context
    }

    private SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link SearchIndexAsyncClient}.
     */
    public void createSearchIndexAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.instantiation
        SearchIndexAsyncClient searchIndexAsyncClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.instantiation
    }

    private SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder().buildClient();
    /**
     * Code snippet for creating a {@link SearchIndexerClient}.
     */
    public void createSearchIndexerClientFromBuilder() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.instantiation
        SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.indexes.SearchIndexerClient.instantiation
    }

    /**
     * Code snippet for creating {@link SearchIndexerClient#createIndexer(SearchIndexer)}.
     */
    public void createSearchIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createIndexer#SearchIndexer
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        SearchIndexer indexerFromService = searchIndexerClient.createIndexer(searchIndexer);
        System.out.printf("The indexer name is %s. The etag of indexer is %s.%n", indexerFromService.getName(),
            indexerFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createIndexer#SearchIndexer
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createIndexerWithResponse(SearchIndexer, Context)}.
     */
    public void createSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createIndexerWithResponse#SearchIndexer-Context
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        Response<SearchIndexer> indexerFromServiceResponse = searchIndexerClient.createIndexerWithResponse(
            searchIndexer, new Context(key1, value1));

        System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
            indexerFromServiceResponse.getStatusCode(), indexerFromServiceResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createIndexerWithResponse#SearchIndexer-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexer(String)}
     */
    public void getSearchIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexer#String
        SearchIndexer indexerFromService =
            searchIndexerClient.getIndexer("searchIndexer");
        System.out.printf("The indexer name is %s. The etag of indexer is %s.%n", indexerFromService.getName(),
            indexerFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexerWithResponse(String, Context)}}
     */
    public void getSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-Context
        Response<SearchIndexer> indexerFromServiceResponse = searchIndexerClient.getIndexerWithResponse(
            "searchIndexer", new Context(key1, value1));

        System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
            indexerFromServiceResponse.getStatusCode(), indexerFromServiceResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listIndexers()}
     */
    public void listIndexers() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexers
        PagedIterable<SearchIndexer> indexers = searchIndexerClient.listIndexers();
        for (SearchIndexer indexer: indexers) {
            System.out.printf("The indexer name is %s. The etag of indexer is %s.%n", indexer.getName(),
                indexer.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexers
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listIndexers(Context)}
     */
    public void listIndexersWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context
        PagedIterable<SearchIndexer> indexers = searchIndexerClient.listIndexers(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + indexers.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndexer indexer: indexers) {
            System.out.printf("The indexer name is %s. The etag of index is %s.%n",
                indexer.getName(), indexer.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listIndexerNames()}
     */
    public void listIndexerNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames
        PagedIterable<String> indexers = searchIndexerClient.listIndexerNames();
        for (String indexerName: indexers) {
            System.out.printf("The indexer name is %s.%n", indexerName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listIndexerNames(Context)}
     */
    public void listIndexerNamesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames#Context
        PagedIterable<String> indexers = searchIndexerClient.listIndexerNames(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + indexers.iterableByPage().iterator().next().getStatusCode());
        for (String indexerName: indexers) {
            System.out.printf("The indexer name is %s.%n", indexerName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames#Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateIndexer(SearchIndexer)}
     */
    public void createOrUpdateIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexer#SearchIndexer
        SearchIndexer searchIndexerFromService = searchIndexerClient.getIndexer("searchIndexer");
        searchIndexerFromService.setFieldMappings(Collections.singletonList(
            new FieldMapping("hotelName").setTargetFieldName("HotelName")));
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        SearchIndexer indexerFromService = searchIndexerClient.createOrUpdateIndexer(searchIndexer);
        System.out.printf("The indexer name is %s. The target field name of indexer is %s.%n",
            indexerFromService.getName(), indexerFromService.getFieldMappings().get(0).getTargetFieldName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexer#SearchIndexer
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateIndexer(SearchIndexer)}
     */
    public void createOrUpdateIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean-Context
        SearchIndexer searchIndexerFromService = searchIndexerClient.getIndexer("searchIndexer");
        searchIndexerFromService.setFieldMappings(Collections.singletonList(
            new FieldMapping("hotelName").setTargetFieldName("HotelName")));
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        Response<SearchIndexer> indexerFromService = searchIndexerClient.createOrUpdateIndexerWithResponse(
            searchIndexer, true, new Context(key1, value1));
        System.out.printf("The status code of the response is %s.%nThe indexer name is %s. "
            + "The target field name of indexer is %s.%n", indexerFromService.getStatusCode(),
            indexerFromService.getValue().getName(),
            indexerFromService.getValue().getFieldMappings().get(0).getTargetFieldName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteIndexer(String)}
     */
    public void deleteSearchIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexer#String
        searchIndexerClient.deleteIndexer("searchIndexer");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteIndexerWithResponse(SearchIndexer, boolean, Context)}
     */
    public void deleteSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#SearchIndexer-boolean-Context
        SearchIndexer searchIndexer = searchIndexerClient.getIndexer("searchIndexer");
        Response<Void> deleteResponse = searchIndexerClient.deleteIndexerWithResponse(searchIndexer, true,
            new Context(key1, value1));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#SearchIndexer-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetIndexer(String)}
     */
    public void resetIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexer#String
        searchIndexerClient.resetIndexer("searchIndexer");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetIndexerWithResponse(String, Context)}
     */
    public void resetIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-Context
        Response<Void> response = searchIndexerClient.resetIndexerWithResponse("searchIndexer",
            new Context(key1, value1));
        System.out.println("The status code of the response is " + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#runIndexer(String)}
     */
    public void runIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.runIndexer#String
        searchIndexerClient.runIndexer("searchIndexer");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.runIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#runIndexerWithResponse(String, Context)}
     */
    public void runIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-Context
        Response<Void> response = searchIndexerClient.runIndexerWithResponse("searchIndexer",
            new Context(key1, value1));
        System.out.println("The status code of the response is " + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexerStatus(String)}
     */
    public void getIndexerStatus() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatus#String
        SearchIndexerStatus indexerStatus = searchIndexerClient.getIndexerStatus("searchIndexer");
        System.out.printf("The indexer status is %s.%n", indexerStatus.getStatus());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatus#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexerStatusWithResponse(String, Context)}
     */
    public void getIndexerStatusWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-Context
        Response<SearchIndexerStatus> response = searchIndexerClient.getIndexerStatusWithResponse("searchIndexer",
            new Context(key1, value1));
        System.out.printf("The status code of the response is %s.%nThe indexer status is %s.%n",
            response.getStatusCode(), response.getValue().getStatus());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-Context
    }

    /**
     * Code snippet for creating {@link SearchIndexerClient#createDataSourceConnection(SearchIndexerDataSourceConnection)}.
     */
    public void createDataSource() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnection#SearchIndexerDataSourceConnection
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            com.azure.search.documents.indexes.models.SearchIndexerDataSourceType.AZURE_BLOB, "{connectionString}",
            new com.azure.search.documents.indexes.models.SearchIndexerDataContainer("container"));
        SearchIndexerDataSourceConnection dataSourceFromService =
            searchIndexerClient.createDataSourceConnection(dataSource);
        System.out.printf("The data source name is %s. The etag of data source is %s.%n",
            dataSourceFromService.getName(), dataSourceFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, Context)}.
     */
    public void createDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-Context
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            SearchIndexerDataSourceType.AZURE_BLOB, "{connectionString}",
            new SearchIndexerDataContainer("container"));
        Response<SearchIndexerDataSourceConnection> dataSourceFromService =
            searchIndexerClient.createDataSourceConnectionWithResponse(dataSource, new Context(key1, value1));

        System.out.printf("The status code of the response is %s. The data source name is %s.%n",
            dataSourceFromService.getStatusCode(), dataSourceFromService.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getDataSourceConnection(String)}
     */
    public void getDataSource() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnection#String
        SearchIndexerDataSourceConnection dataSource =
            searchIndexerClient.getDataSourceConnection("dataSource");
        System.out.printf("The dataSource name is %s. The etag of dataSource is %s.%n", dataSource.getName(),
            dataSource.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnection#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getDataSourceConnectionWithResponse(String, Context)}
     */
    public void getDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-Context
        Response<SearchIndexerDataSourceConnection> dataSource =
            searchIndexerClient.getDataSourceConnectionWithResponse(
                "dataSource", new Context(key1, value1));

        System.out.printf("The status code of the response is %s. The data source name is %s.%n",
            dataSource.getStatusCode(), dataSource.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnections()}
     */
    public void listDataSources() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnections
        PagedIterable<SearchIndexerDataSourceConnection> dataSources = searchIndexerClient.listDataSourceConnections();
        for (SearchIndexerDataSourceConnection dataSource: dataSources) {
            System.out.printf("The dataSource name is %s. The etag of dataSource is %s.%n", dataSource.getName(),
                dataSource.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnections
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnections(Context)}
     */
    public void listDataSourcesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionsWithResponse#Context
        PagedIterable<SearchIndexerDataSourceConnection> dataSources =
            searchIndexerClient.listDataSourceConnections(new Context(key1, value1));

        System.out.println("The status code of the response is"
            + dataSources.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndexerDataSourceConnection dataSource: dataSources) {
            System.out.printf("The dataSource name is %s. The etag of dataSource is %s.%n",
                dataSource.getName(), dataSource.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionsWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnectionNames()}
     */
    public void listDataSourceNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNames
        PagedIterable<String> dataSources = searchIndexerClient.listDataSourceConnectionNames();
        for (String dataSourceName: dataSources) {
            System.out.printf("The dataSource name is %s.%n", dataSourceName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNames
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnectionNames(Context)}
     */
    public void listDataSourceNamesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNamesWithContext#Context
        PagedIterable<String> dataSources = searchIndexerClient.listDataSourceConnectionNames(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + dataSources.iterableByPage().iterator().next().getStatusCode());
        for (String dataSourceName: dataSources) {
            System.out.printf("The dataSource name is %s.%n", dataSourceName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNamesWithContext#Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateDataSourceConnection(SearchIndexerDataSourceConnection)}
     */
    public void createOrUpdateDataSource() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection
        SearchIndexerDataSourceConnection dataSource = searchIndexerClient.getDataSourceConnection("dataSource");
        dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));

        SearchIndexerDataSourceConnection updateDataSource = searchIndexerClient.createOrUpdateDataSourceConnection(dataSource);
        System.out.printf("The dataSource name is %s. The container name of dataSource is %s.%n",
            updateDataSource.getName(), updateDataSource.getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createIndexerWithResponse(SearchIndexer, Context)}
     */
    public void createOrUpdateDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context
        SearchIndexerDataSourceConnection dataSource = searchIndexerClient.getDataSourceConnection("dataSource");
        dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));

        Response<SearchIndexerDataSourceConnection> updateDataSource = searchIndexerClient
            .createOrUpdateDataSourceConnectionWithResponse(dataSource, true, new Context(key1, value1));
        System.out.printf("The status code of the response is %s.%nThe dataSource name is %s. "
            + "The container name of dataSource is %s.%n", updateDataSource.getStatusCode(),
            updateDataSource.getValue().getName(), updateDataSource.getValue().getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteDataSourceConnection(String)}
     */
    public void deleteDataSource() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnection#String
        searchIndexerClient.deleteDataSourceConnection("dataSource");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnection#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, boolean, Context)}
     */
    public void deleteDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context
        SearchIndexerDataSourceConnection dataSource =
            searchIndexerClient.getDataSourceConnection("dataSource");
        Response<Void> deleteResponse = searchIndexerClient.deleteDataSourceConnectionWithResponse(dataSource, true,
            new Context(key1, value1));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createSkillset(SearchIndexerSkillset)}
     */
    public void createSearchIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createSkillset#SearchIndexerSkillset
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image")
                .setSource("/document/normalized_images/*")
        );

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text")
                .setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText")
                .setTargetName("myLayoutText")
        );
        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset("searchIndexerSkillset",
            Collections.singletonList(new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*")));
        SearchIndexerSkillset skillset = searchIndexerClient.createSkillset(searchIndexerSkillset);
        System.out.printf("The indexer skillset name is %s. The etag of indexer skillset is %s.%n",
            skillset.getName(), skillset.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createSkillsetWithResponse(SearchIndexerSkillset, Context)}.
     */
    public void createSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createSkillsetWithResponse#SearchIndexerSkillset-Context
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image")
                .setSource("/document/normalized_images/*")
        );

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text")
                .setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText")
                .setTargetName("myLayoutText")
        );
        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset("searchIndexerSkillset",
            Collections.singletonList(new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*")));
        Response<SearchIndexerSkillset> skillsetWithResponse =
            searchIndexerClient.createSkillsetWithResponse(searchIndexerSkillset, new Context(key1, value1));
        System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
            skillsetWithResponse.getStatusCode(), skillsetWithResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createSkillsetWithResponse#SearchIndexerSkillset-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getSkillset(String)}
     */
    public void getSearchIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getSearchIndexerSkillset#String
        SearchIndexerSkillset indexerSkillset =
            searchIndexerClient.getSkillset("searchIndexerSkillset");
        System.out.printf("The indexer skillset name is %s. The etag of indexer skillset is %s.%n",
            indexerSkillset.getName(), indexerSkillset.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getSearchIndexerSkillset#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getSkillsetWithResponse(String, Context)}
     */
    public void getSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-Context
        Response<SearchIndexerSkillset> skillsetWithResponse = searchIndexerClient.getSkillsetWithResponse(
            "searchIndexerSkillset", new Context(key1, value1));

        System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
            skillsetWithResponse.getStatusCode(), skillsetWithResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsets()}
     */
    public void listIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets
        PagedIterable<SearchIndexerSkillset> indexerSkillsets = searchIndexerClient.listSkillsets();
        for (SearchIndexerSkillset skillset: indexerSkillsets) {
            System.out.printf("The skillset name is %s. The etag of skillset is %s.%n", skillset.getName(),
                skillset.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsets(Context)}
     */
    public void listIndexerSkillsetsWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context
        PagedIterable<SearchIndexerSkillset> indexerSkillsets = searchIndexerClient.listSkillsets(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + indexerSkillsets.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndexerSkillset skillset: indexerSkillsets) {
            System.out.printf("The skillset name is %s. The etag of skillset is %s.%n",
                skillset.getName(), skillset.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsetNames()}
     */
    public void listIndexerSkillsetNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNames
        PagedIterable<String> skillsetNames = searchIndexerClient.listSkillsetNames();
        for (String skillsetName: skillsetNames) {
            System.out.printf("The indexer skillset name is %s.%n", skillsetName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNames
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsetNames(Context)}
     */
    public void listIndexerSkillsetNamesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNamesWithResponse#Context
        PagedIterable<String> skillsetNames = searchIndexerClient.listSkillsetNames(new Context(key1, value1));
        System.out.println("The status code of the response is"
            + skillsetNames.iterableByPage().iterator().next().getStatusCode());
        for (String skillsetName: skillsetNames) {
            System.out.printf("The indexer skillset name is %s.%n", skillsetName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNamesWithResponse#Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateSkillset(SearchIndexerSkillset)}
     */
    public void createOrUpdateIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset
        SearchIndexerSkillset indexerSkillset = searchIndexerClient.getSkillset("searchIndexerSkilset");
        indexerSkillset.setDescription("This is new description!");
        SearchIndexerSkillset updateSkillset = searchIndexerClient.createOrUpdateSkillset(indexerSkillset);
        System.out.printf("The indexer skillset name is %s. The description of indexer skillset is %s.%n",
            updateSkillset.getName(), updateSkillset.getDescription());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateSkillsetWithResponse(SearchIndexerSkillset, boolean, Context)}
     */
    public void createOrUpdateIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
        SearchIndexerSkillset indexerSkillset = searchIndexerClient.getSkillset("searchIndexerSkilset");
        indexerSkillset.setDescription("This is new description!");
        Response<SearchIndexerSkillset> updateSkillsetResponse = searchIndexerClient.createOrUpdateSkillsetWithResponse(
            indexerSkillset, true, new Context(key1, value1));
        System.out.printf("The status code of the response is %s.%nThe indexer skillset name is %s. "
                + "The description of indexer skilset is %s.%n", updateSkillsetResponse.getStatusCode(),
            updateSkillsetResponse.getValue().getName(),
            updateSkillsetResponse.getValue().getDescription());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteSkillset(String)}
     */
    public void deleteSearchIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillset#String
        searchIndexerClient.deleteSkillset("searchIndexerSkillset");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillset#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteSkillsetWithResponse(SearchIndexerSkillset, boolean, Context)}
     */
    public void deleteSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
        SearchIndexerSkillset searchIndexerSkilset = searchIndexerClient.getSkillset("searchIndexerSkilset");
        Response<Void> deleteResponse = searchIndexerClient.deleteSkillsetWithResponse(searchIndexerSkilset, true,
            new Context(key1, value1));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
    }

    private SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link SearchIndexerAsyncClient}.
     */
    public void createSearchIndexerAsyncClientFromBuilder() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.instantiation
        SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.instantiation
    }
}
