// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.models.AnalyzerName;
import com.azure.search.documents.models.CorsOptions;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.GetIndexStatisticsResult;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.MagnitudeScoringFunction;
import com.azure.search.documents.models.MagnitudeScoringParameters;
import com.azure.search.documents.models.ScoringFunctionAggregation;
import com.azure.search.documents.models.ScoringFunctionInterpolation;
import com.azure.search.documents.models.ScoringProfile;
import com.azure.search.documents.models.SearchErrorException;
import com.azure.search.documents.models.Suggester;
import com.azure.search.documents.models.SynonymMap;
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

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
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
        Index index = createTestIndex();
        Index createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefinitionWithResponse() {
        Index index = createTestIndex();
        Response<Index> createIndexResponse = client.createIndexWithResponse(index.setName("hotel2"),
            generateRequestOptions(), Context.NONE);
        indexesToDelete.add(createIndexResponse.getValue().getName());

        assertObjectEquals(index, createIndexResponse.getValue(), true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefaultValues() {
        Index index = createTestIndex()
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
        Index indexResponse = client.createIndex(index);
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
        Index index = new Index()
            .setName(indexName)
            .setFields(Collections.singletonList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(false)
            ));
        String expectedMessage = String.format("The request is invalid. Details: index : Found 0 key fields in index '%s'. "
            + "Each index must have exactly one key field.", indexName);

        try {
            client.createIndex(index);
            fail("createOrUpdateIndex did not throw an expected Exception");
        } catch (Exception ex) {
            assertEquals(SearchErrorException.class, ex.getClass());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ((SearchErrorException) ex).getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void getIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        Index createdIndex = client.getIndex(index.getName());
        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void getIndexReturnsCorrectDefinitionWithResponse() {
        Index index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        Response<Index> getIndexResponse = client.getIndexWithResponse(index.getName(), generateRequestOptions(),
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
        Index indexToCreate = createTestIndex();

        // Create the resource in the search service
        Index originalIndex = client.createOrUpdateIndexWithResponse(indexToCreate, false, false, null, Context.NONE)
            .getValue();

        // Update the resource, the eTag will be changed
        Index updatedIndex = client.createOrUpdateIndexWithResponse(originalIndex
            .setCorsOptions(new CorsOptions().setAllowedOrigins("https://test.com/")), false, false, null, Context.NONE)
            .getValue();

        try {
            client.deleteIndexWithResponse(originalIndex, true, null, Context.NONE);
            fail("deleteFunc should have failed due to selected MatchConditions");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteIndexWithResponse(updatedIndex, true, null, Context.NONE);
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        Index index = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();

        client.deleteIndexWithResponse(index, true, null, Context.NONE);

        // Try to delete again and expect to fail
        try {
            client.deleteIndexWithResponse(index, true, null, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    @Test
    public void deleteIndexIsIdempotent() {
        Index index = new Index()
            .setName(HOTEL_INDEX_NAME)
            .setFields(Collections.singletonList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(true)
            ));
        Response<Void> deleteResponse = client.deleteIndexWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<Index> createResponse = client.createIndexWithResponse(index, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = client.deleteIndexWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteIndexWithResponse(index, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteIndex() {
        Index index = createTestIndex();
        client.createIndex(index);
        client.deleteIndex(index.getName());

        assertThrows(SearchErrorException.class, () -> client.getIndex(index.getName()));
    }

    @Test
    public void canCreateAndListIndexes() {
        Index index1 = createTestIndex();
        index1.setName("a" + index1.getName());
        Index index2 = createTestIndex();
        index2.setName("b" + index1.getName());

        client.createIndex(index1);
        indexesToDelete.add(index1.getName());
        client.createIndex(index2);
        indexesToDelete.add(index2.getName());

        PagedIterable<Index> actual = client.listIndexes();
        List<Index> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(index1.getName(), result.get(0).getName());
        assertEquals(index2.getName(), result.get(1).getName());
    }

    @Test
    public void canListIndexesWithSelectedField() {
        Index index1 = createTestIndex();
        index1.setName("a" + index1.getName());
        Index index2 = createTestIndex();
        index2.setName("b" + index1.getName());

        client.createIndex(index1);
        indexesToDelete.add(index1.getName());
        client.createIndex(index2);
        indexesToDelete.add(index2.getName());

        PagedIterable<Index> selectedFieldListResponse = client.listIndexes("name",
            generateRequestOptions(), Context.NONE);
        List<Index> result = selectedFieldListResponse.stream().collect(Collectors.toList());

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
        client.createSynonymMap(synonymMap);
        synonymMapsToDelete.add(synonymMap.getName());

        Index index = new Index()
            .setName(HOTEL_INDEX_NAME)
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(true),
                new Field()
                    .setName("HotelName")
                    .setType(DataType.EDM_STRING)
                    .setSynonymMaps(Collections.singletonList(synonymMapName))
            ));

        Index createdIndex = client.createIndex(index);
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

        client.createSynonymMap(synonymMap);
        synonymMapsToDelete.add(synonymMap.getName());

        // Create an index
        Index index = createTestIndex();
        Field hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMaps(Collections.singletonList(synonymMapName));
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        // Update an existing index
        Index existingIndex = client.getIndex(index.getName());
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMaps(Collections.emptyList());

        Index updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateIndexDefinition() {
        Index fullFeaturedIndex = createTestIndex();

        // Start out with no scoring profiles and different CORS options.
        Index initialIndex = createTestIndex();
        initialIndex.setName(fullFeaturedIndex.getName())
            .setScoringProfiles(new ArrayList<>())
            .setDefaultScoringProfile(null)
            .setCorsOptions(initialIndex.getCorsOptions().setAllowedOrigins("*"));

        Index index = client.createIndex(initialIndex);
        indexesToDelete.add(index.getName());

        // Now update the index.
        String[] allowedOrigins = fullFeaturedIndex.getCorsOptions()
            .getAllowedOrigins()
            .toArray(new String[0]);
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(index.getCorsOptions().setAllowedOrigins(allowedOrigins));

        Index updatedIndex = client.createOrUpdateIndex(index);

        assertObjectEquals(fullFeaturedIndex, updatedIndex, true, "etag", "@odata.etag");

        // Modify the fields on an existing index
        Index existingIndex = client.getIndex(fullFeaturedIndex.getName());

        SynonymMap synonymMap = client.createSynonymMap(new SynonymMap()
            .setName(testResourceNamer.randomName("names", 32))
            .setSynonyms("hotel,motel")
        );
        synonymMapsToDelete.add(synonymMap.getName());

        Field tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setHidden(true)
            .setSearchAnalyzer(AnalyzerName.WHITESPACE)
            .setSynonymMaps(Collections.singletonList(synonymMap.getName()));

        Field hotelWebSiteField = new Field()
            .setName("HotelWebsite")
            .setType(DataType.EDM_STRING)
            .setSearchable(Boolean.TRUE)
            .setFilterable(Boolean.TRUE);
        existingIndex.getFields().add(hotelWebSiteField);

        Field hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setHidden(true);

        updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();

        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateSuggesterWithNewIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        Index existingIndex = client.getIndex(index.getName());

        existingIndex.getFields().addAll(Arrays.asList(
            new Field()
                .setName("HotelAmenities")
                .setType(DataType.EDM_STRING),
            new Field()
                .setName("HotelRewards")
                .setType(DataType.EDM_STRING)));
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Arrays.asList("HotelAmenities", "HotelRewards"))
        ));

        Index updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        Index existingIndex = client.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
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
        Index expected = createTestIndex();

        Index actual = client.createOrUpdateIndex(expected);
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        actual = client.createOrUpdateIndex(expected.setName("hotel1"));
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        Index res = client.createOrUpdateIndex(expected.setName("hotel2"));
        indexesToDelete.add(res.getName());
        assertEquals(expected.getName(), res.getName());
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponse() {
        Index expected = createTestIndex();

        Index actual = client.createOrUpdateIndexWithResponse(expected, false, false,
            generateRequestOptions(), Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        actual = client.createOrUpdateIndexWithResponse(expected.setName("hotel1"),
            false, false, generateRequestOptions(), Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        Response<Index> createOrUpdateResponse = client.createOrUpdateIndexWithResponse(expected.setName("hotel2"),
            false, false, generateRequestOptions(), Context.NONE);
        indexesToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        Index index = client.createOrUpdateIndexWithResponse(createTestIndex(), false, true, null, Context.NONE)
            .getValue();
        indexesToDelete.add(index.getName());

        assertFalse(CoreUtils.isNullOrEmpty(index.getETag()));
    }


    @Test
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource() {
        Index original = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        Index updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, false, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        Index original = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        Index updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged() {
        Index original = client.createOrUpdateIndexWithResponse(createTestIndex(), false, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        Index updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        try {
            client.createOrUpdateIndexWithResponse(original, false, true, null, Context.NONE);
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
        Index index = createTestIndex();
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        GetIndexStatisticsResult indexStatistics = client.getIndexStatistics(index.getName());
        assertEquals(0, indexStatistics.getDocumentCount());
        assertEquals(0, indexStatistics.getStorageSize());
    }

    @Test
    public void canCreateAndGetIndexStatsWithResponse() {
        Index index = createTestIndex();
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        Response<GetIndexStatisticsResult> indexStatisticsResponse = client.getIndexStatisticsWithResponse(index.getName(),
            generateRequestOptions(), Context.NONE);
        assertEquals(0, indexStatisticsResponse.getValue().getDocumentCount());
        assertEquals(0, indexStatisticsResponse.getValue().getStorageSize());
    }

    Index mutateCorsOptionsInIndex(Index index) {
        index.getCorsOptions().setAllowedOrigins("*");
        return index;
    }

    Field getFieldByName(Index index, String name) {
        return index.getFields()
            .stream()
            .filter(f -> f.getName().equals(name))
            .findFirst().get();
    }
}
