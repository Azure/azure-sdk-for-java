// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.AnalyzeResult;
import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.DataSourceCredentials;
import com.azure.search.documents.indexes.models.DocumentKeysOrIds;
import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.GetIndexStatisticsResult;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.LexicalTokenizerName;
import com.azure.search.documents.indexes.models.ListDataSourcesResult;
import com.azure.search.documents.indexes.models.ListIndexersResult;
import com.azure.search.documents.indexes.models.ListSkillsetsResult;
import com.azure.search.documents.indexes.models.ListSynonymMapsResult;
import com.azure.search.documents.indexes.models.OcrSkill;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchAlias;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SkillNames;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.AutocompleteMode;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.AutocompleteResult;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsOptions;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.models.LookupDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchPagedFlux;
import com.azure.search.documents.models.SearchPagedIterable;
import com.azure.search.documents.models.SearchPagedResponse;
import com.azure.search.documents.models.SuggestDocumentsResult;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused")
public class SearchJavaDocCodeSnippets {
    private static final SearchClient SEARCH_CLIENT = new SearchClientBuilder().buildClient();
    private static final long SEARCH_SKIP_LIMIT = 100_000; // May change over time

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

    /**
     * Code snippet for {@link SearchClient#indexDocuments(IndexDocumentsBatch)}.
     */
    public void uploadDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-upload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        IndexDocumentsResult result = SEARCH_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument)));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s upload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-upload
    }

    /**
     * Code snippet for
     * {@link SearchClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void uploadDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-upload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.indexDocumentsWithResponse(
            new IndexDocumentsBatch(new IndexAction().setActionType(IndexActionType.UPLOAD)
                .setAdditionalProperties(searchDocument)), null,
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s upload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-upload
    }

    /**
     * Code snippet for {@link SearchClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void mergeDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-merge
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelName", "merge");
        IndexDocumentsResult result = SEARCH_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(searchDocument)));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-merge
    }

    /**
     * Code snippet for {@link SearchClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void mergeDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-merge
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.indexDocumentsWithResponse(
            new IndexDocumentsBatch(new IndexAction().setActionType(IndexActionType.MERGE)
                .setAdditionalProperties(searchDocument)), null,
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-merge
    }

    /**
     * Code snippet for {@link SearchClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void mergeOrUploadDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-mergeOrUpload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        IndexDocumentsResult result = SEARCH_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE_OR_UPLOAD).setAdditionalProperties(searchDocument)));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-mergeOrUpload
    }

    /**
     * Code snippet for {@link SearchClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void mergeOrUploadDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-mergeOrUpload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.indexDocumentsWithResponse(
            new IndexDocumentsBatch(new IndexAction().setActionType(IndexActionType.MERGE_OR_UPLOAD)
                .setAdditionalProperties(searchDocument)), null,
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-mergeOrUpload
    }

    /**
     * Code snippet for {@link SearchClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void deleteDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-delete
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        IndexDocumentsResult result = SEARCH_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(searchDocument)));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch-delete
    }


    /**
     * Code snippet for {@link SearchClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void deleteDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-delete
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.indexDocumentsWithResponse(
            new IndexDocumentsBatch(new IndexAction().setActionType(IndexActionType.DELETE)
                .setAdditionalProperties(searchDocument)), null,
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-delete
    }

    /**
     * Code snippet for {@link SearchClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void indexDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch
        Map<String, Object> searchDocument1 = new LinkedHashMap<>();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        Map<String, Object> searchDocument2 = new LinkedHashMap<>();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch indexDocumentsBatch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument1),
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(searchDocument2));
        IndexDocumentsResult result = SEARCH_CLIENT.indexDocuments(indexDocumentsBatch);
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s finish successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch
    }

    /**
     * Code snippet for {@link SearchClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void indexDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions
        Map<String, Object> searchDocument1 = new LinkedHashMap<>();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        Map<String, Object> searchDocument2 = new LinkedHashMap<>();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch indexDocumentsBatch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument1),
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(searchDocument2));
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.indexDocumentsWithResponse(indexDocumentsBatch,
            null, new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s finish successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions
    }

    /**
     * Code snippet for {@link SearchClient#getDocument(String)}
     */
    public void getDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.getDocuments#String
        LookupDocument result = SEARCH_CLIENT.getDocument("hotelId");
        result.getAdditionalProperties()
            .forEach((key, value) -> System.out.printf("Document key %s, Document value %s", key, value));
        // END: com.azure.search.documents.SearchClient.getDocuments#String
    }

    /**
     * Code snippet for {@link SearchClient#getDocumentWithResponse(String, RequestOptions)}
     */
    public void getDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.getDocumentWithResponse#String-RequestOptions
        Response<LookupDocument> resultResponse = SEARCH_CLIENT.getDocumentWithResponse("hotelId",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        LookupDocument document = resultResponse.getValue();
        document.getAdditionalProperties()
            .forEach((key, value) -> System.out.printf("Document key %s, Document value %s", key, value));
        // END: com.azure.search.documents.SearchClient.getDocumentWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchClient#getDocumentCount()}
     */
    public void getDocumentCount() {
        // BEGIN: com.azure.search.documents.SearchClient.getDocumentCount
        long count = SEARCH_CLIENT.getDocumentCount();
        System.out.printf("There are %d documents in service.", count);
        // END: com.azure.search.documents.SearchClient.getDocumentCount
    }

    /**
     * Code snippet for {@link SearchClient#getDocumentCountWithResponse(RequestOptions)}
     */
    public void getDocumentCountWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.getDocumentCountWithResponse#RequestOptions
        Response<Long> countResponse = SEARCH_CLIENT.getDocumentCountWithResponse(
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + countResponse.getStatusCode());
        System.out.printf("There are %d documents in service.", countResponse.getValue());
        // END: com.azure.search.documents.SearchClient.getDocumentCountWithResponse#RequestOptions
    }

    /**
     * Code snippet for {@link SearchClient#search(SearchOptions)}
     */
    public void searchDocumentsWithOptions() {
        // BEGIN: com.azure.search.documents.SearchClient.search#SearchOptions
        SearchPagedIterable searchPagedIterable = SEARCH_CLIENT.search(new SearchOptions()
            .setSearchText("searchText").setOrderBy("hotelId desc"));

        boolean firstPage = true;
        long numberOfDocumentsReturned = 0;
        for (SearchPagedResponse resultResponse: searchPagedIterable.iterableByPage()) {
            if (firstPage) {
                System.out.printf("There are around %d results.", resultResponse.getCount());
                firstPage = false;
            }
            numberOfDocumentsReturned += resultResponse.getElements().stream().count();
            resultResponse.getElements().forEach(searchResult -> searchResult.getAdditionalProperties()
                .forEach((key, value) -> System.out.printf("Document key %s, document value %s", key, value)));

            if (numberOfDocumentsReturned >= SEARCH_SKIP_LIMIT) {
                // Reached the $skip limit, stop requesting more documents.
                break;
            }
        }
        // END: com.azure.search.documents.SearchClient.search#SearchOptions
    }

    /**
     * Code snippet for {@link SearchClient#suggest(SuggestOptions)}
     */
    public void suggestDocumentsWithOptions() {
        // BEGIN: com.azure.search.documents.SearchClient.suggest#SuggestOptions
        SuggestDocumentsResult results = SEARCH_CLIENT.suggest(new SuggestOptions("searchText", "sg")
            .setOrderBy("hotelId desc"));
        for (SuggestResult result : results.getResults()) {
            result.getAdditionalProperties()
                .forEach((key, value) -> System.out.printf("Document key %s, document value %s", key, value));
        }
        // END: com.azure.search.documents.SearchClient.suggest#SuggestOptions
    }

    /**
     * Code snippet for {@link SearchClient#autocomplete(AutocompleteOptions)}
     */
    public void autocompleteDocumentsWithOptions() {
        // BEGIN: com.azure.search.documents.SearchClient.autocomplete#AutocompleteOptions
        AutocompleteResult results = SEARCH_CLIENT.autocomplete(new AutocompleteOptions("searchText", "sg")
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT));
        for (AutocompleteItem result : results.getResults()) {
            System.out.printf("The complete term is %s", result.getText());
        }
        // END: com.azure.search.documents.SearchClient.autocomplete#AutocompleteOptions
    }

    private static final SearchAsyncClient SEARCH_ASYNC_CLIENT = new SearchClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocuments(IndexDocumentsBatch)}.
     */
    public void uploadDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-upload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument)))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s upload successfully? %b%n",
                        indexingResult.getKey(), indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-upload
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void uploadDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-upload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.indexDocumentsWithResponse(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument)), null,
            null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s upload successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-upload
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void mergeDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-merge
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelName", "merge");
        SEARCH_ASYNC_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(searchDocument)))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-merge
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void mergeDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-merge
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.indexDocumentsWithResponse(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE).setAdditionalProperties(searchDocument)),
            null, null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-merge
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void mergeOrUploadDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-mergeOrUpload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE_OR_UPLOAD).setAdditionalProperties(searchDocument)))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n",
                        indexingResult.getKey(), indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-mergeOrUpload
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void mergeOrUploadDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-mergeOrUpload
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.indexDocumentsWithResponse(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.MERGE_OR_UPLOAD).setAdditionalProperties(searchDocument)),
            null, null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n",
                        indexingResult.getKey(), indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-mergeOrUpload
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void deleteDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-delete
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(searchDocument)))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch-delete
    }


    /**
     * Code snippet for {@link SearchAsyncClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void deleteDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-delete
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.indexDocumentsWithResponse(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(searchDocument)), null,
                null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions-delete
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void indexDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch
        Map<String, Object> searchDocument1 = new LinkedHashMap<>();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        Map<String, Object> searchDocument2 = new LinkedHashMap<>();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch indexDocumentsBatch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument1),
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(searchDocument2));
        SEARCH_ASYNC_CLIENT.indexDocuments(indexDocumentsBatch)
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s finish successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, RequestOptions)}
     */
    public void indexDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions
        Map<String, Object> searchDocument1 = new LinkedHashMap<>();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        Map<String, Object> searchDocument2 = new LinkedHashMap<>();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch indexDocumentsBatch = new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(searchDocument1),
            new IndexAction().setActionType(IndexActionType.DELETE).setAdditionalProperties(searchDocument2));
        SEARCH_ASYNC_CLIENT.indexDocumentsWithResponse(indexDocumentsBatch, null, null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s finish successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-RequestOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#getDocument(String)}
     */
    public void getDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.getDocuments#String
        SEARCH_ASYNC_CLIENT.getDocument("hotelId")
            .subscribe(result -> result.getAdditionalProperties()
                .forEach((key, value) -> System.out.printf("Document key %s, Document value %s", key, value)));
        // END: com.azure.search.documents.SearchAsyncClient.getDocuments#String
    }

    /**
     * Code snippet for {@link SearchAsyncClient#getDocumentWithResponse(String, RequestOptions)}
     */
    public void getDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.getDocumentWithResponse#String-RequestOptions
        SEARCH_ASYNC_CLIENT.getDocumentWithResponse("hotelId", null)
            .subscribe(response -> {
                System.out.println("The status code of the response is " + response.getStatusCode());
                response.getValue().getAdditionalProperties()
                    .forEach((key, value) -> System.out.printf("Document key %s, Document value %s", key, value));
            });
        // END: com.azure.search.documents.SearchAsyncClient.getDocumentWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#getDocumentCount()}
     */
    public void getDocumentCountAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.getDocumentCount
        SEARCH_ASYNC_CLIENT.getDocumentCount()
            .subscribe(count -> System.out.printf("There are %d documents in service.", count));
        // END: com.azure.search.documents.SearchAsyncClient.getDocumentCount
    }

    /**
     * Code snippet for {@link SearchAsyncClient#getDocumentCountWithResponse(RequestOptions)}
     */
    public void getDocumentCountWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.getDocumentCountWithResponse#RequestOptions
        SEARCH_ASYNC_CLIENT.getDocumentCountWithResponse(new RequestOptions())
            .subscribe(countResponse -> {
                System.out.println("The status code of the response is " + countResponse.getStatusCode());
                System.out.printf("There are %d documents in service.",
                    Long.parseLong(countResponse.getValue().toString()));
            });
        // END: com.azure.search.documents.SearchAsyncClient.getDocumentCountWithResponse#RequestOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#search(SearchOptions)}
     */
    public void searchDocumentsWithOptionsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.search#SearchOptions
        SearchPagedFlux pagedFlux = SEARCH_ASYNC_CLIENT.search(new SearchOptions().setSearchText("searchText")
            .setOrderBy("hotelId desc"));

        AtomicBoolean firstPage = new AtomicBoolean(true);
        AtomicLong numberOfDocumentsReturned = new AtomicLong();
        pagedFlux.byPage()
            .doOnNext(page -> {
                if (firstPage.getAndSet(false)) {
                    System.out.printf("There are around %d results.", page.getCount());
                }
            })
            .takeUntil(page -> {
                // Reached the $skip limit, stop requesting more documents.
                return numberOfDocumentsReturned.addAndGet(page.getElements().stream().count()) >= SEARCH_SKIP_LIMIT;
            })
            .subscribe(page -> page.getElements().forEach(searchDocument -> searchDocument.getAdditionalProperties()
                .forEach((key, value) -> System.out.printf("Document key %s, document value %s", key, value))));
        // END: com.azure.search.documents.SearchAsyncClient.search#SearchOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#suggest(SuggestOptions)}
     */
    public void suggestDocumentsWithOptionsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.suggest#SuggestOptions
        SEARCH_ASYNC_CLIENT.suggest(new SuggestOptions("searchText", "sg").setOrderBy("hotelId desc"))
            .subscribe(results -> results.getResults().forEach(result -> result.getAdditionalProperties()
                .forEach((key, value) -> System.out.printf("Document key %s, document value %s", key, value))));
        // END: com.azure.search.documents.SearchAsyncClient.suggest#SuggestOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#autocomplete(AutocompleteOptions)}
     */
    public void autocompleteDocumentsWithOptionsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.autocomplete#AutocompleteOptions
        SEARCH_ASYNC_CLIENT.autocomplete(new AutocompleteOptions("searchText", "sg")
                .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT))
            .subscribe(results -> results.getResults().forEach(result ->
                System.out.printf("The complete term is %s", result.getText())));
        // END: com.azure.search.documents.SearchAsyncClient.autocomplete#AutocompleteOptions
    }

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

    private static final SearchIndexClient SEARCH_INDEX_CLIENT = new SearchIndexClientBuilder().buildClient();
    private static final String KEY_1 = "key1";
    private static final String VALUE_1 = "val1";

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
        SearchIndex searchIndex = new SearchIndex("searchIndex",
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true));
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.createIndex(searchIndex);
        System.out.printf("The index name is %s. The ETag of index is %s.%n", indexFromService.getName(),
            indexFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexClient#createIndexWithResponse(SearchIndex, RequestOptions)}.
     */
    public void createSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createIndexWithResponse#SearchIndex-RequestOptions
        SearchIndex searchIndex = new SearchIndex("searchIndex",
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true));

        Response<SearchIndex> response = SEARCH_INDEX_CLIENT.createIndexWithResponse(searchIndex,
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %s. The index name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createIndexWithResponse#SearchIndex-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndex(String)}
     */
    public void getSearchIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndex#String
        SearchIndex indexFromService =
            SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        System.out.printf("The index name is %s. The ETag of index is %s.%n", indexFromService.getName(),
            indexFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndex#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexWithResponse(String, RequestOptions)}
     */
    public void getSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-RequestOptions
        Response<SearchIndex> response = SEARCH_INDEX_CLIENT.getIndexWithResponse("searchIndex",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s. The index name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexStatistics(String)}
     */
    public void getSearchIndexStatistics() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String
        GetIndexStatisticsResult statistics = SEARCH_INDEX_CLIENT.getIndexStatistics("searchIndex");
        System.out.printf("There are %d documents and storage size of %d available in 'searchIndex'.%n",
            statistics.getDocumentCount(), statistics.getStorageSize());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexStatisticsWithResponse(String, RequestOptions)}
     */
    public void getSearchIndexStatisticsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatisticsWithResponse#String-RequestOptions
        Response<GetIndexStatisticsResult> response = SEARCH_INDEX_CLIENT.getIndexStatisticsWithResponse("searchIndex",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        GetIndexStatisticsResult statistics = response.getValue();
        System.out.printf("The status code of the response is %s.%n"
                + "There are %d documents and storage size of %d available in 'searchIndex'.%n",
            response.getStatusCode(), statistics.getDocumentCount(), statistics.getStorageSize());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatisticsWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#listIndexes()}
     */
    public void listIndexes() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexes
        PagedIterable<SearchIndex> indexes = SEARCH_INDEX_CLIENT.listIndexes();
        for (SearchIndex index: indexes) {
            System.out.printf("The index name is %s. The ETag of index is %s.%n", index.getName(),
                index.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexes
    }

//    /**
//     * Code snippet for {@link SearchIndexClient#listIndexes(RequestOptions)}
//     */
//    public void listIndexesWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context
//        PagedIterable<SearchIndex> indexes = SEARCH_INDEX_CLIENT.listIndexes(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + indexes.iterableByPage().iterator().next().getStatusCode());
//        for (SearchIndex index: indexes) {
//            System.out.printf("The index name is %s. The ETag of index is %s.%n", index.getName(), index.getETag());
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context
//    }

    /**
     * Code snippet for {@link SearchIndexClient#listIndexNames()}
     */
    public void listIndexNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames
        PagedIterable<String> indexes = SEARCH_INDEX_CLIENT.listIndexNames();
        for (String indexName: indexes) {
            System.out.printf("The index name is %s.%n", indexName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames
    }

//    /**
//     * Code snippet for {@link SearchIndexClient#listIndexNames(RequestOptions)}
//     */
//    public void listIndexNamesWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames#Context
//        PagedIterable<String> indexes = SEARCH_INDEX_CLIENT.listIndexNames(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + indexes.iterableByPage().iterator().next().getStatusCode());
//        for (String indexName: indexes) {
//            System.out.printf("The index name is %s.%n", indexName);
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames#Context
//    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateIndex(SearchIndex)}
     */
    public void createOrUpdateIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndex#SearchIndex
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        indexFromService.setSuggesters(Collections.singletonList(new SearchSuggester("sg",
            Collections.singletonList("hotelName"))));
        SearchIndex updatedIndex = SEARCH_INDEX_CLIENT.createOrUpdateIndex(indexFromService);
        System.out.printf("The index name is %s. The suggester name of index is %s.%n", updatedIndex.getName(),
            updatedIndex.getSuggesters().get(0).getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateIndexWithResponse(SearchIndex, RequestOptions)}
     */
    public void createOrUpdateIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-RequestOptions
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        indexFromService.setSuggesters(new SearchSuggester("sg", "hotelName"));
        Response<SearchIndex> updatedIndexResponse = SEARCH_INDEX_CLIENT.createOrUpdateIndexWithResponse(
            indexFromService, new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, indexFromService.getETag())
                .addQueryParam("allowIndexDowntime", "false").setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the normal response is %s.%n"
                + "The index name is %s. The ETag of index is %s.%n", updatedIndexResponse.getStatusCode(),
            updatedIndexResponse.getValue().getName(), updatedIndexResponse.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteIndex(String)}
     */
    public void deleteSearchIndex() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteIndex#String
        SEARCH_INDEX_CLIENT.deleteIndex("searchIndex");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteIndex#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteIndexWithResponse(String, RequestOptions)}
     */
    public void deleteSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#String-RequestOptions
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        Response<Void> deleteResponse = SEARCH_INDEX_CLIENT.deleteIndexWithResponse(indexFromService.getName(),
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, indexFromService.getETag())
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#analyzeText(String, AnalyzeTextOptions)}
     */
    public void analyzeText() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions
        AnalyzeResult result = SEARCH_INDEX_CLIENT.analyzeText("searchIndex",
            new AnalyzeTextOptions("The quick brown fox").setTokenizerName(LexicalTokenizerName.CLASSIC));
        for (AnalyzedTokenInfo tokenInfo : result.getTokens()) {
            System.out.printf("The token emitted by the analyzer is %s.%n", tokenInfo.getToken());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#analyzeTextWithResponse(String, AnalyzeTextOptions, RequestOptions)}
     */
    public void analyzeTextResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.analyzeTextWithResponse#String-AnalyzeTextOptions-RequestOptions
        Response<AnalyzeResult> response = SEARCH_INDEX_CLIENT.analyzeTextWithResponse("searchIndex",
            new AnalyzeTextOptions("The quick brown fox").setTokenizerName(LexicalTokenizerName.CLASSIC),
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + response.getStatusCode());
        for (AnalyzedTokenInfo tokenInfo : response.getValue().getTokens()) {
            System.out.printf("The token emitted by the analyzer is %s.%n", tokenInfo.getToken());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.analyzeTextWithResponse#String-AnalyzeTextOptions-RequestOptions
    }

    /**
     * Code snippet for creating {@link SearchIndexClient#createSynonymMap(SynonymMap)}.
     */
    public void createSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMap#SynonymMap
        SynonymMap synonymMap = new SynonymMap("synonymMap",
            "United States, United States of America, USA\nWashington, Wash. => WA");
        SynonymMap synonymMapFromService = SEARCH_INDEX_CLIENT.createSynonymMap(synonymMap);
        System.out.printf("The synonym map name is %s. The ETag of synonym map is %s.%n",
            synonymMapFromService.getName(), synonymMapFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexClient#createSynonymMapWithResponse(SynonymMap, RequestOptions)}.
     */
    public void createSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMapWithResponse#SynonymMap-RequestOptions
        Response<SynonymMap> response = SEARCH_INDEX_CLIENT.createSynonymMapWithResponse(
            new SynonymMap("synonymMap", "United States, United States of America, USA\nWashington, Wash. => WA"),
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %d.%n"
                + "The synonym map name is %s. The ETag of synonym map is %s.%n", response.getStatusCode(),
            response.getValue().getName(), response.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMapWithResponse#SynonymMap-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#getSynonymMap(String)}
     */
    public void getSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMap#String
        SynonymMap synonymMapFromService =
            SEARCH_INDEX_CLIENT.getSynonymMap("synonymMap");
        System.out.printf("The synonym map is %s. The ETag of synonym map is %s.%n", synonymMapFromService.getName(),
            synonymMapFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMap#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getSynonymMapWithResponse(String, RequestOptions)}
     */
    public void getSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-RequestOptions
        Response<SynonymMap> response = SEARCH_INDEX_CLIENT.getSynonymMapWithResponse("synonymMap",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %d.%n"
                + "The synonym map name is %s. The ETag of synonym map is %s.%n", response.getStatusCode(),
            response.getValue().getName(), response.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMaps()}
     */
    public void listSynonymMaps() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps
        ListSynonymMapsResult synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMaps();
        for (SynonymMap synonymMap: synonymMaps.getSynonymMaps()) {
            System.out.printf("The synonymMap name is %s. The ETag of synonymMap is %s.%n", synonymMap.getName(),
                synonymMap.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps
    }

//    /**
//     * Code snippet for {@link SearchIndexClient#listSynonymMaps(RequestOptions)}
//     */
//    public void listSynonymMapsWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context
//        PagedIterable<SynonymMap> synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMaps(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + synonymMaps.iterableByPage().iterator().next().getStatusCode());
//        for (SynonymMap index: synonymMaps) {
//            System.out.printf("The index name is %s. The ETag of index is %s.%n", index.getName(), index.getETag());
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context
//    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMapNames()}
     */
    public void listSynonymMapNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNames
        List<String> synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMapNames();
        for (String synonymMap: synonymMaps) {
            System.out.printf("The synonymMap name is %s.%n", synonymMap);
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNames
    }

//    /**
//     * Code snippet for {@link SearchIndexClient#listSynonymMapNames(RequestOptions)}
//     */
//    public void listSynonymMapNamesWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNamesWithResponse#Context
//        PagedIterable<String> synonymMaps = SEARCH_INDEX_CLIENT.listIndexNames(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + synonymMaps.iterableByPage().iterator().next().getStatusCode());
//        for (String synonymMapNames: synonymMaps) {
//            System.out.printf("The synonymMap name is %s.%n", synonymMapNames);
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNamesWithResponse#Context
//    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateSynonymMap(SynonymMap)}
     */
    public void createOrUpdateSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMap#SynonymMap
        SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap("synonymMapName");
        synonymMap.getSynonyms().clear();
        synonymMap.getSynonyms().add("United States, United States of America, USA, America\nWashington, Wash. => WA");
        SynonymMap updatedSynonymMap = SEARCH_INDEX_CLIENT.createOrUpdateSynonymMap(synonymMap);
        System.out.printf("The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getName(),
            updatedSynonymMap.getSynonyms());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateSynonymMapWithResponse(SynonymMap, RequestOptions)}
     */
    public void createOrUpdateSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMapWithResponse#SynonymMap-RequestOptions
        SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap("synonymMap");
        synonymMap.getSynonyms().clear();
        synonymMap.getSynonyms().add("United States, United States of America, USA, America\nWashington, Wash. => WA");
        Response<SynonymMap> updatedSynonymMap = SEARCH_INDEX_CLIENT.createOrUpdateSynonymMapWithResponse(synonymMap,
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, synonymMap.getETag())
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the normal response is %s.%n"
                + "The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getStatusCode(),
            updatedSynonymMap.getValue().getName(), updatedSynonymMap.getValue().getSynonyms());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMapWithResponse#SynonymMap-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteSynonymMap(String)}
     */
    public void deleteSynonymMap() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMap#String
        SEARCH_INDEX_CLIENT.deleteSynonymMap("synonymMap");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMap#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteSynonymMapWithResponse(String, RequestOptions)}
     */
    public void deleteSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#String-RequestOptions
        SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap("synonymMap");
        Response<Void> response = SEARCH_INDEX_CLIENT.deleteSynonymMapWithResponse(synonymMap.getName(),
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, synonymMap.getETag())
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is" + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#getServiceStatistics()}
     */
    public void getServiceStatistics() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatistics
        SearchServiceStatistics serviceStatistics = SEARCH_INDEX_CLIENT.getServiceStatistics();
        System.out.printf("There are %s search indexes in your service.%n",
            serviceStatistics.getCounters().getIndexCounter());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatistics
    }

    /**
     * Code snippet for {@link SearchIndexClient#getServiceStatisticsWithResponse(RequestOptions)}
     */
    public void getServiceStatisticsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#RequestOptions
        Response<SearchServiceStatistics> response = SEARCH_INDEX_CLIENT.getServiceStatisticsWithResponse(
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %s.%nThere are %s search indexes in your service.%n",
            response.getStatusCode(), response.getValue().getCounters().getIndexCounter());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#RequestOptions
    }

    private static final SearchIndexAsyncClient SEARCH_INDEX_ASYNC_CLIENT = new SearchIndexClientBuilder()
        .buildAsyncClient();

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

    /**
     * Code snippet for creating {@link SearchIndexAsyncClient#createIndex(SearchIndex)}.
     */
    public void createSearchIndexAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndex#SearchIndex
        SearchIndex searchIndex = new SearchIndex("searchIndex",
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true));
        SEARCH_INDEX_ASYNC_CLIENT.createIndex(searchIndex)
            .subscribe(indexFromService ->
                System.out.printf("The index name is %s. The ETag of index is %s.%n", indexFromService.getName(),
                indexFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createIndexWithResponse(SearchIndex, RequestOptions)}.
     */
    public void createSearchIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndexWithResponse#SearchIndex-RequestOptions
        SearchIndex searchIndex = new SearchIndex("searchIndex",
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true));

        SEARCH_INDEX_ASYNC_CLIENT.createIndexWithResponse(searchIndex, new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The index name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndexWithResponse#SearchIndex-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getIndex(String)}
     */
    public void getSearchIndexAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndex#String
        SEARCH_INDEX_ASYNC_CLIENT.getIndex("searchIndex")
            .subscribe(indexFromService ->
                System.out.printf("The index name is %s. The ETag of index is %s.%n", indexFromService.getName(),
                    indexFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndex#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getIndexWithResponse(String, RequestOptions)}}
     */
    public void getSearchIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexWithResponse#String-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getIndexWithResponse("searchIndex", new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The index name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getIndexStatistics(String)}
     */
    public void getSearchIndexStatisticsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatistics#String
        SEARCH_INDEX_ASYNC_CLIENT.getIndexStatistics("searchIndex")
            .subscribe(statistics ->
                System.out.printf("There are %d documents and storage size of %d available in 'searchIndex'.%n",
                statistics.getDocumentCount(), statistics.getStorageSize()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatistics#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getIndexStatisticsWithResponse(String, RequestOptions)}
     */
    public void getSearchIndexStatisticsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatisticsWithResponse#String-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getIndexStatisticsWithResponse("searchIndex", new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s.%n"
                    + "There are %d documents and storage size of %d available in 'searchIndex'.%n",
                response.getStatusCode(), response.getValue().getDocumentCount(),
                response.getValue().getStorageSize()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatisticsWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#listIndexes()}
     */
    public void listIndexesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexes
        SEARCH_INDEX_ASYNC_CLIENT.listIndexes()
            .subscribe(index ->
                System.out.printf("The index name is %s. The ETag of index is %s.%n", index.getName(),
                    index.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexes
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#listIndexNames()}
     */
    public void listIndexNamesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexNames
        SEARCH_INDEX_ASYNC_CLIENT.listIndexNames()
            .subscribe(indexName -> System.out.printf("The index name is %s.%n", indexName));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.listIndexNames
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateIndex(SearchIndex)}
     */
    public void createOrUpdateIndexAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndex#SearchIndex
        SEARCH_INDEX_ASYNC_CLIENT.getIndex("searchIndex")
            .doOnNext(indexFromService -> indexFromService.setSuggesters(Collections.singletonList(
                new SearchSuggester("sg", Collections.singletonList("hotelName")))))
            .flatMap(SEARCH_INDEX_ASYNC_CLIENT::createOrUpdateIndex)
            .subscribe(updatedIndex ->
                System.out.printf("The index name is %s. The suggester name of index is %s.%n",
                    updatedIndex.getName(), updatedIndex.getSuggesters().get(0).getName()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateIndexWithResponse(SearchIndex, RequestOptions)}
     */
    public void createOrUpdateIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndexWithResponse#SearchIndex-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getIndex("searchIndex")
            .doOnNext(indexFromService -> indexFromService.setSuggesters(new SearchSuggester("sg", "hotelName")))
            .flatMap(indexFromService -> SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateIndexWithResponse(indexFromService,
                new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, indexFromService.getETag())
                    .addQueryParam("allowIndexDowntime", "false")))
            .subscribe(updatedIndexResponse -> System.out.printf("The status code of the normal response is %s.%n"
                    + "The index name is %s. The ETag of index is %s.%n", updatedIndexResponse.getStatusCode(),
                updatedIndexResponse.getValue().getName(), updatedIndexResponse.getValue().getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndexWithResponse#SearchIndex-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#deleteIndex(String)}
     */
    public void deleteSearchIndexAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndex#String
        SEARCH_INDEX_ASYNC_CLIENT.deleteIndex("searchIndex")
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndex#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#deleteIndexWithResponse(String, RequestOptions)}
     */
    public void deleteSearchIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndexWithResponse#String-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getIndex("searchIndex")
            .flatMap(indexFromService -> SEARCH_INDEX_ASYNC_CLIENT.deleteIndexWithResponse(indexFromService.getName(),
                new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, indexFromService.getETag())))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndexWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#analyzeText(String, AnalyzeTextOptions)}
     */
    public void analyzeTextAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.analyzeText#String-AnalyzeTextOptions
        SEARCH_INDEX_ASYNC_CLIENT.analyzeText("searchIndex",
            new AnalyzeTextOptions("The quick brown fox").setTokenizerName(LexicalTokenizerName.CLASSIC))
            .subscribe(result -> result.getTokens().forEach(tokenInfo ->
                System.out.printf("The token emitted by the analyzer is %s.%n", tokenInfo.getToken())));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.analyzeText#String-AnalyzeTextOptions
    }

    /**
     * Code snippet for creating {@link SearchIndexAsyncClient#createSynonymMap(SynonymMap)}.
     */
    public void createSynonymMapAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMap#SynonymMap
        SynonymMap synonymMap = new SynonymMap("synonymMap",
            "United States, United States of America, USA\nWashington, Wash. => WA");
        SEARCH_INDEX_ASYNC_CLIENT.createSynonymMap(synonymMap)
            .subscribe(synonymMapFromService ->
                System.out.printf("The synonym map name is %s. The ETag of synonym map is %s.%n",
                synonymMapFromService.getName(), synonymMapFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createSynonymMapWithResponse(SynonymMap, RequestOptions)}
     */
    public void createSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMapWithResponse#SynonymMap-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.createSynonymMapWithResponse(new SynonymMap("synonymMap",
                "United States, United States of America, USA\nWashington, Wash. => WA"), new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %d.%n"
                    + "The synonym map name is %s. The ETag of synonym map is %s.%n", response.getStatusCode(),
                response.getValue().getName(), response.getValue().getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMapWithResponse#SynonymMap-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getSynonymMap(String)}
     */
    public void getSynonymMapAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("synonymMap")
            .subscribe(synonymMapFromService ->
                System.out.printf("The synonym map is %s. The ETag of synonym map is %s.%n",
                    synonymMapFromService.getName(), synonymMapFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMap#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getSynonymMapWithResponse(String, RequestOptions)}}
     */
    public void getSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMapWithResponse#String-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMapWithResponse("synonymMap", new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %d.%n"
                    + "The synonym map name is %s. The ETag of synonym map is %s.%n",
                response.getStatusCode(), response.getValue().getName(), response.getValue().getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMapWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#listSynonymMaps()}
     */
    public void listSynonymMapsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMaps
        SEARCH_INDEX_ASYNC_CLIENT.listSynonymMaps()
            .subscribe(result -> result.getSynonymMaps().forEach(synonymMap ->
                System.out.printf("The synonymMap name is %s. The ETag of synonymMap is %s.%n",
                    synonymMap.getName(), synonymMap.getETag())));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMaps
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#listSynonymMapNames()}
     */
    public void listSynonymMapNamesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMapNames
        SEARCH_INDEX_ASYNC_CLIENT.listSynonymMapNames()
            .subscribe(synonymMap -> System.out.printf("The synonymMap name is %s.%n", synonymMap));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMapNames
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateSynonymMap(SynonymMap)}
     */
    public void createOrUpdateSynonymMapAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMap#SynonymMap
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("searchIndex")
            .doOnNext(synonymMap -> {
                synonymMap.getSynonyms().clear();
                synonymMap.getSynonyms()
                    .add("United States, United States of America, USA, America\nWashington, Wash. => WA");
            })
            .flatMap(SEARCH_INDEX_ASYNC_CLIENT::createOrUpdateSynonymMap)
            .subscribe(updatedSynonymMap ->
                System.out.printf("The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getName(),
                updatedSynonymMap.getSynonyms()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateSynonymMapWithResponse(SynonymMap, RequestOptions)}
     */
    public void createOrUpdateSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMapWithResponse#SynonymMap-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("searchIndex")
            .flatMap(synonymMap -> {
                synonymMap.getSynonyms().clear();
                synonymMap.getSynonyms().add(
                    "United States, United States of America, USA, America\nWashington, Wash. => WA");
                return SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateSynonymMapWithResponse(synonymMap,
                    new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, synonymMap.getETag()));
            })
            .subscribe(updatedSynonymMap ->
                System.out.printf("The status code of the normal response is %s.%n"
                    + "The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getStatusCode(),
                updatedSynonymMap.getValue().getName(), updatedSynonymMap.getValue().getSynonyms()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMapWithResponse#SynonymMap-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#deleteSynonymMap(String)}
     */
    public void deleteSynonymMapAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMap#String
        SEARCH_INDEX_ASYNC_CLIENT.deleteSynonymMap("synonymMap")
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMap#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#deleteSynonymMapWithResponse(String, RequestOptions)}
     */
    public void deleteSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMapWithResponse#String-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("synonymMap")
            .flatMap(synonymMap -> SEARCH_INDEX_ASYNC_CLIENT.deleteSynonymMapWithResponse(synonymMap.getName(),
                new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, synonymMap.getETag())))
            .subscribe(response -> System.out.println("The status code of the response is" + response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMapWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getServiceStatistics()}
     */
    public void getServiceStatisticsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatistics
        SEARCH_INDEX_ASYNC_CLIENT.getServiceStatistics()
            .subscribe(serviceStatistics -> System.out.printf("There are %s search indexes in your service.%n",
                serviceStatistics.getCounters().getIndexCounter()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatistics
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getServiceStatisticsWithResponse(RequestOptions)}
     */
    public void getServiceStatisticsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatisticsWithResponse#RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getServiceStatisticsWithResponse(new RequestOptions())
            .subscribe(response -> System.out.printf(
                "The status code of the response is %s.%n" + "There are %s search indexes in your service.%n",
                response.getStatusCode(), response.getValue().getCounters().getIndexCounter()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatisticsWithResponse#RequestOptions
    }

    private static final SearchIndexerClient SEARCH_INDEXER_CLIENT = new SearchIndexerClientBuilder().buildClient();
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
        SearchIndexer indexerFromService = SEARCH_INDEXER_CLIENT.createIndexer(searchIndexer);
        System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexerFromService.getName(),
            indexerFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createIndexer#SearchIndexer
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createIndexerWithResponse(SearchIndexer, RequestOptions)}.
     */
    public void createSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createIndexerWithResponse#SearchIndexer-RequestOptions
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource", "searchIndex");
        Response<SearchIndexer> response = SEARCH_INDEXER_CLIENT.createIndexerWithResponse(searchIndexer,
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createIndexerWithResponse#SearchIndexer-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexer(String)}
     */
    public void getSearchIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexer#String
        SearchIndexer indexerFromService =
            SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexerFromService.getName(),
            indexerFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexerWithResponse(String, RequestOptions)}
     */
    public void getSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-RequestOptions
        Response<SearchIndexer> response = SEARCH_INDEXER_CLIENT.getIndexerWithResponse(
            "searchIndexer", new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-RequestOptions
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listIndexers()}
     */
    public void listIndexers() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexers
        ListIndexersResult indexers = SEARCH_INDEXER_CLIENT.listIndexers();
        for (SearchIndexer indexer: indexers.getIndexers()) {
            System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexer.getName(),
                indexer.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexers
    }

//    /**
//     * Code snippet for {@link SearchIndexerClient#listIndexers(RequestOptions)}
//     */
//    public void listIndexersWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context
//        PagedIterable<SearchIndexer> indexers = SEARCH_INDEXER_CLIENT.listIndexers(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + indexers.iterableByPage().iterator().next().getStatusCode());
//        for (SearchIndexer indexer: indexers) {
//            System.out.printf("The indexer name is %s. The ETag of index is %s.%n",
//                indexer.getName(), indexer.getETag());
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context
//    }

    /**
     * Code snippet for {@link SearchIndexerClient#listIndexerNames()}
     */
    public void listIndexerNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames
        List<String> indexers = SEARCH_INDEXER_CLIENT.listIndexerNames();
        for (String indexerName: indexers) {
            System.out.printf("The indexer name is %s.%n", indexerName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames
    }

//    /**
//     * Code snippet for {@link SearchIndexerClient#listIndexerNames(RequestOptions)}
//     */
//    public void listIndexerNamesWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames#Context
//        PagedIterable<String> indexers = SEARCH_INDEXER_CLIENT.listIndexerNames(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + indexers.iterableByPage().iterator().next().getStatusCode());
//        for (String indexerName: indexers) {
//            System.out.printf("The indexer name is %s.%n", indexerName);
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames#Context
//    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateIndexer(SearchIndexer)}
     */
    public void createOrUpdateIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexer#SearchIndexer
        SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        searchIndexerFromService.setFieldMappings(Collections.singletonList(
            new FieldMapping("hotelName").setTargetFieldName("HotelName")));
        SearchIndexer updateIndexer = SEARCH_INDEXER_CLIENT.createOrUpdateIndexer(searchIndexerFromService);
        System.out.printf("The indexer name is %s. The target field name of indexer is %s.%n",
            updateIndexer.getName(), updateIndexer.getFieldMappings().get(0).getTargetFieldName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexer#SearchIndexer
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateIndexerWithResponse(SearchIndexer, RequestOptions)}
     */
    public void createOrUpdateIndexerWithResponse2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-RequestOptions
        SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        searchIndexerFromService.setFieldMappings(Collections.singletonList(
            new FieldMapping("hotelName").setTargetFieldName("HotelName")));
        Response<SearchIndexer> indexerFromService = SEARCH_INDEXER_CLIENT.createOrUpdateIndexerWithResponse(
            searchIndexerFromService,
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, searchIndexerFromService.getETag())
                .addQueryParam("ignoreResetRequirements", "true")
                .addQueryParam("disableCacheReprocessingChangeDetection", "false")
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %s.%nThe indexer name is %s. "
                + "The target field name of indexer is %s.%n", indexerFromService.getStatusCode(),
            indexerFromService.getValue().getName(),
            indexerFromService.getValue().getFieldMappings().get(0).getTargetFieldName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteIndexer(String)}
     */
    public void deleteSearchIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexer#String
        SEARCH_INDEXER_CLIENT.deleteIndexer("searchIndexer");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteIndexerWithResponse(String, RequestOptions)}
     */
    public void deleteSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#String-RequestOptions
        SearchIndexer searchIndexer = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        Response<Void> deleteResponse = SEARCH_INDEXER_CLIENT.deleteIndexerWithResponse(searchIndexer.getName(),
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, searchIndexer.getETag())
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetIndexer(String)}
     */
    public void resetIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexer#String
        SEARCH_INDEXER_CLIENT.resetIndexer("searchIndexer");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetIndexerWithResponse(String, RequestOptions)}
     */
    public void resetIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-RequestOptions
        Response<Void> response = SEARCH_INDEXER_CLIENT.resetIndexerWithResponse("searchIndexer",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#runIndexer(String)}
     */
    public void runIndexer() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.runIndexer#String
        SEARCH_INDEXER_CLIENT.runIndexer("searchIndexer");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.runIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#runIndexerWithResponse(String, RequestOptions)}
     */
    public void runIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-RequestOptions
        Response<Void> response = SEARCH_INDEXER_CLIENT.runIndexerWithResponse("searchIndexer",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.println("The status code of the response is " + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexerStatus(String)}
     */
    public void getIndexerStatus() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatus#String
        SearchIndexerStatus indexerStatus = SEARCH_INDEXER_CLIENT.getIndexerStatus("searchIndexer");
        System.out.printf("The indexer status is %s.%n", indexerStatus.getStatus());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatus#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexerStatusWithResponse(String, RequestOptions)}
     */
    public void getIndexerStatusWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-RequestOptions
        Response<SearchIndexerStatus> response = SEARCH_INDEXER_CLIENT.getIndexerStatusWithResponse("searchIndexer",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s.%nThe indexer status is %s.%n",
            response.getStatusCode(), response.getValue().getStatus());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetDocuments(String, Boolean, DocumentKeysOrIds)}
     */
    public void resetDocuments() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetDocuments#String-Boolean-DocumentKeyOrIds
        // Reset the documents with keys 1234 and 4321.
        SEARCH_INDEXER_CLIENT.resetDocuments("searchIndexer", false,
            new DocumentKeysOrIds().setDocumentKeys("1234", "4321"));

        // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
        SEARCH_INDEXER_CLIENT.resetDocuments("searchIndexer", true,
            new DocumentKeysOrIds().setDocumentKeys("1235", "5321"));
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetDocuments#String-Boolean-DocumentKeyOrIds
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetDocumentsWithResponse(String, RequestOptions)}
     */
    public void resetDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetDocumentsWithResponse#String-RequestOptions
        SearchIndexer searchIndexer = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");

        // Reset the documents with keys 1234 and 4321.
        Response<Void> resetDocsResult = SEARCH_INDEXER_CLIENT.resetDocumentsWithResponse(searchIndexer.getName(),
            new RequestOptions().addQueryParam("overwrite", "false")
                .setBody(BinaryData.fromObject(new DocumentKeysOrIds().setDocumentKeys("1234", "4321")))
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("Requesting documents to be reset completed with status code %d.%n",
            resetDocsResult.getStatusCode());

        // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
        resetDocsResult = SEARCH_INDEXER_CLIENT.resetDocumentsWithResponse(searchIndexer.getName(),
            new RequestOptions().addQueryParam("overwrite", "true")
                .setBody(BinaryData.fromObject(new DocumentKeysOrIds().setDocumentKeys("1235", "5321")))
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("Overwriting the documents to be reset completed with status code %d.%n",
            resetDocsResult.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetDocumentsWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for creating {@link SearchIndexerClient#createDataSourceConnection(SearchIndexerDataSourceConnection)}.
     */
    public void createDataSource() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnection#SearchIndexerDataSourceConnection
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            com.azure.search.documents.indexes.models.SearchIndexerDataSourceType.AZURE_BLOB,
            new DataSourceCredentials().setConnectionString("{connectionString}"),
            new com.azure.search.documents.indexes.models.SearchIndexerDataContainer("container"));
        SearchIndexerDataSourceConnection dataSourceFromService =
            SEARCH_INDEXER_CLIENT.createDataSourceConnection(dataSource);
        System.out.printf("The data source name is %s. The ETag of data source is %s.%n",
            dataSourceFromService.getName(), dataSourceFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, RequestOptions)}.
     */
    public void createDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            SearchIndexerDataSourceType.AZURE_BLOB,
            new DataSourceCredentials().setConnectionString("{connectionString}"),
            new SearchIndexerDataContainer("container"));
        Response<SearchIndexerDataSourceConnection> response
            = SEARCH_INDEXER_CLIENT.createDataSourceConnectionWithResponse(dataSource,
                new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s. The data source name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getDataSourceConnection(String)}
     */
    public void getDataSource() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnection#String
        SearchIndexerDataSourceConnection dataSource =
            SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n", dataSource.getName(),
            dataSource.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnection#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getDataSourceConnectionWithResponse(String, RequestOptions)}
     */
    public void getDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-RequestOptions
        Response<SearchIndexerDataSourceConnection> response =
            SEARCH_INDEXER_CLIENT.getDataSourceConnectionWithResponse("dataSource",
                new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s. The data source name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-RequestOptions
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnections()}
     */
    public void listDataSources() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnections
        ListDataSourcesResult dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnections();
        for (SearchIndexerDataSourceConnection dataSource: dataSources.getDataSources()) {
            System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n", dataSource.getName(),
                dataSource.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnections
    }

//    /**
//     * Code snippet for {@link SearchIndexerClient#listDataSourceConnections(RequestOptions)}
//     */
//    public void listDataSourcesWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionsWithResponse#Context
//        PagedIterable<SearchIndexerDataSourceConnection> dataSources =
//            SEARCH_INDEXER_CLIENT.listDataSourceConnections(new Context(KEY_1, VALUE_1));
//
//        System.out.println("The status code of the response is"
//            + dataSources.iterableByPage().iterator().next().getStatusCode());
//        for (SearchIndexerDataSourceConnection dataSource: dataSources) {
//            System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n",
//                dataSource.getName(), dataSource.getETag());
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionsWithResponse#Context
//    }

    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnectionNames()}
     */
    public void listDataSourceNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNames
        List<String> dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnectionNames();
        for (String dataSourceName: dataSources) {
            System.out.printf("The dataSource name is %s.%n", dataSourceName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNames
    }

//    /**
//     * Code snippet for {@link SearchIndexerClient#listDataSourceConnectionNames()}
//     */
//    public void listDataSourceNamesWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNamesWithContext#Context
//        PagedIterable<String> dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnectionNames(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + dataSources.iterableByPage().iterator().next().getStatusCode());
//        for (String dataSourceName: dataSources) {
//            System.out.printf("The dataSource name is %s.%n", dataSourceName);
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNamesWithContext#Context
//    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, RequestOptions)}
     */
    public void createOrUpdateDataSourceWithResponse2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
        SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        dataSource.getContainer().setQuery("newquery");

        Response<SearchIndexerDataSourceConnection> updateDataSource = SEARCH_INDEXER_CLIENT
            .createOrUpdateDataSourceConnectionWithResponse(dataSource, new RequestOptions()
                .setHeader(HttpHeaderName.IF_MATCH, dataSource.getETag())
                .addQueryParam("ignoreResetRequirements", "true")
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %s.%nThe dataSource name is %s. "
                + "The container name of dataSource is %s.%n", updateDataSource.getStatusCode(),
            updateDataSource.getValue().getName(), updateDataSource.getValue().getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteDataSourceConnection(String)}
     */
    public void deleteDataSource() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnection#String
        SEARCH_INDEXER_CLIENT.deleteDataSourceConnection("dataSource");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnection#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteDataSourceConnectionWithResponse(String, RequestOptions)}
     */
    public void deleteDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnectionWithResponse#String-RequestOptions
        SearchIndexerDataSourceConnection dataSource =
            SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        Response<Void> deleteResponse = SEARCH_INDEXER_CLIENT.deleteDataSourceConnectionWithResponse(
            dataSource.getName(), new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, dataSource.getETag())
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnectionWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createSkillset(SearchIndexerSkillset)}
     */
    public void createSearchIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createSkillset#SearchIndexerSkillset
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image").setSource("/document/normalized_images/*"));

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text").setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText").setTargetName("myLayoutText"));
        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset("searchIndexerSkillset",
            new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*"));
        SearchIndexerSkillset skillset = SEARCH_INDEXER_CLIENT.createSkillset(searchIndexerSkillset);
        System.out.printf("The indexer skillset name is %s. The ETag of indexer skillset is %s.%n",
            skillset.getName(), skillset.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createSkillsetWithResponse(SearchIndexerSkillset, RequestOptions)}.
     */
    public void createSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image").setSource("/document/normalized_images/*"));

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text").setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText").setTargetName("myLayoutText"));
        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset("searchIndexerSkillset",
            new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*"));
        Response<SearchIndexerSkillset> response
            = SEARCH_INDEXER_CLIENT.createSkillsetWithResponse(searchIndexerSkillset,
                new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getSkillset(String)}
     */
    public void getSearchIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getSearchIndexerSkillset#String
        SearchIndexerSkillset indexerSkillset =
            SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        System.out.printf("The indexer skillset name is %s. The ETag of indexer skillset is %s.%n",
            indexerSkillset.getName(), indexerSkillset.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getSearchIndexerSkillset#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getSkillsetWithResponse(String, RequestOptions)}
     */
    public void getSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-RequestOptions
        Response<SearchIndexerSkillset> response = SEARCH_INDEXER_CLIENT.getSkillsetWithResponse(
            "searchIndexerSkillset", new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-RequestOptions
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsets()}
     */
    public void listIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets
        ListSkillsetsResult indexerSkillsets = SEARCH_INDEXER_CLIENT.listSkillsets();
        for (SearchIndexerSkillset skillset: indexerSkillsets.getSkillsets()) {
            System.out.printf("The skillset name is %s. The ETag of skillset is %s.%n", skillset.getName(),
                skillset.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets
    }

//    /**
//     * Code snippet for {@link SearchIndexerClient#listSkillsets(RequestOptions)}
//     */
//    public void listIndexerSkillsetsWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context
//        PagedIterable<SearchIndexerSkillset> indexerSkillsets = SEARCH_INDEXER_CLIENT
//            .listSkillsets(new Context(KEY_1, VALUE_1));
//        System.out.println("The status code of the response is"
//            + indexerSkillsets.iterableByPage().iterator().next().getStatusCode());
//        for (SearchIndexerSkillset skillset: indexerSkillsets) {
//            System.out.printf("The skillset name is %s. The ETag of skillset is %s.%n",
//                skillset.getName(), skillset.getETag());
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context
//    }

    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsetNames()}
     */
    public void listIndexerSkillsetNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNames
        List<String> skillsetNames = SEARCH_INDEXER_CLIENT.listSkillsetNames();
        for (String skillsetName: skillsetNames) {
            System.out.printf("The indexer skillset name is %s.%n", skillsetName);
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNames
    }

//    /**
//     * Code snippet for {@link SearchIndexerClient#listSkillsetNames()}
//     */
//    public void listIndexerSkillsetNamesWithContext() {
//        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNamesWithResponse#Context
//        List<String> skillsetNames = SEARCH_INDEXER_CLIENT.listSkillsetNames();
//        for (String skillsetName: skillsetNames) {
//            System.out.printf("The indexer skillset name is %s.%n", skillsetName);
//        }
//        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNamesWithResponse#Context
//    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateSkillset(SearchIndexerSkillset)}
     */
    public void createOrUpdateIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset
        SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        indexerSkillset.setDescription("This is new description!");
        SearchIndexerSkillset updateSkillset = SEARCH_INDEXER_CLIENT.createOrUpdateSkillset(indexerSkillset);
        System.out.printf("The indexer skillset name is %s. The description of indexer skillset is %s.%n",
            updateSkillset.getName(), updateSkillset.getDescription());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateSkillsetWithResponse(SearchIndexerSkillset, RequestOptions)}
     */
    public void createOrUpdateIndexerSkillsetWithResponse2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
        SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        indexerSkillset.setDescription("This is new description!");
        Response<SearchIndexerSkillset> updateSkillsetResponse = SEARCH_INDEXER_CLIENT
            .createOrUpdateSkillsetWithResponse(indexerSkillset, new RequestOptions()
                .setHeader(HttpHeaderName.IF_MATCH, indexerSkillset.getETag())
                .addQueryParam("ignoreResetRequirements", "true")
                .addQueryParam("disableCacheReprocessingChangeDetection", "false")
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %s.%nThe indexer skillset name is %s. "
                + "The description of indexer skillset is %s.%n", updateSkillsetResponse.getStatusCode(),
            updateSkillsetResponse.getValue().getName(),
            updateSkillsetResponse.getValue().getDescription());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteSkillset(String)}
     */
    public void deleteSearchIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillset#String
        SEARCH_INDEXER_CLIENT.deleteSkillset("searchIndexerSkillset");
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillset#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#deleteSkillsetWithResponse(String, RequestOptions)}
     */
    public void deleteSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#String-RequestOptions
        SearchIndexerSkillset searchIndexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        Response<Void> deleteResponse = SEARCH_INDEXER_CLIENT.deleteSkillsetWithResponse(
            searchIndexerSkillset.getName(), new RequestOptions()
                .setHeader(HttpHeaderName.IF_MATCH, searchIndexerSkillset.getETag())
                .setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#String-RequestOptions
    }

    private static final SearchIndexerAsyncClient SEARCH_INDEXER_ASYNC_CLIENT = new SearchIndexerClientBuilder()
        .buildAsyncClient();

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

    /**
     * Code snippet for creating {@link SearchIndexerAsyncClient#createIndexer(SearchIndexer)}.
     */
    public void createSearchIndexerAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexer#SearchIndexer
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        SEARCH_INDEXER_ASYNC_CLIENT.createIndexer(searchIndexer)
            .subscribe(indexerFromService ->
                System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexerFromService.getName(),
                indexerFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexer#SearchIndexer
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createIndexerWithResponse(SearchIndexer, RequestOptions)}.
     */
    public void createSearchIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexerWithResponse#SearchIndexer-RequestOptions
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        SEARCH_INDEXER_ASYNC_CLIENT.createIndexerWithResponse(searchIndexer, new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexerWithResponse#SearchIndexer-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getIndexer(String)}
     */
    public void getSearchIndexerAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexer#String
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .subscribe(indexerFromService ->
                System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexerFromService.getName(),
                    indexerFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getIndexerWithResponse(String, RequestOptions)}}
     */
    public void getSearchIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexerWithResponse("searchIndexer", new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerWithResponse#String-RequestOptions
    }


    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listIndexers()}
     */
    public void listIndexersAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexers
        SEARCH_INDEXER_ASYNC_CLIENT.listIndexers()
            .subscribe(result -> result.getIndexers().forEach(indexer ->
                System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexer.getName(),
                    indexer.getETag())));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexers
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listIndexerNames()}
     */
    public void listIndexerNamesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexerNames
        SEARCH_INDEXER_ASYNC_CLIENT.listIndexerNames()
            .subscribe(indexerName -> System.out.printf("The indexer name is %s.%n", indexerName));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexerNames
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateIndexer(SearchIndexer)}
     */
    public void createOrUpdateIndexerAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexer#SearchIndexer
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexerFromService -> {
                searchIndexerFromService.setFieldMappings(Collections.singletonList(
                    new FieldMapping("hotelName").setTargetFieldName("HotelName")));
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateIndexer(searchIndexerFromService);
            })
            .subscribe(updatedIndexer ->
                System.out.printf("The indexer name is %s. The target field name of indexer is %s.%n",
                updatedIndexer.getName(), updatedIndexer.getFieldMappings().get(0).getTargetFieldName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexer#SearchIndexer
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateIndexerWithResponse(SearchIndexer, RequestOptions)}
     */
    public void createOrUpdateIndexerWithResponseAsync2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#SearchIndexer-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexerFromService -> {
                searchIndexerFromService.setFieldMappings(Collections.singletonList(
                    new FieldMapping("hotelName").setTargetFieldName("HotelName")));
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateIndexerWithResponse(searchIndexerFromService,
                    new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, searchIndexerFromService.getETag())
                        .addQueryParam("ignoreResetRequirements", "true")
                        .addQueryParam("disableCacheReprocessingChangeDetection", "false"));
            })
            .subscribe(indexerFromService ->
                System.out.printf("The status code of the response is %s.%nThe indexer name is %s. "
                        + "The target field name of indexer is %s.%n", indexerFromService.getStatusCode(),
                    indexerFromService.getValue().getName(),
                    indexerFromService.getValue().getFieldMappings().get(0).getTargetFieldName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#SearchIndexer-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#deleteIndexer(String)}
     */
    public void deleteSearchIndexerAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexer#String
        SEARCH_INDEXER_ASYNC_CLIENT.deleteIndexer("searchIndexer")
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#deleteIndexerWithResponse(String, RequestOptions)}
     */
    public void deleteSearchIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexerWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexer ->
                SEARCH_INDEXER_ASYNC_CLIENT.deleteIndexerWithResponse(searchIndexer.getName(),
                    new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, searchIndexer.getETag())))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexerWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetIndexer(String)}
     */
    public void resetIndexerAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexer#String
        SEARCH_INDEXER_ASYNC_CLIENT.resetIndexer("searchIndexer")
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetIndexerWithResponse(String, RequestOptions)}
     */
    public void resetIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexerWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.resetIndexerWithResponse("searchIndexer", new RequestOptions())
            .subscribe(response ->
                System.out.println("The status code of the response is " + response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexerWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#runIndexer(String)}
     */
    public void runIndexerAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexer#String
        SEARCH_INDEXER_ASYNC_CLIENT.runIndexer("searchIndexer")
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#runIndexerWithResponse(String, RequestOptions)}
     */
    public void runIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexerWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.runIndexerWithResponse("searchIndexer", new RequestOptions())
            .subscribe(response ->
                System.out.println("The status code of the response is " + response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexerWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getIndexerStatus(String)}
     */
    public void getIndexerStatusAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatus#String
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexerStatus("searchIndexer")
            .subscribe(indexerStatus ->
                System.out.printf("The indexer status is %s.%n", indexerStatus.getStatus()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatus#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getIndexerStatusWithResponse(String, RequestOptions)}
     */
    public void getIndexerStatusWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatusWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexerStatusWithResponse("searchIndexer", new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s.%nThe indexer status is %s.%n",
                response.getStatusCode(), response.getValue().getStatus()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatusWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetDocuments(String, Boolean, DocumentKeysOrIds)}
     */
    public void resetDocumentsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocuments#String-Boolean-DocumentKeysOrIds
        // Reset the documents with keys 1234 and 4321.
        SEARCH_INDEXER_ASYNC_CLIENT.resetDocuments("searchIndexer", false,
                new DocumentKeysOrIds().setDocumentKeys("1234", "4321"))
            // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
            .then(SEARCH_INDEXER_ASYNC_CLIENT.resetDocuments("searchIndexer", true,
                new DocumentKeysOrIds().setDocumentKeys("1235", "5321")))
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocuments#String-Boolean-DocumentKeysOrIds
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetDocumentsWithResponse(String, RequestOptions)}
     */
    public void resetDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocumentsWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexer -> SEARCH_INDEXER_ASYNC_CLIENT.resetDocumentsWithResponse(searchIndexer.getName(),
                    new RequestOptions().addQueryParam("overwrite", "false")
                        .setBody(BinaryData.fromObject(new DocumentKeysOrIds().setDocumentKeys("1234", "4321"))))
                .flatMap(resetDocsResult -> {
                    System.out.printf("Requesting documents to be reset completed with status code %d.%n",
                        resetDocsResult.getStatusCode());

                    // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
                    return SEARCH_INDEXER_ASYNC_CLIENT.resetDocumentsWithResponse(searchIndexer.getName(),
                        new RequestOptions().addQueryParam("overwrite", "true")
                            .setBody(BinaryData.fromObject(new DocumentKeysOrIds().setDocumentKeys("1235", "5321"))));
                }))
            .subscribe(resetDocsResult ->
                System.out.printf("Overwriting the documents to be reset completed with status code %d.%n",
                    resetDocsResult.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocumentsWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for creating {@link SearchIndexerAsyncClient#createDataSourceConnection(SearchIndexerDataSourceConnection)}.
     */
    public void createDataSourceAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnection#SearchIndexerDataSourceConnection
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            SearchIndexerDataSourceType.AZURE_BLOB,
            new DataSourceCredentials().setConnectionString("{connectionString}"),
            new SearchIndexerDataContainer("container"));
        SEARCH_INDEXER_ASYNC_CLIENT.createDataSourceConnection(dataSource)
            .subscribe(dataSourceFromService ->
                System.out.printf("The data source name is %s. The ETag of data source is %s.%n",
                    dataSourceFromService.getName(), dataSourceFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, RequestOptions)}.
     */
    public void createDataSourceWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            SearchIndexerDataSourceType.AZURE_BLOB,
            new DataSourceCredentials().setConnectionString("{connectionString}"),
            new SearchIndexerDataContainer("container"));
        SEARCH_INDEXER_ASYNC_CLIENT.createDataSourceConnectionWithResponse(dataSource, new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The data source name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getDataSourceConnection(String)}
     */
    public void getDataSourceAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnection#String
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection("dataSource")
            .subscribe(dataSource ->
                System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n", dataSource.getName(),
                dataSource.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnection#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getDataSourceConnectionWithResponse(String, RequestOptions)}
     */
    public void getDataSourceWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnectionWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnectionWithResponse("dataSource", new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The data source name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnectionWithResponse#String-RequestOptions
    }


    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listDataSourceConnections()}
     */
    public void listDataSourcesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnections
        SEARCH_INDEXER_ASYNC_CLIENT.listDataSourceConnections()
            .subscribe(result -> result.getDataSources().forEach(dataSource ->
                System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n",
                    dataSource.getName(), dataSource.getETag())));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnections
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listDataSourceConnectionNames()}
     */
    public void listDataSourceNamesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnectionNames
        SEARCH_INDEXER_ASYNC_CLIENT.listDataSourceConnectionNames()
            .subscribe(dataSourceName -> System.out.printf("The dataSource name is %s.%n", dataSourceName));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnectionNames
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateDataSourceConnection(SearchIndexerDataSourceConnection)}
     */
    public void createOrUpdateDataSourceAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection
        SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        dataSource.getContainer().setQuery("newquery");

        SearchIndexerDataSourceConnection updateDataSource = SEARCH_INDEXER_CLIENT
            .createOrUpdateDataSourceConnection(dataSource);
        System.out.printf("The dataSource name is %s. The container name of dataSource is %s.%n",
            updateDataSource.getName(), updateDataSource.getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, RequestOptions)}
     */
    public void createOrUpdateDataSourceWithResponseAsync2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection("dataSource")
            .flatMap(dataSource -> {
                dataSource.getContainer().setQuery("newquery");
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateDataSourceConnectionWithResponse(dataSource,
                    new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, dataSource.getETag())
                        .addQueryParam("ignoreResetRequirements", "true"));
            })
            .subscribe(updateDataSource ->
                System.out.printf("The status code of the response is %s.%nThe dataSource name is %s. "
                        + "The container name of dataSource is %s.%n", updateDataSource.getStatusCode(),
                    updateDataSource.getValue().getName(), updateDataSource.getValue().getContainer().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#deleteDataSourceConnection(String)}
     */
    public void deleteDataSourceAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnection#String
        SEARCH_INDEXER_ASYNC_CLIENT.deleteDataSourceConnection("dataSource")
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnection#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#deleteDataSourceConnectionWithResponse(String, RequestOptions)}
     */
    public void deleteDataSourceWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnectionWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection("dataSource")
            .flatMap(dataSource -> SEARCH_INDEXER_ASYNC_CLIENT.deleteDataSourceConnectionWithResponse(
                dataSource.getName(), new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, dataSource.getETag())))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnectionWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createSkillset(SearchIndexerSkillset)}
     */
    public void createSearchIndexerSkillsetAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillset#SearchIndexerSkillset
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image").setSource("/document/normalized_images/*"));

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text").setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText").setTargetName("myLayoutText"));
        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset("searchIndexerSkillset",
            Collections.singletonList(new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*")));
        SEARCH_INDEXER_ASYNC_CLIENT.createSkillset(searchIndexerSkillset)
            .subscribe(skillset ->
                System.out.printf("The indexer skillset name is %s. The ETag of indexer skillset is %s.%n",
                skillset.getName(), skillset.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createSkillsetWithResponse(SearchIndexerSkillset, RequestOptions)}.
     */
    public void createSearchIndexerSkillsetWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry("image").setSource("/document/normalized_images/*"));

        List<OutputFieldMappingEntry> outputs = Arrays.asList(
            new OutputFieldMappingEntry("text").setTargetName("mytext"),
            new OutputFieldMappingEntry("layoutText").setTargetName("myLayoutText"));
        SearchIndexerSkillset searchIndexerSkillset = new SearchIndexerSkillset("searchIndexerSkillset",
            new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(true)
                .setDefaultLanguageCode(null)
                .setName("myocr")
                .setDescription("Extracts text (plain and structured) from image.")
                .setContext("/document/normalized_images/*"));
        SEARCH_INDEXER_ASYNC_CLIENT.createSkillsetWithResponse(searchIndexerSkillset, new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getSkillset(String)}
     */
    public void getSearchIndexerSkillsetAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSearchIndexerSkillset#String
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .subscribe(indexerSkillset ->
                System.out.printf("The indexer skillset name is %s. The ETag of indexer skillset is %s.%n",
                indexerSkillset.getName(), indexerSkillset.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSearchIndexerSkillset#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#getSkillsetWithResponse(String, RequestOptions)}
     */
    public void getSearchIndexerSkillsetWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSkillsetWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillsetWithResponse("searchIndexerSkillset", new RequestOptions())
            .subscribe(response -> System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
                response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSkillsetWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listSkillsets()}
     */
    public void listIndexerSkillsetAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsets
        SEARCH_INDEXER_ASYNC_CLIENT.listSkillsets()
            .subscribe(result -> result.getSkillsets().forEach(skillset ->
                System.out.printf("The skillset name is %s. The ETag of skillset is %s.%n", skillset.getName(),
                    skillset.getETag())));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsets
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listSkillsetNames()}
     */
    public void listIndexerSkillsetNamesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsetNames
        SEARCH_INDEXER_ASYNC_CLIENT.listSkillsetNames()
            .subscribe(skillsetName -> System.out.printf("The indexer skillset name is %s.%n", skillsetName));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsetNames
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateSkillset(SearchIndexerSkillset)}
     */
    public void createOrUpdateIndexerSkillsetAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(indexerSkillset -> {
                indexerSkillset.setDescription("This is new description!");
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateSkillset(indexerSkillset);
            }).subscribe(updateSkillset ->
                System.out.printf("The indexer skillset name is %s. The description of indexer skillset is %s.%n",
                updateSkillset.getName(), updateSkillset.getDescription()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateSkillsetWithResponse(SearchIndexerSkillset, RequestOptions)}
     */
    public void createOrUpdateIndexerSkillsetWithResponseAsync2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(indexerSkillset -> {
                indexerSkillset.setDescription("This is new description!");
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateSkillsetWithResponse(indexerSkillset,
                    new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, indexerSkillset.getETag())
                        .addQueryParam("ignoreResetRequirements", "true")
                        .addQueryParam("disableCacheReprocessingChangeDetection", "false"));
            })
            .subscribe(updateSkillsetResponse ->
                System.out.printf("The status code of the response is %s.%nThe indexer skillset name is %s. "
                        + "The description of indexer skillset is %s.%n", updateSkillsetResponse.getStatusCode(),
                    updateSkillsetResponse.getValue().getName(),
                    updateSkillsetResponse.getValue().getDescription()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#deleteSkillset(String)}
     */
    public void deleteSearchIndexerSkillsetAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillset#String
        SEARCH_INDEXER_ASYNC_CLIENT.deleteSkillset("searchIndexerSkillset")
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillset#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#deleteSkillsetWithResponse(String, RequestOptions)}
     */
    public void deleteSearchIndexerSkillsetWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillsetWithResponse#String-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(searchIndexerSkillset ->
                SEARCH_INDEXER_ASYNC_CLIENT.deleteSkillsetWithResponse(searchIndexerSkillset.getName(),
                    new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, searchIndexerSkillset.getETag())))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillsetWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetSkills(String, SkillNames)}
     */
    public void resetSkills() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetSkills#String-SkillNames
        // Reset the "myOcr" and "myText" skills.
        SEARCH_INDEXER_CLIENT.resetSkills("searchIndexerSkillset", new SkillNames().setSkillNames("myOcr", "myText"));
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetSkills#String-SkillNames
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetSkillsWithResponse(String, SkillNames, RequestOptions)}
     */
    public void resetSkillsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetSkillsWithResponse#String-SkillNames-RequestOptions
        SearchIndexerSkillset searchIndexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");

        // Reset the "myOcr" and "myText" skills.
        Response<Void> resetSkillsResponse = SEARCH_INDEXER_CLIENT.resetSkillsWithResponse(
            searchIndexerSkillset.getName(), new SkillNames().setSkillNames("myOcr", "myText"),
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));
        System.out.printf("Resetting skills completed with status code %d.%n", resetSkillsResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetSkillsWithResponse#String-SkillNames-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetSkills(String, SkillNames)}
     */
    public void resetSkillsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkills#String-SkillNames
        // Reset the "myOcr" and "myText" skills.
        SEARCH_INDEXER_ASYNC_CLIENT.resetSkills("searchIndexerSkillset",
                new SkillNames().setSkillNames("myOcr", "myText"))
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkills#String-SkillNames
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetSkillsWithResponse(String, SkillNames, RequestOptions)}
     */
    public void resetSkillsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkillsWithResponse#String-SkillNames-RequestOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(searchIndexerSkillset -> SEARCH_INDEXER_ASYNC_CLIENT.resetSkillsWithResponse(
                searchIndexerSkillset.getName(), new SkillNames().setSkillNames("myOcr", "myText"),
                new RequestOptions()))
            .subscribe(resetSkillsResponse -> System.out.printf("Resetting skills completed with status code %d.%n",
                resetSkillsResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkillsWithResponse#String-SkillNames-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createAlias(SearchAlias)}.
     */
    public void createAliasAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAlias#SearchAlias
        SEARCH_INDEX_ASYNC_CLIENT.createAlias(new SearchAlias("my-alias", "index-to-alias"))
            .subscribe(searchAlias -> System.out.printf("Created alias '%s' that aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createAliasWithResponse(SearchAlias, RequestOptions)}.
     */
    public void createAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAliasWithResponse#SearchAlias-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.createAliasWithResponse(new SearchAlias("my-alias", "index-to-alias"), new RequestOptions())
            .subscribe(response -> System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
                response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAliasWithResponse#SearchAlias-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#createAlias(SearchAlias)}.
     */
    public void createAlias() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createAlias#SearchAlias
        SearchAlias searchAlias = SEARCH_INDEX_CLIENT.createAlias(new SearchAlias("my-alias", "index-to-alias"));
        System.out.printf("Created alias '%s' that aliases index '%s'.", searchAlias.getName(),
            searchAlias.getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexClient#createAliasWithResponse(SearchAlias, RequestOptions)}.
     */
    public void createAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createAliasWithResponse#SearchAlias-RequestOptions
        Response<SearchAlias> response = SEARCH_INDEX_CLIENT.createAliasWithResponse(
            new SearchAlias("my-alias", "index-to-alias"),
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createAliasWithResponse#SearchAlias-RequestOptions

    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateAlias(SearchAlias)}.
     */
    public void createOrUpdateAliasAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAlias#SearchAlias
        SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAlias(new SearchAlias("my-alias", "index-to-alias"))
            .flatMap(searchAlias -> {
                System.out.printf("Created alias '%s' that aliases index '%s'.", searchAlias.getName(),
                    searchAlias.getIndexes().get(0));

                return SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAlias(new SearchAlias(searchAlias.getName(),
                    "new-index-to-alias"));
            }).subscribe(searchAlias -> System.out.printf("Updated alias '%s' to aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateAliasWithResponse(SearchAlias, RequestOptions)}.
     */
    public void createOrUpdateAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAliasWithResponse#SearchAlias-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAliasWithResponse(new SearchAlias("my-alias", "index-to-alias"),
                new RequestOptions())
            .flatMap(response -> {
                System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));

                return SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAliasWithResponse(
                    new SearchAlias(response.getValue().getName(), "new-index-to-alias")
                        .setETag(response.getValue().getETag()),
                    new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, response.getValue().getETag()));
            }).subscribe(response ->
                System.out.printf("Response status code %d. Updated alias '%s' that aliases index '%s'.",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAliasWithResponse#SearchAlias-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateAlias(SearchAlias)}.
     */
    public void createOrUpdateAlias() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAlias#SearchAlias
        SearchAlias searchAlias = SEARCH_INDEX_CLIENT.createOrUpdateAlias(
            new SearchAlias("my-alias", "index-to-alias"));

        System.out.printf("Created alias '%s' that aliases index '%s'.", searchAlias.getName(),
            searchAlias.getIndexes().get(0));

        searchAlias = SEARCH_INDEX_CLIENT.createOrUpdateAlias(new SearchAlias(searchAlias.getName(),
            "new-index-to-alias"));

        System.out.printf("Updated alias '%s' to aliases index '%s'.", searchAlias.getName(),
            searchAlias.getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateAliasWithResponse(SearchAlias, RequestOptions)}.
     */
    public void createOrUpdateAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAliasWithResponse#SearchAlias-RequestOptions
        Response<SearchAlias> response = SEARCH_INDEX_CLIENT.createOrUpdateAliasWithResponse(
            new SearchAlias("my-alias", "index-to-alias"),
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));

        response = SEARCH_INDEX_CLIENT.createOrUpdateAliasWithResponse(
            new SearchAlias(response.getValue().getName(), "new-index-to-alias")
                .setETag(response.getValue().getETag()),
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, response.getValue().getETag())
                .setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("Response status code %d. Updated alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAliasWithResponse#SearchAlias-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getAlias(String)}.
     */
    public void getAliasAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getAlias#String
        SEARCH_INDEX_ASYNC_CLIENT.getAlias("my-alias")
            .subscribe(searchAlias -> System.out.printf("Retrieved alias '%s' that aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getAlias#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#getAliasWithResponse(String, RequestOptions)}.
     */
    public void getAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getAliasWithResponse#String-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getAliasWithResponse("my-alias", new RequestOptions())
            .subscribe(response -> System.out.printf("Response status code %d. Retrieved alias '%s' that aliases index '%s'.",
                response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getAliasWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#getAlias(String)}.
     */
    public void getAlias() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getAlias#String
        SearchAlias searchAlias = SEARCH_INDEX_CLIENT.getAlias("my-alias");

        System.out.printf("Retrieved alias '%s' that aliases index '%s'.", searchAlias.getName(),
            searchAlias.getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.getAlias#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getAliasWithResponse(String, RequestOptions)}.
     */
    public void getAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getAliasWithResponse#String-RequestOptions
        Response<SearchAlias> response = SEARCH_INDEX_CLIENT.getAliasWithResponse("my-alias",
            new RequestOptions().setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("Response status code %d. Retrieved alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.getAliasWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#deleteAlias(String)}.
     */
    public void deleteAliasAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteAlias#String
        SEARCH_INDEX_ASYNC_CLIENT.deleteAlias("my-alias")
            .subscribe(ignored -> System.out.println("Deleted alias 'my-alias'."));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteAlias#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#deleteAliasWithResponse(String, RequestOptions)}.
     */
    public void deleteAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteAliasWithResponse#String-RequestOptions
        SEARCH_INDEX_ASYNC_CLIENT.getAlias("my-alias")
            .flatMap(searchAlias -> SEARCH_INDEX_ASYNC_CLIENT.deleteAliasWithResponse(searchAlias.getName(),
                new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, searchAlias.getETag())))
            .subscribe(response -> System.out.printf("Response status code %d. Deleted alias 'my-alias'.",
                response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteAliasWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteAlias(String)}.
     */
    public void deleteAlias() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteAlias#String
        SEARCH_INDEX_CLIENT.deleteAlias("my-alias");

        System.out.println("Deleted alias 'my-alias'.");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteAlias#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteAliasWithResponse(String, RequestOptions)}.
     */
    public void deleteAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteAliasWithResponse#String-RequestOptions
        SearchAlias searchAlias = SEARCH_INDEX_CLIENT.getAlias("my-alias");

        Response<Void> response = SEARCH_INDEX_CLIENT.deleteAliasWithResponse(searchAlias.getName(),
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, searchAlias.getETag())
                .setContext(new Context(KEY_1, VALUE_1)));

        System.out.printf("Response status code %d. Deleted alias 'my-alias'.", response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteAliasWithResponse#String-RequestOptions
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#listAliases()}.
     */
    public void listAliasesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.listAliases
        SEARCH_INDEX_ASYNC_CLIENT.listAliases()
            .doOnNext(searchAlias -> System.out.printf("Listed alias '%s' that aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)))
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.listAliases
    }

    /**
     * Code snippet for {@link SearchIndexClient#listAliases()}.
     */
    public void listAliases() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listAliases
        SEARCH_INDEX_CLIENT.listAliases()
            .forEach(searchAlias -> System.out.printf("Listed alias '%s' that aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexClient.listAliases
    }

    /**
     * Code snippet for {@link SearchIndexClient#listAliases(RequestOptions)}.
     */
    public void listAliasesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listAliases#RequestOptions
        SEARCH_INDEX_CLIENT.listAliases(new RequestOptions().setContext(new Context(KEY_1, VALUE_1)))
            .forEach(searchAlias -> System.out.printf("Listed alias '%s' that aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexClient.listAliases#RequestOptions
    }
}
