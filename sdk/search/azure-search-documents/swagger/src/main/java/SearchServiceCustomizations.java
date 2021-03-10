// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

import java.lang.reflect.Modifier;

public class SearchServiceCustomizations extends Customization {
    // Common modifier combinations
    private static final int PUBLIC_ABSTRACT = Modifier.PUBLIC | Modifier.ABSTRACT;
    private static final int PUBLIC_FINAL = Modifier.PUBLIC | Modifier.FINAL;

    // Packages
    private static final String IMPLEMENTATION_MODELS = "com.azure.search.documents.indexes.implementation.models";
    private static final String MODELS = "com.azure.search.documents.indexes.models";

    // Classes
    private static final String MAGNITUDE_SCORING_PARAMETERS = "MagnitudeScoringParameters";
    private static final String SCORING_FUNCTION = "ScoringFunction";
    private static final String SEARCH_FIELD_DATA_TYPE = "SearchFieldDataType";

    private static final String SIMILARITY_ALGORITHM = "SimilarityAlgorithm";
    private static final String BM_25_SIMILARITY_ALGORITHM = "BM25SimilarityAlgorithm";
    private static final String CLASSIC_SIMILARITY_ALGORITHM = "ClassicSimilarityAlgorithm";

    private static final String DATA_CHANGE_DETECTION_POLICY = "DataChangeDetectionPolicy";
    private static final String HIGH_WATER_MARK_CHANGE_DETECTION_POLICY = "HighWaterMarkChangeDetectionPolicy";
    private static final String SQL_INTEGRATED_CHANGE_TRACKING_POLICY = "SqlIntegratedChangeTrackingPolicy";

    private static final String DATA_DELETION_DETECTION_POLICY = "DataDeletionDetectionPolicy";
    private static final String SOFT_DELETE_COLUMN_DELETION_DETECTION_POLICY = "SoftDeleteColumnDeletionDetectionPolicy";

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeImplementationModelsPackage(libraryCustomization.getPackage(IMPLEMENTATION_MODELS));
        customizeModelsPackage(libraryCustomization.getPackage(MODELS));
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        // Change ScoringFunction to an abstract class.
        changeClassModifier(packageCustomization.getClass(SCORING_FUNCTION), PUBLIC_ABSTRACT);
        customizeMagnitudeScoringParameters(packageCustomization.getClass(MAGNITUDE_SCORING_PARAMETERS));
        customizeSearchFieldDataType(packageCustomization.getClass(SEARCH_FIELD_DATA_TYPE));

        customizeSimilarityAlgorithm(packageCustomization.getClass(SIMILARITY_ALGORITHM));
        // Change BM25SimilarityAlgorithm to a final class.
        changeClassModifier(packageCustomization.getClass(BM_25_SIMILARITY_ALGORITHM), PUBLIC_FINAL);
        // Change ClassicSimilarityAlgorithm to a final class.
        changeClassModifier(packageCustomization.getClass(CLASSIC_SIMILARITY_ALGORITHM), PUBLIC_FINAL);

        // Change DataChangeDetectionPolicy to an abstract class.
        changeClassModifier(packageCustomization.getClass(DATA_CHANGE_DETECTION_POLICY), PUBLIC_ABSTRACT);
        // Change HighWaterMarkChangeDetectionPolicy to a final class.
        changeClassModifier(packageCustomization.getClass(HIGH_WATER_MARK_CHANGE_DETECTION_POLICY), PUBLIC_FINAL);
        // Change SqlIntegratedChangeTrackingPolicy to a final class.
        changeClassModifier(packageCustomization.getClass(SQL_INTEGRATED_CHANGE_TRACKING_POLICY), PUBLIC_FINAL);

        // Change DataDeletionDetectionPolicy to an abstract class.
        changeClassModifier(packageCustomization.getClass(DATA_DELETION_DETECTION_POLICY), PUBLIC_ABSTRACT);
        // Change SoftDeleteColumnDeletionDetectionPolicy to a final class.
        changeClassModifier(packageCustomization.getClass(SOFT_DELETE_COLUMN_DELETION_DETECTION_POLICY), PUBLIC_FINAL);
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public static SearchFieldDataType collection(SearchFieldDataType dataType) {\n" +
            "    return fromString(String.format(\"Collection(%s)\", dataType.toString()));\n" +
            "}")
            .addAnnotation("@JsonCreator")
            .getJavadoc()
            .setDescription("Returns a collection of a specific SearchFieldDataType")
            .setParam("dataType", "the corresponding SearchFieldDataType")
            .setReturn("a Collection of the corresponding SearchFieldDataType");
    }

    private void customizeMagnitudeScoringParameters(ClassCustomization classCustomization) {
        classCustomization.getMethod("isShouldBoostBeyondRangeByConstant")
            .rename("shouldBoostBeyondRangeByConstant");
    }

    private void customizeSimilarityAlgorithm(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, Modifier.PUBLIC | Modifier.ABSTRACT);
        classCustomization.removeAnnotation("@JsonTypeName");
        classCustomization.addAnnotation("@JsonTypeName(\"Similarity\")");
    }

    private static void changeClassModifier(ClassCustomization classCustomization, int modifier) {
        classCustomization.setModifier(modifier);
    }
}
