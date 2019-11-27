// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.IndexGetStatisticsResult;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.Suggester;
import com.azure.search.models.SynonymMap;
import com.azure.search.test.AccessConditionTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IndexManagementSyncTests extends IndexManagementTestBase {
    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<Index,
        AccessOptions,
        Index> createOrUpdateIndexFunc =
            (Index index, AccessOptions ac) ->
                createIndex(index, false, ac.getAccessCondition(), ac.getRequestOptions());

    private BiFunction<Index,
        AccessOptions,
        Index> createOrUpdateIndexWithResponseFunc =
            (Index index, AccessOptions ac) ->
                createIndexWithResponse(index, false, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Index> newIndexFunc = this::createTestIndex;

    private Function<Index, Index> mutateIndexFunc = this::mutateCorsOptionsInIndex;

    private BiConsumer<String, AccessOptions> deleteIndexFunc =
        (String name, AccessOptions ac) ->
            deleteIndex(name, ac.getAccessCondition(), ac.getRequestOptions());

    private void deleteIndex(String indexName,
                                   AccessCondition accessCondition,
                                   RequestOptions requestOptions) {
        client.deleteIndex(indexName,
            accessCondition,
            requestOptions);
    }

    private Index createIndex(Index index, boolean allowDowntime,
                              AccessCondition accessCondition,
                              RequestOptions requestOptions) {
        return client.createOrUpdateIndex(index, allowDowntime, accessCondition, requestOptions);
    }

    private Index createIndexWithResponse(Index index, boolean allowDowntime,
                                          AccessCondition accessCondition,
                                          RequestOptions requestOptions) {
        return client.createOrUpdateIndexWithResponse(index,
            allowDowntime,
            accessCondition,
            requestOptions,
            Context.NONE).getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    @Override
    public void createIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();
        Index createdIndex = client.createIndex(index);
        assertIndexesEqual(index, createdIndex);

        createdIndex = client.createIndex(index.setName("hotel1"), generateRequestOptions());
        assertIndexesEqual(index, createdIndex);

        Response<Index> createIndexResponse = client.createIndexWithResponse(index.setName("hotel2"), generateRequestOptions(), Context.NONE);
        assertIndexesEqual(index, createIndexResponse.getValue());
    }

    @Override
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
        Assert.assertNull(indexResponse.getCorsOptions().getMaxAgeInSeconds());
        Assert.assertEquals(ScoringFunctionAggregation.SUM, scoringProfile.getFunctionAggregation());
        Assert.assertNotNull(scoringProfile.getFunctions().get(0));
        Assert.assertEquals(ScoringFunctionInterpolation.LINEAR, scoringProfile.getFunctions().get(0).getInterpolation());
    }

    @Override
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
            Assert.fail("createIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
            Assert.assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Override
    public void getIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();
        client.createIndex(index);
        Index createdIndex = client.getIndex(index.getName());
        assertIndexesEqual(index, createdIndex);

        createdIndex = client.getIndex(index.getName(), generateRequestOptions());
        assertIndexesEqual(index, createdIndex);

        Response<Index> getIndexResponse = client.getIndexWithResponse(index.getName(), generateRequestOptions(), Context.NONE);
        assertIndexesEqual(index, getIndexResponse.getValue());
    }

    @Override
    public void getIndexThrowsOnNotFound() {
        try {
            client.getIndex("thisindexdoesnotexist");
            Assert.fail("getIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
            Assert.assertTrue(ex.getMessage().contains("No index with the name 'thisindexdoesnotexist' was found in the service"));
        }
    }

    @Override
    public void existsReturnsTrueForExistingIndex() {
        Index index = createTestIndex();
        client.createIndex(index);

        Assert.assertTrue(client.indexExists(index.getName()));
        Assert.assertTrue(client.indexExists(index.getName(), generateRequestOptions()));
        Assert.assertTrue(client.indexExistsWithResponse(index.getName(), generateRequestOptions(), Context.NONE).getValue());
    }

    @Override
    public void existsReturnsFalseForNonExistingIndex() {
        Assert.assertFalse(client.indexExists("invalidindex"));
    }

    @Override
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.deleteIfNotChangedWorksOnlyOnCurrentResource(
            deleteIndexFunc,
            newIndexFunc,
            createOrUpdateIndexFunc,
            HOTEL_INDEX_NAME);
    }

    @Override
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests act = new AccessConditionTests();

        act.deleteIfExistsWorksOnlyWhenResourceExists(
            deleteIndexFunc,
            createOrUpdateIndexFunc,
            newIndexFunc,
            HOTEL_INDEX_NAME);
    }

    @Override
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
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        Response<Index> createResponse = client.createIndexWithResponse(index, generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.CREATED.code(), createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

    @Override
    public void canCreateAndDeleteIndex() {
        Index index = createTestIndex();
        client.createIndex(index);
        client.deleteIndex(index.getName());
        Assert.assertFalse(client.indexExists(index.getName()));
    }

    @Override
    public void canCreateAndListIndexes() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1);
        client.createIndex(index2);

        PagedIterable<Index> actual = client.listIndexes();
        List<Index> result = actual.stream().collect(Collectors.toList());

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(index1.getName(), result.get(0).getName());
        Assert.assertEquals(index2.getName(), result.get(1).getName());

        actual = client.listIndexes("name", generateRequestOptions());
        result = actual.stream().collect(Collectors.toList());

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(index1.getName(), result.get(0).getName());
        Assert.assertEquals(index2.getName(), result.get(1).getName());

        PagedResponse<Index> listResponse = client.listIndexesWithResponse("name",
            generateRequestOptions(), Context.NONE);
        result = listResponse.getItems();

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(index1.getName(), result.get(0).getName());
        Assert.assertEquals(index2.getName(), result.get(1).getName());
    }

    @Override
    public void canListIndexesWithSelectedField() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1);
        client.createIndex(index2);

        PagedIterable<Index> selectedFieldListResponse = client.listIndexes("name", generateRequestOptions());
        List<Index> result = selectedFieldListResponse.stream().collect(Collectors.toList());

        result.forEach(res -> {
            Assert.assertNotNull(res.getName());
            Assert.assertNull(res.getFields());
            Assert.assertNull(res.getDefaultScoringProfile());
            Assert.assertNull(res.getCorsOptions());
            Assert.assertNull(res.getScoringProfiles());
            Assert.assertNull(res.getSuggesters());
            Assert.assertNull(res.getAnalyzers());
            Assert.assertNull(res.getTokenizers());
            Assert.assertNull(res.getTokenFilters());
            Assert.assertNull(res.getCharFilters());
        });

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(result.get(0).getName(), index1.getName());
        Assert.assertEquals(result.get(1).getName(), index2.getName());
    }

    @Override
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
        Assert.assertEquals(actualSynonym, expectedSynonym);
    }

    @Override
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

        Index updatedIndex = client.createOrUpdateIndex(existingIndex,
            true, new AccessCondition(), generateRequestOptions());
        assertIndexesEqual(existingIndex, updatedIndex);
    }

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

        assertIndexesEqual(fullFeaturedIndex, updatedIndex);

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

        updatedIndex = client.createOrUpdateIndex(existingIndex,
            true, new AccessCondition(), generateRequestOptions());
        assertIndexesEqual(existingIndex, updatedIndex);
    }

    @Override
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

        Index updatedIndex = client.createOrUpdateIndex(existingIndex,
            true, new AccessCondition(), generateRequestOptions());

        assertIndexesEqual(existingIndex, updatedIndex);
    }

    @Override
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index);

        Index existingIndex = client.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Collections.singletonList(existingFieldName))
        ));

        try {
            client.createOrUpdateIndex(existingIndex,
                true, new AccessCondition(), generateRequestOptions());
            Assert.fail("createOrUpdateIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) ex)
                .getResponse().getStatusCode());
            String expectedMessage = String.format("Fields that were already present in an index (%s) cannot be "
                + "referenced by a new suggester. Only new fields added in the same index update operation are allowed.", existingFieldName);
            Assert.assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Override
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExist() {
        Index expected = createTestIndex();

        Index actual = client.createOrUpdateIndex(expected);
        assertIndexesEqual(expected, actual);

        actual = client.createOrUpdateIndex(expected.setName("hotel1"),
            false, new AccessCondition(), generateRequestOptions());
        assertIndexesEqual(expected, actual);

        Response<Index> createOrUpdateResponse = client.createOrUpdateIndexWithResponse(expected.setName("hotel2"),
            false, new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
    }

    @Override
    public void createOrUpdateIndexIfNotExistsFailsOnExistingResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.createOrUpdateIfNotExistsFailsOnExistingResource(
            createOrUpdateIndexFunc,
            newIndexFunc,
            mutateIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateIndexFunc,
            newIndexFunc);
    }

    @Override
    public void createOrUpdateIndexWithResponseIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateIndexWithResponseFunc,
            newIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsSucceedsOnExistingResource(
            newIndexFunc,
            createOrUpdateIndexFunc,
            mutateIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfExistsFailsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsFailsOnNoResource(
            newIndexFunc,
            createOrUpdateIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchanged(
            newIndexFunc,
            createOrUpdateIndexFunc,
            mutateIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedFailsWhenResourceChanged(
            newIndexFunc,
            createOrUpdateIndexFunc,
            mutateIndexFunc);
    }

    @Override
    public void canCreateAndGetIndexStats() {
        Index index = createTestIndex();
        client.createOrUpdateIndex(index);
        IndexGetStatisticsResult indexStatistics = client.getIndexStatistics(index.getName());
        Assert.assertEquals(0, indexStatistics.getDocumentCount());
        Assert.assertEquals(0, indexStatistics.getStorageSize());

        indexStatistics = client.getIndexStatistics(index.getName(), generateRequestOptions());
        Assert.assertEquals(0, indexStatistics.getDocumentCount());
        Assert.assertEquals(0, indexStatistics.getStorageSize());

        Response<IndexGetStatisticsResult> indexStatisticsResponse = client.getIndexStatisticsWithResponse(index.getName(),
            generateRequestOptions(), Context.NONE);
        Assert.assertEquals(0, indexStatisticsResponse.getValue().getDocumentCount());
        Assert.assertEquals(0, indexStatisticsResponse.getValue().getStorageSize());


    }
}
