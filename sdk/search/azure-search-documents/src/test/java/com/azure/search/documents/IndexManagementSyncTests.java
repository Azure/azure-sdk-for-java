// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SynonymMapClient;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.GetIndexStatisticsResult;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.RequestOptions;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.Suggester;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.SearchErrorException;
import com.azure.search.documents.test.AccessConditionTests;
import com.azure.search.documents.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexManagementSyncTests extends SearchServiceTestBase {
    private SearchIndexClient searchIndexClient;
    private SynonymMapClient synonymMapClient;

    // commonly used lambda definitions
    private BiFunction<SearchIndex, AccessOptions, SearchIndex> createOrUpdateIndexFunc = (SearchIndex index,
        AccessOptions ac) -> createOrUpdateIndex(index, ac.getOnlyIfUnchanged(), ac.getRequestOptions());

    private Supplier<SearchIndex> newIndexFunc = this::createTestIndex;

    private Function<SearchIndex, SearchIndex> mutateIndexFunc = this::mutateCorsOptionsInIndex;

    private BiConsumer<SearchIndex, AccessOptions> deleteIndexFunc =
        (SearchIndex index, AccessOptions ac) ->
            searchIndexClient.deleteWithResponse(index, ac.getOnlyIfUnchanged(), ac.getRequestOptions(),
                Context.NONE);

    private SearchIndex createOrUpdateIndex(SearchIndex index, Boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return searchIndexClient.createOrUpdateWithResponse(index, false, onlyIfUnchanged,
            requestOptions, Context.NONE).getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        searchIndexClient = getSearchServiceClientBuilder().buildClient().getSearchIndexClient();
        synonymMapClient = getSearchServiceClientBuilder().buildClient().getSynonymMapClient();
    }

    @Test
    public void createIndexReturnsCorrectDefinition() {
        SearchIndex index = createTestIndex();

        SearchIndex createdIndex = searchIndexClient.create(index);
        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefinitionWithResponse() {
        SearchIndex index = createTestIndex();

        Response<SearchIndex> createIndexResponse = searchIndexClient.createWithResponse(index.setName("hotel2"),
            generateRequestOptions(), Context.NONE);
        assertObjectEquals(index, createIndexResponse.getValue(), true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefaultValues() {
        SearchIndex index = createTestIndex()
            .setCorsOptions(new CorsOptions().setAllowedOrigins("*"))
            .setScoringProfiles(Collections.singletonList(new ScoringProfile()
                .setName("MyProfile")
                .setFunctions(Collections.singletonList(new MagnitudeScoringFunction()
                    .setParameters(new MagnitudeScoringParameters()
                        .setBoostingRangeStart(1)
                        .setBoostingRangeEnd(4))
                    .setFieldName("Rating")
                    .setBoost(2.0))
                )
            ));

        SearchIndex indexResponse = searchIndexClient.create(index);
        ScoringProfile scoringProfile = indexResponse.getScoringProfiles().get(0);
        assertNull(indexResponse.getCorsOptions().getMaxAgeInSeconds());
        assertEquals(ScoringFunctionAggregation.SUM, scoringProfile.getFunctionAggregation());
        assertNotNull(scoringProfile.getFunctions().get(0));
        assertEquals(ScoringFunctionInterpolation.LINEAR, scoringProfile.getFunctions().get(0).getInterpolation());
    }

    @Test
    public void createIndexFailsWithUsefulMessageOnUserError() {
        String indexName = HOTEL_INDEX_NAME;
        SearchIndex index = new SearchIndex()
            .setName(indexName)
            .setFields(Collections.singletonList(
                new SearchField()
                    .setName("HotelId")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(false)
            ));
        String expectedMessage = String.format("The request is invalid. Details: index : Found 0 key fields in index '%s'. "
            + "Each index must have exactly one key field.", indexName);

        try {
            searchIndexClient.create(index);
            fail("createOrUpdateSearchIndex did not throw an expected Exception");
        } catch (Exception ex) {
            assertEquals(SearchErrorException.class, ex.getClass());
            assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((SearchErrorException) ex).getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void getIndexReturnsCorrectDefinition() {
        SearchIndex index = createTestIndex();
        searchIndexClient.create(index);

        SearchIndex createdIndex = searchIndexClient.getIndex(index.getName());
        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void getIndexReturnsCorrectDefinitionWithResponse() {
        SearchIndex index = createTestIndex();
        searchIndexClient.create(index);

        Response<SearchIndex> getIndexResponse = searchIndexClient.getIndexWithResponse(index.getName(), generateRequestOptions(),
            Context.NONE);
        assertObjectEquals(index, getIndexResponse.getValue(), true, "etag");
    }

    @Test
    public void getIndexThrowsOnNotFound() {
        assertHttpResponseException(
            () -> searchIndexClient.getIndex("thisindexdoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "No index with the name 'thisindexdoesnotexist' was found in the service"
        );
    }

    @Test
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndex indexToCreate = createTestIndex();
        AccessOptions accessOptions = new AccessOptions(false);

        // Create the resource in the search service
        SearchIndex originalIndex = createOrUpdateIndexFunc.apply(indexToCreate, accessOptions);

        // Update the resource, the eTag will be changed
        SearchIndex updatedIndex = createOrUpdateIndexFunc.apply(originalIndex
            .setCorsOptions(new CorsOptions().setAllowedOrigins("https://test.com/")), accessOptions);

        try {
            accessOptions = new AccessOptions(true);
            deleteIndexFunc.accept(originalIndex, accessOptions);
            fail("deleteFunc should have failed due to selected MatchConditions");
        } catch (Exception exc) {
            assertEquals(SearchErrorException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((SearchErrorException) exc).getResponse().getStatusCode());
        }

        accessOptions = new AccessOptions(true);

        // Delete should succeed
        deleteIndexFunc.accept(updatedIndex, accessOptions);
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteIndexFunc, createOrUpdateIndexFunc,
            newIndexFunc);
    }

    @Test
    public void deleteIndexIsIdempotent() {
        SearchIndex index = new SearchIndex()
            .setName(HOTEL_INDEX_NAME)
            .setFields(Collections.singletonList(
                new SearchField()
                    .setName("HotelId")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(true)
            ));
        Response<Void> deleteResponse = searchIndexClient.deleteWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        Response<SearchIndex> createResponse = searchIndexClient.createWithResponse(index, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = searchIndexClient.deleteWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = searchIndexClient.deleteWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteIndex() {
        SearchIndex index = createTestIndex();
        searchIndexClient.create(index);
        searchIndexClient.delete(index.getName());

        assertThrows(SearchErrorException.class, () -> searchIndexClient.getIndex(index.getName()));
    }

    @Test
    public void canCreateAndListIndexes() {
        SearchIndex index1 = createTestIndex();
        SearchIndex index2 = createTestIndex().setName("hotels2");

        searchIndexClient.create(index1);
        searchIndexClient.create(index2);

        PagedIterable<SearchIndex> actual = searchIndexClient.listIndexes();
        List<SearchIndex> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(index1.getName(), result.get(0).getName());
        assertEquals(index2.getName(), result.get(1).getName());
    }

    @Test
    public void canListIndexesWithSelectedField() {
        SearchIndex index1 = createTestIndex();
        SearchIndex index2 = createTestIndex().setName("hotels2");

        searchIndexClient.create(index1);
        searchIndexClient.create(index2);

        PagedIterable<SearchIndex> selectedFieldListResponse =
            searchIndexClient.listSearchIndexNames(generateRequestOptions(), Context.NONE);
        List<SearchIndex> result = selectedFieldListResponse.stream().collect(Collectors.toList());

        result.forEach(res -> {
            assertNotNull(res.getName());
            assertNull(res.getFields());
            assertNull(res.getDefaultScoringProfile());
            assertNull(res.getCorsOptions());
            assertNull(res.getScoringProfiles());
            assertNull(res.getSuggesters());
            assertNull(res.getAnalyzers());
            assertNull(res.getTokenizers());
            assertNull(res.getTokenFilters());
            assertNull(res.getCharFilters());
        });

        assertEquals(2, result.size());
        assertEquals(result.get(0).getName(), index1.getName());
        assertEquals(result.get(1).getName(), index2.getName());
    }

    @Test
    public void canAddSynonymFieldProperty() {
        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap().setName(synonymMapName).setSynonyms("hotel,motel");
        synonymMapClient.create(synonymMap);

        SearchIndex index = new SearchIndex()
            .setName(HOTEL_INDEX_NAME)
            .setFields(Arrays.asList(
                new SearchField()
                    .setName("HotelId")
                    .setType(SearchFieldDataType.STRING)
                    .setKey(true),
                new SearchField()
                    .setName("HotelName")
                    .setType(SearchFieldDataType.STRING)
                    .setSynonymMaps(Collections.singletonList(synonymMapName))
            ));

        SearchIndex createdIndex = searchIndexClient.create(index);

        List<String> actualSynonym = index.getFields().get(1).getSynonymMaps();
        List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMaps();
        assertEquals(actualSynonym, expectedSynonym);
    }

    @Test
    public void canUpdateSynonymFieldProperty() {
        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap()
            .setName(synonymMapName)
            .setSynonyms("hotel,motel");

        synonymMapClient.create(synonymMap);

        // Create an index
        SearchIndex index = createTestIndex();
        SearchField hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMaps(Collections.singletonList(synonymMapName));
        searchIndexClient.create(index);

        // Update an existing index
        SearchIndex existingIndex = searchIndexClient.getIndex(index.getName());
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMaps(Collections.emptyList());

        SearchIndex updatedIndex = searchIndexClient.createOrUpdateWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateIndexDefinition() {
        SearchIndex fullFeaturedIndex = createTestIndex();

        // Start out with no scoring profiles and different CORS options.
        SearchIndex initialIndex = createTestIndex();
        initialIndex.setName(fullFeaturedIndex.getName())
            .setScoringProfiles(new ArrayList<>())
            .setDefaultScoringProfile(null)
            .setCorsOptions(initialIndex.getCorsOptions().setAllowedOrigins("*"));

        SearchIndex index = searchIndexClient.create(initialIndex);

        // Now update the index.
        String[] allowedOrigins = fullFeaturedIndex.getCorsOptions()
            .getAllowedOrigins()
            .toArray(new String[0]);
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(index.getCorsOptions().setAllowedOrigins(allowedOrigins));

        SearchIndex updatedIndex = searchIndexClient.createOrUpdate(index);

        assertObjectEquals(fullFeaturedIndex, updatedIndex, true, "etag", "@odata.etag");

        // Modify the fields on an existing index
        SearchIndex existingIndex = searchIndexClient.getIndex(fullFeaturedIndex.getName());

        SynonymMap synonymMap = synonymMapClient.create(new SynonymMap()
            .setName("names")
            .setSynonyms("hotel,motel")
        );

        SearchField tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setHidden(true)
            .setSearchAnalyzer(LexicalAnalyzerName.WHITESPACE)
            .setSynonymMaps(Collections.singletonList(synonymMap.getName()));

        SearchField hotelWebSiteField = new SearchField()
            .setName("HotelWebsite")
            .setType(SearchFieldDataType.STRING)
            .setSearchable(Boolean.TRUE)
            .setFilterable(Boolean.TRUE);
        addFieldToIndex(existingIndex, hotelWebSiteField);

        SearchField hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setHidden(true);

        updatedIndex = searchIndexClient.createOrUpdateWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();

        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateSuggesterWithNewIndexFields() {
        SearchIndex index = createTestIndex();
        searchIndexClient.create(index);

        SearchIndex existingIndex = searchIndexClient.getIndex(index.getName());

        existingIndex.getFields().addAll(Arrays.asList(
            new SearchField()
                .setName("HotelAmenities")
                .setType(SearchFieldDataType.STRING),
            new SearchField()
                .setName("HotelRewards")
                .setType(SearchFieldDataType.STRING)));
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Arrays.asList("HotelAmenities", "HotelRewards"))
        ));

        SearchIndex updatedIndex = searchIndexClient.createOrUpdateWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        SearchIndex index = createTestIndex();
        searchIndexClient.create(index);

        SearchIndex existingIndex = searchIndexClient.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Collections.singletonList(existingFieldName))
        ));

        assertHttpResponseException(
            () -> searchIndexClient.createOrUpdate(existingIndex),
            HttpResponseStatus.BAD_REQUEST,
            String.format("Fields that were already present in an index (%s) cannot be "
                    + "referenced by a new suggester. Only new fields added in the same index update operation are allowed.",
                existingFieldName)
        );
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExist() {
        SearchIndex expected = createTestIndex();

        SearchIndex actual = searchIndexClient.createOrUpdate(expected);
        assertObjectEquals(expected, actual, true, "etag");

        actual = searchIndexClient.createOrUpdate(expected.setName("hotel1"));
        assertObjectEquals(expected, actual, true, "etag");

        SearchIndex res = searchIndexClient.createOrUpdate(expected.setName("hotel2"));
        assertEquals(expected.getName(), res.getName());
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponse() {
        SearchIndex expected = createTestIndex();

        SearchIndex actual = searchIndexClient.createOrUpdateWithResponse(expected, false, false,
            generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(expected, actual, true, "etag");

        actual = searchIndexClient.createOrUpdateWithResponse(expected.setName("hotel1"),
            false, false, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(expected, actual, true, "etag");

        Response<SearchIndex> createOrUpdateResponse = searchIndexClient.createOrUpdateWithResponse(expected.setName("hotel2"),
            false, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests.createOrUpdateIfNotExistsSucceedsOnNoResource(createOrUpdateIndexFunc, newIndexFunc);
    }


    @Test
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource() {
        AccessConditionTests.updateIfExistsSucceedsOnExistingResource(newIndexFunc, createOrUpdateIndexFunc,
            mutateIndexFunc);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionTests.updateIfNotChangedSucceedsWhenResourceUnchanged(newIndexFunc, createOrUpdateIndexFunc,
            mutateIndexFunc);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged() {
        AccessConditionTests.updateIfNotChangedFailsWhenResourceChanged(newIndexFunc, createOrUpdateIndexFunc,
            mutateIndexFunc);
    }

    @Test
    public void canCreateAndGetIndexStats() {
        SearchIndex index = createTestIndex();
        searchIndexClient.createOrUpdate(index);
        GetIndexStatisticsResult indexStatistics = searchIndexClient.getStatistics(index.getName());
        assertEquals(0, indexStatistics.getDocumentCount());
        assertEquals(0, indexStatistics.getStorageSize());
    }

    @Test
    public void canCreateAndGetIndexStatsWithResponse() {
        SearchIndex index = createTestIndex();
        searchIndexClient.createOrUpdate(index);

        Response<GetIndexStatisticsResult> indexStatisticsResponse = searchIndexClient.getStatisticsWithResponse(index.getName(),
            generateRequestOptions(), Context.NONE);
        assertEquals(0, indexStatisticsResponse.getValue().getDocumentCount());
        assertEquals(0, indexStatisticsResponse.getValue().getStorageSize());
    }

    SearchIndex mutateCorsOptionsInIndex(SearchIndex index) {
        index.getCorsOptions().setAllowedOrigins("*");
        return index;
    }

    SearchField getFieldByName(SearchIndex index, String name) {
        return index.getFields()
            .stream()
            .filter(f -> f.getName().equals(name))
            .findFirst().get();
    }
}
