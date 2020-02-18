package com.azure.search;

import com.azure.core.util.CoreUtils;
import com.azure.search.models.Analyzer;
import com.azure.search.models.CharFilter;
import com.azure.search.models.CognitiveServicesAccount;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.DataChangeDetectionPolicy;
import com.azure.search.models.DataContainer;
import com.azure.search.models.DataDeletionDetectionPolicy;
import com.azure.search.models.DataSource;
import com.azure.search.models.Field;
import com.azure.search.models.FieldMapping;
import com.azure.search.models.FieldMappingFunction;
import com.azure.search.models.HighWaterMarkChangeDetectionPolicy;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexingParameters;
import com.azure.search.models.IndexingSchedule;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.Skill;
import com.azure.search.models.Skillset;
import com.azure.search.models.SoftDeleteColumnDeletionDetectionPolicy;
import com.azure.search.models.Suggester;
import com.azure.search.models.TokenFilter;
import com.azure.search.models.Tokenizer;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.HotelAddress;
import com.azure.search.test.environment.models.HotelRoom;
import com.azure.search.test.environment.models.LoudHotel;
import com.azure.search.test.environment.models.ModelWithPrimitiveCollections;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class contains helper methods for running Azure Search tests.
 */
public final class TestHelpers {
    public static boolean areDataSourcesEqual(DataSource actual, DataSource expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getType(), expected.getType())
            && Objects.equals(actual.getCredentials().getConnectionString(),
                expected.getCredentials().getConnectionString())
            && areDataContainersEqual(actual.getContainer(), expected.getContainer())
            && Objects.equals(actual.getDescription(), expected.getDescription())
            && areChangeDetectionPoliciesEqual(actual.getDataChangeDetectionPolicy(),
                expected.getDataChangeDetectionPolicy())
            && areDeletionDetectionPoliciesEqual(actual.getDataDeletionDetectionPolicy(),
                expected.getDataDeletionDetectionPolicy());
    }

    private static boolean areDataContainersEqual(DataContainer actual, DataContainer expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getQuery(), expected.getQuery());
    }

    private static boolean areChangeDetectionPoliciesEqual(DataChangeDetectionPolicy actual,
        DataChangeDetectionPolicy expected) {
        if (actual == null) {
            return expected == null;
        }

        Class<? extends DataChangeDetectionPolicy> actualClass = actual.getClass();
        Class<? extends DataChangeDetectionPolicy> expectedClass = expected.getClass();

        if (actualClass != expectedClass) {
            return false;
        }

        if (actualClass == HighWaterMarkChangeDetectionPolicy.class) {
            return Objects.equals(((HighWaterMarkChangeDetectionPolicy) actual).getHighWaterMarkColumnName(),
                ((HighWaterMarkChangeDetectionPolicy) expected).getHighWaterMarkColumnName());
        } else {
            return true;
        }
    }

    private static boolean areDeletionDetectionPoliciesEqual(DataDeletionDetectionPolicy actual,
        DataDeletionDetectionPolicy expected) {
        if (actual == null) {
            return expected == null;
        }

        Class<? extends DataDeletionDetectionPolicy> actualClass = actual.getClass();
        Class<? extends DataDeletionDetectionPolicy> expectedClass = expected.getClass();

        if (actualClass != expectedClass) {
            return false;
        }

        if (actualClass == SoftDeleteColumnDeletionDetectionPolicy.class) {
            SoftDeleteColumnDeletionDetectionPolicy softActual = (SoftDeleteColumnDeletionDetectionPolicy) actual;
            SoftDeleteColumnDeletionDetectionPolicy softExpected = (SoftDeleteColumnDeletionDetectionPolicy) expected;

            return Objects.equals(softActual.getSoftDeleteColumnName(), softExpected.getSoftDeleteColumnName())
                && Objects.equals(softActual.getSoftDeleteMarkerValue(), softExpected.getSoftDeleteMarkerValue());
        } else {
            return true;
        }
    }

    public static boolean areIndexersEqual(Indexer actual, Indexer expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getDescription(), expected.getDescription())
            && Objects.equals(actual.getDataSourceName(), expected.getDataSourceName())
            && Objects.equals(actual.getSkillsetName(), expected.getSkillsetName())
            && Objects.equals(actual.getTargetIndexName(), expected.getTargetIndexName())
            && areIndexingSchedulesEqual(actual.getSchedule(), expected.getSchedule())
            && areIndexingParametersEqual(actual.getParameters(), expected.getParameters())
            && areFieldMappingListsEqual(actual.getFieldMappings(), expected.getFieldMappings())
            && areFieldMappingListsEqual(actual.getOutputFieldMappings(), expected.getOutputFieldMappings())
            && Objects.equals(actual.isDisabled(), expected.isDisabled());
    }

    private static boolean areIndexingSchedulesEqual(IndexingSchedule actual, IndexingSchedule expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getStartTime(), expected.getStartTime())
            && Objects.equals(actual.getInterval(), expected.getInterval());
    }

    private static boolean areIndexingParametersEqual(IndexingParameters actual, IndexingParameters expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getBatchSize(), expected.getBatchSize())
            && Objects.equals(actual.getConfiguration(), expected.getConfiguration())
            && Objects.equals(actual.getMaxFailedItems(), expected.getMaxFailedItems())
            && Objects.equals(actual.getMaxFailedItemsPerBatch(), expected.getMaxFailedItemsPerBatch())
            && Objects.equals(actual.isBase64EncodeKeys(), expected.isBase64EncodeKeys());
    }

    private static boolean areFieldMappingListsEqual(List<FieldMapping> actual, List<FieldMapping> expected) {
        if (actual == null) {
            return expected == null;
        }

        if (actual.size() != expected.size()) {
            return false;
        }

        for (int i = 0; i < actual.size(); i++) {
            if (!areFieldMappingsEqual(actual.get(i), expected.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean areFieldMappingsEqual(FieldMapping actual, FieldMapping expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getSourceFieldName(), expected.getSourceFieldName())
            && Objects.equals(actual.getTargetFieldName(), expected.getTargetFieldName())
            && areFieldMappingFunctionsEqual(actual.getMappingFunction(), expected.getMappingFunction());
    }

    private static boolean areFieldMappingFunctionsEqual(FieldMappingFunction actual, FieldMappingFunction expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getParameters(), expected.getParameters());
    }

    public static boolean areHotelsEqual(Hotel actual, Hotel expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.hotelId(), expected.hotelId())
            && Objects.equals(actual.hotelName(), expected.hotelName())
            && Objects.equals(actual.description(), expected.description())
            && Objects.equals(actual.descriptionFr(), expected.descriptionFr())
            && Objects.equals(actual.category(), expected.category())
            && Objects.equals(actual.tags(), expected.tags())
            && Objects.equals(actual.parkingIncluded(), expected.parkingIncluded())
            && Objects.equals(actual.smokingAllowed(), expected.smokingAllowed())
            && Objects.equals(actual.lastRenovationDate(), expected.lastRenovationDate())
            && Objects.equals(actual.rating(), expected.rating())
            && Objects.equals(actual.location(), expected.location())
            && areHotelAddressesEqual(actual.address(), expected.address())
            && areHotelRoomListsEqual(actual.rooms(), expected.rooms());
    }

    public static boolean areLoudHotelsEqual(LoudHotel actual, LoudHotel expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.hotelId(), expected.hotelId())
            && Objects.equals(actual.hotelName(), expected.hotelName())
            && Objects.equals(actual.description(), expected.description())
            && Objects.equals(actual.descriptionFrench(), expected.descriptionFrench())
            && Objects.equals(actual.category(), expected.category())
            && Objects.equals(actual.tags(), expected.tags())
            && Objects.equals(actual.parkingIncluded(), expected.parkingIncluded())
            && Objects.equals(actual.smokingAllowed(), expected.smokingAllowed())
            && Objects.equals(actual.lastRenovationDate(), expected.lastRenovationDate())
            && Objects.equals(actual.rating(), expected.rating())
            && Objects.equals(actual.location(), expected.location())
            && areHotelAddressesEqual(actual.address(), expected.address())
            && areHotelRoomListsEqual(actual.rooms(), expected.rooms());
    }

    private static boolean areHotelAddressesEqual(HotelAddress actual, HotelAddress expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.streetAddress(), expected.streetAddress())
            && Objects.equals(actual.city(), expected.city())
            && Objects.equals(actual.stateProvince(), expected.stateProvince())
            && Objects.equals(actual.country(), expected.country())
            && Objects.equals(actual.postalCode(), expected.postalCode());
    }

    private static boolean areHotelRoomListsEqual(List<HotelRoom> actual, List<HotelRoom> expected) {
        if (CoreUtils.isNullOrEmpty(actual)) {
            return CoreUtils.isNullOrEmpty(expected);
        }

        if (actual.size() != expected.size()) {
            return false;
        }

        for (int i = 0; i < actual.size(); i++) {
            if (!areHotelRoomsEqual(actual.get(i), expected.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean areHotelRoomsEqual(HotelRoom actual, HotelRoom expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.description(), expected.description())
            && Objects.equals(actual.descriptionFr(), expected.descriptionFr())
            && Objects.equals(actual.type(), expected.type())
            && Objects.equals(actual.baseRate(), expected.baseRate())
            && Objects.equals(actual.bedOptions(), expected.bedOptions())
            && Objects.equals(actual.sleepsCount(), expected.sleepsCount())
            && Objects.equals(actual.smokingAllowed(), expected.smokingAllowed())
            && Objects.equals(actual.tags(), expected.tags());
    }

    public static boolean areModelsWithPrimitivesEqual(ModelWithPrimitiveCollections actual,
        ModelWithPrimitiveCollections expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.key(), expected.key())
            && Arrays.equals(actual.bools(), expected.bools())
            && Arrays.equals(actual.dates(), expected.dates())
            && Arrays.equals(actual.doubles(), expected.doubles())
            && Arrays.equals(actual.ints(), expected.ints())
            && Arrays.equals(actual.longs(), expected.longs())
            && Arrays.equals(actual.points(), expected.points())
            && Arrays.equals(actual.strings(), expected.strings());
    }

    public static boolean areIndexesEqual(Index actual, Index expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getDefaultScoringProfile(), expected.getDefaultScoringProfile())


        /*
         * Options to control Cross-Origin Resource Sharing (CORS) for the index.
         */
        @JsonProperty(value = "corsOptions")
        private CorsOptions corsOptions;

        /*
         * The suggesters for the index.
         */
        @JsonProperty(value = "suggesters")
        private List<Suggester> suggesters;

        /*
         * The analyzers for the index.
         */
        @JsonProperty(value = "analyzers")
        private List<Analyzer> analyzers;

        /*
         * The tokenizers for the index.
         */
        @JsonProperty(value = "tokenizers")
        private List<Tokenizer> tokenizers;

        /*
         * The token filters for the index.
         */
        @JsonProperty(value = "tokenFilters")
        private List<TokenFilter> tokenFilters;

        /*
         * The character filters for the index.
         */
        @JsonProperty(value = "charFilters")
        private List<CharFilter> charFilters;

        /*
         * The fields of the index.
         */
        @JsonProperty(value = "fields", required = true)
        private List<Field> fields;

        /*
         * The scoring profiles for the index.
         */
        @JsonProperty(value = "scoringProfiles")
        private List<ScoringProfile> scoringProfiles;
    }

    public static boolean areSkillsetsEqual(Skillset actual, Skillset expected) {
        if (actual == null) {
            return expected == null;
        }

        return Objects.equals(actual.getName(), expected.getName())
            && Objects.equals(actual.getDescription(), expected.getDescription())


        /*
         * A list of skills in the skillset.
         */
        @JsonProperty(value = "skills", required = true)
        private List<Skill> skills;

        /*
         * Details about cognitive services to be used when running skills.
         */
        @JsonProperty(value = "cognitiveServices")
        private CognitiveServicesAccount cognitiveServicesAccount;
    }

    private static boolean areSkillListsEqual(List<Skill> actual, List<Skill> expected) {

    }

    private static boolean areSkillsEqual(Skill actual, Skill expected) {

    }

    private static boolean areCognitiveServiceAccountsEqual(CognitiveServicesAccount actual,
        CognitiveServicesAccount expected) {
        if (actual == null) {
            return  expected == null;
        }

        return Objects.equals(actual.getDescription(), expected.getDescription());
    }
}
