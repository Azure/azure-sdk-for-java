// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.SearchTestBase;
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
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SynonymMap;
import org.junit.jupiter.api.Assertions;
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

    private SearchIndexClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchIndexClientBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        boolean synonymMapsDeleted = false;
        for (String synonymMap : synonymMapsToDelete) {
            client.deleteSynonymMap(synonymMap);
            synonymMapsDeleted = true;
        }

        for (String index : indexesToDelete) {
            client.deleteIndex(index);
        }

        if (synonymMapsDeleted) {
            sleepIfRunningAgainstService(5000);
        }
    }

    @Test
    public void createIndexReturnsCorrectDefinition() {
        SearchIndex index = createTestIndex();
        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefinitionWithResponse() {
        SearchIndex index = createTestIndex();
        Response<SearchIndex> createIndexResponse = client.createIndexWithResponse(index.setName("hotel2"),
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
        SearchIndex indexResponse = client.createIndex(index);
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
            client.createIndex(index);
            fail("createOrUpdateIndex did not throw an expected Exception");
        } catch (Exception ex) {
            assertEquals(HttpResponseException.class, ex.getClass());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ((HttpResponseException) ex).getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void getIndexReturnsCorrectDefinition() {
        SearchIndex index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex createdIndex = client.getIndex(index.getName());
        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void getIndexReturnsCorrectDefinitionWithResponse() {
        SearchIndex index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        Response<SearchIndex> getIndexResponse = client.getIndexWithResponse(index.getName(), generateRequestOptions(),
            Context.NONE);
        assertObjectEquals(index, getIndexResponse.getValue(), true, "etag");
    }

    @Test
    public void getIndexThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getIndex("thisindexdoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "No index with the name 'thisindexdoesnotexist' was found in the service"
        );
    }

    @Test
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndex indexToCreate = createTestIndex();

        // Create the resource in the search service
        SearchIndex originalIndex = client.createOrUpdateIndexWithResponse(indexToCreate, false, false, null, Context.NONE)
            .getValue();

        // Update the resource, the eTag will be changed
        SearchIndex updatedIndex = client.createOrUpdateIndexWithResponse(originalIndex
            .setCorsOptions(new CorsOptions().setAllowedOrigins("https://test.com/")), false, false, null, Context.NONE)
            .getValue();

        try {
            client.deleteIndexWithResponse(originalIndex, true, null, Context.NONE);
            fail("deleteFunc should have failed due to selected MatchConditions");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteIndexWithResponse(updatedIndex, true, null, Context.NONE);
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        SearchIndex index = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();

        client.deleteIndexWithResponse(index, true, null, Context.NONE);

        // Try to delete again and expect to fail
        try {
            client.deleteIndexWithResponse(index, true, null, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (HttpResponseException ex) {
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
        Response<Void> deleteResponse = client.deleteIndexWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<SearchIndex> createResponse = client.createIndexWithResponse(index, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = client.deleteIndexWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteIndexWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteIndex() {
        SearchIndex index = createTestIndex();
        client.createIndex(index);
        client.deleteIndex(index.getName());

        assertThrows(HttpResponseException.class, () -> client.getIndex(index.getName()));
    }

    @Test
    public void canCreateAndListIndexes() {
        SearchIndex index1 = createTestIndex();
        index1.setName("a" + index1.getName());
        SearchIndex index2 = createTestIndex();
        index2.setName("b" + index1.getName());

        client.createIndex(index1);
        indexesToDelete.add(index1.getName());
        client.createIndex(index2);
        indexesToDelete.add(index2.getName());

        PagedIterable<SearchIndex> actual = client.listIndexes();
        List<SearchIndex> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertObjectEquals(index1, result.get(0), true);
        assertObjectEquals(index2, result.get(1), true);
    }

    @Test
    public void canListIndexesWithSelectedField() {
        SearchIndex index1 = createTestIndex();
        index1.setName("a" + index1.getName());
        SearchIndex index2 = createTestIndex();
        index2.setName("b" + index1.getName());

        client.createIndex(index1);
        indexesToDelete.add(index1.getName());
        client.createIndex(index2);
        indexesToDelete.add(index2.getName());

        PagedIterable<String> selectedFieldListResponse = client.listIndexNames(generateRequestOptions(), Context.NONE);
        List<String> result = selectedFieldListResponse.stream().collect(Collectors.toList());

        result.forEach(Assertions::assertNotNull);

        assertEquals(2, result.size());
        assertEquals(result.get(0), index1.getName());
        assertEquals(result.get(1), index2.getName());
    }

    @Test
    public void canAddSynonymFieldProperty() {
        String synonymMapName = testResourceNamer.randomName("names", 32);
        SynonymMap synonymMap = new SynonymMap().setName(synonymMapName).setSynonyms("hotel,motel");
        client.createSynonymMap(synonymMap);
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
                    .setSynonymMapNames(Collections.singletonList(synonymMapName))
            ));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        List<String> actualSynonym = index.getFields().get(1).getSynonymMapNames();
        List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMapNames();
        assertEquals(actualSynonym, expectedSynonym);
    }

    @Test
    public void canUpdateSynonymFieldProperty() {
        String synonymMapName = testResourceNamer.randomName("names", 32);
        SynonymMap synonymMap = new SynonymMap()
            .setName(synonymMapName)
            .setSynonyms("hotel,motel");

        client.createSynonymMap(synonymMap);
        synonymMapsToDelete.add(synonymMap.getName());

        // Create an index
        SearchIndex index = createTestIndex();
        SearchField hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMapNames(Collections.singletonList(synonymMapName));
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        // Update an existing index
        SearchIndex existingIndex = client.getIndex(index.getName());
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMapNames(Collections.emptyList());

        SearchIndex updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
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

        SearchIndex index = client.createIndex(initialIndex);
        indexesToDelete.add(index.getName());

        // Now update the index.
        String[] allowedOrigins = fullFeaturedIndex.getCorsOptions()
            .getAllowedOrigins()
            .toArray(new String[0]);
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(index.getCorsOptions().setAllowedOrigins(allowedOrigins));

        SearchIndex updatedIndex = client.createOrUpdateIndex(index);

        assertObjectEquals(fullFeaturedIndex, updatedIndex, true, "etag", "@odata.etag");

        // Modify the fields on an existing index
        SearchIndex existingIndex = client.getIndex(fullFeaturedIndex.getName());

        SynonymMap synonymMap = client.createSynonymMap(new SynonymMap()
            .setName(testResourceNamer.randomName("names", 32))
            .setSynonyms("hotel,motel")
        );
        synonymMapsToDelete.add(synonymMap.getName());

        SearchField tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setHidden(true)
            .setSearchAnalyzerName(LexicalAnalyzerName.WHITESPACE)
            .setSynonymMapNames(Collections.singletonList(synonymMap.getName()));

        SearchField hotelWebSiteField = new SearchField()
            .setName("HotelWebsite")
            .setType(SearchFieldDataType.STRING)
            .setSearchable(Boolean.TRUE)
            .setFilterable(Boolean.TRUE);
        existingIndex.getFields().add(hotelWebSiteField);

        SearchField hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setHidden(true);

        updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();

        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateSuggesterWithNewIndexFields() {
        SearchIndex index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex existingIndex = client.getIndex(index.getName());

        existingIndex.getFields().addAll(Arrays.asList(
            new SearchField()
                .setName("HotelAmenities")
                .setType(SearchFieldDataType.STRING),
            new SearchField()
                .setName("HotelRewards")
                .setType(SearchFieldDataType.STRING)));
        existingIndex.setSearchSuggesters(Collections.singletonList(new SearchSuggester()
            .setName("Suggestion")
            .setSourceFields(Arrays.asList("HotelAmenities", "HotelRewards"))
        ));

        SearchIndex updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        SearchIndex index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex existingIndex = client.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSearchSuggesters(Collections.singletonList(new SearchSuggester()
            .setName("Suggestion")
            .setSourceFields(Collections.singletonList(existingFieldName))
        ));

        assertHttpResponseException(
            () -> client.createOrUpdateIndex(existingIndex),
            HttpURLConnection.HTTP_BAD_REQUEST,
            String.format("Fields that were already present in an index (%s) cannot be "
                    + "referenced by a new suggester. Only new fields added in the same index update operation are allowed.",
                existingFieldName)
        );
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExist() {
        SearchIndex expected = createTestIndex();

        SearchIndex actual = client.createOrUpdateIndex(expected);
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        actual = client.createOrUpdateIndex(expected.setName("hotel1"));
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        SearchIndex res = client.createOrUpdateIndex(expected.setName("hotel2"));
        indexesToDelete.add(res.getName());
        assertEquals(expected.getName(), res.getName());
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponse() {
        SearchIndex expected = createTestIndex();

        SearchIndex actual = client.createOrUpdateIndexWithResponse(expected, false, false,
            generateRequestOptions(), Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        actual = client.createOrUpdateIndexWithResponse(expected.setName("hotel1"),
            false, false, generateRequestOptions(), Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        Response<SearchIndex> createOrUpdateResponse = client.createOrUpdateIndexWithResponse(expected.setName("hotel2"),
            false, false, generateRequestOptions(), Context.NONE);
        indexesToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        SearchIndex index = client.createOrUpdateIndexWithResponse(createTestIndex(), false, true, null, Context.NONE)
            .getValue();
        indexesToDelete.add(index.getName());

        assertFalse(CoreUtils.isNullOrEmpty(index.getETag()));
    }


    @Test
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource() {
        SearchIndex original = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, false, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        SearchIndex original = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged() {
        SearchIndex original = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        try {
            client.createOrUpdateIndexWithResponse(original, false, true, null, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void canCreateAndGetIndexStats() {
        SearchIndex index = createTestIndex();
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        GetIndexStatisticsResult indexStatistics = client.getIndexStatistics(index.getName());
        assertEquals(0, indexStatistics.getDocumentCount());
        assertEquals(0, indexStatistics.getStorageSize());
    }

    @Test
    public void canCreateAndGetIndexStatsWithResponse() {
        SearchIndex index = createTestIndex();
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        Response<GetIndexStatisticsResult> indexStatisticsResponse = client.getIndexStatisticsWithResponse(index.getName(),
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
