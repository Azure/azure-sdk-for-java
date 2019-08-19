package com.azure.search.service.samples;

import com.azure.search.service.SearchServiceClient;
import com.azure.search.service.customization.SearchCredentials;
import com.azure.search.service.implementation.SearchServiceClientImpl;
import com.azure.search.service.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddNewIndexSample {
    public static void main(String[] args) {

        String searchServiceName = "searchServiceName";
        String apiAdminKey = "apiAdminKey";

        SearchCredentials credentials = new SearchCredentials(apiAdminKey);
        SearchServiceClient searchServiceClient = new SearchServiceClientImpl(credentials)
            .withSearchServiceName(searchServiceName);


        Index index = new Index();

        List<Field> fields = new ArrayList<>();
        fields.add(new Field()
            .withName("hotelId")
            .withType(DataType.EDM_STRING)
            .withKey(true)
            .withFilterable(true)
            .withSortable(true)
            .withFacetable(true)
        );
        fields.add(new Field()
            .withName("hotelName")
            .withType(DataType.EDM_STRING)
            .withSearchable(true)
            .withFilterable(true)
            .withSortable(true)
            .withFacetable(false)
        );
        fields.add(new Field()
            .withName("description")
            .withSearchable(true)
            .withType(DataType.EDM_STRING)
            .withAnalyzer(AnalyzerName.ENLUCENE)
        );

        List<Suggester> suggesters = new ArrayList<>();
        suggesters.add(new Suggester()
            .withName("sg")
            .withSourceFields(Arrays.asList("description", "hotelName"))
        );

//        List<ScoringProfile> scoringProfiles = new ArrayList<>();
//        scoringProfiles.add(new ScoringProfile()
//            .withName("nearest")
//            .withFunctionAggregation(ScoringFunctionAggregation.SUM)
//            .withFunctions(Arrays.asList(
//                new DistanceScoringFunction()
//                    .withFieldName("location")
//                    .withBoost(2.0)
//                    .with(new ScoringFunctionInterpolation()
//                        .withReferencePointParameter("myloc")
//                        .withBoostingDistance(100))
//            ))
//        )

        index.withName("testidx2")
            .withFields(fields)
            .withSuggesters(suggesters);

        searchServiceClient.indexes().create(index);
    }
}
