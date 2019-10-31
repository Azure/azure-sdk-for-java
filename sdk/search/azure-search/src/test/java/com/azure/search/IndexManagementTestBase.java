// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.AnalyzerName;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.DataType;
import com.azure.search.models.DistanceScoringFunction;
import com.azure.search.models.DistanceScoringParameters;
import com.azure.search.models.Field;
import com.azure.search.models.FreshnessScoringFunction;
import com.azure.search.models.FreshnessScoringParameters;
import com.azure.search.models.Index;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.Suggester;
import com.azure.search.models.TagScoringFunction;
import com.azure.search.models.TagScoringParameters;
import com.azure.search.models.TextWeights;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public abstract class IndexManagementTestBase extends SearchServiceTestBase {

    @Test
    public abstract void createIndexReturnsCorrectDefinition();

    @Test
    public abstract void createIndexReturnsCorrectDefaultValues();

    @Test
    public abstract void createIndexFailsWithUsefulMessageOnUserError();

    @Test
    public abstract void getIndexReturnsCorrectDefinition();

    @Test
    public abstract void getIndexThrowsOnNotFound();

    @Test
    public abstract void existsReturnsTrueForExistingIndex();

    @Test
    public abstract void existsReturnsFalseForNonExistingIndex();

    @Test
    public abstract void deleteIndexIfNotChangedWorksOnlyOnCurrentResource();

    @Test
    public abstract void deleteIndexIfExistsWorksOnlyWhenResourceExists();

    @Test
    public abstract void deleteIndexIsIdempotent();

    @Test
    public abstract void canCreateAndDeleteIndex();

    @Test
    public abstract void canCreateAndListIndexes();

    @Test
    public abstract void canListIndexesWithSelectedField();

    @Test
    public abstract void canAddSynonymFieldProperty();

    @Test
    public abstract void canUpdateSynonymFieldProperty();

    @Test
    public abstract void canUpdateIndexDefinition();

    @Test
    public abstract void canUpdateSuggesterWithNewIndexFields();

    @Test
    public abstract void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields();

    @Test
    public abstract void createOrUpdateIndexCreatesWhenIndexDoesNotExist();

    @Test
    public abstract void createOrUpdateIndexIfNotExistsFailsOnExistingResource();

    @Test
    public abstract void createOrUpdateIndexIfNotExistsSucceedsOnNoResource();

    @Test
    public abstract void createOrUpdateIndexIfExistsSucceedsOnExistingResource();

    @Test
    public abstract void createOrUpdateIndexIfExistsFailsOnNoResource();

    @Test
    public abstract void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged();

    @Test
    public abstract void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged();

    protected void assertIndexesEqual(Index expected, Index actual) {
        expected.setETag("none");
        actual.setETag("none");

        // we ignore defaults as when properties are not set they are returned from the service with
        // default values
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
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
                .setAllowedOrigins("http://tempuri.org", "http://localhost:80")
                .setMaxAgeInSeconds(60L))
            .setSuggesters(Arrays.asList(new Suggester()
                .setName("FancySuggester")
                .setSourceFields(Arrays.asList("HotelName"))));
    }

    protected Index mutateCorsOptionsInIndex(Index index) {
        index.setCorsOptions(index.getCorsOptions().setAllowedOrigins("*"));
        return index;
    }

    protected Field getFieldByName(Index index, String name) {
        return index.getFields()
            .stream()
            .filter(f -> f.getName().equals(name))
            .findFirst().get();
    }

    protected void addFieldToIndex(Index index, Field field) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(index.getFields());
        fields.add(field);

        index.setFields(fields);
    }
}
