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
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexStatistics;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SynonymMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.HOTEL_INDEX_NAME;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
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
        SearchIndex index = createTestIndex(null);
        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefinitionWithResponse() {

        SearchIndex index = createTestIndex("hotel2");
        Response<SearchIndex> createIndexResponse = client.createIndexWithResponse(index, Context.NONE);
        indexesToDelete.add(createIndexResponse.getValue().getName());

        assertObjectEquals(index, createIndexResponse.getValue(), true, "etag");
    }

    @Test
    public void createIndexReturnsCorrectDefaultValues() {
        SearchIndex index = createTestIndex(null)
            .setCorsOptions(new CorsOptions(Collections.singletonList("*")))
            .setScoringProfiles(new ScoringProfile("MyProfile")
                .setFunctions(new MagnitudeScoringFunction("Rating", 2.0, new MagnitudeScoringParameters(1, 4))));
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
        SearchIndex index = new SearchIndex(indexName)
            .setFields(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(false));
        String expectedMessage = String.format("Found 0 key fields in index '%s'. "
            + "Each index must have exactly one key field.", indexName);

        try {
            client.createIndex(index);
            fail("createOrUpdateIndex did not throw an expected Exception");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains(expectedMessage), () -> String.format("Expected exception to contain "
                + "message '%s' but the message was '%s'.", expectedMessage, ex.getMessage()));
        } catch (Throwable throwable) {
            fail("Expected HttpResponseException to be thrown.", throwable);
        }
    }

    @Test
    public void getIndexReturnsCorrectDefinition() {
        SearchIndex index = createTestIndex(null);
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex createdIndex = client.getIndex(index.getName());
        assertObjectEquals(index, createdIndex, true, "etag");
    }

    @Test
    public void getIndexReturnsCorrectDefinitionWithResponse() {
        SearchIndex index = createTestIndex(null);
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        Response<SearchIndex> getIndexResponse = client.getIndexWithResponse(index.getName(), Context.NONE);
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
        SearchIndex indexToCreate = createTestIndex(null);

        // Create the resource in the search service
        SearchIndex originalIndex = client.createOrUpdateIndexWithResponse(indexToCreate, false, false, Context.NONE)
            .getValue();

        // Update the resource, the eTag will be changed
        SearchIndex updatedIndex = client.createOrUpdateIndexWithResponse(originalIndex
                .setCorsOptions(new CorsOptions(Collections.singletonList("https://test.com/"))), false, false,
            Context.NONE)
            .getValue();

        try {
            client.deleteIndexWithResponse(originalIndex, true, Context.NONE);
            fail("deleteFunc should have failed due to selected MatchConditions");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteIndexWithResponse(updatedIndex, true, Context.NONE);
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        SearchIndex index = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE)
            .getValue();

        client.deleteIndexWithResponse(index, true, Context.NONE);

        // Try to delete again and expect to fail
        try {
            client.deleteIndexWithResponse(index, true, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    @Test
    public void deleteIndexIsIdempotent() {
        SearchIndex index = new SearchIndex(HOTEL_INDEX_NAME)
            .setFields(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true));
        Response<Void> deleteResponse = client.deleteIndexWithResponse(index, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<SearchIndex> createResponse = client.createIndexWithResponse(index, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = client.deleteIndexWithResponse(index, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteIndexWithResponse(index, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteIndex() {
        SearchIndex index = createTestIndex(null);
        client.createIndex(index);
        client.deleteIndex(index.getName());

        assertThrows(HttpResponseException.class, () -> client.getIndex(index.getName()));
    }

    @Test
    public void canCreateAndListIndexes() {
        SearchIndex index1 = createTestIndex(null);
        mutateName(index1, "a" + index1.getName());
        SearchIndex index2 = createTestIndex(null);
        mutateName(index2, "b" + index2.getName());

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
        SearchIndex index1 = createTestIndex(null);
        mutateName(index1, "a" + index1.getName());
        SearchIndex index2 = createTestIndex(null);
        mutateName(index2, "b" + index2.getName());

        client.createIndex(index1);
        indexesToDelete.add(index1.getName());
        client.createIndex(index2);
        indexesToDelete.add(index2.getName());

        PagedIterable<String> selectedFieldListResponse = client.listIndexNames(Context.NONE);
        List<String> result = selectedFieldListResponse.stream().collect(Collectors.toList());

        result.forEach(Assertions::assertNotNull);

        assertEquals(2, result.size());
        assertEquals(result.get(0), index1.getName());
        assertEquals(result.get(1), index2.getName());
    }

    @Test
    public void canAddSynonymFieldProperty() {
        String synonymMapName = testResourceNamer.randomName("names", 32);
        SynonymMap synonymMap = new SynonymMap(synonymMapName).setSynonyms("hotel,motel");
        client.createSynonymMap(synonymMap);
        synonymMapsToDelete.add(synonymMap.getName());

        SearchIndex index = new SearchIndex(HOTEL_INDEX_NAME)
            .setFields(Arrays.asList(
                new SearchField("HotelId", SearchFieldDataType.STRING)
                    .setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING)
                    .setSynonymMapNames(synonymMapName)));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        List<String> actualSynonym = index.getFields().get(1).getSynonymMapNames();
        List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMapNames();
        assertEquals(actualSynonym, expectedSynonym);
    }

    @Test
    public void canUpdateSynonymFieldProperty() {
        String synonymMapName = testResourceNamer.randomName("names", 32);
        SynonymMap synonymMap = new SynonymMap(synonymMapName)
            .setSynonyms("hotel,motel");

        client.createSynonymMap(synonymMap);
        synonymMapsToDelete.add(synonymMap.getName());

        // Create an index
        SearchIndex index = createTestIndex(null);
        SearchField hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMapNames(synonymMapName);
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        // Update an existing index
        SearchIndex existingIndex = client.getIndex(index.getName());
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMapNames(Collections.emptyList());

        SearchIndex updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateIndexDefinition() {
        SearchIndex fullFeaturedIndex = createTestIndex(null);

        // Start out with no scoring profiles and different CORS options.
        SearchIndex initialIndex = createTestIndex(fullFeaturedIndex.getName());
        initialIndex.setScoringProfiles(new ArrayList<>())
            .setDefaultScoringProfile(null)
            .setCorsOptions(mutateCorsOptionsInIndex(initialIndex, Collections.singletonList("*")).getCorsOptions());

        SearchIndex index = client.createIndex(initialIndex);
        indexesToDelete.add(index.getName());

        // Now update the index.
        List<String> allowedOrigins = fullFeaturedIndex.getCorsOptions()
            .getAllowedOrigins();
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(mutateCorsOptionsInIndex(index, allowedOrigins).getCorsOptions());

        SearchIndex updatedIndex = client.createOrUpdateIndex(index);

        assertObjectEquals(fullFeaturedIndex, updatedIndex, true, "etag", "@odata.etag");

        // Modify the fields on an existing index
        SearchIndex existingIndex = client.getIndex(fullFeaturedIndex.getName());

        SynonymMap synonymMap = client.createSynonymMap(new SynonymMap(
            testResourceNamer.randomName("names", 32))
            .setSynonyms("hotel,motel")
        );
        synonymMapsToDelete.add(synonymMap.getName());

        SearchField tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setHidden(true)
            .setSearchAnalyzerName(LexicalAnalyzerName.WHITESPACE)
            .setSynonymMapNames(synonymMap.getName());

        SearchField hotelWebSiteField = new SearchField("HotelWebsite", SearchFieldDataType.STRING)
            .setSearchable(Boolean.TRUE)
            .setFilterable(Boolean.TRUE);
        existingIndex.getFields().add(hotelWebSiteField);

        SearchField hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setHidden(true);

        updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, Context.NONE).getValue();

        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateSuggesterWithNewIndexFields() {
        SearchIndex index = createTestIndex(null);
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex existingIndex = client.getIndex(index.getName());

        existingIndex.getFields().addAll(Arrays.asList(
            new SearchField("HotelAmenities", SearchFieldDataType.STRING),
            new SearchField("HotelRewards", SearchFieldDataType.STRING)));
        existingIndex.setSuggesters(new SearchSuggester("Suggestion", Arrays.asList("HotelAmenities", "HotelRewards")));

        SearchIndex updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, false, Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        SearchIndex index = createTestIndex(null);
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex existingIndex = client.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSuggesters(new SearchSuggester("Suggestion", Collections.singletonList(existingFieldName)));

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
        SearchIndex expected = createTestIndex(null);

        SearchIndex actual = client.createOrUpdateIndex(expected);
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        mutateName(expected, "hotel1");
        actual = client.createOrUpdateIndex(expected);
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        mutateName(expected, "hotel2");
        SearchIndex res = client.createOrUpdateIndex(expected);
        indexesToDelete.add(res.getName());
        assertEquals(expected.getName(), res.getName());
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponse() {
        SearchIndex expected = createTestIndex(null);

        SearchIndex actual = client.createOrUpdateIndexWithResponse(expected, false, false, Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        mutateName(expected, "hotel1");
        actual = client.createOrUpdateIndexWithResponse(expected, false, false, Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");

        mutateName(expected, "hotel2");
        Response<SearchIndex> createOrUpdateResponse = client.createOrUpdateIndexWithResponse(expected, false, false,
            Context.NONE);
        indexesToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        SearchIndex index = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, true, Context.NONE)
            .getValue();
        indexesToDelete.add(index.getName());

        assertFalse(CoreUtils.isNullOrEmpty(index.getETag()));
    }


    @Test
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource() {
        SearchIndex original = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original,
            Collections.singletonList("*")), false, false, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        SearchIndex original = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original,
            Collections.singletonList("*")), false, true, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged() {
        SearchIndex original = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        indexesToDelete.add(original.getName());

        SearchIndex updated = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original,
            Collections.singletonList("*")), false, true, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        try {
            client.createOrUpdateIndexWithResponse(original, false, true, Context.NONE);
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
        SearchIndex index = createTestIndex(null);
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndexStatistics indexStatistics = client.getIndexStatistics(index.getName());
        assertEquals(0, indexStatistics.getDocumentCount());
        assertEquals(0, indexStatistics.getStorageSize());
    }

    @Test
    public void canCreateAndGetIndexStatsWithResponse() {
        SearchIndex index = createTestIndex(null);
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        Response<SearchIndexStatistics> indexStatisticsResponse = client.getIndexStatisticsWithResponse(index.getName(),
            Context.NONE);
        assertEquals(0, indexStatisticsResponse.getValue().getDocumentCount());
        assertEquals(0, indexStatisticsResponse.getValue().getStorageSize());
    }

    void mutateName(SearchIndex updateIndex, String indexName) {
        try {
            Field updateField = updateIndex.getClass().getDeclaredField("name");
            updateField.setAccessible(true);
            updateField.set(updateIndex, indexName);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    SearchIndex mutateCorsOptionsInIndex(SearchIndex index, List<String> allowedOrigins) {
        CorsOptions updateCorsOptions = index.getCorsOptions();
        try {
            Field updateField = updateCorsOptions.getClass().getDeclaredField("allowedOrigins");
            updateField.setAccessible(true);
            updateField.set(updateCorsOptions, allowedOrigins);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
        return index;
    }

    SearchField getFieldByName(SearchIndex index, String name) {
        return index.getFields()
            .stream()
            .filter(f -> f.getName().equals(name))
            .findFirst().get();
    }
}
