// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.GetIndexStatisticsResult;
import com.azure.search.models.Index;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.Suggester;
import com.azure.search.models.SynonymMap;
import com.azure.search.test.AccessConditionTests;
import com.azure.search.test.AccessOptions;
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

import static com.azure.search.TestHelpers.generateIfNotChangedAccessCondition;
import static com.azure.search.TestHelpers.getETag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class IndexManagementSyncTests extends SearchServiceTestBase {
    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<Index, AccessOptions, Index> createOrUpdateIndexFunc = (Index index, AccessOptions ac) ->
        createOrUpdateIndex(index, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Index> newIndexFunc = this::createTestIndex;

    private Function<Index, Index> mutateIndexFunc = this::mutateCorsOptionsInIndex;

    private BiConsumer<String, AccessOptions> deleteIndexFunc =
        (String name, AccessOptions ac) ->
            client.deleteIndexWithResponse(name, ac.getAccessCondition(), ac.getRequestOptions(), Context.NONE);

    private Index createOrUpdateIndex(Index index, AccessCondition accessCondition, RequestOptions requestOptions) {
        return client.createOrUpdateIndexWithResponse(index, false, accessCondition, requestOptions, Context.NONE)
            .getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    @Test
    public void createIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();

        Index createdIndex = client.createIndex(index);
        TestHelpers.assertIndexesEqual(index, createdIndex);
    }

    @Test
    public void createIndexReturnsCorrectDefinitionWithResponse() {
        Index index = createTestIndex();

        Response<Index> createIndexResponse = client.createIndexWithResponse(index.setName("hotel2"),
            generateRequestOptions(), Context.NONE);
        TestHelpers.assertIndexesEqual(index, createIndexResponse.getValue());
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
            assertEquals(HttpResponseException.class, ex.getClass());
            assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void getIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();
        client.createIndex(index);

        Index createdIndex = client.getIndex(index.getName());
        TestHelpers.assertIndexesEqual(index, createdIndex);
    }

    @Test
    public void getIndexReturnsCorrectDefinitionWithResponse() {
        Index index = createTestIndex();
        client.createIndex(index);

        Response<Index> getIndexResponse = client.getIndexWithResponse(index.getName(), generateRequestOptions(),
            Context.NONE);
        TestHelpers.assertIndexesEqual(index, getIndexResponse.getValue());
    }

    @Test
    public void getIndexThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getIndex("thisindexdoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "No index with the name 'thisindexdoesnotexist' was found in the service"
        );
    }

    @Test
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        Index indexToCreate = createTestIndex();
        AccessOptions accessOptions = new AccessOptions(null);

        // Create the resource in the search service
        Index originalIndex = createOrUpdateIndexFunc.apply(indexToCreate, accessOptions);

        // Get the eTag for the newly created resource
        String eTagStale = getETag(originalIndex);

        // Update the resource, the eTag will be changed
        Index updatedIndex = createOrUpdateIndexFunc.apply(originalIndex
            .setCorsOptions(new CorsOptions().setAllowedOrigins("https://test.com/")), accessOptions);

        try {
            accessOptions = new AccessOptions(generateIfNotChangedAccessCondition(eTagStale));
            deleteIndexFunc.accept(HOTEL_INDEX_NAME, accessOptions);
            fail("deleteFunc should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            assertEquals(HttpResponseException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }

        // Get the new eTag
        String eTagCurrent = getETag(updatedIndex);
        accessOptions = new AccessOptions(generateIfNotChangedAccessCondition(eTagCurrent));

        // Delete should succeed
        deleteIndexFunc.accept(HOTEL_INDEX_NAME, accessOptions);
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteIndexFunc, createOrUpdateIndexFunc,
            newIndexFunc, HOTEL_INDEX_NAME);
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
        Response<Void> deleteResponse = client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        Response<Index> createResponse = client.createIndexWithResponse(index, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteIndex() {
        Index index = createTestIndex();
        client.createIndex(index);
        client.deleteIndex(index.getName());

        assertThrows(HttpResponseException.class, () -> client.getIndex(index.getName()));
    }

    @Test
    public void canCreateAndListIndexes() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1);
        client.createIndex(index2);

        PagedIterable<Index> actual = client.listIndexes();
        List<Index> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(index1.getName(), result.get(0).getName());
        assertEquals(index2.getName(), result.get(1).getName());
    }

    @Test
    public void canListIndexesWithSelectedField() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1);
        client.createIndex(index2);

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
        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap().setName(synonymMapName).setSynonyms("hotel,motel");
        client.createSynonymMap(synonymMap);

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

        client.createSynonymMap(synonymMap);

        // Create an index
        Index index = createTestIndex();
        Field hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMaps(Collections.singletonList(synonymMapName));
        client.createIndex(index);

        // Update an existing index
        Index existingIndex = client.getIndex(index.getName());
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMaps(Collections.emptyList());

        Index updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, new AccessCondition(), generateRequestOptions(), Context.NONE).getValue();
        TestHelpers.assertIndexesEqual(existingIndex, updatedIndex);
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

        // Now update the index.
        String[] allowedOrigins = fullFeaturedIndex.getCorsOptions()
            .getAllowedOrigins()
            .toArray(new String[0]);
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(index.getCorsOptions().setAllowedOrigins(allowedOrigins));

        Index updatedIndex = client.createOrUpdateIndex(index);

        TestHelpers.assertIndexesEqual(fullFeaturedIndex, updatedIndex);

        // Modify the fields on an existing index
        Index existingIndex = client.getIndex(fullFeaturedIndex.getName());

        SynonymMap synonymMap = client.createSynonymMap(new SynonymMap()
            .setName("names")
            .setSynonyms("hotel,motel")
        );

        Field tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setRetrievable(false)
            .setSearchAnalyzer(AnalyzerName.WHITESPACE.toString())
            .setSynonymMaps(Collections.singletonList(synonymMap.getName()));

        Field hotelWebSiteField = new Field()
            .setName("HotelWebsite")
            .setType(DataType.EDM_STRING)
            .setSearchable(Boolean.TRUE)
            .setFilterable(Boolean.TRUE);
        addFieldToIndex(existingIndex, hotelWebSiteField);

        Field hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setRetrievable(false);

        updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex,
            true, new AccessCondition(), generateRequestOptions(), Context.NONE).getValue();
        TestHelpers.assertIndexesEqual(existingIndex, updatedIndex);
    }

    @Test
    public void canUpdateSuggesterWithNewIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index);

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
            true, new AccessCondition(), generateRequestOptions(), Context.NONE).getValue();

        TestHelpers.assertIndexesEqual(existingIndex, updatedIndex);
    }

    @Test
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index);

        Index existingIndex = client.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Collections.singletonList(existingFieldName))
        ));

        assertHttpResponseException(
            () -> client.createOrUpdateIndex(existingIndex),
            HttpResponseStatus.BAD_REQUEST,
            String.format("Fields that were already present in an index (%s) cannot be "
                    + "referenced by a new suggester. Only new fields added in the same index update operation are allowed.",
                existingFieldName)
        );
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExist() {
        Index expected = createTestIndex();

        Index actual = client.createOrUpdateIndex(expected);
        TestHelpers.assertIndexesEqual(expected, actual);

        actual = client.createOrUpdateIndex(expected.setName("hotel1"));
        TestHelpers.assertIndexesEqual(expected, actual);

        Index res = client.createOrUpdateIndex(expected.setName("hotel2"));
        assertEquals(expected.getName(), res.getName());
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponse() {
        Index expected = createTestIndex();

        Index actual = client.createOrUpdateIndexWithResponse(expected, false, new AccessCondition(),
            generateRequestOptions(), Context.NONE).getValue();
        TestHelpers.assertIndexesEqual(expected, actual);

        actual = client.createOrUpdateIndexWithResponse(expected.setName("hotel1"),
            false, new AccessCondition(), generateRequestOptions(), Context.NONE).getValue();
        TestHelpers.assertIndexesEqual(expected, actual);

        Response<Index> createOrUpdateResponse = client.createOrUpdateIndexWithResponse(expected.setName("hotel2"),
            false, new AccessCondition(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateIndexIfNotExistsFailsOnExistingResource() {
        AccessConditionTests.createOrUpdateIfNotExistsFailsOnExistingResource(createOrUpdateIndexFunc, newIndexFunc,
            mutateIndexFunc);
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
    public void createOrUpdateIndexIfExistsFailsOnNoResource() {
        AccessConditionTests.updateIfExistsFailsOnNoResource(newIndexFunc, createOrUpdateIndexFunc);
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
        Index index = createTestIndex();
        client.createOrUpdateIndex(index);
        GetIndexStatisticsResult indexStatistics = client.getIndexStatistics(index.getName());
        assertEquals(0, indexStatistics.getDocumentCount());
        assertEquals(0, indexStatistics.getStorageSize());
    }

    @Test
    public void canCreateAndGetIndexStatsWithResponse() {
        Index index = createTestIndex();
        client.createOrUpdateIndex(index);

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
