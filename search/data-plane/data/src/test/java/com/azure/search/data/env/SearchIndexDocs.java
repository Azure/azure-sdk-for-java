package com.azure.search.data.env;

import com.azure.search.data.SearchIndexASyncClient;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.customization.SearchIndexClientBuilderImpl;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchIndexDocs {

    private static final String HOTELS_DATA_JSON = "HotelsDataArray.json";

    private String searchServiceName;
    private String apiAdminKey;
    private String indexName;
    private String dnsSuffix;
    private String apiVersion;

    private SearchIndexASyncClient searchIndexASyncClient;

    /**
     * Creates an instance of SearchIndexASyncClient to be used in uploading documents to a certain index,
     * to be used in tests.
     *
     * @param searchServiceName The name of the Search service
     * @param apiAdminKey The Admin key of the Search service
     * @param indexName The name of the index
     * @param dnsSuffix DNS suffix of the Search service
     * @param apiVersion used API version
     */
    public SearchIndexDocs(
        String searchServiceName, String apiAdminKey, String indexName,
        String dnsSuffix, String apiVersion) {
        this.searchServiceName = searchServiceName;
        this.apiAdminKey = apiAdminKey;
        this.indexName = indexName;
        this.dnsSuffix = dnsSuffix;
        this.apiVersion = apiVersion;
    }

    /**
     * Created new documents in the index. The new documents are retrieved from HotelsDataArray.json
     * @throws IOException If the file in HOTELS_DATA_JSON is not existing or invalid.
     */
    public void initialize() throws IOException {
        if (searchIndexASyncClient == null) {
            searchIndexASyncClient = new SearchIndexClientBuilderImpl()
                .serviceName(searchServiceName)
                .searchDnsSuffix(dnsSuffix)
                .indexName(indexName)
                .apiVersion(apiVersion)
                .policy(new SearchPipelinePolicy(apiAdminKey))
                .buildAsyncClient();
        }
        addDocsData();
    }

    private void addDocsData() throws IOException {
        Reader docsData = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(HOTELS_DATA_JSON));
        List<Map> hotels = new ObjectMapper().readValue(docsData, List.class);

        List<IndexAction> indexActions = createIndexActions(hotels);

        System.out.println("Indexing " + indexActions.size() + " docs");
        DocumentIndexResult documentIndexResult = searchIndexASyncClient.index(new IndexBatch().actions(indexActions))
            .block();

        System.out.println("Indexing Results:");
        assert documentIndexResult != null;
        documentIndexResult.results().forEach(result ->
            System.out.println(
                "key:" + result.key() + (result.succeeded() ? " Succeeded" : " Error: " + result.errorMessage()))
        );
    }

    private List<IndexAction> createIndexActions(List<Map> hotels) {
        List<IndexAction> indexActions = new ArrayList<>();
        assert hotels != null;
        hotels.forEach(h -> {
            Map<String, Object> hotel = new HashMap<String, Object>(h);
            indexActions.add(new IndexAction()
                .actionType(IndexActionType.UPLOAD)
                .additionalProperties(hotel)
            );
        });
        return indexActions;
    }
}
