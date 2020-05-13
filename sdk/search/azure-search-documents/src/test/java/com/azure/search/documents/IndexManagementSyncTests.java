// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.GetIndexStatisticsResult;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.Suggester;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.SearchErrorException;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.HOTEL_INDEX_NAME;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.generateRequestOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexManagementSyncTests extends SearchTestBase {
    private final List<String> indexesToDelete = new ArrayList<>();
    private final List<String> synonymMapsToDelete = new ArrayList<>();

    private SearchServiceClient client;
    private SearchIndexClient searchIndexClient;

    @Override
    protected void beforeTest() {
        client = getSearchServiceClientBuilder().buildClient();
        searchIndexClient = client.getSearchIndexClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        boolean synonymMapsDeleted = false;
        for (String synonymMap : synonymMapsToDelete) {
            client.getSynonymMapClient().delete(synonymMap);
            synonymMapsDeleted = true;
        }

        for (String index : indexesToDelete) {
            client.getSearchIndexClient().delete(index);
        }

        if (synonymMapsDeleted) {
            sleepIfRunningAgainstService(5000);
        }
    }

    @Test
    public void createIndexReturnsCorrectDefinition() {
        SearchIndex index = createTestIndex();
        SearchIndex createdIndex = searchIndexClient.create(index);
        indexesToDelete.add(createdIndex.getName());

        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefinitionWithResponse() {
        SearchIndex index = createTestIndex();
        Response<SearchIndex> createIndexResponse = searchIndexClient.createWithResponse(index.setName("hotel2"),
            generateRequestOptions(), Context.NONE);
        indexesToDelete.add(createIndexResponse.getValue().getName());

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
        indexesToDelete.add(indexResponse.getName());

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
            fail("createOrUpdateIndex did not throw an expected Exception");
        } catch (Exception ex) {
            assertEquals(SearchErrorException.class, ex.getClass());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ((SearchErrorException) ex).getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void getIndexReturnsCorrectDefinition() {
        SearchIndex index = createTestIndex();
        searchIndexClient.create(index);
        indexesToDelete.add(index.getName());

        SearchIndex createdIndex = searchIndexClient.getIndex(index.getName());
        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void getIndexReturnsCorrectDefinitionWithResponse() {
        SearchIndex index = createTestIndex();
        searchIndexClient.create(index);
        indexesToDelete.add(index.getName());

        Response<SearchIndex> getIndexResponse = searchIndexClient.getIndexWithResponse(index.getName(), generateRequestOptions(),
            Context.NONE);
        assertObjectEquals(index, getIndexResponse.getValue(), true, "etag");
    }

    @Test
    public void getIndexThrowsOnNotFound() {
        assertHttpResponseException(
            () -> searchIndexClient.getIndex("thisindexdoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "No index with the name 'thisindexdoesnotexist' was found in the service"
        );
    }

    @Test
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndex indexToCreate = createTestIndex();

        // Create the resource in the search service
        SearchIndex originalIndex = searchIndexClient.createOrUpdateWithResponse(indexToCreate, false, false, null, Context.NONE)
            .getValue();

        // Update the resource, the eTag will be changed
        SearchIndex updatedIndex = searchIndexClient.createOrUpdateWithResponse(originalIndex
            .setCorsOptions(new CorsOptions().setAllowedOrigins("https://test.com/")), false, false, null, Context.NONE)
            .getValue();

        try {
            searchIndexClient.deleteWithResponse(originalIndex, true, null, Context.NONE);
            fail("deleteFunc should have failed due to selected MatchConditions");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        searchIndexClient.deleteWithResponse(updatedIndex, true, null, Context.NONE);
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        SearchIndex index = searchIndexClient.createOrUpdateWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();

        searchIndexClient.deleteWithResponse(index, true, null, Context.NONE);

        // Try to delete again and expect to fail
        try {
            searchIndexClient.deleteWithResponse(index, true, null, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
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
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<SearchIndex> createResponse = searchIndexClient.createWithResponse(index, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = searchIndexClient.deleteWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = searchIndexClient.deleteWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
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
        index1.setName("a" + index1.getName());
        SearchIndex index2 = createTestIndex();
        index2.setName("b" + index1.getName());

        searchIndexClient.create(index1);
        indexesToDelete.add(index1.getName());
        searchIndexClient.create(index2);
        indexesToDelete.add(index2.getName());

        PagedIterable<SearchIndex> actual = searchIndexClient.listIndexes();
        List<SearchIndex>  result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(index1.getName(), result.get(0).getName());
        assertEquals(index2.getName(), result.get(1).getName());
    }

    @Test
    public void canListIndexesWithSelectedField() {
        SearchIndex index1 = createTestIndex();
        index1.setName("a" + index1.getName());
        SearchIndex index2 = createTestIndex();
        index2.setName("b" + index1.getName());

        searchIndexClient.create(index1);
        indexesToDelete.add(index1.getName());
        searchIndexClient.create(index2);
        indexesToDelete.add(index2.getName());

        PagedIterable<SearchIndex> selectedFieldListResponse = searchIndexClient.listSearchIndexNames(
            generateRequestOptions(), Context.NONE);
        List<SearchIndex>  result = selectedFieldListResponse.stream().collect(Collectors.toList());

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
        String synonymMapName = testResourceNamer.randomName("names", 32);
        SynonymMap synonymMap = new SynonymMap().setName(synonymMapName).setSynonyms("hotel,motel");
        client.getSynonymMapClient().create(synonymMap);
        synonymMapsToDelete.add(synonymMap.getName());

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
        indexesToDelete.add(createdIndex.getName());

        List<String> actualSynonym = index.getFields().get(1).getSynonymMaps();
        List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMaps();
        assertEquals(actualSynonym, expectedSynonym);
    }

    @Test
    public void canUpdateSynonymFieldProperty() {
        String synonymMapName = testResourceNamer.randomName("names", 32);
        SynonymMap synonymMap = new SynonymMap()
            .setName(synonymMapName)
            .setSynonyms("hotel,motel");

        client.getSynonymMapClient().create(synonymMap);
        synonymMapsToDelete.add(synonymMap.getName());

        // Create an index
        SearchIndex index = createTestIndex();
        SearchField hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMaps(Collections.singletonList(synonymMapName));
        searchIndexClient.create(index);
        indexesToDelete.add(index.getName());

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
        indexesToDelete.add(index.getName());

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

        SynonymMap synonymMap = client.getSynonymMapClient().create(new SynonymMap()
            .setName(testResourceNamer.randomName("names", 32))
            .setSynonyms("hotel,motel")
        );
        synonymMapsToDelete.add(synonymMap.getName());

        SearchField tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setHidden(true)
            .setSearchAnalyzer(LexicalAnalyzerName.WHITESPACE)
            .setSynonymMaps(Collections.singletonList(synonymMap.getName()));

        SearchField hotelWebSiteField = new SearchField()
            .setName("HotelWebsite")
            .setType(SearchFieldDataType.STRING)
            .setSearchable(Boolean.TRUE)
            .setFilterable(Boolean.TRUE);
        existingIndex.getFields().add(hotelWebSiteField);

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
        indexesToDelete.add(index.getName());

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
        indexesToDelete.add(index.getName());

        SearchIndex existingIndex = searchIndexClient.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Collections.singletonList(existingFieldName))
        ));

        assertHttpResponseException(
            () -> searchIndexClient.createOrUpdate(existingIndex),
            HttpURLConnection.HTTP_BAD_REQUEST,
            String.format("Fields that were already present in an index (%s) cannot be referenced by a new suggester. "
                    + "Only new fields added in the same index update operation are allowed.",
                existingFieldName)
        );
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExist() {
        SearchIndex expected = createTestIndex();

        SearchIndex actual = searchIndexClient.createOrUpdate(expected);
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        actual = searchIndexClient.createOrUpdate(expected.setName("hotel1"));
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        SearchIndex res = searchIndexClient.createOrUpdate(expected.setName("hotel2"));
        indexesToDelete.add(res.getName());
        assertEquals(expected.getName(), res.getName());
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponse() {
        SearchIndex expected = createTestIndex();

        SearchIndex actual = searchIndexClient.createOrUpdateWithResponse(expected, false, false,
            generateRequestOptions(), Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        actual = searchIndexClient.createOrUpdateWithResponse(expected.setName("hotel1"),
            false, false, generateRequestOptions(), Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        Response<SearchIndex> createOrUpdateResponse = searchIndexClient.createOrUpdateWithResponse(expected.setName("hotel2"),
            false, false, generateRequestOptions(), Context.NONE);
        indexesToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        SearchIndex index = searchIndexClient.createOrUpdateWithResponse(createTestIndex(), false, true, null, Context.NONE)
            .getValue();
        indexesToDelete.add(index.getName());

        assertFalse(CoreUtils.isNullOrEmpty(index.getETag()));
    }


    @Test
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource() {
        SearchIndex original = searchIndexClient.createOrUpdateWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = searchIndexClient.createOrUpdateWithResponse(mutateCorsOptionsInIndex(original), false, false, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        SearchIndex original = searchIndexClient.createOrUpdateWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = searchIndexClient.createOrUpdateWithResponse(mutateCorsOptionsInIndex(original), false, true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged() {
        SearchIndex original = searchIndexClient.createOrUpdateWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = searchIndexClient.createOrUpdateWithResponse(mutateCorsOptionsInIndex(original), false, true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        try {
            searchIndexClient.createOrUpdateWithResponse(original, false, true, null, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void canCreateAndGetIndexStats() {
        SearchIndex index = createTestIndex();
        searchIndexClient.createOrUpdate(index);
        indexesToDelete.add(index.getName());

        GetIndexStatisticsResult indexStatistics = searchIndexClient.getStatistics(index.getName());
        assertEquals(0, indexStatistics.getDocumentCount());
        assertEquals(0, indexStatistics.getStorageSize());
    }

    @Test
    public void canCreateAndGetIndexStatsWithResponse() {
        SearchIndex index = createTestIndex();
        searchIndexClient.createOrUpdate(index);
        indexesToDelete.add(index.getName());

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
