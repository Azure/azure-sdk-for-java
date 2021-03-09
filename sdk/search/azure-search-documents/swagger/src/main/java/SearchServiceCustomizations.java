// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

import java.lang.reflect.Modifier;

public class SearchServiceCustomizations extends Customization {
    // Packages
    private static final String IMPLEMENTATION_MODELS = "com.azure.search.documents.indexes.implementation.models";
    private static final String MODELS = "com.azure.search.documents.indexes.models";

    // Classes
    private static final String MAGNITUDE_SCORING_PARAMETERS = "MagnitudeScoringParameters";
    private static final String SCORING_FUNCTION = "ScoringFunction";
    private static final String SEARCH_FIELD_DATA_TYPE = "SearchFieldDataType";

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeImplementationModelsPackage(libraryCustomization.getPackage(IMPLEMENTATION_MODELS));
        customizeModelsPackage(libraryCustomization.getPackage(MODELS));
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeScoringFunction(packageCustomization.getClass(SCORING_FUNCTION));
        customizeMagnitudeScoringParameters(packageCustomization.getClass(MAGNITUDE_SCORING_PARAMETERS));
        customizeSearchFieldDataType(packageCustomization.getClass(SEARCH_FIELD_DATA_TYPE));
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

    private void customizeScoringFunction(ClassCustomization classCustomization) {
        int publicAbstractModifier = Modifier.PUBLIC | Modifier.ABSTRACT;
        classCustomization.setModifier(publicAbstractModifier);
    }

    private void customizeMagnitudeScoringParameters(ClassCustomization classCustomization) {
        classCustomization.getMethod("isShouldBoostBeyondRangeByConstant")
            .rename("shouldBoostBeyondRangeByConstant");
    }
}
