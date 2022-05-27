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
import com.azure.search.documents.indexes.models.CreateOrUpdateDataSourceConnectionOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateIndexerOptions;
import com.azure.search.documents.indexes.models.CreateOrUpdateSkillsetOptions;
import com.azure.search.documents.indexes.models.FieldMapping;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.LexicalTokenizerName;
import com.azure.search.documents.indexes.models.OcrSkill;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchAlias;
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
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.AutocompleteMode;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.IndexDocumentsOptions;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedIterable;
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import com.azure.search.documents.util.SuggestPagedIterable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class SearchJavaDocCodeSnippets {
    private static final SearchClient SEARCH_CLIENT = new SearchClientBuilder().buildClient();

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
     * Code snippet for {@link SearchClient#uploadDocuments(Iterable)}.
     */
    public void uploadDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.uploadDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        IndexDocumentsResult result = SEARCH_CLIENT.uploadDocuments(Collections.singletonList(searchDocument));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s upload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.uploadDocuments#Iterable
    }

    /**
     * Code snippet for {@link SearchClient#uploadDocumentsWithResponse(Iterable, IndexDocumentsOptions, Context)}
     */
    public void uploadDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.uploadDocumentsWithResponse(
            Collections.singletonList(searchDocument), null, new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s upload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
    }

    /**
     * Code snippet for {@link SearchClient#mergeDocuments(Iterable)}
     */
    public void mergeDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.mergeDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelName", "merge");
        IndexDocumentsResult result = SEARCH_CLIENT.mergeDocuments(Collections.singletonList(searchDocument));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.mergeDocuments#Iterable
    }

    /**
     * Code snippet for {@link SearchClient#mergeDocumentsWithResponse(Iterable, IndexDocumentsOptions, Context)}
     */
    public void mergeDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.mergeDocumentsWithResponse(
            Collections.singletonList(searchDocument), null, new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
    }

    /**
     * Code snippet for {@link SearchClient#mergeOrUploadDocuments(Iterable)}
     */
    public void mergeOrUploadDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.mergeOrUploadDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        IndexDocumentsResult result = SEARCH_CLIENT.mergeOrUploadDocuments(Collections.singletonList(searchDocument));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.mergeOrUploadDocuments#Iterable
    }

    /**
     * Code snippet for {@link SearchClient#mergeOrUploadDocumentsWithResponse(Iterable, IndexDocumentsOptions, Context)}
     */
    public void mergeOrUploadDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.mergeOrUploadDocumentsWithResponse(
            Collections.singletonList(searchDocument), null, new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
    }

    /**
     * Code snippet for {@link SearchClient#deleteDocuments(Iterable)}
     */
    public void deleteDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.deleteDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        IndexDocumentsResult result = SEARCH_CLIENT.deleteDocuments(Collections.singletonList(searchDocument));
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.deleteDocuments#Iterable
    }


    /**
     * Code snippet for {@link SearchClient#deleteDocumentsWithResponse(Iterable, IndexDocumentsOptions, Context)}
     */
    public void deleteDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.deleteDocumentsWithResponse(
            Collections.singletonList(searchDocument), null, new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions-Context
    }

    /**
     * Code snippet for {@link SearchClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void indexDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch
        SearchDocument searchDocument1 = new SearchDocument();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        SearchDocument searchDocument2 = new SearchDocument();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch<SearchDocument> indexDocumentsBatch = new IndexDocumentsBatch<>();
        indexDocumentsBatch.addUploadActions(Collections.singletonList(searchDocument1));
        indexDocumentsBatch.addDeleteActions(Collections.singletonList(searchDocument2));
        IndexDocumentsResult result = SEARCH_CLIENT.indexDocuments(indexDocumentsBatch);
        for (IndexingResult indexingResult : result.getResults()) {
            System.out.printf("Does document with key %s finish successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocuments#IndexDocumentsBatch
    }

    /**
     * Code snippet for {@link SearchClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions, Context)}
     */
    public void indexDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-Context
        SearchDocument searchDocument1 = new SearchDocument();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        SearchDocument searchDocument2 = new SearchDocument();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch<SearchDocument> indexDocumentsBatch = new IndexDocumentsBatch<>();
        indexDocumentsBatch.addUploadActions(Collections.singletonList(searchDocument1));
        indexDocumentsBatch.addDeleteActions(Collections.singletonList(searchDocument2));
        Response<IndexDocumentsResult> resultResponse = SEARCH_CLIENT.indexDocumentsWithResponse(indexDocumentsBatch,
            null, new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
            System.out.printf("Does document with key %s finish successfully? %b%n", indexingResult.getKey(),
                indexingResult.isSucceeded());
        }
        // END: com.azure.search.documents.SearchClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions-Context
    }

    /**
     * Code snippet for {@link SearchClient#getDocument(String, Class)}
     */
    public void getDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.getDocuments#String-Class
        SearchDocument result = SEARCH_CLIENT.getDocument("hotelId", SearchDocument.class);
        for (Map.Entry<String, Object> keyValuePair : result.entrySet()) {
            System.out.printf("Document key %s, Document value %s", keyValuePair.getKey(), keyValuePair.getValue());
        }
        // END: com.azure.search.documents.SearchClient.getDocuments#String-Class
    }

    /**
     * Code snippet for {@link SearchClient#getDocumentWithResponse(String, Class, List, Context)}
     */
    public void getDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.getDocumentWithResponse#String-Class-List-Context
        Response<SearchDocument> resultResponse = SEARCH_CLIENT.getDocumentWithResponse("hotelId",
            SearchDocument.class, null, new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + resultResponse.getStatusCode());
        for (Map.Entry<String, Object> keyValuePair : resultResponse.getValue().entrySet()) {
            System.out.printf("Document key %s, Document value %s", keyValuePair.getKey(), keyValuePair.getValue());
        }
        // END: com.azure.search.documents.SearchClient.getDocumentWithResponse#String-Class-List-Context
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
     * Code snippet for {@link SearchClient#getDocumentCountWithResponse(Context)}
     */
    public void getDocumentCountWithResponse() {
        // BEGIN: com.azure.search.documents.SearchClient.getDocumentCountWithResponse#Context
        Response<Long> countResponse = SEARCH_CLIENT.getDocumentCountWithResponse(new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + countResponse.getStatusCode());
        System.out.printf("There are %d documents in service.", countResponse.getValue());
        // END: com.azure.search.documents.SearchClient.getDocumentCountWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchClient#search(String)}
     */
    public void searchDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.search#String
        SearchPagedIterable searchPagedIterable = SEARCH_CLIENT.search("searchText");
        System.out.printf("There are around %d results.", searchPagedIterable.getTotalCount());

        for (SearchPagedResponse resultResponse: searchPagedIterable.iterableByPage()) {
            System.out.println("The status code of the response is " + resultResponse.getStatusCode());
            resultResponse.getValue().forEach(searchResult -> {
                for (Map.Entry<String, Object> keyValuePair: searchResult
                    .getDocument(SearchDocument.class).entrySet()) {
                    System.out.printf("Document key %s, document value %s", keyValuePair.getKey(),
                        keyValuePair.getValue());
                }
            });
        }
        // END: com.azure.search.documents.SearchClient.search#String
    }

    /**
     * Code snippet for {@link SearchClient#search(String, SearchOptions, Context)}
     */
    public void searchDocumentsWithOptions() {
        // BEGIN: com.azure.search.documents.SearchClient.search#String-SearchOptions-Context
        SearchPagedIterable searchPagedIterable = SEARCH_CLIENT.search("searchText",
            new SearchOptions().setOrderBy("hotelId desc"), new Context(KEY_1, VALUE_1));
        System.out.printf("There are around %d results.", searchPagedIterable.getTotalCount());
        for (SearchPagedResponse resultResponse: searchPagedIterable.iterableByPage()) {
            System.out.println("The status code of the response is " + resultResponse.getStatusCode());
            resultResponse.getValue().forEach(searchResult -> {
                for (Map.Entry<String, Object> keyValuePair: searchResult
                    .getDocument(SearchDocument.class).entrySet()) {
                    System.out.printf("Document key %s, document value %s", keyValuePair.getKey(),
                        keyValuePair.getValue());
                }
            });
        }
        // END: com.azure.search.documents.SearchClient.search#String-SearchOptions-Context
    }

    /**
     * Code snippet for {@link SearchClient#suggest(String, String)}
     */
    public void suggestDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.suggest#String-String
        SuggestPagedIterable suggestPagedIterable = SEARCH_CLIENT.suggest("searchText", "sg");
        for (SuggestResult result: suggestPagedIterable) {
            SearchDocument searchDocument = result.getDocument(SearchDocument.class);
            for (Map.Entry<String, Object> keyValuePair: searchDocument.entrySet()) {
                System.out.printf("Document key %s, document value %s", keyValuePair.getKey(), keyValuePair.getValue());
            }
        }
        // END: com.azure.search.documents.SearchClient.suggest#String-String
    }

    /**
     * Code snippet for {@link SearchClient#suggest(String, String, SuggestOptions, Context)}
     */
    public void suggestDocumentsWithOptions() {
        // BEGIN: com.azure.search.documents.SearchClient.suggest#String-String-SuggestOptions-Context
        SuggestPagedIterable suggestPagedIterable = SEARCH_CLIENT.suggest("searchText", "sg",
            new SuggestOptions().setOrderBy("hotelId desc"), new Context(KEY_1, VALUE_1));
        for (SuggestResult result: suggestPagedIterable) {
            SearchDocument searchDocument = result.getDocument(SearchDocument.class);
            for (Map.Entry<String, Object> keyValuePair: searchDocument.entrySet()) {
                System.out.printf("Document key %s, document value %s", keyValuePair.getKey(), keyValuePair.getValue());
            }
        }
        // END: com.azure.search.documents.SearchClient.suggest#String-String-SuggestOptions-Context
    }

    /**
     * Code snippet for {@link SearchClient#autocomplete(String, String)}
     */
    public void autocompleteDocuments() {
        // BEGIN: com.azure.search.documents.SearchClient.autocomplete#String-String
        AutocompletePagedIterable autocompletePagedIterable = SEARCH_CLIENT.autocomplete("searchText", "sg");
        for (AutocompleteItem result: autocompletePagedIterable) {
            System.out.printf("The complete term is %s", result.getText());
        }
        // END: com.azure.search.documents.SearchClient.autocomplete#String-String
    }

    /**
     * Code snippet for {@link SearchClient#autocomplete(String, String, AutocompleteOptions, Context)}
     */
    public void autocompleteDocumentsWithOptions() {
        // BEGIN: com.azure.search.documents.SearchClient.autocomplete#String-String-AutocompleteOptions-Context
        AutocompletePagedIterable autocompletePagedIterable = SEARCH_CLIENT.autocomplete("searchText", "sg",
            new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT),
            new Context(KEY_1, VALUE_1));
        for (AutocompleteItem result: autocompletePagedIterable) {
            System.out.printf("The complete term is %s", result.getText());
        }
        // END: com.azure.search.documents.SearchClient.autocomplete#String-String-AutocompleteOptions-Context
    }

    private static final SearchAsyncClient SEARCH_ASYNC_CLIENT = new SearchClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link SearchAsyncClient#uploadDocuments(Iterable)}.
     */
    public void uploadDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.uploadDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.uploadDocuments(Collections.singletonList(searchDocument))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s upload successfully? %b%n",
                        indexingResult.getKey(), indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.uploadDocuments#Iterable
    }

    /**
     * Code snippet for {@link SearchAsyncClient#uploadDocumentsWithResponse(Iterable, IndexDocumentsOptions)}
     */
    public void uploadDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.uploadDocumentsWithResponse(Collections.singletonList(searchDocument), null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s upload successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.uploadDocumentsWithResponse#Iterable-IndexDocumentsOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#mergeDocuments(Iterable)}
     */
    public void mergeDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.mergeDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelName", "merge");
        SEARCH_ASYNC_CLIENT.mergeDocuments(Collections.singletonList(searchDocument))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.mergeDocuments#Iterable
    }

    /**
     * Code snippet for {@link SearchAsyncClient#mergeDocumentsWithResponse(Iterable, IndexDocumentsOptions)}
     */
    public void mergeDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.mergeDocumentsWithResponse(Collections.singletonList(searchDocument), null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s merge successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.mergeDocumentsWithResponse#Iterable-IndexDocumentsOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#mergeOrUploadDocuments(Iterable)}
     */
    public void mergeOrUploadDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.mergeOrUploadDocuments(Collections.singletonList(searchDocument))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n",
                        indexingResult.getKey(), indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocuments#Iterable
    }

    /**
     * Code snippet for {@link SearchAsyncClient#mergeOrUploadDocumentsWithResponse(Iterable, IndexDocumentsOptions)}
     */
    public void mergeOrUploadDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.mergeOrUploadDocumentsWithResponse(Collections.singletonList(searchDocument), null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s mergeOrUpload successfully? %b%n",
                        indexingResult.getKey(), indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.mergeOrUploadDocumentsWithResponse#Iterable-IndexDocumentsOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#deleteDocuments(Iterable)}
     */
    public void deleteDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.deleteDocuments#Iterable
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.deleteDocuments(Collections.singletonList(searchDocument))
            .subscribe(result -> {
                for (IndexingResult indexingResult : result.getResults()) {
                    System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.deleteDocuments#Iterable
    }


    /**
     * Code snippet for {@link SearchAsyncClient#deleteDocumentsWithResponse(Iterable, IndexDocumentsOptions)}
     */
    public void deleteDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions
        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("hotelId", "1");
        searchDocument.put("hotelName", "test");
        SEARCH_ASYNC_CLIENT.deleteDocumentsWithResponse(Collections.singletonList(searchDocument), null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s delete successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.deleteDocumentsWithResponse#Iterable-IndexDocumentsOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#indexDocuments(IndexDocumentsBatch)}
     */
    public void indexDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocuments#IndexDocumentsBatch
        SearchDocument searchDocument1 = new SearchDocument();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        SearchDocument searchDocument2 = new SearchDocument();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch<SearchDocument> indexDocumentsBatch = new IndexDocumentsBatch<>();
        indexDocumentsBatch.addUploadActions(Collections.singletonList(searchDocument1));
        indexDocumentsBatch.addDeleteActions(Collections.singletonList(searchDocument2));
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
     * Code snippet for {@link SearchAsyncClient#indexDocumentsWithResponse(IndexDocumentsBatch, IndexDocumentsOptions)}
     */
    public void indexDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions
        SearchDocument searchDocument1 = new SearchDocument();
        searchDocument1.put("hotelId", "1");
        searchDocument1.put("hotelName", "test1");
        SearchDocument searchDocument2 = new SearchDocument();
        searchDocument2.put("hotelId", "2");
        searchDocument2.put("hotelName", "test2");
        IndexDocumentsBatch<SearchDocument> indexDocumentsBatch = new IndexDocumentsBatch<>();
        indexDocumentsBatch.addUploadActions(Collections.singletonList(searchDocument1));
        indexDocumentsBatch.addDeleteActions(Collections.singletonList(searchDocument2));
        SEARCH_ASYNC_CLIENT.indexDocumentsWithResponse(indexDocumentsBatch, null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (IndexingResult indexingResult : resultResponse.getValue().getResults()) {
                    System.out.printf("Does document with key %s finish successfully? %b%n", indexingResult.getKey(),
                        indexingResult.isSucceeded());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.indexDocumentsWithResponse#IndexDocumentsBatch-IndexDocumentsOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#getDocument(String, Class)}
     */
    public void getDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.getDocuments#String-Class
        SEARCH_ASYNC_CLIENT.getDocument("hotelId", SearchDocument.class)
            .subscribe(result -> {
                for (Map.Entry<String, Object> keyValuePair : result.entrySet()) {
                    System.out.printf("Document key %s, Document value %s", keyValuePair.getKey(),
                        keyValuePair.getValue());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.getDocuments#String-Class
    }

    /**
     * Code snippet for {@link SearchAsyncClient#getDocumentWithResponse(String, Class, List)}
     */
    public void getDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.getDocumentWithResponse#String-Class-List
        SEARCH_ASYNC_CLIENT.getDocumentWithResponse("hotelId", SearchDocument.class, null)
            .subscribe(resultResponse -> {
                System.out.println("The status code of the response is " + resultResponse.getStatusCode());
                for (Map.Entry<String, Object> keyValuePair : resultResponse.getValue().entrySet()) {
                    System.out.printf("Document key %s, Document value %s", keyValuePair.getKey(),
                        keyValuePair.getValue());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.getDocumentWithResponse#String-Class-List
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
     * Code snippet for {@link SearchAsyncClient#getDocumentCountWithResponse()}
     */
    public void getDocumentCountWithResponseAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.getDocumentCountWithResponse
        SEARCH_ASYNC_CLIENT.getDocumentCountWithResponse()
            .subscribe(countResponse -> {
                System.out.println("The status code of the response is " + countResponse.getStatusCode());
                System.out.printf("There are %d documents in service.", countResponse.getValue());
            });
        // END: com.azure.search.documents.SearchAsyncClient.getDocumentCountWithResponse
    }

    /**
     * Code snippet for {@link SearchAsyncClient#search(String)}
     */
    public void searchDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.search#String
        SearchPagedFlux searchPagedFlux = SEARCH_ASYNC_CLIENT.search("searchText");
        searchPagedFlux.getTotalCount().subscribe(
            count -> System.out.printf("There are around %d results.", count)
        );
        searchPagedFlux.byPage()
            .subscribe(resultResponse -> {
                for (SearchResult result: resultResponse.getValue()) {
                    SearchDocument searchDocument = result.getDocument(SearchDocument.class);
                    for (Map.Entry<String, Object> keyValuePair: searchDocument.entrySet()) {
                        System.out.printf("Document key %s, document value %s", keyValuePair.getKey(), keyValuePair.getValue());
                    }
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.search#String
    }

    /**
     * Code snippet for {@link SearchAsyncClient#search(String, SearchOptions, Context)}
     */
    public void searchDocumentsWithOptionsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.search#String-SearchOptions
        SearchPagedFlux pagedFlux = SEARCH_ASYNC_CLIENT.search("searchText",
            new SearchOptions().setOrderBy("hotelId desc"));

        pagedFlux.getTotalCount().subscribe(count -> System.out.printf("There are around %d results.", count));

        pagedFlux.byPage()
            .subscribe(searchResultResponse -> searchResultResponse.getValue().forEach(searchDocument -> {
                for (Map.Entry<String, Object> keyValuePair
                    : searchDocument.getDocument(SearchDocument.class).entrySet()) {
                    System.out.printf("Document key %s, document value %s", keyValuePair.getKey(),
                        keyValuePair.getValue());
                }
            }));
        // END: com.azure.search.documents.SearchAsyncClient.search#String-SearchOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#suggest(String, String)}
     */
    public void suggestDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.suggest#String-String
        SEARCH_ASYNC_CLIENT.suggest("searchText", "sg")
            .subscribe(results -> {
                for (Map.Entry<String, Object> keyValuePair: results.getDocument(SearchDocument.class).entrySet()) {
                    System.out.printf("Document key %s, document value %s", keyValuePair.getKey(),
                        keyValuePair.getValue());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.suggest#String-String
    }

    /**
     * Code snippet for {@link SearchAsyncClient#suggest(String, String, SuggestOptions)}
     */
    public void suggestDocumentsWithOptionsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.suggest#String-String-SuggestOptions
        SEARCH_ASYNC_CLIENT.suggest("searchText", "sg",
            new SuggestOptions().setOrderBy("hotelId desc"))
            .subscribe(results -> {
                for (Map.Entry<String, Object> keyValuePair: results.getDocument(SearchDocument.class).entrySet()) {
                    System.out.printf("Document key %s, document value %s", keyValuePair.getKey(),
                        keyValuePair.getValue());
                }
            });
        // END: com.azure.search.documents.SearchAsyncClient.suggest#String-String-SuggestOptions
    }

    /**
     * Code snippet for {@link SearchAsyncClient#autocomplete(String, String)}
     */
    public void autocompleteDocumentsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.autocomplete#String-String
        SEARCH_ASYNC_CLIENT.autocomplete("searchText", "sg")
            .subscribe(result -> System.out.printf("The complete term is %s", result.getText()));
        // END: com.azure.search.documents.SearchAsyncClient.autocomplete#String-String
    }

    /**
     * Code snippet for {@link SearchAsyncClient#autocomplete(String, String, AutocompleteOptions)}
     */
    public void autocompleteDocumentsWithOptionsAsync() {
        // BEGIN: com.azure.search.documents.SearchAsyncClient.autocomplete#String-String-AutocompleteOptions
        SEARCH_ASYNC_CLIENT.autocomplete("searchText", "sg",
            new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT))
            .subscribe(result ->
                System.out.printf("The complete term is %s", result.getText())
            );
        // END: com.azure.search.documents.SearchAsyncClient.autocomplete#String-String-AutocompleteOptions
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
        List<SearchField> searchFields = Arrays.asList(
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true)
        );
        SearchIndex searchIndex = new SearchIndex("searchIndex", searchFields);
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.createIndex(searchIndex);
        System.out.printf("The index name is %s. The ETag of index is %s.%n", indexFromService.getName(),
            indexFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexClient#createIndexWithResponse(SearchIndex, Context)}.
     */
    public void createSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createIndexWithResponse#SearchIndex-Context
        List<SearchField> searchFields = Arrays.asList(
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true)
        );
        SearchIndex searchIndex = new SearchIndex("searchIndex", searchFields);

        Response<SearchIndex> indexFromServiceResponse =
            SEARCH_INDEX_CLIENT.createIndexWithResponse(searchIndex, new Context(KEY_1, VALUE_1));
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
            SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        System.out.printf("The index name is %s. The ETag of index is %s.%n", indexFromService.getName(),
            indexFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndex#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexWithResponse(String, Context)}}
     */
    public void getSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-Context
        Response<SearchIndex> indexFromServiceResponse =
            SEARCH_INDEX_CLIENT.getIndexWithResponse("searchIndex", new Context(KEY_1, VALUE_1));

        System.out.printf("The status code of the response is %s. The index name is %s.%n",
            indexFromServiceResponse.getStatusCode(), indexFromServiceResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexStatistics(String)}
     */
    public void getSearchIndexStatistics() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String
        SearchIndexStatistics statistics = SEARCH_INDEX_CLIENT.getIndexStatistics("searchIndex");
        System.out.printf("There are %d documents and storage size of %d available in 'searchIndex'.%n",
            statistics.getDocumentCount(), statistics.getStorageSize());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatistics#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#getIndexStatisticsWithResponse(String, Context)}
     */
    public void getSearchIndexStatisticsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getIndexStatisticsWithResponse#String-Context
        Response<SearchIndexStatistics> statistics = SEARCH_INDEX_CLIENT.getIndexStatisticsWithResponse("searchIndex",
            new Context(KEY_1, VALUE_1));
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
        PagedIterable<SearchIndex> indexes = SEARCH_INDEX_CLIENT.listIndexes();
        for (SearchIndex index: indexes) {
            System.out.printf("The index name is %s. The ETag of index is %s.%n", index.getName(),
                index.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexes
    }

    /**
     * Code snippet for {@link SearchIndexClient#listIndexes(Context)}
     */
    public void listIndexesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context
        PagedIterable<SearchIndex> indexes = SEARCH_INDEX_CLIENT.listIndexes(new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is"
            + indexes.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndex index: indexes) {
            System.out.printf("The index name is %s. The ETag of index is %s.%n", index.getName(), index.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listIndexesWithResponse#Context
    }

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

    /**
     * Code snippet for {@link SearchIndexClient#listIndexNames(Context)}
     */
    public void listIndexNamesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listIndexNames#Context
        PagedIterable<String> indexes = SEARCH_INDEX_CLIENT.listIndexNames(new Context(KEY_1, VALUE_1));
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
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        indexFromService.setSuggesters(Collections.singletonList(new SearchSuggester("sg",
            Collections.singletonList("hotelName"))));
        SearchIndex updatedIndex = SEARCH_INDEX_CLIENT.createOrUpdateIndex(indexFromService);
        System.out.printf("The index name is %s. The suggester name of index is %s.%n", updatedIndex.getName(),
            updatedIndex.getSuggesters().get(0).getName());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexClient#createIndexWithResponse(SearchIndex, Context)}
     */
    public void createOrUpdateIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        indexFromService.setSuggesters(Collections.singletonList(new SearchSuggester("sg",
            Collections.singletonList("hotelName"))));
        Response<SearchIndex> updatedIndexResponse = SEARCH_INDEX_CLIENT.createOrUpdateIndexWithResponse(indexFromService, true,
            false, new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the normal response is %s.%n"
                + "The index name is %s. The ETag of index is %s.%n", updatedIndexResponse.getStatusCode(),
            updatedIndexResponse.getValue().getName(), updatedIndexResponse.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context
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
     * Code snippet for {@link SearchIndexClient#deleteIndexWithResponse(SearchIndex, boolean, Context)}
     */
    public void deleteSearchIndexWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#SearchIndex-boolean-Context
        SearchIndex indexFromService = SEARCH_INDEX_CLIENT.getIndex("searchIndex");
        Response<Void> deleteResponse = SEARCH_INDEX_CLIENT.deleteIndexWithResponse(indexFromService, true,
            new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteIndexWithResponse#SearchIndex-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#analyzeText(String, AnalyzeTextOptions)}
     */
    public void analyzeText() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.analyzeText#String-AnalyzeTextOptions
        PagedIterable<AnalyzedTokenInfo> tokenInfos = SEARCH_INDEX_CLIENT.analyzeText("searchIndex",
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
        PagedIterable<AnalyzedTokenInfo> tokenInfos = SEARCH_INDEX_CLIENT.analyzeText("searchIndex",
            new AnalyzeTextOptions("The quick brown fox", LexicalTokenizerName.CLASSIC), new Context(KEY_1, VALUE_1));
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
        SynonymMap synonymMapFromService = SEARCH_INDEX_CLIENT.createSynonymMap(synonymMap);
        System.out.printf("The synonym map name is %s. The ETag of synonym map is %s.%n",
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
        Response<SynonymMap> synonymMapFromService = SEARCH_INDEX_CLIENT.createSynonymMapWithResponse(synonymMap,
            new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %d.%n"
                + "The synonym map name is %s. The ETag of synonym map is %s.%n", synonymMapFromService.getStatusCode(),
            synonymMapFromService.getValue().getName(), synonymMapFromService.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createSynonymMapWithResponse#SynonymMap-Context
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
     * Code snippet for {@link SearchIndexClient#getSynonymMapWithResponse(String, Context)}}
     */
    public void getSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-Context
        Response<SynonymMap> synonymMapFromService =
            SEARCH_INDEX_CLIENT.getSynonymMapWithResponse("synonymMap", new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %d.%n"
                + "The synonym map name is %s. The ETag of synonym map is %s.%n", synonymMapFromService.getStatusCode(),
            synonymMapFromService.getValue().getName(), synonymMapFromService.getValue().getETag());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getSynonymMapWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMaps()}
     */
    public void listSynonymMaps() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps
        PagedIterable<SynonymMap> synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMaps();
        for (SynonymMap synonymMap: synonymMaps) {
            System.out.printf("The synonymMap name is %s. The ETag of synonymMap is %s.%n", synonymMap.getName(),
                synonymMap.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMaps
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMaps(Context)}
     */
    public void listSynonymMapsWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context
        PagedIterable<SynonymMap> synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMaps(new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is"
            + synonymMaps.iterableByPage().iterator().next().getStatusCode());
        for (SynonymMap index: synonymMaps) {
            System.out.printf("The index name is %s. The ETag of index is %s.%n", index.getName(), index.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapsWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexClient#listSynonymMapNames()}
     */
    public void listSynonymMapNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listSynonymMapNames
        PagedIterable<String> synonymMaps = SEARCH_INDEX_CLIENT.listSynonymMapNames();
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
        PagedIterable<String> synonymMaps = SEARCH_INDEX_CLIENT.listIndexNames(new Context(KEY_1, VALUE_1));
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
        SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap("searchIndex");
        synonymMap.setSynonyms("United States, United States of America, USA, America\nWashington, Wash. => WA");
        SynonymMap updatedSynonymMap = SEARCH_INDEX_CLIENT.createOrUpdateSynonymMap(synonymMap);
        System.out.printf("The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getName(),
            updatedSynonymMap.getSynonyms());
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateSynonymMapWithResponse(SynonymMap, boolean, Context)}
     */
    public void createOrUpdateSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context
        SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap("searchIndex");
        synonymMap.setSynonyms("United States, United States of America, USA, America\nWashington, Wash. => WA");
        Response<SynonymMap> updatedSynonymMap =
            SEARCH_INDEX_CLIENT.createOrUpdateSynonymMapWithResponse(synonymMap, true,
                new Context(KEY_1, VALUE_1));
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
        SEARCH_INDEX_CLIENT.deleteSynonymMap("synonymMap");
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMap#String
    }

    /**
     * Code snippet for {@link SearchIndexClient#deleteSynonymMapWithResponse(SynonymMap, boolean, Context)}
     */
    public void deleteSynonymMapWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#SynonymMap-boolean-Context
        SynonymMap synonymMap = SEARCH_INDEX_CLIENT.getSynonymMap("synonymMap");
        Response<Void> response = SEARCH_INDEX_CLIENT.deleteSynonymMapWithResponse(synonymMap, true,
            new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is" + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteSynonymMapWithResponse#SynonymMap-boolean-Context
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
     * Code snippet for {@link SearchIndexClient#getServiceStatisticsWithResponse(Context)}
     */
    public void getServiceStatisticsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#Context
        Response<SearchServiceStatistics> serviceStatistics =
            SEARCH_INDEX_CLIENT.getServiceStatisticsWithResponse(new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThere are %s search indexes in your service.%n",
            serviceStatistics.getStatusCode(),
            serviceStatistics.getValue().getCounters().getIndexCounter());
        // END: com.azure.search.documents.indexes.SearchIndexClient.getServiceStatisticsWithResponse#Context
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
        List<SearchField> searchFields = Arrays.asList(
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true)
        );
        SearchIndex searchIndex = new SearchIndex("searchIndex", searchFields);
        SEARCH_INDEX_ASYNC_CLIENT.createIndex(searchIndex)
            .subscribe(indexFromService ->
                System.out.printf("The index name is %s. The ETag of index is %s.%n", indexFromService.getName(),
                indexFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndex#SearchIndex
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createIndexWithResponse(SearchIndex)}.
     */
    public void createSearchIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndexWithResponse#SearchIndex
        List<SearchField> searchFields = Arrays.asList(
            new SearchField("hotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("hotelName", SearchFieldDataType.STRING).setSearchable(true)
        );
        SearchIndex searchIndex = new SearchIndex("searchIndex", searchFields);

        SEARCH_INDEX_ASYNC_CLIENT.createIndexWithResponse(searchIndex)
            .subscribe(indexFromServiceResponse ->
                System.out.printf("The status code of the response is %s. The index name is %s.%n",
                indexFromServiceResponse.getStatusCode(), indexFromServiceResponse.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createIndexWithResponse#SearchIndex
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
     * Code snippet for {@link SearchIndexAsyncClient#getIndexWithResponse(String)}}
     */
    public void getSearchIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexWithResponse#String
        SEARCH_INDEX_ASYNC_CLIENT.getIndexWithResponse("searchIndex")
            .subscribe(indexFromServiceResponse ->
                System.out.printf("The status code of the response is %s. The index name is %s.%n",
                    indexFromServiceResponse.getStatusCode(), indexFromServiceResponse.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexWithResponse#String
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
     * Code snippet for {@link SearchIndexAsyncClient#getIndexStatisticsWithResponse(String)}
     */
    public void getSearchIndexStatisticsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatisticsWithResponse#String
        SEARCH_INDEX_ASYNC_CLIENT.getIndexStatisticsWithResponse("searchIndex")
            .subscribe(statistics -> System.out.printf("The status code of the response is %s.%n"
                    + "There are %d documents and storage size of %d available in 'searchIndex'.%n",
                statistics.getStatusCode(), statistics.getValue().getDocumentCount(),
                statistics.getValue().getStorageSize()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getIndexStatisticsWithResponse#String
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
     * Code snippet for {@link SearchIndexAsyncClient#createIndexWithResponse(SearchIndex)}
     */
    public void createOrUpdateIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context
        SEARCH_INDEX_ASYNC_CLIENT.getIndex("searchIndex")
            .doOnNext(indexFromService -> indexFromService.setSuggesters(Collections.singletonList(
                new SearchSuggester("sg", Collections.singletonList("hotelName")))))
            .flatMap(indexFromService -> SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateIndexWithResponse(indexFromService, true,
                false))
            .subscribe(updatedIndexResponse -> System.out.printf("The status code of the normal response is %s.%n"
                    + "The index name is %s. The ETag of index is %s.%n", updatedIndexResponse.getStatusCode(),
                updatedIndexResponse.getValue().getName(), updatedIndexResponse.getValue().getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateIndexWithResponse#SearchIndex-boolean-boolean-Context
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
     * Code snippet for {@link SearchIndexAsyncClient#deleteIndexWithResponse(SearchIndex, boolean)}
     */
    public void deleteSearchIndexWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndexWithResponse#SearchIndex-boolean
        SEARCH_INDEX_ASYNC_CLIENT.getIndex("searchIndex")
            .flatMap(indexFromService -> SEARCH_INDEX_ASYNC_CLIENT.deleteIndexWithResponse(indexFromService, true))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteIndexWithResponse#SearchIndex-boolean
    }

    /**
     * Code snippet for {@link SearchIndexClient#analyzeText(String, AnalyzeTextOptions)}
     */
    public void analyzeTextAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.analyzeText#String-AnalyzeTextOptions
        SEARCH_INDEX_ASYNC_CLIENT.analyzeText("searchIndex",
            new AnalyzeTextOptions("The quick brown fox", LexicalTokenizerName.CLASSIC))
            .subscribe(tokenInfo ->
                System.out.printf("The token emitted by the analyzer is %s.%n", tokenInfo.getToken()));
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
     * Code snippet for {@link SearchIndexAsyncClient#createSynonymMapWithResponse(SynonymMap)}
     */
    public void createSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMapWithResponse#SynonymMap
        SynonymMap synonymMap = new SynonymMap("synonymMap",
            "United States, United States of America, USA\nWashington, Wash. => WA");
        SEARCH_INDEX_ASYNC_CLIENT.createSynonymMapWithResponse(synonymMap)
            .subscribe(synonymMapFromService ->
                System.out.printf("The status code of the response is %d.%n"
                    + "The synonym map name is %s. The ETag of synonym map is %s.%n",
                    synonymMapFromService.getStatusCode(),
                synonymMapFromService.getValue().getName(), synonymMapFromService.getValue().getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createSynonymMapWithResponse#SynonymMap
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
     * Code snippet for {@link SearchIndexAsyncClient#getSynonymMapWithResponse(String)}}
     */
    public void getSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMapWithResponse#String
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMapWithResponse("synonymMap")
            .subscribe(synonymMapFromService -> System.out.printf("The status code of the response is %d.%n"
                    + "The synonym map name is %s. The ETag of synonym map is %s.%n",
                synonymMapFromService.getStatusCode(), synonymMapFromService.getValue().getName(),
                synonymMapFromService.getValue().getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getSynonymMapWithResponse#String
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#listSynonymMaps()}
     */
    public void listSynonymMapsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.listSynonymMaps
        SEARCH_INDEX_ASYNC_CLIENT.listSynonymMaps()
            .subscribe(synonymMap -> System.out.printf("The synonymMap name is %s. The ETag of synonymMap is %s.%n",
                synonymMap.getName(), synonymMap.getETag()));
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
            .doOnNext(synonymMap -> synonymMap
                .setSynonyms("United States, United States of America, USA, America\nWashington, Wash. => WA"))
            .flatMap(SEARCH_INDEX_ASYNC_CLIENT::createOrUpdateSynonymMap)
            .subscribe(updatedSynonymMap ->
                System.out.printf("The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getName(),
                updatedSynonymMap.getSynonyms()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMap#SynonymMap
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateSynonymMapWithResponse(SynonymMap, boolean)}
     */
    public void createOrUpdateSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("searchIndex")
            .flatMap(synonymMap -> {
                synonymMap.setSynonyms(
                    "United States, United States of America, USA, America\nWashington, Wash. => WA");
                return SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateSynonymMapWithResponse(synonymMap, true);
            })
            .subscribe(updatedSynonymMap ->
                System.out.printf("The status code of the normal response is %s.%n"
                    + "The synonym map name is %s. The synonyms are %s.%n", updatedSynonymMap.getStatusCode(),
                updatedSynonymMap.getValue().getName(), updatedSynonymMap.getValue().getSynonyms()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateSynonymMapWithResponse#SynonymMap-boolean-Context
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
     * Code snippet for {@link SearchIndexAsyncClient#deleteSynonymMapWithResponse(SynonymMap, boolean)}
     */
    public void deleteSynonymMapWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMapWithResponse#SynonymMap-boolean
        SEARCH_INDEX_ASYNC_CLIENT.getSynonymMap("synonymMap")
            .flatMap(synonymMap -> SEARCH_INDEX_ASYNC_CLIENT.deleteSynonymMapWithResponse(synonymMap, true))
            .subscribe(response -> System.out.println("The status code of the response is" + response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteSynonymMapWithResponse#SynonymMap-boolean
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
     * Code snippet for {@link SearchIndexAsyncClient#getServiceStatisticsWithResponse()}
     */
    public void getServiceStatisticsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatisticsWithResponse
        SEARCH_INDEX_ASYNC_CLIENT.getServiceStatisticsWithResponse()
            .subscribe(serviceStatistics ->
                System.out.printf("The status code of the response is %s.%n"
                        + "There are %s search indexes in your service.%n",
                serviceStatistics.getStatusCode(),
                serviceStatistics.getValue().getCounters().getIndexCounter()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getServiceStatisticsWithResponse
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
     * Code snippet for {@link SearchIndexerClient#createIndexerWithResponse(SearchIndexer, Context)}.
     */
    public void createSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createIndexerWithResponse#SearchIndexer-Context
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        Response<SearchIndexer> indexerFromServiceResponse = SEARCH_INDEXER_CLIENT.createIndexerWithResponse(
            searchIndexer, new Context(KEY_1, VALUE_1));

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
            SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexerFromService.getName(),
            indexerFromService.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexer#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getIndexerWithResponse(String, Context)}}
     */
    public void getSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-Context
        Response<SearchIndexer> indexerFromServiceResponse = SEARCH_INDEXER_CLIENT.getIndexerWithResponse(
            "searchIndexer", new Context(KEY_1, VALUE_1));

        System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
            indexerFromServiceResponse.getStatusCode(), indexerFromServiceResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerWithResponse#String-Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listIndexers()}
     */
    public void listIndexers() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexers
        PagedIterable<SearchIndexer> indexers = SEARCH_INDEXER_CLIENT.listIndexers();
        for (SearchIndexer indexer: indexers) {
            System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexer.getName(),
                indexer.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexers
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listIndexers(Context)}
     */
    public void listIndexersWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context
        PagedIterable<SearchIndexer> indexers = SEARCH_INDEXER_CLIENT.listIndexers(new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is"
            + indexers.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndexer indexer: indexers) {
            System.out.printf("The indexer name is %s. The ETag of index is %s.%n",
                indexer.getName(), indexer.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listIndexersWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listIndexerNames()}
     */
    public void listIndexerNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listIndexerNames
        PagedIterable<String> indexers = SEARCH_INDEXER_CLIENT.listIndexerNames();
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
        PagedIterable<String> indexers = SEARCH_INDEXER_CLIENT.listIndexerNames(new Context(KEY_1, VALUE_1));
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
        SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        searchIndexerFromService.setFieldMappings(Collections.singletonList(
            new FieldMapping("hotelName").setTargetFieldName("HotelName")));
        SearchIndexer updateIndexer = SEARCH_INDEXER_CLIENT.createOrUpdateIndexer(searchIndexerFromService);
        System.out.printf("The indexer name is %s. The target field name of indexer is %s.%n",
            updateIndexer.getName(), updateIndexer.getFieldMappings().get(0).getTargetFieldName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexer#SearchIndexer
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateIndexerWithResponse(SearchIndexer, boolean, Context)}
     */
    public void createOrUpdateIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean-Context
        SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        searchIndexerFromService.setFieldMappings(Collections.singletonList(
            new FieldMapping("hotelName").setTargetFieldName("HotelName")));
        Response<SearchIndexer> indexerFromService = SEARCH_INDEXER_CLIENT.createOrUpdateIndexerWithResponse(
            searchIndexerFromService, true, new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThe indexer name is %s. "
            + "The target field name of indexer is %s.%n", indexerFromService.getStatusCode(),
            indexerFromService.getValue().getName(),
            indexerFromService.getValue().getFieldMappings().get(0).getTargetFieldName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateIndexerWithResponse(CreateOrUpdateIndexerOptions, Context)}
     */
    public void createOrUpdateIndexerWithResponse2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions-Context
        SearchIndexer searchIndexerFromService = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        searchIndexerFromService.setFieldMappings(Collections.singletonList(
            new FieldMapping("hotelName").setTargetFieldName("HotelName")));
        CreateOrUpdateIndexerOptions options = new CreateOrUpdateIndexerOptions(searchIndexerFromService)
            .setOnlyIfUnchanged(true)
            .setCacheReprocessingChangeDetectionDisabled(false)
            .setCacheResetRequirementsIgnored(true);
        Response<SearchIndexer> indexerFromService = SEARCH_INDEXER_CLIENT.createOrUpdateIndexerWithResponse(
            options, new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThe indexer name is %s. "
                + "The target field name of indexer is %s.%n", indexerFromService.getStatusCode(),
            indexerFromService.getValue().getName(),
            indexerFromService.getValue().getFieldMappings().get(0).getTargetFieldName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions-Context
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
     * Code snippet for {@link SearchIndexerClient#deleteIndexerWithResponse(SearchIndexer, boolean, Context)}
     */
    public void deleteSearchIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#SearchIndexer-boolean-Context
        SearchIndexer searchIndexer = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");
        Response<Void> deleteResponse = SEARCH_INDEXER_CLIENT.deleteIndexerWithResponse(searchIndexer, true,
            new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteIndexerWithResponse#SearchIndexer-boolean-Context
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
     * Code snippet for {@link SearchIndexerClient#resetIndexerWithResponse(String, Context)}
     */
    public void resetIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-Context
        Response<Void> response = SEARCH_INDEXER_CLIENT.resetIndexerWithResponse("searchIndexer",
            new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetIndexerWithResponse#String-Context
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
     * Code snippet for {@link SearchIndexerClient#runIndexerWithResponse(String, Context)}
     */
    public void runIndexerWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-Context
        Response<Void> response = SEARCH_INDEXER_CLIENT.runIndexerWithResponse("searchIndexer",
            new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is " + response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.runIndexerWithResponse#String-Context
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
     * Code snippet for {@link SearchIndexerClient#getIndexerStatusWithResponse(String, Context)}
     */
    public void getIndexerStatusWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-Context
        Response<SearchIndexerStatus> response = SEARCH_INDEXER_CLIENT.getIndexerStatusWithResponse("searchIndexer",
            new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThe indexer status is %s.%n",
            response.getStatusCode(), response.getValue().getStatus());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getIndexerStatusWithResponse#String-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetDocuments(String, Boolean, List, List)}
     */
    public void resetDocuments() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetDocuments#String-Boolean-List-List
        // Reset the documents with keys 1234 and 4321.
        SEARCH_INDEXER_CLIENT.resetDocuments("searchIndexer", false, Arrays.asList("1234", "4321"), null);

        // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
        SEARCH_INDEXER_CLIENT.resetDocuments("searchIndexer", true, Arrays.asList("1235", "5321"), null);
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetDocuments#String-Boolean-List-List
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetDocumentsWithResponse(SearchIndexer, Boolean, List, List, Context)}
     */
    public void resetDocumentsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List-Context
        SearchIndexer searchIndexer = SEARCH_INDEXER_CLIENT.getIndexer("searchIndexer");

        // Reset the documents with keys 1234 and 4321.
        Response<Void> resetDocsResult = SEARCH_INDEXER_CLIENT.resetDocumentsWithResponse(searchIndexer, false,
            Arrays.asList("1234", "4321"), null, new Context(KEY_1, VALUE_1));
        System.out.printf("Requesting documents to be reset completed with status code %d.%n",
            resetDocsResult.getStatusCode());

        // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
        resetDocsResult = SEARCH_INDEXER_CLIENT.resetDocumentsWithResponse(searchIndexer, true,
            Arrays.asList("1235", "5321"), null, new Context(KEY_1, VALUE_1));
        System.out.printf("Overwriting the documents to be reset completed with status code %d.%n",
            resetDocsResult.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List-Context
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
            SEARCH_INDEXER_CLIENT.createDataSourceConnection(dataSource);
        System.out.printf("The data source name is %s. The ETag of data source is %s.%n",
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
            SEARCH_INDEXER_CLIENT.createDataSourceConnectionWithResponse(dataSource, new Context(KEY_1, VALUE_1));

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
            SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n", dataSource.getName(),
            dataSource.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnection#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getDataSourceConnectionWithResponse(String, Context)}
     */
    public void getDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-Context
        Response<SearchIndexerDataSourceConnection> dataSource =
            SEARCH_INDEXER_CLIENT.getDataSourceConnectionWithResponse(
                "dataSource", new Context(KEY_1, VALUE_1));

        System.out.printf("The status code of the response is %s. The data source name is %s.%n",
            dataSource.getStatusCode(), dataSource.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getDataSourceConnectionWithResponse#String-Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnections()}
     */
    public void listDataSources() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnections
        PagedIterable<SearchIndexerDataSourceConnection> dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnections();
        for (SearchIndexerDataSourceConnection dataSource: dataSources) {
            System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n", dataSource.getName(),
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
            SEARCH_INDEXER_CLIENT.listDataSourceConnections(new Context(KEY_1, VALUE_1));

        System.out.println("The status code of the response is"
            + dataSources.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndexerDataSourceConnection dataSource: dataSources) {
            System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n",
                dataSource.getName(), dataSource.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionsWithResponse#Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listDataSourceConnectionNames()}
     */
    public void listDataSourceNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listDataSourceConnectionNames
        PagedIterable<String> dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnectionNames();
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
        PagedIterable<String> dataSources = SEARCH_INDEXER_CLIENT.listDataSourceConnectionNames(new Context(KEY_1, VALUE_1));
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
        SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));

        SearchIndexerDataSourceConnection updateDataSource = SEARCH_INDEXER_CLIENT.createOrUpdateDataSourceConnection(dataSource);
        System.out.printf("The dataSource name is %s. The container name of dataSource is %s.%n",
            updateDataSource.getName(), updateDataSource.getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, boolean, Context)}
     */
    public void createOrUpdateDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context
        SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));

        Response<SearchIndexerDataSourceConnection> updateDataSource = SEARCH_INDEXER_CLIENT
            .createOrUpdateDataSourceConnectionWithResponse(dataSource, true, new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThe dataSource name is %s. "
            + "The container name of dataSource is %s.%n", updateDataSource.getStatusCode(),
            updateDataSource.getValue().getName(), updateDataSource.getValue().getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateDataSourceConnectionWithResponse(CreateOrUpdateDataSourceConnectionOptions, Context)}
     */
    public void createOrUpdateDataSourceWithResponse2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions-Context
        SearchIndexerDataSourceConnection dataSource = SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));
        CreateOrUpdateDataSourceConnectionOptions options = new CreateOrUpdateDataSourceConnectionOptions(dataSource)
            .setOnlyIfUnchanged(true)
            .setCacheResetRequirementsIgnored(true);

        Response<SearchIndexerDataSourceConnection> updateDataSource = SEARCH_INDEXER_CLIENT
            .createOrUpdateDataSourceConnectionWithResponse(options, new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThe dataSource name is %s. "
                + "The container name of dataSource is %s.%n", updateDataSource.getStatusCode(),
            updateDataSource.getValue().getName(), updateDataSource.getValue().getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions-Context
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
     * Code snippet for {@link SearchIndexerClient#deleteDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, boolean, Context)}
     */
    public void deleteDataSourceWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean-Context
        SearchIndexerDataSourceConnection dataSource =
            SEARCH_INDEXER_CLIENT.getDataSourceConnection("dataSource");
        Response<Void> deleteResponse = SEARCH_INDEXER_CLIENT.deleteDataSourceConnectionWithResponse(dataSource, true,
            new Context(KEY_1, VALUE_1));
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
        SearchIndexerSkillset skillset = SEARCH_INDEXER_CLIENT.createSkillset(searchIndexerSkillset);
        System.out.printf("The indexer skillset name is %s. The ETag of indexer skillset is %s.%n",
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
            SEARCH_INDEXER_CLIENT.createSkillsetWithResponse(searchIndexerSkillset, new Context(KEY_1, VALUE_1));
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
            SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        System.out.printf("The indexer skillset name is %s. The ETag of indexer skillset is %s.%n",
            indexerSkillset.getName(), indexerSkillset.getETag());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getSearchIndexerSkillset#String
    }

    /**
     * Code snippet for {@link SearchIndexerClient#getSkillsetWithResponse(String, Context)}
     */
    public void getSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-Context
        Response<SearchIndexerSkillset> skillsetWithResponse = SEARCH_INDEXER_CLIENT.getSkillsetWithResponse(
            "searchIndexerSkillset", new Context(KEY_1, VALUE_1));

        System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
            skillsetWithResponse.getStatusCode(), skillsetWithResponse.getValue().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.getSkillsetWithResponse#String-Context
    }


    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsets()}
     */
    public void listIndexerSkillset() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets
        PagedIterable<SearchIndexerSkillset> indexerSkillsets = SEARCH_INDEXER_CLIENT.listSkillsets();
        for (SearchIndexerSkillset skillset: indexerSkillsets) {
            System.out.printf("The skillset name is %s. The ETag of skillset is %s.%n", skillset.getName(),
                skillset.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsets
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsets(Context)}
     */
    public void listIndexerSkillsetsWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context
        PagedIterable<SearchIndexerSkillset> indexerSkillsets = SEARCH_INDEXER_CLIENT.listSkillsets(new Context(KEY_1, VALUE_1));
        System.out.println("The status code of the response is"
            + indexerSkillsets.iterableByPage().iterator().next().getStatusCode());
        for (SearchIndexerSkillset skillset: indexerSkillsets) {
            System.out.printf("The skillset name is %s. The ETag of skillset is %s.%n",
                skillset.getName(), skillset.getETag());
        }
        // END: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetsWithContext#Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#listSkillsetNames()}
     */
    public void listIndexerSkillsetNames() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.listSkillsetNames
        PagedIterable<String> skillsetNames = SEARCH_INDEXER_CLIENT.listSkillsetNames();
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
        PagedIterable<String> skillsetNames = SEARCH_INDEXER_CLIENT.listSkillsetNames(new Context(KEY_1, VALUE_1));
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
        SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        indexerSkillset.setDescription("This is new description!");
        SearchIndexerSkillset updateSkillset = SEARCH_INDEXER_CLIENT.createOrUpdateSkillset(indexerSkillset);
        System.out.printf("The indexer skillset name is %s. The description of indexer skillset is %s.%n",
            updateSkillset.getName(), updateSkillset.getDescription());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateIndexerSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateSkillsetWithResponse(SearchIndexerSkillset, boolean, Context)}
     */
    public void createOrUpdateIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
        SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        indexerSkillset.setDescription("This is new description!");
        Response<SearchIndexerSkillset> updateSkillsetResponse = SEARCH_INDEXER_CLIENT.createOrUpdateSkillsetWithResponse(
            indexerSkillset, true, new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThe indexer skillset name is %s. "
                + "The description of indexer skillset is %s.%n", updateSkillsetResponse.getStatusCode(),
            updateSkillsetResponse.getValue().getName(),
            updateSkillsetResponse.getValue().getDescription());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#createOrUpdateSkillsetWithResponse(CreateOrUpdateSkillsetOptions, Context)}
     */
    public void createOrUpdateIndexerSkillsetWithResponse2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions-Context
        SearchIndexerSkillset indexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        indexerSkillset.setDescription("This is new description!");
        CreateOrUpdateSkillsetOptions options = new CreateOrUpdateSkillsetOptions(indexerSkillset)
            .setOnlyIfUnchanged(true)
            .setCacheReprocessingChangeDetectionDisabled(false)
            .setCacheResetRequirementsIgnored(true);
        Response<SearchIndexerSkillset> updateSkillsetResponse = SEARCH_INDEXER_CLIENT.createOrUpdateSkillsetWithResponse(
            options, new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %s.%nThe indexer skillset name is %s. "
                + "The description of indexer skillset is %s.%n", updateSkillsetResponse.getStatusCode(),
            updateSkillsetResponse.getValue().getName(),
            updateSkillsetResponse.getValue().getDescription());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions-Context
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
     * Code snippet for {@link SearchIndexerClient#deleteSkillsetWithResponse(SearchIndexerSkillset, boolean, Context)}
     */
    public void deleteSearchIndexerSkillsetWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
        SearchIndexerSkillset searchIndexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");
        Response<Void> deleteResponse = SEARCH_INDEXER_CLIENT.deleteSkillsetWithResponse(searchIndexerSkillset, true,
            new Context(KEY_1, VALUE_1));
        System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean-Context
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetSkills(String, List)}
     */
    public void resetSkills() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetSkills#String-List
        // Reset the "myOcr" and "myText" skills.
        SEARCH_INDEXER_CLIENT.resetSkills("searchIndexerSkillset", Arrays.asList("myOcr", "myText"));
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetSkills#String-List
    }

    /**
     * Code snippet for {@link SearchIndexerClient#resetSkillsWithResponse(SearchIndexerSkillset, List, Context)}
     */
    public void resetSkillsWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerClient.resetSkillsWithResponse#SearchIndexerSkillset-List-Context
        SearchIndexerSkillset searchIndexerSkillset = SEARCH_INDEXER_CLIENT.getSkillset("searchIndexerSkillset");

        // Reset the "myOcr" and "myText" skills.
        Response<Void> resetSkillsResponse = SEARCH_INDEXER_CLIENT.resetSkillsWithResponse(searchIndexerSkillset,
            Arrays.asList("myOcr", "myText"), new Context(KEY_1, VALUE_1));
        System.out.printf("Resetting skills completed with status code %d.%n", resetSkillsResponse.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexerClient.resetSkillsWithResponse#SearchIndexerSkillset-List-Context
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
     * Code snippet for {@link SearchIndexerAsyncClient#createIndexerWithResponse(SearchIndexer)}.
     */
    public void createSearchIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexerWithResponse#SearchIndexer
        SearchIndexer searchIndexer = new SearchIndexer("searchIndexer", "dataSource",
            "searchIndex");
        SEARCH_INDEXER_ASYNC_CLIENT.createIndexerWithResponse(searchIndexer)
            .subscribe(indexerFromServiceResponse ->
                System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
                    indexerFromServiceResponse.getStatusCode(), indexerFromServiceResponse.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createIndexerWithResponse#SearchIndexer
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
     * Code snippet for {@link SearchIndexerAsyncClient#getIndexerWithResponse(String)}}
     */
    public void getSearchIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerWithResponse#String
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexerWithResponse("searchIndexer")
            .subscribe(indexerFromServiceResponse ->
                System.out.printf("The status code of the response is %s. The indexer name is %s.%n",
                indexerFromServiceResponse.getStatusCode(), indexerFromServiceResponse.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerWithResponse#String
    }


    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listIndexers()}
     */
    public void listIndexersAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listIndexers
        SEARCH_INDEXER_ASYNC_CLIENT.listIndexers()
            .subscribe(indexer ->
                System.out.printf("The indexer name is %s. The ETag of indexer is %s.%n", indexer.getName(),
                indexer.getETag()));
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
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateIndexerWithResponse(SearchIndexer, boolean)}
     */
    public void createOrUpdateIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexerFromService -> {
                searchIndexerFromService.setFieldMappings(Collections.singletonList(
                    new FieldMapping("hotelName").setTargetFieldName("HotelName")));
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateIndexerWithResponse(searchIndexerFromService, true);
            })
            .subscribe(indexerFromService ->
                System.out.printf("The status code of the response is %s.%nThe indexer name is %s. "
                    + "The target field name of indexer is %s.%n", indexerFromService.getStatusCode(),
                indexerFromService.getValue().getName(),
                indexerFromService.getValue().getFieldMappings().get(0).getTargetFieldName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#SearchIndexer-boolean
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateIndexerWithResponse(CreateOrUpdateIndexerOptions)}
     */
    public void createOrUpdateIndexerWithResponseAsync2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexerFromService -> {
                searchIndexerFromService.setFieldMappings(Collections.singletonList(
                    new FieldMapping("hotelName").setTargetFieldName("HotelName")));
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateIndexerWithResponse(
                    new CreateOrUpdateIndexerOptions(searchIndexerFromService)
                        .setOnlyIfUnchanged(true)
                        .setCacheReprocessingChangeDetectionDisabled(false)
                        .setCacheResetRequirementsIgnored(true));
            })
            .subscribe(indexerFromService ->
                System.out.printf("The status code of the response is %s.%nThe indexer name is %s. "
                        + "The target field name of indexer is %s.%n", indexerFromService.getStatusCode(),
                    indexerFromService.getValue().getName(),
                    indexerFromService.getValue().getFieldMappings().get(0).getTargetFieldName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateIndexerWithResponse#CreateOrUpdateIndexerOptions
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
     * Code snippet for {@link SearchIndexerAsyncClient#deleteIndexerWithResponse(SearchIndexer, boolean)}
     */
    public void deleteSearchIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexerWithResponse#SearchIndexer-boolean
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexer ->
                SEARCH_INDEXER_ASYNC_CLIENT.deleteIndexerWithResponse(searchIndexer, true))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteIndexerWithResponse#SearchIndexer-boolean
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
     * Code snippet for {@link SearchIndexerAsyncClient#resetIndexerWithResponse(String)}
     */
    public void resetIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexerWithResponse#String
        SEARCH_INDEXER_ASYNC_CLIENT.resetIndexerWithResponse("searchIndexer")
            .subscribe(response ->
                System.out.println("The status code of the response is " + response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetIndexerWithResponse#String
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
     * Code snippet for {@link SearchIndexerAsyncClient#runIndexerWithResponse(String)}
     */
    public void runIndexerWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexerWithResponse#String
        SEARCH_INDEXER_ASYNC_CLIENT.runIndexerWithResponse("searchIndexer")
            .subscribe(response ->
                System.out.println("The status code of the response is " + response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.runIndexerWithResponse#String
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
     * Code snippet for {@link SearchIndexerAsyncClient#getIndexerStatusWithResponse(String)}
     */
    public void getIndexerStatusWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatusWithResponse#String
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexerStatusWithResponse("searchIndexer")
            .subscribe(response ->
                System.out.printf("The status code of the response is %s.%nThe indexer status is %s.%n",
                response.getStatusCode(), response.getValue().getStatus()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getIndexerStatusWithResponse#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetDocuments(String, Boolean, List, List)}
     */
    public void resetDocumentsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocuments#String-Boolean-List-List
        // Reset the documents with keys 1234 and 4321.
        SEARCH_INDEXER_ASYNC_CLIENT.resetDocuments("searchIndexer", false, Arrays.asList("1234", "4321"), null)
            // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
            .then(SEARCH_INDEXER_ASYNC_CLIENT.resetDocuments("searchIndexer", true, Arrays.asList("1235", "5321"), null))
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocuments#String-Boolean-List-List
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetDocumentsWithResponse(SearchIndexer, Boolean, List, List)}
     */
    public void resetDocumentsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List
        SEARCH_INDEXER_ASYNC_CLIENT.getIndexer("searchIndexer")
            .flatMap(searchIndexer -> SEARCH_INDEXER_ASYNC_CLIENT.resetDocumentsWithResponse(searchIndexer, false,
                Arrays.asList("1234", "4321"), null)
                .flatMap(resetDocsResult -> {
                    System.out.printf("Requesting documents to be reset completed with status code %d.%n",
                        resetDocsResult.getStatusCode());

                    // Clear the previous documents to be reset and replace them with documents 1235 and 5231.
                    return SEARCH_INDEXER_ASYNC_CLIENT.resetDocumentsWithResponse(searchIndexer, true,
                        Arrays.asList("1235", "5321"), null);
                }))
            .subscribe(resetDocsResult ->
                System.out.printf("Overwriting the documents to be reset completed with status code %d.%n",
                    resetDocsResult.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetDocumentsWithResponse#SearchIndexer-Boolean-List-List
    }

    /**
     * Code snippet for creating {@link SearchIndexerAsyncClient#createDataSourceConnection(SearchIndexerDataSourceConnection)}.
     */
    public void createDataSourceAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnection#SearchIndexerDataSourceConnection
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            com.azure.search.documents.indexes.models.SearchIndexerDataSourceType.AZURE_BLOB, "{connectionString}",
            new com.azure.search.documents.indexes.models.SearchIndexerDataContainer("container"));
        SEARCH_INDEXER_ASYNC_CLIENT.createDataSourceConnection(dataSource)
            .subscribe(dataSourceFromService ->
                System.out.printf("The data source name is %s. The ETag of data source is %s.%n",
                    dataSourceFromService.getName(), dataSourceFromService.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection)}.
     */
    public void createDataSourceWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection
        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("dataSource",
            SearchIndexerDataSourceType.AZURE_BLOB, "{connectionString}",
            new SearchIndexerDataContainer("container"));
        SEARCH_INDEXER_ASYNC_CLIENT.createDataSourceConnectionWithResponse(dataSource)
            .subscribe(dataSourceFromService ->
                System.out.printf("The status code of the response is %s. The data source name is %s.%n",
                dataSourceFromService.getStatusCode(), dataSourceFromService.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection
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
     * Code snippet for {@link SearchIndexerAsyncClient#getDataSourceConnectionWithResponse(String)}
     */
    public void getDataSourceWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnectionWithResponse#String
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnectionWithResponse("dataSource")
            .subscribe(dataSource ->
                System.out.printf("The status code of the response is %s. The data source name is %s.%n",
                dataSource.getStatusCode(), dataSource.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getDataSourceConnectionWithResponse#String
    }


    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listDataSourceConnections()}
     */
    public void listDataSourcesAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listDataSourceConnections
        SEARCH_INDEXER_ASYNC_CLIENT.listDataSourceConnections()
            .subscribe(dataSource ->
                System.out.printf("The dataSource name is %s. The ETag of dataSource is %s.%n",
                    dataSource.getName(), dataSource.getETag())
            );
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
        dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));

        SearchIndexerDataSourceConnection updateDataSource = SEARCH_INDEXER_CLIENT.createOrUpdateDataSourceConnection(dataSource);
        System.out.printf("The dataSource name is %s. The container name of dataSource is %s.%n",
            updateDataSource.getName(), updateDataSource.getContainer().getName());
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnection#SearchIndexerDataSourceConnection
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, boolean)}
     */
    public void createOrUpdateDataSourceWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection("dataSource")
            .flatMap(dataSource -> {
                dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateDataSourceConnectionWithResponse(dataSource, true);
            })
            .subscribe(updateDataSource ->
                System.out.printf("The status code of the response is %s.%nThe dataSource name is %s. "
                    + "The container name of dataSource is %s.%n", updateDataSource.getStatusCode(),
                updateDataSource.getValue().getName(), updateDataSource.getValue().getContainer().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateDataSourceConnectionWithResponse(CreateOrUpdateDataSourceConnectionOptions)}
     */
    public void createOrUpdateDataSourceWithResponseAsync2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection("dataSource")
            .flatMap(dataSource -> {
                dataSource.setContainer(new SearchIndexerDataContainer("updatecontainer"));
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateDataSourceConnectionWithResponse(
                    new CreateOrUpdateDataSourceConnectionOptions(dataSource)
                        .setOnlyIfUnchanged(true)
                        .setCacheResetRequirementsIgnored(true));
            })
            .subscribe(updateDataSource ->
                System.out.printf("The status code of the response is %s.%nThe dataSource name is %s. "
                        + "The container name of dataSource is %s.%n", updateDataSource.getStatusCode(),
                    updateDataSource.getValue().getName(), updateDataSource.getValue().getContainer().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse#CreateOrUpdateDataSourceConnectionOptions
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
     * Code snippet for {@link SearchIndexerAsyncClient#deleteDataSourceConnectionWithResponse(SearchIndexerDataSourceConnection, boolean)}
     */
    public void deleteDataSourceWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean
        SEARCH_INDEXER_ASYNC_CLIENT.getDataSourceConnection("dataSource")
            .flatMap(dataSource -> SEARCH_INDEXER_ASYNC_CLIENT.deleteDataSourceConnectionWithResponse(dataSource, true))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteDataSourceConnectionWithResponse#SearchIndexerDataSourceConnection-boolean
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createSkillset(SearchIndexerSkillset)}
     */
    public void createSearchIndexerSkillsetAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillset#SearchIndexerSkillset
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
        SEARCH_INDEXER_ASYNC_CLIENT.createSkillset(searchIndexerSkillset)
            .subscribe(skillset ->
                System.out.printf("The indexer skillset name is %s. The ETag of indexer skillset is %s.%n",
                skillset.getName(), skillset.getETag()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillset#SearchIndexerSkillset
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createSkillsetWithResponse(SearchIndexerSkillset)}.
     */
    public void createSearchIndexerSkillsetWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillsetWithResponse#SearchIndexerSkillset
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
        SEARCH_INDEXER_ASYNC_CLIENT.createSkillsetWithResponse(searchIndexerSkillset)
            .subscribe(skillsetWithResponse ->
                System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
                skillsetWithResponse.getStatusCode(), skillsetWithResponse.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createSkillsetWithResponse#SearchIndexerSkillset
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
     * Code snippet for {@link SearchIndexerAsyncClient#getSkillsetWithResponse(String)}
     */
    public void getSearchIndexerSkillsetWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSkillsetWithResponse#String
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillsetWithResponse("searchIndexerSkillset")
            .subscribe(skillsetWithResponse ->
                System.out.printf("The status code of the response is %s. The indexer skillset name is %s.%n",
                skillsetWithResponse.getStatusCode(), skillsetWithResponse.getValue().getName()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.getSkillsetWithResponse#String
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#listSkillsets()}
     */
    public void listIndexerSkillsetAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.listSkillsets
        SEARCH_INDEXER_ASYNC_CLIENT.listSkillsets()
            .subscribe(skillset ->
                System.out.printf("The skillset name is %s. The ETag of skillset is %s.%n", skillset.getName(),
                skillset.getETag()));
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
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateSkillsetWithResponse(SearchIndexerSkillset, boolean)}
     */
    public void createOrUpdateIndexerSkillsetWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(indexerSkillset -> {
                indexerSkillset.setDescription("This is new description!");
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateSkillsetWithResponse(indexerSkillset, true);
            })
            .subscribe(updateSkillsetResponse ->
                System.out.printf("The status code of the response is %s.%nThe indexer skillset name is %s. "
                    + "The description of indexer skillset is %s.%n", updateSkillsetResponse.getStatusCode(),
                updateSkillsetResponse.getValue().getName(),
                updateSkillsetResponse.getValue().getDescription()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#SearchIndexerSkillset-boolean
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#createOrUpdateSkillsetWithResponse(CreateOrUpdateSkillsetOptions)}
     */
    public void createOrUpdateIndexerSkillsetWithResponseAsync2() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(indexerSkillset -> {
                indexerSkillset.setDescription("This is new description!");
                return SEARCH_INDEXER_ASYNC_CLIENT.createOrUpdateSkillsetWithResponse(
                    new CreateOrUpdateSkillsetOptions(indexerSkillset)
                        .setOnlyIfUnchanged(true)
                        .setCacheReprocessingChangeDetectionDisabled(false)
                        .setCacheResetRequirementsIgnored(true));
            })
            .subscribe(updateSkillsetResponse ->
                System.out.printf("The status code of the response is %s.%nThe indexer skillset name is %s. "
                    + "The description of indexer skillset is %s.%n", updateSkillsetResponse.getStatusCode(),
                    updateSkillsetResponse.getValue().getName(),
                    updateSkillsetResponse.getValue().getDescription()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.createOrUpdateSkillsetWithResponse#CreateOrUpdateSkillsetOptions
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
     * Code snippet for {@link SearchIndexerAsyncClient#deleteSkillsetWithResponse(SearchIndexerSkillset, boolean)}
     */
    public void deleteSearchIndexerSkillsetWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(searchIndexerSkillset ->
                SEARCH_INDEXER_ASYNC_CLIENT.deleteSkillsetWithResponse(searchIndexerSkillset, true))
            .subscribe(deleteResponse ->
                System.out.printf("The status code of the response is %d.%n", deleteResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.deleteSkillsetWithResponse#SearchIndexerSkillset-boolean
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetSkills(String, List)}
     */
    public void resetSkillsAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkills#String-List
        // Reset the "myOcr" and "myText" skills.
        SEARCH_INDEXER_ASYNC_CLIENT.resetSkills("searchIndexerSkillset", Arrays.asList("myOcr", "myText"))
            .subscribe();
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkills#String-List
    }

    /**
     * Code snippet for {@link SearchIndexerAsyncClient#resetSkillsWithResponse(SearchIndexerSkillset, List)}
     */
    public void resetSkillsWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkillsWithResponse#SearchIndexerSkillset-List
        SEARCH_INDEXER_ASYNC_CLIENT.getSkillset("searchIndexerSkillset")
            .flatMap(searchIndexerSkillset -> SEARCH_INDEXER_ASYNC_CLIENT.resetSkillsWithResponse(searchIndexerSkillset,
                Arrays.asList("myOcr", "myText")))
            .subscribe(resetSkillsResponse -> System.out.printf("Resetting skills completed with status code %d.%n",
                resetSkillsResponse.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexerAsyncClient.resetSkillsWithResponse#SearchIndexerSkillset-List
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createAlias(SearchAlias)}.
     */
    public void createAliasAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAlias#SearchAlias
        SEARCH_INDEX_ASYNC_CLIENT.createAlias(new SearchAlias("my-alias", Collections.singletonList("index-to-alias")))
            .subscribe(searchAlias -> System.out.printf("Created alias '%s' that aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createAliasWithResponse(SearchAlias)}.
     */
    public void createAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAliasWithResponse#SearchAlias
        SEARCH_INDEX_ASYNC_CLIENT.createAliasWithResponse(new SearchAlias("my-alias",
            Collections.singletonList("index-to-alias")))
            .subscribe(response ->
                System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createAliasWithResponse#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexClient#createAlias(SearchAlias)}.
     */
    public void createAlias() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createAlias#SearchAlias
        SearchAlias searchAlias = SEARCH_INDEX_CLIENT.createAlias(new SearchAlias("my-alias",
            Collections.singletonList("index-to-alias")));
        System.out.printf("Created alias '%s' that aliases index '%s'.", searchAlias.getName(),
            searchAlias.getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexClient#createAliasWithResponse(SearchAlias, Context)}.
     */
    public void createAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createAliasWithResponse#SearchAlias-Context
        Response<SearchAlias> response = SEARCH_INDEX_CLIENT.createAliasWithResponse(new SearchAlias("my-alias",
                Collections.singletonList("index-to-alias")), new Context(KEY_1, VALUE_1));

        System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createAliasWithResponse#SearchAlias-Context

    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateAlias(SearchAlias)}.
     */
    public void createOrUpdateAliasAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAlias#SearchAlias
        SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAlias(
            new SearchAlias("my-alias", Collections.singletonList("index-to-alias")))
            .flatMap(searchAlias -> {
                System.out.printf("Created alias '%s' that aliases index '%s'.", searchAlias.getName(),
                    searchAlias.getIndexes().get(0));

                return SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAlias(new SearchAlias(searchAlias.getName(),
                    Collections.singletonList("new-index-to-alias")));
            }).subscribe(searchAlias -> System.out.printf("Updated alias '%s' to aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexAsyncClient#createOrUpdateAliasWithResponse(SearchAlias, boolean)}.
     */
    public void createOrUpdateAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAliasWithResponse#SearchAlias-boolean
        SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAliasWithResponse(
                new SearchAlias("my-alias", Collections.singletonList("index-to-alias")), false)
            .flatMap(response -> {
                System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));

                return SEARCH_INDEX_ASYNC_CLIENT.createOrUpdateAliasWithResponse(
                    new SearchAlias(response.getValue().getName(), Collections.singletonList("new-index-to-alias"))
                    .setETag(response.getValue().getETag()), true);
            }).subscribe(response ->
                System.out.printf("Response status code %d. Updated alias '%s' that aliases index '%s'.",
                response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.createOrUpdateAliasWithResponse#SearchAlias-boolean
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateAlias(SearchAlias)}.
     */
    public void createOrUpdateAlias() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAlias#SearchAlias
        SearchAlias searchAlias = SEARCH_INDEX_CLIENT.createOrUpdateAlias(
            new SearchAlias("my-alias", Collections.singletonList("index-to-alias")));

        System.out.printf("Created alias '%s' that aliases index '%s'.", searchAlias.getName(),
            searchAlias.getIndexes().get(0));

        searchAlias = SEARCH_INDEX_CLIENT.createOrUpdateAlias(new SearchAlias(searchAlias.getName(),
            Collections.singletonList("new-index-to-alias")));

        System.out.printf("Updated alias '%s' to aliases index '%s'.", searchAlias.getName(),
            searchAlias.getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAlias#SearchAlias
    }

    /**
     * Code snippet for {@link SearchIndexClient#createOrUpdateAliasWithResponse(SearchAlias, boolean, Context)}.
     */
    public void createOrUpdateAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAliasWithResponse#SearchAlias-boolean-Context
        Response<SearchAlias> response = SEARCH_INDEX_CLIENT.createOrUpdateAliasWithResponse(
            new SearchAlias("my-alias", Collections.singletonList("index-to-alias")), false, new Context(KEY_1, VALUE_1));

        System.out.printf("Response status code %d. Created alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));

        response = SEARCH_INDEX_CLIENT.createOrUpdateAliasWithResponse(
            new SearchAlias(response.getValue().getName(), Collections.singletonList("new-index-to-alias"))
                .setETag(response.getValue().getETag()), true, new Context(KEY_1, VALUE_1));

        System.out.printf("Response status code %d. Updated alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.createOrUpdateAliasWithResponse#SearchAlias-boolean-Context
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
     * Code snippet for {@link SearchIndexAsyncClient#getAliasWithResponse(String)}.
     */
    public void getAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.getAliasWithResponse#String
        SEARCH_INDEX_ASYNC_CLIENT.getAliasWithResponse("my-alias")
            .subscribe(response ->
                System.out.printf("Response status code %d. Retrieved alias '%s' that aliases index '%s'.",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.getAliasWithResponse#String
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
     * Code snippet for {@link SearchIndexClient#getAliasWithResponse(String, Context)}.
     */
    public void getAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.getAliasWithResponse#String-Context
        Response<SearchAlias> response = SEARCH_INDEX_CLIENT.getAliasWithResponse("my-alias", new Context(KEY_1, VALUE_1));

        System.out.printf("Response status code %d. Retrieved alias '%s' that aliases index '%s'.",
            response.getStatusCode(), response.getValue().getName(), response.getValue().getIndexes().get(0));
        // END: com.azure.search.documents.indexes.SearchIndexClient.getAliasWithResponse#String-Context
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
     * Code snippet for {@link SearchIndexAsyncClient#deleteAliasWithResponse(SearchAlias, boolean)}.
     */
    public void deleteAliasWithResponseAsync() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteAliasWithResponse#SearchAlias-boolean
        SEARCH_INDEX_ASYNC_CLIENT.getAlias("my-alias")
            .flatMap(searchAlias -> SEARCH_INDEX_ASYNC_CLIENT.deleteAliasWithResponse(searchAlias, true))
            .subscribe(response -> System.out.printf("Response status code %d. Deleted alias 'my-alias'.",
                response.getStatusCode()));
        // END: com.azure.search.documents.indexes.SearchIndexAsyncClient.deleteAliasWithResponse#SearchAlias-boolean
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
     * Code snippet for {@link SearchIndexClient#deleteAliasWithResponse(SearchAlias, boolean, Context)}.
     */
    public void deleteAliasWithResponse() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.deleteAliasWithResponse#SearchAlias-boolean-Context
        SearchAlias searchAlias = SEARCH_INDEX_CLIENT.getAlias("my-alias");

        Response<Void> response = SEARCH_INDEX_CLIENT.deleteAliasWithResponse(searchAlias, true,
            new Context(KEY_1, VALUE_1));

        System.out.printf("Response status code %d. Deleted alias 'my-alias'.", response.getStatusCode());
        // END: com.azure.search.documents.indexes.SearchIndexClient.deleteAliasWithResponse#SearchAlias-boolean-Context
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
     * Code snippet for {@link SearchIndexClient#listAliases(Context)}.
     */
    public void listAliasesWithContext() {
        // BEGIN: com.azure.search.documents.indexes.SearchIndexClient.listAliases#Context
        SEARCH_INDEX_CLIENT.listAliases(new Context(KEY_1, VALUE_1))
            .forEach(searchAlias -> System.out.printf("Listed alias '%s' that aliases index '%s'.",
                searchAlias.getName(), searchAlias.getIndexes().get(0)));
        // END: com.azure.search.documents.indexes.SearchIndexClient.listAliases#Context
    }
}
