// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.search.models.AnalyzerName;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.DistanceScoringParameters;
import com.azure.search.models.DistanceScoringFunction;
import com.azure.search.models.TagScoringParameters;
import com.azure.search.models.TagScoringFunction;
import com.azure.search.models.TextWeights;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.Suggester;
import com.azure.search.models.FreshnessScoringParameters;
import com.azure.search.models.FreshnessScoringFunction;
import com.azure.search.test.environment.models.ModelComparer;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;

public abstract class IndexManagementTestBase extends SearchServiceTestBase {
    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    protected SearchServiceClientBuilder getSearchServiceClientBuilder() {
        if (!interceptorManager.isPlaybackMode()) {
            return new SearchServiceClientBuilder()
                .serviceName(searchServiceName)
                .searchDnsSuffix(searchDnsSuffix)
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .credential(apiKeyCredentials)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .addPolicy(new HttpLoggingPolicy(
                    new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));
        } else {
            return new SearchServiceClientBuilder()
                .serviceName("searchServiceName")
                .searchDnsSuffix(searchDnsSuffix)
                .httpClient(interceptorManager.getPlaybackClient());
        }
    }

    public abstract void createIndexReturnsCorrectDefinition();

    @Test
    public abstract void createIndexReturnsCorrectDefaultValues();

    @Test
    public abstract void createIndexFailsWithUsefulMessageOnUserError();

    public abstract void getIndexReturnsCorrectDefinition();

    public abstract void getIndexThrowsOnNotFound();

    public abstract void existsReturnsTrueForExistingIndex();

    public abstract void existsReturnsFalseForNonExistingIndex();

    public abstract void deleteIndexIfNotChangedWorksOnlyOnCurrentResource();

    public abstract void deleteIndexIfExistsWorksOnlyWhenResourceExists();

    public abstract void deleteIndexIsIdempotent();

    public abstract void canCreateAndDeleteIndex();

    protected static boolean assertIndexesEqual(Index expected, Index actual) {
        return Objects.equals(expected.getName(), actual.getName())
            && ModelComparer.collectionEquals(expected.getFields(), actual.getFields())
            && ModelComparer.collectionEquals(expected.getScoringProfiles(), actual.getScoringProfiles())
            && Objects.equals(expected.getDefaultScoringProfile(), actual.getDefaultScoringProfile())
            && Objects.equals(expected.getCorsOptions(), actual.getCorsOptions())
            && ModelComparer.collectionEquals(expected.getSuggesters(), actual.getSuggesters())
            && ModelComparer.collectionEquals(expected.getAnalyzers(), actual.getAnalyzers())
            && ModelComparer.collectionEquals(expected.getTokenizers(), actual.getTokenizers())
            && ModelComparer.collectionEquals(expected.getTokenFilters(), actual.getTokenFilters())
            && ModelComparer.collectionEquals(expected.getCharFilters(), actual.getCharFilters());
    }

    protected Index createTestIndex() {

        Map<String, Double> weights = new HashMap<String, Double>();
        weights.put("Description", 1.5);
        weights.put("Category", 2.0);
        return new Index()
            .setName("hotels")
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.TRUE)
                    .setSearchable(Boolean.FALSE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("HotelName")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.FALSE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Description")
                    .setType(DataType.EDM_STRING)
                    .setKey(Boolean.FALSE)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setAnalyzer(AnalyzerName.EN_LUCENE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("DescriptionFr")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setAnalyzer(AnalyzerName.FR_LUCENE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Description_Custom")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.FALSE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.FALSE)
                    .setSearchAnalyzer(AnalyzerName.STOP)
                    .setIndexAnalyzer(AnalyzerName.STOP)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Category")
                    .setType(DataType.EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Tags")
                    .setType(DataType.COLLECTION_EDM_STRING)
                    .setSearchable(Boolean.TRUE)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.FALSE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("ParkingIncluded")
                    .setType(DataType.EDM_BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("SmokingAllowed")
                    .setType(DataType.EDM_BOOLEAN)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("LastRenovationDate")
                    .setType(DataType.EDM_DATE_TIME_OFFSET)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Rating")
                    .setType(DataType.EDM_INT32)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Address")
                    .setType(DataType.EDM_COMPLEX_TYPE)
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("StreetAddress")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("City")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("StateProvince")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("Country")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("PostalCode")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                        )
                    ),
                new Field()
                    .setName("Location")
                    .setType(DataType.EDM_GEOGRAPHY_POINT)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.FALSE)
                    .setRetrievable(Boolean.TRUE)
                    .setRetrievable(Boolean.TRUE),
                new Field()
                    .setName("Rooms")
                    .setType(DataType.COLLECTION_EDM_COMPLEX_TYPE)
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("Description")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.EN_LUCENE),
                        new Field()
                            .setName("DescriptionFr")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                            .setAnalyzer(AnalyzerName.FR_LUCENE),
                        new Field()
                            .setName("Type")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("BaseRate")
                            .setType(DataType.EDM_DOUBLE)
                            .setKey(Boolean.FALSE)
                            .setSearchable(Boolean.FALSE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("BedOptions")
                            .setType(DataType.EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("SleepsCount")
                            .setType(DataType.EDM_INT32)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("SmokingAllowed")
                            .setType(DataType.EDM_BOOLEAN)
                            .setFilterable(Boolean.TRUE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE),
                        new Field()
                            .setName("Tags")
                            .setType(DataType.COLLECTION_EDM_STRING)
                            .setSearchable(Boolean.TRUE)
                            .setFilterable(Boolean.TRUE)
                            .setSortable(Boolean.FALSE)
                            .setFacetable(Boolean.TRUE)
                            .setRetrievable(Boolean.TRUE)
                        )
                    ),
                new Field()
                    .setName("TotalGuests")
                    .setType(DataType.EDM_INT64)
                    .setFilterable(Boolean.TRUE)
                    .setSortable(Boolean.TRUE)
                    .setFacetable(Boolean.TRUE)
                    .setRetrievable(Boolean.FALSE
                    ),
                new Field()
                    .setName("ProfitMargin")
                    .setType(DataType.EDM_DOUBLE)
                )
            )
            .setScoringProfiles(Arrays.asList(
                new ScoringProfile()
                    .setName("MyProfile")
                    .setFunctionAggregation(ScoringFunctionAggregation.AVERAGE)
                    .setFunctions(Arrays.asList(
                        new MagnitudeScoringFunction()
                            .setParameters(new MagnitudeScoringParameters()
                                .setBoostingRangeStart(1)
                                .setBoostingRangeEnd(4)
                                .setShouldBoostBeyondRangeByConstant(true))
                            .setFieldName("Rating")
                            .setBoost(2.0)
                            .setInterpolation(ScoringFunctionInterpolation.CONSTANT),
                        new DistanceScoringFunction()
                            .setParameters(new DistanceScoringParameters()
                                .setBoostingDistance(5)
                                .setReferencePointParameter("Loc"))
                            .setFieldName("Location")
                            .setBoost(1.5)
                            .setInterpolation(ScoringFunctionInterpolation.LINEAR),
                        new FreshnessScoringFunction()
                            .setParameters(new FreshnessScoringParameters()
                                .setBoostingDuration(Duration.ofDays(365)))
                            .setFieldName("LastRenovationDate")
                            .setBoost(1.1)
                            .setInterpolation(ScoringFunctionInterpolation.LOGARITHMIC)
                    ))
                    .setTextWeights(new TextWeights()
                        .setWeights(weights)),
                new ScoringProfile()
                    .setName("ProfileTwo")
                    .setFunctionAggregation(ScoringFunctionAggregation.MAXIMUM)
                    .setFunctions(Collections.singletonList(
                        new TagScoringFunction()
                            .setParameters(new TagScoringParameters().setTagsParameter("MyTags"))
                            .setFieldName("Tags")
                            .setBoost(1.5)
                            .setInterpolation(ScoringFunctionInterpolation.LINEAR)
                    )),
                new ScoringProfile()
                    .setName("ProfileThree")
                    .setFunctionAggregation(ScoringFunctionAggregation.MINIMUM)
                    .setFunctions(Collections.singletonList(
                        new MagnitudeScoringFunction()
                            .setParameters(new MagnitudeScoringParameters()
                                .setBoostingRangeStart(0)
                                .setBoostingRangeEnd(10)
                                .setShouldBoostBeyondRangeByConstant(false))
                            .setFieldName("Rating")
                            .setBoost(3.0)
                            .setInterpolation(ScoringFunctionInterpolation.QUADRATIC)
                    )),
                new ScoringProfile()
                    .setName("ProfileFour")
                    .setFunctionAggregation(ScoringFunctionAggregation.FIRST_MATCHING)
                    .setFunctions(Collections.singletonList(
                        new MagnitudeScoringFunction()
                            .setParameters(new MagnitudeScoringParameters()
                                .setBoostingRangeStart(1)
                                .setBoostingRangeEnd(5)
                                .setShouldBoostBeyondRangeByConstant(false))
                            .setFieldName("Rating")
                            .setBoost(3.14)
                            .setInterpolation(ScoringFunctionInterpolation.CONSTANT)
                    ))
            ))
            .setDefaultScoringProfile("MyProfile")
            .setCorsOptions(new CorsOptions()
                .setAllowedOrigins(Arrays.asList("http://tempuri.org", "http://localhost:80"))
                .setMaxAgeInSeconds(60L))
            .setSuggesters(Arrays.asList(new Suggester()
                .setName("FancySuggester")
                .setSourceFields(Arrays.asList("HotelName"))));
    }
}
