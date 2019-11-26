// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.Suggester;
import com.azure.search.models.SynonymMap;
import com.azure.search.test.AccessConditionAsyncTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class IndexManagementAsyncTests extends IndexManagementTestBase {
    private SearchServiceAsyncClient client;

    // commonly used lambda definitions
    private BiFunction<Index,
        AccessOptions,
        Mono<Index>> createOrUpdateAsyncFunc =
            (Index index, AccessOptions ac) ->
                createIndex(index, false, ac.getAccessCondition(), ac.getRequestOptions());

    private BiFunction<Index,
        AccessOptions,
        Mono<Index>> createOrUpdateWithResponseAsyncFunc =
            (Index index, AccessOptions ac) ->
                createIndexWithResponse(index, false, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Index> newIndexFunc =
        () -> createTestIndex();

    private Function<Index, Index> changeIndexFunc =
        (Index index) -> mutateCorsOptionsInIndex(index);

    private BiFunction<String, AccessOptions, Mono<Void>> deleteIndexAsyncFunc =
        (String name, AccessOptions ac) ->
            deleteIndex(name, ac.getAccessCondition(), ac.getRequestOptions());

    private Mono<Void> deleteIndex(String indexName,
                                     AccessCondition accessCondition,
                                     RequestOptions requestOptions) {
        return client.deleteIndex(indexName,
            accessCondition,
            requestOptions);
    }

    private Mono<Index> createIndex(Index index, boolean allowDowntime,
                                    AccessCondition accessCondition,
                                    RequestOptions requestOptions) {
        return client.createOrUpdateIndex(index, allowDowntime, accessCondition, requestOptions);
    }

    private Mono<Index> createIndexWithResponse(Index index, boolean allowDowntime,
                                                          AccessCondition accessCondition,
                                                          RequestOptions requestOptions) {
        return client.createOrUpdateIndexWithResponse(index, allowDowntime, accessCondition, requestOptions)
            .map(Response::getValue);
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Override
    public void createIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();
        StepVerifier
            .create(client.createIndex(index))
            .assertNext(createdIndex -> {
                assertIndexesEqual(index, createdIndex);
            })
            .verifyComplete();

        StepVerifier
            .create(client.createIndex(index.setName("hotel1"), generateRequestOptions()))
            .assertNext(createdIndex -> {
                assertIndexesEqual(index, createdIndex);
            })
            .verifyComplete();

        StepVerifier
            .create(client.createIndexWithResponse(index.setName("hotel2"), generateRequestOptions()))
            .assertNext(createdIndex -> {
                assertIndexesEqual(index, createdIndex.getValue());
            })
            .verifyComplete();
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

        StepVerifier
            .create(client.createIndex(index))
            .assertNext(indexResponse -> {
                ScoringProfile scoringProfile = indexResponse.getScoringProfiles().get(0);
                Assert.assertNull(indexResponse.getCorsOptions().getMaxAgeInSeconds());
                Assert.assertEquals(ScoringFunctionAggregation.SUM, scoringProfile.getFunctionAggregation());
                Assert.assertNotNull(scoringProfile.getFunctions().get(0));
                Assert.assertEquals(ScoringFunctionInterpolation.LINEAR, scoringProfile.getFunctions().get(0).getInterpolation());
            })
            .verifyComplete();
    }

    @Override
    public void createIndexFailsWithUsefulMessageOnUserError() {
        Index index = new Index()
            .setName(HOTEL_INDEX_NAME)
            .setFields(Collections.singletonList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(false)
            ));

        String expectedMessage = String.format("The request is invalid. Details: index : Found 0 key fields in index '%s'. "
            + "Each index must have exactly one key field.", HOTEL_INDEX_NAME);

        StepVerifier
            .create(client.createIndex(index))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(expectedMessage));
            });
    }

    @Override
    public void getIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();
        client.createIndex(index).block();

        StepVerifier
            .create(client.getIndex(index.getName()))
            .assertNext(res -> {
                assertIndexesEqual(index, res);
            })
            .verifyComplete();

        StepVerifier
            .create(client.getIndex(index.getName(), generateRequestOptions()))
            .assertNext(res -> {
                assertIndexesEqual(index, res);
            })
            .verifyComplete();

        StepVerifier
            .create(client.getIndexWithResponse(index.getName(), generateRequestOptions()))
            .assertNext(res -> {
                assertIndexesEqual(index, res.getValue());
            })
            .verifyComplete();
    }

    @Override
    public void getIndexThrowsOnNotFound() {
        StepVerifier
            .create(client.getIndex("thisindexdoesnotexist"))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains("No index with the name 'thisindexdoesnotexist' was found in the service"));
            });
    }

    @Override
    public void existsReturnsTrueForExistingIndex() {
        Index index = createTestIndex();
        client.createIndex(index).block();

        StepVerifier
            .create(client.indexExists(index.getName()))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();

        StepVerifier
            .create(client.indexExists(index.getName(), generateRequestOptions()))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();

        StepVerifier
            .create(client.indexExistsWithResponse(index.getName(), generateRequestOptions()))
            .assertNext(res -> Assert.assertTrue(res.getValue()))
            .verifyComplete();
    }

    @Override
    public void existsReturnsFalseForNonExistingIndex() {
        StepVerifier
            .create(client.indexExists("invalidindex"))
            .assertNext(res -> Assert.assertFalse(res))
            .verifyComplete();
    }

    @Override
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfNotChangedWorksOnlyOnCurrentResourceAsync(
            deleteIndexAsyncFunc,
            newIndexFunc,
            createOrUpdateAsyncFunc,
            changeIndexFunc,
            HOTEL_INDEX_NAME);
    }

    @Override
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfExistsWorksOnlyWhenResourceExistsAsync(
            deleteIndexAsyncFunc,
            createOrUpdateAsyncFunc,
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
        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions()))
            .assertNext(indexResponse -> {
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.createIndexWithResponse(index, generateRequestOptions()))
            .assertNext(indexResponse -> {
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();

        // Delete the same index twice
        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions()))
            .assertNext(indexResponse -> {
                Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), new AccessCondition(), generateRequestOptions()))
            .assertNext(indexResponse -> {
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();
    }

    @Override
    public void canCreateAndDeleteIndex() {
        Index index = createTestIndex();
        client.createIndex(index).block();
        client.deleteIndex(index.getName()).block();

        StepVerifier
            .create(client.indexExists(index.getName()))
            .assertNext(response -> {
                Assert.assertFalse(response);
            })
            .verifyComplete();
    }

    @Override
    public void canCreateAndListIndexes() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1).block();
        client.createIndex(index2).block();

        PagedFlux<Index> listResponse = client.listIndexes();

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(index1.getName(), result.get(0).getName());
                Assert.assertEquals(index2.getName(), result.get(1).getName());
            })
            .verifyComplete();

        listResponse = client.listIndexes("name", generateRequestOptions());

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(index1.getName(), result.get(0).getName());
                Assert.assertEquals(index2.getName(), result.get(1).getName());
            })
            .verifyComplete();

        StepVerifier
            .create(client.listIndexesWithResponse("name", generateRequestOptions()))
            .assertNext(result -> {
                Assert.assertEquals(2, result.getItems().size());
                Assert.assertEquals(index1.getName(), result.getValue().get(0).getName());
                Assert.assertEquals(index2.getName(), result.getValue().get(1).getName());
            })
            .verifyComplete();
    }

    @Override
    public void canListIndexesWithSelectedField() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1).block();
        client.createIndex(index2).block();

        PagedFlux<Index> selectedFieldListResponse = client.listIndexes("name", generateRequestOptions());

        StepVerifier
            .create(selectedFieldListResponse.collectList())
            .assertNext(result -> {
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
            })
            .verifyComplete();

        StepVerifier
            .create(selectedFieldListResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(index1.getName(), result.get(0).getName());
                Assert.assertEquals(index2.getName(), result.get(1).getName());
            })
            .verifyComplete();
    }

    @Override
    public void canAddSynonymFieldProperty() {
        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap().setName(synonymMapName).setSynonyms("hotel,motel");
        client.createSynonymMap(synonymMap).block();

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

        StepVerifier
            .create(client.createIndex(index))
            .assertNext(createdIndex -> {
                List<String> actualSynonym = index.getFields().get(1).getSynonymMaps();
                List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMaps();
                Assert.assertEquals(actualSynonym, expectedSynonym);
            })
            .verifyComplete();
    }

    @Override
    public void canUpdateSynonymFieldProperty() {
        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap()
            .setName(synonymMapName)
            .setSynonyms("hotel,motel");

        client.createSynonymMap(synonymMap).block();

        // Create an index
        Index index = createTestIndex();
        Field hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMaps(Collections.singletonList(synonymMapName));
        client.createIndex(index).block();

        // Update an existing index
        Index existingIndex = client.getIndex(index.getName()).block();
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMaps(Collections.<String>emptyList());

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex,
                true, new AccessCondition(), generateRequestOptions()))
            .assertNext(res  -> {
                assertIndexesEqual(existingIndex, res);
            })
            .verifyComplete();
    }

    public void canUpdateIndexDefinition() {
        Index fullFeaturedIndex = createTestIndex();

        // Start out with no scoring profiles and different CORS options.
        Index initialIndex = createTestIndex();
        initialIndex.setName(fullFeaturedIndex.getName())
            .setScoringProfiles(new ArrayList<>())
            .setDefaultScoringProfile(null)
            .setCorsOptions(initialIndex.getCorsOptions().setAllowedOrigins("*"));

        Index index = client.createIndex(initialIndex).block();

        // Now update the index.
        String[] allowedOrigins = fullFeaturedIndex.getCorsOptions()
            .getAllowedOrigins()
            .toArray(new String[0]);
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(index.getCorsOptions().setAllowedOrigins(allowedOrigins));

        StepVerifier
            .create(client.createOrUpdateIndex(index))
            .assertNext(res -> assertIndexesEqual(fullFeaturedIndex, res))
            .verifyComplete();

        // Modify the fields on an existing index
        Index existingIndex = client.getIndex(fullFeaturedIndex.getName()).block();

        SynonymMap synonymMap = client.createSynonymMap(new SynonymMap()
            .setName("names")
            .setSynonyms("hotel,motel")
        ).block();

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

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex,
                true, new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> assertIndexesEqual(existingIndex, res))
            .verifyComplete();
    }

    @Override
    public void canUpdateSuggesterWithNewIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index).block();

        Index existingIndex = client.getIndex(index.getName()).block();

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

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex,
                true, new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> assertIndexesEqual(existingIndex, res))
            .verifyComplete();
    }

    @Override
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index).block();

        Index existingIndex = client.getIndex(index.getName()).block();
        String existingFieldName = "Category";
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Collections.singletonList(existingFieldName))
        ));

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex,
                true, new AccessCondition(), generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error)
                    .getResponse().getStatusCode());
                String expectedMessage = String.format("Fields that were already present in an index (%s) cannot be "
                    + "referenced by a new suggester. Only new fields added in the same index update operation are allowed.", existingFieldName);
                Assert.assertTrue(error.getMessage().contains(expectedMessage));
            });
    }

    @Override
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExist() {
        Index expected = createTestIndex();

        StepVerifier
            .create(client.createOrUpdateIndex(expected))
            .assertNext(res -> assertIndexesEqual(expected, res))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateIndex(expected.setName("hotel1"),
                false, new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> assertIndexesEqual(expected, res))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateIndexWithResponse(expected.setName("hotel2"),
                false, new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode()))
            .verifyComplete();
    }

    @Override
    public void createOrUpdateIndexIfNotExistsFailsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsFailsOnExistingResourceAsync(
            createOrUpdateAsyncFunc,
            newIndexFunc,
            changeIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateAsyncFunc,
            newIndexFunc);
    }

    @Override
    public void createOrUpdateIndexWithResponseIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateWithResponseAsyncFunc,
            newIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource()
        throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsSucceedsOnExistingResourceAsync(
            newIndexFunc,
            createOrUpdateAsyncFunc,
            changeIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfExistsFailsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsFailsOnNoResourceAsync(
            newIndexFunc,
            createOrUpdateAsyncFunc);
    }

    @Override
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged()
        throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchangedAsync(
            newIndexFunc,
            createOrUpdateAsyncFunc,
            changeIndexFunc);
    }

    @Override
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged()
        throws NoSuchFieldException, IllegalAccessException {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedFailsWhenResourceChangedAsync(
            newIndexFunc,
            createOrUpdateAsyncFunc,
            changeIndexFunc);
    }

    @Override
    public void canCreateAndGetIndexStats() {
        Index testIndex = createTestIndex();
        Index index = client.createOrUpdateIndex(testIndex).block();

        StepVerifier
            .create(client.getIndexStatistics(index.getName()))
            .assertNext(stats -> {
                Assert.assertEquals(0, stats.getDocumentCount());
                Assert.assertEquals(0, stats.getStorageSize());
            })
            .verifyComplete();

        StepVerifier
            .create(client.getIndexStatistics(index.getName(), generateRequestOptions()))
            .assertNext(stats -> {
                Assert.assertEquals(0, stats.getDocumentCount());
                Assert.assertEquals(0, stats.getStorageSize());
            })
            .verifyComplete();

        StepVerifier
            .create(client.getIndexStatisticsWithResponse(index.getName(), generateRequestOptions()))
            .assertNext(stats -> {
                Assert.assertEquals(0, stats.getValue().getDocumentCount());
                Assert.assertEquals(0, stats.getValue().getStorageSize());
            })
            .verifyComplete();

    }
}
